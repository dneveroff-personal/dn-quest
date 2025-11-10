package dn.quest.gameengine.repository;

import dn.quest.gameengine.entity.Level;
import dn.quest.gameengine.entity.LevelHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с подсказками уровней
 */
@Repository
public interface LevelHintRepository extends JpaRepository<LevelHint, Long> {

    // Базовые запросы
    List<LevelHint> findByLevel(Level level);
    List<LevelHint> findByLevelOrderByOrderIndex(Level level);

    @Query("select h from LevelHint h where h.level = :level order by h.orderIndex asc")
    List<LevelHint> findByLevelOrderByOrderIndex(@Param("level") Level level);

    // Запросы для поиска активных подсказок
    @Query("select h from LevelHint h where h.isActive = true order by h.level.orderIndex asc, h.orderIndex asc")
    List<LevelHint> findActiveHints();

    @Query("select h from LevelHint h where h.level = :level and h.isActive = true order by h.orderIndex asc")
    List<LevelHint> findActiveHintsByLevel(@Param("level") Level level);

    // Запросы для поиска по типу подсказки
    List<LevelHint> findByHintType(String hintType);

    @Query("select h from LevelHint h where h.level = :level and h.hintType = :hintType order by h.orderIndex asc")
    List<LevelHint> findByLevelAndHintType(@Param("level") Level level, @Param("hintType") String hintType);

    // Запросы для поиска по уровню сложности
    List<LevelHint> findByDifficultyLevel(String difficultyLevel);

    @Query("select h from LevelHint h where h.level = :level and h.difficultyLevel = :difficultyLevel order by h.orderIndex asc")
    List<LevelHint> findByLevelAndDifficultyLevel(@Param("level") Level level, @Param("difficultyLevel") String difficultyLevel);

    // Запросы для поиска по времени доступности
    @Query("select h from LevelHint h where h.offsetSec <= :maxOffset order by h.offsetSec asc")
    List<LevelHint> findHintsAvailableWithin(@Param("maxOffset") Integer maxOffset);

    @Query("select h from LevelHint h where h.level = :level and h.offsetSec <= :maxOffset order by h.offsetSec asc")
    List<LevelHint> findHintsAvailableWithinByLevel(@Param("level") Level level, @Param("maxOffset") Integer maxOffset);

    // Запросы для поиска подсказок с штрафами
    @Query("select h from LevelHint h where h.penaltySeconds > 0 order by h.penaltySeconds desc")
    List<LevelHint> findHintsWithPenalty();

    @Query("select h from LevelHint h where h.level = :level and h.penaltySeconds > 0 order by h.orderIndex asc")
    List<LevelHint> findHintsWithPenaltyByLevel(@Param("level") Level level);

    // Запросы для поиска бесплатных подсказок
    @Query("select h from LevelHint h where h.penaltySeconds = 0 or h.penaltySeconds is null order by h.level.orderIndex asc, h.orderIndex asc")
    List<LevelHint> findFreeHints();

    @Query("select h from LevelHint h where h.level = :level and (h.penaltySeconds = 0 or h.penaltySeconds is null) order by h.orderIndex asc")
    List<LevelHint> findFreeHintsByLevel(@Param("level") Level level);

    // Статистические запросы
    @Query("select count(h) from LevelHint h where h.level = :level")
    long countByLevel(@Param("level") Level level);

    @Query("select count(h) from LevelHint h where h.level = :level and h.isActive = true")
    long countActiveByLevel(@Param("level") Level level);

    @Query("select count(h) from LevelHint h where h.hintType = :hintType")
    long countByHintType(@Param("hintType") String hintType);

    @Query("select count(h) from LevelHint h where h.difficultyLevel = :difficultyLevel")
    long countByDifficultyLevel(@Param("difficultyLevel") String difficultyLevel);

    // Запросы для анализа использования
    @Query("select h from LevelHint h where h.usageCount > 0 order by h.usageCount desc")
    List<LevelHint> findMostUsedHints();

    @Query("select h from LevelHint h where h.usageCount = 0")
    List<LevelHint> findUnusedHints();

    @Query("select avg(h.usageCount) from LevelHint h where h.usageCount > 0")
    Double getAverageUsageCount();

    // Запросы для поиска по тексту подсказки
    @Query("select h from LevelHint h where lower(h.text) like lower(concat('%', :keyword, '%'))")
    List<LevelHint> findByTextContainingIgnoreCase(@Param("keyword") String keyword);

    @Query("select h from LevelHint h where h.level = :level and lower(h.text) like lower(concat('%', :keyword, '%'))")
    List<LevelHint> findByLevelAndTextContainingIgnoreCase(@Param("level") Level level, @Param("keyword") String keyword);

    // Запросы для анализа по уровням
    @Query("select h.level.id, count(h), avg(h.offsetSec), avg(h.penaltySeconds) from LevelHint h group by h.level.id")
    List<Object[]> getHintStatisticsByLevel();

    @Query("select h.level.id, h.hintType, count(h) from LevelHint h group by h.level.id, h.hintType")
    List<Object[]> getHintTypeStatisticsByLevel();

    // Запросы для поиска подсказок с определенными характеристиками
    @Query("select h from LevelHint h where h.offsetSec <= :earlyThreshold order by h.offsetSec asc")
    List<LevelHint> findEarlyHints(@Param("earlyThreshold") Integer earlyThreshold);

    @Query("select h from LevelHint h where h.offsetSec >= :lateThreshold order by h.offsetSec desc")
    List<LevelHint> findLateHints(@Param("lateThreshold") Integer lateThreshold);

    // Запросы для поиска подсказок по стоимости
    @Query("select h from LevelHint h where h.penaltySeconds > :threshold order by h.penaltySeconds desc")
    List<LevelHint> findExpensiveHints(@Param("threshold") Integer threshold);

    @Query("select h from LevelHint h where h.penaltySeconds <= :threshold order by h.penaltySeconds asc")
    List<LevelHint> findCheapHints(@Param("threshold") Integer threshold);

    // Запросы для анализа эффективности подсказок
    @Query("select h from LevelHint h where h.usageCount > 0 order by (CAST(h.usageCount AS double) / h.penaltySeconds) desc")
    List<LevelHint> findMostEfficientHints();

    // Запросы для поиска подсказок по квесту
    @Query("select h from LevelHint h where h.level.quest.id = :questId order by h.level.orderIndex asc, h.orderIndex asc")
    List<LevelHint> findByQuestId(@Param("questId") Long questId);

    @Query("select h from LevelHint h where h.level.quest.id = :questId and h.isActive = true order by h.level.orderIndex asc, h.orderIndex asc")
    List<LevelHint> findActiveByQuestId(@Param("questId") Long questId);

    // Запросы для комплексного анализа
    @Query("select h.hintType, count(h), avg(h.usageCount), avg(h.penaltySeconds) from LevelHint h group by h.hintType")
    List<Object[]> getHintTypeStatistics();

    @Query("select h.difficultyLevel, count(h), avg(h.usageCount), avg(h.penaltySeconds) from LevelHint h group by h.difficultyLevel")
    List<Object[]> getHintDifficultyStatistics();

    // Запросы для поиска подсказок с определенными фильтрами
    @Query("""
        select h from LevelHint h 
        where (:level is null or h.level = :level)
          and (:isActive is null or h.isActive = :isActive)
          and (:hintType is null or h.hintType = :hintType)
          and (:difficultyLevel is null or h.difficultyLevel = :difficultyLevel)
          and (:maxOffset is null or h.offsetSec <= :maxOffset)
          and (:maxPenalty is null or h.penaltySeconds <= :maxPenalty)
        order by h.level.orderIndex asc, h.orderIndex asc
    """)
    List<LevelHint> findHintsWithFilters(
        @Param("level") Level level,
        @Param("isActive") Boolean isActive,
        @Param("hintType") String hintType,
        @Param("difficultyLevel") String difficultyLevel,
        @Param("maxOffset") Integer maxOffset,
        @Param("maxPenalty") Integer maxPenalty
    );

    // Запросы для поиска подсказок, которые были использованы
    @Query("select h from LevelHint h where h.usageCount > 0 order by h.lastUsed desc")
    List<LevelHint> findRecentlyUsedHints();

    // Запросы для анализа распределения подсказок по времени
    @Query("select h.offsetSec, count(h) from LevelHint h group by h.offsetSec order by h.offsetSec asc")
    List<Object[]> getHintDistributionByOffset();

    // Запросы для поиска подсказок с высоким соотношением использования к штрафу
    @Query("select h from LevelHint h where h.penaltySeconds > 0 and h.usageCount > 0 order by (CAST(h.usageCount AS double) / h.penaltySeconds) desc")
    List<LevelHint> findMostCostEffectiveHints();

    // Запросы для поиска подсказок, которые редко используются
    @Query("select h from LevelHint h where h.usageCount > 0 and h.usageCount < :threshold order by h.usageCount asc")
    List<LevelHint> findRarelyUsedHints(@Param("threshold") Integer threshold);
}