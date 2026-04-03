package dn.quest.statistics.service;

import dn.quest.statistics.dto.AnalyticsReportDTO;
import dn.quest.statistics.dto.StatisticsRequestDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для аналитики и отчетов
 */
public interface AnalyticsService {

    /**
     * Сгенерировать аналитический отчет
     */
    AnalyticsReportDTO generateAnalyticsReport(StatisticsRequestDTO request);

    /**
     * Получить отчет по вовлеченности пользователей
     */
    Map<String, Object> getUserEngagementReport(LocalDate startDate, LocalDate endDate, String groupBy);

    /**
     * Получить отчет по производительности квестов
     */
    Map<String, Object> getQuestPerformanceReport(LocalDate startDate, LocalDate endDate, String category, UUID authorId);

    /**
     * Получить отчет по игровым сессиям
     */
    Map<String, Object> getGameSessionReport(LocalDate startDate, LocalDate endDate, Long questId, UUID userId);

    /**
     * Получить отчет по командной активности
     */
    Map<String, Object> getTeamActivityReport(LocalDate startDate, LocalDate endDate, String teamType);

    /**
     * Получить прогнозы и тренды
     */
    Map<String, Object> getForecasts(String forecastType, int periodDays, double confidenceLevel);

    /**
     * Получить когортный анализ
     */
    Map<String, Object> getCohortAnalysis(LocalDate startDate, LocalDate endDate, String cohortSize);

    /**
     * Получить воронку конверсии
     */
    Map<String, Object> getConversionFunnel(LocalDate startDate, LocalDate endDate, String funnelType);

    /**
     * Получить сегментацию пользователей
     */
    Map<String, Object> getUserSegmentation(String segmentationType, LocalDate startDate, LocalDate endDate);

    /**
     * Получить доступные типы отчетов
     */
    List<Map<String, String>> getAvailableReportTypes();

    /**
     * Получить метаданные для отчетов
     */
    Map<String, Object> getReportsMetadata();

    /**
     * Получить KPI метрики
     */
    Map<String, Object> getKpiMetrics(LocalDate startDate, LocalDate endDate);

    /**
     * Получить отчет по удержанию пользователей
     */
    Map<String, Object> getUserRetentionReport(LocalDate startDate, LocalDate endDate, String period);

    /**
     * Получить отчет по монетизации
     */
    Map<String, Object> getMonetizationReport(LocalDate startDate, LocalDate endDate);

    /**
     * Получить отчет по производительности системы
     */
    Map<String, Object> getSystemPerformanceReport(LocalDate startDate, LocalDate endDate);

    /**
     * Получить сравнительный анализ
     */
    Map<String, Object> getComparativeAnalysis(LocalDate startDate1, LocalDate endDate1, 
                                             LocalDate startDate2, LocalDate endDate2, String metricType);

    /**
     * Получить тепловую карту активности
     */
    Map<String, Object> getActivityHeatmap(LocalDate startDate, LocalDate endDate, String granularity);

    /**
     * Получить анализ паттернов поведения
     */
    Map<String, Object> getBehavioralPatternsAnalysis(LocalDate startDate, LocalDate endDate, UUID userId);

    /**
     * Получить прогноз оттока пользователей
     */
    Map<String, Object> getChurnPredictionReport(LocalDate startDate, LocalDate endDate);

    /**
     * Получить анализ эффективности контента
     */
    Map<String, Object> getContentEffectivenessReport(LocalDate startDate, LocalDate endDate, String contentType);

    /**
     * Получить отчет по A/B тестам
     */
    Map<String, Object> getAbTestReport(String testId, LocalDate startDate, LocalDate endDate);

    /**
     * Получить анализ пользовательского пути
     */
    Map<String, Object> getUserJourneyAnalysis(LocalDate startDate, LocalDate endDate, String journeyType);

    /**
     * Получить отчет по демографии
     */
    Map<String, Object> getDemographicsReport(LocalDate startDate, LocalDate endDate);

    /**
     * Получить анализ геолокации
     */
    Map<String, Object> getGeographicAnalysis(LocalDate startDate, LocalDate endDate);

    /**
     * Получить отчет по устройствам
     */
    Map<String, Object> getDeviceAnalyticsReport(LocalDate startDate, LocalDate endDate);

    /**
     * Получить анализ временных паттернов
     */
    Map<String, Object> getTemporalPatternsAnalysis(LocalDate startDate, LocalDate endDate, String timeUnit);

    /**
     * Получить анализ корреляций
     */
    Map<String, Object> getCorrelationAnalysis(LocalDate startDate, LocalDate endDate, List<String> metrics);

    /**
     * Получить анализ выбросов
     */
    Map<String, Object> getOutlierAnalysis(LocalDate startDate, LocalDate endDate, String metric);

    /**
     * Получить статистическую сводку
     */
    Map<String, Object> getStatisticalSummary(LocalDate startDate, LocalDate endDate, List<String> metrics);
}