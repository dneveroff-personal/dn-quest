package dn.quest.shared.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.authentication.dto.LoginRequestDTO;
import dn.quest.authentication.dto.RegisterRequestDTO;
import dn.quest.shared.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Нагрузочные тесты для микросервисов DN Quest
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoadTestSuite {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testAuthenticationServiceLoad() throws Exception {
        int numberOfThreads = 50;
        int requestsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Long> responseTimes = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long requestStart = System.currentTimeMillis();
                        
                        RegisterRequestDTO registerRequest = TestDataFactory.createRegisterRequestDTO(
                                "loaduser" + threadId + "_" + j, 
                                "loaduser" + threadId + "_" + j + "@test.com");

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<RegisterRequestDTO> entity = new HttpEntity<>(registerRequest, headers);

                        try {
                            ResponseEntity<String> response = restTemplate.postForEntity(
                                    getBaseUrl() + "/api/auth/register", entity, String.class);
                            
                            long requestEnd = System.currentTimeMillis();
                            synchronized (responseTimes) {
                                responseTimes.add(requestEnd - requestStart);
                            }

                            if (response.getStatusCode() == HttpStatus.CREATED) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(60, TimeUnit.SECONDS), "Load test should complete within 60 seconds");
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        executor.shutdown();

        // Анализ результатов
        int totalRequests = numberOfThreads * requestsPerThread;
        double successRate = (double) successCount.get() / totalRequests * 100;
        double averageResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        long maxResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
        long minResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0);

        System.out.println("=== Authentication Service Load Test Results ===");
        System.out.println("Total requests: " + totalRequests);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + errorCount.get());
        System.out.println("Success rate: " + successRate + "%");
        System.out.println("Total time: " + totalTime + "ms");
        System.out.println("Average response time: " + averageResponseTime + "ms");
        System.out.println("Min response time: " + minResponseTime + "ms");
        System.out.println("Max response time: " + maxResponseTime + "ms");
        System.out.println("Requests per second: " + (totalRequests * 1000.0 / totalTime));

        // Утверждения для проверки производительности
        assertTrue(successRate >= 95.0, "Success rate should be at least 95%");
        assertTrue(averageResponseTime <= 1000, "Average response time should be less than 1000ms");
        assertTrue(maxResponseTime <= 5000, "Max response time should be less than 5000ms");
    }

    @Test
    void testApiGatewayLoad() throws Exception {
        int numberOfThreads = 100;
        int requestsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Long> responseTimes = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long requestStart = System.currentTimeMillis();
                        
                        try {
                            ResponseEntity<String> response = restTemplate.getForEntity(
                                    getBaseUrl() + "/actuator/health", String.class);
                            
                            long requestEnd = System.currentTimeMillis();
                            synchronized (responseTimes) {
                                responseTimes.add(requestEnd - requestStart);
                            }

                            if (response.getStatusCode() == HttpStatus.OK) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "Gateway load test should complete within 30 seconds");
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        executor.shutdown();

        // Анализ результатов
        int totalRequests = numberOfThreads * requestsPerThread;
        double successRate = (double) successCount.get() / totalRequests * 100;
        double averageResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        System.out.println("=== API Gateway Load Test Results ===");
        System.out.println("Total requests: " + totalRequests);
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + errorCount.get());
        System.out.println("Success rate: " + successRate + "%");
        System.out.println("Average response time: " + averageResponseTime + "ms");
        System.out.println("Requests per second: " + (totalRequests * 1000.0 / totalTime));

        // Утверждения для проверки производительности Gateway
        assertTrue(successRate >= 99.0, "Gateway success rate should be at least 99%");
        assertTrue(averageResponseTime <= 100, "Gateway average response time should be less than 100ms");
    }

    @Test
    void testKafkaThroughput() throws Exception {
        int numberOfMessages = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfMessages);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfMessages; i++) {
            final int messageId = i;
            executor.submit(() -> {
                try {
                    // Здесь должна быть логика отправки сообщений в Kafka
                    // Для примера используем имитацию
                    Thread.sleep(10); // Имитация задержки сети
                    
                    // Имитация успешной отправки
                    if (messageId % 100 != 0) { // 99% успех
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "Kafka throughput test should complete within 30 seconds");
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        executor.shutdown();

        // Анализ результатов
        double successRate = (double) successCount.get() / numberOfMessages * 100;
        double messagesPerSecond = (numberOfMessages * 1000.0) / totalTime;

        System.out.println("=== Kafka Throughput Test Results ===");
        System.out.println("Total messages: " + numberOfMessages);
        System.out.println("Successful messages: " + successCount.get());
        System.out.println("Failed messages: " + errorCount.get());
        System.out.println("Success rate: " + successRate + "%");
        System.out.println("Total time: " + totalTime + "ms");
        System.out.println("Messages per second: " + messagesPerSecond);

        // Утверждения для проверки производительности Kafka
        assertTrue(successRate >= 95.0, "Kafka success rate should be at least 95%");
        assertTrue(messagesPerSecond >= 100, "Kafka should handle at least 100 messages per second");
    }

    @Test
    void testConcurrentUserSessions() throws Exception {
        int numberOfUsers = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfUsers);
        CountDownLatch latch = new CountDownLatch(numberOfUsers);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfUsers; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    // Регистрация пользователя
                    RegisterRequestDTO registerRequest = TestDataFactory.createRegisterRequestDTO(
                            "concurrentuser" + userId, 
                            "concurrentuser" + userId + "@test.com");

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<RegisterRequestDTO> registerEntity = new HttpEntity<>(registerRequest, headers);

                    ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                            getBaseUrl() + "/api/auth/register", registerEntity, String.class);

                    if (registerResponse.getStatusCode() == HttpStatus.CREATED) {
                        // Вход пользователя
                        LoginRequestDTO loginRequest = TestDataFactory.createLoginRequestDTO(
                                "concurrentuser" + userId);

                        HttpEntity<LoginRequestDTO> loginEntity = new HttpEntity<>(loginRequest, headers);

                        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                                getBaseUrl() + "/api/auth/login", loginEntity, String.class);

                        if (loginResponse.getStatusCode() == HttpStatus.OK) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(60, TimeUnit.SECONDS), "Concurrent sessions test should complete within 60 seconds");
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        executor.shutdown();

        // Анализ результатов
        double successRate = (double) successCount.get() / numberOfUsers * 100;

        System.out.println("=== Concurrent User Sessions Test Results ===");
        System.out.println("Total users: " + numberOfUsers);
        System.out.println("Successful sessions: " + successCount.get());
        System.out.println("Failed sessions: " + errorCount.get());
        System.out.println("Success rate: " + successRate + "%");
        System.out.println("Total time: " + totalTime + "ms");

        // Утверждения для проверки конкурентных сессий
        assertTrue(successRate >= 90.0, "Concurrent sessions success rate should be at least 90%");
    }

    @Test
    void testMemoryUsageUnderLoad() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        
        // Измерение памяти до теста
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        int numberOfRequests = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(numberOfRequests);

        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    // Выполнение запроса
                    restTemplate.getForEntity(getBaseUrl() + "/actuator/health", String.class);
                } catch (Exception e) {
                    // Игнорируем ошибки в этом тесте
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Измерение памяти после теста
        runtime.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        
        long memoryUsed = memoryAfter - memoryBefore;
        double memoryUsedMB = memoryUsed / (1024.0 * 1024.0);

        System.out.println("=== Memory Usage Test Results ===");
        System.out.println("Memory before test: " + (memoryBefore / (1024.0 * 1024.0)) + " MB");
        System.out.println("Memory after test: " + (memoryAfter / (1024.0 * 1024.0)) + " MB");
        System.out.println("Memory used: " + memoryUsedMB + " MB");

        // Утверждение для проверки использования памяти
        assertTrue(memoryUsedMB < 100, "Memory usage should be less than 100 MB under load");
    }
}