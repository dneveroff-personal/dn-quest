package dn.quest.notification.service.impl;

import dn.quest.notification.entity.NotificationQueue;
import dn.quest.notification.enums.NotificationStatus;
import dn.quest.notification.repository.NotificationQueueRepository;
import dn.quest.notification.service.channel.NotificationChannelManager;
import dn.quest.notification.service.RetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Реализация сервиса для управления retry логикой и Dead Letter Queue
 */
@Service
@Transactional
public class RetryServiceImpl implements RetryService {

    private static final Logger logger = LoggerFactory.getLogger(RetryServiceImpl.class);

    private final NotificationQueueRepository queueRepository;
    private final NotificationChannelManager channelManager;

    @Value("${app.notification.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${app.notification.retry.base-delay-minutes:1}")
    private int baseDelayMinutes;

    @Value("${app.notification.retry.max-delay-minutes:60}")
    private int maxDelayMinutes;

    @Value("${app.notification.retry.exponential-backoff:true}")
    private boolean useExponentialBackoff;

    @Value("${app.notification.dlq.cleanup-days:7}")
    private int dlqCleanupDays;

    public RetryServiceImpl(NotificationQueueRepository queueRepository,
                           NotificationChannelManager channelManager) {
        this.queueRepository = queueRepository;
        this.channelManager = channelManager;
    }

    @Override
    public void handleFailedNotification(NotificationQueue queueItem, String errorMessage) {
        logger.info("Handling failed notification: id={}, attempt={}", 
                   queueItem.getId(), queueItem.getRetryCount());

        if (canRetry(queueItem)) {
            retryNotification(queueItem);
        } else {
            moveToDeadLetterQueue(queueItem, 
                "Превышено максимальное количество попыток: " + errorMessage);
        }
    }

    @Override
    @Async
    public boolean retryNotification(NotificationQueue queueItem) {
        try {
            logger.info("Retrying notification: id={}, attempt={}", 
                       queueItem.getId(), queueItem.getRetryCount() + 1);

            // Увеличиваем счетчик попыток
            queueItem.incrementRetryCount();
            
            // Устанавливаем время следующей попытки
            LocalDateTime nextRetryTime = calculateNextRetryTime(queueItem.getRetryCount());
            queueItem.setNextRetryAt(nextRetryTime);
            
            // Сбрасываем статус на PENDING
            queueItem.setStatus(NotificationStatus.PENDING);
            queueItem.setErrorMessage(null);
            
            // Сохраняем изменения
            queueRepository.save(queueItem);
            
            logger.info("Notification scheduled for retry: id={}, nextRetry={}", 
                       queueItem.getId(), nextRetryTime);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error retrying notification: id=" + queueItem.getId(), e);
            moveToDeadLetterQueue(queueItem, "Ошибка при планировании повторной попытки: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void moveToDeadLetterQueue(NotificationQueue queueItem, String reason) {
        try {
            logger.warn("Moving notification to Dead Letter Queue: id={}, reason={}", 
                       queueItem.getId(), reason);

            queueItem.setStatus(NotificationStatus.DEAD_LETTER);
            queueItem.setErrorMessage(reason);
            queueItem.setProcessedAt(LocalDateTime.now());
            
            queueRepository.save(queueItem);
            
            logger.info("Notification moved to Dead Letter Queue: id={}", queueItem.getId());
            
        } catch (Exception e) {
            logger.error("Error moving notification to Dead Letter Queue: id=" + queueItem.getId(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationQueue> getNotificationsReadyForRetry() {
        return queueRepository.findReadyForRetry(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationQueue> getDeadLetterNotifications() {
        return queueRepository.findByStatus(NotificationStatus.DEAD_LETTER);
    }

    @Override
    @Async
    public CompletableFuture<Void> retryDeadLetterNotifications() {
        try {
            logger.info("Starting retry of Dead Letter Queue notifications");
            
            List<NotificationQueue> dlqNotifications = getDeadLetterNotifications();
            int retriedCount = 0;
            
            for (NotificationQueue notification : dlqNotifications) {
                try {
                    // Сбрасываем счетчик попыток и статус
                    notification.setRetryCount(0);
                    notification.setStatus(NotificationStatus.PENDING);
                    notification.setNextRetryAt(LocalDateTime.now());
                    notification.setErrorMessage(null);
                    notification.setProcessedAt(null);
                    
                    queueRepository.save(notification);
                    retriedCount++;
                    
                    logger.debug("DLQ notification reset for retry: id={}", notification.getId());
                    
                } catch (Exception e) {
                    logger.error("Error resetting DLQ notification: id=" + notification.getId(), e);
                }
            }
            
            logger.info("Completed DLQ retry: {} notifications reset", retriedCount);
            
        } catch (Exception e) {
            logger.error("Error during DLQ retry process", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public int cleanupDeadLetterQueue(int daysToKeep) {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(daysToKeep);
            
            List<NotificationQueue> oldDlqNotifications = queueRepository.findByStatus(NotificationStatus.DEAD_LETTER)
                .stream()
                .filter(n -> n.getProcessedAt() != null && n.getProcessedAt().isBefore(threshold))
                .toList();
            
            queueRepository.deleteAll(oldDlqNotifications);
            
            logger.info("Cleaned up {} old DLQ notifications", oldDlqNotifications.size());
            return oldDlqNotifications.size();
            
        } catch (Exception e) {
            logger.error("Error cleaning up Dead Letter Queue", e);
            return 0;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RetryStatistics getRetryStatistics() {
        RetryStatistics stats = new RetryStatistics();
        
        try {
            // Общая статистика по retry
            List<NotificationQueue> allNotifications = queueRepository.findAll();
            
            long totalRetries = 0;
            long successfulRetries = 0;
            long failedRetries = 0;
            long deadLetterCount = 0;
            
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            
            for (NotificationQueue notification : allNotifications) {
                totalRetries += notification.getRetryCount();
                
                if (notification.getStatus() == NotificationStatus.SENT && notification.getRetryCount() > 0) {
                    successfulRetries += notification.getRetryCount();
                } else if (notification.getStatus() == NotificationStatus.FAILED) {
                    failedRetries += notification.getRetryCount();
                } else if (notification.getStatus() == NotificationStatus.DEAD_LETTER) {
                    deadLetterCount++;
                }
                
                // Статистика за последний час/день
                if (notification.getUpdatedAt() != null) {
                    if (notification.getUpdatedAt().isAfter(oneHourAgo)) {
                        stats.setRetriedInLastHour(stats.getRetriedInLastHour() + notification.getRetryCount());
                    }
                    if (notification.getUpdatedAt().isAfter(oneDayAgo)) {
                        stats.setRetriedInLastDay(stats.getRetriedInLastDay() + notification.getRetryCount());
                    }
                }
            }
            
            stats.setTotalRetries(totalRetries);
            stats.setSuccessfulRetries(successfulRetries);
            stats.setFailedRetries(failedRetries);
            stats.setDeadLetterCount(deadLetterCount);
            
            // Среднее количество попыток
            long notificationsWithRetries = allNotifications.stream()
                .mapToLong(n -> n.getRetryCount() > 0 ? 1 : 0)
                .sum();
            
            if (notificationsWithRetries > 0) {
                stats.setAverageRetryCount((double) totalRetries / notificationsWithRetries);
            }
            
        } catch (Exception e) {
            logger.error("Error calculating retry statistics", e);
        }
        
        return stats;
    }

    @Override
    public boolean canRetry(NotificationQueue queueItem) {
        return queueItem.getRetryCount() < maxRetryAttempts && 
               queueItem.getStatus() != NotificationStatus.DEAD_LETTER;
    }

    @Override
    public LocalDateTime calculateNextRetryTime(int retryCount) {
        int delayMinutes;
        
        if (useExponentialBackoff) {
            // Экспоненциальная задержка: 1, 2, 4, 8, 16, 32 минут
            delayMinutes = Math.min(baseDelayMinutes * (int) Math.pow(2, retryCount), maxDelayMinutes);
        } else {
            // Линейная задержка: 1, 2, 3, 4, 5 минут
            delayMinutes = Math.min(baseDelayMinutes * (retryCount + 1), maxDelayMinutes);
        }
        
        // Добавляем случайную компоненту для избежания thundering herd
        int jitter = (int) (Math.random() * delayMinutes * 0.1); // 10% jitter
        
        return LocalDateTime.now().plusMinutes(delayMinutes + jitter);
    }
}