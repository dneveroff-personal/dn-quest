package dn.quest.questmanagement.repository;

import dn.quest.questmanagement.entity.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с уровнями квестов
 */
@Repository
public interface LevelRepository extends JpaRepository<Level, Long>, JpaSpecificationExecutor<Level> {

    /**
     * Найти уровни по ID квеста с сортировкой по порядковому номеру
     */
    @Query("SELECT l FROM Level l WHERE l.questId = :questId ORDER BY l.orderIndex ASC")
    List<Level> findByQuestIdOrderByOrderIndex(@Param("questId") Long questId);

    /**
     * Найти уровни по ID квеста с пагинацией
     */
    @Query("SELECT l FROM Level l WHERE l.questId = :questId ORDER BY l.orderIndex ASC")
    Page<Level> findByQuestIdOrderByOrderIndex(@Param("questId") Long questId, Pageable pageable);

    /**
     * Найти уровень по ID квеста и порядковому номеру
     */
    @Query("SELECT l FROM Level l WHERE l.questId = :questId AND l.orderIndex = :orderIndex")
    Optional<Level> findByQuestIdAndOrderIndex(@Param("questId") Long questId, 
                                              @Param("orderIndex") Integer orderIndex);

    /**
     * Найти первый уровень квеста
     */
    @Query("SELECT l FROM Level l WHERE l.questId = :questId ORDER BY l.orderIndex ASC")
    Optional<Level> findFirstByQuestId(@Param("questId") Long questId);

    /**
     * Найти первый уровень квеста (альтернативный метод)
     */
    @Query("SELECT l FROM Level l WHERE l.questId = :questId ORDER BY l.orderIndex ASC")
    List<Level> findFirstLevelByQuestId(@Param("questId") Long questId, Pageable pageable);

    /**
     * Найти следующий уровень после указанного порядкового номера
     */
    @Query("SELECT l FROM Level l WHERE l.questId = :questId AND l.orderIndex > :orderIndex " +
           "ORDER BY l.orderIndex ASC")
    Optional<Level> findNextLevel(@Param("questId") Long questId, 
                                  @Param("orderIndex") Integer orderIndex);

    /**
     * Найти следующий уровень после указанного порядкового номера (альтернативный метод)
     */
    @Query("SELECT l FROM Level l WHERE l.questId = :questId AND l.orderIndex > :orderIndex " +
           "ORDER BY l.orderIndex ASC")
    List<Level> findNextLevelByQuestId(@Param("questId") Long questId, 
                                       @Param("orderIndex") Integer orderIndex, 
                                       Pageable pageable);

    /**
     * Найти предыдущий уровень перед указанным порядковым номером
     */
    @Query("SELECT l FROM Level l WHERE l.questId = :questId AND l.orderIndex < :orderIndex " +
           "ORDER BY l.orderIndex DESC")
    Optional<Level> findPreviousLevel(@Param("questId") Long questId, 
                                      @Param("orderIndex") Integer orderIndex);

    /**
     * Найти последний уровень квеста
     */
    @Query("SELECT l FROM Level l WHERE l.questId = :questId ORDER BY l.orderIndex DESC")
    Optional<Level> findLastByQuestId(@Param("questId") Long questId);

    /**
     * Найти уровни по названию (без учета регистра)
     */
    @Query("SELECT l FROM Level l WHERE LOWER(l.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "ORDER BY l.title ASC")
    List<Level> findByTitleContainingIgnoreCase(@Param("title") String title);

    /**
     * Найти уровни по названию с пагинацией
     */
    @Query("SELECT l FROM Level l WHERE LOWER(l.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "ORDER BY l.title ASC")
    Page<Level> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    /**
     * Найти уровни с ограничением по времени
     */
    @Query("SELECT l FROM Level l WHERE l.apTime IS NOT NULL AND l.apTime > 0 " +
           "ORDER BY l.apTime ASC")
    List<Level> findLevelsWithTimeLimit();

    /**
     * Найти уровни с ограничением по времени с пагинацией
     */
    @Query("SELECT l FROM Level l WHERE l.apTime IS NOT NULL AND l.apTime > 0 " +
           "ORDER BY l.apTime ASC")
    Page<Level> findLevelsWithTimeLimit(Pageable pageable);

    /**
     * Найти уровни, требующие секторы
     */
    @Query("SELECT l FROM Level l WHERE l.requiredSectors IS NOT NULL AND l.requiredSectors > 0 " +
           "ORDER BY l.requiredSectors DESC")
    List<Level> findLevelsWithRequiredSectors();

    /**
     * Найти геолокационные уровни
     */
    @Query("SELECT l FROM Level l WHERE l.latitude IS NOT NULL AND l.longitude IS NOT NULL " +
           "ORDER BY l.questId, l.orderIndex ASC")
    List<Level> findGeolocationLevels();

    /**
     * Найти активные уровни
     */
    List<Level> findByActiveTrue();

    /**
     * Найти активные уровни по ID квеста
     */
    @Query("SELECT l FROM Level l WHERE l.questId = :questId AND l.active = true " +
           "ORDER BY l.orderIndex ASC")
    List<Level> findActiveByQuestId(@Param("questId") Long questId);

    /**
     * Найти уровни созданные пользователем
     */
    List<Level> findByCreatedBy(Long createdBy);

    /**
     * Найти уровни созданные пользователем с пагинацией
     */
    Page<Level> findByCreatedBy(Long createdBy, Pageable pageable);

    /**
     * Подсчет уровней по ID квеста
     */
    @Query("SELECT COUNT(l) FROM Level l WHERE l.questId = :questId")
    long countByQuestId(@Param("questId") Long questId);

    /**
     * Подсчет активных уровней по ID квеста
     */
    @Query("SELECT COUNT(l) FROM Level l WHERE l.questId = :questId AND l.active = true")
    long countActiveByQuestId(@Param("questId") Long questId);

    /**
     * Подсчет уровней с ограничением по времени по ID квеста
     */
    @Query("SELECT COUNT(l) FROM Level l WHERE l.questId = :questId AND l.apTime IS NOT NULL AND l.apTime > 0")
    long countWithTimeLimitByQuestId(@Param("questId") Long questId);

    /**
     * Подсчет геолокационных уровней по ID квеста
     */
    @Query("SELECT COUNT(l) FROM Level l WHERE l.questId = :questId AND l.latitude IS NOT NULL AND l.longitude IS NOT NULL")
    long countGeolocationByQuestId(@Param("questId") Long questId);

    /**
     * Проверить существование уровня по ID квеста и порядковому номеру
     */
    boolean existsByQuestIdAndOrderIndex(Long questId, Integer orderIndex);

    /**
     * Найти максимальный порядковый номер уровня для квеста
     */
    @Query("SELECT COALESCE(MAX(l.orderIndex), 0) FROM Level l WHERE l.questId = :questId")
    Integer findMaxOrderIndexByQuestId(@Param("questId") Long questId);

    /**
     * Обновить порядковые номера уровней
     */
    @Modifying
    @Query("UPDATE Level l SET l.orderIndex = :newOrderIndex, l.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE l.id = :levelId")
    int updateOrderIndex(@Param("levelId") Long levelId, @Param("newOrderIndex") Integer newOrderIndex);

    /**
     * Активировать уровни
     */
    @Modifying
    @Query("UPDATE Level l SET l.active = true, l.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE l.id IN :levelIds")
    int activateLevels(@Param("levelIds") List<Long> levelIds);

    /**
     * Деактивировать уровни
     */
    @Modifying
    @Query("UPDATE Level l SET l.active = false, l.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE l.id IN :levelIds")
    int deactivateLevels(@Param("levelIds") List<Long> levelIds);

    /**
     * Удалить уровни по ID квеста
     */
    @Modifying
    @Query("DELETE FROM Level l WHERE l.questId = :questId")
    int deleteByQuestId(@Param("questId") Long questId);

    /**
     * Найти уровни для копирования (активные уровни опубликованных квестов)
     */
    @Query("SELECT l FROM Level l WHERE l.active = true AND l.questId IN " +
           "(SELECT q.id FROM Quest q WHERE q.published = true AND q.archived = false) " +
           "ORDER BY l.questId, l.orderIndex ASC")
    List<Level> findLevelsForCopying();

    /**
     * Найти дубликаты порядковых номеров для квеста
     */
    @Query("SELECT l.orderIndex, COUNT(l) FROM Level l WHERE l.questId = :questId " +
           "GROUP BY l.orderIndex HAVING COUNT(l) > 1")
    List<Object[]> findDuplicateOrderIndexes(@Param("questId") Long questId);

    /**
     * Получить статистику по уровням квеста
     */
    @Query("SELECT " +
           "COUNT(l) as totalLevels, " +
           "COUNT(CASE WHEN l.active = true THEN 1 END) as activeLevels, " +
           "COUNT(CASE WHEN l.apTime IS NOT NULL AND l.apTime > 0 THEN 1 END) as levelsWithTimeLimit, " +
           "COUNT(CASE WHEN l.requiredSectors IS NOT NULL AND l.requiredSectors > 0 THEN 1 END) as levelsWithSectors, " +
           "COUNT(CASE WHEN l.latitude IS NOT NULL AND l.longitude IS NOT NULL THEN 1 END) as geolocationLevels " +
           "FROM Level l WHERE l.questId = :questId")
    Object[] getQuestLevelStatistics(@Param("questId") Long questId);

    /**
     * Найти уровни в радиусе от указанной точки
     */
    @Query(value = "SELECT * FROM levels WHERE quest_id = :questId AND latitude IS NOT NULL AND longitude IS NOT NULL " +
           "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * " +
           "cos(radians(longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(latitude)))) < :radiusKm " +
           "ORDER BY order_index ASC", nativeQuery = true)
    List<Level> findLevelsInRadius(@Param("questId") Long questId, 
                                   @Param("latitude") Double latitude, 
                                   @Param("longitude") Double longitude, 
                                   @Param("radiusKm") Double radiusKm);

    /**
     * Найти уровни с дополнительными параметрами
     */
    @Query("SELECT l FROM Level l WHERE l.additionalParams IS NOT NULL " +
           "ORDER BY l.questId, l.orderIndex ASC")
    List<Level> findLevelsWithAdditionalParams();
}