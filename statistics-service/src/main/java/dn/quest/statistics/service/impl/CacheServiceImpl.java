package dn.quest.statistics.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import dn.quest.statistics.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Реализация сервиса кэширования с использованием Caffeine
 */
@Service
@Slf4j
public class CacheServiceImpl implements CacheService {

    // Кэши для различных типов данных
    private final Cache<String, Map<String, Object>> userStatisticsCache;
    private final Cache<String, Map<String, Object>> questStatisticsCache;
    private final Cache<String, Map<String, Object>> teamStatisticsCache;
    private final Cache<String, List<Map<String, Object>>> leaderboardCache;
    private final Cache<String, List<Map<String, Object>>> topRecordsCache;
    private final Cache<String, Map<String, Object>> platformOverviewCache;
    private final Cache<String, Map<String, Object>> metricTrendsCache;
    private final Cache<String, Map<String, Object>> statisticsByCategoriesCache;
    private final Cache<String, Map<String, Object>> systemMetricsCache;
    private final Cache<String, Map<String, Object>> analyticsReportCache;
    private final Cache<String, byte[]> reportDataCache;

    // Конфигурация TTL
    @Value("${statistics.cache.user-stats-ttl:30}")
    private int userStatsTtlMinutes;

    @Value("${statistics.cache.quest-stats-ttl:60}")
    private int questStatsTtlMinutes;

    @Value("${statistics.cache.team-stats-ttl:45}")
    private int teamStatsTtlMinutes;

    @Value("${statistics.cache.leaderboard-ttl:15}")
    private int leaderboardTtlMinutes;

    @Value("${statistics.cache.platform-overview-ttl:120}")
    private int platformOverviewTtlMinutes;

    @Value("${statistics.cache.analytics-ttl:180}")
    private int analyticsTtlMinutes;

    @Value("${statistics.cache.report-data-ttl:1440}")
    private int reportDataTtlMinutes;

    // Конфигурация размера кэша
    @Value("${statistics.cache.max-size:10000}")
    private int maxCacheSize;

    public CacheServiceImpl() {
        // Инициализация кэшей с настройками по умолчанию
        this.userStatisticsCache = Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.questStatisticsCache = Caffeine.newBuilder()
                .maximumSize(3000)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.teamStatisticsCache = Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(45, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.leaderboardCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.topRecordsCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(20, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.platformOverviewCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(120, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.metricTrendsCache = Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(90, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.statisticsByCategoriesCache = Caffeine.newBuilder()
                .maximumSize(300)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.systemMetricsCache = Caffeine.newBuilder()
                .maximumSize(400)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.analyticsReportCache = Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(180, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.reportDataCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(1440, TimeUnit.MINUTES) // 24 часа
                .recordStats()
                .build();
    }

    @Override
    public Map<String, Object> getUserStatistics(UUID userId, LocalDate date) {
        String key = buildUserStatsKey(userId, date);
        return userStatisticsCache.getIfPresent(key);
    }

    @Override
    public void cacheUserStatistics(UUID userId, LocalDate date, Map<String, Object> statistics) {
        String key = buildUserStatsKey(userId, date);
        userStatisticsCache.put(key, statistics);
        log.debug("Cached user statistics for user: {} date: {}", userId, date);
    }

    @Override
    public Map<String, Object> getQuestStatistics(UUID questId, LocalDate date) {
        String key = buildQuestStatsKey(questId, date);
        return questStatisticsCache.getIfPresent(key);
    }

    @Override
    public void cacheQuestStatistics(UUID questId, LocalDate date, Map<String, Object> statistics) {
        String key = buildQuestStatsKey(questId, date);
        questStatisticsCache.put(key, statistics);
        log.debug("Cached quest statistics for quest: {} date: {}", questId, date);
    }

    @Override
    public Map<String, Object> getTeamStatistics(UUID teamId, LocalDate date) {
        String key = buildTeamStatsKey(teamId, date);
        return teamStatisticsCache.getIfPresent(key);
    }

    @Override
    public void cacheTeamStatistics(UUID teamId, LocalDate date, Map<String, Object> statistics) {
        String key = buildTeamStatsKey(teamId, date);
        teamStatisticsCache.put(key, statistics);
        log.debug("Cached team statistics for team: {} date: {}", teamId, date);
    }

    @Override
    public List<Map<String, Object>> getLeaderboard(String type, String period, LocalDate date) {
        String key = buildLeaderboardKey(type, period, date);
        return leaderboardCache.getIfPresent(key);
    }

    @Override
    public void cacheLeaderboard(String type, String period, LocalDate date, List<Map<String, Object>> leaderboard) {
        String key = buildLeaderboardKey(type, period, date);
        leaderboardCache.put(key, leaderboard);
        log.debug("Cached leaderboard for type: {} period: {} date: {}", type, period, date);
    }

    @Override
    public List<Map<String, Object>> getTopRecords(String entityType, String metric, LocalDate date) {
        String key = buildTopRecordsKey(entityType, metric, date);
        return topRecordsCache.getIfPresent(key);
    }

    @Override
    public void cacheTopRecords(String entityType, String metric, LocalDate date, List<Map<String, Object>> records) {
        String key = buildTopRecordsKey(entityType, metric, date);
        topRecordsCache.put(key, records);
        log.debug("Cached top records for entity: {} metric: {} date: {}", entityType, metric, date);
    }

    @Override
    public Map<String, Object> getPlatformOverview(LocalDate startDate, LocalDate endDate) {
        String key = buildPlatformOverviewKey(startDate, endDate);
        return platformOverviewCache.getIfPresent(key);
    }

    @Override
    public void cachePlatformOverview(LocalDate startDate, LocalDate endDate, Map<String, Object> overview) {
        String key = buildPlatformOverviewKey(startDate, endDate);
        platformOverviewCache.put(key, overview);
        log.debug("Cached platform overview from {} to {}", startDate, endDate);
    }

    @Override
    public Map<String, Object> getMetricTrends(List<String> metrics, String period, int periods) {
        String key = buildMetricTrendsKey(metrics, period, periods);
        return metricTrendsCache.getIfPresent(key);
    }

    @Override
    public void cacheMetricTrends(List<String> metrics, String period, int periods, Map<String, Object> trends) {
        String key = buildMetricTrendsKey(metrics, period, periods);
        metricTrendsCache.put(key, trends);
        log.debug("Cached metric trends for metrics: {} period: {} periods: {}", metrics, period, periods);
    }

    @Override
    public Map<String, Object> getStatisticsByCategories(String entityType, LocalDate startDate, LocalDate endDate) {
        String key = buildStatisticsByCategoriesKey(entityType, startDate, endDate);
        return statisticsByCategoriesCache.getIfPresent(key);
    }

    @Override
    public void cacheStatisticsByCategories(String entityType, LocalDate startDate, LocalDate endDate, Map<String, Object> statistics) {
        String key = buildStatisticsByCategoriesKey(entityType, startDate, endDate);
        statisticsByCategoriesCache.put(key, statistics);
        log.debug("Cached statistics by categories for entity: {} from {} to {}", entityType, startDate, endDate);
    }

    @Override
    public Map<String, Object> getSystemMetrics(String category, LocalDate date) {
        String key = buildSystemMetricsKey(category, date);
        return systemMetricsCache.getIfPresent(key);
    }

    @Override
    public void cacheSystemMetrics(String category, LocalDate date, Map<String, Object> metrics) {
        String key = buildSystemMetricsKey(category, date);
        systemMetricsCache.put(key, metrics);
        log.debug("Cached system metrics for category: {} date: {}", category, date);
    }

    @Override
    public Map<String, Object> getAnalyticsReport(String reportKey) {
        return analyticsReportCache.getIfPresent(reportKey);
    }

    @Override
    public void cacheAnalyticsReport(String reportKey, Map<String, Object> report) {
        analyticsReportCache.put(reportKey, report);
        log.debug("Cached analytics report with key: {}", reportKey);
    }

    @Override
    public byte[] getReportData(String reportKey) {
        return reportDataCache.getIfPresent(reportKey);
    }

    @Override
    public void cacheReportData(String reportKey, byte[] data) {
        reportDataCache.put(reportKey, data);
        log.debug("Cached report data with key: {} size: {} bytes", reportKey, data.length);
    }

    @Override
    public void invalidateUserCache(UUID userId) {
        String pattern = "user_stats:" + userId + ":*";
        clearCacheByPattern(pattern);
        log.info("Invalidated cache for user: {}", userId);
    }

    @Override
    public void invalidateQuestCache(UUID questId) {
        String pattern = "quest_stats:" + questId + ":*";
        clearCacheByPattern(pattern);
        log.info("Invalidated cache for quest: {}", questId);
    }

    @Override
    public void invalidateTeamCache(UUID teamId) {
        String pattern = "team_stats:" + teamId + ":*";
        clearCacheByPattern(pattern);
        log.info("Invalidated cache for team: {}", teamId);
    }

    @Override
    public void invalidateLeaderboardCache(String type, String period, LocalDate date) {
        String key = buildLeaderboardKey(type, period, date);
        leaderboardCache.invalidate(key);
        log.info("Invalidated leaderboard cache for type: {} period: {} date: {}", type, period, date);
    }

    @Override
    public void invalidateDateCache(LocalDate date) {
        // Инвалидируем все кэши, связанные с конкретной датой
        String dateStr = date.toString();
        
        userStatisticsCache.asMap().keySet().removeIf(key -> key.endsWith(":" + dateStr));
        questStatisticsCache.asMap().keySet().removeIf(key -> key.endsWith(":" + dateStr));
        teamStatisticsCache.asMap().keySet().removeIf(key -> key.endsWith(":" + dateStr));
        leaderboardCache.asMap().keySet().removeIf(key -> key.endsWith(":" + dateStr));
        topRecordsCache.asMap().keySet().removeIf(key -> key.endsWith(":" + dateStr));
        systemMetricsCache.asMap().keySet().removeIf(key -> key.endsWith(":" + dateStr));
        
        log.info("Invalidated all cache entries for date: {}", date);
    }

    @Override
    public void invalidateAllStatisticsCache() {
        userStatisticsCache.invalidateAll();
        questStatisticsCache.invalidateAll();
        teamStatisticsCache.invalidateAll();
        leaderboardCache.invalidateAll();
        topRecordsCache.invalidateAll();
        platformOverviewCache.invalidateAll();
        metricTrendsCache.invalidateAll();
        statisticsByCategoriesCache.invalidateAll();
        systemMetricsCache.invalidateAll();
        
        log.info("Invalidated all statistics cache");
    }

    @Override
    public void invalidateAnalyticsCache() {
        analyticsReportCache.invalidateAll();
        log.info("Invalidated analytics cache");
    }

    @Override
    public void invalidateReportsCache() {
        reportDataCache.invalidateAll();
        log.info("Invalidated reports cache");
    }

    @Override
    public void cleanupExpiredCache() {
        // Caffeine автоматически очищает истекшие записи, но можно принудительно вызвать
        userStatisticsCache.cleanUp();
        questStatisticsCache.cleanUp();
        teamStatisticsCache.cleanUp();
        leaderboardCache.cleanUp();
        topRecordsCache.cleanUp();
        platformOverviewCache.cleanUp();
        metricTrendsCache.cleanUp();
        statisticsByCategoriesCache.cleanUp();
        systemMetricsCache.cleanUp();
        analyticsReportCache.cleanUp();
        reportDataCache.cleanUp();
        
        log.info("Cleaned up expired cache entries");
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = Map.ofEntries(
                Map.entry("userStatistics", getCacheStats(userStatisticsCache)),
                Map.entry("questStatistics", getCacheStats(questStatisticsCache)),
                Map.entry("teamStatistics", getCacheStats(teamStatisticsCache)),
                Map.entry("leaderboard", getCacheStats(leaderboardCache)),
                Map.entry("topRecords", getCacheStats(topRecordsCache)),
                Map.entry("platformOverview", getCacheStats(platformOverviewCache)),
                Map.entry("metricTrends", getCacheStats(metricTrendsCache)),
                Map.entry("statisticsByCategories", getCacheStats(statisticsByCategoriesCache)),
                Map.entry("systemMetrics", getCacheStats(systemMetricsCache)),
                Map.entry("analyticsReport", getCacheStats(analyticsReportCache)),
                Map.entry("reportData", getCacheStats(reportDataCache))
        );
        
        return stats;
    }

    @Override
    public void warmupCache() {
        log.info("Starting cache warmup");
        
        // Здесь можно добавить логику предварительного прогрева кэша
        // Например, загрузить популярные данные
        
        log.info("Cache warmup completed");
    }

    @Override
    public boolean existsInCache(String key) {
        return userStatisticsCache.getIfPresent(key) != null ||
               questStatisticsCache.getIfPresent(key) != null ||
               teamStatisticsCache.getIfPresent(key) != null ||
               leaderboardCache.getIfPresent(key) != null ||
               topRecordsCache.getIfPresent(key) != null ||
               platformOverviewCache.getIfPresent(key) != null ||
               metricTrendsCache.getIfPresent(key) != null ||
               statisticsByCategoriesCache.getIfPresent(key) != null ||
               systemMetricsCache.getIfPresent(key) != null ||
               analyticsReportCache.getIfPresent(key) != null ||
               reportDataCache.getIfPresent(key) != null;
    }

    @Override
    public long getCacheSize() {
        return userStatisticsCache.estimatedSize() +
               questStatisticsCache.estimatedSize() +
               teamStatisticsCache.estimatedSize() +
               leaderboardCache.estimatedSize() +
               topRecordsCache.estimatedSize() +
               platformOverviewCache.estimatedSize() +
               metricTrendsCache.estimatedSize() +
               statisticsByCategoriesCache.estimatedSize() +
               systemMetricsCache.estimatedSize() +
               analyticsReportCache.estimatedSize() +
               reportDataCache.estimatedSize();
    }

    @Override
    public long getCacheEntryCount() {
        return getCacheSize();
    }

    @Override
    public void clearCacheByPattern(String pattern) {
        Pattern regex = Pattern.compile(pattern.replace("*", ".*"));
        
        userStatisticsCache.asMap().keySet().removeIf(key -> regex.matcher(key).matches());
        questStatisticsCache.asMap().keySet().removeIf(key -> regex.matcher(key).matches());
        teamStatisticsCache.asMap().keySet().removeIf(key -> regex.matcher(key).matches());
        leaderboardCache.asMap().keySet().removeIf(key -> regex.matcher(key).matches());
        topRecordsCache.asMap().keySet().removeIf(key -> regex.matcher(key).matches());
        platformOverviewCache.asMap().keySet().removeIf(key -> regex.matcher(key).matches());
        metricTrendsCache.asMap().keySet().removeIf(key -> regex.matcher(key).matches());
        statisticsByCategoriesCache.asMap().keySet().removeIf(key -> regex.matcher(key).matches());
        systemMetricsCache.asMap().keySet().removeIf(key -> regex.matcher(key).matches());
        analyticsReportCache.asMap().keySet().removeIf(key -> regex.matcher(key).matches());
        reportDataCache.asMap().keySet().removeIf(key -> regex.matcher(key).matches());
        
        log.debug("Cleared cache entries matching pattern: {}", pattern);
    }

    // Вспомогательные методы для построения ключей
    private String buildUserStatsKey(UUID userId, LocalDate date) {
        return "user_stats:" + userId + ":" + date;
    }

    private String buildQuestStatsKey(UUID questId, LocalDate date) {
        return "quest_stats:" + questId + ":" + date;
    }

    private String buildTeamStatsKey(UUID teamId, LocalDate date) {
        return "team_stats:" + teamId + ":" + date;
    }

    private String buildLeaderboardKey(String type, String period, LocalDate date) {
        return "leaderboard:" + type + ":" + period + ":" + (date != null ? date : "all_time");
    }

    private String buildTopRecordsKey(String entityType, String metric, LocalDate date) {
        return "top_records:" + entityType + ":" + metric + ":" + date;
    }

    private String buildPlatformOverviewKey(LocalDate startDate, LocalDate endDate) {
        return "platform_overview:" + startDate + ":" + endDate;
    }

    private String buildMetricTrendsKey(List<String> metrics, String period, int periods) {
        return "metric_trends:" + String.join(",", metrics) + ":" + period + ":" + periods;
    }

    private String buildStatisticsByCategoriesKey(String entityType, LocalDate startDate, LocalDate endDate) {
        return "stats_by_categories:" + entityType + ":" + startDate + ":" + endDate;
    }

    private String buildSystemMetricsKey(String category, LocalDate date) {
        return "system_metrics:" + category + ":" + date;
    }

    private Map<String, Object> getCacheStats(Cache<?, ?> cache) {
        CacheStats stats = cache.stats();
        return Map.of(
            "size", cache.estimatedSize(),
            "hitCount", stats.hitCount(),
            "missCount", stats.missCount(),
            "hitRate", stats.hitRate(),
            "evictionCount", stats.evictionCount(),
            "loadTime", stats.totalLoadTime()
        );
    }
}