package dn.quest.notification.entity;

import dn.quest.notification.enums.NotificationCategory;
import dn.quest.notification.enums.NotificationPriority;
import dn.quest.notification.enums.NotificationStatus;
import dn.quest.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Сущность уведомления
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user_id", columnList = "userId"),
    @Index(name = "idx_notification_status", columnList = "status"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_category", columnList = "category"),
    @Index(name = "idx_notification_created_at", columnList = "createdAt"),
    @Index(name = "idx_notification_scheduled_at", columnList = "scheduledAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Уникальный идентификатор уведомления
     */
    @Column(unique = true, nullable = false, length = 64)
    private String notificationId;

    /**
     * ID получателя уведомления (UUID пользователя)
     */
    @Column(nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    /**
     * Email получателя
     */
    @Column(length = 255)
    private String recipientEmail;

    /**
     * Телефон получателя (для SMS)
     */
    @Column(length = 20)
    private String recipientPhone;

    /**
     * Telegram chat ID получателя
     */
    @Column(length = 50)
    private String telegramChatId;

    /**
     * FCM token получателя (для push)
     */
    @Column(length = 255)
    private String fcmToken;

    /**
     * Тип уведомления
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    /**
     * Категория уведомления
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationCategory category;

    /**
     * Приоритет уведомления
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NotificationPriority priority;

    /**
     * Тема уведомления
     */
    @Column(length = 255)
    private String subject;

    /**
     * Содержание уведомления (текст)
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * HTML содержимое (для email)
     */
    @Column(columnDefinition = "TEXT")
    private String htmlContent;

    /**
     * Дополнительные данные для шаблона в JSON формате
     */
    @Column(columnDefinition = "JSON")
    private String templateData;

    /**
     * ID связанной сущности
     */
    @Column(length = 64)
    private String relatedEntityId;

    /**
     * Тип связанной сущности
     */
    @Column(length = 50)
    private String relatedEntityType;

    /**
     * ID источника события, вызвавшего уведомление
     */
    @Column(length = 64)
    private String sourceEventId;

    /**
     * Тип источника события
     */
    @Column(length = 50)
    private String sourceEventType;

    /**
     * Статус уведомления
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    /**
     * Количество попыток отправки
     */
    @Column(nullable = false)
    private Integer retryCount;

    /**
     * Максимальное количество попыток
     */
    @Column(nullable = false)
    private Integer maxRetries;

    /**
     * Ошибка при отправке (если есть)
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Время создания уведомления
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Запланированное время отправки
     */
    private Instant scheduledAt;

    /**
     * Время отправки
     */
    private Instant sentAt;

    /**
     * Время доставки
     */
    private Instant deliveredAt;

    /**
     * Время прочтения
     */
    private Instant readAt;

    /**
     * Время последнего обновления
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * ID корреляции для трассировки
     */
    @Column(length = 64)
    private String correlationId;

    /**
     * Дополнительные метаданные в JSON формате
     */
    @Column(columnDefinition = "JSON")
    private String metadata;
}