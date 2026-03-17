package dn.quest.questmanagement.repository;

import dn.quest.questmanagement.entity.QuestRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с рейтингами квестов
 */
@Repository
public interface QuestRatingRepository extends JpaRepository<QuestRating, Long> {

    /**
     * Найти рейтинг квеста от пользователя
     */
    Optional<QuestRating> findByQuestIdAndUserId(Long questId, Long userId);

    /**
     * Найти все рейтинги квеста
     */
    List<QuestRating> findByQuestId(Long questId);

    /**
     * Найти все рейтинги пользователя
     */
    List<QuestRating> findByUserId(Long userId);

    /**
     * Удалить рейтинг квеста от пользователя
     */
    @Modifying
    @Query("DELETE FROM QuestRating r WHERE r.questId = :questId AND r.userId = :userId")
    void deleteByQuestIdAndUserId(@Param("questId") Long questId, @Param("userId") Long userId);

    /**
     * Удалить все рейтинги квеста
     */
    @Modifying
    @Query("DELETE FROM QuestRating r WHERE r.questId = :questId")
    void deleteByQuestId(@Param("questId") Long questId);

    /**
     * Удалить все рейтинги пользователя
     */
    @Modifying
    @Query("DELETE FROM QuestRating r WHERE r.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * Подсчитать количество рейтингов квеста
     */
    @Query("SELECT COUNT(r) FROM QuestRating r WHERE r.questId = :questId")
    long countByQuestId(@Param("questId") Long questId);

    /**
     * Подсчитать средний рейтинг квеста
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM QuestRating r WHERE r.questId = :questId")
    Double getAverageRatingByQuestId(@Param("questId") Long questId);

    /**
     * Подсчитать количество рейтингов с определенным значением для квеста
     */
    @Query("SELECT COUNT(r) FROM QuestRating r WHERE r.questId = :questId AND r.rating = :rating")
    long countByQuestIdAndRating(@Param("questId") Long questId, @Param("rating") Integer rating);

    /**
     * Найти рейтинги квеста с определенным значением
     */
    List<QuestRating> findByQuestIdAndRating(Long questId, Integer rating);

    /**
     * Проверить существование рейтинга
     */
    boolean existsByQuestIdAndUserId(Long questId, Long userId);

    /**
     * Найти рейтинги, созданные в определенном периоде
     */
    @Query("SELECT r FROM QuestRating r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    List<QuestRating> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                           @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Найти последние рейтинги квеста
     */
    @Query("SELECT r FROM QuestRating r WHERE r.questId = :questId ORDER BY r.createdAt DESC")
    List<QuestRating> findLatestByQuestId(@Param("questId") Long questId, org.springframework.data.domain.Pageable pageable);

    /**
     * Получить статистику рейтингов для квеста
     */
    @Query("SELECT r.rating, COUNT(r) FROM QuestRating r WHERE r.questId = :questId GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingStatsByQuestId(@Param("questId") Long questId);
}