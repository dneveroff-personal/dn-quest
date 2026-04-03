package dn.quest.questmanagement.repository;

import dn.quest.questmanagement.entity.Code;
import dn.quest.shared.enums.CodeType;
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
 * Repository для работы с кодами уровней
 */
@Repository
public interface CodeRepository extends JpaRepository<Code, UUID>, JpaSpecificationExecutor<Code> {

    /**
     * Найти коды по ID уровня
     */
    List<Code> findByLevelId(UUID levelId);

    /**
     * Найти коды по ID уровня с пагинацией
     */
    Page<Code> findByLevelId(UUID levelId, Pageable pageable);

    /**
     * Найти коды по ID уровня с сортировкой по типу и номеру сектора
     */
    @Query("SELECT c FROM Code c WHERE c.levelId = :levelId ORDER BY c.codeType, c.sectorNo")
    List<Code> findByLevelIdOrderByTypeAndSector(@Param("levelId") UUID levelId);

    /**
     * Найти коды по типу
     */
    List<Code> findByCodeType(CodeType codeType);

    /**
     * Найти коды по типу с пагинацией
     */
    Page<Code> findByCodeType(CodeType codeType, Pageable pageable);

    /**
     * Найти коды по ID уровня и типу
     */
    @Query("SELECT c FROM Code c WHERE c.levelId = :levelId AND c.codeType = :codeType " +
           "ORDER BY c.sectorNo")
    List<Code> findByLevelIdAndCodeType(@Param("levelId") UUID levelId, 
                                        @Param("codeType") CodeType codeType);

    /**
     * Найти обычные коды по ID уровня
     */
    @Query("SELECT c FROM Code c WHERE c.levelId = :levelId AND c.codeType = 'NORMAL' " +
           "ORDER BY c.sectorNo")
    List<Code> findNormalCodesByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти бонусные коды по ID уровня
     */
    @Query("SELECT c FROM Code c WHERE c.levelId = :levelId AND c.codeType = 'BONUS' " +
           "ORDER BY c.shiftSeconds DESC")
    List<Code> findBonusCodesByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти штрафные коды по ID уровня
     */
    @Query("SELECT c FROM Code c WHERE c.levelId = :levelId AND c.codeType = 'PENALTY' " +
           "ORDER BY c.shiftSeconds ASC")
    List<Code> findPenaltyCodesByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти код по ID уровня и номеру сектора
     */
    @Query("SELECT c FROM Code c WHERE c.levelId = :levelId AND c.sectorNo = :sectorNo " +
           "AND c.codeType = 'NORMAL'")
    Optional<Code> findNormalCodeByLevelIdAndSectorNo(@Param("levelId") UUID levelId, 
                                                     @Param("sectorNo") Integer sectorNo);

    /**
     * Найти код по значению (нормализованному)
     */
    @Query("SELECT c FROM Code c WHERE c.codeValue = :codeValue")
    List<Code> findByCodeValue(@Param("codeValue") String codeValue);

    /**
     * Найти код по ID уровня и значению
     */
    @Query("SELECT c FROM Code c WHERE c.levelId = :levelId AND c.codeValue = :codeValue")
    Optional<Code> findByLevelIdAndCodeValue(@Param("levelId") UUID levelId,
                                            @Param("codeValue") String codeValue);

    /**
     * Найти код по значению (без учета регистра)
     */
    @Query("SELECT c FROM Code c WHERE LOWER(c.codeValue) = LOWER(:codeValue)")
    List<Code> findByCodeValueIgnoreCase(@Param("codeValue") String codeValue);

    /**
     * Найти активные коды
     */
    List<Code> findByActiveTrue();

    /**
     * Найти активные коды по ID уровня
     */
    @Query("SELECT c FROM Code c WHERE c.levelId = :levelId AND c.active = true " +
           "ORDER BY c.codeType, c.sectorNo")
    List<Code> findActiveByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти коды с ограничением по использованию
     */
    @Query("SELECT c FROM Code c WHERE c.maxUsageCount IS NOT NULL AND c.maxUsageCount > 0 " +
           "ORDER BY c.maxUsageCount ASC")
    List<Code> findCodesWithUsageLimit();

    /**
     * Найти коды, которые можно использовать
     */
    @Query("SELECT c FROM Code c WHERE c.active = true AND " +
           "(c.maxUsageCount IS NULL OR c.usageCount < c.maxUsageCount) " +
           "ORDER BY c.codeType, c.sectorNo")
    List<Code> findUsableCodes();

    /**
     * Найти коды, которые можно использовать по ID уровня
     */
    @Query("SELECT c FROM Code c WHERE c.levelId = :levelId AND c.active = true AND " +
           "(c.maxUsageCount IS NULL OR c.usageCount < c.maxUsageCount) " +
           "ORDER BY c.codeType, c.sectorNo")
    List<Code> findUsableCodesByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти коды созданные пользователем
     */
    List<Code> findByCreatedBy(Long createdBy);

    /**
     * Найти коды созданные пользователем с пагинацией
     */
    Page<Code> findByCreatedBy(Long createdBy, Pageable pageable);

    /**
     * Найти коды по описанию
     */
    @Query("SELECT c FROM Code c WHERE LOWER(c.description) LIKE LOWER(CONCAT('%', :description, '%')) " +
           "ORDER BY c.description")
    List<Code> findByDescriptionContainingIgnoreCase(@Param("description") String description);

    /**
     * Найти коды по сдвигу времени
     */
    @Query("SELECT c FROM Code c WHERE c.shiftSeconds = :shiftSeconds " +
           "ORDER BY c.codeType, c.sectorNo")
    List<Code> findByShiftSeconds(@Param("shiftSeconds") Integer shiftSeconds);

    /**
     * Найти бонусные коды (с положительным сдвигом времени)
     */
    @Query("SELECT c FROM Code c WHERE c.shiftSeconds > 0 " +
           "ORDER BY c.shiftSeconds DESC")
    List<Code> findBonusCodes();

    /**
     * Найти штрафные коды (с отрицательным сдвигом времени)
     */
    @Query("SELECT c FROM Code c WHERE c.shiftSeconds < 0 " +
           "ORDER BY c.shiftSeconds ASC")
    List<Code> findPenaltyCodes();

    /**
     * Подсчет кодов по ID уровня
     */
    @Query("SELECT COUNT(c) FROM Code c WHERE c.levelId = :levelId")
    long countByLevelId(@Param("levelId") UUID levelId);

    /**
     * Подсчет кодов по типу
     */
    long countByCodeType(CodeType codeType);

    /**
     * Подсчет кодов по ID уровня и типу
     */
    @Query("SELECT COUNT(c) FROM Code c WHERE c.levelId = :levelId AND c.codeType = :codeType")
    long countByLevelIdAndCodeType(@Param("levelId") UUID levelId, 
                                   @Param("codeType") CodeType codeType);

    /**
     * Подсчет активных кодов по ID уровня
     */
    @Query("SELECT COUNT(c) FROM Code c WHERE c.levelId = :levelId AND c.active = true")
    long countActiveByLevelId(@Param("levelId") UUID levelId);

    /**
     * Подсчет используемых кодов по ID уровня
     */
    @Query("SELECT COUNT(c) FROM Code c WHERE c.levelId = :levelId AND c.active = true AND " +
           "(c.maxUsageCount IS NULL OR c.usageCount < c.maxUsageCount)")
    long countUsableByLevelId(@Param("levelId") UUID levelId);

    /**
     * Подсчет обычных кодов по ID уровня
     */
    @Query("SELECT COUNT(c) FROM Code c WHERE c.levelId = :levelId AND c.codeType = 'NORMAL'")
    long countNormalCodesByLevelId(@Param("levelId") UUID levelId);

    /**
     * Проверить существование кода по ID уровня и номеру сектора
     */
    @Query("SELECT COUNT(c) > 0 FROM Code c WHERE c.levelId = :levelId AND c.sectorNo = :sectorNo " +
           "AND c.codeType = 'NORMAL'")
    boolean existsNormalCodeByLevelIdAndSectorNo(@Param("levelId") UUID levelId, 
                                                @Param("sectorNo") Integer sectorNo);

    /**
     * Проверить существование кода по значению
     */
    boolean existsByCodeValue(String codeValue);

    /**
     * Проверить существование кода по ID уровня и значению
     */
    @Query("SELECT COUNT(c) > 0 FROM Code c WHERE c.levelId = :levelId AND c.codeValue = :codeValue")
    boolean existsByLevelIdAndCodeValue(@Param("levelId") UUID levelId, 
                                       @Param("codeValue") String codeValue);

    /**
     * Найти максимальный номер сектора для уровня
     */
    @Query("SELECT COALESCE(MAX(c.sectorNo), 0) FROM Code c WHERE c.levelId = :levelId AND c.codeType = 'NORMAL'")
    Integer findMaxSectorNoByLevelId(@Param("levelId") UUID levelId);

    /**
     * Обновить счетчик использований кода
     */
    @Modifying
    @Query("UPDATE Code c SET c.usageCount = c.usageCount + 1, c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.id = :codeId")
    int incrementUsageCount(@Param("codeId") Long codeId);

    /**
     * Активировать коды
     */
    @Modifying
    @Query("UPDATE Code c SET c.active = true, c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.id IN :codeIds")
    int activateCodes(@Param("codeIds") List<Long> codeIds);

    /**
     * Деактивировать коды
     */
    @Modifying
    @Query("UPDATE Code c SET c.active = false, c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.id IN :codeIds")
    int deactivateCodes(@Param("codeIds") List<Long> codeIds);

    /**
     * Удалить коды по ID уровня
     */
    @Modifying
    @Query("DELETE FROM Code c WHERE c.levelId = :levelId")
    int deleteByLevelId(@Param("levelId") UUID levelId);

    /**
     * Найти дубликаты кодов по значению
     */
    @Query("SELECT c.codeValue, COUNT(c) FROM Code c GROUP BY c.codeValue HAVING COUNT(c) > 1")
    List<Object[]> findDuplicateCodes();

    /**
     * Найти дубликаты кодов по уровню и номеру сектора
     */
    @Query("SELECT c.levelId, c.sectorNo, COUNT(c) FROM Code c " +
           "WHERE c.codeType = 'NORMAL' GROUP BY c.levelId, c.sectorNo HAVING COUNT(c) > 1")
    List<Object[]> findDuplicateSectorCodes();

    /**
     * Получить статистику по кодам уровня
     */
    @Query("SELECT " +
           "COUNT(c) as totalCodes, " +
           "COUNT(CASE WHEN c.active = true THEN 1 END) as activeCodes, " +
           "COUNT(CASE WHEN c.codeType = 'NORMAL' THEN 1 END) as normalCodes, " +
           "COUNT(CASE WHEN c.codeType = 'BONUS' THEN 1 END) as bonusCodes, " +
           "COUNT(CASE WHEN c.codeType = 'PENALTY' THEN 1 END) as penaltyCodes, " +
           "SUM(c.usageCount) as totalUsages " +
           "FROM Code c WHERE c.levelId = :levelId")
    Object[] getLevelCodeStatistics(@Param("levelId") UUID levelId);

    /**
     * Найти наиболее используемые коды
     */
    @Query("SELECT c FROM Code c WHERE c.usageCount > 0 " +
           "ORDER BY c.usageCount DESC")
    List<Code> findMostUsedCodes(Pageable pageable);

    /**
     * Найти коды с истекающим лимитом использования
     */
    @Query("SELECT c FROM Code c WHERE c.maxUsageCount IS NOT NULL AND " +
           "c.usageCount >= c.maxUsageCount - 5 AND c.usageCount < c.maxUsageCount " +
           "ORDER BY (c.maxUsageCount - c.usageCount) ASC")
    List<Code> findCodesNearUsageLimit();

    /**
     * Найти коды для копирования (активные коды активных уровней)
     */
    @Query("SELECT c FROM Code c WHERE c.active = true AND c.levelId IN " +
           "(SELECT l.id FROM Level l WHERE l.active = true) " +
           "ORDER BY c.levelId, c.codeType, c.sectorNo")
    List<Code> findCodesForCopying();

    /**
     * Сбросить счетчики использований для кодов уровня
     */
    @Modifying
    @Query("UPDATE Code c SET c.usageCount = 0, c.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE c.levelId = :levelId")
    int resetUsageCountByLevelId(@Param("levelId") UUID levelId);
}