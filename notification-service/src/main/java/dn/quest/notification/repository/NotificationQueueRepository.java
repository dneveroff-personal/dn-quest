package dn.quest.notification.repository;

import dn.quest.notification.entity.NotificationQueue;
import dn.quest.notification.enums.NotificationPriority;
import dn.quest.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с очередью уведомлений
 */
@Repository
public interface NotificationQueueRepository extends JpaRepository<NotificationQueue, UUID> {

    /**
     * Находит уведомления в очереди по статусу
     */
    List<NotificationQueue> findByStatus(NotificationStatus status);

    /**
     * Находит уведомления в очереди по статусу и приоритету
     */
    List<NotificationQueue> findByStatusAndPriorityOrderByCreatedAtAsc(
            NotificationStatus status, NotificationPriority priority);

    /**
     * Находит уведомления готовые к повторной отправке
     */
    @Query("SELECT nq FROM NotificationQueue nq WHERE nq.status = 'FAILED' " +
           "AND nq.retryCount < nq.maxRetries " +
           "AND (nq.nextRetryAt IS NULL OR nq.nextRetryAt <= :now)")
    List<NotificationQueue> findReadyForRetry(@Param("now") LocalDateTime now);

    /**
     * Находит уведомления готовые к обработке
     */
    @Query("SELECT nq FROM NotificationQueue nq WHERE nq.status = 'PENDING' " +
           "AND (nq.scheduledAt IS NULL OR nq.scheduledAt <= :now) " +
           "ORDER BY nq.priority DESC, nq.createdAt ASC")
    List<NotificationQueue> findReadyForProcessing(@Param("now") LocalDateTime now);

    /**
     * Находит уведомления по ID пользователя
     */
    Page<NotificationQueue> findByUserId(UUID userId, Pageable pageable);

    /**
     * Находит уведомления по ID пользователя и статусу
     */
    List<NotificationQueue> findByUserIdAndStatus(UUID userId, NotificationStatus status);

    /**
     * Подсчитывает количество уведомлений по статусу
     */
    long countByStatus(NotificationStatus status);

    /**
     * Подсчитывает количество уведомлений по статусу и приоритету
     */
    long countByStatusAndPriority(NotificationStatus status, NotificationPriority priority);

    /**
     * Находит просроченные уведомления
     */
    @Query("SELECT nq FROM NotificationQueue nq WHERE nq.status = 'PENDING' " +
           "AND nq.scheduledAt IS NOT NULL AND nq.scheduledAt < :threshold")
    List<NotificationQueue> findStaleNotifications(@Param("threshold") LocalDateTime threshold);

    /**
     * Удаляет старые обработанные уведомления
     */
    @Modifying
    @Query("DELETE FROM NotificationQueue nq WHERE nq.status IN ('SENT', 'FAILED') " +
           "AND nq.processedAt IS NOT NULL AND nq.processedAt < :threshold")
    int deleteOldProcessedNotifications(@Param("threshold") LocalDateTime threshold);

    /**
     * Обновляет статус уведомлений в очереди
     */
    @Modifying
    @Query("UPDATE NotificationQueue nq SET nq.status = :status, nq.processedAt = :processedAt " +
           "WHERE nq.id = :id")
    int updateNotificationStatus(@Param("id") UUID id,
                                @Param("status") NotificationStatus status, 
                                @Param("processedAt") LocalDateTime processedAt);

    /**
     * Находит уведомления по типу канала
     */
    List<NotificationQueue> findByChannelTypeAndStatus(String channelType, NotificationStatus status);

    /**
     * Получает статистику по очереди
     */
    @Query("SELECT nq.status, COUNT(nq) FROM NotificationQueue nq GROUP BY nq.status")
    List<Object[]> getQueueStatistics();

    /**
     * Получает среднее время обработки уведомлений
     */
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (processed_at - created_at))) FROM notifications.notification_queue WHERE processed_at IS NOT NULL", nativeQuery = true)
    Double getAverageProcessingTime();

    /**
     * Находит уведомления с ошибками для анализа
     */
    @Query("SELECT nq FROM NotificationQueue nq WHERE nq.status = 'FAILED' " +
           "AND nq.errorMessage IS NOT NULL ORDER BY nq.updatedAt DESC")
    List<NotificationQueue> findFailedNotifications();

    /**
     * Подсчитывает количество уведомлений по типу канала
     */
    @Query("SELECT nq.channelType, COUNT(nq) FROM NotificationQueue nq GROUP BY nq.channelType")
    List<Object[]> countByChannelType();

    /**
     * Находит уведомления по ID пользователя (без пагинации)
     */
    List<NotificationQueue> findByUserId(UUID userId);
}