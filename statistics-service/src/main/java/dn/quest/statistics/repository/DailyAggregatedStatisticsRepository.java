package dn.quest.statistics.repository;

import dn.quest.statistics.entity.DailyAggregatedStatistics;
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

/**
 * Репозиторий для работы с агрегированной ежедневной статистикой
 */
@Repository
public interface DailyAggregatedStatisticsRepository extends JpaRepository<DailyAggregatedStatistics, Long> {

    /**
     * Найти статистику по дате
     */
    Optional<DailyAggregatedStatistics> findByDate(LocalDate date);

    /**
     * Найти статистику за период
     */
    List<DailyAggregatedStatistics> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);

    /**
     * Найти статистику за период с пагинацией
     */
    Page<DailyAggregatedStatistics> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Найти последнюю статистику
     */
    Optional<DailyAggregatedStatistics> findFirstByOrderByDateDesc();

    /**
     * Получить статистику за последние N дней
     */
    List<DailyAggregatedStatistics> findTop30ByOrderByDateDesc();

    /**
     * Получить общее количество пользователей за период
     */
    @Query("SELECT SUM(d.totalUsers) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate")
    Long getTotalUsersByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить количество новых регистраций за период
     */
    @Query("SELECT SUM(d.newRegistrations) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate")
    Long getNewRegistrationsByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить среднее количество активных пользователей за период
     */
    @Query("SELECT AVG(d.activeUsers) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate")
    Double getAvgActiveUsersByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить общее количество квестов за период
     */
    @Query("SELECT SUM(d.totalQuests) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate")
    Long getTotalQuestsByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить количество новых квестов за период
     */
    @Query("SELECT SUM(d.newQuests) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate")
    Long getNewQuestsByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить общее количество игровых сессий за период
     */
    @Query("SELECT SUM(d.totalGameSessions) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate")
    Long getTotalGameSessionsByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить общее время в игре за период (в минутах)
     */
    @Query("SELECT SUM(d.totalGameTime) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate")
    Long getTotalGameTimeByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить среднее время игровой сессии за период (в минутах)
     */
    @Query("SELECT AVG(d.avgSessionTime) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate AND d.avgSessionTime IS NOT NULL")
    Double getAvgSessionTimeByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить общее количество команд за период
     */
    @Query("SELECT SUM(d.totalTeams) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate")
    Long getTotalTeamsByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить общее количество файлов за период
     */
    @Query("SELECT SUM(d.totalFiles) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate")
    Long getTotalFilesByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить общий размер хранилища за период (в байтах)
     */
    @Query("SELECT SUM(d.totalStorageUsed) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate")
    Long getTotalStorageUsedByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить средний коэффициент удержания за период
     */
    @Query("SELECT AVG(d.retentionRate) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate AND d.retentionRate IS NOT NULL")
    Double getAvgRetentionRateByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить средний коэффициент конверсии за период
     */
    @Query("SELECT AVG(d.conversionRate) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate AND d.conversionRate IS NOT NULL")
    Double getAvgConversionRateByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить пиковое количество одновременных пользователей за период
     */
    @Query("SELECT MAX(d.peakConcurrentUsers) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate AND d.peakConcurrentUsers IS NOT NULL")
    Integer getPeakConcurrentUsersByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить среднее время ответа системы за период (в миллисекундах)
     */
    @Query("SELECT AVG(d.avgResponseTime) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate AND d.avgResponseTime IS NOT NULL")
    Double getAvgResponseTimeByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить общее количество ошибок системы за период
     */
    @Query("SELECT SUM(d.systemErrors) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate")
    Long getTotalSystemErrorsByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить среднее время работы системы за период (в процентах)
     */
    @Query("SELECT AVG(d.uptimePercentage) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate AND d.uptimePercentage IS NOT NULL")
    Double getAvgUptimePercentageByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить тренд роста пользователей (сравнение с предыдущим периодом)
     */
    @Query("SELECT " +
           "(SELECT SUM(d.newRegistrations) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate) - " +
           "(SELECT SUM(d.newRegistrations) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :prevStartDate AND :prevEndDate)")
    Long getUserGrowthTrend(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, 
                           @Param("prevStartDate") LocalDate prevStartDate, @Param("prevEndDate") LocalDate prevEndDate);

    /**
     * Получить тренд роста квестов (сравнение с предыдущим периодом)
     */
    @Query("SELECT " +
           "(SELECT SUM(d.newQuests) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :startDate AND :endDate) - " +
           "(SELECT SUM(d.newQuests) FROM DailyAggregatedStatistics d WHERE d.date BETWEEN :prevStartDate AND :prevEndDate)")
    Long getQuestGrowthTrend(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, 
                           @Param("prevStartDate") LocalDate prevStartDate, @Param("prevEndDate") LocalDate prevEndDate);

    /**
     * Получить статистику по типам агрегации
     */
    @Query("SELECT d.aggregationType, COUNT(d) as count FROM DailyAggregatedStatistics d GROUP BY d.aggregationType")
    List<Object[]> getAggregationTypeStats();

    /**
     * Обновить общее количество пользователей
     */
    @Modifying
    @Query("UPDATE DailyAggregatedStatistics d SET d.totalUsers = :count WHERE d.date = :date")
    int updateTotalUsers(@Param("date") LocalDate date, @Param("count") Integer count);

    /**
     * Обновить количество активных пользователей
     */
    @Modifying
    @Query("UPDATE DailyAggregatedStatistics d SET d.activeUsers = :count WHERE d.date = :date")
    int updateActiveUsers(@Param("date") LocalDate date, @Param("count") Integer count);

    /**
     * Обновить количество новых регистраций
     */
    @Modifying
    @Query("UPDATE DailyAggregatedStatistics d SET d.newRegistrations = :count WHERE d.date = :date")
    int updateNewRegistrations(@Param("date") LocalDate date, @Param("count") Integer count);

    /**
     * Обновить общее количество квестов
     */
    @Modifying
    @Query("UPDATE DailyAggregatedStatistics d SET d.totalQuests = :count WHERE d.date = :date")
    int updateTotalQuests(@Param("date") LocalDate date, @Param("count") Integer count);

    /**
     * Обновить количество игровых сессий
     */
    @Modifying
    @Query("UPDATE DailyAggregatedStatistics d SET d.totalGameSessions = :count WHERE d.date = :date")
    int updateTotalGameSessions(@Param("date") LocalDate date, @Param("count") Integer count);

    /**
     * Обновить общее время в игре
     */
    @Modifying
    @Query("UPDATE DailyAggregatedStatistics d SET d.totalGameTime = :time WHERE d.date = :date")
    int updateTotalGameTime(@Param("date") LocalDate date, @Param("time") Long time);

    /**
     * Обновить количество отправок кода
     */
    @Modifying
    @Query("UPDATE DailyAggregatedStatistics d SET d.codeSubmissions = :count WHERE d.date = :date")
    int updateCodeSubmissions(@Param("date") LocalDate date, @Param("count") Integer count);

    /**
     * Обновить количество успешных отправок кода
     */
    @Modifying
    @Query("UPDATE DailyAggregatedStatistics d SET d.successfulCodeSubmissions = :count WHERE d.date = :date")
    int updateSuccessfulCodeSubmissions(@Param("date") LocalDate date, @Param("count") Integer count);

    /**
     * Обновить общее количество команд
     */
    @Modifying
    @Query("UPDATE DailyAggregatedStatistics d SET d.totalTeams = :count WHERE d.date = :date")
    int updateTotalTeams(@Param("date") LocalDate date, @Param("count") Integer count);

    /**
     * Обновить общее количество файлов
     */
    @Modifying
    @Query("UPDATE DailyAggregatedStatistics d SET d.totalFiles = :count WHERE d.date = :date")
    int updateTotalFiles(@Param("date") LocalDate date, @Param("count") Integer count);

    /**
     * Обновить общий размер хранилища
     */
    @Modifying
    @Query("UPDATE DailyAggregatedStatistics d SET d.totalStorageUsed = :size WHERE d.date = :date")
    int updateTotalStorageUsed(@Param("date") LocalDate date, @Param("size") Long size);

    /**
     * Удалить старую статистику (для очистки)
     */
    @Modifying
    @Query("DELETE FROM DailyAggregatedStatistics d WHERE d.date < :date")
    int deleteStatisticsOlderThan(@Param("date") LocalDate date);
}