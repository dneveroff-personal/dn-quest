package dn.quest.questmanagement.controller;

import dn.quest.questmanagement.dto.LevelHintDTO;
import dn.quest.questmanagement.service.LevelHintService;
import dn.quest.questmanagement.service.LevelHintService.HintUsageResult;
import dn.quest.questmanagement.service.LevelHintService.HintAvailabilityResult;
import dn.quest.questmanagement.service.LevelHintService.HintUsageStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST контроллер для управления подсказками уровней
 */
@Slf4j
@RestController
@RequestMapping("/api/quests/{questId}/levels/{levelId}/hints")
@RequiredArgsConstructor
public class LevelHintController {

    private final LevelHintService levelHintService;

    /**
     * Создать новую подсказку
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LevelHintDTO> createHint(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @Valid @RequestBody LevelHintDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        LevelHintDTO createdHint = levelHintService.createHint(dto, levelId);
        
        log.info("Hint created successfully with ID: {} for level: {}", createdHint.getId(), levelId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHint);
    }

    /**
     * Обновить существующую подсказку
     */
    @PutMapping("/{hintId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LevelHintDTO> updateHint(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @PathVariable Long hintId,
            @Valid @RequestBody LevelHintDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        LevelHintDTO updatedHint = levelHintService.updateHint(hintId, dto);
        
        log.info("Hint updated successfully with ID: {} for level: {}", hintId, levelId);
        return ResponseEntity.ok(updatedHint);
    }

    /**
     * Удалить подсказку
     */
    @DeleteMapping("/{hintId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteHint(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @PathVariable Long hintId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        levelHintService.deleteHint(hintId);
        
        log.info("Hint deleted successfully with ID: {} for level: {}", hintId, levelId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получить подсказку по ID
     */
    @GetMapping("/{hintId}")
    public ResponseEntity<LevelHintDTO> getHintById(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @PathVariable Long hintId) {
        
        LevelHintDTO hint = levelHintService.getHintById(hintId);
        return ResponseEntity.ok(hint);
    }

    /**
     * Получить все подсказки уровня
     */
    @GetMapping
    public ResponseEntity<List<LevelHintDTO>> getHintsByLevelId(
            @PathVariable Long questId,
            @PathVariable Long levelId) {
        
        List<LevelHintDTO> hints = levelHintService.getHintsByLevelId(levelId);
        return ResponseEntity.ok(hints);
    }

    /**
     * Получить доступные подсказки уровня для пользователя
     */
    @GetMapping("/available")
    public ResponseEntity<List<LevelHintDTO>> getAvailableHintsByLevelId(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        List<LevelHintDTO> hints = levelHintService.getAvailableHintsByLevelId(levelId, userId);
        return ResponseEntity.ok(hints);
    }

    /**
     * Получить использованные подсказки уровня для пользователя
     */
    @GetMapping("/used")
    public ResponseEntity<List<LevelHintDTO>> getUsedHintsByLevelId(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        List<LevelHintDTO> hints = levelHintService.getUsedHintsByLevelId(levelId, userId);
        return ResponseEntity.ok(hints);
    }

    /**
     * Получить неиспользованные подсказки уровня для пользователя
     */
    @GetMapping("/unused")
    public ResponseEntity<List<LevelHintDTO>> getUnusedHintsByLevelId(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        List<LevelHintDTO> hints = levelHintService.getUnusedHintsByLevelId(levelId, userId);
        return ResponseEntity.ok(hints);
    }

    /**
     * Использовать подсказку
     */
    @PostMapping("/{hintId}/use")
    public ResponseEntity<HintUsageResult> useHint(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @PathVariable Long hintId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        HintUsageResult result = levelHintService.useHint(hintId, userId);
        
        log.info("Hint used successfully with ID: {} by user: {}", hintId, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Проверить доступность подсказки
     */
    @GetMapping("/{hintId}/availability")
    public ResponseEntity<HintAvailabilityResult> checkHintAvailability(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @PathVariable Long hintId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        HintAvailabilityResult result = levelHintService.checkHintAvailability(hintId, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Получить подсказки по времени
     */
    @GetMapping("/available-after")
    public ResponseEntity<List<LevelHintDTO>> getHintsByAvailableAfter(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime availableAfter) {
        
        List<LevelHintDTO> hints = levelHintService.getHintsByAvailableAfter(levelId, availableAfter);
        return ResponseEntity.ok(hints);
    }

    /**
     * Получить бесплатные подсказки уровня
     */
    @GetMapping("/free")
    public ResponseEntity<List<LevelHintDTO>> getFreeHintsByLevelId(
            @PathVariable Long questId,
            @PathVariable Long levelId) {
        
        List<LevelHintDTO> hints = levelHintService.getFreeHintsByLevelId(levelId);
        return ResponseEntity.ok(hints);
    }

    /**
     * Получить платные подсказки уровня
     */
    @GetMapping("/paid")
    public ResponseEntity<List<LevelHintDTO>> getPaidHintsByLevelId(
            @PathVariable Long questId,
            @PathVariable Long levelId) {
        
        List<LevelHintDTO> hints = levelHintService.getPaidHintsByLevelId(levelId);
        return ResponseEntity.ok(hints);
    }

    /**
     * Получить статистику использования подсказок уровня
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<HintUsageStatistics> getHintUsageStatistics(
            @PathVariable Long questId,
            @PathVariable Long levelId) {
        
        HintUsageStatistics statistics = levelHintService.getHintUsageStatistics(levelId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Сбросить использование подсказок уровня
     */
    @PostMapping("/reset-usage")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> resetHintUsage(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        levelHintService.resetHintUsage(levelId);
        
        log.info("Hint usage reset successfully for level: {} by user: {}", levelId, getUserIdFromUserDetails(userDetails));
        return ResponseEntity.ok().build();
    }

    /**
     * Активировать/деактивировать подсказку
     */
    @PutMapping("/{hintId}/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LevelHintDTO> toggleHintActive(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @PathVariable Long hintId,
            @RequestParam boolean active,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        LevelHintDTO updatedHint = levelHintService.toggleHintActive(hintId, active);
        
        log.info("Hint active status changed successfully with ID: {} to active: {} by user: {}", 
                hintId, active, getUserIdFromUserDetails(userDetails));
        return ResponseEntity.ok(updatedHint);
    }

    // Вспомогательный метод для получения ID пользователя из UserDetails
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        // В реальном приложении здесь будет извлечение ID из JWT токена или БД
        // Для упрощения используем username как ID
        return Long.parseLong(userDetails.getUsername());
    }
}