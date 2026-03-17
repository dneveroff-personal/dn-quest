package dn.quest.shared.events.notification;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие уведомления
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationEvent extends BaseEvent {

    /**
     * ID уведомления
     */
    private String notificationId;

    /**
     * ID получателя уведомления
     */
    private String recipientId;

    /**
     * Имя получателя уведомления
     */
    private String recipientName;

    /**
     * Email получателя уведомления
     */
    private String recipientEmail;

    /**
     * Тип уведомления (email, push, in_app, sms)
     */
    private String notificationType;

    /**
     * Категория уведомления (system, quest, team, game, etc.)
     */
    private String category;

    /**
     * Тема уведомления
     */
    private String subject;

    /**
     * Содержание уведомления
     */
    private String content;

    /**
     * HTML содержимое (для email)
     */
    private String htmlContent;

    /**
     * Дополнительные данные для шаблона
     */
    private Map<String, Object> templateData;

    /**
     * Приоритет уведомления (low, normal, high, urgent)
     */
    private String priority;

    /**
     * ID связанной сущности
     */
    private String relatedEntityId;

    /**
     * Тип связанной сущности
     */
    private String relatedEntityType;

    /**
     * Время создания уведомления
     */
    private java.time.Instant createdAt;

    /**
     * Запланированное время отправки
     */
    private java.time.Instant scheduledAt;

    /**
     * Время отправки
     */
    private java.time.Instant sentAt;

    /**
     * Статус уведомления (pending, sent, delivered, failed, read)
     */
    private String status;

    /**
     * Количество попыток отправки
     */
    private Integer retryCount;

    /**
     * Максимальное количество попыток
     */
    private Integer maxRetries;

    /**
     * Ошибка при отправке (если есть)
     */
    private String errorMessage;

    /**
     * ID источника события, вызвавшего уведомление
     */
    private String sourceEventId;

    /**
     * Тип источника события
     */
    private String sourceEventType;

    /**
     * Создание события уведомления
     */
    public static NotificationEvent create(String notificationId, String recipientId,
                                         String recipientName, String recipientEmail,
                                         String notificationType, String category,
                                         String subject, String content,
                                         String htmlContent, Map<String, Object> templateData,
                                         String priority, String relatedEntityId,
                                         String relatedEntityType, java.time.Instant createdAt,
                                         java.time.Instant scheduledAt,
                                         String sourceEventId, String sourceEventType,
                                         String correlationId) {
        return NotificationEvent.builder()
                .eventType("Notification")
                .eventVersion("1.0")
                .source("notification-service")
                .correlationId(correlationId)
                .notificationId(notificationId)
                .recipientId(recipientId)
                .recipientName(recipientName)
                .recipientEmail(recipientEmail)
                .notificationType(notificationType)
                .category(category)
                .subject(subject)
                .content(content)
                .htmlContent(htmlContent)
                .templateData(templateData)
                .priority(priority)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .createdAt(createdAt)
                .scheduledAt(scheduledAt)
                .sentAt(null)
                .status("pending")
                .retryCount(0)
                .maxRetries(3)
                .errorMessage(null)
                .sourceEventId(sourceEventId)
                .sourceEventType(sourceEventType)
                .data(Map.of(
                        "notificationId", notificationId,
                        "recipientId", recipientId,
                        "recipientName", recipientName,
                        "recipientEmail", recipientEmail,
                        "notificationType", notificationType,
                        "category", category,
                        "subject", subject,
                        "content", content,
                        "htmlContent", htmlContent,
                        "templateData", templateData,
                        "priority", priority,
                        "relatedEntityId", relatedEntityId,
                        "relatedEntityType", relatedEntityType,
                        "createdAt", createdAt.toString(),
                        "scheduledAt", scheduledAt != null ? scheduledAt.toString() : null,
                        "sourceEventId", sourceEventId,
                        "sourceEventType", sourceEventType
                ))
                .build();
    }
}