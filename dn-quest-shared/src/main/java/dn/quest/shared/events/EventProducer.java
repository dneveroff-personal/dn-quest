package dn.quest.shared.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.constants.KafkaTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.Duration;
import java.util.UUID;

/**
 * Базовый продюсер для публикации событий в Kafka
 */
@Slf4j
@Component
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    // Метрики
    private final Counter kafkaEventPublishedCounter;
    private final Counter kafkaEventPublishedByTypeCounter;
    private final Counter kafkaEventPublishedByTopicCounter;
    private final Timer kafkaEventPublishTimer;

    public EventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                        ObjectMapper objectMapper,
                        Counter kafkaEventPublishedCounter,
                        Counter kafkaEventPublishedByTypeCounter,
                        Counter kafkaEventPublishedByTopicCounter,
                        Timer kafkaEventPublishTimer) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.kafkaEventPublishedCounter = kafkaEventPublishedCounter;
        this.kafkaEventPublishedByTypeCounter = kafkaEventPublishedByTypeCounter;
        this.kafkaEventPublishedByTopicCounter = kafkaEventPublishedByTopicCounter;
        this.kafkaEventPublishTimer = kafkaEventPublishTimer;
    }

    /**
     * Публикация события в указанный топик
     */
    public void publishEvent(String topic, BaseEvent event) {
        Timer.Sample sample = Timer.start();
        
        try {
            // Устанавливаем обязательные поля если они не установлены
            if (event.getEventId() == null) {
                event.setEventId(UUID.randomUUID().toString());
            }
            if (event.getTimestamp() == null) {
                event.setTimestamp(java.time.Instant.now());
            }
            if (event.getEventVersion() == null) {
                event.setEventVersion("1.0");
            }

            String key = generateEventKey(event);
            
            ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
            
            future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                @Override
                public void onSuccess(SendResult<String, Object> result) {
                    log.info("Event published successfully: {} to topic: {} with key: {}",
                            event.getEventType(), topic, key);
                    
                    // Обновляем метрики
                    kafkaEventPublishedCounter.increment();
                    kafkaEventPublishedByTypeCounter.increment("event_type", event.getEventType());
                    kafkaEventPublishedByTopicCounter.increment("topic", topic);
                    sample.stop(kafkaEventPublishTimer);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    log.error("Failed to publish event: {} to topic: {} with key: {}",
                            event.getEventType(), topic, key, throwable);
                    sample.stop(kafkaEventPublishTimer);
                }
            });
            
        } catch (Exception e) {
            log.error("Error publishing event: {} to topic: {}", event.getEventType(), topic, e);
            sample.stop(kafkaEventPublishTimer);
        }
    }

    /**
     * Публикация пользовательского события
     */
    public void publishUserEvent(BaseEvent event) {
        publishEvent(KafkaTopics.USER_EVENTS, event);
    }

    /**
     * Публикация события квеста
     */
    public void publishQuestEvent(BaseEvent event) {
        publishEvent(KafkaTopics.QUEST_EVENTS, event);
    }

    /**
     * Публикация игрового события
     */
    public void publishGameEvent(BaseEvent event) {
        publishEvent(KafkaTopics.GAME_EVENTS, event);
    }

    /**
     * Публикация командного события
     */
    public void publishTeamEvent(BaseEvent event) {
        publishEvent(KafkaTopics.TEAM_EVENTS, event);
    }

    /**
     * Публикация файлового события
     */
    public void publishFileEvent(BaseEvent event) {
        publishEvent(KafkaTopics.FILE_EVENTS, event);
    }

    /**
     * Публикация события уведомления
     */
    public void publishNotificationEvent(BaseEvent event) {
        publishEvent(KafkaTopics.NOTIFICATION_EVENTS, event);
    }

    /**
     * Публикация статистического события
     */
    public void publishStatisticsEvent(BaseEvent event) {
        publishEvent(KafkaTopics.STATISTICS_EVENTS, event);
    }

    /**
     * Генерация ключа для события на основе его типа и данных
     */
    private String generateEventKey(BaseEvent event) {
        if (event.getData() != null && event.getData().containsKey("userId")) {
            return "user-" + event.getData().get("userId");
        }
        if (event.getData() != null && event.getData().containsKey("questId")) {
            return "quest-" + event.getData().get("questId");
        }
        if (event.getData() != null && event.getData().containsKey("teamId")) {
            return "team-" + event.getData().get("teamId");
        }
        if (event.getData() != null && event.getData().containsKey("sessionId")) {
            return "session-" + event.getData().get("sessionId");
        }
        return event.getEventType() + "-" + event.getEventId();
    }

    /**
     * Создание базового события с обязательными полями
     */
    public BaseEvent createBaseEvent(String eventType, String source, String correlationId) {
        return BaseEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .eventVersion("1.0")
                .timestamp(java.time.Instant.now())
                .source(source)
                .correlationId(correlationId)
                .build();
    }

    /**
     * Создание базового события с метаданными
     */
    public BaseEvent createBaseEvent(String eventType, String source, String correlationId, 
                                   BaseEvent.EventMetadata metadata) {
        return BaseEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .eventVersion("1.0")
                .timestamp(java.time.Instant.now())
                .source(source)
                .correlationId(correlationId)
                .metadata(metadata)
                .build();
    }
}