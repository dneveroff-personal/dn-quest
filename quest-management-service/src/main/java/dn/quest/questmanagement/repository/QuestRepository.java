package dn.quest.questmanagement.repository;

import dn.quest.questmanagement.entity.Quest;
import dn.quest.questmanagement.entity.QuestStatus;
import dn.quest.shared.enums.Difficulty;
import dn.quest.shared.enums.QuestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repository для работы с квестами
 */
@Repository
public interface QuestRepository extends JpaRepository<Quest, Long>, JpaSpecificationExecutor<Quest> {

    /**
     * Найти квест по номеру
     */
    Optional<Quest> findByNumber(Long number);

    /**
     * Найти активные квесты
     */
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.archived = false " +
           "AND (q.startAt IS NULL OR q.startAt <= :now) " +
           "AND (q.endAt IS NULL OR q.endAt >= :now) " +
           "ORDER BY q.startAt ASC")
    List<Quest> findActiveQuests(@Param("now") Instant now);

    /**
     * Найти опубликованные квесты с пагинацией
     */
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.archived = false " +
           "ORDER BY q.createdAt DESC")
    Page<Quest> findPublishedQuests(Pageable pageable);

    /**
     * Найти квесты по статусу
     */
    List<Quest> findByStatus(QuestStatus status);

    /**
     * Найти квесты по статусу с пагинацией
     */
    Page<Quest> findByStatus(QuestStatus status, Pageable pageable);

    /**
     * Найти квесты по автору
     */
    @Query("SELECT q FROM Quest q WHERE :authorId MEMBER OF q.authorIds")
    List<Quest> findByAuthorId(@Param("authorId") UUID authorId);

    /**
     * Найти квесты по автору с пагинацией
     */
    @Query("SELECT q FROM Quest q WHERE :authorId MEMBER OF q.authorIds " +
           "ORDER BY q.createdAt DESC")
    Page<Quest> findByAuthorId(@Param("authorId") UUID authorId, Pageable pageable);

    /**
     * Найти квесты по сложности
     */
    List<Quest> findByDifficulty(Difficulty difficulty);

    /**
     * Найти квесты по типу
     */
    List<Quest> findByQuestType(QuestType questType);

    /**
     * Найти квесты по категории
     */
    List<Quest> findByCategory(String category);

    /**
     * Найти квесты по тегу
     */
    @Query("SELECT q FROM Quest q WHERE :tag MEMBER OF q.tags")
    List<Quest> findByTag(@Param("tag") String tag);

    /**
     * Найти квесты по нескольким тегам
     */
    @Query("SELECT q FROM Quest q WHERE FUNCTION('array_overlap', q.tags, :tags) = true")
    List<Quest> findByTags(@Param("tags") String[] tags);

    /**
     * Поиск квестов по названию (без учета регистра)
     */
    @Query("SELECT q FROM Quest q WHERE LOWER(q.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "ORDER BY q.title ASC")
    List<Quest> findByTitleContainingIgnoreCase(@Param("title") String title);

    /**
     * Поиск квестов по названию с пагинацией
     */
    @Query("SELECT q FROM Quest q WHERE LOWER(q.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "ORDER BY q.title ASC")
    Page<Quest> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    /**
     * Найти квесты созданные пользователем
     */
    List<Quest> findByCreatedBy(Long createdBy);

    /**
     * Найти квесты созданные пользователем с пагинацией
     */
    Page<Quest> findByCreatedBy(Long createdBy, Pageable pageable);

    /**
     * Найти шаблоны квестов
     */
    @Query("SELECT q FROM Quest q WHERE q.isTemplate = true AND q.archived = false " +
           "ORDER BY q.title ASC")
    List<Quest> findTemplates();

    /**
     * Найти квесты по родительскому квесту
     */
    List<Quest> findByParentQuestId(Long parentQuestId);

    /**
     * Найти квесты, которые можно копировать (опубликованные и не архивные)
     */
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.archived = false " +
           "AND q.isTemplate = false " +
           "ORDER BY q.title ASC")
    List<Quest> findCopyableQuests();

    /**
     * Подсчет квестов по статусу
     */
    long countByStatus(QuestStatus status);

    /**
     * Подсчет опубликованных квестов
     */
    @Query("SELECT COUNT(q) FROM Quest q WHERE q.published = true AND q.archived = false")
    long countPublishedQuests();

    /**
     * Подсчет активных квестов
     */
    @Query("SELECT COUNT(q) FROM Quest q WHERE q.published = true AND q.archived = false " +
           "AND (q.startAt IS NULL OR q.startAt <= :now) " +
           "AND (q.endAt IS NULL OR q.endAt >= :now)")
    long countActiveQuests(@Param("now") Instant now);

    /**
     * Подсчет квестов автора
     */
    @Query("SELECT COUNT(q) FROM Quest q WHERE :authorId MEMBER OF q.authorIds")
    long countByAuthorId(@Param("authorId") UUID authorId);

    /**
     * Проверить существование квеста с таким номером
     */
    boolean existsByNumber(Long number);

    /**
     * Проверить существование квеста с таким названием
     */
    boolean existsByTitleIgnoreCase(String title);

    /**
     * Найти максимальный номер квеста
     */
    @Query("SELECT COALESCE(MAX(q.number), 0) FROM Quest q")
    Long findMaxNumber();

    /**
     * Обновить статус квестов
     */
    @Modifying
    @Query("UPDATE Quest q SET q.status = :status, q.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE q.id IN :questIds")
    int updateStatus(@Param("questIds") List<Long> questIds, @Param("status") QuestStatus status);

    /**
     * Архивировать квесты
     */
    @Modifying
    @Query("UPDATE Quest q SET q.archived = true, q.archivedAt = CURRENT_TIMESTAMP, " +
           "q.archiveReason = :reason, q.published = false, q.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE q.id IN :questIds")
    int archiveQuests(@Param("questIds") List<Long> questIds, @Param("reason") String reason);

    /**
     * Найти квесты для публикации (черновики, готовые к публикации)
     */
    @Query("SELECT q FROM Quest q WHERE q.status = :status AND q.archived = false " +
           "ORDER BY q.createdAt ASC")
    List<Quest> findQuestsForPublishing(@Param("status") QuestStatus status);

    /**
     * Найти просроченные квесты
     */
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.archived = false " +
           "AND q.endAt IS NOT NULL AND q.endAt < :now " +
           "ORDER BY q.endAt ASC")
    List<Quest> findExpiredQuests(@Param("now") Instant now);

    /**
     * Найти квесты, которые скоро начнутся
     */
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.archived = false " +
           "AND q.startAt IS NOT NULL AND q.startAt > :now AND q.startAt <= :future " +
           "ORDER BY q.startAt ASC")
    List<Quest> findUpcomingQuests(@Param("now") Instant now, @Param("future") Instant future);

    /**
     * Найти популярные квесты (по количеству авторов или другим критериям)
     */
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.archived = false " +
           "ORDER BY SIZE(q.authorIds) DESC, q.createdAt DESC")
    List<Quest> findPopularQuests(Pageable pageable);

    /**
     * Найти последние квесты
     */
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.archived = false " +
           "ORDER BY q.createdAt DESC")
    List<Quest> findLatestQuests(Pageable pageable);

    /**
     * Найти квесты по сложности и типу
     */
    @Query("SELECT q FROM Quest q WHERE q.difficulty = :difficulty AND q.questType = :questType " +
           "AND q.published = true AND q.archived = false " +
           "ORDER BY q.title ASC")
    List<Quest> findByDifficultyAndType(@Param("difficulty") Difficulty difficulty, 
                                       @Param("questType") QuestType questType);

    /**
     * Найти квесты в диапазоне дат
     */
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.archived = false " +
           "AND ((q.startAt IS NULL OR q.startAt >= :startDate) " +
           "AND (q.endAt IS NULL OR q.endAt <= :endDate)) " +
           "ORDER BY q.startAt ASC")
    List<Quest> findByDateRange(@Param("startDate") Instant startDate, 
                                @Param("endDate") Instant endDate);

    /**
     * Получить статистику по квестам
     */
    @Query("SELECT q.difficulty, COUNT(q) FROM Quest q WHERE q.published = true AND q.archived = false " +
           "GROUP BY q.difficulty")
    List<Object[]> getQuestStatisticsByDifficulty();

    /**
     * Получить статистику по квестам по типам
     */
    @Query("SELECT q.questType, COUNT(q) FROM Quest q WHERE q.published = true AND q.archived = false " +
           "GROUP BY q.questType")
    List<Object[]> getQuestStatisticsByType();

    /**
     * Обновить средний рейтинг квеста
     */
    @Modifying
    @Query("UPDATE Quest q SET q.averageRating = :averageRating WHERE q.id = :questId")
    void updateAverageRating(@Param("questId") Long questId, @Param("averageRating") Double averageRating);

    /**
     * Найти квесты с самым высоким рейтингом
     */
    @Query("SELECT q FROM Quest q WHERE q.published = true AND q.archived = false ORDER BY q.averageRating DESC")
    Page<Quest> findTopRatedQuests(Pageable pageable);
}