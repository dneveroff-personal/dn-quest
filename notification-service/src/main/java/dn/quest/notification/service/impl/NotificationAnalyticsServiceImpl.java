package dn.quest.notification.service.impl;

import dn.quest.notification.entity.Notification;
import dn.quest.notification.entity.NotificationQueue;
import dn.quest.notification.enums.*;
import dn.quest.notification.repository.NotificationRepository;
import dn.quest.notification.repository.NotificationQueueRepository;
import dn.quest.notification.service.NotificationAnalyticsService;
import dn.quest.notification.service.channel.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для аналитики и мониторинга уведомлений
 */
@Service
@Transactional(readOnly = true)
public class NotificationAnalyticsServiceImpl implements NotificationAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationAnalyticsServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final NotificationQueueRepository queueRepository;

    // In-memory метрики для реального времени
    private final Map<String, NotificationMetric> notificationMetrics = new ConcurrentHashMap<>();
    private final AtomicLong totalNotificationsSent = new AtomicLong(0);
    private final AtomicLong totalNotificationsDelivered = new AtomicLong(0);
    private final AtomicLong totalNotificationsFailed = new AtomicLong(0);
    private final AtomicLong totalNotificationsOpened = new AtomicLong(0);
    private final AtomicLong totalNotificationsClicked = new AtomicLong(0);

    public NotificationAnalyticsServiceImpl(NotificationRepository notificationRepository,
                                          NotificationQueueRepository queueRepository) {
        this.notificationRepository = notificationRepository;
        this.queueRepository = queueRepository;
    }

    @Override
    public NotificationStatistics getOverallStatistics() {
        NotificationStatistics stats = new NotificationStatistics();
        
        try {
            // Получаем общие счетчики
            stats.setTotalSent(totalNotificationsSent.get());
            stats.setTotalDelivered(totalNotificationsDelivered.get());
            stats.setTotalFailed(totalNotificationsFailed.get());
            stats.setTotalOpened(totalNotificationsOpened.get());
            stats.setTotalClicked(totalNotificationsClicked.get());
            
            // Вычисляем rates
            if (stats.getTotalSent() > 0) {
                stats.setDeliveryRate((double) stats.getTotalDelivered() / stats.getTotalSent() * 100);
                stats.setOpenRate((double) stats.getTotalOpened() / stats.getTotalDelivered() * 100);
                stats.setClickRate((double) stats.getTotalClicked() / stats.getTotalOpened() * 100);
            }
            
            // Среднее время доставки
            Double avgDeliveryTime = notificationRepository.getAverageDeliveryTime();
            if (avgDeliveryTime != null) {
                stats.setAverageDeliveryTimeSeconds(avgDeliveryTime);
            }
            
        } catch (Exception e) {
            logger.error("Error calculating overall statistics", e);
        }
        
        return stats;
    }

    @Override
    public NotificationStatistics getStatisticsForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        NotificationStatistics stats = new NotificationStatistics();
        
        try {
            List<Notification> notifications = notificationRepository.findByCreatedAtBetween(startDate, endDate);
            
            long totalSent = notifications.size();
            long totalDelivered = notifications.stream()
                .mapToLong(n -> n.getStatus() == NotificationStatus.DELIVERED ? 1 : 0)
                .sum();
            long totalFailed = notifications.stream()
                .mapToLong(n -> n.getStatus() == NotificationStatus.FAILED ? 1 : 0)
                .sum();
            
            stats.setTotalSent(totalSent);
            stats.setTotalDelivered(totalDelivered);
            stats.setTotalFailed(totalFailed);
            
            if (totalSent > 0) {
                stats.setDeliveryRate((double) totalDelivered / totalSent * 100);
            }
            
        } catch (Exception e) {
            logger.error("Error calculating statistics for period", e);
        }
        
        return stats;
    }

    @Override
    public Map<NotificationType, Long> getStatisticsByType() {
        try {
            List<Object[]> results = notificationRepository.countByType();
            return results.stream()
                .collect(Collectors.toMap(
                    row -> (NotificationType) row[0],
                    row -> (Long) row[1]
                ));
        } catch (Exception e) {
            logger.error("Error calculating statistics by type", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<NotificationCategory, Long> getStatisticsByCategory() {
        try {
            List<Object[]> results = notificationRepository.countByCategory();
            return results.stream()
                .collect(Collectors.toMap(
                    row -> (NotificationCategory) row[0],
                    row -> (Long) row[1]
                ));
        } catch (Exception e) {
            logger.error("Error calculating statistics by category", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<NotificationChannel, ChannelStatistics> getStatisticsByChannel() {
        Map<NotificationChannel, ChannelStatistics> channelStats = new HashMap<>();
        
        try {
            // Получаем статистику по каналам из очереди
            List<Object[]> results = queueRepository.countByChannelType();
            
            for (Object[] row : results) {
                String channelType = (String) row[0];
                Long count = (Long) row[1];
                
                try {
                    NotificationChannel channel = NotificationChannel.valueOf(channelType);
                    ChannelStatistics stats = new ChannelStatistics();
                    stats.setSent(count);
                    
                    // Дополнительная статистика по каналу
                    List<NotificationQueue> channelNotifications = queueRepository.findByChannelTypeAndStatus(
                        channelType, NotificationStatus.SENT);
                    stats.setDelivered(channelNotifications.size());
                    
                    if (stats.getSent() > 0) {
                        stats.setDeliveryRate((double) stats.getDelivered() / stats.getSent() * 100);
                    }
                    
                    channelStats.put(channel, stats);
                } catch (IllegalArgumentException e) {
                    logger.warn("Unknown channel type: {}", channelType);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error calculating statistics by channel", e);
        }
        
        return channelStats;
    }

    @Override
    public Map<NotificationStatus, Long> getStatisticsByStatus() {
        try {
            List<Object[]> results = notificationRepository.countByStatus();
            return results.stream()
                .collect(Collectors.toMap(
                    row -> (NotificationStatus) row[0],
                    row -> (Long) row[1]
                ));
        } catch (Exception e) {
            logger.error("Error calculating statistics by status", e);
            return new HashMap<>();
        }
    }

    @Override
    public List<DeliveryTimeStatistics> getDeliveryTimeStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<DeliveryTimeStatistics> deliveryStats = new ArrayList<>();
        
        try {
            // Группируем по дням
            LocalDateTime current = startDate;
            while (current.isBefore(endDate)) {
                LocalDateTime dayEnd = current.plusDays(1);
                
                List<Notification> dayNotifications = notificationRepository
                    .findByCreatedAtBetweenAndStatus(current, dayEnd, NotificationStatus.DELIVERED);
                
                if (!dayNotifications.isEmpty()) {
                    DeliveryTimeStatistics dayStats = new DeliveryTimeStatistics();
                    dayStats.setDate(current);
                    dayStats.setNotificationCount(dayNotifications.size());
                    
                    double avgDeliveryTime = dayNotifications.stream()
                        .filter(n -> n.getSentAt() != null && n.getDeliveredAt() != null)
                        .mapToLong(n -> ChronoUnit.SECONDS.between(n.getSentAt(), n.getDeliveredAt()))
                        .average()
                        .orElse(0.0);
                    
                    dayStats.setAverageDeliveryTimeSeconds(avgDeliveryTime);
                    deliveryStats.add(dayStats);
                }
                
                current = dayEnd;
            }
            
        } catch (Exception e) {
            logger.error("Error calculating delivery time statistics", e);
        }
        
        return deliveryStats;
    }

    @Override
    public List<UserNotificationStatistics> getTopUsersByNotificationCount(int limit) {
        List<UserNotificationStatistics> userStats = new ArrayList<>();
        
        try {
            List<Object[]> results = notificationRepository.countByUserIdOrderByCountDesc();
            
            for (int i = 0; i < Math.min(results.size(), limit); i++) {
                Object[] row = results.get(i);
                Long userId = (Long) row[0];
                Long count = (Long) row[1];
                
                UserNotificationStatistics userStat = new UserNotificationStatistics();
                userStat.setUserId(userId);
                userStat.setTotalNotifications(count);
                
                // Дополнительная статистика по пользователю
                List<Notification> userNotifications = notificationRepository.findByUserId(userId);
                long deliveredCount = userNotifications.stream()
                    .mapToLong(n -> n.getStatus() == NotificationStatus.DELIVERED ? 1 : 0)
                    .sum();
                
                userStat.setDeliveredNotifications(deliveredCount);
                
                if (userStat.getTotalNotifications() > 0) {
                    userStat.setDeliveryRate((double) deliveredCount / userStat.getTotalNotifications() * 100);
                }
                
                userStats.add(userStat);
            }
            
        } catch (Exception e) {
            logger.error("Error calculating top users by notification count", e);
        }
        
        return userStats;
    }

    @Override
    public ErrorStatistics getErrorStatistics() {
        ErrorStatistics errorStats = new ErrorStatistics();
        
        try {
            // Получаем неудачные уведомления
            List<Notification> failedNotifications = notificationRepository.findByStatus(NotificationStatus.FAILED);
            
            errorStats.setTotalErrors(failedNotifications.size());
            
            // Группируем ошибки по типам
            Map<String, Long> errorsByType = failedNotifications.stream()
                .filter(n -> n.getErrorMessage() != null)
                .collect(Collectors.groupingBy(
                    n -> extractErrorType(n.getErrorMessage()),
                    Collectors.counting()
                ));
            errorStats.setErrorsByType(errorsByType);
            
            // Последние ошибки
            List<RecentError> recentErrors = failedNotifications.stream()
                .filter(n -> n.getErrorMessage() != null)
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .limit(10)
                .map(n -> {
                    RecentError error = new RecentError();
                    error.setNotificationId(n.getNotificationId());
                    error.setErrorType(extractErrorType(n.getErrorMessage()));
                    error.setErrorMessage(n.getErrorMessage());
                    error.setTimestamp(n.getUpdatedAt());
                    return error;
                })
                .collect(Collectors.toList());
            errorStats.setRecentErrors(recentErrors);
            
        } catch (Exception e) {
            logger.error("Error calculating error statistics", e);
        }
        
        return errorStats;
    }

    @Override
    public PerformanceMetrics getPerformanceMetrics() {
        PerformanceMetrics metrics = new PerformanceMetrics();
        
        try {
            // Метрики JVM
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            
            metrics.setMemoryUsageMB(memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024));
            metrics.setActiveThreads(threadBean.getThreadCount());
            
            // Метрики очереди
            long queueSize = queueRepository.countByStatus(NotificationStatus.PENDING);
            metrics.setQueueSize(queueSize);
            
            // Среднее время обработки из in-memory метрик
            double avgProcessingTime = notificationMetrics.values().stream()
                .filter(m -> m.getProcessingTimeMs() > 0)
                .mapToLong(NotificationMetric::getProcessingTimeMs)
                .average()
                .orElse(0.0);
            metrics.setAverageProcessingTimeMs(avgProcessingTime);
            
            // Уведомлений в секунду (упрощенный расчет)
            long notificationsInLastMinute = notificationMetrics.values().stream()
                .filter(m -> m.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(1)))
                .count();
            metrics.setNotificationsPerSecond(notificationsInLastMinute / 60);
            
        } catch (Exception e) {
            logger.error("Error calculating performance metrics", e);
        }
        
        return metrics;
    }

    @Override
    public List<NotificationTrend> getNotificationTrends(LocalDateTime startDate, LocalDateTime endDate) {
        List<NotificationTrend> trends = new ArrayList<>();
        
        try {
            // Группируем по дням
            LocalDateTime current = startDate;
            while (current.isBefore(endDate)) {
                LocalDateTime dayEnd = current.plusDays(1);
                
                List<Notification> dayNotifications = notificationRepository
                    .findByCreatedAtBetween(current, dayEnd);
                
                NotificationTrend trend = new NotificationTrend();
                trend.setDate(current);
                trend.setSentCount(dayNotifications.size());
                
                long deliveredCount = dayNotifications.stream()
                    .mapToLong(n -> n.getStatus() == NotificationStatus.DELIVERED ? 1 : 0)
                    .sum();
                trend.setDeliveredCount(deliveredCount);
                
                long failedCount = dayNotifications.stream()
                    .mapToLong(n -> n.getStatus() == NotificationStatus.FAILED ? 1 : 0)
                    .sum();
                trend.setFailedCount(failedCount);
                
                if (trend.getSentCount() > 0) {
                    trend.setDeliveryRate((double) deliveredCount / trend.getSentCount() * 100);
                }
                
                trends.add(trend);
                current = dayEnd;
            }
            
        } catch (Exception e) {
            logger.error("Error calculating notification trends", e);
        }
        
        return trends;
    }

    @Override
    public Map<String, TemplateStatistics> getTemplateStatistics() {
        Map<String, TemplateStatistics> templateStats = new HashMap<>();
        
        try {
            // Анализируем использование шаблонов из метрик
            notificationMetrics.values().stream()
                .filter(m -> m.getTemplateId() != null)
                .collect(Collectors.groupingBy(
                    NotificationMetric::getTemplateId,
                    Collectors.counting()
                ))
                .forEach((templateId, count) -> {
                    TemplateStatistics stats = new TemplateStatistics();
                    stats.setTemplateId(templateId);
                    stats.setUsageCount(count);
                    
                    // Расчет success rate
                    long successCount = notificationMetrics.values().stream()
                        .filter(m -> templateId.equals(m.getTemplateId()) && m.isSuccess())
                        .count();
                    
                    if (count > 0) {
                        stats.setSuccessRate((double) successCount / count * 100);
                    }
                    
                    templateStats.put(templateId, stats);
                });
            
        } catch (Exception e) {
            logger.error("Error calculating template statistics", e);
        }
        
        return templateStats;
    }

    @Override
    @Transactional
    public void recordNotificationSent(String notificationId, NotificationType type, 
                                     NotificationChannel channel, long deliveryTimeMs) {
        try {
            totalNotificationsSent.incrementAndGet();
            totalNotificationsDelivered.incrementAndGet();
            
            NotificationMetric metric = new NotificationMetric();
            metric.setNotificationId(notificationId);
            metric.setType(type);
            metric.setChannel(channel);
            metric.setProcessingTimeMs(deliveryTimeMs);
            metric.setSuccess(true);
            metric.setTimestamp(LocalDateTime.now());
            
            notificationMetrics.put(notificationId, metric);
            
            // Очистка старых метрик (хранить только последние 10000)
            if (notificationMetrics.size() > 10000) {
                cleanupOldMetrics();
            }
            
        } catch (Exception e) {
            logger.error("Error recording notification sent", e);
        }
    }

    @Override
    @Transactional
    public void recordNotificationError(String notificationId, String errorType, String errorMessage) {
        try {
            totalNotificationsFailed.incrementAndGet();
            
            NotificationMetric metric = notificationMetrics.get(notificationId);
            if (metric != null) {
                metric.setSuccess(false);
                metric.setErrorType(errorType);
                metric.setErrorMessage(errorMessage);
            }
            
        } catch (Exception e) {
            logger.error("Error recording notification error", e);
        }
    }

    @Override
    @Transactional
    public void recordNotificationOpened(String notificationId, LocalDateTime openedAt) {
        try {
            totalNotificationsOpened.incrementAndGet();
            
            NotificationMetric metric = notificationMetrics.get(notificationId);
            if (metric != null) {
                metric.setOpenedAt(openedAt);
            }
            
        } catch (Exception e) {
            logger.error("Error recording notification opened", e);
        }
    }

    @Override
    @Transactional
    public void recordNotificationClicked(String notificationId, String clickTarget, LocalDateTime clickedAt) {
        try {
            totalNotificationsClicked.incrementAndGet();
            
            NotificationMetric metric = notificationMetrics.get(notificationId);
            if (metric != null) {
                metric.setClickedAt(clickedAt);
                metric.setClickTarget(clickTarget);
            }
            
        } catch (Exception e) {
            logger.error("Error recording notification clicked", e);
        }
    }

    private String extractErrorType(String errorMessage) {
        if (errorMessage == null) return "UNKNOWN";
        
        if (errorMessage.toLowerCase().contains("timeout")) return "TIMEOUT";
        if (errorMessage.toLowerCase().contains("connection")) return "CONNECTION_ERROR";
        if (errorMessage.toLowerCase().contains("authentication")) return "AUTH_ERROR";
        if (errorMessage.toLowerCase().contains("rate limit")) return "RATE_LIMIT";
        if (errorMessage.toLowerCase().contains("invalid")) return "INVALID_REQUEST";
        
        return "OTHER";
    }

    private void cleanupOldMetrics() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        notificationMetrics.entrySet().removeIf(entry -> 
            entry.getValue().getTimestamp().isBefore(cutoff));
    }

    // Внутренний класс для хранения метрик
    private static class NotificationMetric {
        private String notificationId;
        private NotificationType type;
        private NotificationChannel channel;
        private String templateId;
        private long processingTimeMs;
        private boolean success;
        private String errorType;
        private String errorMessage;
        private LocalDateTime timestamp;
        private LocalDateTime openedAt;
        private LocalDateTime clickedAt;
        private String clickTarget;

        // Getters and setters
        public String getNotificationId() { return notificationId; }
        public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

        public NotificationType getType() { return type; }
        public void setType(NotificationType type) { this.type = type; }

        public NotificationChannel getChannel() { return channel; }
        public void setChannel(NotificationChannel channel) { this.channel = channel; }

        public String getTemplateId() { return templateId; }
        public void setTemplateId(String templateId) { this.templateId = templateId; }

        public long getProcessingTimeMs() { return processingTimeMs; }
        public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getErrorType() { return errorType; }
        public void setErrorType(String errorType) { this.errorType = errorType; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public LocalDateTime getOpenedAt() { return openedAt; }
        public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }

        public LocalDateTime getClickedAt() { return clickedAt; }
        public void setClickedAt(LocalDateTime clickedAt) { this.clickedAt = clickedAt; }

        public String getClickTarget() { return clickTarget; }
        public void setClickTarget(String clickTarget) { this.clickTarget = clickTarget; }
    }
}