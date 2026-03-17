package dn.quest.shared.config;

import dn.quest.shared.constants.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Улучшенная тестовая конфигурация Kafka с поддержкой всех топиков
 */
@Slf4j
@TestConfiguration
public class EnhancedTestKafkaConfig {

    /**
     * Встроенный Kafka брокер с полным набором топиков
     */
    @Bean
    @Primary
    public EmbeddedKafkaBroker embeddedKafkaBroker() {
        return new EmbeddedKafkaBroker(1, true, 1, 
                KafkaTopics.USER_EVENTS,
                KafkaTopics.QUEST_EVENTS,
                KafkaTopics.GAME_EVENTS,
                KafkaTopics.TEAM_EVENTS,
                KafkaTopics.FILE_EVENTS,
                KafkaTopics.NOTIFICATION_EVENTS,
                KafkaTopics.STATISTICS_EVENTS,
                KafkaTopics.USER_EVENTS_DLQ,
                KafkaTopics.QUEST_EVENTS_DLQ,
                KafkaTopics.GAME_EVENTS_DLQ,
                KafkaTopics.TEAM_EVENTS_DLQ,
                KafkaTopics.FILE_EVENTS_DLQ,
                KafkaTopics.NOTIFICATION_EVENTS_DLQ,
                KafkaTopics.STATISTICS_EVENTS_DLQ,
                KafkaTopics.GENERAL_DLQ)
                .brokerProperty("listeners", "PLAINTEXT://localhost:9092")
                .brokerProperty("port", "9092")
                .brokerProperty("auto.create.topics.enable", "true")
                .brokerProperty("num.partitions", "3")
                .brokerProperty("default.replication.factor", "1");
    }

    /**
     * Фабрика потребителей с расширенной конфигурацией
     */
    @Bean
    @Primary
    public ConsumerFactory<String, String> consumerFactory(EmbeddedKafkaBroker embeddedKafka) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka);
        
        // Настройка для десериализации JSON
        consumerProps.put("spring.json.trusted.packages", "dn.quest.shared.events");
        consumerProps.put("spring.json.value.default.type", "dn.quest.shared.events.BaseEvent");
        consumerProps.put("spring.json.use.type.headers", "false");
        
        // Настройка для обработки ошибок
        consumerProps.put("spring.json.value.default.type", "dn.quest.shared.events.BaseEvent");
        consumerProps.put("spring.json.type.mapping", """
                BaseEvent:dn.quest.shared.events.BaseEvent,
                UserRegisteredEvent:dn.quest.shared.events.user.UserRegisteredEvent,
                UserUpdatedEvent:dn.quest.shared.events.user.UserUpdatedEvent,
                UserDeletedEvent:dn.quest.shared.events.user.UserDeletedEvent,
                QuestCreatedEvent:dn.quest.shared.events.quest.QuestCreatedEvent,
                QuestUpdatedEvent:dn.quest.shared.events.quest.QuestUpdatedEvent,
                QuestDeletedEvent:dn.quest.shared.events.quest.QuestDeletedEvent,
                QuestPublishedEvent:dn.quest.shared.events.quest.QuestPublishedEvent,
                GameSessionStartedEvent:dn.quest.shared.events.game.GameSessionStartedEvent,
                CodeSubmittedEvent:dn.quest.shared.events.game.CodeSubmittedEvent,
                LevelCompletedEvent:dn.quest.shared.events.game.LevelCompletedEvent,
                GameSessionFinishedEvent:dn.quest.shared.events.game.GameSessionFinishedEvent,
                TeamCreatedEvent:dn.quest.shared.events.team.TeamCreatedEvent,
                TeamUpdatedEvent:dn.quest.shared.events.team.TeamUpdatedEvent,
                TeamMemberAddedEvent:dn.quest.shared.events.team.TeamMemberAddedEvent,
                TeamMemberRemovedEvent:dn.quest.shared.events.team.TeamMemberRemovedEvent,
                FileUploadedEvent:dn.quest.shared.events.file.FileUploadedEvent,
                FileUpdatedEvent:dn.quest.shared.events.file.FileUpdatedEvent,
                FileDeletedEvent:dn.quest.shared.events.file.FileDeletedEvent,
                NotificationEvent:dn.quest.shared.events.notification.NotificationEvent
                """);
        
        // Настройка для надежности
        consumerProps.put("enable.auto.commit", "false");
        consumerProps.put("auto.offset.reset", "earliest");
        consumerProps.put("max.poll.records", "10");
        consumerProps.put("session.timeout.ms", "30000");
        consumerProps.put("heartbeat.interval.ms", "10000");
        
        return new DefaultConsumerFactory<>(consumerProps);
    }

    /**
     * Фабрика продюсеров с расширенной конфигурацией
     */
    @Bean
    @Primary
    public ProducerFactory<String, String> producerFactory(EmbeddedKafkaBroker embeddedKafka) {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        
        // Настройка для сериализации JSON
        producerProps.put("spring.json.add.type.headers", "false");
        
        // Настройка для надежности
        producerProps.put("acks", "all");
        producerProps.put("retries", "3");
        producerProps.put("batch.size", "16384");
        producerProps.put("linger.ms", "1");
        producerProps.put("buffer.memory", "33554432");
        producerProps.put("delivery.timeout.ms", "120000");
        producerProps.put("request.timeout.ms", "30000");
        
        return new DefaultKafkaProducerFactory<>(producerProps);
    }

    /**
     * Kafka шаблон с расширенной конфигурацией
     */
    @Bean
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
        template.setDefaultTopic(KafkaTopics.USER_EVENTS);
        
        // Настройка для обработки ошибок
        template.setProducerListener(new org.springframework.kafka.support.ProducerListener<String, String>() {
            @Override
            public void onSuccess(org.apache.kafka.clients.producer.ProducerRecord<String, String> producerRecord, 
                                 org.apache.kafka.clients.producer.RecordMetadata recordMetadata) {
                log.debug("Message sent successfully to topic: {}, partition: {}, offset: {}", 
                        recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
            }

            @Override
            public void onError(org.apache.kafka.clients.producer.ProducerRecord<String, String> producerRecord, 
                              org.apache.kafka.clients.producer.RecordMetadata recordMetadata, 
                              Exception exception) {
                log.error("Failed to send message to topic: {}", producerRecord.topic(), exception);
            }
        });
        
        return template;
    }

    /**
     * Фабрика контейнеров слушателей с расширенной конфигурацией
     */
    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setIdleEventInterval(30000L);
        factory.getContainerProperties().setIdleBetweenPolls(100);
        
        // Настройка обработки ошибок
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler(
                new org.springframework.kafka.listener.DeadLetterPublishingRecoverer(
                        kafkaTemplate(consumerFactory),
                        (record, ex) -> {
                            String topic = record.topic();
                            String dlqTopic = topic + ".dlq";
                            return new org.apache.kafka.clients.producer.ProducerRecord<>(dlqTopic, record.key(), record.value());
                        }
                )));
        
        // Настройка повторных попыток
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler(
                (record, ex) -> {
                    log.error("Error processing record from topic: {}", record.topic(), ex);
                }));
        
        return factory;
    }

    /**
     * Утилиты для тестирования Kafka
     */
    @Bean
    @Primary
    public KafkaTestUtils kafkaTestUtils(EmbeddedKafkaBroker embeddedKafka) {
        return new KafkaTestUtils(embeddedKafka);
    }

    /**
     * Утилиты для работы с топиками в тестах
     */
    @Bean
    @Primary
    public KafkaTopicTestUtils kafkaTopicTestUtils(EmbeddedKafkaBroker embeddedKafka) {
        return new KafkaTopicTestUtils(embeddedKafka);
    }

    /**
     * Утилиты для тестирования Kafka
     */
    public static class KafkaTestUtils {
        private final EmbeddedKafkaBroker embeddedKafka;

        public KafkaTestUtils(EmbeddedKafkaBroker embeddedKafka) {
            this.embeddedKafka = embeddedKafka;
        }

        /**
         * Ожидание создания топика
         */
        public void waitForTopic(String topic, long timeoutMs) throws InterruptedException {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                if (isTopicExists(topic)) {
                    return;
                }
                Thread.sleep(100);
            }
            throw new RuntimeException("Topic " + topic + " was not created within timeout");
        }

        /**
         * Проверка существования топика
         */
        public boolean isTopicExists(String topic) {
            try {
                embeddedKafka.getTopics().contains(topic);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * Получение всех топиков
         */
        public java.util.Set<String> getAllTopics() {
            return embeddedKafka.getTopics();
        }

        /**
         * Очистка всех топиков
         */
        public void cleanupTopics() {
            log.info("Cleaning up Kafka topics...");
            // В реальной реализации здесь может быть логика для очистки топиков
        }
    }

    /**
     * Утилиты для работы с топиками в тестах
     */
    public static class KafkaTopicTestUtils {
        private final EmbeddedKafkaBroker embeddedKafka;

        public KafkaTopicTestUtils(EmbeddedKafkaBroker embeddedKafka) {
            this.embeddedKafka = embeddedKafka;
        }

        /**
         * Создание тестового сообщения
         */
        public String createTestMessage(String eventType, String data) {
            return String.format("""
                    {
                        "eventId": "%s",
                        "eventType": "%s",
                        "timestamp": "%s",
                        "source": "test",
                        "correlationId": "%s",
                        "data": %s
                    }
                    """,
                    java.util.UUID.randomUUID().toString(),
                    eventType,
                    java.time.Instant.now().toString(),
                    java.util.UUID.randomUUID().toString(),
                    data);
        }

        /**
         * Создание тестового события пользователя
         */
        public String createUserEvent(String eventType, Long userId, String username) {
            return createTestMessage(eventType, String.format("""
                    {
                        "userId": %d,
                        "username": "%s",
                        "email": "%s@test.com",
                        "role": "PLAYER"
                    }
                    """, userId, username, username));
        }

        /**
         * Создание тестового события квеста
         */
        public String createQuestEvent(String eventType, Long questId, String title) {
            return createTestMessage(eventType, String.format("""
                    {
                        "questId": %d,
                        "title": "%s",
                        "description": "Test quest description",
                        "difficulty": "EASY",
                        "questType": "SOLO"
                    }
                    """, questId, title));
        }

        /**
         * Создание тестового игрового события
         */
        public String createGameEvent(String eventType, String sessionId, Long userId) {
            return createTestMessage(eventType, String.format("""
                    {
                        "sessionId": "%s",
                        "userId": %d,
                        "questId": %d,
                        "status": "ACTIVE"
                    }
                    """, sessionId, userId, 1L));
        }

        /**
         * Создание тестового командного события
         */
        public String createTeamEvent(String eventType, Long teamId, String name) {
            return createTestMessage(eventType, String.format("""
                    {
                        "teamId": %d,
                        "name": "%s",
                        "description": "Test team description",
                        "creatorId": %d
                    }
                    """, teamId, name, 1L));
        }

        /**
         * Создание тестового файлового события
         */
        public String createFileEvent(String eventType, String fileId, String fileName) {
            return createTestMessage(eventType, String.format("""
                    {
                        "fileId": "%s",
                        "fileName": "%s",
                        "fileSize": 1024,
                        "mimeType": "text/plain",
                        "userId": %d
                    }
                    """, fileId, fileName, 1L));
        }

        /**
         * Создание тестового уведомления
         */
        public String createNotificationEvent(Long userId, String title, String message) {
            return createTestMessage("NOTIFICATION_CREATED", String.format("""
                    {
                        "userId": %d,
                        "title": "%s",
                        "message": "%s",
                        "type": "INFO"
                    }
                    """, userId, title, message));
        }
    }
}