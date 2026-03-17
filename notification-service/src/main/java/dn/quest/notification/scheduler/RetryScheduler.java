package dn.quest.notification.scheduler;

import dn.quest.notification.entity.NotificationQueue;
import dn.quest.notification.service.RetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Планировщик для обработки retry логики и Dead Letter Queue
 */
@Component
public class RetryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RetryScheduler.class);

    private final RetryService retryService;

    @Value("${app.notification.retry.batch-size:50}")
    private int retryBatchSize;

    @Value("${app.notification.dlq.retry-interval:3600000}")
    private long dlqRetryInterval;

    @Value("${app.notification.dlq.cleanup-interval:86400000}")
    private long dlqCleanupInterval;

    public RetryScheduler(RetryService retryService) {
        this.retryService = retryService;
    }

    /**
     * Обработка уведомлений готовых для повторной отправки
     */
    @Scheduled(fixedDelayString = "${app.notification.retry.processing-interval:30000}")
    public void processRetryNotifications() {
        try {
            logger.debug("Начало обработки уведомлений для повторной отправки");
            
            List<NotificationQueue> retryNotifications = retryService.getNotificationsReadyForRetry();
            
            if (!retryNotifications.isEmpty()) {
                logger.info("Найдено {} уведомлений для повторной отправки", retryNotifications.size());
                
                int processedCount = 0;
                for (NotificationQueue notification : retryNotifications) {
                    try {
                        if (retryService.retryNotification(notification)) {
                            processedCount++;
                        }
                    } catch (Exception e) {
                        logger.error("Ошибка при повторной отправке уведомления: id={}", 
                                   notification.getId(), e);
                    }
                }
                
                logger.info("Обработано {} уведомлений для повторной отправки", processedCount);
            } else {
                logger.debug("Нет уведомлений для повторной отправки");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при обработке уведомлений для повторной отправки", e);
        }
    }

    /**
     * Попытка повторной отправки уведомлений из Dead Letter Queue
     */
    @Scheduled(fixedDelayString = "${app.notification.dlq.retry-interval:3600000}")
    public void retryDeadLetterQueue() {
        try {
            logger.debug("Начало обработки Dead Letter Queue");
            
            List<NotificationQueue> dlqNotifications = retryService.getDeadLetterNotifications();
            
            if (!dlqNotifications.isEmpty()) {
                logger.info("Найдено {} уведомлений в Dead Letter Queue", dlqNotifications.size());
                
                // Пытаемся повторно отправить уведомления из DLQ
                retryService.retryDeadLetterNotifications()
                    .thenRun(() -> logger.info("Завершена обработка Dead Letter Queue"))
                    .exceptionally(throwable -> {
                        logger.error("Ошибка при обработке Dead Letter Queue", throwable);
                        return null;
                    });
            } else {
                logger.debug("Нет уведомлений в Dead Letter Queue");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при обработке Dead Letter Queue", e);
        }
    }

    /**
     * Очистка старых уведомлений из Dead Letter Queue
     */
    @Scheduled(fixedDelayString = "${app.notification.dlq.cleanup-interval:86400000}")
    public void cleanupDeadLetterQueue() {
        try {
            logger.debug("Начало очистки Dead Letter Queue");
            
            int deletedCount = retryService.cleanupDeadLetterQueue(7); // Храним 7 дней
            
            if (deletedCount > 0) {
                logger.info("Удалено {} старых уведомлений из Dead Letter Queue", deletedCount);
            } else {
                logger.debug("Нет старых уведомлений для удаления из Dead Letter Queue");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при очистке Dead Letter Queue", e);
        }
    }

    /**
     * Вывод статистики retry и DLQ
     */
    @Scheduled(fixedDelayString = "${app.notification.retry.stats-interval:300000}")
    public void logRetryStatistics() {
        try {
            var stats = retryService.getRetryStatistics();
            
            logger.info("Статистика retry и DLQ: " +
                       "totalRetries={}, successfulRetries={}, failedRetries={}, " +
                       "dlqCount={}, avgRetryCount={}, retriedLastHour={}, retriedLastDay={}",
                       stats.getTotalRetries(),
                       stats.getSuccessfulRetries(),
                       stats.getFailedRetries(),
                       stats.getDeadLetterCount(),
                       String.format("%.2f", stats.getAverageRetryCount()),
                       stats.getRetriedInLastHour(),
                       stats.getRetriedInLastDay());
            
        } catch (Exception e) {
            logger.error("Ошибка при получении статистики retry", e);
        }
    }

    /**
     * Проверка зависших уведомлений в retry
     */
    @Scheduled(fixedDelayString = "${app.notification.retry.stale-check-interval:600000}")
    public void checkStaleRetryNotifications() {
        try {
            logger.debug("Начало проверки зависших уведомлений в retry");
            
            List<NotificationQueue> retryNotifications = retryService.getNotificationsReadyForRetry();
            
            // Проверяем уведомления, которые долго находятся в статусе retry
            long staleCount = retryNotifications.stream()
                .filter(n -> n.getNextRetryAt() != null && 
                           n.getNextRetryAt().isBefore(java.time.LocalDateTime.now().minusHours(1)))
                .count();
            
            if (staleCount > 0) {
                logger.warn("Найдено {} зависших уведомлений в retry", staleCount);
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при проверке зависших уведомлений в retry", e);
        }
    }
}