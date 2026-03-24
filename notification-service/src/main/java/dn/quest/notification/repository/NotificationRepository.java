package dn.quest.notification.repository;

import dn.quest.notification.entity.Notification;
import dn.quest.notification.enums.NotificationStatus;
import dn.quest.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с уведомлениями
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Найти уведомление по ID
     */
    Optional<Notification> findByNotificationId(String notificationId);

    /**
     * Найти все уведомления пользователя
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Найти уведомления пользователя по статусу
     */
    Page<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, NotificationStatus status, Pageable pageable);

    /**
     * Найти уведомления пользователя по типу
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type, Pageable pageable);

    /**
     * Найти непрочитанные уведомления пользователя
     */
    List<Notification> findByUserIdAndStatusNotOrderByCreatedAtDesc(Long userId, NotificationStatus status);

    /**
     * Найти уведомления для отправки (статус PENDING и время отправки наступило)
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now)")
    List<Notification> findNotificationsToSend(@Param("now") Instant now);

    /**
     * Найти уведомления для повторной отправки (статус FAILED и количество попыток меньше максимального)
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount < n.maxRetries")
    List<Notification> findNotificationsToRetry();

    /**
     * Обновить статус уведомления
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.updatedAt = :now WHERE n.notificationId = :notificationId")
    int updateNotificationStatus(@Param("notificationId") String notificationId, 
                                @Param("status") NotificationStatus status, 
                                @Param("now") Instant now);

    /**
     * Отметить уведомление как доставленное
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'DELIVERED', n.deliveredAt = :deliveredAt, n.updatedAt = :now WHERE n.notificationId = :notificationId")
    int markAsDelivered(@Param("notificationId") String notificationId, 
                       @Param("deliveredAt") Instant deliveredAt, 
                       @Param("now") Instant now);

    /**
     * Отметить уведомление как прочитанное
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readAt, n.updatedAt = :now WHERE n.notificationId = :notificationId")
    int markAsRead(@Param("notificationId") String notificationId, 
                  @Param("readAt") Instant readAt, 
                  @Param("now") Instant now);

    /**
     * Увеличить счетчик попыток отправки
     */
    @Modifying
    @Query("UPDATE Notification n SET n.retryCount = n.retryCount + 1, n.updatedAt = :now WHERE n.notificationId = :notificationId")
    int incrementRetryCount(@Param("notificationId") String notificationId, @Param("now") Instant now);

    /**
     * Обновить ошибку отправки
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = 'FAILED', n.errorMessage = :errorMessage, n.updatedAt = :now WHERE n.notificationId = :notificationId")
    int updateError(@Param("notificationId") String notificationId, 
                   @Param("errorMessage") String errorMessage, 
                   @Param("now") Instant now);

    /**
     * Подсчитать количество уведомлений пользователя по статусу
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") NotificationStatus status);

    /**
     * Подсчитать количество уведомлений пользователя за период
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.createdAt BETWEEN :start AND :end")
    long countByUserIdAndCreatedAtBetween(@Param("userId") Long userId, 
                                         @Param("start") Instant start, 
                                         @Param("end") Instant end);

    /**
     * Найти уведомления по связанной сущности
     */
    List<Notification> findByRelatedEntityIdAndRelatedEntityTypeOrderByCreatedAtDesc(String relatedEntityId, String relatedEntityType);

    /**
     * Удалить старые уведомления
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate AND n.status IN ('DELIVERED', 'READ')")
    int deleteOldNotifications(@Param("cutoffDate") Instant cutoffDate);

    /**
     * Найти уведомления для статистики
     */
    @Query("SELECT n.type, n.status, COUNT(n) FROM Notification n WHERE n.createdAt BETWEEN :start AND :end GROUP BY n.type, n.status")
    List<Object[]> getNotificationStatistics(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Найти уведомления по периоду
     */
    List<Notification> findByCreatedAtBetween(Instant start, Instant end);

    /**
     * Найти уведомления по статусу
     */
    List<Notification> findByStatus(NotificationStatus status);

    /**
     * Найти уведомления по ID пользователя
     */
    List<Notification> findByUserId(Long userId);

    /**
     * Подсчитать количество уведомлений по типу
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n GROUP BY n.type")
    List<Object[]> countByType();

    /**
     * Подсчитать количество уведомлений по категории
     */
    @Query("SELECT n.category, COUNT(n) FROM Notification n GROUP BY n.category")
    List<Object[]> countByCategory();

    /**
     * Подсчитать количество уведомлений по статусу
     */
    @Query("SELECT n.status, COUNT(n) FROM Notification n GROUP BY n.status")
    List<Object[]> countByStatus();

    /**
     * Подсчитать количество уведомлений по пользователю (по убыванию)
     */
    @Query("SELECT n.userId, COUNT(n) as cnt FROM Notification n GROUP BY n.userId ORDER BY cnt DESC")
    List<Object[]> countByUserIdOrderByCountDesc();

    /**
     * Получить среднее время доставки
     */
    @Query("SELECT AVG(FUNCTION('TIMESTAMPDIFF', 'SECOND', n.createdAt, n.deliveredAt)) FROM Notification n WHERE n.deliveredAt IS NOT NULL")
    Double getAverageDeliveryTime();

    /**
     * Найти уведомления по статусу и периоду
     */
    List<Notification> findByCreatedAtBetweenAndStatus(Instant start, Instant end, NotificationStatus status);
}