package dn.quest.notification.service;

import dn.quest.notification.enums.NotificationCategory;
import dn.quest.notification.enums.NotificationStatus;
import dn.quest.notification.enums.NotificationType;
import dn.quest.notification.service.channel.NotificationChannel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Setter
    @Getter
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
    }

    @Setter
    @Getter
    class ChannelStatistics {
        private long sent;
        private long delivered;
        private long failed;
        private double deliveryRate;
        private double averageDeliveryTimeSeconds;
    }

    @Setter
    @Getter
    class DeliveryTimeStatistics {
        private LocalDateTime date;
        private double averageDeliveryTimeSeconds;
        private long notificationCount;
    }

    @Setter
    @Getter
    class UserNotificationStatistics {
        private UUID userId;
        private String username;
        private long totalNotifications;
        private long deliveredNotifications;
        private long openedNotifications;
        private double deliveryRate;
        private double openRate;
    }

    @Setter
    @Getter
    class ErrorStatistics {
        private long totalErrors;
        private Map<String, Long> errorsByType;
        private Map<String, Long> errorsByChannel;
        private List<RecentError> recentErrors;
    }

    @Setter
    @Getter
    class RecentError {
        private String notificationId;
        private String errorType;
        private String errorMessage;
        private LocalDateTime timestamp;
    }

    @Setter
    @Getter
    class PerformanceMetrics {
        private double averageProcessingTimeMs;
        private long notificationsPerSecond;
        private long queueSize;
        private double cpuUsage;
        private long memoryUsageMB;
        private int activeThreads;
    }

    @Setter
    @Getter
    class NotificationTrend {
        private LocalDateTime date;
        private long sentCount;
        private long deliveredCount;
        private long failedCount;
        private double deliveryRate;
    }

    @Setter
    @Getter
    class TemplateStatistics {
        private String templateId;
        private long usageCount;
        private double successRate;
        private double averageProcessingTimeMs;
    }
}