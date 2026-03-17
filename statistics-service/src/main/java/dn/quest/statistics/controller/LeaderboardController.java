package dn.quest.statistics.controller;

import dn.quest.statistics.dto.LeaderboardDTO;
import dn.quest.statistics.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * Контроллер для работы с лидербордами
 */
@RestController
@RequestMapping("/api/stats/leaderboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Leaderboard API", description = "API для работы с лидербордами")
@SecurityRequirement(name = "bearerAuth")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    /**
     * Получить глобальный лидерборд
     */
    @GetMapping("/global")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить глобальный лидерборд", description = "Возвращает глобальный лидерборд пользователей")
    public ResponseEntity<Page<LeaderboardDTO>> getGlobalLeaderboard(
            @Parameter(description = "Период (daily, weekly, monthly, all_time)") 
            @RequestParam(defaultValue = "all_time") String period,
            
            @Parameter(description = "Категория") 
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Дата для периодических лидербордов") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            
            @Parameter(description = "Номер страницы") 
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Размер страницы") 
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting global leaderboard for period: {} category: {} date: {}", period, category, date);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "rank"));
        Page<LeaderboardDTO> leaderboard = leaderboardService.getGlobalLeaderboard(period, category, date, pageable);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * Получить лидерборд квестов
     */
    @GetMapping("/quests")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить лидерборд квестов", description = "Возвращает лидерборд квестов по различным метрикам")
    public ResponseEntity<Page<LeaderboardDTO>> getQuestLeaderboard(
            @Parameter(description = "Период (daily, weekly, monthly, all_time)") 
            @RequestParam(defaultValue = "all_time") String period,
            
            @Parameter(description = "Категория квестов") 
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Метрика сортировки") 
            @RequestParam(defaultValue = "rating") String metric,
            
            @Parameter(description = "Дата для периодических лидербордов") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            
            @Parameter(description = "Номер страницы") 
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Размер страницы") 
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting quest leaderboard for period: {} category: {} metric: {} date: {}", period, category, metric, date);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "rank"));
        Page<LeaderboardDTO> leaderboard = leaderboardService.getQuestLeaderboard(period, category, metric, date, pageable);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * Получить лидерборд команд
     */
    @GetMapping("/teams")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить лидерборд команд", description = "Возвращает лидерборд команд по различным метрикам")
    public ResponseEntity<Page<LeaderboardDTO>> getTeamLeaderboard(
            @Parameter(description = "Период (daily, weekly, monthly, all_time)") 
            @RequestParam(defaultValue = "all_time") String period,
            
            @Parameter(description = "Категория команд") 
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Метрика сортировки") 
            @RequestParam(defaultValue = "rating") String metric,
            
            @Parameter(description = "Дата для периодических лидербордов") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            
            @Parameter(description = "Номер страницы") 
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Размер страницы") 
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting team leaderboard for period: {} category: {} metric: {} date: {}", period, category, metric, date);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "rank"));
        Page<LeaderboardDTO> leaderboard = leaderboardService.getTeamLeaderboard(period, category, metric, date, pageable);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * Получить позицию пользователя в лидерборде
     */
    @GetMapping("/users/{userId}/position")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить позицию пользователя в лидерборде", description = "Возвращает позицию и статистику пользователя в различных лидербордах")
    public ResponseEntity<Map<String, Object>> getUserLeaderboardPosition(
            @Parameter(description = "ID пользователя") 
            @PathVariable Long userId,
            
            @Parameter(description = "Период (daily, weekly, monthly, all_time)") 
            @RequestParam(defaultValue = "all_time") String period,
            
            @Parameter(description = "Дата для периодических лидербордов") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting leaderboard position for user: {} period: {} date: {}", userId, period, date);
        Map<String, Object> position = leaderboardService.getUserLeaderboardPosition(userId, period, date);
        return ResponseEntity.ok(position);
    }

    /**
     * Получить позицию квеста в лидерборде
     */
    @GetMapping("/quests/{questId}/position")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить позицию квеста в лидерборде", description = "Возвращает позицию и статистику квеста в различных лидербордах")
    public ResponseEntity<Map<String, Object>> getQuestLeaderboardPosition(
            @Parameter(description = "ID квеста") 
            @PathVariable Long questId,
            
            @Parameter(description = "Период (daily, weekly, monthly, all_time)") 
            @RequestParam(defaultValue = "all_time") String period,
            
            @Parameter(description = "Метрика") 
            @RequestParam(defaultValue = "rating") String metric,
            
            @Parameter(description = "Дата для периодических лидербордов") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting leaderboard position for quest: {} period: {} metric: {} date: {}", questId, period, metric, date);
        Map<String, Object> position = leaderboardService.getQuestLeaderboardPosition(questId, period, metric, date);
        return ResponseEntity.ok(position);
    }

    /**
     * Получить позицию команды в лидерборде
     */
    @GetMapping("/teams/{teamId}/position")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить позицию команды в лидерборде", description = "Возвращает позицию и статистику команды в различных лидербордах")
    public ResponseEntity<Map<String, Object>> getTeamLeaderboardPosition(
            @Parameter(description = "ID команды") 
            @PathVariable Long teamId,
            
            @Parameter(description = "Период (daily, weekly, monthly, all_time)") 
            @RequestParam(defaultValue = "all_time") String period,
            
            @Parameter(description = "Метрика") 
            @RequestParam(defaultValue = "rating") String metric,
            
            @Parameter(description = "Дата для периодических лидербордов") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting leaderboard position for team: {} period: {} metric: {} date: {}", teamId, period, metric, date);
        Map<String, Object> position = leaderboardService.getTeamLeaderboardPosition(teamId, period, metric, date);
        return ResponseEntity.ok(position);
    }

    /**
     * Получить окружение пользователя в лидерборде
     */
    @GetMapping("/users/{userId}/surrounding")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить окружение пользователя в лидерборде", description = "Возвращает пользователей вокруг указанного в лидерборде")
    public ResponseEntity<List<LeaderboardDTO>> getUserSurroundingInLeaderboard(
            @Parameter(description = "ID пользователя") 
            @PathVariable Long userId,
            
            @Parameter(description = "Период (daily, weekly, monthly, all_time)") 
            @RequestParam(defaultValue = "all_time") String period,
            
            @Parameter(description = "Количество пользователей выше и ниже") 
            @RequestParam(defaultValue = "5") int count,
            
            @Parameter(description = "Дата для периодических лидербордов") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting surrounding users for user: {} period: {} count: {} date: {}", userId, period, count, date);
        List<LeaderboardDTO> surrounding = leaderboardService.getUserSurroundingInLeaderboard(userId, period, count, date);
        return ResponseEntity.ok(surrounding);
    }

    /**
     * Получить историю позиций в лидерборде
     */
    @GetMapping("/users/{userId}/history")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить историю позиций в лидерборде", description = "Возвращает историю позиций пользователя в лидерборде за период")
    public ResponseEntity<Map<String, Object>> getUserLeaderboardHistory(
            @Parameter(description = "ID пользователя") 
            @PathVariable Long userId,
            
            @Parameter(description = "Период (daily, weekly, monthly)") 
            @RequestParam(defaultValue = "daily") String period,
            
            @Parameter(description = "Начальная дата") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Конечная дата") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Getting leaderboard history for user: {} period: {} from {} to {}", userId, period, startDate, endDate);
        Map<String, Object> history = leaderboardService.getUserLeaderboardHistory(userId, period, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    /**
     * Получить доступные категории лидербордов
     */
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить доступные категории лидербордов", description = "Возвращает список доступных категорий для лидербордов")
    public ResponseEntity<Map<String, List<String>>> getLeaderboardCategories() {
        log.info("Getting leaderboard categories");
        Map<String, List<String>> categories = leaderboardService.getLeaderboardCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Получить статистику лидербордов
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Получить статистику лидербордов", description = "Возвращает статистику по лидербордам")
    public ResponseEntity<Map<String, Object>> getLeaderboardStats(
            @Parameter(description = "Период (daily, weekly, monthly, all_time)") 
            @RequestParam(defaultValue = "all_time") String period,
            
            @Parameter(description = "Дата для периодических лидербордов") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("Getting leaderboard stats for period: {} date: {}", period, date);
        Map<String, Object> stats = leaderboardService.getLeaderboardStats(period, date);
        return ResponseEntity.ok(stats);
    }
}