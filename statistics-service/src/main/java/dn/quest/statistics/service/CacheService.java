package dn.quest.statistics.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Сервис для кэширования статистических данных
 */
public interface CacheService {

    /**
     * Получить кэшированную статистику пользователя
     */
    Map<String, Object> getUserStatistics(Long userId, LocalDate date);

    /**
     * Сохранить статистику пользователя в кэш
     */
    void cacheUserStatistics(Long userId, LocalDate date, Map<String, Object> statistics);

    /**
     * Получить кэшированную статистику квеста
     */
    Map<String, Object> getQuestStatistics(Long questId, LocalDate date);

    /**
     * Сохранить статистику квеста в кэш
     */
    void cacheQuestStatistics(Long questId, LocalDate date, Map<String, Object> statistics);

    /**
     * Получить кэшированную статистику команды
     */
    Map<String, Object> getTeamStatistics(Long teamId, LocalDate date);

    /**
     * Сохранить статистику команды в кэш
     */
    void cacheTeamStatistics(Long teamId, LocalDate date, Map<String, Object> statistics);

    /**
     * Получить кэшированный лидерборд
     */
    List<Map<String, Object>> getLeaderboard(String type, String period, LocalDate date);

    /**
     * Сохранить лидерборд в кэш
     */
    void cacheLeaderboard(String type, String period, LocalDate date, List<Map<String, Object>> leaderboard);

    /**
     * Получить кэшированные топ записи
     */
    List<Map<String, Object>> getTopRecords(String entityType, String metric, LocalDate date);

    /**
     * Сохранить топ записи в кэш
     */
    void cacheTopRecords(String entityType, String metric, LocalDate date, List<Map<String, Object>> records);

    /**
     * Получить кэшированную общую статистику платформы
     */
    Map<String, Object> getPlatformOverview(LocalDate startDate, LocalDate endDate);

    /**
     * Сохранить общую статистику платформы в кэш
     */
    void cachePlatformOverview(LocalDate startDate, LocalDate endDate, Map<String, Object> overview);

    /**
     * Получить кэшированные тренды метрик
     */
    Map<String, Object> getMetricTrends(List<String> metrics, String period, int periods);

    /**
     * Сохранить тренды метрик в кэш
     */
    void cacheMetricTrends(List<String> metrics, String period, int periods, Map<String, Object> trends);

    /**
     * Получить кэшированную статистику по категориям
     */
    Map<String, Object> getStatisticsByCategories(String entityType, LocalDate startDate, LocalDate endDate);

    /**
     * Сохранить статистику по категориям в кэш
     */
    void cacheStatisticsByCategories(String entityType, LocalDate startDate, LocalDate endDate, Map<String, Object> statistics);

    /**
     * Получить кэшированные системные метрики
     */
    Map<String, Object> getSystemMetrics(String category, LocalDate date);

    /**
     * Сохранить системные метрики в кэш
     */
    void cacheSystemMetrics(String category, LocalDate date, Map<String, Object> metrics);

    /**
     * Получить кэшированный аналитический отчет
     */
    Map<String, Object> getAnalyticsReport(String reportKey);

    /**
     * Сохранить аналитический отчет в кэш
     */
    void cacheAnalyticsReport(String reportKey, Map<String, Object> report);

    /**
     * Получить кэшированные данные для отчета
     */
    byte[] getReportData(String reportKey);

    /**
     * Сохранить данные отчета в кэш
     */
    void cacheReportData(String reportKey, byte[] data);

    /**
     * Инвалидировать кэш пользователя
     */
    void invalidateUserCache(Long userId);

    /**
     * Инвалидировать кэш квеста
     */
    void invalidateQuestCache(Long questId);

    /**
     * Инвалидировать кэш команды
     */
    void invalidateTeamCache(Long teamId);

    /**
     * Инвалидировать кэш лидербордов
     */
    void invalidateLeaderboardCache(String type, String period, LocalDate date);

    /**
     * Инвалидировать кэш за дату
     */
    void invalidateDateCache(LocalDate date);

    /**
     * Инвалидировать весь кэш статистики
     */
    void invalidateAllStatisticsCache();

    /**
     * Инвалидировать кэш аналитики
     */
    void invalidateAnalyticsCache();

    /**
     * Инвалидировать кэш отчетов
     */
    void invalidateReportsCache();

    /**
     * Очистить истекший кэш
     */
    void cleanupExpiredCache();

    /**
     * Получить статистику кэша
     */
    Map<String, Object> getCacheStatistics();

    /**
     * Предварительно прогреть кэш
     */
    void warmupCache();

    /**
     * Проверить наличие данных в кэше
     */
    boolean existsInCache(String key);

    /**
     * Получить размер кэша
     */
    long getCacheSize();

    /**
     * Получить количество записей в кэше
     */
    long getCacheEntryCount();

    /**
     * Очистить кэш по шаблону
     */
    void clearCacheByPattern(String pattern);
}