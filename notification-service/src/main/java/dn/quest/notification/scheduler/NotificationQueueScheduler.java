package dn.quest.notification.scheduler;

import dn.quest.notification.entity.NotificationQueue;
import dn.quest.notification.service.NotificationQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Планировщик для обработки очереди уведомлений
 */
@Component
public class NotificationQueueScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationQueueScheduler.class);

    private final NotificationQueueService queueService;

    @Value("${app.notification.queue.batch-size:50}")
    private int batchSize;

    @Value("${app.notification.queue.processing-interval:5000}")
    private long processingInterval;

    @Value("${app.notification.queue.retry-interval:30000}")
    private long retryInterval;

    @Value("${app.notification.queue.cleanup-interval:3600000}")
    private long cleanupInterval;

    public NotificationQueueScheduler(NotificationQueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Обработка новых уведомлений в очереди
     */
    @Scheduled(fixedDelayString = "${app.notification.queue.processing-interval:5000}")
    public void processPendingNotifications() {
        try {
            logger.debug("Начало обработки ожидающих уведомлений");
            
            List<NotificationQueue> pendingNotifications = queueService.getReadyForProcessing(batchSize);
            
            if (!pendingNotifications.isEmpty()) {
                logger.info("Найдено {} уведомлений для обработки", pendingNotifications.size());
                
                // Пакетная обработка уведомлений
                queueService.processBatch(pendingNotifications)
                    .thenRun(() -> logger.debug("Завершена обработка пакета уведомлений"))
                    .exceptionally(throwable -> {
                        logger.error("Ошибка при пакетной обработке уведомлений", throwable);
                        return null;
                    });
            } else {
                logger.debug("Нет ожидающих уведомлений для обработки");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при обработке ожидающих уведомлений", e);
        }
    }

    /**
     * Обработка уведомлений для повторной отправки
     */
    @Scheduled(fixedDelayString = "${app.notification.queue.retry-interval:30000}")
    public void processRetryNotifications() {
        try {
            logger.debug("Начало обработки уведомлений для повторной отправки");
            
            List<NotificationQueue> retryNotifications = queueService.getReadyForRetry(batchSize / 2);
            
            if (!retryNotifications.isEmpty()) {
                logger.info("Найдено {} уведомлений для повторной отправки", retryNotifications.size());
                
                // Пакетная обработка повторных уведомлений
                queueService.processBatch(retryNotifications)
                    .thenRun(() -> logger.debug("Завершена обработка пакета повторных уведомлений"))
                    .exceptionally(throwable -> {
                        logger.error("Ошибка при пакетной обработке повторных уведомлений", throwable);
                        return null;
                    });
            } else {
                logger.debug("Нет уведомлений для повторной отправки");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при обработке уведомлений для повторной отправки", e);
        }
    }

    /**
     * Очистка старых уведомлений
     */
    @Scheduled(fixedDelayString = "${app.notification.queue.cleanup-interval:3600000}")
    public void cleanupOldNotifications() {
        try {
            logger.debug("Начало очистки старых уведомлений");
            
            int deletedCount = queueService.cleanupOldNotifications(30); // Храним 30 дней
            
            if (deletedCount > 0) {
                logger.info("Удалено {} старых уведомлений из очереди", deletedCount);
            } else {
                logger.debug("Нет старых уведомлений для удаления");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при очистке старых уведомлений", e);
        }
    }

    /**
     * Проверка просроченных уведомлений
     */
    @Scheduled(fixedDelayString = "${app.notification.queue.stale-check-interval:600000}")
    public void checkStaleNotifications() {
        try {
            logger.debug("Начало проверки просроченных уведомлений");
            
            List<NotificationQueue> staleNotifications = queueService.getStaleNotifications();
            
            if (!staleNotifications.isEmpty()) {
                logger.warn("Найдено {} просроченных уведомлений", staleNotifications.size());
                
                // Логируем просроченные уведомления для анализа
                staleNotifications.forEach(notification -> 
                    logger.warn("Просроченное уведомление: id={}, status={}, createdAt={}", 
                               notification.getId(), notification.getStatus(), notification.getCreatedAt()));
            } else {
                logger.debug("Нет просроченных уведомлений");
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при проверке просроченных уведомлений", e);
        }
    }

    /**
     * Вывод статистики очереди
     */
    @Scheduled(fixedDelayString = "${app.notification.queue.stats-interval:300000}")
    public void logQueueStatistics() {
        try {
            var stats = queueService.getQueueStatistics();
            
            logger.info("Статистика очереди уведомлений: " +
                       "pending={}, processing={}, sent={}, failed={}, " +
                       "avgTime={}s, retries={}",
                       stats.getPendingCount(),
                       stats.getProcessingCount(),
                       stats.getSentCount(),
                       stats.getFailedCount(),
                       stats.getAverageProcessingTimeSeconds(),
                       stats.getRetryCount());
            
        } catch (Exception e) {
            logger.error("Ошибка при получении статистики очереди", e);
        }
    }
}