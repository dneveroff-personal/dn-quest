package dn.quest.notification.dto;

import dn.quest.notification.enums.NotificationCategory;
import dn.quest.notification.enums.NotificationPriority;
import dn.quest.notification.enums.NotificationStatus;
import dn.quest.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO для уведомления
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    /**
     * ID записи в базе данных
     */
    private Long id;

    /**
     * Уникальный идентификатор уведомления
     */
    private String notificationId;

    /**
     * ID получателя уведомления
     */
    private Long userId;

    /**
     * Тип уведомления
     */
    private NotificationType type;

    /**
     * Категория уведомления
     */
    private NotificationCategory category;

    /**
     * Приоритет уведомления
     */
    private NotificationPriority priority;

    /**
     * Тема уведомления
     */
    private String subject;

    /**
     * Содержание уведомления
     */
    private String content;

    /**
     * HTML содержимое
     */
    private String htmlContent;

    /**
     * Статус уведомления
     */
    private NotificationStatus status;

    /**
     * Количество попыток отправки
     */
    private Integer retryCount;

    /**
     * Максимальное количество попыток
     */
    private Integer maxRetries;

    /**
     * Ошибка при отправке
     */
    private String errorMessage;

    /**
     * ID связанной сущности
     */
    private String relatedEntityId;

    /**
     * Тип связанной сущности
     */
    private String relatedEntityType;

    /**
     * Время создания
     */
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
    private Instant updatedAt;

    /**
     * ID корреляции
     */
    private String correlationId;
}