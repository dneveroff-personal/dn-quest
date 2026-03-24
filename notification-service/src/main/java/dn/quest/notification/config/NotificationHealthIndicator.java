package dn.quest.notification.config;

import dn.quest.notification.service.NotificationAnalyticsService;
import dn.quest.notification.service.NotificationQueueService;
import dn.quest.notification.service.RetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator для Notification Service
 */
@Component
public class NotificationHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(NotificationHealthIndicator.class);

    private final NotificationQueueService queueService;
    private final RetryService retryService;
    private final NotificationAnalyticsService analyticsService;

    public NotificationHealthIndicator(NotificationQueueService queueService,
                                     RetryService retryService,
                                     NotificationAnalyticsService analyticsService) {
        this.queueService = queueService;
        this.retryService = retryService;
        this.analyticsService = analyticsService;
    }

    @Override
    public Health health() {
        try {
            Health.Builder builder = Health.up();

            // Проверяем состояние очереди
            var queueStats = queueService.getQueueStatistics();
            builder.withDetail("queue", queueStats)
                  .withDetail("queueSize", queueStats.getPendingCount() + queueStats.getProcessingCount());

            // Проверяем состояние retry
            var retryStats = retryService.getRetryStatistics();
            builder.withDetail("retry", retryStats)
                  .withDetail("deadLetterCount", retryStats.getDeadLetterCount());

            // Проверяем производительность
            var performanceMetrics = analyticsService.getPerformanceMetrics();
            builder.withDetail("performance", performanceMetrics);

            // Проверяем общую статистику
            var overallStats = analyticsService.getOverallStatistics();
            builder.withDetail("statistics", overallStats);

            // Определяем статус на основе метрик
            if (queueStats.getFailedCount() > queueStats.getSentCount() * 0.1) {
                // Если более 10% неудачных уведомлений
                builder = Health.down();
                builder.withDetail("reason", "High failure rate in queue");
            } else if (retryStats.getDeadLetterCount() > 100) {
                // Если много уведомлений в DLQ
                builder = Health.down();
                builder.withDetail("reason", "Too many notifications in Dead Letter Queue");
            } else if (performanceMetrics.getAverageProcessingTimeMs() > 5000) {
                // Если среднее время обработки слишком высокое
                builder = Health.down();
                builder.withDetail("reason", "High average processing time");
            } else if (queueStats.getPendingCount() > 1000) {
                // Если очередь слишком большая
                builder = Health.down();
                builder.withDetail("reason", "Queue size too large");
            }

            return builder.build();

        } catch (Exception e) {
            logger.error("Error checking notification service health", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}