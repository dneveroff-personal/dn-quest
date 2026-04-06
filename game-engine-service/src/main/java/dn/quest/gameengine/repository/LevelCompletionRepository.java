package dn.quest.gameengine.repository;

import dn.quest.gameengine.entity.LevelCompletion;
import dn.quest.gameengine.entity.Quest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с завершениями уровней
 */
@Repository
public interface LevelCompletionRepository extends JpaRepository<LevelCompletion, Long> {

    // Базовые запросы
    @Query("select lc from LevelCompletion lc where lc.level.quest = :quest order by lc.passTime asc")
    List<LevelCompletion> findByQuest(@Param("quest") Quest quest);

    // Запросы для лидербордов
    @Query("""
        select new dn.quest.gameengine.dto.LevelCompletionDTO(
            l.id,
            l.title,
            s.team.id,
            coalesce(s.team.name, '-'),
            u.id,
            coalesce(u.publicName, '-'),
            lc.passTime,
            '00:00:00',
            lc.bonusOnLevelSec,
            lc.penaltyOnLevelSec
        )
        from LevelCompletion lc
        join lc.level l
        join lc.session s
        left join s.team
        left join lc.passedByUser u
        where l.quest.id = :questId
        order by lc.passTime asc
    """)
    List<dn.quest.gameengine.dto.LevelCompletionDTO> findLeaderboardByQuestId(@Param("questId") UUID questId);

    // Статистические запросы
    @Query("select count(distinct lc.session.id) from LevelCompletion lc where lc.level.quest.id = :questId")
    long countDistinctSessionsByQuestId(@Param("questId") UUID questId);

    @Query("select coalesce(avg(lc.durationSec),0) from LevelCompletion lc where lc.level.quest.id = :questId")
    double averageDurationSecByQuestId(@Param("questId") UUID questId);

    // Запросы по времени
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.passTime >= :since ORDER BY lc.passTime DESC")
    List<LevelCompletion> findByPassTimeAfter(@Param("since") Instant since);

    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.passTime BETWEEN :start AND :end ORDER BY lc.passTime DESC")
    List<LevelCompletion> findByPassTimeBetween(@Param("start") Instant start, @Param("end") Instant end);

    // Запросы для анализа производительности
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.durationSec <= :threshold ORDER BY lc.durationSec ASC")
    List<LevelCompletion> findFastestCompletions(@Param("threshold") long threshold);

    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.durationSec >= :threshold ORDER BY lc.durationSec DESC")
    List<LevelCompletion> findSlowestCompletions(@Param("threshold") long threshold);

    // Запросы для анализа бонусов и штрафов
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.bonusOnLevelSec > 0 ORDER BY lc.bonusOnLevelSec DESC")
    List<LevelCompletion> findCompletionsWithBonus();

    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.penaltyOnLevelSec > 0 ORDER BY lc.penaltyOnLevelSec DESC")
    List<LevelCompletion> findCompletionsWithPenalty();

    @Query("SELECT AVG(lc.bonusOnLevelSec) FROM LevelCompletion lc WHERE lc.level.quest.id = :questId")
    Double getAverageBonusByQuestId(@Param("questId") UUID questId);

    @Query("SELECT AVG(lc.penaltyOnLevelSec) FROM LevelCompletion lc WHERE lc.level.quest.id = :questId")
    Double getAveragePenaltyByQuestId(@Param("questId") UUID questId);

    // Запросы для анализа попыток
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.totalAttempts > 0 ORDER BY lc.totalAttempts DESC")
    List<LevelCompletion> findCompletionsWithMostAttempts();

    @Query("SELECT AVG(lc.totalAttempts) FROM LevelCompletion lc WHERE lc.level.quest.id = :questId")
    Double getAverageAttemptsByQuestId(@Param("questId") UUID questId);

    @Query("SELECT AVG(lc.successfulAttempts) FROM LevelCompletion lc WHERE lc.level.quest.id = :questId")
    Double getAverageSuccessfulAttemptsByQuestId(@Param("questId") UUID questId);

    // Запросы для анализа подсказок
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.hintsUsed > 0 ORDER BY lc.hintsUsed DESC")
    List<LevelCompletion> findCompletionsWithHints();

    @Query("SELECT AVG(lc.hintsUsed) FROM LevelCompletion lc WHERE lc.level.quest.id = :questId")
    Double getAverageHintsByQuestId(@Param("questId") UUID questId);

    // Запросы для анализа методов завершения
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.completionMethod = :method ORDER BY lc.passTime DESC")
    List<LevelCompletion> findByCompletionMethod(@Param("method") String method);

    @Query("SELECT lc.completionMethod, COUNT(lc) FROM LevelCompletion lc WHERE lc.level.quest.id = :questId GROUP BY lc.completionMethod")
    List<Object[]> getCompletionMethodStatistics(@Param("questId") UUID questId);

    // Запросы для анализа по пользователям
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.passedByUser.id = :userId ORDER BY lc.passTime DESC")
    List<LevelCompletion> findByPassedByUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(lc) FROM LevelCompletion lc WHERE lc.passedByUser.id = :userId")
    long countByPassedByUser(@Param("userId") UUID userId);

    // Запросы для анализа по командам
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.session.team.id = :teamId ORDER BY lc.passTime DESC")
    List<LevelCompletion> findByTeam(@Param("teamId") UUID teamId);

    @Query("SELECT COUNT(lc) FROM LevelCompletion lc WHERE lc.session.team.id = :teamId")
    long countByTeam(@Param("teamId") UUID teamId);

    // Запросы для анализа по уровням
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.level.id = :levelId ORDER BY lc.passTime DESC")
    List<LevelCompletion> findByLevel(@Param("levelId") UUID levelId);

    @Query("SELECT COUNT(lc) FROM LevelCompletion lc WHERE lc.level.id = :levelId")
    long countByLevel(@Param("levelId") UUID levelId);

    @Query("SELECT AVG(lc.durationSec) FROM LevelCompletion lc WHERE lc.level.id = :levelId")
    Double getAverageDurationByLevel(@Param("levelId") UUID levelId);

    // Запросы для анализа эффективности
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.totalAttempts > 0 ORDER BY (CAST(lc.successfulAttempts AS double) / lc.totalAttempts) DESC")
    List<LevelCompletion> findMostEfficientCompletions();

    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.totalAttempts > 0 ORDER BY (CAST(lc.successfulAttempts AS double) / lc.totalAttempts) ASC")
    List<LevelCompletion> findLeastEfficientCompletions();

    // Запросы для пагинации
    Page<LevelCompletion> findByQuestOrderByPassTimeAsc(Quest quest, Pageable pageable);
    
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.level.quest.id = :questId ORDER BY lc.passTime ASC")
    Page<LevelCompletion> findByQuestIdOrderByPassTimeAsc(@Param("questId") UUID questId, Pageable pageable);

    // Запросы для анализа по секторам
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.sectorsClosed >= :threshold ORDER BY lc.sectorsClosed DESC")
    List<LevelCompletion> findCompletionsWithMinSectors(@Param("threshold") int threshold);

    @Query("SELECT AVG(lc.sectorsClosed) FROM LevelCompletion lc WHERE lc.level.quest.id = :questId")
    Double getAverageSectorsByQuestId(@Param("questId") UUID questId);

    // Запросы для поиска лучших результатов
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.level.id = :levelId ORDER BY lc.adjustedDurationSec ASC")
    List<LevelCompletion> findBestCompletionsByLevel(@Param("levelId") UUID levelId);

    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.level.quest.id = :questId ORDER BY lc.adjustedDurationSec ASC")
    List<LevelCompletion> findBestCompletionsByQuest(@Param("questId") UUID questId);

    // Запросы для анализа по IP
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.ipAddress = :ipAddress ORDER BY lc.passTime DESC")
    List<LevelCompletion> findByIpAddress(@Param("ipAddress") String ipAddress);

    // Запросы для анализа временных паттернов
    @Query("SELECT lc FROM LevelCompletion lc WHERE DATE(lc.passTime) = CURRENT_DATE ORDER BY lc.passTime DESC")
    List<LevelCompletion> findTodayCompletions();

    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.passTime >= :since ORDER BY lc.passTime DESC")
    List<LevelCompletion> findRecentCompletions(@Param("since") Instant since);

    // Запросы для статистики по квестам
    @Query("SELECT lc.level.quest.id, COUNT(lc), AVG(lc.durationSec), AVG(lc.totalAttempts) FROM LevelCompletion lc GROUP BY lc.level.quest.id")
    List<Object[]> getQuestStatistics();

    // Запросы для анализа качества прохождения
    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.completionMethod = 'MANUAL' ORDER BY lc.durationSec ASC")
    List<LevelCompletion> findManualCompletions();

    @Query("SELECT lc FROM LevelCompletion lc WHERE lc.completionMethod != 'MANUAL' ORDER BY lc.durationSec ASC")
    List<LevelCompletion> findAutoCompletions();
}