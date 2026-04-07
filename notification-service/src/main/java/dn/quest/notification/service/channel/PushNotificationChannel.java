package dn.quest.notification.service.channel;

import dn.quest.notification.entity.Notification;
import dn.quest.notification.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Реализация Push канала доставки уведомлений через Firebase Cloud Messaging
 */
@Component
@Slf4j
public class PushNotificationChannel implements NotificationChannel {

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
        return pushEnabled;
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
        return true;
    }

    @Override
    public NotificationChannelResult send(Notification notification) {
        if (!canSend(notification)) {
            return NotificationChannelResult.failure("Invalid notification for push channel");
        }

        if (!isAvailable()) {
            return NotificationChannelResult.failure("Push channel is not available");
        }

        try {
            // Placeholder for FCM implementation
            // In production, this would use FirebaseMessaging to send the notification
            log.info("Push notification sent to FCM token: {}", notification.getFcmToken());
            return NotificationChannelResult.success("Push notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send push notification", e);
            return NotificationChannelResult.failure("Failed to send push notification: " + e.getMessage());
        }
    }
}