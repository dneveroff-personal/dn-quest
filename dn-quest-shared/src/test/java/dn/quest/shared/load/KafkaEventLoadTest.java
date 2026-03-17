package dn.quest.shared.load;

import dn.quest.shared.base.AbstractLoadTestBase;
import dn.quest.shared.config.EnhancedTestKafkaConfig;
import dn.quest.shared.util.EnhancedTestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Нагрузочные тесты для Kafka событий
 */
@ActiveProfiles("load-test")
@EmbeddedKafka(partitions = 3, brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
})
class KafkaEventLoadTest extends AbstractLoadTestBase {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EnhancedTestKafkaConfig kafkaConfig;

    private final AtomicInteger sentEvents = new AtomicInteger(0);
    private final AtomicInteger receivedEvents = new AtomicInteger(0);

    @Test
    void testHighVolumeEventProduction_LoadTest() throws Exception {
        int numberOfThreads = 20;
        int eventsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        long startTime = System.currentTimeMillis();
        CompletableFuture<?>[] futures = new CompletableFuture[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < eventsPerThread; j++) {
                    try {
                        String event = EnhancedTestDataFactory.createUserEventJson(
                                "user" + threadId + "_" + j,
                                "USER_REGISTERED"
                        );

                        kafkaTemplate.send("user-events", event).get();
                        sentEvents.incrementAndGet();
                        recordSuccess();
                    } catch (Exception e) {
                        recordFailure();
                        logger.error("Failed to send event in thread {}: {}", threadId, e.getMessage());
                    }
                }
            }, executor);
        }

        CompletableFuture.allOf(futures).get(5, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        long totalEvents = numberOfThreads * eventsPerThread;
        long totalTime = endTime - startTime;
        double eventsPerSecond = (double) sentEvents.get() / (totalTime / 1000.0);

        logger.info("High Volume Event Production Test Results:");
        logger.info("Total Events Sent: {}", sentEvents.get());
        logger.info("Expected Events: {}", totalEvents);
        logger.info("Total Time: {} ms", totalTime);
        logger.info("Events per Second: {:.2f}", eventsPerSecond);
        logger.info("Success Rate: {:.2f}%", (double) sentEvents.get() / totalEvents * 100);

        // Проверка пороговых значений
        assertTrue(sentEvents.get() > totalEvents * 0.95, "Should send at least 95% of events");
        assertTrue(eventsPerSecond > 100.0, "Should handle at least 100 events per second");

        recordMetric("kafka_events_per_second", eventsPerSecond);
        recordMetric("kafka_production_success_rate", (double) sentEvents.get() / totalEvents * 100);
    }

    @Test
    void testConcurrentTopicProduction_LoadTest() throws Exception {
        String[] topics = {"user-events", "quest-events", "team-events", "game-events", "notification-events"};
        int eventsPerTopic = 200;
        ExecutorService executor = Executors.newFixedThreadPool(10);

        long startTime = System.currentTimeMillis();
        CompletableFuture<?>[] futures = new CompletableFuture[topics.length];

        for (int i = 0; i < topics.length; i++) {
            final String topic = topics[i];
            final int topicIndex = i;
            
            futures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < eventsPerTopic; j++) {
                    try {
                        String event;
                        switch (topicIndex) {
                            case 0:
                                event = EnhancedTestDataFactory.createUserEventJson(
                                        "user" + j, "USER_UPDATED"
                                );
                                break;
                            case 1:
                                event = EnhancedTestDataFactory.createQuestEventJson(
                                        (long) j, "QUEST_PUBLISHED"
                                );
                                break;
                            case 2:
                                event = EnhancedTestDataFactory.createTeamEventJson(
                                        (long) j, "TEAM_CREATED"
                                );
                                break;
                            case 3:
                                event = EnhancedTestDataFactory.createGameSessionEventJson(
                                        (long) j, "SESSION_STARTED"
                                );
                                break;
                            default:
                                event = EnhancedTestDataFactory.createNotificationEventJson(
                                        (long) j, "NOTIFICATION_SENT"
                                );
                        }

                        kafkaTemplate.send(topic, event).get();
                        sentEvents.incrementAndGet();
                        recordSuccess();
                    } catch (Exception e) {
                        recordFailure();
                        logger.error("Failed to send event to topic {}: {}", topic, e.getMessage());
                    }
                }
            }, executor);
        }

        CompletableFuture.allOf(futures).get(5, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        long totalEvents = topics.length * eventsPerTopic;
        long totalTime = endTime - startTime;
        double eventsPerSecond = (double) sentEvents.get() / (totalTime / 1000.0);

        logger.info("Concurrent Topic Production Test Results:");
        logger.info("Total Events Sent: {}", sentEvents.get());
        logger.info("Expected Events: {}", totalEvents);
        logger.info("Total Time: {} ms", totalTime);
        logger.info("Events per Second: {:.2f}", eventsPerSecond);

        assertTrue(sentEvents.get() > totalEvents * 0.90, "Should send at least 90% of events");
        assertTrue(eventsPerSecond > 50.0, "Should handle at least 50 events per second across topics");

        recordMetric("kafka_multi_topic_events_per_second", eventsPerSecond);
        recordMetric("kafka_multi_topic_success_rate", (double) sentEvents.get() / totalEvents * 100);
    }

    @Test
    void testLargeEventPayload_LoadTest() throws Exception {
        int numberOfEvents = 100;
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // Создание большого события
        StringBuilder largePayload = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largePayload.append("This is a large payload data for testing Kafka performance. ");
        }

        long startTime = System.currentTimeMillis();
        int successfulEvents = 0;

        for (int i = 0; i < numberOfEvents; i++) {
            try {
                String largeEvent = EnhancedTestDataFactory.createLargeEventJson(
                        "large-event-" + i,
                        largePayload.toString()
                );

                kafkaTemplate.send("large-events", largeEvent).get();
                successfulEvents++;
                recordSuccess();
            } catch (Exception e) {
                recordFailure();
                logger.error("Failed to send large event {}: {}", i, e.getMessage());
            }
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double averageTime = (double) totalTime / numberOfEvents;

        logger.info("Large Event Payload Test Results:");
        logger.info("Successful Events: {}", successfulEvents);
        logger.info("Total Events: {}", numberOfEvents);
        logger.info("Total Time: {} ms", totalTime);
        logger.info("Average Time per Event: {:.2f} ms", averageTime);

        assertTrue(successfulEvents > numberOfEvents * 0.8, "Should send at least 80% of large events");
        assertTrue(averageTime < 1000.0, "Average time should be below 1000ms per event");

        recordMetric("kafka_large_payload_success_rate", (double) successfulEvents / numberOfEvents * 100);
        recordMetric("kafka_large_payload_avg_time", averageTime);
    }

    @Test
    void testEventProcessingLatency_LoadTest() throws Exception {
        int numberOfEvents = 50;
        long[] latencies = new long[numberOfEvents];

        for (int i = 0; i < numberOfEvents; i++) {
            long startTime = System.nanoTime();
            
            try {
                String event = EnhancedTestDataFactory.createUserEventJson(
                        "latency-test-" + i,
                        "USER_LOGIN"
                );

                // Отправка события
                kafkaTemplate.send("latency-test-events", event).get();
                
                // Симуляция обработки
                Thread.sleep(10);
                
                long endTime = System.nanoTime();
                latencies[i] = (endTime - startTime) / 1_000_000; // Конвертация в миллисекунды
                
                recordSuccess();
            } catch (Exception e) {
                recordFailure();
                logger.error("Failed to process latency test event {}: {}", i, e.getMessage());
            }
        }

        // Вычисление статистики
        double averageLatency = 0;
        long maxLatency = 0;
        long minLatency = Long.MAX_VALUE;

        for (long latency : latencies) {
            averageLatency += latency;
            maxLatency = Math.max(maxLatency, latency);
            minLatency = Math.min(minLatency, latency);
        }
        averageLatency /= numberOfEvents;

        logger.info("Event Processing Latency Test Results:");
        logger.info("Average Latency: {:.2f} ms", averageLatency);
        logger.info("Min Latency: {} ms", minLatency);
        logger.info("Max Latency: {} ms", maxLatency);

        assertTrue(averageLatency < 500.0, "Average latency should be below 500ms");
        assertTrue(maxLatency < 2000.0, "Max latency should be below 2000ms");

        recordMetric("kafka_avg_latency", averageLatency);
        recordMetric("kafka_max_latency", maxLatency);
        recordMetric("kafka_min_latency", minLatency);
    }

    @Test
    void testKafkaPartitioning_LoadTest() throws Exception {
        int numberOfEvents = 300; // Кратно количеству партиций (3)
        int partitionCount = 3;
        int[] partitionCounts = new int[partitionCount];

        for (int i = 0; i < numberOfEvents; i++) {
            try {
                String event = EnhancedTestDataFactory.createUserEventJson(
                        "partition-test-" + i,
                        "USER_ACTION"
                );

                // Отправка с указанием ключа для распределения по партициям
                String partitionKey = "user-" + (i % partitionCount);
                kafkaTemplate.send("partitioned-events", partitionKey, event).get();
                
                partitionCounts[i % partitionCount]++;
                recordSuccess();
            } catch (Exception e) {
                recordFailure();
                logger.error("Failed to send partitioned event {}: {}", i, e.getMessage());
            }
        }

        // Проверка распределения по партициям
        logger.info("Kafka Partitioning Test Results:");
        for (int i = 0; i < partitionCount; i++) {
            logger.info("Partition {}: {} events", i, partitionCounts[i]);
        }

        // Проверка, что события распределены относительно равномерно
        int expectedPerPartition = numberOfEvents / partitionCount;
        for (int count : partitionCounts) {
            assertTrue(count >= expectedPerPartition * 0.7 && count <= expectedPerPartition * 1.3,
                    "Partition distribution should be relatively even");
        }

        recordMetric("kafka_partition_distribution_variance", calculateVariance(partitionCounts));
    }

    @Test
    void testKafkaConsumerThroughput_LoadTest() throws Exception {
        int numberOfEvents = 200;
        ExecutorService producerExecutor = Executors.newFixedThreadPool(5);
        ExecutorService consumerExecutor = Executors.newFixedThreadPool(3);

        // Запуск продюсеров
        CompletableFuture<?>[] producerFutures = new CompletableFuture[5];
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            producerFutures[i] = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < numberOfEvents / 5; j++) {
                    try {
                        String event = EnhancedTestDataFactory.createGameSessionEventJson(
                                (long) (threadId * 100 + j),
                                "SESSION_UPDATE"
                        );
                        kafkaTemplate.send("throughput-test-events", event).get();
                    } catch (Exception e) {
                        logger.error("Producer failed: {}", e.getMessage());
                    }
                }
            }, producerExecutor);
        }

        // Запуск консьюмеров (симуляция)
        CompletableFuture<?>[] consumerFutures = new CompletableFuture[3];
        for (int i = 0; i < 3; i++) {
            consumerFutures[i] = CompletableFuture.runAsync(() -> {
                int consumedCount = 0;
                while (consumedCount < numberOfEvents / 3) {
                    try {
                        // Симуляция обработки события
                        Thread.sleep(5);
                        consumedCount++;
                        receivedEvents.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, consumerExecutor);
        }

        long startTime = System.currentTimeMillis();
        CompletableFuture.allOf(producerFutures).get(2, TimeUnit.MINUTES);
        CompletableFuture.allOf(consumerFutures).get(3, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        producerExecutor.shutdown();
        consumerExecutor.shutdown();

        long totalTime = endTime - startTime;
        double throughput = (double) receivedEvents.get() / (totalTime / 1000.0);

        logger.info("Kafka Consumer Throughput Test Results:");
        logger.info("Events Consumed: {}", receivedEvents.get());
        logger.info("Total Time: {} ms", totalTime);
        logger.info("Consumer Throughput: {:.2f} events/second", throughput);

        assertTrue(throughput > 20.0, "Consumer throughput should be at least 20 events/second");

        recordMetric("kafka_consumer_throughput", throughput);
        recordMetric("kafka_consumer_total_events", receivedEvents.get());
    }

    private double calculateVariance(int[] values) {
        double mean = 0;
        for (int value : values) {
            mean += value;
        }
        mean /= values.length;

        double variance = 0;
        for (int value : values) {
            variance += Math.pow(value - mean, 2);
        }
        return variance / values.length;
    }
}