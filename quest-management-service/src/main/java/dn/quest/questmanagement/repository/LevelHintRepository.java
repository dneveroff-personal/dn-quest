package dn.quest.questmanagement.repository;

import dn.quest.questmanagement.entity.LevelHint;
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
import java.util.UUID;

/**
 * Repository для работы с подсказками уровней
 */
@Repository
public interface LevelHintRepository extends JpaRepository<LevelHint, Long>, JpaSpecificationExecutor<LevelHint> {

    /**
     * Найти подсказки по ID уровня с сортировкой по порядковому номеру
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId ORDER BY h.orderIndex ASC")
    List<LevelHint> findByLevelIdOrderByOrderIndex(@Param("levelId") UUID levelId);

    /**
     * Найти подсказки по ID уровня с пагинацией
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId ORDER BY h.orderIndex ASC")
    Page<LevelHint> findByLevelIdOrderByOrderIndex(@Param("levelId") UUID levelId, Pageable pageable);

    /**
     * Найти подсказку по ID уровня и порядковому номеру
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId AND h.orderIndex = :orderIndex")
    Optional<LevelHint> findByLevelIdAndOrderIndex(@Param("levelId") UUID levelId, 
                                                   @Param("orderIndex") Integer orderIndex);

    /**
     * Найти подсказки по типу
     */
    List<LevelHint> findByHintType(LevelHint.HintType hintType);

    /**
     * Найти подсказки по типу с пагинацией
     */
    Page<LevelHint> findByHintType(LevelHint.HintType hintType, Pageable pageable);

    /**
     * Найти подсказки по ID уровня и типу
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId AND h.hintType = :hintType " +
           "ORDER BY h.orderIndex ASC")
    List<LevelHint> findByLevelIdAndHintType(@Param("levelId") UUID levelId, 
                                             @Param("hintType") LevelHint.HintType hintType);

    /**
     * Найти текстовые подсказки по ID уровня
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId AND h.hintType = 'TEXT' " +
           "ORDER BY h.orderIndex ASC")
    List<LevelHint> findTextHintsByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти файловые подсказки по ID уровня
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId AND h.hintType = 'FILE' " +
           "ORDER BY h.orderIndex ASC")
    List<LevelHint> findFileHintsByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти подсказки по типу файла
     */
    List<LevelHint> findByFileType(LevelHint.FileType fileType);

    /**
     * Найти подсказки по ID уровня и типу файла
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId AND h.fileType = :fileType " +
           "ORDER BY h.orderIndex ASC")
    List<LevelHint> findByLevelIdAndFileType(@Param("levelId") UUID levelId, 
                                             @Param("fileType") LevelHint.FileType fileType);

    /**
     * Найти подсказки по смещению времени
     */
    @Query("SELECT h FROM LevelHint h WHERE h.offsetSec = :offsetSec " +
           "ORDER BY h.levelId, h.orderIndex ASC")
    List<LevelHint> findByOffsetSec(@Param("offsetSec") Integer offsetSec);

    /**
     * Найти подсказки по смещению времени (меньше или равно)
     */
    @Query("SELECT h FROM LevelHint h WHERE h.offsetSec <= :offsetSec " +
           "ORDER BY h.offsetSec ASC, h.orderIndex ASC")
    List<LevelHint> findByOffsetSecLessThanEqual(@Param("offsetSec") Integer offsetSec);

    /**
     * Найти подсказки по смещению времени (меньше или равно) для уровня
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId AND h.offsetSec <= :offsetSec " +
           "ORDER BY h.offsetSec ASC, h.orderIndex ASC")
    List<LevelHint> findByLevelIdAndOffsetSecLessThanEqual(@Param("levelId") UUID levelId, 
                                                           @Param("offsetSec") Integer offsetSec);

    /**
     * Найти активные подсказки
     */
    List<LevelHint> findByActiveTrue();

    /**
     * Найти активные подсказки по ID уровня
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId AND h.active = true " +
           "ORDER BY h.orderIndex ASC")
    List<LevelHint> findActiveByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти обязательные подсказки
     */
    List<LevelHint> findByMandatoryTrue();

    /**
     * Найти обязательные подсказки по ID уровня
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId AND h.mandatory = true " +
           "ORDER BY h.orderIndex ASC")
    List<LevelHint> findMandatoryByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти бесплатные подсказки
     */
    @Query("SELECT h FROM LevelHint h WHERE h.cost IS NULL OR h.cost = 0 " +
           "ORDER BY h.levelId, h.orderIndex ASC")
    List<LevelHint> findFreeHints();

    /**
     * Найти бесплатные подсказки по ID уровня
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId AND (h.cost IS NULL OR h.cost = 0) " +
           "ORDER BY h.orderIndex ASC")
    List<LevelHint> findFreeByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти платные подсказки
     */
    @Query("SELECT h FROM LevelHint h WHERE h.cost IS NOT NULL AND h.cost > 0 " +
           "ORDER BY h.cost ASC, h.levelId, h.orderIndex ASC")
    List<LevelHint> findPaidHints();

    /**
     * Найти платные подсказки по ID уровня
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId AND h.cost IS NOT NULL AND h.cost > 0 " +
           "ORDER BY h.cost ASC, h.orderIndex ASC")
    List<LevelHint> findPaidByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти подсказки по стоимости
     */
    @Query("SELECT h FROM LevelHint h WHERE h.cost = :cost " +
           "ORDER BY h.levelId, h.orderIndex ASC")
    List<LevelHint> findByCost(@Param("cost") Integer cost);

    /**
     * Найти подсказки по диапазону стоимости
     */
    @Query("SELECT h FROM LevelHint h WHERE h.cost >= :minCost AND h.cost <= :maxCost " +
           "ORDER BY h.cost ASC, h.levelId, h.orderIndex ASC")
    List<LevelHint> findByCostBetween(@Param("minCost") Integer minCost, 
                                      @Param("maxCost") Integer maxCost);

    /**
     * Найти подсказки по заголовку
     */
    @Query("SELECT h FROM LevelHint h WHERE LOWER(h.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "ORDER BY h.title ASC")
    List<LevelHint> findByTitleContainingIgnoreCase(@Param("title") String title);

    /**
     * Найти подсказки по тексту
     */
    @Query("SELECT h FROM LevelHint h WHERE LOWER(h.hintText) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "ORDER BY h.hintText")
    List<LevelHint> findByHintTextContainingIgnoreCase(@Param("text") String text);

    /**
     * Найти подсказки созданные пользователем
     */
    List<LevelHint> findByCreatedBy(Long createdBy);

    /**
     * Найти подсказки созданные пользователем с пагинацией
     */
    Page<LevelHint> findByCreatedBy(Long createdBy, Pageable pageable);

    /**
     * Найти подсказки с файлами
     */
    @Query("SELECT h FROM LevelHint h WHERE h.fileUrl IS NOT NULL AND h.fileUrl != '' " +
           "ORDER BY h.levelId, h.orderIndex ASC")
    List<LevelHint> findHintsWithFiles();

    /**
     * Найти подсказки с файлами по ID уровня
     */
    @Query("SELECT h FROM LevelHint h WHERE h.levelId = :levelId AND h.fileUrl IS NOT NULL AND h.fileUrl != '' " +
           "ORDER BY h.orderIndex ASC")
    List<LevelHint> findHintsWithFilesByLevelId(@Param("levelId") UUID levelId);

    /**
     * Подсчет подсказок по ID уровня
     */
    @Query("SELECT COUNT(h) FROM LevelHint h WHERE h.levelId = :levelId")
    long countByLevelId(@Param("levelId") UUID levelId);

    /**
     * Подсчет подсказок по типу
     */
    long countByHintType(LevelHint.HintType hintType);

    /**
     * Подсчет подсказок по ID уровня и типу
     */
    @Query("SELECT COUNT(h) FROM LevelHint h WHERE h.levelId = :levelId AND h.hintType = :hintType")
    long countByLevelIdAndHintType(@Param("levelId") UUID levelId, 
                                   @Param("hintType") LevelHint.HintType hintType);

    /**
     * Подсчет активных подсказок по ID уровня
     */
    @Query("SELECT COUNT(h) FROM LevelHint h WHERE h.levelId = :levelId AND h.active = true")
    long countActiveByLevelId(@Param("levelId") UUID levelId);

    /**
     * Подсчет обязательных подсказок по ID уровня
     */
    @Query("SELECT COUNT(h) FROM LevelHint h WHERE h.levelId = :levelId AND h.mandatory = true")
    long countMandatoryByLevelId(@Param("levelId") UUID levelId);

    /**
     * Проверить существование подсказки по ID уровня и порядковому номеру
     */
    boolean existsByLevelIdAndOrderIndex(UUID levelId, Integer orderIndex);

    /**
     * Найти максимальный порядковый номер подсказки для уровня
     */
    @Query("SELECT COALESCE(MAX(h.orderIndex), 0) FROM LevelHint h WHERE h.levelId = :levelId")
    Integer findMaxOrderIndexByLevelId(@Param("levelId") UUID levelId);

    /**
     * Обновить порядковый номер подсказки
     */
    @Modifying
    @Query("UPDATE LevelHint h SET h.orderIndex = :newOrderIndex, h.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE h.id = :hintId")
    int updateOrderIndex(@Param("hintId") Long hintId, @Param("newOrderIndex") Integer newOrderIndex);

    /**
     * Активировать подсказки
     */
    @Modifying
    @Query("UPDATE LevelHint h SET h.active = true, h.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE h.id IN :hintIds")
    int activateHints(@Param("hintIds") List<Long> hintIds);

    /**
     * Деактивировать подсказки
     */
    @Modifying
    @Query("UPDATE LevelHint h SET h.active = false, h.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE h.id IN :hintIds")
    int deactivateHints(@Param("hintIds") List<Long> hintIds);

    /**
     * Удалить подсказки по ID уровня
     */
    @Modifying
    @Query("DELETE FROM LevelHint h WHERE h.levelId = :levelId")
    int deleteByLevelId(@Param("levelId") UUID levelId);

    /**
     * Увеличить счетчик использований подсказки
     */
    @Modifying
    @Query("UPDATE LevelHint h SET h.usageCount = h.usageCount + 1, h.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE h.id = :hintId")
    int incrementUsageCount(@Param("hintId") Long hintId);

    /**
     * Найти дубликаты порядковых номеров для уровня
     */
    @Query("SELECT h.orderIndex, COUNT(h) FROM LevelHint h WHERE h.levelId = :levelId " +
           "GROUP BY h.orderIndex HAVING COUNT(h) > 1")
    List<Object[]> findDuplicateOrderIndexes(@Param("levelId") UUID levelId);

    /**
     * Получить статистику по подсказкам уровня
     */
    @Query("SELECT " +
           "COUNT(h) as totalHints, " +
           "COUNT(CASE WHEN h.active = true THEN 1 END) as activeHints, " +
           "COUNT(CASE WHEN h.hintType = 'TEXT' THEN 1 END) as textHints, " +
           "COUNT(CASE WHEN h.hintType = 'FILE' THEN 1 END) as fileHints, " +
           "COUNT(CASE WHEN h.mandatory = true THEN 1 END) as mandatoryHints, " +
           "COUNT(CASE WHEN h.cost IS NULL OR h.cost = 0 THEN 1 END) as freeHints, " +
           "SUM(h.usageCount) as totalUsages, " +
           "COALESCE(AVG(h.cost), 0) as avgCost " +
           "FROM LevelHint h WHERE h.levelId = :levelId")
    Object[] getLevelHintStatistics(@Param("levelId") UUID levelId);

    /**
     * Найти наиболее используемые подсказки
     */
    @Query("SELECT h FROM LevelHint h WHERE h.usageCount > 0 " +
           "ORDER BY h.usageCount DESC")
    List<LevelHint> findMostUsedHints(Pageable pageable);

    /**
     * Найти подсказки для копирования (активные подсказки активных уровней)
     */
    @Query("SELECT h FROM LevelHint h WHERE h.active = true AND h.levelId IN " +
           "(SELECT l.id FROM Level l WHERE l.active = true) " +
           "ORDER BY h.levelId, h.orderIndex ASC")
    List<LevelHint> findHintsForCopying();

    /**
     * Сбросить счетчики использований для подсказок уровня
     */
    @Modifying
    @Query("UPDATE LevelHint h SET h.usageCount = 0, h.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE h.levelId = :levelId")
    int resetUsageCountByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти подсказки с истекающим сроком доступности
     */
    @Query("SELECT h FROM LevelHint h WHERE h.offsetSec > 0 " +
           "ORDER BY h.offsetSec ASC")
    List<LevelHint> findHintsWithTimeOffset();
}