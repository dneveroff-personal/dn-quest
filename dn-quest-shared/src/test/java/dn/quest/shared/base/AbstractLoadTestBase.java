package dn.quest.shared.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Базовый класс для нагрузочных тестов с расширенными метриками
 * и аналитикой производительности
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractLoadTestBase {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @LocalServerPort
    protected int port;

    protected String baseUrl;
    protected ExecutorService executorService;
    protected MemoryMXBean memoryBean;

    // Метрики производительности
    protected AtomicLong totalRequests = new AtomicLong(0);
    protected AtomicLong successfulRequests = new AtomicLong(0);
    protected AtomicLong failedRequests = new AtomicLong(0);
    protected AtomicLong totalResponseTime = new AtomicLong(0);
    protected AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
    protected AtomicLong maxResponseTime = new AtomicLong(0);
    protected List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

    // Метрики использования ресурсов
    protected AtomicLong initialMemoryUsage = new AtomicLong(0);
    protected AtomicLong peakMemoryUsage = new AtomicLong(0);
    protected AtomicLong finalMemoryUsage = new AtomicLong(0);

    // Пороговые значения для тестов
    protected static final double DEFAULT_SUCCESS_RATE_THRESHOLD = 95.0;
    protected static final long DEFAULT_AVG_RESPONSE_TIME_THRESHOLD = 1000; // ms
    protected static final long DEFAULT_MAX_RESPONSE_TIME_THRESHOLD = 5000; // ms
    protected static final long DEFAULT_MEMORY_USAGE_THRESHOLD = 100 * 1024 * 1024; // 100 MB

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        executorService = Executors.newFixedThreadPool(getThreadPoolSize());
        memoryBean = ManagementFactory.getMemoryMXBean();
        
        // Сброс метрик
        resetMetrics();
        
        // Измерение начального состояния памяти
        System.gc();
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        initialMemoryUsage.set(heapMemory.getUsed());
        
        log.info("Load test initialized - URL: {}, Thread pool size: {}", baseUrl, getThreadPoolSize());
    }

    @AfterEach
    void tearDown() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            
            // Измерение финального состояния памяти
            System.gc();
            MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
            finalMemoryUsage.set(heapMemory.getUsed());
            
            log.info("Load test completed - Final memory usage: {} MB", 
                    finalMemoryUsage.get() / (1024.0 * 1024.0));
                    
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while shutting down executor service", e);
        }
    }

    /**
     * Сброс всех метрик
     */
    protected void resetMetrics() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        totalResponseTime.set(0);
        minResponseTime.set(Long.MAX_VALUE);
        maxResponseTime.set(0);
        responseTimes.clear();
        peakMemoryUsage.set(0);
    }

    /**
     * Получение размера пула потоков для тестов
     */
    protected int getThreadPoolSize() {
        return 50; // Значение по умолчанию
    }

    /**
     * Выполнение нагрузочного теста
     */
    protected LoadTestResult executeLoadTest(LoadTestConfig config) throws Exception {
        log.info("Starting load test - Threads: {}, Requests per thread: {}, Duration: {}ms", 
                config.getNumberOfThreads(), config.getRequestsPerThread(), config.getDuration());

        CountDownLatch latch = new CountDownLatch(config.getNumberOfThreads());
        Instant startTime = Instant.now();

        for (int i = 0; i < config.getNumberOfThreads(); i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    executeThreadLoad(threadId, config, latch);
                } catch (Exception e) {
                    log.error("Error in thread {}", threadId, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Ожидание завершения всех потоков
        boolean completed = latch.await(config.getDuration(), TimeUnit.MILLISECONDS);
        Instant endTime = Instant.now();

        // Обновление пикового использования памяти
        updatePeakMemoryUsage();

        // Генерация результатов
        return generateLoadTestResult(startTime, endTime, completed, config);
    }

    /**
     * Выполнение нагрузки для одного потока
     */
    private void executeThreadLoad(int threadId, LoadTestConfig config, CountDownLatch latch) {
        for (int j = 0; j < config.getRequestsPerThread(); j++) {
            try {
                long requestStart = System.currentTimeMillis();
                
                // Выполнение запроса
                ResponseEntity<String> response = executeRequest(threadId, j);
                
                long requestEnd = System.currentTimeMillis();
                long responseTime = requestEnd - requestStart;
                
                // Обновление метрик
                updateMetrics(response, responseTime);
                
                // Пауза между запросами
                if (config.getDelayBetweenRequests() > 0) {
                    Thread.sleep(config.getDelayBetweenRequests());
                }
                
            } catch (Exception e) {
                log.warn("Request failed in thread {} - Request {}", threadId, j, e);
                failedRequests.incrementAndGet();
                totalRequests.incrementAndGet();
            }
        }
    }

    /**
     * Выполнение одного запроса (переопределяется в конкретных тестах)
     */
    protected abstract ResponseEntity<String> executeRequest(int threadId, int requestId);

    /**
     * Обновление метрик после выполнения запроса
     */
    private void updateMetrics(ResponseEntity<String> response, long responseTime) {
        totalRequests.incrementAndGet();
        
        synchronized (responseTimes) {
            responseTimes.add(responseTime);
        }
        
        totalResponseTime.addAndGet(responseTime);
        
        // Обновление минимального времени ответа
        long currentMin = minResponseTime.get();
        while (responseTime < currentMin && !minResponseTime.compareAndSet(currentMin, responseTime)) {
            currentMin = minResponseTime.get();
        }
        
        // Обновление максимального времени ответа
        long currentMax = maxResponseTime.get();
        while (responseTime > currentMax && !maxResponseTime.compareAndSet(currentMax, responseTime)) {
            currentMax = maxResponseTime.get();
        }
        
        if (response.getStatusCode().is2xxSuccessful()) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }
        
        // Обновление пикового использования памяти
        updatePeakMemoryUsage();
    }

    /**
     * Обновление пикового использования памяти
     */
    private void updatePeakMemoryUsage() {
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        long currentUsage = heapMemory.getUsed();
        
        long currentPeak = peakMemoryUsage.get();
        while (currentUsage > currentPeak && !peakMemoryUsage.compareAndSet(currentPeak, currentUsage)) {
            currentPeak = peakMemoryUsage.get();
        }
    }

    /**
     * Генерация результатов нагрузочного теста
     */
    private LoadTestResult generateLoadTestResult(Instant startTime, Instant endTime, 
                                                 boolean completed, LoadTestConfig config) {
        long totalRequestsValue = totalRequests.get();
        long successfulRequestsValue = successfulRequests.get();
        long failedRequestsValue = failedRequests.get();
        
        double successRate = totalRequestsValue > 0 ? 
                (double) successfulRequestsValue / totalRequestsValue * 100 : 0.0;
        
        double averageResponseTime = totalRequestsValue > 0 ? 
                (double) totalResponseTime.get() / totalRequestsValue : 0.0;
        
        long duration = Duration.between(startTime, endTime).toMillis();
        double requestsPerSecond = duration > 0 ? (totalRequestsValue * 1000.0) / duration : 0.0;
        
        // Расчет процентилей
        Map<Integer, Long> percentiles = calculatePercentiles();
        
        return LoadTestResult.builder()
                .completed(completed)
                .totalRequests(totalRequestsValue)
                .successfulRequests(successfulRequestsValue)
                .failedRequests(failedRequestsValue)
                .successRate(successRate)
                .averageResponseTime(averageResponseTime)
                .minResponseTime(minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get())
                .maxResponseTime(maxResponseTime.get())
                .percentiles(percentiles)
                .duration(duration)
                .requestsPerSecond(requestsPerSecond)
                .initialMemoryUsage(initialMemoryUsage.get())
                .peakMemoryUsage(peakMemoryUsage.get())
                .finalMemoryUsage(finalMemoryUsage.get())
                .config(config)
                .build();
    }

    /**
     * Расчет процентилей времени ответа
     */
    private Map<Integer, Long> calculatePercentiles() {
        if (responseTimes.isEmpty()) {
            return Collections.emptyMap();
        }
        
        List<Long> sortedTimes = responseTimes.stream()
                .sorted()
                .collect(Collectors.toList());
        
        Map<Integer, Long> percentiles = new HashMap<>();
        int[] percentileValues = {50, 75, 90, 95, 99};
        
        for (int percentile : percentileValues) {
            int index = (int) Math.ceil(percentile / 100.0 * sortedTimes.size()) - 1;
            index = Math.max(0, Math.min(index, sortedTimes.size() - 1));
            percentiles.put(percentile, sortedTimes.get(index));
        }
        
        return percentiles;
    }

    /**
     * Проверка результатов теста на соответствие пороговым значениям
     */
    protected void validateLoadTestResults(LoadTestResult result) {
        log.info("Validating load test results...");
        
        if (result.getSuccessRate() < getSuccessRateThreshold()) {
            throw new AssertionError(String.format(
                    "Success rate %.2f%% is below threshold %.2f%%", 
                    result.getSuccessRate(), getSuccessRateThreshold()));
        }
        
        if (result.getAverageResponseTime() > getAverageResponseTimeThreshold()) {
            throw new AssertionError(String.format(
                    "Average response time %.2fms is above threshold %dms", 
                    result.getAverageResponseTime(), getAverageResponseTimeThreshold()));
        }
        
        if (result.getMaxResponseTime() > getMaxResponseTimeThreshold()) {
            throw new AssertionError(String.format(
                    "Max response time %dms is above threshold %dms", 
                    result.getMaxResponseTime(), getMaxResponseTimeThreshold()));
        }
        
        long memoryUsed = result.getPeakMemoryUsage() - result.getInitialMemoryUsage();
        if (memoryUsed > getMemoryUsageThreshold()) {
            throw new AssertionError(String.format(
                    "Memory usage %.2fMB is above threshold %.2fMB", 
                    memoryUsed / (1024.0 * 1024.0), 
                    getMemoryUsageThreshold() / (1024.0 * 1024.0)));
        }
        
        log.info("Load test validation passed!");
    }

    // Методы для настройки пороговых значений (переопределяются в конкретных тестах)
    protected double getSuccessRateThreshold() {
        return DEFAULT_SUCCESS_RATE_THRESHOLD;
    }

    protected long getAverageResponseTimeThreshold() {
        return DEFAULT_AVG_RESPONSE_TIME_THRESHOLD;
    }

    protected long getMaxResponseTimeThreshold() {
        return DEFAULT_MAX_RESPONSE_TIME_THRESHOLD;
    }

    protected long getMemoryUsageThreshold() {
        return DEFAULT_MEMORY_USAGE_THRESHOLD;
    }

    /**
     * Конфигурация нагрузочного теста
     */
    public static class LoadTestConfig {
        private final int numberOfThreads;
        private final int requestsPerThread;
        private final long duration;
        private final long delayBetweenRequests;

        public LoadTestConfig(int numberOfThreads, int requestsPerThread, long duration, long delayBetweenRequests) {
            this.numberOfThreads = numberOfThreads;
            this.requestsPerThread = requestsPerThread;
            this.duration = duration;
            this.delayBetweenRequests = delayBetweenRequests;
        }

        public int getNumberOfThreads() { return numberOfThreads; }
        public int getRequestsPerThread() { return requestsPerThread; }
        public long getDuration() { return duration; }
        public long getDelayBetweenRequests() { return delayBetweenRequests; }
    }

    /**
     * Результаты нагрузочного теста
     */
    public static class LoadTestResult {
        private final boolean completed;
        private final long totalRequests;
        private final long successfulRequests;
        private final long failedRequests;
        private final double successRate;
        private final double averageResponseTime;
        private final long minResponseTime;
        private final long maxResponseTime;
        private final Map<Integer, Long> percentiles;
        private final long duration;
        private final double requestsPerSecond;
        private final long initialMemoryUsage;
        private final long peakMemoryUsage;
        private final long finalMemoryUsage;
        private final LoadTestConfig config;

        private LoadTestResult(Builder builder) {
            this.completed = builder.completed;
            this.totalRequests = builder.totalRequests;
            this.successfulRequests = builder.successfulRequests;
            this.failedRequests = builder.failedRequests;
            this.successRate = builder.successRate;
            this.averageResponseTime = builder.averageResponseTime;
            this.minResponseTime = builder.minResponseTime;
            this.maxResponseTime = builder.maxResponseTime;
            this.percentiles = builder.percentiles;
            this.duration = builder.duration;
            this.requestsPerSecond = builder.requestsPerSecond;
            this.initialMemoryUsage = builder.initialMemoryUsage;
            this.peakMemoryUsage = builder.peakMemoryUsage;
            this.finalMemoryUsage = builder.finalMemoryUsage;
            this.config = builder.config;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public boolean isCompleted() { return completed; }
        public long getTotalRequests() { return totalRequests; }
        public long getSuccessfulRequests() { return successfulRequests; }
        public long getFailedRequests() { return failedRequests; }
        public double getSuccessRate() { return successRate; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public long getMinResponseTime() { return minResponseTime; }
        public long getMaxResponseTime() { return maxResponseTime; }
        public Map<Integer, Long> getPercentiles() { return percentiles; }
        public long getDuration() { return duration; }
        public double getRequestsPerSecond() { return requestsPerSecond; }
        public long getInitialMemoryUsage() { return initialMemoryUsage; }
        public long getPeakMemoryUsage() { return peakMemoryUsage; }
        public long getFinalMemoryUsage() { return finalMemoryUsage; }
        public LoadTestConfig getConfig() { return config; }

        public static class Builder {
            private boolean completed;
            private long totalRequests;
            private long successfulRequests;
            private long failedRequests;
            private double successRate;
            private double averageResponseTime;
            private long minResponseTime;
            private long maxResponseTime;
            private Map<Integer, Long> percentiles;
            private long duration;
            private double requestsPerSecond;
            private long initialMemoryUsage;
            private long peakMemoryUsage;
            private long finalMemoryUsage;
            private LoadTestConfig config;

            public Builder completed(boolean completed) { this.completed = completed; return this; }
            public Builder totalRequests(long totalRequests) { this.totalRequests = totalRequests; return this; }
            public Builder successfulRequests(long successfulRequests) { this.successfulRequests = successfulRequests; return this; }
            public Builder failedRequests(long failedRequests) { this.failedRequests = failedRequests; return this; }
            public Builder successRate(double successRate) { this.successRate = successRate; return this; }
            public Builder averageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; return this; }
            public Builder minResponseTime(long minResponseTime) { this.minResponseTime = minResponseTime; return this; }
            public Builder maxResponseTime(long maxResponseTime) { this.maxResponseTime = maxResponseTime; return this; }
            public Builder percentiles(Map<Integer, Long> percentiles) { this.percentiles = percentiles; return this; }
            public Builder duration(long duration) { this.duration = duration; return this; }
            public Builder requestsPerSecond(double requestsPerSecond) { this.requestsPerSecond = requestsPerSecond; return this; }
            public Builder initialMemoryUsage(long initialMemoryUsage) { this.initialMemoryUsage = initialMemoryUsage; return this; }
            public Builder peakMemoryUsage(long peakMemoryUsage) { this.peakMemoryUsage = peakMemoryUsage; return this; }
            public Builder finalMemoryUsage(long finalMemoryUsage) { this.finalMemoryUsage = finalMemoryUsage; return this; }
            public Builder config(LoadTestConfig config) { this.config = config; return this; }

            public LoadTestResult build() {
                return new LoadTestResult(this);
            }
        }
    }
}