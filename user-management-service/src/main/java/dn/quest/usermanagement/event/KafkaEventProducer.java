package dn.quest.usermanagement.event;

import dn.quest.shared.events.EventProducer;
import dn.quest.shared.events.user.UserRegisteredEvent;
import dn.quest.shared.events.user.UserUpdatedEvent;
import dn.quest.shared.events.user.UserDeletedEvent;
import dn.quest.shared.events.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Сервис для публикации событий в Kafka
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final EventProducer eventProducer;

    /**
     * Публикация события регистрации пользователя
     */
    public void publishUserRegisteredEvent(Long userId, String username, String email, String publicName) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID().toString(),
                userId,
                username,
                email,
                publicName
        );
        eventProducer.publishUserRegisteredEvent(event);
        log.info("Published user registered event for user: {}", userId);
    }

    /**
     * Публикация события обновления пользователя
     */
    public void publishUserUpdatedEvent(Long userId, String username, String email, String publicName) {
        UserUpdatedEvent event = new UserUpdatedEvent(
                UUID.randomUUID().toString(),
                userId,
                username,
                email,
                publicName
        );
        eventProducer.publishUserUpdatedEvent(event);
        log.info("Published user updated event for user: {}", userId);
    }

    /**
     * Публикация события удаления пользователя
     */
    public void publishUserDeletedEvent(Long userId, String username) {
        UserDeletedEvent event = new UserDeletedEvent(
                UUID.randomUUID().toString(),
                userId,
                username
        );
        eventProducer.publishUserDeletedEvent(event);
        log.info("Published user deleted event for user: {}", userId);
    }

    /**
     * Публикация события уведомления пользователя
     */
    public void publishUserNotificationEvent(Long userId, String title, String message, String type) {
        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID().toString(),
                userId,
                title,
                message,
                type
        );
        eventProducer.publishNotificationEvent(event);
        log.info("Published user notification event for user: {}", userId);
    }

    /**
     * Публикация события блокировки пользователя
     */
    public void publishUserBlockedEvent(Long userId, String username, String reason) {
        publishUserNotificationEvent(
                userId,
                "Аккаунт заблокирован",
                String.format("Ваш аккаунт был заблокирован. Причина: %s", reason),
                "USER_BLOCKED"
        );
        log.info("Published user blocked event for user: {}", userId);
    }

    /**
     * Публикация события разблокировки пользователя
     */
    public void publishUserUnblockedEvent(Long userId, String username) {
        publishUserNotificationEvent(
                userId,
                "Аккаунт разблокирован",
                "Ваш аккаунт был разблокирован",
                "USER_UNBLOCKED"
        );
        log.info("Published user unblocked event for user: {}", userId);
    }

    /**
     * Публикация события изменения роли пользователя
     */
    public void publishUserRoleChangedEvent(Long userId, String username, String newRole) {
        publishUserNotificationEvent(
                userId,
                "Изменение роли",
                String.format("Ваша роль была изменена на: %s", newRole),
                "USER_ROLE_CHANGED"
        );
        log.info("Published user role changed event for user: {}", userId);
    }

    /**
     * Публикация события обновления аватара пользователя
     */
    public void publishUserAvatarUpdatedEvent(Long userId, String username, String avatarUrl) {
        publishUserNotificationEvent(
                userId,
                "Аватар обновлен",
                "Ваш аватар был успешно обновлен",
                "USER_AVATAR_UPDATED"
        );
        log.info("Published user avatar updated event for user: {}", userId);
    }

    /**
     * Публикация события активности пользователя
     */
    public void publishUserActivityEvent(Long userId, String username, String activityType, String description) {
        publishUserNotificationEvent(
                userId,
                "Активность",
                description,
                activityType
        );
        log.debug("Published user activity event for user: {}, activity: {}", userId, activityType);
    }
}