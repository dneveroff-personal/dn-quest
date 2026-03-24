package dn.quest.notification.service;

import dn.quest.notification.entity.NotificationQueue;
import dn.quest.notification.enums.NotificationStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для управления retry логикой и Dead Letter Queue
 */
public interface RetryService {

    /**
     * Обрабатывает неудачные уведомления и определяет необходимость повторной попытки
     */
    void handleFailedNotification(NotificationQueue queueItem, String errorMessage);

    /**
     * Выполняет повторную отправку уведомления
     */
    boolean retryNotification(NotificationQueue queueItem);

    /**
     * Перемещает уведомление в Dead Letter Queue
     */
    void moveToDeadLetterQueue(NotificationQueue queueItem, String reason);

    /**
     * Получает уведомления готовые для повторной отправки
     */
    List<NotificationQueue> getNotificationsReadyForRetry();

    /**
     * Получает уведомления из Dead Letter Queue
     */
    List<NotificationQueue> getDeadLetterNotifications();

    /**
     * Пытается повторно отправить уведомления из Dead Letter Queue
     */
    java.util.concurrent.CompletableFuture<Void> retryDeadLetterNotifications();

    /**
     * Очищает старые уведомления из Dead Letter Queue
     */
    int cleanupDeadLetterQueue(int daysToKeep);

    /**
     * Получает статистику по retry и DLQ
     */
    RetryStatistics getRetryStatistics();

    /**
     * Проверяет, можно ли повторить отправку уведомления
     */
    boolean canRetry(NotificationQueue queueItem);

    /**
     * Вычисляет задержку перед следующей попыткой
     */
    LocalDateTime calculateNextRetryTime(int retryCount);

    /**
     * DTO для статистики retry
     */
    class RetryStatistics {
        private long totalRetries;
        private long successfulRetries;
        private long failedRetries;
        private long deadLetterCount;
        private double averageRetryCount;
        private long retriedInLastHour;
        private long retriedInLastDay;

        // Getters and setters
        public long getTotalRetries() { return totalRetries; }
        public void setTotalRetries(long totalRetries) { this.totalRetries = totalRetries; }

        public long getSuccessfulRetries() { return successfulRetries; }
        public void setSuccessfulRetries(long successfulRetries) { this.successfulRetries = successfulRetries; }

        public long getFailedRetries() { return failedRetries; }
        public void setFailedRetries(long failedRetries) { this.failedRetries = failedRetries; }

        public long getDeadLetterCount() { return deadLetterCount; }
        public void setDeadLetterCount(long deadLetterCount) { this.deadLetterCount = deadLetterCount; }

        public double getAverageRetryCount() { return averageRetryCount; }
        public void setAverageRetryCount(double averageRetryCount) { this.averageRetryCount = averageRetryCount; }

        public long getRetriedInLastHour() { return retriedInLastHour; }
        public void setRetriedInLastHour(long retriedInLastHour) { this.retriedInLastHour = retriedInLastHour; }

        public long getRetriedInLastDay() { return retriedInLastDay; }
        public void setRetriedInLastDay(long retriedInLastDay) { this.retriedInLastDay = retriedInLastDay; }
    }
}