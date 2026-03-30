package dn.quest.shared.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.events.user.UserRegisteredEvent;
import dn.quest.shared.events.user.UserUpdatedEvent;
import dn.quest.shared.events.user.UserDeletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционный тест для пользовательских событий
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {KafkaTopics.USER_EVENTS})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class UserEventIntegrationTest extends KafkaTestBase {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final List<BaseEvent> receivedEvents = new ArrayList<>();
    private final CountDownLatch latch = new CountDownLatch(3);

    @Test
    @DisplayName("Должен отправлять и получать UserRegisteredEvent")
    void shouldSendAndReceiveUserRegisteredEvent() throws Exception {
        // Given
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .userId(123L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(KafkaTopics.USER_EVENTS, event.getUserId().toString(), eventJson);
        kafkaTemplate.flush();

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Ожидание получения события");
        
        UserRegisteredEvent receivedEvent = (UserRegisteredEvent) receivedEvents.stream()
                .filter(e -> e instanceof UserRegisteredEvent)
                .findFirst()
                .orElse(null);
        
        assertNotNull(receivedEvent);
        assertEquals(event.getEventId(), receivedEvent.getEventId());
        assertEquals(event.getUserId(), receivedEvent.getUserId());
        assertEquals(event.getUsername(), receivedEvent.getUsername());
        assertEquals(event.getEmail(), receivedEvent.getEmail());
    }

    @Test
    @DisplayName("Должен отправлять и получать UserUpdatedEvent")
    void shouldSendAndReceiveUserUpdatedEvent() throws Exception {
        // Given
        UserUpdatedEvent event = UserUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .userId(456L)
                .username("updateduser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(KafkaTopics.USER_EVENTS, event.getUserId().toString(), eventJson);
        kafkaTemplate.flush();

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Ожидание получения события");
        
        UserUpdatedEvent receivedEvent = (UserUpdatedEvent) receivedEvents.stream()
                .filter(e -> e instanceof UserUpdatedEvent)
                .findFirst()
                .orElse(null);
        
        assertNotNull(receivedEvent);
        assertEquals(event.getEventId(), receivedEvent.getEventId());
        assertEquals(event.getUserId(), receivedEvent.getUserId());
        assertEquals(event.getUsername(), receivedEvent.getUsername());
    }

    @Test
    @DisplayName("Должен отправлять и получать UserDeletedEvent")
    void shouldSendAndReceiveUserDeletedEvent() throws Exception {
        // Given
        UserDeletedEvent event = UserDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .userId(789L)
                .username("deleteduser")
                .reason("Account deletion request")
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(KafkaTopics.USER_EVENTS, event.getUserId().toString(), eventJson);
        kafkaTemplate.flush();

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Ожидание получения события");
        
        UserDeletedEvent receivedEvent = (UserDeletedEvent) receivedEvents.stream()
                .filter(e -> e instanceof UserDeletedEvent)
                .findFirst()
                .orElse(null);
        
        assertNotNull(receivedEvent);
        assertEquals(event.getEventId(), receivedEvent.getEventId());
        assertEquals(event.getUserId(), receivedEvent.getUserId());
        assertEquals(event.getReason(), receivedEvent.getReason());
    }

    @Test
    @DisplayName("Должен обрабатывать несколько событий последовательно")
    void shouldHandleMultipleEventsSequentially() throws Exception {
        // Given
        UserRegisteredEvent registerEvent = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .userId(100L)
                .username("sequentialuser")
                .email("sequential@example.com")
                .firstName("Sequential")
                .lastName("User")
                .build();

        UserUpdatedEvent updateEvent = UserUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .userId(100L)
                .username("sequentialuser_updated")
                .email("sequential_updated@example.com")
                .firstName("Sequential")
                .lastName("User")
                .build();

        UserDeletedEvent deleteEvent = UserDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .userId(100L)
                .username("sequentialuser_updated")
                .reason("Test cleanup")
                .build();

        // When
        kafkaTemplate.send(KafkaTopics.USER_EVENTS, registerEvent.getUserId().toString(), 
                          objectMapper.writeValueAsString(registerEvent));
        kafkaTemplate.send(KafkaTopics.USER_EVENTS, updateEvent.getUserId().toString(), 
                          objectMapper.writeValueAsString(updateEvent));
        kafkaTemplate.send(KafkaTopics.USER_EVENTS, deleteEvent.getUserId().toString(), 
                          objectMapper.writeValueAsString(deleteEvent));
        kafkaTemplate.flush();

        // Then
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Ожидание получения всех событий");
        assertEquals(3, receivedEvents.size());

        assertInstanceOf(UserRegisteredEvent.class, receivedEvents.get(0));
        assertInstanceOf(UserUpdatedEvent.class, receivedEvents.get(1));
        assertInstanceOf(UserDeletedEvent.class, receivedEvents.get(2));
    }

    @KafkaListener(topics = KafkaTopics.USER_EVENTS, 
                   groupId = "test-user-events-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleUserEvents(BaseEvent event) {
        receivedEvents.add(event);
        latch.countDown();
    }
}