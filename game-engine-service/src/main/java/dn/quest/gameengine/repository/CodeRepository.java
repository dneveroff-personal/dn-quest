package dn.quest.gameengine.repository;

import dn.quest.gameengine.entity.Code;
import dn.quest.gameengine.entity.Level;
import dn.quest.shared.enums.CodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с кодами уровней
 */
@Repository
public interface CodeRepository extends JpaRepository<Code, UUID> {

    // Базовые запросы
    @Query("select c from Code c where c.level = :level")
    List<Code> findByLevel(@Param("level") Level level);

    @Query("select c from Code c where c.level = :level and c.type = :type")
    List<Code> findByLevelAndType(@Param("level") Level level, @Param("type") CodeType type);

    @Query("select c from Code c where c.level = :level and c.value = :normalized")
    Optional<Code> findByLevelAndNormalized(@Param("level") Level level, @Param("normalized") String normalized);

    // Запросы для поиска по типу кода
    List<Code> findByType(CodeType type);

    @Query("select c from Code c where c.type = :type order by c.level.orderIndex asc, c.sectorNo asc")
    List<Code> findByTypeOrderByLevelAndSector(@Param("type") CodeType type);

    // Запросы для поиска по секторам
    @Query("select c from Code c where c.sectorNo = :sectorNo order by c.level.orderIndex asc")
    List<Code> findBySectorNo(@Param("sectorNo") Integer sectorNo);

    @Query("select c from Code c where c.level = :level and c.sectorNo = :sectorNo")
    List<Code> findByLevelAndSectorNo(@Param("level") Level level, @Param("sectorNo") Integer sectorNo);

    // Запросы для поиска активных кодов
    @Query("select c from Code c where c.isActive = true order by c.level.orderIndex asc")
    List<Code> findActiveCodes();

    @Query("select c from Code c where c.level = :level and c.isActive = true order by c.sectorNo asc")
    List<Code> findActiveCodesByLevel(@Param("level") Level level);

    // Запросы для поиска кодов с ограничением использования
    @Query("select c from Code c where c.maxUsageCount is not null order by c.maxUsageCount asc")
    List<Code> findCodesWithUsageLimit();

    @Query("select c from Code c where c.maxUsageCount is not null and c.usageCount >= c.maxUsageCount")
    List<Code> findExhaustedCodes();

    // Запросы для поиска кодов, которые можно использовать
    @Query("select c from Code c where c.isActive = true and (c.maxUsageCount is null or c.usageCount < c.maxUsageCount)")
    List<Code> findAvailableCodes();

    @Query("select c from Code c where c.level = :level and c.isActive = true and (c.maxUsageCount is null or c.usageCount < c.maxUsageCount)")
    List<Code> findAvailableCodesByLevel(@Param("level") Level level);

    // Запросы для поиска по значению кода
    @Query("select c from Code c where c.value = :value")
    List<Code> findByValue(@Param("value") String value);

    @Query("select c from Code c where lower(c.value) like lower(concat('%', :value, '%'))")
    List<Code> findByValueContainingIgnoreCase(@Param("value") String value);

    // Запросы для поиска по сдвигу времени
    @Query("select c from Code c where c.shiftSeconds > 0 order by c.shiftSeconds desc")
    List<Code> findBonusCodes();

    @Query("select c from Code c where c.shiftSeconds < 0 order by c.shiftSeconds asc")
    List<Code> findPenaltyCodes();

    @Query("select c from Code c where c.shiftSeconds = 0")
    List<Code> findNeutralCodes();

    // Статистические запросы
    @Query("select count(c) from Code c where c.level = :level")
    long countByLevel(@Param("level") Level level);

    @Query("select count(c) from Code c where c.level = :level and c.type = :type")
    long countByLevelAndType(@Param("level") Level level, @Param("type") CodeType type);

    @Query("select count(c) from Code c where c.type = :type")
    long countByType(@Param("type") CodeType type);

    @Query("select count(c) from Code c where c.isActive = true")
    long countActiveCodes();

    @Query("select count(c) from Code c where c.isActive = false")
    long countInactiveCodes();

    // Запросы для анализа использования
    @Query("select c from Code c where c.usageCount > 0 order by c.usageCount desc")
    List<Code> findMostUsedCodes();

    @Query("select c from Code c where c.usageCount = 0")
    List<Code> findUnusedCodes();

    @Query("select avg(c.usageCount) from Code c where c.usageCount > 0")
    Double getAverageUsageCount();

    // Запросы для поиска кодов с описанием
    @Query("select c from Code c where c.description is not null and c.description != '' order by c.level.orderIndex asc")
    List<Code> findCodesWithDescription();

    @Query("select c from Code c where lower(c.description) like lower(concat('%', :keyword, '%'))")
    List<Code> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);

    // Запросы для анализа по уровням
    @Query("select c.level.id, count(c), avg(c.shiftSeconds) from Code c group by c.level.id")
    List<Object[]> getCodeStatisticsByLevel();

    @Query("select c.level.id, c.type, count(c) from Code c group by c.level.id, c.type")
    List<Object[]> getCodeTypeStatisticsByLevel();

    // Запросы для поиска кодов с определенными характеристиками
    @Query("select c from Code c where c.sectorNo is not null order by c.level.orderIndex asc, c.sectorNo asc")
    List<Code> findSectorCodes();

    @Query("select c from Code c where c.sectorNo is null order by c.level.orderIndex asc")
    List<Code> findNonSectorCodes();

    // Запросы для поиска кодов с бонусами/штрафами
    @Query("select c from Code c where c.shiftSeconds != 0 order by abs(c.shiftSeconds) desc")
    List<Code> findCodesWithTimeShift();

    @Query("select c from Code c where c.shiftSeconds > :threshold order by c.shiftSeconds desc")
    List<Code> findCodesWithBonusAboveThreshold(@Param("threshold") int threshold);

    @Query("select c from Code c where c.shiftSeconds < :threshold order by c.shiftSeconds asc")
    List<Code> findCodesWithPenaltyBelowThreshold(@Param("threshold") int threshold);

    // Запросы для поиска дубликатов
    @Query("select c.value, count(c) from Code c group by c.value having count(c) > 1")
    List<Object[]> findDuplicateCodes();

    @Query("select c.value, count(c) from Code c where c.level.quest.id = :questId group by c.value having count(c) > 1")
    List<Object[]> findDuplicateCodesByQuest(@Param("questId") UUID questId);

    // Запросы для анализа эффективности кодов
    @Query("select c from Code c where c.usageCount > 0 order by (CAST(c.usageCount AS double) / nullif(c.maxUsageCount, 1)) desc")
    List<Code> findMostEfficientCodes();

    // Запросы для поиска кодов по сложности
    @Query("""
        select c from Code c 
        where (:type is null or c.type = :type)
          and (:hasSector is null or (:hasSector = true and c.sectorNo is not null) or (:hasSector = false and c.sectorNo is null))
          and (:minShift is null or c.shiftSeconds >= :minShift)
          and (:maxShift is null or c.shiftSeconds <= :maxShift)
        order by c.level.orderIndex asc, c.sectorNo asc
    """)
    List<Code> findCodesWithFilters(
        @Param("type") CodeType type,
        @Param("hasSector") Boolean hasSector,
        @Param("minShift") Integer minShift,
        @Param("maxShift") Integer maxShift
    );

    // Запросы для поиска кодов, которые скоро закончатся
    @Query("select c from Code c where c.maxUsageCount is not null and c.usageCount >= (c.maxUsageCount * 0.8)")
    List<Code> findCodesNearExhaustion();

    // Запросы для поиска кодов по квесту
    @Query("select c from Code c where c.level.quest.id = :questId order by c.level.orderIndex asc, c.sectorNo asc")
    List<Code> findByQuestId(@Param("questId") UUID questId);

    @Query("select c from Code c where c.level.quest.id = :questId and c.type = :type order by c.level.orderIndex asc")
    List<Code> findByQuestIdAndType(@Param("questId") UUID questId, @Param("type") CodeType type);

    // Запросы для комплексного анализа
    @Query("select c.type, count(c), avg(c.usageCount), avg(c.shiftSeconds) from Code c group by c.type")
    List<Object[]> getCodeTypeStatistics();

    @Query("select c.level.quest.id, count(c), count(case when c.isActive = true then 1 end), avg(c.usageCount) from Code c group by c.level.quest.id")
    List<Object[]> getCodeStatisticsByQuest();
}