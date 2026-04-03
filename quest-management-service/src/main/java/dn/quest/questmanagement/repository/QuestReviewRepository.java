package dn.quest.questmanagement.repository;

import dn.quest.questmanagement.entity.QuestReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository для работы с отзывами на квесты
 */
@Repository
public interface QuestReviewRepository extends JpaRepository<QuestReview, Long> {

    /**
     * Найти отзыв квеста от пользователя
     */
    Optional<QuestReview> findByQuestIdAndUserId(Long questId, UUID userId);

    /**
     * Найти все видимые отзывы квеста с пагинацией
     */
    Page<QuestReview> findByQuestIdAndIsVisibleTrue(Long questId, Pageable pageable);

    /**
     * Найти все отзывы квеста (включая скрытые)
     */
    Page<QuestReview> findByQuestId(Long questId, Pageable pageable);

    /**
     * Найти все отзывы пользователя
     */
    List<QuestReview> findByUserId(UUID userId);

    /**
     * Найти все видимые отзывы пользователя
     */
    List<QuestReview> findByUserIdAndIsVisibleTrue(UUID userId);

    /**
     * Удалить отзыв квеста от пользователя
     */
    @Modifying
    @Query("DELETE FROM QuestReview r WHERE r.questId = :questId AND r.userId = :userId")
    void deleteByQuestIdAndUserId(@Param("questId") Long questId, @Param("userId") UUID userId);

    /**
     * Удалить все отзывы квеста
     */
    @Modifying
    @Query("DELETE FROM QuestReview r WHERE r.questId = :questId")
    void deleteByQuestId(@Param("questId") Long questId);

    /**
     * Удалить все отзывы пользователя
     */
    @Modifying
    @Query("DELETE FROM QuestReview r WHERE r.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Подсчитать количество отзывов квеста
     */
    @Query("SELECT COUNT(r) FROM QuestReview r WHERE r.questId = :questId")
    long countByQuestId(@Param("questId") Long questId);

    /**
     * Подсчитать количество видимых отзывов квеста
     */
    @Query("SELECT COUNT(r) FROM QuestReview r WHERE r.questId = :questId AND r.isVisible = true")
    long countVisibleByQuestId(@Param("questId") Long questId);

    /**
     * Подсчитать количество отзывов пользователя
     */
    @Query("SELECT COUNT(r) FROM QuestReview r WHERE r.userId = :userId")
    long countByUserId(@Param("userId") UUID userId);

    /**
     * Найти отзывы с определенным рейтингом для квеста
     */
    Page<QuestReview> findByQuestIdAndRatingAndIsVisibleTrue(Long questId, Integer rating, Pageable pageable);

    /**
     * Найти отзывы с определенным рейтингом
     */
    List<QuestReview> findByQuestIdAndRating(Long questId, Integer rating);

    /**
     * Проверить существование отзыва
     */
    boolean existsByQuestIdAndUserId(Long questId, UUID userId);

    /**
     * Найти отзывы, созданные в определенном периоде
     */
    @Query("SELECT r FROM QuestReview r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    List<QuestReview> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                           @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Найти последние отзывы квеста
     */
    @Query("SELECT r FROM QuestReview r WHERE r.questId = :questId AND r.isVisible = true ORDER BY r.createdAt DESC")
    List<QuestReview> findLatestByQuestId(@Param("questId") Long questId, Pageable pageable);

    /**
     * Найти последние отзывы пользователя
     */
    @Query("SELECT r FROM QuestReview r WHERE r.userId = :userId AND r.isVisible = true ORDER BY r.createdAt DESC")
    List<QuestReview> findLatestByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Найти отзывы по текстовому содержимому
     */
    @Query("SELECT r FROM QuestReview r WHERE r.isVisible = true AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(r.content) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    Page<QuestReview> findByTextSearch(@Param("searchText") String searchText, Pageable pageable);

    /**
     * Найти отзывы по текстовому содержимому для квеста
     */
    @Query("SELECT r FROM QuestReview r WHERE r.questId = :questId AND r.isVisible = true AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(r.content) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    Page<QuestReview> findByQuestIdAndTextSearch(@Param("questId") Long questId, 
                                               @Param("searchText") String searchText, 
                                               Pageable pageable);

    /**
     * Получить средний рейтинг по отзывам квеста
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM QuestReview r WHERE r.questId = :questId AND r.rating IS NOT NULL AND r.isVisible = true")
    Double getAverageRatingByQuestId(@Param("questId") Long questId);

    /**
     * Получить статистику рейтингов по отзывам для квеста
     */
    @Query("SELECT r.rating, COUNT(r) FROM QuestReview r WHERE r.questId = :questId AND r.rating IS NOT NULL AND r.isVisible = true GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingStatsByQuestId(@Param("questId") Long questId);

    /**
     * Найти отзывы для модерации
     */
    @Query("SELECT r FROM QuestReview r WHERE r.isVisible = false ORDER BY r.createdAt DESC")
    Page<QuestReview> findReviewsForModeration(Pageable pageable);

    /**
     * Найти отзывы с низким рейтингом
     */
    @Query("SELECT r FROM QuestReview r WHERE r.rating <= :maxRating AND r.isVisible = true ORDER BY r.createdAt DESC")
    Page<QuestReview> findLowRatedReviews(@Param("maxRating") Integer maxRating, Pageable pageable);

    /**
     * Найти отзывы с высоким рейтингом
     */
    @Query("SELECT r FROM QuestReview r WHERE r.rating >= :minRating AND r.isVisible = true ORDER BY r.createdAt DESC")
    Page<QuestReview> findHighRatedReviews(@Param("minRating") Integer minRating, Pageable pageable);
}