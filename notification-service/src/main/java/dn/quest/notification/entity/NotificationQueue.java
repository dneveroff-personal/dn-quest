package dn.quest.notification.entity;

import dn.quest.notification.enums.NotificationPriority;
import dn.quest.notification.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Сущность для хранения уведомлений в очереди обработки
 */
@Entity
@Table(name = "notification_queue")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private NotificationPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    @Column(name = "channel_type", nullable = false)
    private String channelType;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    /**
     * Проверяет, можно ли повторить попытку отправки
     */
    public boolean canRetry() {
        return retryCount < maxRetries && 
               (nextRetryAt == null || nextRetryAt.isBefore(LocalDateTime.now()));
    }

    /**
     * Увеличивает счетчик попыток и устанавливает время следующей попытки
     */
    public void incrementRetryCount() {
        this.retryCount++;
        // Экспоненциальная задержка: 1 мин, 5 мин, 15 мин, 30 мин
        int delayMinutes = (int) Math.min(30, Math.pow(5, retryCount - 1));
        this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
    }

    /**
     * Отмечает уведомление как обработанное
     */
    public void markAsProcessed() {
        this.status = NotificationStatus.SENT;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Отмечает уведомление как неудачное
     */
    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }
}