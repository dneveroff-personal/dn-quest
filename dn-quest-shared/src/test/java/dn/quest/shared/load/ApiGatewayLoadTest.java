package dn.quest.shared.load;

import dn.quest.shared.base.AbstractLoadTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Нагрузочные тесты для API Gateway
 */
@ActiveProfiles("load-test")
class ApiGatewayLoadTest extends AbstractLoadTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testConcurrentRequests_LoadTest() throws Exception {
        int numberOfThreads = 50;
        int requestsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        long startTime = System.currentTimeMillis();
        CompletableFuture<?>[] futures = new CompletableFuture[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setBearerAuth(getValidToken());
                        HttpEntity<String> entity = new HttpEntity<>(headers);

                        ResponseEntity<String> response = restTemplate.exchange(
                                "/api/quests",
                                HttpMethod.GET,
                                entity,
                                String.class
                        );

                        assertTrue(response.getStatusCode().is2xxSuccessful());
                        recordSuccess();
                    } catch (Exception e) {
                        recordFailure();
                        logger.error("Request failed in thread {}: {}", threadId, e.getMessage());
                    }
                }
            }, executor);
        }

        CompletableFuture.allOf(futures).get(5, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        long totalRequests = numberOfThreads * requestsPerThread;
        long totalTime = endTime - startTime;
        double requestsPerSecond = (double) totalRequests / (totalTime / 1000.0);

        logger.info("Load Test Results:");
        logger.info("Total Requests: {}", totalRequests);
        logger.info("Total Time: {} ms", totalTime);
        logger.info("Requests per Second: {:.2f}", requestsPerSecond);
        logger.info("Success Rate: {:.2f}%", getSuccessRate());
        logger.info("Average Response Time: {:.2f} ms", getAverageResponseTime());

        // Проверка пороговых значений
        assertTrue(getSuccessRate() > 95.0, "Success rate should be above 95%");
        assertTrue(requestsPerSecond > 100.0, "Should handle at least 100 requests per second");
        assertTrue(getAverageResponseTime() < 1000.0, "Average response time should be below 1000ms");

        // Запись метрик
        recordMetric("concurrent_requests_rps", requestsPerSecond);
        recordMetric("concurrent_requests_success_rate", getSuccessRate());
        recordMetric("concurrent_requests_avg_response_time", getAverageResponseTime());
    }

    @Test
    void testRateLimiting_LoadTest() throws Exception {
        int requestsPerBurst = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20);

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int rateLimitCount = 0;

        for (int i = 0; i < requestsPerBurst; i++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(getValidToken());
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        "/api/quests",
                        HttpMethod.GET,
                        entity,
                        String.class
                );

                if (response.getStatusCode() == HttpStatus.OK) {
                    successCount++;
                } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    rateLimitCount++;
                }
            } catch (Exception e) {
                logger.error("Rate limit test request failed: {}", e.getMessage());
            }
        }

        long endTime = System.currentTimeMillis();

        logger.info("Rate Limiting Test Results:");
        logger.info("Successful Requests: {}", successCount);
        logger.info("Rate Limited Requests: {}", rateLimitCount);
        logger.info("Total Time: {} ms", endTime - startTime);

        // Проверка, что rate limiting работает
        assertTrue(rateLimitCount > 0, "Rate limiting should be active");
        assertTrue(successCount > 0, "Some requests should succeed");

        recordMetric("rate_limit_success_count", successCount);
        recordMetric("rate_limit_blocked_count", rateLimitCount);
    }

    @Test
    void testCircuitBreaker_LoadTest() throws Exception {
        // Симулируем отказ сервиса
        int numberOfThreads = 30;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        long startTime = System.currentTimeMillis();
        int circuitBreakerOpenCount = 0;
        int serviceUnavailableCount = 0;

        CompletableFuture<?>[] futures = new CompletableFuture[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(getValidToken());
                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    // Запрос к эндпоинту, который может вызвать circuit breaker
                    ResponseEntity<String> response = restTemplate.exchange(
                            "/api/quests/99999", // Несуществующий квест
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

                    if (response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                        synchronized (this) {
                            serviceUnavailableCount++;
                        }
                    }
                } catch (Exception e) {
                    if (e.getMessage().contains("CircuitBreaker")) {
                        synchronized (this) {
                            circuitBreakerOpenCount++;
                        }
                    }
                }
            }, executor);
        }

        CompletableFuture.allOf(futures).get(2, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        logger.info("Circuit Breaker Test Results:");
        logger.info("Circuit Breaker Open Count: {}", circuitBreakerOpenCount);
        logger.info("Service Unavailable Count: {}", serviceUnavailableCount);
        logger.info("Total Time: {} ms", endTime - startTime);

        // Проверка работы circuit breaker
        assertTrue(circuitBreakerOpenCount > 0 || serviceUnavailableCount > 0, 
                "Circuit breaker should be activated");

        recordMetric("circuit_breaker_open_count", circuitBreakerOpenCount);
        recordMetric("service_unavailable_count", serviceUnavailableCount);
    }

    @Test
    void testMemoryUsage_LoadTest() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        int numberOfRequests = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(getValidToken());
                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    restTemplate.exchange(
                            "/api/quests",
                            HttpMethod.GET,
                            entity,
                            String.class
                    );
                } catch (Exception e) {
                    logger.debug("Memory test request failed: {}", e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.MINUTES);

        // Принудительная сборка мусора
        System.gc();
        Thread.sleep(1000);

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        double memoryIncreaseMB = memoryIncrease / (1024.0 * 1024.0);

        logger.info("Memory Usage Test Results:");
        logger.info("Initial Memory: {:.2f} MB", initialMemory / (1024.0 * 1024.0));
        logger.info("Final Memory: {:.2f} MB", finalMemory / (1024.0 * 1024.0));
        logger.info("Memory Increase: {:.2f} MB", memoryIncreaseMB);

        // Проверка, что утечек памяти нет
        assertTrue(memoryIncreaseMB < 100.0, "Memory increase should be below 100MB");

        recordMetric("memory_increase_mb", memoryIncreaseMB);
    }

    @Test
    void testConcurrentAuthentication_LoadTest() throws Exception {
        int numberOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        long startTime = System.currentTimeMillis();
        int successfulAuths = 0;

        CompletableFuture<?>[] futures = new CompletableFuture[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    Map<String, String> loginRequest = Map.of(
                            "username", "loadtestuser" + threadId,
                            "password", "LoadTestPassword123!"
                    );

                    ResponseEntity<String> response = restTemplate.postForEntity(
                            "/api/auth/login",
                            loginRequest,
                            String.class
                    );

                    if (response.getStatusCode() == HttpStatus.OK) {
                        synchronized (this) {
                            successfulAuths++;
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Authentication test failed for thread {}: {}", threadId, e.getMessage());
                }
            }, executor);
        }

        CompletableFuture.allOf(futures).get(2, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long totalTime = endTime - startTime;
        double authsPerSecond = (double) successfulAuths / (totalTime / 1000.0);

        logger.info("Concurrent Authentication Test Results:");
        logger.info("Successful Authentications: {}", successfulAuths);
        logger.info("Total Time: {} ms", totalTime);
        logger.info("Authentications per Second: {:.2f}", authsPerSecond);

        // Проверка производительности аутентификации
        assertTrue(authsPerSecond > 10.0, "Should handle at least 10 authentications per second");

        recordMetric("auth_per_second", authsPerSecond);
        recordMetric("auth_success_count", successfulAuths);
    }

    @Test
    void testLargePayload_LoadTest() throws Exception {
        int numberOfRequests = 50;
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Создание большого payload
        StringBuilder largeDescription = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeDescription.append("This is a very long description for testing large payload handling. ");
        }

        long startTime = System.currentTimeMillis();
        int successfulRequests = 0;

        for (int i = 0; i < numberOfRequests; i++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(getValidToken());
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> questRequest = Map.of(
                        "title", "Large Payload Test Quest " + i,
                        "description", largeDescription.toString(),
                        "difficulty", "MEDIUM",
                        "estimatedDuration", 60,
                        "category", "TEST"
                );

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(questRequest, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        "/api/quests",
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    successfulRequests++;
                }
            } catch (Exception e) {
                logger.debug("Large payload test failed: {}", e.getMessage());
            }
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double averageTime = (double) totalTime / numberOfRequests;

        logger.info("Large Payload Test Results:");
        logger.info("Successful Requests: {}", successfulRequests);
        logger.info("Total Time: {} ms", totalTime);
        logger.info("Average Response Time: {:.2f} ms", averageTime);

        // Проверка обработки больших payload
        assertTrue(successfulRequests > numberOfRequests * 0.8, 
                "At least 80% of large payload requests should succeed");
        assertTrue(averageTime < 5000.0, "Average response time should be below 5000ms");

        recordMetric("large_payload_success_rate", (double) successfulRequests / numberOfRequests * 100);
        recordMetric("large_payload_avg_time", averageTime);
    }

    private String getValidToken() {
        // В реальном тесте здесь нужно получить токен от authentication service
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }
}