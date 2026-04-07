package dn.quest.statistics.repository;

import dn.quest.statistics.entity.QuestStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы со статистикой квестов
 */
@Repository
public interface QuestStatisticsRepository extends JpaRepository<QuestStatistics, Long> {

    /**
     * Найти статистику квеста по ID и дате
     */
    Optional<QuestStatistics> findByQuestIdAndDate(UUID questId, LocalDate date);

    /**
     * Найти всю статистику квеста по ID
     */
    List<QuestStatistics> findByQuestIdOrderByDateDesc(UUID questId);

    /**
     * Найти статистику квеста за период
     */
    List<QuestStatistics> findByQuestIdAndDateBetweenOrderByDateDesc(UUID questId, LocalDate startDate, LocalDate endDate);

    /**
     * Найти статистику квеста за период с пагинацией
     */
    Page<QuestStatistics> findByQuestIdAndDateBetweenOrderByDateDesc(UUID questId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Найти статистику по автору
     */
    List<QuestStatistics> findByAuthorIdOrderByDateDesc(UUID authorId);

    /**
     * Найти статистику по автору за период
     */
    List<QuestStatistics> findByAuthorIdAndDateBetweenOrderByDateDesc(UUID authorId, LocalDate startDate, LocalDate endDate);

    /**
     * Получить количество созданных квестов за дату
     */
    @Query("SELECT COALESCE(SUM(q.creations), 0) FROM QuestStatistics q WHERE q.date = :date")
    Long countCreatedQuestsByDate(@Param("date") LocalDate date);

    /**
     * Получить количество опубликованных квестов за дату
     */
    @Query("SELECT COALESCE(SUM(q.publications), 0) FROM QuestStatistics q WHERE q.date = :date")
    Long countPublishedQuestsByDate(@Param("date") LocalDate date);

    /**
     * Получить количество завершенных квестов за дату
     */
    @Query("SELECT COALESCE(SUM(q.completions), 0) FROM QuestStatistics q WHERE q.date = :date")
    Long countCompletedQuestsByDate(@Param("date") LocalDate date);

    /**
     * Получить топ квестов по количеству стартов
     */
    @Query("SELECT q FROM QuestStatistics q WHERE q.date = :date ORDER BY q.starts DESC")
    List<QuestStatistics> findTopQuestsByStarts(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить топ квестов по количеству завершений
     */
    @Query("SELECT q FROM QuestStatistics q WHERE q.date = :date ORDER BY q.completions DESC")
    List<QuestStatistics> findTopQuestsByCompletions(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить топ квестов по рейтингу
     */
    @Query("SELECT q FROM QuestStatistics q WHERE q.date = :date AND q.currentRating IS NOT NULL ORDER BY q.currentRating DESC")
    List<QuestStatistics> findTopQuestsByRating(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить топ квестов по коэффициенту завершения
     */
    @Query("SELECT q FROM QuestStatistics q WHERE q.date = :date AND q.completionRate IS NOT NULL ORDER BY q.completionRate DESC")
    List<QuestStatistics> findTopQuestsByCompletionRate(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить статистику по авторам за период
     */
    @Query("SELECT " +
           "q.authorId, " +
           "COUNT(DISTINCT q.questId) as totalQuests, " +
           "SUM(q.creations) as creations, " +
           "SUM(q.publications) as publications, " +
           "SUM(q.views) as totalViews, " +
           "SUM(q.starts) as totalStarts, " +
           "SUM(q.completions) as totalCompletions " +
           "FROM QuestStatistics q " +
           "WHERE q.date BETWEEN :startDate AND :endDate AND q.authorId IS NOT NULL " +
           "GROUP BY q.authorId")
    List<Object[]> getAuthorPeriodStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить средний коэффициент завершения квестов
     */
    @Query("SELECT AVG(q.completionRate) FROM QuestStatistics q WHERE q.date = :date AND q.completionRate IS NOT NULL")
    Double getAvgCompletionRateByDate(@Param("date") LocalDate date);

    /**
     * Получить среднее время прохождения квестов
     */
    @Query("SELECT AVG(q.avgCompletionTimeMinutes) FROM QuestStatistics q WHERE q.date = :date AND q.avgCompletionTimeMinutes IS NOT NULL")
    Double getAvgCompletionTimeByDate(@Param("date") LocalDate date);

    /**
     * Получить статистику по категориям квестов
     */
    @Query("SELECT " +
           "q.category, " +
           "COUNT(DISTINCT q.questId) as totalQuests, " +
           "SUM(q.starts) as totalStarts, " +
           "SUM(q.completions) as totalCompletions, " +
           "AVG(q.completionRate) as avgCompletionRate " +
           "FROM QuestStatistics q " +
           "WHERE q.date = :date AND q.category IS NOT NULL " +
           "GROUP BY q.category")
    List<Object[]> getCategoryStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить статистику по сложности квестов
     */
    @Query("SELECT " +
           "q.difficultyLevel, " +
           "COUNT(DISTINCT q.questId) as totalQuests, " +
           "SUM(q.starts) as totalStarts, " +
           "SUM(q.completions) as totalCompletions, " +
           "AVG(q.completionRate) as avgCompletionRate " +
           "FROM QuestStatistics q " +
           "WHERE q.date = :date AND q.difficultyLevel IS NOT NULL " +
           "GROUP BY q.difficultyLevel " +
           "ORDER BY q.difficultyLevel")
    List<Object[]> getDifficultyStatsByDate(@Param("date") LocalDate date);

    /**
     * Увеличить количество просмотров квеста
     */
    @Modifying
    @Query("UPDATE QuestStatistics q SET q.views = q.views + 1 WHERE q.questId = :questId AND q.date = :date")
    int incrementViews(@Param("questId") UUID questId, @Param("date") LocalDate date);

    /**
     * Увеличить количество уникальных просмотров квеста
     */
    @Modifying
    @Query("UPDATE QuestStatistics q SET q.uniqueViews = q.uniqueViews + 1 WHERE q.questId = :questId AND q.date = :date")
    int incrementUniqueViews(@Param("questId") UUID questId, @Param("date") LocalDate date);

    /**
     * Увеличить количество стартов квеста
     */
    @Modifying
    @Query("UPDATE QuestStatistics q SET q.starts = q.starts + 1 WHERE q.questId = :questId AND q.date = :date")
    int incrementStarts(@Param("questId") UUID questId, @Param("date") LocalDate date);

    /**
     * Увеличить количество завершений квеста
     */
    @Modifying
    @Query("UPDATE QuestStatistics q SET q.completions = q.completions + 1 WHERE q.questId = :questId AND q.date = :date")
    int incrementCompletions(@Param("questId") UUID questId, @Param("date") LocalDate date);

    /**
     * Обновить рейтинг квеста
     */
    @Modifying
    @Query("UPDATE QuestStatistics q SET q.currentRating = :rating, q.ratingCount = q.ratingCount + 1, q.avgRating = :avgRating WHERE q.questId = :questId AND q.date = :date")
    int updateQuestRating(@Param("questId") UUID questId, @Param("date") LocalDate date, @Param("rating") Double rating, @Param("avgRating") Double avgRating);

    /**
     * Добавить время прохождения квеста
     */
    @Modifying
    @Query("UPDATE QuestStatistics q SET q.totalGameTimeMinutes = q.totalGameTimeMinutes + :minutes WHERE q.questId = :questId AND q.date = :date")
    int addGameTime(@Param("questId") UUID questId, @Param("date") LocalDate date, @Param("minutes") Long minutes);

    /**
     * Получить количество квестов с рейтингом
     */
    @Query("SELECT COUNT(DISTINCT q.questId) FROM QuestStatistics q WHERE q.date = :date AND q.currentRating IS NOT NULL")
    Long countQuestsWithRating(@Param("date") LocalDate date);

    /**
     * Получить популярные теги квестов
     */
    @Query("SELECT q.tags FROM QuestStatistics q WHERE q.date = :date AND q.tags IS NOT NULL")
    List<String> findPopularTags(@Param("date") LocalDate date);

    /**
     * Получить статистику по статусам квестов
     */
    @Query("SELECT " +
           "q.status, " +
           "COUNT(DISTINCT q.questId) as totalQuests, " +
           "SUM(q.starts) as totalStarts, " +
           "SUM(q.completions) as totalCompletions " +
           "FROM QuestStatistics q " +
           "WHERE q.date = :date AND q.status IS NOT NULL " +
           "GROUP BY q.status")
    List<Object[]> getStatusStatsByDate(@Param("date") LocalDate date);

    /**
     * Удалить старую статистику (для очистки)
     */
    @Modifying
    @Query("DELETE FROM QuestStatistics q WHERE q.date < :date")
    int deleteStatisticsOlderThan(@Param("date") LocalDate date);

    /**
     * Найти статистику за дату
     */
    List<QuestStatistics> findByDate(LocalDate date);

    /**
     * Найти статистику до указанной даты
     */
    List<QuestStatistics> findByDateBefore(LocalDate date);

    /**
     * Найти статистику за период
     */
    List<QuestStatistics> findByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Получить количество уникальных квестов за период
     */
    @Query("SELECT COUNT(DISTINCT q.questId) FROM QuestStatistics q WHERE q.date BETWEEN :startDate AND :endDate")
    Long countDistinctQuestsByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить топ квестов по количеству просмотров
     */
    @Query("SELECT q FROM QuestStatistics q WHERE q.date = :date ORDER BY q.views DESC")
    List<QuestStatistics> findTopQuestsByViews(@Param("date") LocalDate date, Pageable pageable);
}