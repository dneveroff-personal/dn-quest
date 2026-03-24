package dn.quest.authentication.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.events.BaseEvent;
import dn.quest.shared.events.user.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka продюсер для публикации событий
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.user-events:dn-quest.users.events}")
    private String userEventsTopic;

    /**
     * Публикация события регистрации пользователя
     */
    public void publishUserRegistered(UserEvent.UserRegisteredData data, String correlationId) {
        BaseEvent event = BaseEvent.builder()
                .eventType("UserRegisteredEvent")
                .data(data)
                .correlationId(correlationId)
                .metadata(createMetadata(data.getUserId(), data.getUsername()))
                .build();

        publishEvent(userEventsTopic, event);
    }

    /**
     * Публикация события обновления пользователя
     */
    public void publishUserUpdated(UserEvent.UserUpdatedData data, String correlationId) {
        BaseEvent event = BaseEvent.builder()
                .eventType("UserUpdatedEvent")
                .data(data)
                .correlationId(correlationId)
                .metadata(createMetadata(data.getUserId(), data.getUsername()))
                .build();

        publishEvent(userEventsTopic, event);
    }

    /**
     * Публикация события удаления пользователя
     */
    public void publishUserDeleted(UserEvent.UserDeletedData data, String correlationId) {
        BaseEvent event = BaseEvent.builder()
                .eventType("UserDeletedEvent")
                .data(data)
                .correlationId(correlationId)
                .metadata(createMetadata(data.getUserId(), data.getUsername()))
                .build();

        publishEvent(userEventsTopic, event);
    }

    /**
     * Публикация события смены пароля
     */
    public void publishUserPasswordChanged(UserEvent.UserPasswordChangedData data, String correlationId) {
        BaseEvent event = BaseEvent.builder()
                .eventType("UserPasswordChangedEvent")
                .data(data)
                .correlationId(correlationId)
                .metadata(createMetadata(data.getUserId(), data.getUsername()))
                .build();

        publishEvent(userEventsTopic, event);
    }

    /**
     * Публикация события изменения роли
     */
    public void publishUserRoleChanged(UserEvent.UserRoleChangedData data, String correlationId) {
        BaseEvent event = BaseEvent.builder()
                .eventType("UserRoleChangedEvent")
                .data(data)
                .correlationId(correlationId)
                .metadata(createMetadata(data.getUserId(), data.getUsername()))
                .build();

        publishEvent(userEventsTopic, event);
    }

    /**
     * Публикация события входа пользователя
     */
    public void publishUserLoggedIn(UserEvent.UserLoggedInData data, String correlationId) {
        BaseEvent event = BaseEvent.builder()
                .eventType("UserLoggedInEvent")
                .data(data)
                .correlationId(correlationId)
                .metadata(createMetadata(data.getUserId(), data.getUsername()))
                .build();

        publishEvent(userEventsTopic, event);
    }

    /**
     * Публикация события выхода пользователя
     */
    public void publishUserLoggedOut(UserEvent.UserLoggedOutData data, String correlationId) {
        BaseEvent event = BaseEvent.builder()
                .eventType("UserLoggedOutEvent")
                .data(data)
                .correlationId(correlationId)
                .metadata(createMetadata(data.getUserId(), data.getUsername()))
                .build();

        publishEvent(userEventsTopic, event);
    }

    /**
     * Публикация события обновления разрешений
     */
    public void publishUserPermissionsUpdated(UserEvent.UserPermissionsUpdatedData data, String correlationId) {
        BaseEvent event = BaseEvent.builder()
                .eventType("UserPermissionsUpdatedEvent")
                .data(data)
                .correlationId(correlationId)
                .metadata(createMetadata(data.getUserId(), data.getUsername()))
                .build();

        publishEvent(userEventsTopic, event);
    }

    /**
     * Универсальный метод публикации события
     */
    private void publishEvent(String topic, BaseEvent event) {
        try {
            String jsonEvent = objectMapper.writeValueAsString(event);
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, event.getEventId(), jsonEvent);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Событие успешно опубликовано: {} в топик: {}", event.getEventType(), topic);
                } else {
                    log.error("Ошибка публикации события: {} в топик: {}", event.getEventType(), topic, ex);
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации события: {}", event.getEventType(), e);
        }
    }

    /**
     * Создание метаданных для события
     */
    private Map<String, Object> createMetadata(Long userId, String username) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", userId);
        metadata.put("username", username);
        return metadata;
    }
}