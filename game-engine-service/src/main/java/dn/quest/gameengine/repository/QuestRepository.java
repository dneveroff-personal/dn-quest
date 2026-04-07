package dn.quest.gameengine.repository;

import dn.quest.gameengine.entity.Quest;
import dn.quest.shared.enums.Difficulty;
import dn.quest.shared.enums.QuestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с квестами
 */
@Repository
public interface QuestRepository extends JpaRepository<Quest, UUID> {

    // Оптимизированные запросы с индексами
    List<Quest> findByEndAtIsNullOrderByStartAtAsc();
    
    @Query("SELECT q FROM Quest q WHERE q.endAt IS NULL ORDER BY q.startAt ASC")
    List<Quest> findActiveQuests();
    
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.endAt IS NULL ORDER BY q.startAt ASC")
    List<Quest> findPublishedActiveQuests();
    
    @Query("""
        SELECT q FROM Quest q
        WHERE q.published = true
          AND q.startAt <= :now
          AND q.endAt IS NULL
        ORDER BY q.startAt ASC
    """)
    List<Quest> findAvailableQuests(@Param("now") Instant now);
    
    // Запросы с пагинацией для больших объемов данных
    @Query("SELECT q FROM Quest q WHERE q.published = true ORDER BY q.startAt ASC")
    Page<Quest> findPublishedQuests(Pageable pageable);
    
    @Query("SELECT q FROM Quest q WHERE q.endAt IS NULL ORDER BY q.startAt ASC")
    Page<Quest> findActiveQuestsPaged(Pageable pageable);
    
    // Запросы с JOIN FETCH для избежания N+1 проблемы
    @Query("SELECT q FROM Quest q LEFT JOIN FETCH q.authors WHERE q.id = :id")
    Optional<Quest> findByIdWithAuthors(@Param("id") UUID id);
    
    @Query("SELECT q FROM Quest q LEFT JOIN FETCH q.authors LEFT JOIN FETCH q.levels WHERE q.id = :id")
    Optional<Quest> findByIdWithAuthorsAndLevels(@Param("id") UUID id);
    
    // Поиск по типу и сложности
    List<Quest> findByType(QuestType type);
    List<Quest> findByDifficulty(Difficulty difficulty);
    List<Quest> findByTypeAndDifficulty(QuestType type, Difficulty difficulty);
    
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.type = :type ORDER BY q.startAt ASC")
    List<Quest> findPublishedByType(@Param("type") QuestType type);
    
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.difficulty = :difficulty ORDER BY q.startAt ASC")
    List<Quest> findPublishedByDifficulty(@Param("difficulty") Difficulty difficulty);
    
    // Поиск по названию
    @Query("SELECT q FROM Quest q WHERE q.published = true AND LOWER(q.name) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY q.startAt ASC")
    List<Quest> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    // Подсчет количества квестов
    @Query("SELECT COUNT(q) FROM Quest q WHERE q.published = true AND q.endAt IS NULL")
    long countPublishedActiveQuests();
    
    @Query("SELECT COUNT(q) FROM Quest q WHERE q.type = :type AND q.published = true")
    long countPublishedByType(@Param("type") QuestType type);
    
    @Query("SELECT COUNT(q) FROM Quest q WHERE q.difficulty = :difficulty AND q.published = true")
    long countPublishedByDifficulty(@Param("difficulty") Difficulty difficulty);
    
    // Запросы по времени
    @Query("SELECT q FROM Quest q WHERE q.startAt >= :since ORDER BY q.startAt ASC")
    List<Quest> findByStartAfter(@Param("since") Instant since);
    
    @Query("SELECT q FROM Quest q WHERE q.startAt <= :before ORDER BY q.startAt DESC")
    List<Quest> findByStartBefore(@Param("before") Instant before);
    
    @Query("SELECT q FROM Quest q WHERE q.startAt BETWEEN :start AND :end ORDER BY q.startAt ASC")
    List<Quest> findByStartBetween(@Param("start") Instant start, @Param("end") Instant end);
    
    // Запросы для статистики
    @Query("SELECT q.type, COUNT(q) FROM Quest q WHERE q.published = true GROUP BY q.type")
    List<Object[]> getQuestTypeStatistics();
    
    @Query("SELECT q.difficulty, COUNT(q) FROM Quest q WHERE q.published = true GROUP BY q.difficulty")
    List<Object[]> getQuestDifficultyStatistics();
    
    // Запросы для поиска квестов автора
    @Query("SELECT q FROM Quest q JOIN q.authors a WHERE a.id = :authorId ORDER BY q.createdAt DESC")
    List<Quest> findByAuthorId(@Param("authorId") UUID authorId);
    
    @Query("SELECT q FROM Quest q JOIN q.authors a WHERE a.id = :authorId AND q.published = true ORDER BY q.startAt ASC")
    List<Quest> findPublishedByAuthorId(@Param("authorId") UUID authorId);
    
    // Запросы для поиска по номеру квеста
    Optional<Quest> findByNumber(Long number);
    
    @Query("SELECT MAX(q.number) FROM Quest q")
    Long findMaxNumber();
    
    // Запросы для поиска квестов с ограничениями по участникам
    @Query("SELECT q FROM Quest q WHERE q.maxParticipants IS NOT NULL ORDER BY q.maxParticipants ASC")
    List<Quest> findQuestsWithParticipantLimit();
    
    @Query("SELECT q FROM Quest q WHERE q.maxParticipants IS NULL OR q.maxParticipants > :minParticipants")
    List<Quest> findQuestsWithMinParticipantLimit(@Param("minParticipants") Integer minParticipants);
    
    // Запросы для поиска квестов с ограничениями по командам
    @Query("SELECT q FROM Quest q WHERE q.type = 'TEAM' AND q.minTeamSize IS NOT NULL ORDER BY q.minTeamSize ASC")
    List<Quest> findTeamQuestsWithMinSize();
    
    @Query("SELECT q FROM Quest q WHERE q.type = 'TEAM' AND q.maxTeamSize IS NOT NULL ORDER BY q.maxTeamSize ASC")
    List<Quest> findTeamQuestsWithMaxSize();
    
    // Запросы для поиска по оценочной длительности
    @Query("SELECT q FROM Quest q WHERE q.estimatedDurationMinutes BETWEEN :min AND :max ORDER BY q.estimatedDurationMinutes ASC")
    List<Quest> findByEstimatedDurationBetween(@Param("min") Integer min, @Param("max") Integer max);
    
    // Запросы для поиска недавно созданных квестов
    @Query("SELECT q FROM Quest q WHERE q.createdAt >= :since ORDER BY q.createdAt DESC")
    List<Quest> findRecentlyCreated(@Param("since") Instant since);
    
    @Query("SELECT q FROM Quest q WHERE q.updatedAt >= :since ORDER BY q.updatedAt DESC")
    List<Quest> findRecentlyUpdated(@Param("since") Instant since);
    
    // Запросы для поиска неопубликованных квестов
    @Query("SELECT q FROM Quest q WHERE q.published = false ORDER BY q.createdAt DESC")
    List<Quest> findUnpublishedQuests();
    
    // Запросы для поиска завершенных квестов
    @Query("SELECT q FROM Quest q WHERE q.endAt IS NOT NULL AND q.endAt <= :now ORDER BY q.endAt DESC")
    List<Quest> findFinishedQuests(@Param("now") Instant now);
    
    // Запросы для поиска квестов, которые скоро начнутся
    @Query("SELECT q FROM Quest q WHERE q.startAt > :now AND q.startAt <= :soon ORDER BY q.startAt ASC")
    List<Quest> findQuestsStartingSoon(@Param("now") Instant now, @Param("soon") Instant soon);
    
    // Запросы для поиска квестов, которые скоро закончатся
    @Query("SELECT q FROM Quest q WHERE q.endAt > :now AND q.endAt <= :soon ORDER BY q.endAt ASC")
    List<Quest> findQuestsEndingSoon(@Param("now") Instant now, @Param("soon") Instant soon);
    
    // Запросы для комплексного поиска
    @Query("""
        SELECT q FROM Quest q 
        WHERE (:published IS NULL OR q.published = :published)
          AND (:type IS NULL OR q.type = :type)
          AND (:difficulty IS NULL OR q.difficulty = :difficulty)
          AND (:title IS NULL OR LOWER(q.name) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:hasTimeLimit IS NULL OR (:hasTimeLimit = true AND q.estimatedDurationMinutes IS NOT NULL) OR (:hasTimeLimit = false AND q.estimatedDurationMinutes IS NULL))
        ORDER BY q.startAt ASC
    """)
    List<Quest> findQuestsWithFilters(
        @Param("published") Boolean published,
        @Param("type") QuestType type,
        @Param("difficulty") Difficulty difficulty,
        @Param("title") String title,
        @Param("hasTimeLimit") Boolean hasTimeLimit
    );
}