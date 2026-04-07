package dn.quest.notification.entity;

import dn.quest.notification.enums.NotificationCategory;
import dn.quest.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Сущность пользовательских предпочтений уведомлений
 */
@Entity
@Table(name = "user_notification_preferences", indexes = {
    @Index(name = "idx_preferences_user_id", columnList = "userId", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID пользователя (UUID)
     */
    @Column(nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID userId;

    /**
     * Email уведомления включены
     */
    @Column(nullable = false)
    private Boolean emailEnabled;

    /**
     * Push уведомления включены
     */
    @Column(nullable = false)
    private Boolean pushEnabled;

    /**
     * In-app уведомления включены
     */
    @Column(nullable = false)
    private Boolean inAppEnabled;

    /**
     * Telegram уведомления включены
     */
    @Column(nullable = false)
    private Boolean telegramEnabled;

    /**
     * SMS уведомления включены
     */
    @Column(nullable = false)
    private Boolean smsEnabled;

    /**
     * Приветственные уведомления включены
     */
    @Column(nullable = false)
    private Boolean welcomeEnabled;

    /**
     * Уведомления о квестах включены
     */
    @Column(nullable = false)
    private Boolean questEnabled;

    /**
     * Игровые уведомления включены
     */
    @Column(nullable = false)
    private Boolean gameEnabled;

    /**
     * Командные уведомления включены
     */
    @Column(nullable = false)
    private Boolean teamEnabled;

    /**
     * Системные уведомления включены
     */
    @Column(nullable = false)
    private Boolean systemEnabled;

    /**
     * Уведомления о безопасности включены
     */
    @Column(nullable = false)
    private Boolean securityEnabled;

    /**
     * Маркетинговые уведомления включены
     */
    @Column(nullable = false)
    private Boolean marketingEnabled;

    /**
     * Напоминания включены
     */
    @Column(nullable = false)
    private Boolean reminderEnabled;

    /**
     * Do Not Disturb режим включен
     */
    @Column(nullable = false)
    private Boolean doNotDisturbEnabled;

    /**
     * Начало Do Not Disturb периода (часы, 0-23)
     */
    private Integer doNotDisturbStartHour;

    /**
     * Конец Do Not Disturb периода (часы, 0-23)
     */
    private Integer doNotDisturbEndHour;

    /**
     * Максимальное количество уведомлений в час
     */
    private Integer maxNotificationsPerHour;

    /**
     * Максимальное количество уведомлений в день
     */
    private Integer maxNotificationsPerDay;

    /**
     * Предпочитаемый язык уведомлений
     */
    @Column(length = 5)
    private String preferredLanguage;

    /**
     * Часовой пояс пользователя
     */
    @Column(length = 50)
    private String timeZone;

    /**
     * Email пользователя
     */
    @Column(length = 255)
    private String email;

    /**
     * Телефон пользователя
     */
    @Column(length = 20)
    private String phone;

    /**
     * Telegram chat ID пользователя
     */
    @Column(length = 50)
    private String telegramChatId;

    /**
     * FCM token пользователя
     */
    @Column(length = 255)
    private String fcmToken;

    /**
     * Время создания
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Время последнего обновления
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * ID пользователя, обновившего настройки (UUID)
     */
    private UUID updatedBy;

    /**
     * Дополнительные настройки в JSON формате
     */
    @Column(columnDefinition = "JSON")
    private String additionalSettings;
}