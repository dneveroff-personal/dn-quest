# Интеграция Kafka в DN Quest

## Обзор

Apache Kafka будет использоваться как центральная шина событий для асинхронной коммуникации между микросервисами платформы DN Quest.

## Архитектура

### Топики Kafka

#### 1. Пользовательские события
```
dn-quest.users.events
- UserRegisteredEvent
- UserUpdatedEvent
- UserDeletedEvent
- UserPasswordChangedEvent
- UserRoleChangedEvent
```

#### 2. События квестов
```
dn-quest.quests.events
- QuestCreatedEvent
- QuestUpdatedEvent
- QuestPublishedEvent
- QuestDeletedEvent
- QuestStartedEvent
- QuestFinishedEvent
```

#### 3. Игровые события
```
dn-quest.game.events
- GameSessionStartedEvent
- GameSessionFinishedEvent
- CodeSubmittedEvent
- LevelCompletedEvent
- LevelFailedEvent
- BonusCodeUsedEvent
- PenaltyCodeUsedEvent
```

#### 4. Командные события
```
dn-quest.teams.events
- TeamCreatedEvent
- TeamUpdatedEvent
- TeamDeletedEvent
- TeamMemberAddedEvent
- TeamMemberRemovedEvent
- TeamCaptainChangedEvent
- TeamInvitationSentEvent
- TeamInvitationAcceptedEvent
- TeamInvitationDeclinedEvent
```

#### 5. Уведомления
```
dn-quest.notifications.events
- EmailNotificationEvent
- PushNotificationEvent
- TelegramNotificationEvent
- SystemNotificationEvent
```

#### 6. Статистические события
```
dn-quest.statistics.events
- UserActivityEvent
- QuestStatisticsEvent
- GameSessionStatisticsEvent
- PerformanceMetricsEvent
```

## Структура событий

### Базовая структура события
```json
{
  "eventId": "uuid-v4",
  "eventType": "UserRegisteredEvent",
  "eventVersion": "1.0",
  "timestamp": "2024-01-01T12:00:00Z",
  "source": "authentication-service",
  "correlationId": "uuid-v4",
  "causationId": "uuid-v4",
  "data": {
    // Специфичные данные события
  },
  "metadata": {
    "userId": "123",
    "sessionId": "456",
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0..."
  }
}
```

### Примеры событий

#### UserRegisteredEvent
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "UserRegisteredEvent",
  "eventVersion": "1.0",
  "timestamp": "2024-01-01T12:00:00Z",
  "source": "authentication-service",
  "correlationId": "550e8400-e29b-41d4-a716-446655440001",
  "data": {
    "userId": "123",
    "username": "player123",
    "email": "player@example.com",
    "publicName": "Player One",
    "role": "PLAYER",
    "registeredAt": "2024-01-01T12:00:00Z"
  },
  "metadata": {
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0..."
  }
}
```

#### GameSessionStartedEvent
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440002",
  "eventType": "GameSessionStartedEvent",
  "eventVersion": "1.0",
  "timestamp": "2024-01-01T12:30:00Z",
  "source": "game-engine-service",
  "correlationId": "550e8400-e29b-41d4-a716-446655440003",
  "data": {
    "sessionId": "789",
    "questId": "456",
    "userId": "123",
    "teamId": null,
    "questType": "SOLO",
    "startedAt": "2024-01-01T12:30:00Z"
  },
  "metadata": {
    "userId": "123",
    "sessionId": "789"
  }
}
```

#### CodeSubmittedEvent
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440004",
  "eventType": "CodeSubmittedEvent",
  "eventVersion": "1.0",
  "timestamp": "2024-01-01T12:45:00Z",
  "source": "game-engine-service",
  "correlationId": "550e8400-e29b-41d4-a716-446655440005",
  "data": {
    "attemptId": "101112",
    "sessionId": "789",
    "levelId": "333",
    "userId": "123",
    "submittedCode": "SECRET123",
    "result": "ACCEPTED_NORMAL",
    "sectorNo": 1,
    "submittedAt": "2024-01-01T12:45:00Z"
  },
  "metadata": {
    "userId": "123",
    "sessionId": "789",
    "levelId": "333"
  }
}
```

## Конфигурация Kafka

### Серверы Kafka
```yaml
kafka:
  bootstrap-servers: kafka1:9092,kafka2:9092,kafka3:9092
  security:
    protocol: SASL_SSL
    sasl:
      mechanism: PLAIN
      jaas:
        config: |
          org.apache.kafka.common.security.plain.PlainLoginModule required \
          username="${KAFKA_USERNAME}" \
          password="${KAFKA_PASSWORD}";
  producer:
    bootstrap-servers: ${kafka.bootstrap-servers}
    key-serializer: org.apache.kafka.common.serialization.StringSerializer
    value-serializer: org.apache.kafka.common.serialization.StringSerializer
    acks: all
    retries: 3
    batch-size: 16384
    linger-ms: 1
    buffer-memory: 33554432
  consumer:
    bootstrap-servers: ${kafka.bootstrap-servers}
    key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    group-id: ${spring.application.name}
    auto-offset-reset: earliest
    enable-auto-commit: false
    max-poll-records: 500
```

### Настройки топиков
```yaml
kafka:
  topics:
    user-events:
      name: dn-quest.users.events
      partitions: 6
      replication-factor: 3
      retention-ms: 604800000  # 7 days
    quest-events:
      name: dn-quest.quests.events
      partitions: 4
      replication-factor: 3
      retention-ms: 604800000
    game-events:
      name: dn-quest.game.events
      partitions: 12
      replication-factor: 3
      retention-ms: 2592000000  # 30 days
    team-events:
      name: dn-quest.teams.events
      partitions: 4
      replication-factor: 3
      retention-ms: 604800000
    notification-events:
      name: dn-quest.notifications.events
      partitions: 8
      replication-factor: 3
      retention-ms: 86400000  # 1 day
    statistics-events:
      name: dn-quest.statistics.events
      partitions: 6
      replication-factor: 3
      retention-ms: 7776000000  # 90 days
```

## Реализация в Spring Boot

### Зависимости
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

### Конфигурация Kafka
```java
@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "dn-quest-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

### Продюсер событий
```java
@Component
@Slf4j
public class EventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public EventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    public void publishUserEvent(String eventType, Object data, String correlationId) {
        Event event = Event.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .eventVersion("1.0")
                .timestamp(Instant.now())
                .source("authentication-service")
                .correlationId(correlationId)
                .data(data)
                .build();
        
        try {
            String jsonEvent = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("dn-quest.users.events", event.getEventId(), jsonEvent)
                    .addCallback(
                            result -> log.info("Event published successfully: {}", eventType),
                            failure -> log.error("Failed to publish event: {}", eventType, failure)
                    );
        } catch (Exception e) {
            log.error("Error serializing event: {}", eventType, e);
        }
    }
}
```

### Консьюмер событий
```java
@Component
@Slf4j
public class EventConsumer {
    
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    
    public EventConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }
    
    @KafkaListener(topics = "dn-quest.users.events", groupId = "notification-service")
    public void handleUserEvents(String message, Acknowledgment acknowledgment) {
        try {
            Event event = objectMapper.readValue(message, Event.class);
            
            switch (event.getEventType()) {
                case "UserRegisteredEvent":
                    handleUserRegistered(event);
                    break;
                case "UserUpdatedEvent":
                    handleUserUpdated(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing user event", e);
            // В реальном приложении здесь может быть логика повторной обработки
        }
    }
    
    private void handleUserRegistered(Event event) {
        UserRegisteredEvent data = objectMapper.convertValue(event.getData(), UserRegisteredEvent.class);
        notificationService.sendWelcomeEmail(data.getUserId(), data.getEmail());
    }
}
```

## Схемы событий (Avro)

### UserRegisteredEvent.avsc
```avro
{
  "type": "record",
  "name": "UserRegisteredEvent",
  "namespace": "dn.quest.events",
  "fields": [
    {"name": "eventId", "type": "string"},
    {"name": "eventType", "type": "string"},
    {"name": "eventVersion", "type": "string"},
    {"name": "timestamp", "type": "long", "logicalType": "timestamp-millis"},
    {"name": "source", "type": "string"},
    {"name": "correlationId", "type": ["null", "string"], "default": null},
    {"name": "data", "type": {
      "type": "record",
      "name": "UserRegisteredData",
      "fields": [
        {"name": "userId", "type": "string"},
        {"name": "username", "type": "string"},
        {"name": "email", "type": ["null", "string"], "default": null},
        {"name": "publicName", "type": "string"},
        {"name": "role", "type": "string"},
        {"name": "registeredAt", "type": "long", "logicalType": "timestamp-millis"}
      ]
    }},
    {"name": "metadata", "type": ["null", {
      "type": "record",
      "name": "EventMetadata",
      "fields": [
        {"name": "userId", "type": ["null", "string"], "default": null},
        {"name": "sessionId", "type": ["null", "string"], "default": null},
        {"name": "ipAddress", "type": ["null", "string"], "default": null},
        {"name": "userAgent", "type": ["null", "string"], "default": null}
      ]
    }], "default": null}
  ]
}
```

## Обработка ошибок

### Dead Letter Queue (DLQ)
```yaml
kafka:
  consumer:
    enable-dlq: true
    dlq-topic-name: dn-quest.dlq
    dlq-ttl: 86400000  # 24 hours
```

### Retry механизм
```java
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 1000, multiplier = 2),
    autoCreateTopics = "false",
    topicSuffixingStrategy = TopicNameSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
)
@KafkaListener(topics = "dn-quest.users.events")
public void handleUserEvents(String message) {
    // Обработка события
}
```

## Мониторинг

### Метрики Kafka
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoint:
    kafka:
      enabled: true
```

### Логирование
```java
@Configuration
public class KafkaLoggingConfig {
    
    @Bean
    public ProducerFactory<String, String> producerFactoryWithLogging() {
        Map<String, Object> configProps = new HashMap<>();
        // ... конфигурация продюсера
        
        // Добавление интерцептора для логирования
        configProps.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, 
                "dn.quest.kafka.logging.ProducerLoggingInterceptor");
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
}
```

## Тестирование

### Тестовый контейнер Kafka
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest"
})
class KafkaEventTest {
    
    @Test
    void testUserEventPublishing() {
        // Тест публикации события
    }
    
    @Test
    void testUserEventConsumption() {
        // Тест потребления события
    }
}
```

## План внедрения

### Этап 1: Базовая инфраструктура
1. Настройка Kafka кластера
2. Создание топиков
3. Базовая конфигурация в приложениях

### Этап 2: Критичные события
1. События регистрации пользователей
2. События игровых сессий
3. События уведомлений

### Этап 3: Расширение
1. Все остальные события
2. Схемы Avro
3. Dead Letter Queue
4. Мониторинг и алерты

### Этап 4: Оптимизация
1. Партиционирование
2. Компрессия
3. Оптимизация производительности
4. Тестирование нагрузки

## Безопасность

### Аутентификация Kafka
```yaml
spring:
  kafka:
    properties:
      security.protocol: SASL_SSL
      sasl.mechanism: PLAIN
      sasl.jaas.config: |
        org.apache.kafka.common.security.plain.PlainLoginModule required \
        username="${KAFKA_USERNAME}" \
        password="${KAFKA_PASSWORD}";
```

### Шифрование сообщений
```java
@Component
public class EncryptedEventProducer {
    
    public void publishEncryptedEvent(String topic, Object event) {
        // Шифрование данных перед публикацией
        String encryptedData = encryptionService.encrypt(serialize(event));
        kafkaTemplate.send(topic, encryptedData);
    }
}
```

## Производительность

### Оптимизация продюсера
- Пакетная отправка сообщений
- Компрессия (gzip, snappy, lz4)
- Асинхронная отправка
- Правильное количество партиций

### Оптимизация консьюмера
- Группировка сообщений
- Пакетная обработка
- Правильный размер poll
- Балансировка нагрузки

## Резервное копирование

### Резервирование топиков
```bash
# Копирование топика
kafka-replica-verification.sh --broker-list kafka1:9092 --topic-replication-factor 3

# Проверка целостности
kafka-verifiable-consumer.sh --broker-list kafka1:9092 --topic dn-quest.users.events
```

### Восстановление
```bash
# Восстановление из бэкапа
kafka-console-producer.sh --broker-list kafka1:9092 --topic dn-quest.users.events --backup-file backup.log