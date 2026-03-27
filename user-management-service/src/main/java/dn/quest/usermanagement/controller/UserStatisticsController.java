package dn.quest.usermanagement.controller;

import dn.quest.shared.dto.PaginationDTO;
import dn.quest.usermanagement.dto.UserStatisticsDTO;
import dn.quest.usermanagement.service.UserStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.time.Instant;
import java.util.List;

/**
 * Контроллер для управления статистикой пользователей
 */
@RestController
@RequestMapping("/api/users/statistics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Statistics", description = "API для управления статистикой пользователей")
public class UserStatisticsController {

    private final UserStatisticsService userStatisticsService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Получить статистику пользователя", description = "Возвращает статистику пользователя по ID")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        return userStatisticsService.getUserStatisticsByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}/experience")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Добавить опыт пользователю", description = "Добавляет опыт пользователю и проверяет переход на новый уровень")
    public ResponseEntity<UserStatisticsDTO> addExperience(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "Количество опыта") @RequestParam Long experience) {
        
        UserStatisticsDTO updatedStatistics = userStatisticsService.addExperience(userId, experience);
        return ResponseEntity.ok(updatedStatistics);
    }

    @PostMapping("/{userId}/score")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Добавить очки пользователю", description = "Добавляет очки пользователю")
    public ResponseEntity<UserStatisticsDTO> addScore(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "Количество очков") @RequestParam Long score) {
        
        UserStatisticsDTO updatedStatistics = userStatisticsService.addScore(userId, score);
        return ResponseEntity.ok(updatedStatistics);
    }

    @PostMapping("/{userId}/quest")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить статистику квестов", description = "Обновляет статистику квестов пользователя")
    public ResponseEntity<UserStatisticsDTO> updateQuestStatistics(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "Квест завершен") @RequestParam Boolean completed,
            @Parameter(description = "Время игры в минутах") @RequestParam(required = false) Long playtimeMinutes) {
        
        UserStatisticsDTO updatedStatistics = userStatisticsService.updateQuestStatistics(userId, completed, playtimeMinutes);
        return ResponseEntity.ok(updatedStatistics);
    }

    @PostMapping("/{userId}/level")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить статистику уровней", description = "Обновляет статистику уровней пользователя")
    public ResponseEntity<UserStatisticsDTO> updateLevelStatistics(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "Уровень завершен") @RequestParam Boolean levelCompleted,
            @Parameter(description = "Код решен") @RequestParam(required = false) Boolean codeSolved,
            @Parameter(description = "Подсказка использована") @RequestParam(required = false) Boolean hintUsed,
            @Parameter(description = "Попытка сделана") @RequestParam(required = false) Boolean attemptMade) {
        
        UserStatisticsDTO updatedStatistics = userStatisticsService.updateLevelStatistics(
                userId, levelCompleted, codeSolved, hintUsed, attemptMade);
        return ResponseEntity.ok(updatedStatistics);
    }

    @PostMapping("/{userId}/team")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить статистику команд", description = "Обновляет статистику команд пользователя")
    public ResponseEntity<UserStatisticsDTO> updateTeamStatistics(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "Тип действия") @RequestParam String actionType) {
        
        UserStatisticsDTO updatedStatistics = userStatisticsService.updateTeamStatistics(userId, actionType);
        return ResponseEntity.ok(updatedStatistics);
    }

    @PostMapping("/{userId}/achievement")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить статистику достижений", description = "Обновляет статистику достижений пользователя")
    public ResponseEntity<UserStatisticsDTO> updateAchievementStatistics(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "Тип достижения") @RequestParam String achievementType) {
        
        UserStatisticsDTO updatedStatistics = userStatisticsService.updateAchievementStatistics(userId, achievementType);
        return ResponseEntity.ok(updatedStatistics);
    }

    @PostMapping("/{userId}/login")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить статистику входов", description = "Обновляет статистику входов пользователя")
    public ResponseEntity<UserStatisticsDTO> updateLoginStatistics(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        UserStatisticsDTO updatedStatistics = userStatisticsService.updateLoginStatistics(userId);
        return ResponseEntity.ok(updatedStatistics);
    }

    @PostMapping("/{userId}/activity")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Обновить активность пользователя", description = "Обновляет время последней активности пользователя")
    public ResponseEntity<UserStatisticsDTO> updateLastActivity(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        UserStatisticsDTO updatedStatistics = userStatisticsService.updateLastActivity(userId);
        return ResponseEntity.ok(updatedStatistics);
    }

    // Лидерборды
    @GetMapping("/leaderboard/score")
    @Operation(summary = "Топ пользователей по очкам", description = "Возвращает топ пользователей по количеству очков")
    public ResponseEntity<PaginationDTO<UserStatisticsDTO>> getTopUsersByScore(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "totalScore"));
        Page<UserStatisticsDTO> users = userStatisticsService.getTopUsersByScore(pageable);
        
        PaginationDTO<UserStatisticsDTO> response = PaginationDTO.<UserStatisticsDTO>builder()
                .content(users.getContent())
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .first(users.isFirst())
                .last(users.isLast())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaderboard/level")
    @Operation(summary = "Топ пользователей по уровню", description = "Возвращает топ пользователей по уровню")
    public ResponseEntity<PaginationDTO<UserStatisticsDTO>> getTopUsersByLevel(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "level", "experiencePoints"));
        Page<UserStatisticsDTO> users = userStatisticsService.getTopUsersByLevel(pageable);
        
        PaginationDTO<UserStatisticsDTO> response = PaginationDTO.<UserStatisticsDTO>builder()
                .content(users.getContent())
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .first(users.isFirst())
                .last(users.isLast())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaderboard/quests")
    @Operation(summary = "Топ пользователей по квестам", description = "Возвращает топ пользователей по количеству завершенных квестов")
    public ResponseEntity<PaginationDTO<UserStatisticsDTO>> getTopUsersByQuests(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "questsCompleted"));
        Page<UserStatisticsDTO> users = userStatisticsService.getTopUsersByQuests(pageable);
        
        PaginationDTO<UserStatisticsDTO> response = PaginationDTO.<UserStatisticsDTO>builder()
                .content(users.getContent())
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .first(users.isFirst())
                .last(users.isLast())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaderboard/codes")
    @Operation(summary = "Топ пользователей по кодам", description = "Возвращает топ пользователей по количеству решенных кодов")
    public ResponseEntity<PaginationDTO<UserStatisticsDTO>> getTopUsersByCodes(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "codesSolved"));
        Page<UserStatisticsDTO> users = userStatisticsService.getTopUsersByCodes(pageable);
        
        PaginationDTO<UserStatisticsDTO> response = PaginationDTO.<UserStatisticsDTO>builder()
                .content(users.getContent())
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .first(users.isFirst())
                .last(users.isLast())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaderboard/achievements")
    @Operation(summary = "Топ пользователей по достижениям", description = "Возвращает топ пользователей по количеству достижений")
    public ResponseEntity<PaginationDTO<UserStatisticsDTO>> getTopUsersByAchievements(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "achievementsUnlocked"));
        Page<UserStatisticsDTO> users = userStatisticsService.getTopUsersByAchievements(pageable);
        
        PaginationDTO<UserStatisticsDTO> response = PaginationDTO.<UserStatisticsDTO>builder()
                .content(users.getContent())
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .first(users.isFirst())
                .last(users.isLast())
                .build();
        
        return ResponseEntity.ok(response);
    }

    // Административные эндпоинты
    @GetMapping("/level/range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить пользователей по уровню", description = "Возвращает пользователей с уровнем в указанном диапазоне")
    public ResponseEntity<List<UserStatisticsDTO>> getUsersByLevel(
            @Parameter(description = "Минимальный уровень") @RequestParam Integer minLevel,
            @Parameter(description = "Максимальный уровень") @RequestParam Integer maxLevel) {
        
        List<UserStatisticsDTO> users = userStatisticsService.getUsersByLevel(minLevel, maxLevel);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/score/range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить пользователей по очкам", description = "Возвращает пользователей с очками в указанном диапазоне")
    public ResponseEntity<List<UserStatisticsDTO>> getUsersByScoreRange(
            @Parameter(description = "Минимальное количество очков") @RequestParam Long minScore,
            @Parameter(description = "Максимальное количество очков") @RequestParam Long maxScore) {
        
        List<UserStatisticsDTO> users = userStatisticsService.getUsersByScoreRange(minScore, maxScore);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить активных игроков", description = "Возвращает активных игроков по критериям")
    public ResponseEntity<List<UserStatisticsDTO>> getActivePlayers(
            @Parameter(description = "Минимальное количество очков") @RequestParam Long minScore,
            @Parameter(description = "Минимальное количество квестов") @RequestParam Integer minQuests,
            @Parameter(description = "Начиная с даты") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        
        List<UserStatisticsDTO> users = userStatisticsService.getActivePlayers(minScore, minQuests, since);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить новых игроков", description = "Возвращает новых игроков начиная с указанной даты")
    public ResponseEntity<List<UserStatisticsDTO>> getNewPlayers(
            @Parameter(description = "Начиная с даты") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since) {
        
        List<UserStatisticsDTO> users = userStatisticsService.getNewPlayers(since);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/veterans")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить ветеранов", description = "Возвращает ветеранов до указанной даты")
    public ResponseEntity<List<UserStatisticsDTO>> getVeteranPlayers(
            @Parameter(description = "До указанной даты") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before) {
        
        List<UserStatisticsDTO> users = userStatisticsService.getVeteranPlayers(before);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/streak/{minDays}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить игроков с серией", description = "Возвращает игроков с серией дней не менее указанной")
    public ResponseEntity<List<UserStatisticsDTO>> getPlayersWithStreak(
            @Parameter(description = "Минимальное количество дней") @PathVariable Integer minDays) {
        
        List<UserStatisticsDTO> users = userStatisticsService.getPlayersWithStreak(minDays);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/frequent/{minLogins}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить частых игроков", description = "Возвращает частых игроков с указанным минимальным количеством входов")
    public ResponseEntity<List<UserStatisticsDTO>> getFrequentPlayers(
            @Parameter(description = "Минимальное количество входов") @PathVariable Integer minLogins) {
        
        List<UserStatisticsDTO> users = userStatisticsService.getFrequentPlayers(minLogins);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/rank/{userId}/score")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Получить ранг по очкам", description = "Возвращает ранг пользователя по очкам")
    public ResponseEntity<Long> getUserRankByScore(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        Long rank = userStatisticsService.getUserRankByScore(userId);
        return ResponseEntity.ok(rank);
    }

    @GetMapping("/rank/{userId}/level")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Получить ранг по уровню", description = "Возвращает ранг пользователя по уровню")
    public ResponseEntity<Long> getUserRankByLevel(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        Long rank = userStatisticsService.getUserRankByLevel(userId);
        return ResponseEntity.ok(rank);
    }

    @GetMapping("/global/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить общую статистику", description = "Возвращает общую статистику сервиса")
    public ResponseEntity<dn.quest.usermanagement.dto.GlobalStatisticsSummaryDTO> getGlobalStatistics() {
        dn.quest.usermanagement.dto.GlobalStatisticsSummaryDTO summary = userStatisticsService.getGlobalStatistics();
        return ResponseEntity.ok(summary);
    }
}