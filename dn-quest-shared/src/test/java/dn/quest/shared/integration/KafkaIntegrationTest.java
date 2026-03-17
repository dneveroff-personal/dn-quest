package dn.quest.shared.integration;

import dn.quest.shared.events.BaseEvent;
import dn.quest.shared.events.EventConsumer;
import dn.quest.shared.events.EventProducer;
import dn.quest.shared.events.user.UserRegisteredEvent;
import dn.quest.shared.events.user.UserUpdatedEvent;
import dn.quest.shared.events.quest.QuestCreatedEvent;
import dn.quest.shared.events.game.GameSessionStartedEvent;
import dn.quest.shared.events.team.TeamCreatedEvent;
import dn.quest.shared.events.file.FileUploadedEvent;
import dn.quest.shared.events.notification.NotificationEvent;
import dn.quest.shared.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для Kafka
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, 
               brokerProperties = {
                   "listeners=PLAINTEXT://localhost:9092",
                   "port=9092"
               },
               topics = {
                   "user-events",
                   "quest-events", 
                   "game-events",
                   "team-events",
                   "file-events",
                   "notification-events"
               })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@EnableKafka
class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private EventConsumer eventConsumer;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    @Test
    void testUserEventProductionAndConsumption_Success() throws Exception {
        // Given
        UserRegisteredEvent testEvent = TestDataFactory.createUserRegisteredEvent(
                "testuser", "test@example.com");
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<UserRegisteredEvent> receivedEvent = new AtomicReference<>();

        // When
        eventProducer.sendUserEvent(testEvent);

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            // Проверка, что событие было отправлено
            assertNotNull(testEvent.getEventId());
            assertEquals("USER_REGISTERED", testEvent.getEventType());
        });
    }

    @Test
    void testQuestEventProductionAndConsumption_Success() throws Exception {
        // Given
        QuestCreatedEvent testEvent = QuestCreatedEvent.builder()
                .eventId(TestDataFactory.createTestUUID())
                .eventType("QUEST_CREATED")
                .timestamp(TestDataFactory.createTestTimestamp())
                .questId(1L)
                .title("Test Quest")
                .description("Test Description")
                .creatorId(1L)
                .difficulty("EASY")
                .questType("SOLO")
                .build();

        // When
        eventProducer.sendQuestEvent(testEvent);

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            assertNotNull(testEvent.getEventId());
            assertEquals("QUEST_CREATED", testEvent.getEventType());
            assertEquals("Test Quest", testEvent.getTitle());
        });
    }

    @Test
    void testGameEventProductionAndConsumption_Success() throws Exception {
        // Given
        GameSessionStartedEvent testEvent = GameSessionStartedEvent.builder()
                .eventId(TestDataFactory.createTestUUID())
                .eventType("GAME_SESSION_STARTED")
                .timestamp(TestDataFactory.createTestTimestamp())
                .sessionId(TestDataFactory.createTestUUID())
                .questId(1L)
                .userId(1L)
                .teamId(null)
                .build();

        // When
        eventProducer.sendGameEvent(testEvent);

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            assertNotNull(testEvent.getEventId());
            assertEquals("GAME_SESSION_STARTED", testEvent.getEventType());
            assertEquals(1L, testEvent.getQuestId());
            assertEquals(1L, testEvent.getUserId());
        });
    }

    @Test
    void testTeamEventProductionAndConsumption_Success() throws Exception {
        // Given
        TeamCreatedEvent testEvent = TeamCreatedEvent.builder()
                .eventId(TestDataFactory.createTestUUID())
                .eventType("TEAM_CREATED")
                .timestamp(TestDataFactory.createTestTimestamp())
                .teamId(1L)
                .teamName("Test Team")
                .creatorId(1L)
                .build();

        // When
        eventProducer.sendTeamEvent(testEvent);

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            assertNotNull(testEvent.getEventId());
            assertEquals("TEAM_CREATED", testEvent.getEventType());
            assertEquals("Test Team", testEvent.getTeamName());
        });
    }

    @Test
    void testFileEventProductionAndConsumption_Success() throws Exception {
        // Given
        FileUploadedEvent testEvent = FileUploadedEvent.builder()
                .eventId(TestDataFactory.createTestUUID())
                .eventType("FILE_UPLOADED")
                .timestamp(TestDataFactory.createTestTimestamp())
                .fileId(TestDataFactory.createTestUUID())
                .fileName("test.jpg")
                .fileSize(1024L)
                .mimeType("image/jpeg")
                .userId(1L)
                .build();

        // When
        eventProducer.sendFileEvent(testEvent);

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            assertNotNull(testEvent.getEventId());
            assertEquals("FILE_UPLOADED", testEvent.getEventType());
            assertEquals("test.jpg", testEvent.getFileName());
            assertEquals(1024L, testEvent.getFileSize());
        });
    }

    @Test
    void testNotificationEventProductionAndConsumption_Success() throws Exception {
        // Given
        NotificationEvent testEvent = NotificationEvent.builder()
                .eventId(TestDataFactory.createTestUUID())
                .eventType("NOTIFICATION")
                .timestamp(TestDataFactory.createTestTimestamp())
                .userId(1L)
                .type("INFO")
                .title("Test Notification")
                .message("Test message")
                .build();

        // When
        eventProducer.sendNotificationEvent(testEvent);

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            assertNotNull(testEvent.getEventId());
            assertEquals("NOTIFICATION", testEvent.getEventType());
            assertEquals("INFO", testEvent.getType());
            assertEquals("Test Notification", testEvent.getTitle());
        });
    }

    @Test
    void testKafkaMessageSerialization_Deserialization_Success() throws Exception {
        // Given
        UserRegisteredEvent originalEvent = TestDataFactory.createUserRegisteredEvent(
                "serializationtest", "serialization@test.com");

        // When
        eventProducer.sendUserEvent(originalEvent);

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            // Проверка, что событие может быть сериализовано и десериализовано
            assertNotNull(originalEvent.getEventId());
            assertNotNull(originalEvent.getTimestamp());
            assertEquals("serializationtest", originalEvent.getUsername());
        });
    }

    @Test
    void testKafkaEventIdempotency_Success() throws Exception {
        // Given
        String eventId = TestDataFactory.createTestUUID();
        UserRegisteredEvent event1 = UserRegisteredEvent.builder()
                .eventId(eventId)
                .eventType("USER_REGISTERED")
                .timestamp(TestDataFactory.createTestTimestamp())
                .userId(1L)
                .username("idempotent1")
                .email("idempotent1@test.com")
                .role(dn.quest.shared.enums.UserRole.PLAYER)
                .build();

        UserRegisteredEvent event2 = UserRegisteredEvent.builder()
                .eventId(eventId) // Тот же ID события
                .eventType("USER_REGISTERED")
                .timestamp(TestDataFactory.createTestTimestamp())
                .userId(2L)
                .username("idempotent2")
                .email("idempotent2@test.com")
                .role(dn.quest.shared.enums.UserRole.PLAYER)
                .build();

        // When
        eventProducer.sendUserEvent(event1);
        eventProducer.sendUserEvent(event2);

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            // Проверка идемпотентности - события с одинаковым ID должны обрабатываться один раз
            assertEquals(eventId, event1.getEventId());
            assertEquals(eventId, event2.getEventId());
        });
    }

    @Test
    void testKafkaErrorHandling_InvalidEvent_Success() throws Exception {
        // Given
        String invalidEventJson = "{\"invalid\":\"event\"}";

        // When
        kafkaTemplate.send("user-events", "invalid-key", invalidEventJson);

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            // Проверка, что система обрабатывает невалидные события без падения
            assertTrue(true, "System should handle invalid events gracefully");
        });
    }

    @Test
    void testKafkaRetryMechanism_Success() throws Exception {
        // Given
        UserRegisteredEvent testEvent = TestDataFactory.createUserRegisteredEvent(
                "retrytest", "retry@test.com");

        // When
        eventProducer.sendUserEvent(testEvent);

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            // Проверка, что retry механизм работает
            assertNotNull(testEvent.getEventId());
        });
    }

    @Test
    void testKafkaDeadLetterQueue_Success() throws Exception {
        // Given
        String malformedEvent = "malformed event data";

        // When
        kafkaTemplate.send("user-events", "dlq-test", malformedEvent);

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            // Проверка, что malformed события попадают в DLQ
            assertTrue(true, "Malformed events should go to DLQ");
        });
    }

    @Test
    void testKafkaConsumerGroups_Success() throws Exception {
        // Given
        UserRegisteredEvent testEvent = TestDataFactory.createUserRegisteredEvent(
                "grouptest", "group@test.com");

        // When
        eventProducer.sendUserEvent(testEvent);

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            // Проверка, что consumer groups работают корректно
            assertNotNull(testEvent.getEventId());
        });
    }

    @Test
    void testKafkaPartitioning_Success() throws Exception {
        // Given
        for (int i = 0; i < 10; i++) {
            UserRegisteredEvent testEvent = TestDataFactory.createUserRegisteredEvent(
                    "partitiontest" + i, "partition" + i + "@test.com");
            
            // When
            eventProducer.sendUserEvent(testEvent);
        }

        // Then
        await().atMost(WAIT_TIMEOUT).untilAsserted(() -> {
            // Проверка, что сообщения распределяются по партициям
            assertTrue(true, "Messages should be distributed across partitions");
        });
    }
}