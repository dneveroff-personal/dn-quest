package dn.quest.notification.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.notification.entity.Notification;
import dn.quest.notification.entity.NotificationQueue;
import dn.quest.notification.enums.NotificationPriority;
import dn.quest.notification.enums.NotificationStatus;
import dn.quest.notification.exception.NotificationException;
import dn.quest.notification.repository.NotificationQueueRepository;
import dn.quest.notification.repository.NotificationRepository;
import dn.quest.notification.service.NotificationChannelManager;
import dn.quest.notification.service.NotificationQueueService;
import dn.quest.notification.service.RetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Реализация сервиса для управления очередью уведомлений
 */
@Service
@Transactional
public class NotificationQueueServiceImpl implements NotificationQueueService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationQueueServiceImpl.class);

    private final NotificationQueueRepository queueRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationChannelManager channelManager;
    private final RetryService retryService;
    private final ObjectMapper objectMapper;

    @Value("${app.notification.queue.batch-size:100}")
    private int defaultBatchSize;

    @Value("${app.notification.queue.cleanup-days:30}")
    private int cleanupDays;

    public NotificationQueueServiceImpl(NotificationQueueRepository queueRepository,
                                     NotificationRepository notificationRepository,
                                     NotificationChannelManager channelManager,
                                     RetryService retryService,
                                     ObjectMapper objectMapper) {
        this.queueRepository = queueRepository;
        this.notificationRepository = notificationRepository;
        this.channelManager = channelManager;
        this.retryService = retryService;
        this.objectMapper = objectMapper;
    }

    @Override
    public NotificationQueue addToQueue(Long notificationId, Long userId, String channelType, 
                                      NotificationPriority priority, String payload) {
        return addToQueue(notificationId, userId, channelType, priority, payload, null);
    }

    @Override
    public NotificationQueue addToQueue(Long notificationId, Long userId, String channelType, 
                                      NotificationPriority priority, String payload, 
                                      LocalDateTime scheduledAt) {
        try {
            NotificationQueue queueItem = NotificationQueue.builder()
                .notificationId(notificationId)
                .userId(userId)
                .channelType(channelType)
                .priority(priority)
                .status(NotificationStatus.PENDING)
                .payload(payload)
                .scheduledAt(scheduledAt)
                .build();

            NotificationQueue saved = queueRepository.save(queueItem);
            logger.info("Уведомление добавлено в очередь: id={}, userId={}, channel={}", 
                       saved.getId(), userId, channelType);
            
            return saved;
        } catch (Exception e) {
            logger.error("Ошибка при добавлении уведомления в очередь", e);
            throw new NotificationException("Не удалось добавить уведомление в очередь", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationQueue> getReadyForProcessing(int limit) {
        List<NotificationQueue> items = queueRepository.findReadyForProcessing(LocalDateTime.now());
        return items.stream().limit(limit).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationQueue> getReadyForRetry(int limit) {
        List<NotificationQueue> items = queueRepository.findReadyForRetry(LocalDateTime.now());
        return items.stream().limit(limit).toList();
    }

    @Override
    @Async
    public CompletableFuture<Void> processNotification(NotificationQueue queueItem) {
        try {
            logger.info("Начало обработки уведомления из очереди: id={}", queueItem.getId());
            
            // Обновляем статус на PROCESSING
            queueItem.setStatus(NotificationStatus.PROCESSING);
            queueRepository.save(queueItem);

            // Получаем уведомление
            Optional<Notification> notificationOpt = notificationRepository.findById(queueItem.getNotificationId());
            if (notificationOpt.isEmpty()) {
                markAsFailed(queueItem.getId(), "Уведомление не найдено");
                return CompletableFuture.completedFuture(null);
            }

            Notification notification = notificationOpt.get();

            // Отправляем уведомление через соответствующий канал
            boolean success = channelManager.sendNotification(notification, queueItem.getChannelType());

            if (success) {
                markAsSent(queueItem.getId());
                logger.info("Уведомление успешно отправлено: queueId={}", queueItem.getId());
            } else {
                retryNotification(queueItem);
                logger.warn("Не удалось отправить уведомление: queueId={}", queueItem.getId());
            }

        } catch (Exception e) {
            logger.error("Ошибка при обработке уведомления из очереди: id=" + queueItem.getId(), e);
            markAsFailed(queueItem.getId(), e.getMessage());
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void retryNotification(NotificationQueue queueItem) {
        retryService.handleFailedNotification(queueItem, queueItem.getErrorMessage());
    }

    @Override
    public void markAsSent(Long queueItemId) {
        Optional<NotificationQueue> queueItemOpt = queueRepository.findById(queueItemId);
        if (queueItemOpt.isPresent()) {
            NotificationQueue queueItem = queueItemOpt.get();
            queueItem.markAsProcessed();
            queueRepository.save(queueItem);
            
            // Обновляем статус основного уведомления
            notificationRepository.findById(queueItem.getNotificationId())
                .ifPresent(notification -> {
                    notification.setStatus(NotificationStatus.SENT);
                    notificationRepository.save(notification);
                });
        }
    }

    @Override
    public void markAsFailed(Long queueItemId, String errorMessage) {
        Optional<NotificationQueue> queueItemOpt = queueRepository.findById(queueItemId);
        if (queueItemOpt.isPresent()) {
            NotificationQueue queueItem = queueItemOpt.get();
            queueItem.markAsFailed(errorMessage);
            queueRepository.save(queueItem);
            
            logger.error("Уведомление отмечено как неудачное: id={}, error={}", 
                        queueItemId, errorMessage);
        }
    }

    @Override
    public void moveToDeadLetterQueue(NotificationQueue queueItem) {
        retryService.moveToDeadLetterQueue(queueItem, "Перемещено в DLQ из очереди");
    }

    @Override
    public int cleanupOldNotifications(int daysToKeep) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysToKeep);
        int deleted = queueRepository.deleteOldProcessedNotifications(threshold);
        
        logger.info("Удалено старых уведомлений из очереди: {}", deleted);
        return deleted;
    }

    @Override
    @Transactional(readOnly = true)
    public QueueStatistics getQueueStatistics() {
        QueueStatistics stats = new QueueStatistics();
        
        // Получаем статистику по статусам
        List<Object[]> statusStats = queueRepository.getQueueStatistics();
        for (Object[] row : statusStats) {
            NotificationStatus status = (NotificationStatus) row[0];
            Long count = (Long) row[1];
            
            switch (status) {
                case PENDING -> stats.setPendingCount(count);
                case PROCESSING -> stats.setProcessingCount(count);
                case SENT -> stats.setSentCount(count);
                case FAILED -> stats.setFailedCount(count);
            }
        }
        
        // Получаем среднее время обработки
        Double avgTime = queueRepository.getAverageProcessingTime();
        stats.setAverageProcessingTimeSeconds(avgTime);
        
        // Подсчитываем количество повторных попыток
        stats.setRetryCount(queueRepository.findByStatus(NotificationStatus.FAILED).stream()
            .mapToLong(q -> q.getRetryCount())
            .sum());
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationQueue> getUserNotifications(Long userId) {
        return queueRepository.findByUserId(userId);
    }

    @Override
    public boolean cancelScheduledNotification(Long queueItemId) {
        Optional<NotificationQueue> queueItemOpt = queueRepository.findById(queueItemId);
        if (queueItemOpt.isPresent()) {
            NotificationQueue queueItem = queueItemOpt.get();
            if (queueItem.getStatus() == NotificationStatus.PENDING && 
                queueItem.getScheduledAt() != null && 
                queueItem.getScheduledAt().isAfter(LocalDateTime.now())) {
                
                queueItem.setStatus(NotificationStatus.CANCELLED);
                queueRepository.save(queueItem);
                logger.info("Запланированное уведомление отменено: id={}", queueItemId);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updatePriority(Long queueItemId, NotificationPriority newPriority) {
        Optional<NotificationQueue> queueItemOpt = queueRepository.findById(queueItemId);
        if (queueItemOpt.isPresent()) {
            NotificationQueue queueItem = queueItemOpt.get();
            if (queueItem.getStatus() == NotificationStatus.PENDING) {
                queueItem.setPriority(newPriority);
                queueRepository.save(queueItem);
                logger.info("Приоритет уведомления обновлен: id={}, priority={}", 
                           queueItemId, newPriority);
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationQueue> getStaleNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        return queueRepository.findStaleNotifications(threshold);
    }

    @Override
    @Async
    public CompletableFuture<Void> processBatch(List<NotificationQueue> queueItems) {
        logger.info("Начало пакетной обработки уведомлений: count={}", queueItems.size());
        
        List<CompletableFuture<Void>> futures = queueItems.stream()
            .map(this::processNotification)
            .toList();
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}