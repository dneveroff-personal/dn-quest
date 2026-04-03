package dn.quest.statistics.repository;

import dn.quest.statistics.entity.UserStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы со статистикой пользователей
 */
@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {

    /**
     * Найти статистику пользователя по ID и дате
     */
    Optional<UserStatistics> findByUserIdAndDate(UUID userId, LocalDate date);

    /**
     * Найти всю статистику пользователя по ID
     */
    List<UserStatistics> findByUserIdOrderByDateDesc(UUID userId);

    /**
     * Найти статистику пользователя за период
     */
    List<UserStatistics> findByUserIdAndDateBetweenOrderByDateDesc(UUID userId, LocalDate startDate, LocalDate endDate);

    /**
     * Найти статистику пользователя за период с пагинацией
     */
    Page<UserStatistics> findByUserIdAndDateBetweenOrderByDateDesc(UUID userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Получить активных пользователей за дату
     */
    @Query("SELECT COUNT(DISTINCT u.userId) FROM UserStatistics u WHERE u.date = :date AND (u.logins > 0 OR u.gameSessions > 0)")
    Long countActiveUsersByDate(@Param("date") LocalDate date);

    /**
     * Получить количество новых регистраций за дату
     */
    @Query("SELECT COALESCE(SUM(u.registrations), 0) FROM UserStatistics u WHERE u.date = :date")
    Long countNewRegistrationsByDate(@Param("date") LocalDate date);

    /**
     * Получить количество новых регистраций за период
     */
    @Query("SELECT COALESCE(SUM(u.registrations), 0) FROM UserStatistics u WHERE u.date BETWEEN :startDate AND :endDate")
    Long countNewRegistrationsByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить топ пользователей по количеству игровых сессий
     */
    @Query("SELECT u FROM UserStatistics u WHERE u.date = :date ORDER BY u.gameSessions DESC")
    List<UserStatistics> findTopUsersByGameSessions(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить топ пользователей по количеству завершенных квестов
     */
    @Query("SELECT u FROM UserStatistics u WHERE u.date = :date ORDER BY u.completedQuests DESC")
    List<UserStatistics> findTopUsersByCompletedQuests(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить топ пользователей по рейтингу
     */
    @Query("SELECT u FROM UserStatistics u WHERE u.date = :date AND u.currentRating IS NOT NULL ORDER BY u.currentRating DESC")
    List<UserStatistics> findTopUsersByRating(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить пользователей с последней активностью после указанного времени
     */
    List<UserStatistics> findByLastActiveAtAfter(LocalDateTime time);

    /**
     * Обновить рейтинг пользователя
     */
    @Modifying
    @Query("UPDATE UserStatistics u SET u.currentRating = :rating, u.ratingChange = :change WHERE u.userId = :userId AND u.date = :date")
    int updateUserRating(@Param("userId") UUID userId, @Param("date") LocalDate date, @Param("rating") Double rating, @Param("change") Double change);

    /**
     * Увеличить количество игровых сессий пользователя
     */
    @Modifying
    @Query("UPDATE UserStatistics u SET u.gameSessions = u.gameSessions + 1, u.lastActiveAt = :lastActiveAt, u.lastIp = :ip, u.lastUserAgent = :userAgent WHERE u.userId = :userId AND u.date = :date")
    int incrementGameSessions(@Param("userId") UUID userId, @Param("date") LocalDate date, @Param("lastActiveAt") LocalDateTime lastActiveAt, @Param("ip") String ip, @Param("userAgent") String userAgent);

    /**
     * Увеличить количество завершенных квестов пользователя
     */
    @Modifying
    @Query("UPDATE UserStatistics u SET u.completedQuests = u.completedQuests + 1 WHERE u.userId = :userId AND u.date = :date")
    int incrementCompletedQuests(@Param("userId") UUID userId, @Param("date") LocalDate date);

    /**
     * Увеличить количество успешных отправок кода
     */
    @Modifying
    @Query("UPDATE UserStatistics u SET u.successfulCodeSubmissions = u.successfulCodeSubmissions + 1 WHERE u.userId = :userId AND u.date = :date")
    int incrementSuccessfulCodeSubmissions(@Param("userId") UUID userId, @Param("date") LocalDate date);

    /**
     * Увеличить количество неудачных отправок кода
     */
    @Modifying
    @Query("UPDATE UserStatistics u SET u.failedCodeSubmissions = u.failedCodeSubmissions + 1 WHERE u.userId = :userId AND u.date = :date")
    int incrementFailedCodeSubmissions(@Param("userId") UUID userId, @Param("date") LocalDate date);

    /**
     * Увеличить количество завершенных уровней
     */
    @Modifying
    @Query("UPDATE UserStatistics u SET u.completedLevels = u.completedLevels + 1 WHERE u.userId = :userId AND u.date = :date")
    int incrementCompletedLevels(@Param("userId") UUID userId, @Param("date") LocalDate date);

    /**
     * Добавить время игры
     */
    @Modifying
    @Query("UPDATE UserStatistics u SET u.totalGameTimeMinutes = u.totalGameTimeMinutes + :minutes WHERE u.userId = :userId AND u.date = :date")
    int addGameTime(@Param("userId") UUID userId, @Param("date") LocalDate date, @Param("minutes") Long minutes);

    /**
     * Получить среднее время завершения уровня по пользователю
     */
    @Query("SELECT AVG(u.avgLevelCompletionTimeSeconds) FROM UserStatistics u WHERE u.userId = :userId AND u.date BETWEEN :startDate AND :endDate AND u.avgLevelCompletionTimeSeconds IS NOT NULL")
    Double getAvgLevelCompletionTimeByUser(@Param("userId") UUID userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить количество пользователей с рейтингом
     */
    @Query("SELECT COUNT(DISTINCT u.userId) FROM UserStatistics u WHERE u.date = :date AND u.currentRating IS NOT NULL")
    Long countUsersWithRating(@Param("date") LocalDate date);

    /**
     * Удалить старую статистику (для очистки)
     */
    @Modifying
    @Query("DELETE FROM UserStatistics u WHERE u.date < :date")
    int deleteStatisticsOlderThan(@Param("date") LocalDate date);

    /**
     * Найти статистику за дату
     */
    List<UserStatistics> findByDate(LocalDate date);

    /**
     * Найти статистику до указанной даты
     */
    List<UserStatistics> findByDateBefore(LocalDate date);

    /**
     * Найти статистику за период
     */
    List<UserStatistics> findByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Получить количество уникальных пользователей за период
     */
    @Query("SELECT COUNT(DISTINCT u.userId) FROM UserStatistics u WHERE u.date BETWEEN :startDate AND :endDate")
    Long countDistinctUsersByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить топ пользователей по количеству созданных квестов
     */
    @Query("SELECT u FROM UserStatistics u WHERE u.date = :date ORDER BY u.createdQuests DESC")
    List<UserStatistics> findTopUsersByCreatedQuests(@Param("date") LocalDate date, Pageable pageable);
}