package dn.quest.gameengine.repository;

import dn.quest.gameengine.entity.Level;
import dn.quest.gameengine.entity.Quest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с уровнями
 */
@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {

    // Базовые запросы
    List<Level> findByQuest(Quest quest);
    List<Level> findByQuestOrderByOrderIndex(Quest quest);

    // Оптимизированные запросы с индексами
    @Query("select l from Level l where l.quest = :quest order by l.orderIndex asc")
    List<Level> findAllOrdered(@Param("quest") Quest quest);

    @Query("select l from Level l where l.quest = :quest order by l.orderIndex asc")
    List<Level> findFirstInQuest(@Param("quest") Quest quest, Pageable pageable);

    default Level findFirstInQuest(Quest quest) {
        List<Level> list = findFirstInQuest(quest, Pageable.ofSize(1));
        return list.isEmpty() ? null : list.get(0);
    }

    @Query("select l from Level l where l.quest = :quest and l.orderIndex > :orderIndex order by l.orderIndex asc")
    List<Level> findNext(@Param("quest") Quest quest, @Param("orderIndex") Integer orderIndex, Pageable pageable);

    default Level findNext(Quest quest, Integer orderIndex) {
        List<Level> list = findNext(quest, orderIndex, Pageable.ofSize(1));
        return list.isEmpty() ? null : list.get(0);
    }

    @Query("select l from Level l where l.quest = :quest and l.orderIndex < :orderIndex order by l.orderIndex desc")
    List<Level> findPrevious(@Param("quest") Quest quest, @Param("orderIndex") Integer orderIndex, Pageable pageable);

    default Level findPrevious(Quest quest, Integer orderIndex) {
        List<Level> list = findPrevious(quest, orderIndex, Pageable.ofSize(1));
        return list.isEmpty() ? null : list.get(0);
    }

    // Запросы для поиска по порядковому номеру
    Optional<Level> findByQuestAndOrderIndex(Quest quest, Integer orderIndex);

    @Query("select l from Level l where l.quest = :quest and l.orderIndex = :orderIndex")
    Optional<Level> findByQuestAndOrderIndex(@Param("quest") Quest quest, @Param("orderIndex") Integer orderIndex);

    // Запросы для поиска уровней с ограничением по времени
    @Query("select l from Level l where l.apTime is not null and l.apTime > 0 order by l.apTime asc")
    List<Level> findLevelsWithTimeLimit();

    @Query("select l from Level l where l.quest = :quest and l.apTime is not null and l.apTime > 0 order by l.orderIndex asc")
    List<Level> findLevelsWithTimeLimitByQuest(@Param("quest") Quest quest);

    // Запросы для поиска уровней с требованием к секторам
    @Query("select l from Level l where l.requiredSectors > 0 order by l.requiredSectors desc")
    List<Level> findLevelsWithSectorRequirement();

    @Query("select l from Level l where l.quest = :quest and l.requiredSectors > 0 order by l.orderIndex asc")
    List<Level> findLevelsWithSectorRequirementByQuest(@Param("quest") Quest quest);

    // Запросы для поиска уровней с ограничением на попытки
    @Query("select l from Level l where l.maxAttemptsPerMinute is not null and l.maxAttemptsPerMinute > 0 order by l.maxAttemptsPerMinute asc")
    List<Level> findLevelsWithAttemptLimit();

    // Запросы для поиска уровней с подсказками
    @Query("select l from Level l where l.maxHints is not null and l.maxHints > 0 order by l.maxHints desc")
    List<Level> findLevelsWithHints();

    @Query("select l from Level l where size(l.hints) > 0 order by size(l.hints) desc")
    List<Level> findLevelsWithExistingHints();

    // Запросы для поиска уровней с кодами
    @Query("select l from Level l where size(l.codes) > 0 order by size(l.codes) desc")
    List<Level> findLevelsWithCodes();

    @Query("select l from Level l where l.quest = :quest and size(l.codes) > 0 order by l.orderIndex asc")
    List<Level> findLevelsWithCodesByQuest(@Param("quest") Quest quest);

    // Статистические запросы
    @Query("select count(l) from Level l where l.quest = :quest")
    long countByQuest(@Param("quest") Quest quest);

    @Query("select avg(l.apTime) from Level l where l.quest = :quest and l.apTime is not null")
    Double getAverageApTimeByQuest(@Param("quest") Quest quest);

    @Query("select sum(l.requiredSectors) from Level l where l.quest = :quest")
    Integer getTotalRequiredSectorsByQuest(@Param("quest") Quest quest);

    @Query("select avg(l.maxAttemptsPerMinute) from Level l where l.quest = :quest and l.maxAttemptsPerMinute is not null")
    Double getAverageMaxAttemptsByQuest(@Param("quest") Quest quest);

    // Запросы для анализа сложности
    @Query("select l from Level l where l.requiredSectors >= :threshold order by l.requiredSectors desc")
    List<Level> findLevelsByMinRequiredSectors(@Param("threshold") int threshold);

    @Query("select l from Level l where l.apTime <= :threshold order by l.apTime asc")
    List<Level> findLevelsByMaxApTime(@Param("threshold") int threshold);

    // Запросы для поиска по названию
    @Query("select l from Level l where lower(l.title) like lower(concat('%', :title, '%')) order by l.title asc")
    List<Level> findByTitleContainingIgnoreCase(@Param("title") String title);

    @Query("select l from Level l where l.quest = :quest and lower(l.title) like lower(concat('%', :title, '%')) order by l.orderIndex asc")
    List<Level> findByQuestAndTitleContainingIgnoreCase(@Param("quest") Quest quest, @Param("title") String title);

    // Запросы для поиска уровней с определенными характеристиками
    @Query("select l from Level l where l.apTime is not null and l.requiredSectors > 0 order by l.orderIndex asc")
    List<Level> findLevelsWithTimeAndSectorRequirements();

    @Query("select l from Level l where l.quest = :quest and l.apTime is not null and l.requiredSectors > 0 order by l.orderIndex asc")
    List<Level> findLevelsWithTimeAndSectorRequirementsByQuest(@Param("quest") Quest quest);

    // Запросы для пагинации
    Page<Level> findByQuestOrderByOrderIndex(Quest quest, Pageable pageable);

    // Запросы для поиска самых сложных уровней
    @Query("select l from Level l order by (l.requiredSectors * 2 + case when l.apTime is not null then 1 else 0 end) desc")
    List<Level> findMostComplexLevels();

    @Query("select l from Level l where l.quest = :quest order by (l.requiredSectors * 2 + case when l.apTime is not null then 1 else 0 end) desc")
    List<Level> findMostComplexLevelsByQuest(@Param("quest") Quest quest);

    // Запросы для поиска самых простых уровней
    @Query("select l from Level l order by (l.requiredSectors * 2 + case when l.apTime is not null then 1 else 0 end) asc")
    List<Level> findEasiestLevels();

    @Query("select l from Level l where l.quest = :quest order by (l.requiredSectors * 2 + case when l.apTime is not null then 1 else 0 end) asc")
    List<Level> findEasiestLevelsByQuest(@Param("quest") Quest quest);

    // Запросы для анализа по количеству кодов
    @Query("select l, size(l.codes) as codeCount from Level l order by codeCount desc")
    List<Object[]> findLevelsByCodeCount();

    @Query("select l, size(l.codes) as codeCount from Level l where l.quest = :quest order by codeCount desc")
    List<Object[]> findLevelsByCodeCountByQuest(@Param("quest") Quest quest);

    // Запросы для анализа по количеству подсказок
    @Query("select l, size(l.hints) as hintCount from Level l order by hintCount desc")
    List<Object[]> findLevelsByHintCount();

    @Query("select l, size(l.hints) as hintCount from Level l where l.quest = :quest order by hintCount desc")
    List<Object[]> findLevelsByHintCountByQuest(@Param("quest") Quest quest);

    // Запросы для поиска уровней с определенными типами кодов
    @Query("select distinct l from Level l join l.codes c where c.type = :codeType order by l.orderIndex asc")
    List<Level> findLevelsWithCodeType(@Param("codeType") String codeType);

    @Query("select distinct l from Level l join l.codes c where l.quest = :quest and c.type = :codeType order by l.orderIndex asc")
    List<Level> findLevelsWithCodeTypeByQuest(@Param("quest") Quest quest, @Param("codeType") String codeType);

    // Запросы для поиска уровней по сложности
    @Query("""
        select l from Level l 
        where (:minRequiredSectors is null or l.requiredSectors >= :minRequiredSectors)
          and (:maxRequiredSectors is null or l.requiredSectors <= :maxRequiredSectors)
          and (:minApTime is null or l.apTime >= :minApTime)
          and (:maxApTime is null or l.apTime <= :maxApTime)
        order by l.orderIndex asc
    """)
    List<Level> findLevelsWithComplexityFilters(
        @Param("minRequiredSectors") Integer minRequiredSectors,
        @Param("maxRequiredSectors") Integer maxRequiredSectors,
        @Param("minApTime") Integer minApTime,
        @Param("maxApTime") Integer maxApTime
    );

    // Запросы для поиска уровней, которые были недавно обновлены
    @Query("select l from Level l where l.updatedAt >= :since order by l.updatedAt desc")
    List<Level> findRecentlyUpdated(@Param("since") java.time.LocalDateTime since);

    // Запросы для поиска завершенных уровней
    @Query("select l from Level l where l.completedAt is not null order by l.completedAt desc")
    List<Level> findCompletedLevels();

    @Query("select l from Level l where l.quest = :quest and l.completedAt is not null order by l.orderIndex asc")
    List<Level> findCompletedLevelsByQuest(@Param("quest") Quest quest);
}