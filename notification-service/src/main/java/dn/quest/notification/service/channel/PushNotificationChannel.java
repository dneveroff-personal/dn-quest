package dn.quest.notification.service.channel;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import dn.quest.notification.entity.Notification;
import dn.quest.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализация Push канала доставки уведомлений через Firebase Cloud Messaging
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PushNotificationChannel implements NotificationChannel {

    private final FirebaseMessaging firebaseMessaging;

    @Value("${app.notification.push.enabled:true}")
    private boolean pushEnabled;

    @Value("${app.notification.push.default-icon:https://example.com/icon.png}")
    private String defaultIcon;

    @Value("${app.notification.push.default-click-action:https://dn-quest.com}")
    private String defaultClickAction;

    @Override
    public String getChannelType() {
        return "push";
    }

    @Override
    public boolean isAvailable() {
        return pushEnabled && firebaseMessaging != null;
    }

    @Override
    public boolean canSend(Notification notification) {
        if (notification.getType() != NotificationType.PUSH) {
            return false;
        }

        String fcmToken = notification.getFcmToken();
        return fcmToken != null && !fcmToken.isEmpty();
    }

    @Override
    public boolean validate(Notification notification) {
        if (notification.getSubject() == null || notification.getSubject().isEmpty()) {
            log.warn("Push notification validation failed: missing subject");
            return false;
        }

        if (notification.getContent() == null || notification.getContent().isEmpty()) {
            log.warn("Push notification validation failed: missing content");
            return false;
        }

        // Проверка длины заголовка и контента (ограничения FCM)
        if (notification.getSubject().length() > 100) {
            log.warn("Push notification validation failed: subject too long (max 100 chars)");
            return false;
        }

        if (notification.getContent().length() > 1000) {
            log.warn("Push notification validation failed: content too long (max 1000 chars)");
            return false;
        }

        return canSend(notification);
    }

    @Override
    public NotificationChannelResult send(Notification notification) {
        if (!validate(notification)) {
            return NotificationChannelResult.failure("Push notification validation failed");
        }

        try {
            // Создание FCM уведомления
            Notification fcmNotification = Notification.builder()
                    .setTitle(notification.getSubject())
                    .setBody(truncateContent(notification.getContent(), 1000))
                    .build();

            // Создание сообщения
            Message.Builder messageBuilder = Message.builder()
                    .setToken(notification.getFcmToken())
                    .setNotification(fcmNotification);

            // Добавление данных в сообщение
            Map<String, String> data = new HashMap<>();
            data.put("notificationId", notification.getNotificationId());
            data.put("userId", notification.getUserId().toString());
            data.put("category", notification.getCategory().getValue());
            data.put("priority", notification.getPriority().getValue());
            data.put("type", "notification");
            
            if (notification.getRelatedEntityId() != null) {
                data.put("relatedEntityId", notification.getRelatedEntityId());
                data.put("relatedEntityType", notification.getRelatedEntityType());
            }

            messageBuilder.putAllData(data);

            // Добавление настроек отображения
            if (defaultIcon != null && !defaultIcon.isEmpty()) {
                messageBuilder.setAndroidConfig(
                    com.google.firebase.messaging.AndroidConfig.builder()
                        .setNotification(
                            com.google.firebase.messaging.AndroidNotification.builder()
                                .setIcon(defaultIcon)
                                .setClickAction(defaultClickAction)
                                .build()
                        )
                        .build()
                );
            }

            Message message = messageBuilder.build();

            // Отправка сообщения
            String messageId = firebaseMessaging.send(message);

            log.info("Push notification sent successfully to user: {} for notification: {}", 
                    notification.getUserId(), notification.getNotificationId());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("userId", notification.getUserId());
            metadata.put("title", notification.getSubject());
            metadata.put("category", notification.getCategory().getValue());
            metadata.put("priority", notification.getPriority().getValue());

            return NotificationChannelResult.success(messageId, metadata);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification to user: {} for notification: {}", 
                    notification.getUserId(), notification.getNotificationId(), e);
            
            String errorCode = e.getMessagingErrorCode() != null ? 
                    e.getMessagingErrorCode().toString() : "FCM_ERROR";
            
            return NotificationChannelResult.failure(
                    "Push notification failed: " + e.getMessage(), 
                    errorCode
            );
        } catch (Exception e) {
            log.error("Unexpected error sending push notification to user: {} for notification: {}", 
                    notification.getUserId(), notification.getNotificationId(), e);
            return NotificationChannelResult.failure(
                    "Unexpected error: " + e.getMessage(), 
                    "PUSH_UNKNOWN_ERROR"
            );
        }
    }

    @Override
    public double getCost(Notification notification) {
        // Push уведомления обычно бесплатны до определенного лимита
        return 0.0;
    }

    @Override
    public int getPriority() {
        return 3; // Высокий приоритет для мгновенных уведомлений
    }

    /**
     * Обрезка контента до максимальной длины
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        
        if (content.length() <= maxLength) {
            return content;
        }
        
        return content.substring(0, maxLength - 3) + "...";
    }

    /**
     * Проверка валидности FCM токена
     */
    private boolean isValidFcmToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // Базовая проверка формата токена FCM
        // FCM токены обычно имеют длину около 150-200 символов
        return token.length() >= 100 && token.length() <= 500;
    }
}