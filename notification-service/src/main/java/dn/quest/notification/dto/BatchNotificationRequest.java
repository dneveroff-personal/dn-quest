package dn.quest.notification.dto;

import dn.quest.notification.enums.NotificationCategory;
import dn.quest.notification.enums.NotificationPriority;
import dn.quest.notification.enums.NotificationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO для запроса пакетной отправки уведомлений
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchNotificationRequest {

    /**
     * Список ID пользователей для отправки
     */
    @NotEmpty(message = "User IDs list cannot be empty")
    private List<UUID> userIds;

    /**
     * Тип уведомления
     */
    @NotNull(message = "Notification type is required")
    private NotificationType type;

    /**
     * Категория уведомления
     */
    @NotNull(message = "Notification category is required")
    private NotificationCategory category;

    /**
     * Приоритет уведомления
     */
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;

    /**
     * Тема уведомления
     */
    @Size(max = 255, message = "Subject must be less than 255 characters")
    private String subject;

    /**
     * Содержание уведомления
     */
    @NotNull(message = "Content is required")
    @Size(max = 2000, message = "Content must be less than 2000 characters")
    private String content;

    /**
     * HTML содержимое
     */
    @Size(max = 10000, message = "HTML content must be less than 10000 characters")
    private String htmlContent;

    /**
     * ID связанной сущности
     */
    private String relatedEntityId;

    /**
     * Тип связанной сущности
     */
    private String relatedEntityType;

    /**
     * Запланированное время отправки
     */
    private Long scheduledAt;

    /**
     * Дополнительные данные для шаблона
     */
    private Map<String, Object> templateData;

    /**
     * ID корреляции для трассировки
     */
    private String correlationId;

    /**
     * Отправлять ли асинхронно
     */
    @Builder.Default
    private Boolean async = true;

    /**
     * Размер пакета для обработки
     */
    @Builder.Default
    private Integer batchSize = 100;
}