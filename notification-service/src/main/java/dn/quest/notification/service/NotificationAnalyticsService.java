package dn.quest.notification.service;

import dn.quest.notification.enums.NotificationCategory;
import dn.quest.notification.enums.NotificationStatus;
import dn.quest.notification.enums.NotificationType;
import dn.quest.notification.service.channel.NotificationChannel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Сервис для аналитики и мониторинга уведомлений
 */
public interface NotificationAnalyticsService {

    /**
     * Получить общую статистику уведомлений
     */
    NotificationStatistics getOverallStatistics();

    /**
     * Получить статистику за период
     */
    NotificationStatistics getStatisticsForPeriod(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Получить статистику по типам уведомлений
     */
    Map<NotificationType, Long> getStatisticsByType();

    /**
     * Получить статистику по категориям уведомлений
     */
    Map<NotificationCategory, Long> getStatisticsByCategory();

    /**
     * Получить статистику по каналам доставки
     */
    Map<NotificationChannel, ChannelStatistics> getStatisticsByChannel();

    /**
     * Получить статистику по статусам
     */
    Map<NotificationStatus, Long> getStatisticsByStatus();

    /**
     * Получить статистику доставки по времени
     */
    List<DeliveryTimeStatistics> getDeliveryTimeStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Получить топ пользователей по количеству уведомлений
     */
    List<UserNotificationStatistics> getTopUsersByNotificationCount(int limit);

    /**
     * Получить статистику ошибок
     */
    ErrorStatistics getErrorStatistics();

    /**
     * Получить производительность системы
     */
    PerformanceMetrics getPerformanceMetrics();

    /**
     * Получить тренды уведомлений
     */
    List<NotificationTrend> getNotificationTrends(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Получить статистику по шаблонам
     */
    Map<String, TemplateStatistics> getTemplateStatistics();

    /**
     * Записать метрику отправки уведомления
     */
    void recordNotificationSent(String notificationId, NotificationType type, 
                               String channelName, long deliveryTimeMs);

    /**
     * Записать метрику отправки уведомления
     */
    void recordNotificationSent(String notificationId, NotificationType type, 
                               NotificationChannel channel, long deliveryTimeMs);

    /**
     * Записать метрику ошибки
     */
    void recordNotificationError(String notificationId, String errorType, String errorMessage);

    /**
     * Записать метрику открытия уведомления
     */
    void recordNotificationOpened(String notificationId, LocalDateTime openedAt);

    /**
     * Записать метрику клика по уведомлению
     */
    void recordNotificationClicked(String notificationId, String clickTarget, LocalDateTime clickedAt);

    // DTO классы для статистики

    class NotificationStatistics {
        private long totalSent;
        private long totalDelivered;
        private long totalFailed;
        private long totalOpened;
        private long totalClicked;
        private double deliveryRate;
        private double openRate;
        private double clickRate;
        private double averageDeliveryTimeSeconds;

        // Getters and setters
        public long getTotalSent() { return totalSent; }
        public void setTotalSent(long totalSent) { this.totalSent = totalSent; }

        public long getTotalDelivered() { return totalDelivered; }
        public void setTotalDelivered(long totalDelivered) { this.totalDelivered = totalDelivered; }

        public long getTotalFailed() { return totalFailed; }
        public void setTotalFailed(long totalFailed) { this.totalFailed = totalFailed; }

        public long getTotalOpened() { return totalOpened; }
        public void setTotalOpened(long totalOpened) { this.totalOpened = totalOpened; }

        public long getTotalClicked() { return totalClicked; }
        public void setTotalClicked(long totalClicked) { this.totalClicked = totalClicked; }

        public double getDeliveryRate() { return deliveryRate; }
        public void setDeliveryRate(double deliveryRate) { this.deliveryRate = deliveryRate; }

        public double getOpenRate() { return openRate; }
        public void setOpenRate(double openRate) { this.openRate = openRate; }

        public double getClickRate() { return clickRate; }
        public void setClickRate(double clickRate) { this.clickRate = clickRate; }

        public double getAverageDeliveryTimeSeconds() { return averageDeliveryTimeSeconds; }
        public void setAverageDeliveryTimeSeconds(double averageDeliveryTimeSeconds) { 
            this.averageDeliveryTimeSeconds = averageDeliveryTimeSeconds; 
        }
    }

    class ChannelStatistics {
        private long sent;
        private long delivered;
        private long failed;
        private double deliveryRate;
        private double averageDeliveryTimeSeconds;

        // Getters and setters
        public long getSent() { return sent; }
        public void setSent(long sent) { this.sent = sent; }

        public long getDelivered() { return delivered; }
        public void setDelivered(long delivered) { this.delivered = delivered; }

        public long getFailed() { return failed; }
        public void setFailed(long failed) { this.failed = failed; }

        public double getDeliveryRate() { return deliveryRate; }
        public void setDeliveryRate(double deliveryRate) { this.deliveryRate = deliveryRate; }

        public double getAverageDeliveryTimeSeconds() { return averageDeliveryTimeSeconds; }
        public void setAverageDeliveryTimeSeconds(double averageDeliveryTimeSeconds) { 
            this.averageDeliveryTimeSeconds = averageDeliveryTimeSeconds; 
        }
    }

    class DeliveryTimeStatistics {
        private LocalDateTime date;
        private double averageDeliveryTimeSeconds;
        private long notificationCount;

        // Getters and setters
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }

        public double getAverageDeliveryTimeSeconds() { return averageDeliveryTimeSeconds; }
        public void setAverageDeliveryTimeSeconds(double averageDeliveryTimeSeconds) { 
            this.averageDeliveryTimeSeconds = averageDeliveryTimeSeconds; 
        }

        public long getNotificationCount() { return notificationCount; }
        public void setNotificationCount(long notificationCount) { this.notificationCount = notificationCount; }
    }

    class UserNotificationStatistics {
        private Long userId;
        private String username;
        private long totalNotifications;
        private long deliveredNotifications;
        private long openedNotifications;
        private double deliveryRate;
        private double openRate;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public long getTotalNotifications() { return totalNotifications; }
        public void setTotalNotifications(long totalNotifications) { this.totalNotifications = totalNotifications; }

        public long getDeliveredNotifications() { return deliveredNotifications; }
        public void setDeliveredNotifications(long deliveredNotifications) { this.deliveredNotifications = deliveredNotifications; }

        public long getOpenedNotifications() { return openedNotifications; }
        public void setOpenedNotifications(long openedNotifications) { this.openedNotifications = openedNotifications; }

        public double getDeliveryRate() { return deliveryRate; }
        public void setDeliveryRate(double deliveryRate) { this.deliveryRate = deliveryRate; }

        public double getOpenRate() { return openRate; }
        public void setOpenRate(double openRate) { this.openRate = openRate; }
    }

    class ErrorStatistics {
        private long totalErrors;
        private Map<String, Long> errorsByType;
        private Map<String, Long> errorsByChannel;
        private List<RecentError> recentErrors;

        // Getters and setters
        public long getTotalErrors() { return totalErrors; }
        public void setTotalErrors(long totalErrors) { this.totalErrors = totalErrors; }

        public Map<String, Long> getErrorsByType() { return errorsByType; }
        public void setErrorsByType(Map<String, Long> errorsByType) { this.errorsByType = errorsByType; }

        public Map<String, Long> getErrorsByChannel() { return errorsByChannel; }
        public void setErrorsByChannel(Map<String, Long> errorsByChannel) { this.errorsByChannel = errorsByChannel; }

        public List<RecentError> getRecentErrors() { return recentErrors; }
        public void setRecentErrors(List<RecentError> recentErrors) { this.recentErrors = recentErrors; }
    }

    class RecentError {
        private String notificationId;
        private String errorType;
        private String errorMessage;
        private LocalDateTime timestamp;

        // Getters and setters
        public String getNotificationId() { return notificationId; }
        public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

        public String getErrorType() { return errorType; }
        public void setErrorType(String errorType) { this.errorType = errorType; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    class PerformanceMetrics {
        private double averageProcessingTimeMs;
        private long notificationsPerSecond;
        private long queueSize;
        private double cpuUsage;
        private long memoryUsageMB;
        private int activeThreads;

        // Getters and setters
        public double getAverageProcessingTimeMs() { return averageProcessingTimeMs; }
        public void setAverageProcessingTimeMs(double averageProcessingTimeMs) { 
            this.averageProcessingTimeMs = averageProcessingTimeMs; 
        }

        public long getNotificationsPerSecond() { return notificationsPerSecond; }
        public void setNotificationsPerSecond(long notificationsPerSecond) { 
            this.notificationsPerSecond = notificationsPerSecond; 
        }

        public long getQueueSize() { return queueSize; }
        public void setQueueSize(long queueSize) { this.queueSize = queueSize; }

        public double getCpuUsage() { return cpuUsage; }
        public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }

        public long getMemoryUsageMB() { return memoryUsageMB; }
        public void setMemoryUsageMB(long memoryUsageMB) { this.memoryUsageMB = memoryUsageMB; }

        public int getActiveThreads() { return activeThreads; }
        public void setActiveThreads(int activeThreads) { this.activeThreads = activeThreads; }
    }

    class NotificationTrend {
        private LocalDateTime date;
        private long sentCount;
        private long deliveredCount;
        private long failedCount;
        private double deliveryRate;

        // Getters and setters
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }

        public long getSentCount() { return sentCount; }
        public void setSentCount(long sentCount) { this.sentCount = sentCount; }

        public long getDeliveredCount() { return deliveredCount; }
        public void setDeliveredCount(long deliveredCount) { this.deliveredCount = deliveredCount; }

        public long getFailedCount() { return failedCount; }
        public void setFailedCount(long failedCount) { this.failedCount = failedCount; }

        public double getDeliveryRate() { return deliveryRate; }
        public void setDeliveryRate(double deliveryRate) { this.deliveryRate = deliveryRate; }
    }

    class TemplateStatistics {
        private String templateId;
        private long usageCount;
        private double successRate;
        private double averageProcessingTimeMs;

        // Getters and setters
        public String getTemplateId() { return templateId; }
        public void setTemplateId(String templateId) { this.templateId = templateId; }

        public long getUsageCount() { return usageCount; }
        public void setUsageCount(long usageCount) { this.usageCount = usageCount; }

        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }

        public double getAverageProcessingTimeMs() { return averageProcessingTimeMs; }
        public void setAverageProcessingTimeMs(double averageProcessingTimeMs) { 
            this.averageProcessingTimeMs = averageProcessingTimeMs; 
        }
    }
}