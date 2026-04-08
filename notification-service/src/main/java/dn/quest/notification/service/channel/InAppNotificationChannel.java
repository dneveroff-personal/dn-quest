package dn.quest.notification.service.channel;

import dn.quest.notification.entity.Notification;
import dn.quest.notification.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация In-app канала доставки уведомлений через WebSocket
 */
@Component
@Slf4j
public class InAppNotificationChannel implements NotificationChannel {

    // Инжектируется через setter — не ломает старт, если WebSocket не сконфигурирован
    private SimpMessagingTemplate messagingTemplate;

    @Value("${app.notification.in-app.enabled:false}")
    private boolean inAppEnabled;

    @Value("${app.notification.in-app.destination:/topic/notifications}")
    private String notificationDestination;

    @Value("${app.notification.in-app.user-destination:/user/{userId}/notifications}")
    private String userNotificationDestination;

    // Кэш активных подключений пользователей
    private final Map<UUID, Boolean> activeUsers = new ConcurrentHashMap<>();

    // Конструктор без аргументов — Spring создаёт бин без проблем
    public InAppNotificationChannel() {
        log.info("InAppNotificationChannel: initialized (WebSocket availability will be checked at runtime)");
    }

    // required = false + @Nullable — инжектируется только если бин SimpMessagingTemplate существует
    @Autowired(required = false)
    public void setMessagingTemplate(@Nullable SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        if (messagingTemplate != null) {
            log.info("InAppNotificationChannel: SimpMessagingTemplate found, WebSocket enabled");
        } else {
            log.warn("InAppNotificationChannel: SimpMessagingTemplate not available, in-app notifications will be disabled");
        }
    }

    @Override
    public String getChannelType() {
        return "in_app";
    }

    @Override
    public boolean isAvailable() {
        return inAppEnabled && messagingTemplate != null;
    }

    @Override
    public boolean canSend(Notification notification) {
        if (notification.getType() != NotificationType.IN_APP) {
            return false;
        }

        // Проверяем, есть ли активное соединение у пользователя
        return isUserActive(notification.getUserId());
    }

    @Override
    public boolean validate(Notification notification) {
        if (notification.getSubject() == null || notification.getSubject().isEmpty()) {
            log.warn("In-app notification validation failed: missing subject");
            return false;
        }

        if (notification.getContent() == null || notification.getContent().isEmpty()) {
            log.warn("In-app notification validation failed: missing content");
            return false;
        }

        return canSend(notification);
    }

    @Override
    public NotificationChannelResult send(Notification notification) {
        if (!validate(notification)) {
            return NotificationChannelResult.failure("In-app notification validation failed");
        }

        try {
            // Создание payload для WebSocket
            Map<String, Object> payload = createNotificationPayload(notification);

            // Отправка уведомления конкретному пользователю
            String destination = userNotificationDestination.replace("{userId}", notification.getUserId().toString());

            messagingTemplate.convertAndSend(destination, payload);

            log.info("In-app notification sent successfully to user: {} for notification: {}",
                    notification.getUserId(), notification.getNotificationId());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("userId", notification.getUserId());
            metadata.put("destination", destination);
            metadata.put("deliveredImmediately", true);

            return NotificationChannelResult.success(
                    "inapp_" + notification.getNotificationId() + "_" + System.currentTimeMillis(),
                    metadata
            );

        } catch (Exception e) {
            log.error("Failed to send in-app notification to user: {} for notification: {}",
                    notification.getUserId(), notification.getNotificationId(), e);
            return NotificationChannelResult.failure(
                    "In-app notification failed: " + e.getMessage(),
                    "IN_APP_SEND_ERROR"
            );
        }
    }

    @Override
    public double getCost(Notification notification) {
        // In-app уведомления бесплатны
        return 0.0;
    }

    @Override
    public int getPriority() {
        return 4; // Самый высокий приоритет для мгновенных уведомлений
    }

    /**
     * Создание payload для уведомления
     */
    private Map<String, Object> createNotificationPayload(Notification notification) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("id", notification.getNotificationId());
        payload.put("type", notification.getType().getValue());
        payload.put("category", notification.getCategory().getValue());
        payload.put("priority", notification.getPriority().getValue());
        payload.put("subject", notification.getSubject());
        payload.put("content", notification.getContent());
        payload.put("createdAt", notification.getCreatedAt().toString());
        payload.put("userId", notification.getUserId());

        if (notification.getRelatedEntityId() != null) {
            Map<String, String> relatedEntity = new HashMap<>();
            relatedEntity.put("id", notification.getRelatedEntityId());
            relatedEntity.put("type", notification.getRelatedEntityType());
            payload.put("relatedEntity", relatedEntity);
        }

        if (notification.getTemplateData() != null && !notification.getTemplateData().isEmpty()) {
            payload.put("templateData", notification.getTemplateData());
        }

        if (notification.getMetadata() != null && !notification.getMetadata().isEmpty()) {
            payload.put("metadata", notification.getMetadata());
        }

        return payload;
    }

    /**
     * Проверка, активен ли пользователь
     */
    private boolean isUserActive(UUID userId) {
        return activeUsers.getOrDefault(userId, false);
    }

    /**
     * Регистрация пользователя как активного
     */
    public void registerUser(UUID userId) {
        activeUsers.put(userId, true);
        log.debug("User {} registered as active for in-app notifications", userId);
    }

    /**
     * Удаление пользователя из активных
     */
    public void unregisterUser(UUID userId) {
        activeUsers.remove(userId);
        log.debug("User {} unregistered from in-app notifications", userId);
    }

    /**
     * Отправка системного уведомления всем активным пользователям
     */
    public NotificationChannelResult broadcastToAll(Notification notification) {
        if (!isAvailable()) {
            return NotificationChannelResult.failure("In-app channel not available");
        }

        try {
            Map<String, Object> payload = createNotificationPayload(notification);
            payload.put("broadcast", true);
            payload.put("broadcastAt", Instant.now().toString());

            messagingTemplate.convertAndSend(notificationDestination, payload);

            log.info("Broadcast in-app notification sent for notification: {}", notification.getNotificationId());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("broadcast", true);
            metadata.put("activeUsers", activeUsers.size());

            return NotificationChannelResult.success(
                    "broadcast_" + notification.getNotificationId() + "_" + System.currentTimeMillis(),
                    metadata
            );

        } catch (Exception e) {
            log.error("Failed to broadcast in-app notification: {}", notification.getNotificationId(), e);
            return NotificationChannelResult.failure(
                    "Broadcast failed: " + e.getMessage(),
                    "IN_APP_BROADCAST_ERROR"
            );
        }
    }

    /**
     * Получение количества активных пользователей
     */
    public int getActiveUsersCount() {
        return activeUsers.size();
    }

    /**
     * Очистка неактивных пользователей (можно вызывать периодически)
     */
    public void cleanupInactiveUsers() {
        // Здесь можно добавить логику проверки времени последней активности
        // Для простоты пока оставим как есть
        log.debug("Active users cleanup completed. Current active users: {}", activeUsers.size());
    }
}