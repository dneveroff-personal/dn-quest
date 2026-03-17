package dn.quest.statistics.repository;

import dn.quest.statistics.entity.SystemStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с системной статистикой
 */
@Repository
public interface SystemStatisticsRepository extends JpaRepository<SystemStatistics, Long> {

    /**
     * Найти статистику по дате и метрике
     */
    Optional<SystemStatistics> findByDateAndMetric(LocalDate date, String metric);

    /**
     * Найти всю статистику по метрике
     */
    List<SystemStatistics> findByMetricOrderByDateDesc(String metric);

    /**
     * Найти статистику по метрике за период
     */
    List<SystemStatistics> findByMetricAndDateBetweenOrderByDateDesc(String metric, LocalDate startDate, LocalDate endDate);

    /**
     * Найти статистику по категории
     */
    List<SystemStatistics> findByCategoryOrderByDateDesc(String category);

    /**
     * Найти статистику по категории за период
     */
    List<SystemStatistics> findByCategoryAndDateBetweenOrderByDateDesc(String category, LocalDate startDate, LocalDate endDate);

    /**
     * Получить все метрики за дату
     */
    List<SystemStatistics> findByDateOrderByMetric(LocalDate date);

    /**
     * Получить активные метрики
     */
    List<SystemStatistics> findByIsActiveTrueOrderByMetric();

    /**
     * Получить метрики по приоритету
     */
    List<SystemStatistics> findByIsActiveTrueOrderByPriorityDesc();

    /**
     * Получить последние значения метрик
     */
    @Query("SELECT s FROM SystemStatistics s WHERE s.date = (SELECT MAX(s2.date) FROM SystemStatistics s2 WHERE s2.metric = s.metric) AND s.isActive = true")
    List<SystemStatistics> findLatestMetrics();

    /**
     * Получить статистику по метрике за последние N дней
     */
    @Query("SELECT s FROM SystemStatistics s WHERE s.metric = :metric AND s.date >= :startDate ORDER BY s.date DESC")
    List<SystemStatistics> findMetricRecent(@Param("metric") String metric, @Param("startDate") LocalDate startDate);

    /**
     * Получить агрегированные значения по метрике за период
     */
    @Query("SELECT " +
           "MIN(s.minValue) as minValue, " +
           "MAX(s.maxValue) as maxValue, " +
           "AVG(s.avgValue) as avgValue, " +
           "SUM(s.totalValue) as totalValue, " +
           "SUM(s.count) as totalCount " +
           "FROM SystemStatistics s " +
           "WHERE s.metric = :metric AND s.date BETWEEN :startDate AND :endDate")
    Object[] getMetricAggregates(@Param("metric") String metric, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить топ метрик по значению
     */
    @Query("SELECT s FROM SystemStatistics s WHERE s.date = :date AND s.value IS NOT NULL ORDER BY s.value DESC")
    List<SystemStatistics> findTopMetricsByValue(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить метрики с наибольшим изменением
     */
    @Query("SELECT s FROM SystemStatistics s WHERE s.date = :date AND s.percentageChange IS NOT NULL ORDER BY ABS(s.percentageChange) DESC")
    List<SystemStatistics> findTopMetricsByChange(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить метрики по категории и дате
     */
    @Query("SELECT s FROM SystemStatistics s WHERE s.category = :category AND s.date = :date ORDER BY s.priority DESC")
    List<SystemStatistics> findMetricsByCategoryAndDate(@Param("category") String category, @Param("date") LocalDate date);

    /**
     * Обновить значение метрики
     */
    @Modifying
    @Query("UPDATE SystemStatistics s SET s.value = :value, s.lastUpdatedAt = :updatedAt WHERE s.id = :id")
    int updateMetricValue(@Param("id") Long id, @Param("value") Double value, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Обновить статистику метрики
     */
    @Modifying
    @Query("UPDATE SystemStatistics s SET " +
           "s.value = :value, " +
           "s.count = :count, " +
           "s.minValue = :minValue, " +
           "s.maxValue = :maxValue, " +
           "s.avgValue = :avgValue, " +
           "s.totalValue = :totalValue, " +
           "s.lastUpdatedAt = :updatedAt " +
           "WHERE s.id = :id")
    int updateMetricStats(@Param("id") Long id, 
                         @Param("value") Double value, 
                         @Param("count") Long count,
                         @Param("minValue") Double minValue, 
                         @Param("maxValue") Double maxValue, 
                         @Param("avgValue") Double avgValue, 
                         @Param("totalValue") Double totalValue,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Увеличить счетчик метрики
     */
    @Modifying
    @Query("UPDATE SystemStatistics s SET s.count = s.count + 1, s.lastUpdatedAt = :updatedAt WHERE s.id = :id")
    int incrementMetricCount(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Добавить к общему значению
     */
    @Modifying
    @Query("UPDATE SystemStatistics s SET s.totalValue = s.totalValue + :value, s.lastUpdatedAt = :updatedAt WHERE s.id = :id")
    int addToTotalValue(@Param("id") Long id, @Param("value") Double value, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Получить уникальные категории
     */
    @Query("SELECT DISTINCT s.category FROM SystemStatistics s WHERE s.category IS NOT NULL AND s.isActive = true")
    List<String> findDistinctCategories();

    /**
     * Получить уникальные метрики в категории
     */
    @Query("SELECT DISTINCT s.metric FROM SystemStatistics s WHERE s.category = :category AND s.metric IS NOT NULL AND s.isActive = true")
    List<String> findDistinctMetricsByCategory(@Param("category") String category);

    /**
     * Получить статистику за период с пагинацией
     */
    Page<SystemStatistics> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Удалить старую статистику (для очистки)
     */
    @Modifying
    @Query("DELETE FROM SystemStatistics s WHERE s.date < :date")
    int deleteStatisticsOlderThan(@Param("date") LocalDate date);

    /**
     * Получить метрики обновленные после указанного времени
     */
    List<SystemStatistics> findByLastUpdatedAtAfter(LocalDateTime time);

    /**
     * Получить метрики по источнику данных
     */
    List<SystemStatistics> findByDataSourceOrderByDateDesc(String dataSource);

    /**
     * Получить метрики по статусу
     */
    List<SystemStatistics> findByStatusOrderByDateDesc(String status);

    /**
     * Найти статистику по категории и дате
     */
    Optional<SystemStatistics> findByCategoryAndDate(String category, LocalDate date);
}