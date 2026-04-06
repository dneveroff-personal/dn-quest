package dn.quest.gameengine.repository;

import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.Level;
import dn.quest.gameengine.entity.LevelProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с прогрессом прохождения уровней
 */
@Repository
public interface LevelProgressRepository extends JpaRepository<LevelProgress, UUID> {

    // Базовые запросы
    List<LevelProgress> findBySession(GameSession session);
    List<LevelProgress> findByLevel(Level level);

    // Поиск по сессии и уровню
    @Query("SELECT lp FROM LevelProgress lp WHERE lp.session = :session AND lp.level = :level")
    Optional<LevelProgress> findBySessionAndLevel(@Param("session") GameSession session, @Param("level") Level level);

    // Текущий активный уровень для сессии
    @Query("SELECT lp FROM LevelProgress lp WHERE lp.session = :session AND lp.closedAt IS NULL")
    Optional<LevelProgress> findCurrentBySession(@Param("session") GameSession session);

    @Query("SELECT lp FROM LevelProgress lp WHERE lp.session = :session AND lp.level = :level AND lp.closedAt IS NULL")
    Optional<LevelProgress> findCurrentBySessionAndLevel(@Param("session") GameSession session, @Param("level") Level level);

    // Запросы для получения завершенного прогресса
    @Query("SELECT lp FROM LevelProgress lp WHERE lp.session = :session AND lp.closedAt IS NOT NULL ORDER BY lp.closedAt DESC")
    List<LevelProgress> findCompletedBySession(@Param("session") GameSession session);

    @Query("SELECT lp FROM LevelProgress lp WHERE lp.level = :level AND lp.closedAt IS NOT NULL ORDER BY lp.closedAt DESC")
    List<LevelProgress> findCompletedByLevel(@Param("level") Level level);

    // Статистические запросы
    @Query("SELECT COUNT(lp) FROM LevelProgress lp WHERE lp.session = :session AND lp.closedAt IS NULL")
    long countActiveBySession(@Param("session") GameSession session);

    @Query("SELECT COUNT(lp) FROM LevelProgress lp WHERE lp.session = :session AND lp.closedAt IS NOT NULL")
    long countCompletedBySession(@Param("session") GameSession session);

    @Query("SELECT COUNT(lp) FROM LevelProgress lp WHERE lp.level = :level AND lp.closedAt IS NOT NULL")
    long countCompletedByLevel(@Param("level") Level level);

    // Запросы для анализа времени прохождения
    @Query("SELECT lp FROM LevelProgress lp WHERE lp.startedAt >= :since ORDER BY lp.startedAt DESC")
    List<LevelProgress> findByStartedAfter(@Param("since") Instant since);

    @Query("SELECT lp FROM LevelProgress lp WHERE lp.closedAt >= :since ORDER BY lp.closedAt DESC")
    List<LevelProgress> findByClosedAfter(@Param("since") Instant since);

    @Query("SELECT lp FROM LevelProgress lp WHERE lp.startedAt BETWEEN :start AND :end ORDER BY lp.startedAt DESC")
    List<LevelProgress> findByStartedBetween(@Param("start") Instant start, @Param("end") Instant end);

    // Запросы для анализа производительности
    @Query("SELECT AVG(lp.bonusOnLevelSec) FROM LevelProgress lp WHERE lp.level = :level AND lp.closedAt IS NOT NULL")
    Double getAverageBonusTimeByLevel(@Param("level") Level level);

    @Query("SELECT AVG(lp.penaltyOnLevelSec) FROM LevelProgress lp WHERE lp.level = :level AND lp.closedAt IS NOT NULL")
    Double getAveragePenaltyTimeByLevel(@Param("level") Level level);

    @Query("SELECT AVG(lp.sectorsClosed) FROM LevelProgress lp WHERE lp.level = :level AND lp.closedAt IS NOT NULL")
    Double getAverageSectorsClosedByLevel(@Param("level") Level level);

    // Запросы для анализа подсказок
    @Query("SELECT lp FROM LevelProgress lp WHERE lp.hintsUsed > 0 ORDER BY lp.hintsUsed DESC")
    List<LevelProgress> findProgressWithHints();

    @Query("SELECT AVG(lp.hintsUsed) FROM LevelProgress lp WHERE lp.level = :level AND lp.closedAt IS NOT NULL")
    Double getAverageHintsUsedByLevel(@Param("level") Level level);

    // Запросы для анализа попыток
    @Query("SELECT lp FROM LevelProgress lp WHERE lp.totalAttempts > 0 ORDER BY lp.totalAttempts DESC")
    List<LevelProgress> findProgressWithAttempts();

    @Query("SELECT AVG(lp.totalAttempts) FROM LevelProgress lp WHERE lp.level = :level AND lp.closedAt IS NOT NULL")
    Double getAverageAttemptsByLevel(@Param("level") Level level);

    @Query("SELECT AVG(lp.successfulAttempts) FROM LevelProgress lp WHERE lp.level = :level AND lp.closedAt IS NOT NULL")
    Double getAverageSuccessfulAttemptsByLevel(@Param("level") Level level);

    // Запросы для поиска проблемных уровней
    @Query("SELECT lp.level, COUNT(lp), AVG(lp.totalAttempts) FROM LevelProgress lp WHERE lp.closedAt IS NOT NULL GROUP BY lp.level ORDER BY AVG(lp.totalAttempts) DESC")
    List<Object[]> findLevelsByAverageAttempts();

    @Query("SELECT lp.level, COUNT(lp), AVG(lp.hintsUsed) FROM LevelProgress lp WHERE lp.closedAt IS NOT NULL GROUP BY lp.level ORDER BY AVG(lp.hintsUsed) DESC")
    List<Object[]> findLevelsByAverageHints();

    // Запросы для анализа успешности
    @Query("SELECT lp FROM LevelProgress lp WHERE lp.sectorsClosed >= :threshold ORDER BY lp.sectorsClosed DESC")
    List<LevelProgress> findProgressWithMinSectors(@Param("threshold") int threshold);

    @Query("SELECT COUNT(lp) FROM LevelProgress lp WHERE lp.level = :level AND lp.sectorsClosed >= :threshold AND lp.closedAt IS NOT NULL")
    long countByLevelAndMinSectors(@Param("level") Level level, @Param("threshold") int threshold);

    // Запросы для получения статистики по сессиям
    @Query(value = "SELECT lp.level_id, COUNT(*), AVG(EXTRACT(EPOCH FROM lp.closed_at - lp.started_at)) FROM level_progress lp WHERE lp.session_id = :session AND lp.closed_at IS NOT NULL GROUP BY lp.level_id", nativeQuery = true)
    List<Object[]> getSessionLevelStatistics(@Param("session") GameSession session);

    // Запросы для поиска долгих прохождений
    @Query("SELECT lp FROM LevelProgress lp WHERE lp.closedAt IS NOT NULL ORDER BY lp.closedAt DESC")
    List<LevelProgress> findLongestCompletions();

    @Query(value = "SELECT * FROM level_progress WHERE closed_at IS NOT NULL AND EXTRACT(EPOCH FROM closed_at - started_at) > :threshold ORDER BY closed_at DESC", nativeQuery = true)
    List<LevelProgress> findLongestCompletionsAboveThreshold(@Param("threshold") long threshold);

    // Запросы для поиска быстрых прохождений
    @Query(value = "SELECT * FROM level_progress WHERE closed_at IS NOT NULL ORDER BY EXTRACT(EPOCH FROM closed_at - started_at) ASC", nativeQuery = true)
    List<LevelProgress> findFastestCompletions();

    // Запросы для анализа по времени
    @Query("SELECT lp FROM LevelProgress lp WHERE lp.lastActivityAt >= :since ORDER BY lp.lastActivityAt DESC")
    List<LevelProgress> findByLastActivityAfter(@Param("since") Instant since);

    @Query("SELECT COUNT(lp) FROM LevelProgress lp WHERE lp.closedAt IS NULL AND lp.lastActivityAt < :threshold")
    long countStaleProgress(@Param("threshold") Instant threshold);

    // Запросы для получения прогресса по квесту
    @Query("SELECT lp FROM LevelProgress lp WHERE lp.level.quest.id = :questId AND lp.session = :session ORDER BY lp.level.orderIndex")
    List<LevelProgress> findByQuestAndSession(@Param("questId") UUID questId, @Param("session") GameSession session);

    // Запросы для анализа эффективности
    @Query("SELECT lp.session, COUNT(lp), AVG(lp.sectorsClosed), AVG(lp.hintsUsed) FROM LevelProgress lp WHERE lp.closedAt IS NOT NULL GROUP BY lp.session")
    List<Object[]> getSessionEfficiencyStatistics();

    // Запросы для поиска прогресса с определенными характеристиками
    @Query("SELECT lp FROM LevelProgress lp WHERE lp.bonusOnLevelSec > 0 ORDER BY lp.bonusOnLevelSec DESC")
    List<LevelProgress> findProgressWithBonus();

    @Query("SELECT lp FROM LevelProgress lp WHERE lp.penaltyOnLevelSec > 0 ORDER BY lp.penaltyOnLevelSec DESC")
    List<LevelProgress> findProgressWithPenalty();

    // Запросы для анализа соотношения успешных/неуспешных попыток
    @Query("SELECT lp FROM LevelProgress lp WHERE lp.totalAttempts > 0 AND (CAST(lp.successfulAttempts AS double) / lp.totalAttempts) < :ratio ORDER BY (CAST(lp.successfulAttempts AS double) / lp.totalAttempts) ASC")
    List<LevelProgress> findProgressWithLowSuccessRatio(@Param("ratio") double ratio);
}