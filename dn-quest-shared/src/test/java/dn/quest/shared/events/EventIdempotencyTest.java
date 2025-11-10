package dn.quest.shared.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.events.user.UserRegisteredEvent;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционный тест для проверки идемпотентности обработки событий
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"idempotency-test-topic"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventIdempotencyTest extends KafkaTestBase {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final List<BaseEvent> processedEvents = new ArrayList<>();
    private final Set<String> processedEventIds = new HashSet<>();
    private CountDownLatch latch;

    @Test
    @DisplayName("Должен обрабатывать дублирующиеся события идемпотентно")
    void shouldHandleDuplicateEventsIdempotently() throws Exception {
        // Given
        String eventId = UUID.randomUUID().toString();
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .eventId(eventId)
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .userId(123L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        String eventJson = objectMapper.writeValueAsString(event);
        latch = new CountDownLatch(3); // Ожидаем 3 сообщения, но обработаем только 1

        // When - отправляем одно и то же событие 3 раза
        kafkaTemplate.send("idempotency-test-topic", event.getUserId().toString(), eventJson);
        kafkaTemplate.send("idempotency-test-topic", event.getUserId().toString(), eventJson);
        kafkaTemplate.send("idempotency-test-topic", event.getUserId().toString(), eventJson);
        kafkaTemplate.flush();

        // Then
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Ожидание получения всех сообщений");
        
        // Должны получить все 3 сообщения
        assertEquals(3, processedEvents.size());
        
        // Но обработать только 1 уникальное событие
        assertEquals(1, processedEventIds.size());
        assertTrue(processedEventIds.contains(eventId));
        
        // Все полученные события должны иметь одинаковый eventId
        processedEvents.forEach(e -> assertEquals(eventId, e.getEventId()));
    }

    @Test
    @DisplayName("Должен обрабатывать разные события с одинаковым содержанием")
    void shouldHandleDifferentEventsWithSameContent() throws Exception {
        // Given
        UserRegisteredEvent event1 = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .userId(124L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        UserRegisteredEvent event2 = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .userId(124L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        latch = new CountDownLatch(2);

        // When
        kafkaTemplate.send("idempotency-test-topic", event1.getUserId().toString(), 
                          objectMapper.writeValueAsString(event1));
        kafkaTemplate.send("idempotency-test-topic", event2.getUserId().toString(), 
                          objectMapper.writeValueAsString(event2));
        kafkaTemplate.flush();

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Ожидание получения всех сообщений");
        
        // Должны обработать оба события, так как у них разные eventId
        assertEquals(2, processedEventIds.size());
        assertEquals(2, processedEvents.size());
        
        // Проверяем, что eventId разные
        assertNotEquals(processedEvents.get(0).getEventId(), processedEvents.get(1).getEventId());
        
        // Но содержимое одинаковое
        UserRegisteredEvent received1 = (UserRegisteredEvent) processedEvents.get(0);
        UserRegisteredEvent received2 = (UserRegisteredEvent) processedEvents.get(1);
        
        assertEquals(received1.getUserId(), received2.getUserId());
        assertEquals(received1.getUsername(), received2.getUsername());
        assertEquals(received1.getEmail(), received2.getEmail());
    }

    @Test
    @DisplayName("Должен обрабатывать события с одинаковым correlationId")
    void shouldHandleEventsWithSameCorrelationId() throws Exception {
        // Given
        String correlationId = UUID.randomUUID().toString();
        
        UserRegisteredEvent event1 = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .userId(125L)
                .username("user1")
                .email("user1@example.com")
                .firstName("User")
                .lastName("One")
                .build();

        UserRegisteredEvent event2 = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .userId(126L)
                .username("user2")
                .email("user2@example.com")
                .firstName("User")
                .lastName("Two")
                .build();

        latch = new CountDownLatch(2);

        // When
        kafkaTemplate.send("idempotency-test-topic", event1.getUserId().toString(), 
                          objectMapper.writeValueAsString(event1));
        kafkaTemplate.send("idempotency-test-topic", event2.getUserId().toString(), 
                          objectMapper.writeValueAsString(event2));
        kafkaTemplate.flush();

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Ожидание получения всех сообщений");
        
        // Должны обработать оба события, так как у них разные eventId
        assertEquals(2, processedEventIds.size());
        assertEquals(2, processedEvents.size());
        
        // Проверяем, что correlationId одинаковый
        processedEvents.forEach(e -> assertEquals(correlationId, e.getCorrelationId()));
        
        // Но eventId разные
        assertNotEquals(processedEvents.get(0).getEventId(), processedEvents.get(1).getEventId());
    }

    @KafkaListener(topics = "idempotency-test-topic", 
                   groupId = "idempotency-test-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleIdempotencyTestEvents(BaseEvent event) {
        // Имитируем идемпотентную обработку
        if (!processedEventIds.contains(event.getEventId())) {
            processedEventIds.add(event.getEventId());
            processedEvents.add(event);
            System.out.println("Processed new event: " + event.getEventId());
        } else {
            processedEvents.add(event); // Добавляем для подсчета полученных сообщений
            System.out.println("Duplicate event ignored: " + event.getEventId());
        }
        latch.countDown();
    }
}