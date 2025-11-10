package dn.quest.notification.dto;

import dn.quest.notification.enums.NotificationCategory;
import dn.quest.notification.enums.NotificationPriority;
import dn.quest.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO для запроса отправки уведомления
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    /**
     * ID получателя уведомления
     */
    @NotNull(message = "User ID is required")
    private Long userId;

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
    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content must be less than 2000 characters")
    private String content;

    /**
     * HTML содержимое
     */
    @Size(max = 10000, message = "HTML content must be less than 10000 characters")
    private String htmlContent;

    /**
     * Email получателя (для email уведомлений)
     */
    private String recipientEmail;

    /**
     * Телефон получателя (для SMS уведомлений)
     */
    private String recipientPhone;

    /**
     * Telegram chat ID получателя
     */
    private String telegramChatId;

    /**
     * FCM token получателя (для push уведомлений)
     */
    private String fcmToken;

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
}