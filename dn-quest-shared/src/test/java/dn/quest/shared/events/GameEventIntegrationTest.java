package dn.quest.shared.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.events.game.CodeSubmittedEvent;
import dn.quest.shared.events.game.GameSessionFinishedEvent;
import dn.quest.shared.events.game.GameSessionStartedEvent;
import dn.quest.shared.events.game.LevelCompletedEvent;
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
 * Интеграционный тест для игровых событий
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {KafkaTopics.GAME_EVENTS})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class GameEventIntegrationTest extends KafkaTestBase {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final List<BaseEvent> receivedEvents = new ArrayList<>();
    private CountDownLatch latch = new CountDownLatch(4);

    @Test
    @DisplayName("Должен отправлять и получать GameSessionStartedEvent")
    void shouldSendAndReceiveGameSessionStartedEvent() throws Exception {
        // Given
        GameSessionStartedEvent event = GameSessionStartedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .sessionId(1001L)
                .userId(123L)
                .teamId(456L)
                .questId(789L)
                .questName("Test Quest")
                .difficultyLevel("MEDIUM")
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(KafkaTopics.GAME_EVENTS, event.getSessionId().toString(), eventJson);
        kafkaTemplate.flush();

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Ожидание получения события");
        
        GameSessionStartedEvent receivedEvent = (GameSessionStartedEvent) receivedEvents.stream()
                .filter(e -> e instanceof GameSessionStartedEvent)
                .findFirst()
                .orElse(null);
        
        assertNotNull(receivedEvent);
        assertEquals(event.getEventId(), receivedEvent.getEventId());
        assertEquals(event.getSessionId(), receivedEvent.getSessionId());
        assertEquals(event.getUserId(), receivedEvent.getUserId());
        assertEquals(event.getQuestId(), receivedEvent.getQuestId());
        assertEquals(event.getQuestName(), receivedEvent.getQuestName());
    }

    @Test
    @DisplayName("Должен отправлять и получать CodeSubmittedEvent")
    void shouldSendAndReceiveCodeSubmittedEvent() throws Exception {
        // Given
        CodeSubmittedEvent event = CodeSubmittedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .sessionId(1002L)
                .userId(124L)
                .levelNumber(1)
                .code("print('Hello World')")
                .language("python")
                .success(true)
                .executionTime(150L)
                .memoryUsage(1024L)
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(KafkaTopics.GAME_EVENTS, event.getSessionId().toString(), eventJson);
        kafkaTemplate.flush();

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Ожидание получения события");
        
        CodeSubmittedEvent receivedEvent = (CodeSubmittedEvent) receivedEvents.stream()
                .filter(e -> e instanceof CodeSubmittedEvent)
                .findFirst()
                .orElse(null);
        
        assertNotNull(receivedEvent);
        assertEquals(event.getEventId(), receivedEvent.getEventId());
        assertEquals(event.getSessionId(), receivedEvent.getSessionId());
        assertEquals(event.getUserId(), receivedEvent.getUserId());
        assertEquals(event.getLevelNumber(), receivedEvent.getLevelNumber());
        assertEquals(event.getCode(), receivedEvent.getCode());
        assertEquals(event.getLanguage(), receivedEvent.getLanguage());
        assertTrue(receivedEvent.isSuccess());
    }

    @Test
    @DisplayName("Должен отправлять и получать LevelCompletedEvent")
    void shouldSendAndReceiveLevelCompletedEvent() throws Exception {
        // Given
        LevelCompletedEvent event = LevelCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .sessionId(1003L)
                .userId(125L)
                .levelNumber(2)
                .completionTime(300000L)
                .attemptsCount(3)
                .hintsUsed(1)
                .score(95)
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(KafkaTopics.GAME_EVENTS, event.getSessionId().toString(), eventJson);
        kafkaTemplate.flush();

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Ожидание получения события");
        
        LevelCompletedEvent receivedEvent = (LevelCompletedEvent) receivedEvents.stream()
                .filter(e -> e instanceof LevelCompletedEvent)
                .findFirst()
                .orElse(null);
        
        assertNotNull(receivedEvent);
        assertEquals(event.getEventId(), receivedEvent.getEventId());
        assertEquals(event.getSessionId(), receivedEvent.getSessionId());
        assertEquals(event.getUserId(), receivedEvent.getUserId());
        assertEquals(event.getLevelNumber(), receivedEvent.getLevelNumber());
        assertEquals(event.getCompletionTime(), receivedEvent.getCompletionTime());
        assertEquals(event.getScore(), receivedEvent.getScore());
    }

    @Test
    @DisplayName("Должен отправлять и получать GameSessionFinishedEvent")
    void shouldSendAndReceiveGameSessionFinishedEvent() throws Exception {
        // Given
        GameSessionFinishedEvent event = GameSessionFinishedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .sessionId(1004L)
                .userId(126L)
                .teamId(457L)
                .questId(790L)
                .completed(true)
                .totalScore(450)
                .totalTime(1800000L)
                .levelsCompleted(5)
                .totalAttempts(12)
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(KafkaTopics.GAME_EVENTS, event.getSessionId().toString(), eventJson);
        kafkaTemplate.flush();

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Ожидание получения события");
        
        GameSessionFinishedEvent receivedEvent = (GameSessionFinishedEvent) receivedEvents.stream()
                .filter(e -> e instanceof GameSessionFinishedEvent)
                .findFirst()
                .orElse(null);
        
        assertNotNull(receivedEvent);
        assertEquals(event.getEventId(), receivedEvent.getEventId());
        assertEquals(event.getSessionId(), receivedEvent.getSessionId());
        assertEquals(event.getUserId(), receivedEvent.getUserId());
        assertEquals(event.getQuestId(), receivedEvent.getQuestId());
        assertTrue(receivedEvent.isCompleted());
        assertEquals(event.getTotalScore(), receivedEvent.getTotalScore());
        assertEquals(event.getLevelsCompleted(), receivedEvent.getLevelsCompleted());
    }

    @Test
    @DisplayName("Должен обрабатывать полный игровой сценарий")
    void shouldHandleCompleteGameScenario() throws Exception {
        // Given
        String correlationId = UUID.randomUUID().toString();
        
        GameSessionStartedEvent startEvent = GameSessionStartedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .sessionId(2001L)
                .userId(200L)
                .teamId(500L)
                .questId(800L)
                .questName("Complete Test Quest")
                .difficultyLevel("EASY")
                .build();

        CodeSubmittedEvent codeEvent = CodeSubmittedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .sessionId(2001L)
                .userId(200L)
                .levelNumber(1)
                .code("solution code")
                .language("java")
                .success(true)
                .executionTime(100L)
                .memoryUsage(512L)
                .build();

        LevelCompletedEvent levelEvent = LevelCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .sessionId(2001L)
                .userId(200L)
                .levelNumber(1)
                .completionTime(60000L)
                .attemptsCount(1)
                .hintsUsed(0)
                .score(100)
                .build();

        GameSessionFinishedEvent finishEvent = GameSessionFinishedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .sessionId(2001L)
                .userId(200L)
                .teamId(500L)
                .questId(800L)
                .completed(true)
                .totalScore(100)
                .totalTime(60000L)
                .levelsCompleted(1)
                .totalAttempts(1)
                .build();

        // When
        kafkaTemplate.send(KafkaTopics.GAME_EVENTS, startEvent.getSessionId().toString(), 
                          objectMapper.writeValueAsString(startEvent));
        kafkaTemplate.send(KafkaTopics.GAME_EVENTS, codeEvent.getSessionId().toString(), 
                          objectMapper.writeValueAsString(codeEvent));
        kafkaTemplate.send(KafkaTopics.GAME_EVENTS, levelEvent.getSessionId().toString(), 
                          objectMapper.writeValueAsString(levelEvent));
        kafkaTemplate.send(KafkaTopics.GAME_EVENTS, finishEvent.getSessionId().toString(), 
                          objectMapper.writeValueAsString(finishEvent));
        kafkaTemplate.flush();

        // Then
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Ожидание получения всех событий");
        assertEquals(4, receivedEvents.size());
        
        // Проверяем последовательность событий
        assertTrue(receivedEvents.get(0) instanceof GameSessionStartedEvent);
        assertTrue(receivedEvents.get(1) instanceof CodeSubmittedEvent);
        assertTrue(receivedEvents.get(2) instanceof LevelCompletedEvent);
        assertTrue(receivedEvents.get(3) instanceof GameSessionFinishedEvent);
        
        // Проверяем correlationId
        receivedEvents.forEach(event -> 
            assertEquals(correlationId, event.getCorrelationId()));
    }

    @KafkaListener(topics = KafkaTopics.GAME_EVENTS, 
                   groupId = "test-game-events-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleGameEvents(BaseEvent event) {
        receivedEvents.add(event);
        latch.countDown();
    }
}