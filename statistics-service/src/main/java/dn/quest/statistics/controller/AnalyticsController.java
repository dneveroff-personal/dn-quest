package dn.quest.statistics.controller;

import dn.quest.statistics.dto.AnalyticsReportDTO;
import dn.quest.statistics.dto.StatisticsRequestDTO;
import dn.quest.statistics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Контроллер для работы с аналитикой
 */
@RestController
@RequestMapping("/api/stats/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics API", description = "API для работы с аналитикой и отчетами")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Сгенерировать аналитический отчет
     */
    @PostMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Сгенерировать аналитический отчет", description = "Генерирует детальный аналитический отчет на основе параметров")
    public ResponseEntity<AnalyticsReportDTO> generateAnalyticsReport(
            @Valid @RequestBody StatisticsRequestDTO request) {
        
        log.info("Generating analytics report with request: {}", request);
        AnalyticsReportDTO report = analyticsService.generateAnalyticsReport(request);
        return ResponseEntity.ok(report);
    }

    /**
     * Получить отчет по вовлеченности пользователей
     */
    @GetMapping("/engagement")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить отчет по вовлеченности пользователей", description = "Возвращает детальный отчет по вовлеченности пользователей")
    public ResponseEntity<Map<String, Object>> getUserEngagementReport(
            @Parameter(description = "Начальная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Группировка (day, week, month)") 
            @RequestParam(defaultValue = "day") String groupBy) {
        
        log.info("Getting user engagement report from {} to {} grouped by {}", startDate, endDate, groupBy);
        Map<String, Object> report = analyticsService.getUserEngagementReport(startDate, endDate, groupBy);
        return ResponseEntity.ok(report);
    }

    /**
     * Получить отчет по производительности квестов
     */
    @GetMapping("/quests/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить отчет по производительности квестов", description = "Возвращает отчет по производительности и популярности квестов")
    public ResponseEntity<Map<String, Object>> getQuestPerformanceReport(
            @Parameter(description = "Начальная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Категория квестов") 
            @RequestParam(required = false) String category,
            
            @Parameter(description = "ID автора для фильтрации") 
            @RequestParam(required = false) UUID authorId) {
        
        log.info("Getting quest performance report from {} to {} category: {} author: {}", 
                startDate, endDate, category, authorId);
        Map<String, Object> report = analyticsService.getQuestPerformanceReport(startDate, endDate, category, authorId);
        return ResponseEntity.ok(report);
    }

    /**
     * Получить отчет по игровым сессиям
     */
    @GetMapping("/sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить отчет по игровым сессиям", description = "Возвращает детальный отчет по игровым сессиям")
    public ResponseEntity<Map<String, Object>> getGameSessionReport(
            @Parameter(description = "Начальная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "ID квеста для фильтрации") 
            @RequestParam(required = false) UUID questId,
            
            @Parameter(description = "ID пользователя для фильтрации") 
            @RequestParam(required = false) UUID userId) {
        
        log.info("Getting game session report from {} to {} quest: {} user: {}", 
                startDate, endDate, questId, userId);
        Map<String, Object> report = analyticsService.getGameSessionReport(startDate, endDate, questId, userId);
        return ResponseEntity.ok(report);
    }

    /**
     * Получить отчет по командной активности
     */
    @GetMapping("/teams/activity")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить отчет по командной активности", description = "Возвращает отчет по активности и производительности команд")
    public ResponseEntity<Map<String, Object>> getTeamActivityReport(
            @Parameter(description = "Начальная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Тип команды") 
            @RequestParam(required = false) String teamType) {
        
        log.info("Getting team activity report from {} to {} type: {}", startDate, endDate, teamType);
        Map<String, Object> report = analyticsService.getTeamActivityReport(startDate, endDate, teamType);
        return ResponseEntity.ok(report);
    }

    /**
     * Получить прогнозы и тренды
     */
    @GetMapping("/forecasts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить прогнозы и тренды", description = "Возвращает прогнозы на основе исторических данных")
    public ResponseEntity<Map<String, Object>> getForecasts(
            @Parameter(description = "Тип прогноза (users, quests, sessions)") 
            @RequestParam String forecastType,
            
            @Parameter(description = "Период прогноза (7, 30, 90 дней)") 
            @RequestParam(defaultValue = "30") int periodDays,
            
            @Parameter(description = "Уровень доверия (0.8, 0.9, 0.95)") 
            @RequestParam(defaultValue = "0.9") double confidenceLevel) {
        
        log.info("Getting forecasts for type: {} period: {} days confidence: {}", 
                forecastType, periodDays, confidenceLevel);
        Map<String, Object> forecasts = analyticsService.getForecasts(forecastType, periodDays, confidenceLevel);
        return ResponseEntity.ok(forecasts);
    }

    /**
     * Получить когортный анализ
     */
    @GetMapping("/cohorts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить когортный анализ", description = "Возвращает когортный анализ удержания пользователей")
    public ResponseEntity<Map<String, Object>> getCohortAnalysis(
            @Parameter(description = "Начальная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Размер когорты (day, week, month)") 
            @RequestParam(defaultValue = "week") String cohortSize) {
        
        log.info("Getting cohort analysis from {} to {} size: {}", startDate, endDate, cohortSize);
        Map<String, Object> analysis = analyticsService.getCohortAnalysis(startDate, endDate, cohortSize);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Получить воронку конверсии
     */
    @GetMapping("/funnel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить воронку конверсии", description = "Возвращает анализ воронки конверсии пользователей")
    public ResponseEntity<Map<String, Object>> getConversionFunnel(
            @Parameter(description = "Начальная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Тип воронки (registration, quest_completion, team_formation)") 
            @RequestParam String funnelType) {
        
        log.info("Getting conversion funnel from {} to {} type: {}", startDate, endDate, funnelType);
        Map<String, Object> funnel = analyticsService.getConversionFunnel(startDate, endDate, funnelType);
        return ResponseEntity.ok(funnel);
    }

    /**
     * Получить сегментацию пользователей
     */
    @GetMapping("/segmentation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить сегментацию пользователей", description = "Возвращает сегментацию пользователей по различным критериям")
    public ResponseEntity<Map<String, Object>> getUserSegmentation(
            @Parameter(description = "Тип сегментации (activity, engagement, performance)") 
            @RequestParam String segmentationType,
            
            @Parameter(description = "Начальная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting user segmentation type: {} from {} to {}", segmentationType, startDate, endDate);
        Map<String, Object> segmentation = analyticsService.getUserSegmentation(segmentationType, startDate, endDate);
        return ResponseEntity.ok(segmentation);
    }

    /**
     * Получить доступные типы отчетов
     */
    @GetMapping("/reports/types")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить доступные типы отчетов", description = "Возвращает список доступных типов аналитических отчетов")
    public ResponseEntity<List<Map<String, String>>> getAvailableReportTypes() {
        log.info("Getting available report types");
        List<Map<String, String>> reportTypes = analyticsService.getAvailableReportTypes();
        return ResponseEntity.ok(reportTypes);
    }

    /**
     * Получить метаданные для отчетов
     */
    @GetMapping("/metadata")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить метаданные для отчетов", description = "Возвращает метаданные и схемы для генерации отчетов")
    public ResponseEntity<Map<String, Object>> getReportsMetadata() {
        log.info("Getting reports metadata");
        Map<String, Object> metadata = analyticsService.getReportsMetadata();
        return ResponseEntity.ok(metadata);
    }
}