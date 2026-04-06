package dn.quest.statistics.controller;

import dn.quest.statistics.dto.StatisticsRequestDTO;
import dn.quest.statistics.dto.UserStatisticsDTO;
import dn.quest.statistics.service.StatisticsQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Контроллер для работы со статистикой
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Statistics API", description = "API для работы со статистикой")
@SecurityRequirement(name = "bearerAuth")
public class StatisticsController {

    private final StatisticsQueryService statisticsQueryService;

    /**
     * Получить общую статистику платформы
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить общую статистику платформы", description = "Возвращает агрегированную статистику по всей платформе")
    public ResponseEntity<Map<String, Object>> getPlatformOverview(
            @Parameter(description = "Начальная дата периода") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting platform overview for period: {} to {}", startDate, endDate);
        Map<String, Object> overview = statisticsQueryService.getPlatformOverview(startDate, endDate);
        return ResponseEntity.ok(overview);
    }

    /**
     * Получить статистику пользователя
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить статистику пользователя", description = "Возвращает детальную статистику по указанному пользователю")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics(
            @Parameter(description = "ID пользователя") 
            @PathVariable UUID userId,
            
            @Parameter(description = "Дата статистики") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting statistics for user: {} for date: {}", userId, date);
        UserStatisticsDTO statistics = statisticsQueryService.getUserStatistics(userId, date);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Получить статистику пользователя за период
     */
    @GetMapping("/users/{userId}/period")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить статистику пользователя за период", description = "Возвращает статистику пользователя за указанный период")
    public ResponseEntity<Page<UserStatisticsDTO>> getUserStatisticsForPeriod(
            @Parameter(description = "ID пользователя") 
            @PathVariable UUID userId,
            
            @Parameter(description = "Начальная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Номер страницы") 
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Размер страницы") 
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting statistics for user: {} for period: {} to {}", userId, startDate, endDate);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<UserStatisticsDTO> statistics = statisticsQueryService.getUserStatisticsForPeriod(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Получить статистику квеста
     */
    @GetMapping("/quests/{questId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить статистику квеста", description = "Возвращает детальную статистику по указанному квесту")
    public ResponseEntity<Map<String, Object>> getQuestStatistics(
            @Parameter(description = "ID квеста") 
            @PathVariable UUID questId,
            
            @Parameter(description = "Дата статистики") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting statistics for quest: {} for date: {}", questId, date);
        Map<String, Object> statistics = statisticsQueryService.getQuestStatistics(questId, date);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Получить статистику команды
     */
    @GetMapping("/teams/{teamId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить статистику команды", description = "Возвращает детальную статистику по указанной команде")
    public ResponseEntity<Map<String, Object>> getTeamStatistics(
            @Parameter(description = "ID команды") 
            @PathVariable UUID teamId,
            
            @Parameter(description = "Дата статистики") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting statistics for team: {} for date: {}", teamId, date);
        Map<String, Object> statistics = statisticsQueryService.getTeamStatistics(teamId, date);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Получить кастомную статистику
     */
    @PostMapping("/custom")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить кастомную статистику", description = "Возвращает статистику на основе пользовательских параметров")
    public ResponseEntity<Map<String, Object>> getCustomStatistics(
            @Valid @RequestBody StatisticsRequestDTO request) {
        
        log.info("Getting custom statistics with request: {}", request);
        Map<String, Object> statistics = statisticsQueryService.getCustomStatistics(request);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Получить топ пользователей по метрике
     */
    @GetMapping("/top/users")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить топ пользователей", description = "Возвращает топ пользователей по указанной метрике")
    public ResponseEntity<List<Map<String, Object>>> getTopUsers(
            @Parameter(description = "Метрика для сортировки") 
            @RequestParam(defaultValue = "completedQuests") String metric,
            
            @Parameter(description = "Количество записей") 
            @RequestParam(defaultValue = "10") int limit,
            
            @Parameter(description = "Дата статистики") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting top users by metric: {} limit: {} date: {}", metric, limit, date);
        List<Map<String, Object>> topUsers = statisticsQueryService.getTopUsers(metric, limit, date);
        return ResponseEntity.ok(topUsers);
    }

    /**
     * Получить топ квестов по метрике
     */
    @GetMapping("/top/quests")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить топ квестов", description = "Возвращает топ квестов по указанной метрике")
    public ResponseEntity<List<Map<String, Object>>> getTopQuests(
            @Parameter(description = "Метрика для сортировки") 
            @RequestParam(defaultValue = "completions") String metric,
            
            @Parameter(description = "Количество записей") 
            @RequestParam(defaultValue = "10") int limit,
            
            @Parameter(description = "Дата статистики") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting top quests by metric: {} limit: {} date: {}", metric, limit, date);
        List<Map<String, Object>> topQuests = statisticsQueryService.getTopQuests(metric, limit, date);
        return ResponseEntity.ok(topQuests);
    }

    /**
     * Получить статистику по категориям
     */
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить статистику по категориям", description = "Возвращает агрегированную статистику по категориям")
    public ResponseEntity<Map<String, Object>> getStatisticsByCategories(
            @Parameter(description = "Тип сущности") 
            @RequestParam String entityType,
            
            @Parameter(description = "Начальная дата периода") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата периода") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting statistics by categories for entity type: {} period: {} to {}", entityType, startDate, endDate);
        Map<String, Object> statistics = statisticsQueryService.getStatisticsByCategories(entityType, startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Получить тренды метрик
     */
    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить тренды метрик", description = "Возвращает тренды ключевых метрик за период")
    public ResponseEntity<Map<String, Object>> getMetricTrends(
            @Parameter(description = "Список метрик") 
            @RequestParam(required = false) List<String> metrics,
            
            @Parameter(description = "Период (days, weeks, months)") 
            @RequestParam(defaultValue = "days") String period,
            
            @Parameter(description = "Количество периодов") 
            @RequestParam(defaultValue = "30") int periods) {
        
        log.info("Getting metric trends for metrics: {} period: {} periods: {}", metrics, period, periods);
        Map<String, Object> trends = statisticsQueryService.getMetricTrends(metrics, period, periods);
        return ResponseEntity.ok(trends);
    }

    /**
     * Получить системную статистику
     */
    @GetMapping("/system")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить системную статистику", description = "Возвращает системную статистику и метрики")
    public ResponseEntity<Map<String, Object>> getSystemStatistics(
            @Parameter(description = "Категория метрик") 
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Дата статистики") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting system statistics for category: {} date: {}", category, date);
        Map<String, Object> statistics = statisticsQueryService.getSystemStatistics(category, date);
        return ResponseEntity.ok(statistics);
    }
}