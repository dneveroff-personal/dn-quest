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

/**
 * Сущность шаблона уведомления
 */
@Entity
@Table(name = "notification_templates", indexes = {
    @Index(name = "idx_template_type", columnList = "type"),
    @Index(name = "idx_template_category", columnList = "category"),
    @Index(name = "idx_template_language", columnList = "language"),
    @Index(name = "idx_template_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Уникальный идентификатор шаблона
     */
    @Column(unique = true, nullable = false, length = 64)
    private String templateId;

    /**
     * Название шаблона
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Описание шаблона
     */
    @Column(length = 500)
    private String description;

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
     * Язык шаблона (ISO 639-1)
     */
    @Column(nullable = false, length = 5)
    private String language;

    /**
     * Шаблон темы
     */
    @Column(length = 500)
    private String subjectTemplate;

    /**
     * Шаблон текстового содержимого
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String contentTemplate;

    /**
     * Шаблон HTML содержимого
     */
    @Column(columnDefinition = "TEXT")
    private String htmlTemplate;

    /**
     * Переменные шаблона (JSON массив)
     */
    @Column(columnDefinition = "JSON")
    private String templateVariables;

    /**
     * Активен ли шаблон
     */
    @Column(nullable = false)
    private Boolean active;

    /**
     * Версия шаблона
     */
    @Column(nullable = false)
    private Integer version;

    /**
     * ID автора шаблона
     */
    private Long createdBy;

    /**
     * ID пользователя, обновившего шаблон
     */
    private Long updatedBy;

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
     * Метаданные шаблона (JSON)
     */
    @Column(columnDefinition = "JSON")
    private String metadata;
}