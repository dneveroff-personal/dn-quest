package dn.quest.statistics.repository;

import dn.quest.statistics.entity.GameStatistics;
import dn.quest.statistics.entity.TeamStatistics;
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
 * Репозиторий для работы с игровой статистикой
 */
@Repository
public interface GameStatisticsRepository extends JpaRepository<GameStatistics, Long> {

    /**
     * Найти статистику игровой сессии по ID
     */
    Optional<GameStatistics> findBySessionId(String sessionId);

    /**
     * Найти статистику по пользователю
     */
    List<GameStatistics> findByUserIdOrderByDateDesc(UUID userId);

    /**
     * Найти статистику по пользователю за период
     */
    List<GameStatistics> findByUserIdAndDateBetweenOrderByDateDesc(UUID userId, LocalDate startDate, LocalDate endDate);

    /**
     * Найти статистику по квесту
     */
    List<GameStatistics> findByQuestIdOrderByDateDesc(UUID questId);

    /**
     * Найти статистику по квесту за период
     */
    List<GameStatistics> findByQuestIdAndDateBetweenOrderByDateDesc(UUID questId, LocalDate startDate, LocalDate endDate);

    /**
     * Найти статистику по команде
     */
    List<GameStatistics> findByTeamIdOrderByDateDesc(UUID teamId);

    /**
     * Найти статистику по команде за период
     */
    List<GameStatistics> findByTeamIdAndDateBetweenOrderByDateDesc(UUID teamId, LocalDate startDate, LocalDate endDate);

    /**
     * Получить количество игровых сессий за дату
     */
    @Query("SELECT COUNT(g) FROM GameStatistics g WHERE g.date = :date")
    Long countGameSessionsByDate(@Param("date") LocalDate date);

    /**
     * Получить количество завершенных игровых сессий за дату
     */
    @Query("SELECT COUNT(g) FROM GameStatistics g WHERE g.date = :date AND g.isCompleted = true")
    Long countCompletedGameSessionsByDate(@Param("date") LocalDate date);

    /**
     * Получить общее время в игре за дату (в минутах)
     */
    @Query("SELECT COALESCE(SUM(g.durationMinutes), 0) FROM GameStatistics g WHERE g.date = :date")
    Long totalGameTimeByDate(@Param("date") LocalDate date);

    /**
     * Получить среднее время игровой сессии за дату (в минутах)
     */
    @Query("SELECT AVG(g.durationMinutes) FROM GameStatistics g WHERE g.date = :date AND g.durationMinutes IS NOT NULL")
    Double avgSessionTimeByDate(@Param("date") LocalDate date);

    /**
     * Получить количество отправок кода за дату
     */
    @Query("SELECT COALESCE(SUM(g.codeSubmissions), 0) FROM GameStatistics g WHERE g.date = :date")
    Long countCodeSubmissionsByDate(@Param("date") LocalDate date);

    /**
     * Получить количество успешных отправок кода за дату
     */
    @Query("SELECT COALESCE(SUM(g.successfulSubmissions), 0) FROM GameStatistics g WHERE g.date = :date")
    Long countSuccessfulCodeSubmissionsByDate(@Param("date") LocalDate date);

    /**
     * Получить коэффициент успешности отправок кода за дату
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN SUM(g.codeSubmissions) = 0 THEN 0 " +
           "ELSE (SUM(g.successfulSubmissions) * 100.0 / SUM(g.codeSubmissions)) " +
           "END " +
           "FROM GameStatistics g WHERE g.date = :date")
    Double getCodeSuccessRateByDate(@Param("date") LocalDate date);

    /**
     * Получить топ игроков по времени в игре
     */
    @Query("SELECT g FROM GameStatistics g WHERE g.date = :date AND g.userId IS NOT NULL ORDER BY g.durationMinutes DESC")
    List<GameStatistics> findTopPlayersByGameTime(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить топ игроков по количеству завершенных уровней
     */
    @Query("SELECT g FROM GameStatistics g WHERE g.date = :date AND g.userId IS NOT NULL ORDER BY g.completedLevels DESC")
    List<GameStatistics> findTopPlayersByCompletedLevels(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить статистику по игровым сессиям за период
     */
    @Query("SELECT " +
           "g.userId, " +
           "COUNT(g) as totalSessions, " +
           "SUM(CASE WHEN g.isCompleted = true THEN 1 ELSE 0 END) as completedSessions, " +
           "SUM(g.durationMinutes) as totalTime, " +
           "AVG(g.durationMinutes) as avgTime, " +
           "SUM(g.codeSubmissions) as totalSubmissions, " +
           "SUM(g.successfulSubmissions) as successfulSubmissions " +
           "FROM GameStatistics g " +
           "WHERE g.date BETWEEN :startDate AND :endDate AND g.userId IS NOT NULL " +
           "GROUP BY g.userId")
    List<Object[]> getUserGamePeriodStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить статистику по квестам за период
     */
    @Query("SELECT " +
           "g.questId, " +
           "COUNT(g) as totalSessions, " +
           "SUM(CASE WHEN g.isCompleted = true THEN 1 ELSE 0 END) as completedSessions, " +
           "SUM(g.durationMinutes) as totalTime, " +
           "AVG(g.durationMinutes) as avgTime, " +
           "AVG(g.completionPercentage) as avgCompletion " +
           "FROM GameStatistics g " +
           "WHERE g.date BETWEEN :startDate AND :endDate " +
           "GROUP BY g.questId")
    List<Object[]> getQuestGamePeriodStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить статистику по типам сессий
     */
    @Query("SELECT " +
           "g.sessionType, " +
           "COUNT(g) as totalSessions, " +
           "SUM(CASE WHEN g.isCompleted = true THEN 1 ELSE 0 END) as completedSessions, " +
           "AVG(g.durationMinutes) as avgTime " +
           "FROM GameStatistics g " +
           "WHERE g.date = :date " +
           "GROUP BY g.sessionType")
    List<Object[]> getSessionTypeStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить статистику по статусам сессий
     */
    @Query("SELECT " +
           "g.status, " +
           "COUNT(g) as totalSessions, " +
           "AVG(g.durationMinutes) as avgTime, " +
           "AVG(g.completionPercentage) as avgCompletion " +
           "FROM GameStatistics g " +
           "WHERE g.date = :date " +
           "GROUP BY g.status")
    List<Object[]> getSessionStatusStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить среднее время завершения уровня за дату
     */
    @Query("SELECT AVG(g.avgLevelTimeMinutes) FROM GameStatistics g WHERE g.date = :date AND g.avgLevelTimeMinutes IS NOT NULL")
    Double getAvgLevelTimeByDate(@Param("date") LocalDate date);

    /**
     * Получить статистику по устройствам
     */
    @Query("SELECT " +
           "g.deviceType, " +
           "COUNT(g) as totalSessions, " +
           "AVG(g.durationMinutes) as avgTime " +
           "FROM GameStatistics g " +
           "WHERE g.date = :date AND g.deviceType IS NOT NULL " +
           "GROUP BY g.deviceType")
    List<Object[]> getDeviceStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить статистику по браузерам
     */
    @Query("SELECT " +
           "g.browser, " +
           "COUNT(g) as totalSessions " +
           "FROM GameStatistics g " +
           "WHERE g.date = :date AND g.browser IS NOT NULL " +
           "GROUP BY g.browser " +
           "ORDER BY COUNT(g) DESC")
    List<Object[]> getBrowserStatsByDate(@Param("date") LocalDate date);

    /**
     * Обновить статус игровой сессии
     */
    @Modifying
    @Query("UPDATE GameStatistics g SET g.status = :status, g.endTime = :endTime, g.durationMinutes = :duration, g.isCompleted = :completed WHERE g.sessionId = :sessionId")
    int updateSessionStatus(@Param("sessionId") String sessionId, @Param("status") String status, @Param("endTime") LocalDateTime endTime, @Param("duration") Long duration, @Param("completed") Boolean completed);

    /**
     * Увеличить количество отправок кода
     */
    @Modifying
    @Query("UPDATE GameStatistics g SET g.codeSubmissions = g.codeSubmissions + 1 WHERE g.sessionId = :sessionId")
    int incrementCodeSubmissions(@Param("sessionId") String sessionId);

    /**
     * Увеличить количество успешных отправок кода
     */
    @Modifying
    @Query("UPDATE GameStatistics g SET g.successfulSubmissions = g.successfulSubmissions + 1 WHERE g.sessionId = :sessionId")
    int incrementSuccessfulSubmissions(@Param("sessionId") String sessionId);

    /**
     * Увеличить количество неудачных отправок кода
     */
    @Modifying
    @Query("UPDATE GameStatistics g SET g.failedSubmissions = g.failedSubmissions + 1 WHERE g.sessionId = :sessionId")
    int incrementFailedSubmissions(@Param("sessionId") String sessionId);

    /**
     * Увеличить количество завершенных уровней
     */
    @Modifying
    @Query("UPDATE GameStatistics g SET g.completedLevels = g.completedLevels + 1 WHERE g.sessionId = :sessionId")
    int incrementCompletedLevels(@Param("sessionId") String sessionId);

    /**
     * Обновить текущий уровень
     */
    @Modifying
    @Query("UPDATE GameStatistics g SET g.currentLevel = :level WHERE g.sessionId = :sessionId")
    int updateCurrentLevel(@Param("sessionId") String sessionId, @Param("level") Integer level);

    /**
     * Обновить процент выполнения
     */
    @Modifying
    @Query("UPDATE GameStatistics g SET g.completionPercentage = :percentage WHERE g.sessionId = :sessionId")
    int updateCompletionPercentage(@Param("sessionId") String sessionId, @Param("percentage") Double percentage);

    /**
     * Получить активные игровые сессии
     */
    List<GameStatistics> findByStatusAndStartTimeBefore(String status, LocalDateTime time);

    /**
     * Получить количество активных сессий
     */
    @Query("SELECT COUNT(g) FROM GameStatistics g WHERE g.status = :status")
    Long countActiveSessions(@Param("status") String status);

    /**
     * Удалить старую статистику (для очистки)
     */
    @Modifying
    @Query("DELETE FROM GameStatistics g WHERE g.date < :date")
    int deleteStatisticsOlderThan(@Param("date") LocalDate date);

    /**
     * Найти статистику за дату
     */
    List<GameStatistics> findByDate(LocalDate date);

    /**
     * Найти статистику до указанной даты
     */
    List<GameStatistics> findByDateBefore(LocalDate date);

    /**
     * Получить количество уникальных сессий за период
     */
    @Query("SELECT COUNT(DISTINCT g.sessionId) FROM GameStatistics g WHERE g.date BETWEEN :startDate AND :endDate")
    Long countDistinctSessionsByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Найти статистику за период
     */
    List<GameStatistics> findByDateBetween(LocalDate startDate, LocalDate endDate);
}