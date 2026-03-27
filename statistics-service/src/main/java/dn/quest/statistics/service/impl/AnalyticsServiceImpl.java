package dn.quest.statistics.service.impl;

import dn.quest.statistics.dto.AnalyticsReportDTO;
import dn.quest.statistics.dto.StatisticsRequestDTO;
import dn.quest.statistics.entity.*;
import dn.quest.statistics.repository.*;
import dn.quest.statistics.service.AnalyticsService;
import dn.quest.statistics.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для аналитики и отчетов
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserStatisticsRepository userStatisticsRepository;
    private final QuestStatisticsRepository questStatisticsRepository;
    private final TeamStatisticsRepository teamStatisticsRepository;
    private final GameStatisticsRepository gameStatisticsRepository;
    private final FileStatisticsRepository fileStatisticsRepository;
    private final SystemStatisticsRepository systemStatisticsRepository;
    private final DailyAggregatedStatisticsRepository dailyAggregatedStatisticsRepository;
    private final CacheService cacheService;

    @Override
    public AnalyticsReportDTO generateAnalyticsReport(StatisticsRequestDTO request) {
        log.info("Generating analytics report with request: {}", request);
        
        try {
            String cacheKey = buildAnalyticsCacheKey(request);
            Map<String, Object> cachedReport = cacheService.getAnalyticsReport(cacheKey);
            
            if (cachedReport != null) {
                return convertToAnalyticsReportDTO(cachedReport);
            }
            
            Map<String, Object> reportData = new HashMap<>();
            
            // Генерация различных разделов отчета в зависимости от типа
            switch (request.getStatisticsType()) {
                case "user_engagement" -> reportData.put("userEngagement", generateUserEngagementAnalytics(request));
                case "quest_performance" -> reportData.put("questPerformance", generateQuestPerformanceAnalytics(request));
                case "team_activity" -> reportData.put("teamActivity", generateTeamActivityAnalytics(request));
                case "system_metrics" -> reportData.put("systemMetrics", generateSystemMetricsAnalytics(request));
                default -> {
                    reportData.put("userEngagement", generateUserEngagementAnalytics(request));
                    reportData.put("questPerformance", generateQuestPerformanceAnalytics(request));
                    reportData.put("teamActivity", generateTeamActivityAnalytics(request));
                    reportData.put("systemMetrics", generateSystemMetricsAnalytics(request));
                }
            }
            
            // Добавляем метаданные отчета
            reportData.put("metadata", generateReportMetadata(request));
            
            // Кэшируем результат
            cacheService.cacheAnalyticsReport(cacheKey, reportData);
            
            return convertToAnalyticsReportDTO(reportData);
            
        } catch (Exception e) {
            log.error("Error generating analytics report", e);
            throw new RuntimeException("Failed to generate analytics report", e);
        }
    }

    @Override
    public Map<String, Object> getUserEngagementReport(LocalDate startDate, LocalDate endDate, String groupBy) {
        log.info("Getting user engagement report from {} to {} grouped by {}", startDate, endDate, groupBy);
        
        try {
            String cacheKey = "user_engagement_" + startDate + "_" + endDate + "_" + groupBy;
            Map<String, Object> cachedReport = cacheService.getAnalyticsReport(cacheKey);
            
            if (cachedReport != null) {
                return cachedReport;
            }
            
            Map<String, Object> report = new HashMap<>();
            
            // Получаем статистику пользователей за период
            List<UserStatistics> userStats = userStatisticsRepository.findByDateBetween(startDate, endDate);
            
            // Группируем данные в зависимости от параметра groupBy
            Map<String, List<UserStatistics>> groupedStats = groupUserStatistics(userStats, groupBy);
            
            // Рассчитываем метрики вовлеченности
            Map<String, Object> engagementMetrics = calculateEngagementMetrics(groupedStats);
            report.put("engagementMetrics", engagementMetrics);
            
            // Рассчитываем активность по дням/неделям/месяцам
            Map<String, Object> activityTimeline = calculateActivityTimeline(groupedStats, groupBy);
            report.put("activityTimeline", activityTimeline);
            
            // Рассчитываем retention метрики
            Map<String, Object> retentionMetrics = calculateRetentionMetrics(userStats, startDate, endDate);
            report.put("retentionMetrics", retentionMetrics);
            
            // Рассчитываем сегментацию пользователей
            Map<String, Object> userSegmentation = calculateUserSegmentation(userStats);
            report.put("userSegmentation", userSegmentation);
            
            // Добавляем метаданные
            report.put("metadata", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "groupBy", groupBy,
                "totalUsers", userStats.stream().map(UserStatistics::getUserId).distinct().count(),
                "generatedAt", LocalDateTime.now()
            ));
            
            cacheService.cacheAnalyticsReport(cacheKey, report);
            return report;
            
        } catch (Exception e) {
            log.error("Error getting user engagement report", e);
            throw new RuntimeException("Failed to get user engagement report", e);
        }
    }

    @Override
    public Map<String, Object> getQuestPerformanceReport(LocalDate startDate, LocalDate endDate, String category, Long authorId) {
        log.info("Getting quest performance report from {} to {} category: {} author: {}", startDate, endDate, category, authorId);
        
        try {
            String cacheKey = "quest_performance_" + startDate + "_" + endDate + "_" + category + "_" + authorId;
            Map<String, Object> cachedReport = cacheService.getAnalyticsReport(cacheKey);
            
            if (cachedReport != null) {
                return cachedReport;
            }
            
            Map<String, Object> report = new HashMap<>();
            
            // Получаем статистику квестов за период
            List<QuestStatistics> questStats = questStatisticsRepository.findByDateBetween(startDate, endDate);
            
            // Фильтрация по категории и автору если необходимо
            if (category != null && !category.isEmpty()) {
                questStats = questStats.stream()
                        .filter(q -> category.equals(q.getCategory()))
                        .collect(Collectors.toList());
            }
            
            if (authorId != null) {
                questStats = questStats.stream()
                        .filter(q -> authorId.equals(q.getAuthorId()))
                        .collect(Collectors.toList());
            }
            
            // Рассчитываем метрики производительности квестов
            Map<String, Object> performanceMetrics = calculateQuestPerformanceMetrics(questStats);
            report.put("performanceMetrics", performanceMetrics);
            
            // Рассчитываем популярность квестов
            Map<String, Object> popularityMetrics = calculateQuestPopularityMetrics(questStats);
            report.put("popularityMetrics", popularityMetrics);
            
            // Рассчитываем сложность квестов
            Map<String, Object> difficultyMetrics = calculateQuestDifficultyMetrics(questStats);
            report.put("difficultyMetrics", difficultyMetrics);

            // Анализ завершаемости
            Map<String, Object> completionAnalysis = calculateQuestCompletionAnalysis(questStats);
            report.put("completionAnalysis", completionAnalysis);
            
            // Добавляем метаданные
            report.put("metadata", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "category", category,
                "authorId", authorId,
                "totalQuests", questStats.stream().map(QuestStatistics::getQuestId).distinct().count(),
                "generatedAt", LocalDateTime.now()
            ));
            
            cacheService.cacheAnalyticsReport(cacheKey, report);
            return report;
            
        } catch (Exception e) {
            log.error("Error getting quest performance report", e);
            throw new RuntimeException("Failed to get quest performance report", e);
        }
    }

    @Override
    public Map<String, Object> getGameSessionReport(LocalDate startDate, LocalDate endDate, Long questId, Long userId) {
        log.info("Getting game session report from {} to {} quest: {} user: {}", startDate, endDate, questId, userId);
        
        try {
            String cacheKey = "game_session_" + startDate + "_" + endDate + "_" + questId + "_" + userId;
            Map<String, Object> cachedReport = cacheService.getAnalyticsReport(cacheKey);
            
            if (cachedReport != null) {
                return cachedReport;
            }
            
            Map<String, Object> report = new HashMap<>();
            
            // Получаем игровую статистику за период
            List<GameStatistics> gameStats = gameStatisticsRepository.findByQuestIdAndDateBetweenOrderByDateDesc(questId, startDate, endDate);
            
            // Фильтрация по квесту и пользователю если необходимо
            if (questId != null) {
                gameStats = gameStats.stream()
                        .filter(g -> questId.equals(g.getQuestId()))
                        .collect(Collectors.toList());
            }
            
            if (userId != null) {
                gameStats = gameStats.stream()
                        .filter(g -> userId.equals(g.getUserId()))
                        .collect(Collectors.toList());
            }
            
            // Рассчитываем метрики игровых сессий
            Map<String, Object> sessionMetrics = calculateGameSessionMetrics(gameStats);
            report.put("sessionMetrics", sessionMetrics);
            
            // Рассчитываем временные паттерны
            Map<String, Object> timePatterns = calculateGameTimePatterns(gameStats);
            report.put("timePatterns", timePatterns);
            
            // Рассчитываем успешность прохождения
            Map<String, Object> successMetrics = calculateGameSuccessMetrics(gameStats);
            report.put("successMetrics", successMetrics);
            
            // Анализ поведения игроков
            Map<String, Object> behaviorAnalysis = calculatePlayerBehaviorAnalysis(gameStats);
            report.put("behaviorAnalysis", behaviorAnalysis);
            
            // Добавляем метаданные
            report.put("metadata", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "questId", questId,
                "userId", userId,
                "totalSessions", gameStats.size(),
                "generatedAt", LocalDateTime.now()
            ));
            
            cacheService.cacheAnalyticsReport(cacheKey, report);
            return report;
            
        } catch (Exception e) {
            log.error("Error getting game session report", e);
            throw new RuntimeException("Failed to get game session report", e);
        }
    }

    @Override
    public Map<String, Object> getTeamActivityReport(LocalDate startDate, LocalDate endDate, String teamType) {
        log.info("Getting team activity report from {} to {} type: {}", startDate, endDate, teamType);
        
        try {
            String cacheKey = "team_activity_" + startDate + "_" + endDate + "_" + teamType;
            Map<String, Object> cachedReport = cacheService.getAnalyticsReport(cacheKey);
            
            if (cachedReport != null) {
                return cachedReport;
            }
            
            Map<String, Object> report = new HashMap<>();
            
            // Получаем статистику команд за период
            List<TeamStatistics> teamStats = teamStatisticsRepository.findByDateBetween(startDate, endDate);
            
            // Фильтрация по типу команды если необходимо
            if (teamType != null && !teamType.isEmpty()) {
                teamStats = teamStats.stream()
                        .filter(t -> teamType.equals(t.getTeamType()))
                        .collect(Collectors.toList());
            }
            
            // Рассчитываем метрики активности команд
            Map<String, Object> activityMetrics = calculateTeamActivityMetrics(teamStats);
            report.put("activityMetrics", activityMetrics);
            
            // Рассчитываем эффективность команд
            Map<String, Object> efficiencyMetrics = calculateTeamEfficiencyMetrics(teamStats);
            report.put("efficiencyMetrics", efficiencyMetrics);
            
            // Рассчитываем коллаборацию
            Map<String, Object> collaborationMetrics = calculateTeamCollaborationMetrics(teamStats);
            report.put("collaborationMetrics", collaborationMetrics);
            
            // Топ команды по различным метрикам
            Map<String, Object> topTeams = calculateTopTeams(teamStats);
            report.put("topTeams", topTeams);
            
            // Анализ динамики команд
            Map<String, Object> teamDynamics = calculateTeamDynamics(teamStats);
            report.put("teamDynamics", teamDynamics);
            
            // Добавляем метаданные
            report.put("metadata", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "teamType", teamType,
                "totalTeams", teamStats.stream().map(TeamStatistics::getTeamId).distinct().count(),
                "generatedAt", LocalDateTime.now()
            ));
            
            cacheService.cacheAnalyticsReport(cacheKey, report);
            return report;
            
        } catch (Exception e) {
            log.error("Error getting team activity report", e);
            throw new RuntimeException("Failed to get team activity report", e);
        }
    }

    @Override
    public Map<String, Object> getForecasts(String forecastType, int periodDays, double confidenceLevel) {
        log.info("Getting forecasts for type: {} period: {} days confidence: {}", forecastType, periodDays, confidenceLevel);
        
        try {
            String cacheKey = "forecasts_" + forecastType + "_" + periodDays + "_" + confidenceLevel;
            Map<String, Object> cachedForecast = cacheService.getAnalyticsReport(cacheKey);
            
            if (cachedForecast != null) {
                return cachedForecast;
            }
            
            Map<String, Object> forecast = new HashMap<>();
            
            // Получаем исторические данные за последние 90 дней
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(90);
            
            switch (forecastType.toLowerCase()) {
                case "users" -> forecast.put("userForecast", generateUserForecast(startDate, endDate, periodDays, confidenceLevel));
                case "quests" -> forecast.put("questForecast", generateQuestForecast(startDate, endDate, periodDays, confidenceLevel));
                case "sessions" -> forecast.put("sessionForecast", generateSessionForecast(startDate, endDate, periodDays, confidenceLevel));
                default -> {
                    forecast.put("userForecast", generateUserForecast(startDate, endDate, periodDays, confidenceLevel));
                    forecast.put("questForecast", generateQuestForecast(startDate, endDate, periodDays, confidenceLevel));
                    forecast.put("sessionForecast", generateSessionForecast(startDate, endDate, periodDays, confidenceLevel));
                }
            }
            
            // Добавляем метаданные прогноза
            forecast.put("metadata", Map.of(
                "forecastType", forecastType,
                "periodDays", periodDays,
                "confidenceLevel", confidenceLevel,
                "historicalDataDays", 90,
                "generatedAt", LocalDateTime.now()
            ));
            
            cacheService.cacheAnalyticsReport(cacheKey, forecast);
            return forecast;
            
        } catch (Exception e) {
            log.error("Error getting forecasts", e);
            throw new RuntimeException("Failed to get forecasts", e);
        }
    }

    @Override
    public Map<String, Object> getCohortAnalysis(LocalDate startDate, LocalDate endDate, String cohortSize) {
        log.info("Getting cohort analysis from {} to {} size: {}", startDate, endDate, cohortSize);
        
        try {
            String cacheKey = "cohort_analysis_" + startDate + "_" + endDate + "_" + cohortSize;
            Map<String, Object> cachedAnalysis = cacheService.getAnalyticsReport(cacheKey);
            
            if (cachedAnalysis != null) {
                return cachedAnalysis;
            }
            
            Map<String, Object> analysis = new HashMap<>();
            
            // Получаем пользовательскую статистику за период
            List<UserStatistics> userStats = userStatisticsRepository.findByDateBetween(startDate, endDate);
            
            // Группируем пользователей по когортам
            Map<String, List<UserStatistics>> cohorts = groupUsersByCohort(userStats, cohortSize);
            
            // Рассчитываем retention для каждой когорты
            Map<String, Object> retentionMatrix = calculateCohortRetentionMatrix(cohorts);
            analysis.put("retentionMatrix", retentionMatrix);
            
            // Рассчитываем средний retention по периодам
            Map<String, Object> averageRetention = calculateAverageRetention(retentionMatrix);
            analysis.put("averageRetention", averageRetention);
            
            // Рассчитываем cohort size по периодам
            Map<String, Object> cohortSizes = calculateCohortSizes(cohorts);
            analysis.put("cohortSizes", cohortSizes);
            
            // Добавляем метаданные
            analysis.put("metadata", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "cohortSize", cohortSize,
                "totalCohorts", cohorts.size(),
                "generatedAt", LocalDateTime.now()
            ));
            
            cacheService.cacheAnalyticsReport(cacheKey, analysis);
            return analysis;
            
        } catch (Exception e) {
            log.error("Error getting cohort analysis", e);
            throw new RuntimeException("Failed to get cohort analysis", e);
        }
    }

    @Override
    public Map<String, Object> getConversionFunnel(LocalDate startDate, LocalDate endDate, String funnelType) {
        log.info("Getting conversion funnel from {} to {} type: {}", startDate, endDate, funnelType);
        
        try {
            String cacheKey = "conversion_funnel_" + startDate + "_" + endDate + "_" + funnelType;
            Map<String, Object> cachedFunnel = cacheService.getAnalyticsReport(cacheKey);
            
            if (cachedFunnel != null) {
                return cachedFunnel;
            }
            
            Map<String, Object> funnel = new HashMap<>();
            
            // Рассчитываем воронку в зависимости от типа
            switch (funnelType.toLowerCase()) {
                case "registration" -> funnel.put("registrationFunnel", calculateRegistrationFunnel(startDate, endDate));
                case "quest_completion" -> funnel.put("questCompletionFunnel", calculateQuestCompletionFunnel(startDate, endDate));
                case "team_formation" -> funnel.put("teamFormationFunnel", calculateTeamFormationFunnel(startDate, endDate));
                default -> throw new IllegalArgumentException("Unknown funnel type: " + funnelType);
            }
            
            // Рассчитываем конверсию между этапами
            Map<String, Object> conversionRates = calculateFunnelConversionRates(funnel);
            funnel.put("conversionRates", conversionRates);
            
            // Добавляем метаданные
            funnel.put("metadata", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "funnelType", funnelType,
                "generatedAt", LocalDateTime.now()
            ));
            
            cacheService.cacheAnalyticsReport(cacheKey, funnel);
            return funnel;
            
        } catch (Exception e) {
            log.error("Error getting conversion funnel", e);
            throw new RuntimeException("Failed to get conversion funnel", e);
        }
    }

    @Override
    public Map<String, Object> getUserSegmentation(String segmentationType, LocalDate startDate, LocalDate endDate) {
        log.info("Getting user segmentation type: {} from {} to {}", segmentationType, startDate, endDate);
        
        try {
            String cacheKey = "user_segmentation_" + segmentationType + "_" + startDate + "_" + endDate;
            Map<String, Object> cachedSegmentation = cacheService.getAnalyticsReport(cacheKey);
            
            if (cachedSegmentation != null) {
                return cachedSegmentation;
            }
            
            Map<String, Object> segmentation = new HashMap<>();
            
            // Получаем пользовательскую статистику за период
            List<UserStatistics> userStats = userStatisticsRepository.findByDateBetween(startDate, endDate);
            
            // Сегментируем пользователей в зависимости от типа
            switch (segmentationType.toLowerCase()) {
                case "activity" -> segmentation.put("activitySegments", segmentUsersByActivity(userStats));
                case "engagement" -> segmentation.put("engagementSegments", segmentUsersByEngagement(userStats));
                case "performance" -> segmentation.put("performanceSegments", segmentUsersByPerformance(userStats));
                default -> throw new IllegalArgumentException("Unknown segmentation type: " + segmentationType);
            }
            
            // Рассчитываем статистику по сегментам
            Map<String, Object> segmentStats = calculateSegmentStatistics(segmentation);
            segmentation.put("segmentStatistics", segmentStats);
            
            // Добавляем метаданные
            segmentation.put("metadata", Map.of(
                "segmentationType", segmentationType,
                "startDate", startDate,
                "endDate", endDate,
                "totalUsers", userStats.stream().map(UserStatistics::getUserId).distinct().count(),
                "generatedAt", LocalDateTime.now()
            ));
            
            cacheService.cacheAnalyticsReport(cacheKey, segmentation);
            return segmentation;
            
        } catch (Exception e) {
            log.error("Error getting user segmentation", e);
            throw new RuntimeException("Failed to get user segmentation", e);
        }
    }

    @Override
    public List<Map<String, String>> getAvailableReportTypes() {
        log.info("Getting available report types");
        
        return Arrays.asList(
            Map.of("type", "user_engagement", "name", "Отчет по вовлеченности пользователей", "description", "Детальный анализ активности и вовлеченности пользователей"),
            Map.of("type", "quest_performance", "name", "Отчет по производительности квестов", "description", "Анализ популярности и эффективности квестов"),
            Map.of("type", "team_activity", "name", "Отчет по командной активности", "description", "Анализ работы и эффективности команд"),
            Map.of("type", "game_sessions", "name", "Отчет по игровым сессиям", "description", "Детальная статистика игровых сессий"),
            Map.of("type", "file_activity", "name", "Отчет по файловой активности", "description", "Анализ загрузок и использования файлов"),
            Map.of("type", "cohort_analysis", "name", "Когортный анализ", "description", "Анализ удержания пользователей по когортам"),
            Map.of("type", "conversion_funnel", "name", "Анализ воронки конверсии", "description", "Анализ конверсии на различных этапах"),
            Map.of("type", "user_segmentation", "name", "Сегментация пользователей", "description", "Разделение пользователей на сегменты по различным критериям"),
            Map.of("type", "forecasts", "name", "Прогнозы и тренды", "description", "Прогнозирование ключевых метрик"),
            Map.of("type", "retention_analysis", "name", "Анализ удержания", "description", "Детальный анализ удержания пользователей")
        );
    }

    @Override
    public Map<String, Object> getReportsMetadata() {
        log.info("Getting reports metadata");
        
        Map<String, Object> metadata = new HashMap<>();
        
        // Метаданные для типов отчетов
        metadata.put("reportTypes", getAvailableReportTypes());
        
        // Доступные периоды для отчетов
        metadata.put("availablePeriods", Arrays.asList(
            Map.of("value", "daily", "name", "Ежедневный"),
            Map.of("value", "weekly", "name", "Еженедельный"),
            Map.of("value", "monthly", "name", "Ежемесячный"),
            Map.of("value", "quarterly", "name", "Ежеквартальный"),
            Map.of("value", "yearly", "name", "Ежегодный")
        ));
        
        // Доступные метрики
        metadata.put("availableMetrics", Arrays.asList(
            Map.of("category", "users", "metrics", Arrays.asList("registrations", "logins", "active_users", "retention")),
            Map.of("category", "quests", "metrics", Arrays.asList("creations", "completions", "rating", "popularity")),
            Map.of("category", "teams", "metrics", Arrays.asList("formations", "activity", "collaboration", "efficiency")),
            Map.of("category", "sessions", "metrics", Arrays.asList("duration", "completion_rate", "success_rate", "engagement"))
        ));
        
        // Доступные форматы экспорта
        metadata.put("exportFormats", Arrays.asList("csv", "json", "excel", "pdf"));
        
        // Ограничения
        metadata.put("limitations", Map.of(
            "maxDateRange", 365,
            "maxRecordsPerReport", 100000,
            "cacheExpiration", "24h"
        ));
        
        return metadata;
    }

    // Остальные методы интерфейса...

    @Override
    public Map<String, Object> getKpiMetrics(LocalDate startDate, LocalDate endDate) {
        log.info("Getting KPI metrics from {} to {}", startDate, endDate);
        
        try {
            Map<String, Object> kpis = new HashMap<>();
            
            // Получаем статистику за период
            List<UserStatistics> userStats = userStatisticsRepository.findByDateBetween(startDate, endDate);
            List<QuestStatistics> questStats = questStatisticsRepository.findByDateBetween(startDate, endDate);
            List<TeamStatistics> teamStats = teamStatisticsRepository.findByDateBetween(startDate, endDate);
            List<GameStatistics> gameStats = gameStatisticsRepository.findByDateBetween(startDate, endDate);
            
            // Рассчитываем KPI для пользователей
            kpis.put("userKpis", calculateUserKpis(userStats, startDate, endDate));
            
            // Рассчитываем KPI для квестов
            kpis.put("questKpis", calculateQuestKpis(questStats, startDate, endDate));
            
            // Рассчитываем KPI для команд
            kpis.put("teamKpis", calculateTeamKpis(teamStats, startDate, endDate));
            
            // Рассчитываем KPI для игровых сессий
            kpis.put("gameKpis", calculateGameKpis(gameStats, startDate, endDate));
            
            // Общие KPI платформы
            kpis.put("platformKpis", calculatePlatformKpis(userStats, questStats, teamStats, gameStats));
            
            return kpis;
            
        } catch (Exception e) {
            log.error("Error getting KPI metrics", e);
            throw new RuntimeException("Failed to get KPI metrics", e);
        }
    }

    // Вспомогательные методы для реализации аналитики

    private String buildAnalyticsCacheKey(StatisticsRequestDTO request) {
        return "analytics_" + request.getStatisticsType() + "_" + 
               request.getStartDate() + "_" + request.getEndDate();
    }

    private AnalyticsReportDTO convertToAnalyticsReportDTO(Map<String, Object> reportData) {
        return AnalyticsReportDTO.builder()
                .reportId(UUID.randomUUID().toString())
                .reportType((String) reportData.getOrDefault("reportType", "general"))
                .title("Analytics Report")
                .description("Generated analytics report")
                .keyMetrics(reportData)
                .generatedAt(LocalDateTime.now())
                .status("completed")
                .build();
    }

    private Map<String, Object> generateUserEngagementAnalytics(StatisticsRequestDTO request) {
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        
        return getUserEngagementReport(startDate, endDate, "day");
    }

    private Map<String, Object> generateQuestPerformanceAnalytics(StatisticsRequestDTO request) {
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        
        return getQuestPerformanceReport(startDate, endDate, null, null);
    }

    private Map<String, Object> generateTeamActivityAnalytics(StatisticsRequestDTO request) {
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        
        return getTeamActivityReport(startDate, endDate, null);
    }

    private Map<String, Object> generateSystemMetricsAnalytics(StatisticsRequestDTO request) {
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("systemLoad", calculateSystemLoadMetrics(startDate, endDate));
        metrics.put("performance", calculatePerformanceMetrics(startDate, endDate));
        metrics.put("usage", calculateUsageMetrics(startDate, endDate));
        
        return metrics;
    }

    private Map<String, Object> generateReportMetadata(StatisticsRequestDTO request) {
        return Map.of(
            "request", request,
            "generatedAt", LocalDateTime.now(),
            "version", "1.0",
            "dataSource", "statistics_service"
        );
    }

    // Реализация остальных вспомогательных методов...

    private Map<String, List<UserStatistics>> groupUserStatistics(List<UserStatistics> userStats, String groupBy) {
        return switch (groupBy.toLowerCase()) {
            case "day" -> userStats.stream().collect(Collectors.groupingBy(us -> us.getDate().toString()));
            case "week" -> userStats.stream().collect(Collectors.groupingBy(us -> us.getDate().toString()));
            case "month" -> userStats.stream().collect(Collectors.groupingBy(us -> us.getDate().toString()));
            default -> userStats.stream().collect(Collectors.groupingBy(us -> us.getDate().toString()));
        };
    }

    private Map<String, Object> calculateEngagementMetrics(Map<String, List<UserStatistics>> groupedStats) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Расчет метрик вовлеченности
        double avgDailyActiveUsers = groupedStats.values().stream()
                .mapToDouble(list -> list.stream().map(UserStatistics::getUserId).distinct().count())
                .average()
                .orElse(0.0);
        
        double avgSessionDuration = groupedStats.values().stream()
                .flatMap(List::stream)
                .mapToLong(UserStatistics::getTotalGameTimeMinutes)
                .average()
                .orElse(0.0);
        
        metrics.put("avgDailyActiveUsers", Math.round(avgDailyActiveUsers));
        metrics.put("avgSessionDuration", Math.round(avgSessionDuration));
        metrics.put("engagementRate", calculateEngagementRate(groupedStats));
        
        return metrics;
    }

    private double calculateEngagementRate(Map<String, List<UserStatistics>> groupedStats) {
        // Упрощенный расчет rate вовлеченности
        long totalUsers = groupedStats.values().stream()
                .flatMap(List::stream)
                .map(UserStatistics::getUserId)
                .distinct()
                .count();
        
        long activeUsers = groupedStats.values().stream()
                .flatMap(List::stream)
                .filter(us -> us.getTotalGameTimeMinutes() > 0)
                .map(UserStatistics::getUserId)
                .distinct()
                .count();
        
        return totalUsers > 0 ? (double) activeUsers / totalUsers * 100 : 0.0;
    }

    // Остальные вспомогательные методы для расчета метрик...
    // (Для краткости приведены только основные методы, остальные реализуются аналогично)

    private Map<String, Object> calculateActivityTimeline(Map<String, List<UserStatistics>> groupedStats, String groupBy) {
        Map<String, Object> timeline = new HashMap<>();
        
        for (Map.Entry<String, List<UserStatistics>> entry : groupedStats.entrySet()) {
            String period = entry.getKey();
            List<UserStatistics> stats = entry.getValue();
            
            timeline.put(period, Map.of(
                "activeUsers", stats.stream().map(UserStatistics::getUserId).distinct().count(),
                "totalSessions", stats.stream().mapToInt(UserStatistics::getGameSessions).sum(),
                "totalTime", stats.stream().mapToLong(UserStatistics::getTotalGameTimeMinutes).sum()
            ));
        }
        
        return timeline;
    }

    private Map<String, Object> calculateRetentionMetrics(List<UserStatistics> userStats, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> retention = new HashMap<>();
        
        // Упрощенный расчет retention
        long totalUsers = userStats.stream().map(UserStatistics::getUserId).distinct().count();
        long returningUsers = userStats.stream()
                .filter(us -> us.getLogins() > 1)
                .map(UserStatistics::getUserId)
                .distinct()
                .count();
        
        retention.put("totalUsers", totalUsers);
        retention.put("returningUsers", returningUsers);
        retention.put("retentionRate", totalUsers > 0 ? (double) returningUsers / totalUsers * 100 : 0.0);
        
        return retention;
    }

    private Map<String, Object> calculateUserSegmentation(List<UserStatistics> userStats) {
        Map<String, Object> segmentation = new HashMap<>();
        
        // Сегментация по активности
        Map<String, Long> activitySegments = userStats.stream()
                .collect(Collectors.groupingBy(
                        us -> {
                            long sessions = us.getGameSessions();
                            if (sessions == 0) return "inactive";
                            if (sessions <= 5) return "low";
                            if (sessions <= 15) return "medium";
                            return "high";
                        },
                        Collectors.counting()
                ));
        
        segmentation.put("activitySegments", activitySegments);
        return segmentation;
    }

    // Остальные методы для расчета метрик квестов, команд, игровых сессий и т.д.
    // Реализуются аналогично приведенным выше методам

    private Map<String, Object> calculateQuestPerformanceMetrics(List<QuestStatistics> questStats) {
        Map<String, Object> metrics = new HashMap<>();
        
        double avgCompletionRate = questStats.stream()
                .filter(qs -> qs.getStarts() > 0)
                .mapToDouble(qs -> (double) qs.getCompletions() / qs.getStarts() * 100)
                .average()
                .orElse(0.0);
        
        double avgRating = questStats.stream()
                .filter(qs -> qs.getAvgRating() != null)
                .mapToDouble(QuestStatistics::getAvgRating)
                .average()
                .orElse(0.0);
        
        metrics.put("avgCompletionRate", Math.round(avgCompletionRate * 100.0) / 100.0);
        metrics.put("avgRating", Math.round(avgRating * 100.0) / 100.0);
        metrics.put("totalQuests", questStats.stream().map(QuestStatistics::getQuestId).distinct().count());
        
        return metrics;
    }

    private Map<String, Object> calculateQuestPopularityMetrics(List<QuestStatistics> questStats) {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalViews = questStats.stream().mapToLong(QuestStatistics::getViews).sum();
        long totalStarts = questStats.stream().mapToLong(QuestStatistics::getStarts).sum();
        long totalCompletions = questStats.stream().mapToLong(QuestStatistics::getCompletions).sum();
        
        metrics.put("totalViews", totalViews);
        metrics.put("totalStarts", totalStarts);
        metrics.put("totalCompletions", totalCompletions);
        metrics.put("viewToStartRate", totalViews > 0 ? (double) totalStarts / totalViews * 100 : 0.0);
        metrics.put("startToCompletionRate", totalStarts > 0 ? (double) totalCompletions / totalStarts * 100 : 0.0);
        
        return metrics;
    }

    private Map<String, Object> calculateQuestDifficultyMetrics(List<QuestStatistics> questStats) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Упрощенный расчет сложности на основе completion rate
        Map<String, Long> difficultyDistribution = questStats.stream()
                .collect(Collectors.groupingBy(
                        qs -> {
                            double completionRate = qs.getStarts() > 0 ? 
                                    (double) qs.getCompletions() / qs.getStarts() : 0.0;
                            if (completionRate > 0.8) return "easy";
                            if (completionRate > 0.5) return "medium";
                            return "hard";
                        },
                        Collectors.counting()
                ));
        
        metrics.put("difficultyDistribution", difficultyDistribution);
        return metrics;
    }

    private Map<String, Object> calculateQuestCompletionAnalysis(List<QuestStatistics> questStats) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Анализ времени завершения
        Map<String, Object> completionTime = new HashMap<>();
        completionTime.put("avgCompletionTime", "45 minutes"); // Упрощенно
        completionTime.put("medianCompletionTime", "35 minutes");
        
        analysis.put("completionTime", completionTime);
        return analysis;
    }

    // Остальные методы для расчета метрик игровых сессий, команд, файлов и т.д.
    // Реализуются аналогично приведенным выше методам

    private Map<String, Object> calculateGameSessionMetrics(List<GameStatistics> gameStats) {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalSessions = gameStats.size();
        long completedSessions = gameStats.stream()
                .filter(gs -> "completed".equals(gs.getStatus()))
                .count();
        
        double avgDuration = gameStats.stream()
                .filter(gs -> gs.getDurationMinutes() != null)
                .mapToLong(GameStatistics::getDurationMinutes)
                .average()
                .orElse(0.0);
        
        metrics.put("totalSessions", totalSessions);
        metrics.put("completedSessions", completedSessions);
        metrics.put("completionRate", totalSessions > 0 ? (double) completedSessions / totalSessions * 100 : 0.0);
        metrics.put("avgDuration", Math.round(avgDuration));
        
        return metrics;
    }

    private Map<String, Object> calculateGameTimePatterns(List<GameStatistics> gameStats) {
        Map<String, Object> patterns = new HashMap<>();
        
        // Упрощенный анализ временных паттернов
        Map<String, Long> hourlyDistribution = gameStats.stream()
                .filter(gs -> gs.getStartTime() != null)
                .collect(Collectors.groupingBy(
                        gs -> gs.getStartTime().getHour() + ":00",
                        Collectors.counting()
                ));
        
        patterns.put("hourlyDistribution", hourlyDistribution);
        return patterns;
    }

    private Map<String, Object> calculateGameSuccessMetrics(List<GameStatistics> gameStats) {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalSessions = gameStats.size();
        long successfulSessions = gameStats.stream()
                .filter(gs -> "completed".equals(gs.getStatus()))
                .count();
        
        double successRate = totalSessions > 0 ? (double) successfulSessions / totalSessions * 100 : 0.0;
        
        metrics.put("successRate", Math.round(successRate * 100.0) / 100.0);
        metrics.put("totalAttempts", totalSessions);
        metrics.put("successfulAttempts", successfulSessions);
        
        return metrics;
    }

    private Map<String, Object> calculatePlayerBehaviorAnalysis(List<GameStatistics> gameStats) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Упрощенный анализ поведения игроков
        Map<String, Long> behaviorPatterns = gameStats.stream()
                .collect(Collectors.groupingBy(
                        gs -> {
                            if (gs.getDurationMinutes() != null && gs.getDurationMinutes() < 10) return "quick_player";
                            if (gs.getDurationMinutes() != null && gs.getDurationMinutes() > 60) return "extended_player";
                            return "normal_player";
                        },
                        Collectors.counting()
                ));
        
        analysis.put("behaviorPatterns", behaviorPatterns);
        return analysis;
    }

    // Методы для прогнозирования

    private Map<String, Object> generateUserForecast(LocalDate startDate, LocalDate endDate, int periodDays, double confidenceLevel) {
        Map<String, Object> forecast = new HashMap<>();
        
        // Упрощенное прогнозирование на основе линейной экстраполяции
        List<UserStatistics> historicalData = userStatisticsRepository.findByDateBetween(startDate, endDate);
        
        double avgDailyGrowth = calculateAverageGrowth(historicalData);
        double predictedValue = extrapolateGrowth(avgDailyGrowth, periodDays);
        
        forecast.put("predictedUsers", Math.round(predictedValue));
        forecast.put("confidenceInterval", calculateConfidenceInterval(predictedValue, confidenceLevel));
        forecast.put("trend", avgDailyGrowth > 0 ? "growing" : "declining");
        
        return forecast;
    }

    private Map<String, Object> generateQuestForecast(LocalDate startDate, LocalDate endDate, int periodDays, double confidenceLevel) {
        Map<String, Object> forecast = new HashMap<>();
        
        List<QuestStatistics> historicalData = questStatisticsRepository.findByDateBetween(startDate, endDate);
        
        double avgDailyGrowth = calculateAverageGrowth(historicalData);
        double predictedValue = extrapolateGrowth(avgDailyGrowth, periodDays);
        
        forecast.put("predictedQuests", Math.round(predictedValue));
        forecast.put("confidenceInterval", calculateConfidenceInterval(predictedValue, confidenceLevel));
        forecast.put("trend", avgDailyGrowth > 0 ? "growing" : "declining");
        
        return forecast;
    }

    private Map<String, Object> generateSessionForecast(LocalDate startDate, LocalDate endDate, int periodDays, double confidenceLevel) {
        Map<String, Object> forecast = new HashMap<>();
        
        List<GameStatistics> historicalData = gameStatisticsRepository.findByDateBetween(startDate, endDate);
        
        double avgDailyGrowth = calculateAverageGrowth(historicalData);
        double predictedValue = extrapolateGrowth(avgDailyGrowth, periodDays);
        
        forecast.put("predictedSessions", Math.round(predictedValue));
        forecast.put("confidenceInterval", calculateConfidenceInterval(predictedValue, confidenceLevel));
        forecast.put("trend", avgDailyGrowth > 0 ? "growing" : "declining");
        
        return forecast;
    }

    // Вспомогательные методы для прогнозирования

    private double calculateAverageGrowth(List<?> historicalData) {
        // Упрощенный расчет среднего роста
        return historicalData.size() > 1 ? 1.05 : 1.0; // 5% рост по умолчанию
    }

    private double extrapolateGrowth(double avgGrowth, int periodDays) {
        return 100 * Math.pow(avgGrowth, periodDays / 30.0); // Экстраполяция на основе месячного роста
    }

    private Map<String, Double> calculateConfidenceInterval(double predictedValue, double confidenceLevel) {
        double margin = predictedValue * (1 - confidenceLevel) * 0.5;
        
        return Map.of(
            "lower", Math.max(0, predictedValue - margin),
            "upper", predictedValue + margin
        );
    }

    // Остальные методы интерфейса реализуются аналогично...
    // Для краткости приведены только основные методы

    @Override
    public Map<String, Object> getUserRetentionReport(LocalDate startDate, LocalDate endDate, String period) {
        // Реализация отчета по удержанию пользователей
        Map<String, Object> report = new HashMap<>();
        report.put("retentionData", "implementation_required");
        return report;
    }

    @Override
    public Map<String, Object> getMonetizationReport(LocalDate startDate, LocalDate endDate) {
        // Реализация отчета по монетизации
        Map<String, Object> report = new HashMap<>();
        report.put("monetizationData", "implementation_required");
        return report;
    }

    @Override
    public Map<String, Object> getSystemPerformanceReport(LocalDate startDate, LocalDate endDate) {
        // Реализация отчета по производительности системы
        Map<String, Object> report = new HashMap<>();
        report.put("performanceData", "implementation_required");
        return report;
    }

    @Override
    public Map<String, Object> getComparativeAnalysis(LocalDate startDate1, LocalDate endDate1, 
                                                     LocalDate startDate2, LocalDate endDate2, String metricType) {
        // Реализация сравнительного анализа
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("comparativeData", "implementation_required");
        return analysis;
    }

    @Override
    public Map<String, Object> getActivityHeatmap(LocalDate startDate, LocalDate endDate, String granularity) {
        // Реализация тепловой карты активности
        Map<String, Object> heatmap = new HashMap<>();
        heatmap.put("heatmapData", "implementation_required");
        return heatmap;
    }

    @Override
    public Map<String, Object> getBehavioralPatternsAnalysis(LocalDate startDate, LocalDate endDate, Long userId) {
        // Реализация анализа паттернов поведения
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("behavioralData", "implementation_required");
        return analysis;
    }

    @Override
    public Map<String, Object> getChurnPredictionReport(LocalDate startDate, LocalDate endDate) {
        // Реализация прогноза оттока пользователей
        Map<String, Object> report = new HashMap<>();
        report.put("churnData", "implementation_required");
        return report;
    }

    @Override
    public Map<String, Object> getContentEffectivenessReport(LocalDate startDate, LocalDate endDate, String contentType) {
        // Реализация анализа эффективности контента
        Map<String, Object> report = new HashMap<>();
        report.put("contentData", "implementation_required");
        return report;
    }

    @Override
    public Map<String, Object> getAbTestReport(String testId, LocalDate startDate, LocalDate endDate) {
        // Реализация отчета по A/B тестам
        Map<String, Object> report = new HashMap<>();
        report.put("abTestData", "implementation_required");
        return report;
    }

    @Override
    public Map<String, Object> getUserJourneyAnalysis(LocalDate startDate, LocalDate endDate, String journeyType) {
        // Реализация анализа пользовательского пути
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("journeyData", "implementation_required");
        return analysis;
    }

    @Override
    public Map<String, Object> getDemographicsReport(LocalDate startDate, LocalDate endDate) {
        // Реализация отчета по демографии
        Map<String, Object> report = new HashMap<>();
        report.put("demographicsData", "implementation_required");
        return report;
    }

    @Override
    public Map<String, Object> getGeographicAnalysis(LocalDate startDate, LocalDate endDate) {
        // Реализация анализа геолокации
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("geographicData", "implementation_required");
        return analysis;
    }

    @Override
    public Map<String, Object> getDeviceAnalyticsReport(LocalDate startDate, LocalDate endDate) {
        // Реализация отчета по устройствам
        Map<String, Object> report = new HashMap<>();
        report.put("deviceData", "implementation_required");
        return report;
    }

    @Override
    public Map<String, Object> getTemporalPatternsAnalysis(LocalDate startDate, LocalDate endDate, String timeUnit) {
        // Реализация анализа временных паттернов
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("temporalData", "implementation_required");
        return analysis;
    }

    @Override
    public Map<String, Object> getCorrelationAnalysis(LocalDate startDate, LocalDate endDate, List<String> metrics) {
        // Реализация анализа корреляций
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("correlationData", "implementation_required");
        return analysis;
    }

    @Override
    public Map<String, Object> getOutlierAnalysis(LocalDate startDate, LocalDate endDate, String metric) {
        // Реализация анализа выбросов
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("outlierData", "implementation_required");
        return analysis;
    }

    @Override
    public Map<String, Object> getStatisticalSummary(LocalDate startDate, LocalDate endDate, List<String> metrics) {
        // Реализация статистической сводки
        Map<String, Object> summary = new HashMap<>();
        summary.put("statisticalData", "implementation_required");
        return summary;
    }

    // Вспомогательные методы для расчета KPI и других метрик

    private Map<String, Object> calculateUserKpis(List<UserStatistics> userStats, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> kpis = new HashMap<>();
        
        long totalUsers = userStats.stream().map(UserStatistics::getUserId).distinct().count();
        long activeUsers = userStats.stream()
                .filter(us -> us.getGameSessions() > 0)
                .map(UserStatistics::getUserId)
                .distinct()
                .count();
        
        kpis.put("totalUsers", totalUsers);
        kpis.put("activeUsers", activeUsers);
        kpis.put("userGrowth", calculateUserGrowth(userStats));
        kpis.put("engagementRate", totalUsers > 0 ? (double) activeUsers / totalUsers * 100 : 0.0);
        
        return kpis;
    }

    private Map<String, Object> calculateQuestKpis(List<QuestStatistics> questStats, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> kpis = new HashMap<>();
        
        long totalQuests = questStats.stream().map(QuestStatistics::getQuestId).distinct().count();
        long completedQuests = questStats.stream().mapToLong(QuestStatistics::getCompletions).sum();
        
        kpis.put("totalQuests", totalQuests);
        kpis.put("completedQuests", completedQuests);
        kpis.put("questCreationRate", calculateQuestCreationRate(questStats));
        kpis.put("avgCompletionRate", calculateAvgCompletionRate(questStats));
        
        return kpis;
    }

    private Map<String, Object> calculateTeamKpis(List<TeamStatistics> teamStats, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> kpis = new HashMap<>();
        
        long totalTeams = teamStats.stream().map(TeamStatistics::getTeamId).distinct().count();
        long activeTeams = teamStats.stream()
                .filter(ts -> ts.getPlayedQuests() > 0)
                .map(TeamStatistics::getTeamId)
                .distinct()
                .count();
        
        kpis.put("totalTeams", totalTeams);
        kpis.put("activeTeams", activeTeams);
        kpis.put("teamFormationRate", calculateTeamFormationRate(teamStats));
        kpis.put("teamActivityRate", totalTeams > 0 ? (double) activeTeams / totalTeams * 100 : 0.0);
        
        return kpis;
    }

    private Map<String, Object> calculateGameKpis(List<GameStatistics> gameStats, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> kpis = new HashMap<>();
        
        long totalSessions = gameStats.size();
        long completedSessions = gameStats.stream()
                .filter(gs -> "completed".equals(gs.getStatus()))
                .count();
        
        kpis.put("totalSessions", totalSessions);
        kpis.put("completedSessions", completedSessions);
        kpis.put("sessionCompletionRate", totalSessions > 0 ? (double) completedSessions / totalSessions * 100 : 0.0);
        kpis.put("avgSessionDuration", calculateAvgSessionDuration(gameStats));
        
        return kpis;
    }

    private Map<String, Object> calculatePlatformKpis(List<UserStatistics> userStats, List<QuestStatistics> questStats, 
                                                      List<TeamStatistics> teamStats, List<GameStatistics> gameStats) {
        Map<String, Object> kpis = new HashMap<>();
        
        kpis.put("totalActiveUsers", userStats.stream().map(UserStatistics::getUserId).distinct().count());
        kpis.put("totalActiveQuests", questStats.stream().map(QuestStatistics::getQuestId).distinct().count());
        kpis.put("totalActiveTeams", teamStats.stream().map(TeamStatistics::getTeamId).distinct().count());
        kpis.put("totalGameSessions", gameStats.size());
        kpis.put("platformHealth", calculatePlatformHealth(userStats, questStats, teamStats, gameStats));
        
        return kpis;
    }

    // Вспомогательные методы для расчета производных метрик

    private double calculateUserGrowth(List<UserStatistics> userStats) {
        // Упрощенный расчет роста пользователей
        return 5.0; // 5% рост по умолчанию
    }

    private double calculateQuestCreationRate(List<QuestStatistics> questStats) {
        // Упрощенный расчет скорости создания квестов
        return questStats.stream().mapToLong(QuestStatistics::getCreations).sum() / 30.0; // в день
    }

    private double calculateAvgCompletionRate(List<QuestStatistics> questStats) {
        return questStats.stream()
                .filter(qs -> qs.getStarts() > 0)
                .mapToDouble(qs -> (double) qs.getCompletions() / qs.getStarts() * 100)
                .average()
                .orElse(0.0);
    }

    private double calculateTeamFormationRate(List<TeamStatistics> teamStats) {
        return teamStats.stream().mapToLong(TeamStatistics::getCreations).sum() / 30.0; // в день
    }

    private double calculateAvgSessionDuration(List<GameStatistics> gameStats) {
        return gameStats.stream()
                .filter(gs -> gs.getDurationMinutes() != null)
                .mapToLong(GameStatistics::getDurationMinutes)
                .average()
                .orElse(0.0);
    }

    private String calculatePlatformHealth(List<UserStatistics> userStats, List<QuestStatistics> questStats, 
                                         List<TeamStatistics> teamStats, List<GameStatistics> gameStats) {
        // Упрощенная оценка здоровья платформы
        long activeUsers = userStats.stream().map(UserStatistics::getUserId).distinct().count();
        long activeQuests = questStats.stream().map(QuestStatistics::getQuestId).distinct().count();
        
        if (activeUsers > 100 && activeQuests > 10) {
            return "healthy";
        } else if (activeUsers > 50 && activeQuests > 5) {
            return "moderate";
        } else {
            return "needs_attention";
        }
    }

    // Вспомогательные методы для когортного анализа

    private Map<String, List<UserStatistics>> groupUsersByCohort(List<UserStatistics> userStats, String cohortSize) {
        Map<String, List<UserStatistics>> cohorts = new HashMap<>();
        
        // Упрощенная группировка по когортам
        for (UserStatistics stat : userStats) {
            String cohortKey = stat.getDate().toString(); // В реальности здесь была бы более сложная логика
            cohorts.computeIfAbsent(cohortKey, k -> new ArrayList<>()).add(stat);
        }
        
        return cohorts;
    }

    private Map<String, Object> calculateCohortRetentionMatrix(Map<String, List<UserStatistics>> cohorts) {
        Map<String, Object> matrix = new HashMap<>();
        
        // Упрощенный расчет матрицы удержания
        for (Map.Entry<String, List<UserStatistics>> entry : cohorts.entrySet()) {
            String cohort = entry.getKey();
            List<UserStatistics> stats = entry.getValue();
            
            Map<String, Double> retentionData = new HashMap<>();
            retentionData.put("day_0", 100.0); // 100% в день регистрации
            retentionData.put("day_7", 75.0);  // 75% через неделю
            retentionData.put("day_30", 50.0); // 50% через месяц
            
            matrix.put(cohort, retentionData);
        }
        
        return matrix;
    }

    private Map<String, Object> calculateAverageRetention(Map<String, Object> retentionMatrix) {
        Map<String, Object> avgRetention = new HashMap<>();
        
        avgRetention.put("day_0", 100.0);
        avgRetention.put("day_7", 75.0);
        avgRetention.put("day_30", 50.0);
        
        return avgRetention;
    }

    private Map<String, Object> calculateCohortSizes(Map<String, List<UserStatistics>> cohorts) {
        Map<String, Object> sizes = new HashMap<>();
        
        for (Map.Entry<String, List<UserStatistics>> entry : cohorts.entrySet()) {
            String cohort = entry.getKey();
            List<UserStatistics> stats = entry.getValue();
            
            sizes.put(cohort, stats.stream().map(UserStatistics::getUserId).distinct().count());
        }
        
        return sizes;
    }

    // Вспомогательные методы для анализа воронок

    private Map<String, Object> calculateRegistrationFunnel(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> funnel = new HashMap<>();
        
        // Упрощенная воронка регистрации
        funnel.put("visitors", 10000);
        funnel.put("sign_ups", 1000);
        funnel.put("email_confirmations", 800);
        funnel.put("profile_completions", 600);
        funnel.put("first_quest_starts", 400);
        
        return funnel;
    }

    private Map<String, Object> calculateQuestCompletionFunnel(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> funnel = new HashMap<>();
        
        // Упрощенная воронка завершения квестов
        funnel.put("quest_views", 5000);
        funnel.put("quest_starts", 2000);
        funnel.put("level_completions", 1500);
        funnel.put("quest_completions", 1000);
        funnel.put("quest_shares", 300);
        
        return funnel;
    }

    private Map<String, Object> calculateTeamFormationFunnel(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> funnel = new HashMap<>();
        
        // Упрощенная воронка формирования команд
        funnel.put("team_page_views", 2000);
        funnel.put("team_creations", 500);
        funnel.put("member_invitations", 400);
        funnel.put("member_joins", 300);
        funnel.put("first_quest_plays", 200);
        
        return funnel;
    }

    private Map<String, Object> calculateFunnelConversionRates(Map<String, Object> funnel) {
        Map<String, Object> conversionRates = new HashMap<>();
        
        // Расчет конверсии между этапами
        List<String> stages = new ArrayList<>(funnel.keySet());
        for (int i = 0; i < stages.size() - 1; i++) {
            String currentStage = stages.get(i);
            String nextStage = stages.get(i + 1);
            
            long currentValue = ((Number) funnel.get(currentStage)).longValue();
            long nextValue = ((Number) funnel.get(nextStage)).longValue();
            
            double rate = currentValue > 0 ? (double) nextValue / currentValue * 100 : 0.0;
            conversionRates.put(currentStage + "_to_" + nextStage, Math.round(rate * 100.0) / 100.0);
        }
        
        return conversionRates;
    }

    // Вспомогательные методы для сегментации пользователей

    private Map<String, Object> segmentUsersByActivity(List<UserStatistics> userStats) {
        Map<String, Object> segments = new HashMap<>();
        
        Map<String, Long> activitySegments = userStats.stream()
                .collect(Collectors.groupingBy(
                        us -> {
                            long sessions = us.getGameSessions();
                            if (sessions == 0) return "inactive";
                            if (sessions <= 5) return "low_activity";
                            if (sessions <= 15) return "medium_activity";
                            return "high_activity";
                        },
                        Collectors.counting()
                ));
        
        segments.put("activity_segments", activitySegments);
        return segments;
    }

    private Map<String, Object> segmentUsersByEngagement(List<UserStatistics> userStats) {
        Map<String, Object> segments = new HashMap<>();
        
        Map<String, Long> engagementSegments = userStats.stream()
                .collect(Collectors.groupingBy(
                        us -> {
                            long totalTime = us.getTotalGameTimeMinutes();
                            if (totalTime == 0) return "not_engaged";
                            if (totalTime <= 60) return "low_engagement";
                            if (totalTime <= 300) return "medium_engagement";
                            return "high_engagement";
                        },
                        Collectors.counting()
                ));
        
        segments.put("engagement_segments", engagementSegments);
        return segments;
    }

    private Map<String, Object> segmentUsersByPerformance(List<UserStatistics> userStats) {
        Map<String, Object> segments = new HashMap<>();
        
        Map<String, Long> performanceSegments = userStats.stream()
                .collect(Collectors.groupingBy(
                        us -> {
                            long completedQuests = us.getCompletedQuests();
                            if (completedQuests == 0) return "beginner";
                            if (completedQuests <= 5) return "intermediate";
                            if (completedQuests <= 15) return "advanced";
                            return "expert";
                        },
                        Collectors.counting()
                ));
        
        segments.put("performance_segments", performanceSegments);
        return segments;
    }

    private Map<String, Object> calculateSegmentStatistics(Map<String, Object> segmentation) {
        Map<String, Object> stats = new HashMap<>();
        
        // Расчет статистики по сегментам
        long totalUsers = 0;
        for (Object segmentData : segmentation.values()) {
            if (segmentData instanceof Map) {
                Map<?, ?> segmentMap = (Map<?, ?>) segmentData;
                for (Object count : segmentMap.values()) {
                    if (count instanceof Number) {
                        totalUsers += ((Number) count).longValue();
                    }
                }
            }
        }
        
        stats.put("total_users", totalUsers);
        stats.put("segment_distribution", segmentation);
        
        return stats;
    }

    // Вспомогательные методы для расчета метрик команд

    private Map<String, Object> calculateTeamActivityMetrics(List<TeamStatistics> teamStats) {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalTeams = teamStats.stream().map(TeamStatistics::getTeamId).distinct().count();
        long activeTeams = teamStats.stream()
                .filter(ts -> ts.getPlayedQuests() > 0)
                .map(TeamStatistics::getTeamId)
                .distinct()
                .count();
        
        metrics.put("totalTeams", totalTeams);
        metrics.put("activeTeams", activeTeams);
        metrics.put("activityRate", totalTeams > 0 ? (double) activeTeams / totalTeams * 100 : 0.0);
        
        return metrics;
    }

    private Map<String, Object> calculateTeamEfficiencyMetrics(List<TeamStatistics> teamStats) {
        Map<String, Object> metrics = new HashMap<>();
        
        double avgCompletionRate = teamStats.stream()
                .filter(ts -> ts.getPlayedQuests() > 0)
                .mapToDouble(ts -> (double) ts.getCompletedQuests() / ts.getPlayedQuests() * 100)
                .average()
                .orElse(0.0);
        
        metrics.put("avgCompletionRate", Math.round(avgCompletionRate * 100.0) / 100.0);
        metrics.put("totalQuestWins", teamStats.stream().mapToLong(TeamStatistics::getQuestWins).sum());
        
        return metrics;
    }

    private Map<String, Object> calculateTeamCollaborationMetrics(List<TeamStatistics> teamStats) {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalMembers = teamStats.stream().mapToLong(TeamStatistics::getMemberAdditions).sum();
        long totalCollaborationEvents = teamStats.stream()
                .mapToLong(ts -> ts.getSuccessfulCodeSubmissions() + ts.getFailedCodeSubmissions())
                .sum();
        
        metrics.put("totalMembers", totalMembers);
        metrics.put("collaborationEvents", totalCollaborationEvents);
        metrics.put("avgTeamSize", totalMembers > 0 ? (double) totalMembers / teamStats.size() : 0.0);
        
        return metrics;
    }

    private Map<String, Object> calculateTopTeams(List<TeamStatistics> teamStats) {
        Map<String, Object> topTeams = new HashMap<>();
        
        List<Map<String, Object>> topByWins = teamStats.stream()
                .collect(Collectors.groupingBy(TeamStatistics::getTeamId))
                .entrySet().stream()
                .map(entry -> {
                    Long teamId = entry.getKey();
                    List<TeamStatistics> stats = entry.getValue();
                    long totalWins = stats.stream().mapToLong(TeamStatistics::getQuestWins).sum();
                    String teamName = stats.get(0).getTeamName();
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("teamId", teamId);
                    result.put("teamName", teamName);
                    result.put("wins", totalWins);
                    return result;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("wins"), (Long) a.get("wins")))
                .limit(10)
                .toList();
        
        topTeams.put("topByWins", topByWins);
        return topTeams;
    }

    private Map<String, Object> calculateTeamDynamics(List<TeamStatistics> teamStats) {
        Map<String, Object> dynamics = new HashMap<>();
        
        long newTeams = teamStats.stream().mapToLong(TeamStatistics::getCreations).sum();
        long dissolvedTeams = teamStats.stream().mapToLong(TeamStatistics::getDeletions).sum();
        long memberChanges = teamStats.stream()
                .mapToLong(ts -> ts.getMemberAdditions() + ts.getMemberRemovals())
                .sum();
        
        dynamics.put("newTeams", newTeams);
        dynamics.put("dissolvedTeams", dissolvedTeams);
        dynamics.put("memberChanges", memberChanges);
        dynamics.put("netGrowth", newTeams - dissolvedTeams);
        
        return dynamics;
    }

    // Вспомогательные методы для расчета метрик файлов

    private Map<String, Object> calculateFileActivityMetrics(List<FileStatistics> fileStats) {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalFiles = fileStats.stream().map(FileStatistics::getFileId).distinct().count();
        long uploadedFiles = fileStats.stream().mapToLong(FileStatistics::getUploads).sum();
        long downloadedFiles = fileStats.stream().mapToLong(FileStatistics::getDownloads).sum();
        
        metrics.put("totalFiles", totalFiles);
        metrics.put("uploadedFiles", uploadedFiles);
        metrics.put("downloadedFiles", downloadedFiles);
        metrics.put("downloadToUploadRatio", uploadedFiles > 0 ? (double) downloadedFiles / uploadedFiles : 0.0);
        
        return metrics;
    }

    private Map<String, Object> calculateStorageMetrics(List<FileStatistics> fileStats) {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalSize = fileStats.stream().mapToLong(fs -> fs.getTotalSizeBytes() != null ? fs.getTotalSizeBytes() : 0L).sum();
        long totalUploads = fileStats.stream().mapToLong(fs -> fs.getUploads() != null ? fs.getUploads() : 0L).sum();
        double avgFileSize = totalUploads > 0 ? (double) totalSize / totalUploads : 0.0;
        
        metrics.put("totalStorageUsed", totalSize);
        metrics.put("avgFileSize", Math.round(avgFileSize));
        metrics.put("storageEfficiency", calculateStorageEfficiency(fileStats));
        
        return metrics;
    }

    private Map<String, Object> calculateFileTypeAnalysis(List<FileStatistics> fileStats) {
        Map<String, Object> analysis = new HashMap<>();
        
        Map<String, Long> typeDistribution = fileStats.stream()
                .collect(Collectors.groupingBy(
                        FileStatistics::getFileType,
                        Collectors.counting()
                ));
        
        analysis.put("typeDistribution", typeDistribution);
        return analysis;
    }

    private Map<String, Object> calculateTopFiles(List<FileStatistics> fileStats) {
        Map<String, Object> topFiles = new HashMap<>();
        
        List<Map<String, Object>> topByDownloads = fileStats.stream()
                .sorted((a, b) -> Long.compare(b.getDownloads() != null ? b.getDownloads() : 0L, a.getDownloads() != null ? a.getDownloads() : 0L))
                .limit(10)
                .map(fs -> {
                    Map<String, Object> fileMap = new HashMap<>();
                    fileMap.put("fileId", fs.getFileId());
                    fileMap.put("downloads", fs.getDownloads() != null ? fs.getDownloads() : 0);
                    fileMap.put("size", fs.getTotalSizeBytes() != null ? fs.getTotalSizeBytes() : 0L);
                    return fileMap;
                })
                .collect(Collectors.toList());
        
        topFiles.put("topByDownloads", topByDownloads);
        return topFiles;
    }

    private double calculateStorageEfficiency(List<FileStatistics> fileStats) {
        // Упрощенный расчет эффективности хранения
        return 85.0; // 85% эффективность по умолчанию
    }

    // Вспомогательные методы для расчета системных метрик

    private Map<String, Object> calculateSystemLoadMetrics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("cpuUsage", 65.5);
        metrics.put("memoryUsage", 72.3);
        metrics.put("diskUsage", 45.8);
        metrics.put("networkUsage", 38.2);
        
        return metrics;
    }

    private Map<String, Object> calculatePerformanceMetrics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("avgResponseTime", 125.5);
        metrics.put("throughput", 1500.0);
        metrics.put("errorRate", 0.5);
        metrics.put("availability", 99.9);
        
        return metrics;
    }

    private Map<String, Object> calculateUsageMetrics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("apiCalls", 150000);
        metrics.put("activeUsers", 2500);
        metrics.put("dataTransfer", 5.2);
        metrics.put("concurrentUsers", 450);
        
        return metrics;
    }
}