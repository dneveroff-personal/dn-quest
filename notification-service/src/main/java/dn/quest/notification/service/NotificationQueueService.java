package dn.quest.notification.service;

import dn.quest.notification.entity.NotificationQueue;
import dn.quest.notification.enums.NotificationPriority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис для управления очередью уведомлений
 */
public interface NotificationQueueService {

    /**
     * Добавляет уведомление в очередь
     */
    NotificationQueue addToQueue(String notificationId, UUID userId, String channelType,
                               NotificationPriority priority, String payload);

    /**
     * Добавляет отложенное уведомление в очередь
     */
    NotificationQueue addToQueue(String notificationId, UUID userId, String channelType,
                               NotificationPriority priority, String payload, 
                               LocalDateTime scheduledAt);

    /**
     * Получает уведомления готовые к обработке
     */
    List<NotificationQueue> getReadyForProcessing(int limit);

    /**
     * Получает уведомления готовые к повторной отправке
     */
    List<NotificationQueue> getReadyForRetry(int limit);

    /**
     * Обрабатывает уведомление из очереди
     */
    CompletableFuture<Void> processNotification(NotificationQueue queueItem);

    /**
     * Повторяет отправку уведомления
     */
    void retryNotification(NotificationQueue queueItem);

    /**
     * Отмечает уведомление как успешно отправленное
     */
    void markAsSent(UUID queueItemId);

    /**
     * Отмечает уведомление как неудачное
     */
    void markAsFailed(UUID queueItemId, String errorMessage);

    /**
     * Перемещает уведомление в Dead Letter Queue
     */
    void moveToDeadLetterQueue(NotificationQueue queueItem);

    /**
     * Очищает старые обработанные уведомления
     */
    int cleanupOldNotifications(int daysToKeep);

    /**
     * Получает статистику очереди
     */
    QueueStatistics getQueueStatistics();

    /**
     * Получает уведомления по ID пользователя
     */
    List<NotificationQueue> getUserNotifications(UUID userId);

    /**
     * Отменяет запланированное уведомление
     */
    boolean cancelScheduledNotification(UUID queueItemId);

    /**
     * Изменяет приоритет уведомления в очереди
     */
    boolean updatePriority(UUID queueItemId, NotificationPriority newPriority);

    /**
     * Получает просроченные уведомления
     */
    List<NotificationQueue> getStaleNotifications();

    /**
     * Пакетная обработка уведомлений
     */
    CompletableFuture<Void> processBatch(List<NotificationQueue> queueItems);

    /**
     * DTO для статистики очереди
     */
    class QueueStatistics {
        private long pendingCount;
        private long processingCount;
        private long sentCount;
        private long failedCount;
        private Double averageProcessingTimeSeconds;
        private long retryCount;

        // Getters and setters
        public long getPendingCount() { return pendingCount; }
        public void setPendingCount(long pendingCount) { this.pendingCount = pendingCount; }

        public long getProcessingCount() { return processingCount; }
        public void setProcessingCount(long processingCount) { this.processingCount = processingCount; }

        public long getSentCount() { return sentCount; }
        public void setSentCount(long sentCount) { this.sentCount = sentCount; }

        public long getFailedCount() { return failedCount; }
        public void setFailedCount(long failedCount) { this.failedCount = failedCount; }

        public Double getAverageProcessingTimeSeconds() { return averageProcessingTimeSeconds; }
        public void setAverageProcessingTimeSeconds(Double averageProcessingTimeSeconds) { 
            this.averageProcessingTimeSeconds = averageProcessingTimeSeconds; 
        }

        public long getRetryCount() { return retryCount; }
        public void setRetryCount(long retryCount) { this.retryCount = retryCount; }
    }
}