package dn.quest.questmanagement.controller;

import dn.quest.questmanagement.dto.LevelDTO;
import dn.quest.questmanagement.service.LevelService;
import dn.quest.questmanagement.service.LevelService.LevelIntegrityResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST контроллер для управления уровнями квестов
 */
@Slf4j
@RestController
@RequestMapping("/api/quests/{questId}/levels")
@RequiredArgsConstructor
public class LevelController {

    private final LevelService levelService;

    /**
     * Создать новый уровень
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LevelDTO> createLevel(
            @PathVariable Long questId,
            @Valid @RequestBody LevelDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Проверка прав на редактирование квеста (будет реализовано через QuestService)
        LevelDTO createdLevel = levelService.createLevel(dto, questId);
        
        log.info("Level created successfully with ID: {} for quest: {}", createdLevel.getId(), questId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLevel);
    }

    /**
     * Обновить существующий уровень
     */
    @PutMapping("/{levelId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LevelDTO> updateLevel(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @Valid @RequestBody LevelDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        LevelDTO updatedLevel = levelService.updateLevel(levelId, dto);
        
        log.info("Level updated successfully with ID: {} for quest: {}", levelId, questId);
        return ResponseEntity.ok(updatedLevel);
    }

    /**
     * Удалить уровень
     */
    @DeleteMapping("/{levelId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteLevel(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        levelService.deleteLevel(levelId);
        
        log.info("Level deleted successfully with ID: {} for quest: {}", levelId, questId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получить уровень по ID
     */
    @GetMapping("/{levelId}")
    public ResponseEntity<LevelDTO> getLevelById(
            @PathVariable Long questId,
            @PathVariable Long levelId) {
        
        LevelDTO level = levelService.getLevelById(levelId);
        return ResponseEntity.ok(level);
    }

    /**
     * Получить все уровни квеста
     */
    @GetMapping
    public ResponseEntity<List<LevelDTO>> getLevelsByQuestId(@PathVariable Long questId) {
        List<LevelDTO> levels = levelService.getLevelsByQuestId(questId);
        return ResponseEntity.ok(levels);
    }

    /**
     * Получить уровень по порядковому номеру в квесте
     */
    @GetMapping("/order/{order}")
    public ResponseEntity<LevelDTO> getLevelByOrder(
            @PathVariable Long questId,
            @PathVariable Integer order) {
        
        LevelDTO level = levelService.getLevelByOrder(questId, order);
        return ResponseEntity.ok(level);
    }

    /**
     * Получить следующий уровень
     */
    @GetMapping("/{currentOrder}/next")
    public ResponseEntity<LevelDTO> getNextLevel(
            @PathVariable Long questId,
            @PathVariable Integer currentOrder) {
        
        LevelDTO nextLevel = levelService.getNextLevel(questId, currentOrder);
        return ResponseEntity.ok(nextLevel);
    }

    /**
     * Получить предыдущий уровень
     */
    @GetMapping("/{currentOrder}/previous")
    public ResponseEntity<LevelDTO> getPreviousLevel(
            @PathVariable Long questId,
            @PathVariable Integer currentOrder) {
        
        LevelDTO previousLevel = levelService.getPreviousLevel(questId, currentOrder);
        return ResponseEntity.ok(previousLevel);
    }

    /**
     * Изменить порядок уровня
     */
    @PutMapping("/{levelId}/order")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LevelDTO> changeLevelOrder(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @RequestParam Integer newOrder,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        LevelDTO updatedLevel = levelService.changeLevelOrder(levelId, newOrder);
        
        log.info("Level order changed successfully with ID: {} to order: {} for quest: {}", 
                levelId, newOrder, questId);
        return ResponseEntity.ok(updatedLevel);
    }

    /**
     * Переместить уровень вверх
     */
    @PostMapping("/{levelId}/move-up")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LevelDTO> moveLevelUp(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        LevelDTO updatedLevel = levelService.moveLevelUp(levelId);
        
        log.info("Level moved up successfully with ID: {} for quest: {}", levelId, questId);
        return ResponseEntity.ok(updatedLevel);
    }

    /**
     * Переместить уровень вниз
     */
    @PostMapping("/{levelId}/move-down")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LevelDTO> moveLevelDown(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        LevelDTO updatedLevel = levelService.moveLevelDown(levelId);
        
        log.info("Level moved down successfully with ID: {} for quest: {}", levelId, questId);
        return ResponseEntity.ok(updatedLevel);
    }

    /**
     * Получить уровни в радиусе от точки
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<LevelDTO>> getLevelsInRadius(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radiusKm) {
        
        List<LevelDTO> levels = levelService.getLevelsInRadius(latitude, longitude, radiusKm);
        return ResponseEntity.ok(levels);
    }

    /**
     * Получить уровни с медиа файлами
     */
    @GetMapping("/with-media")
    public ResponseEntity<List<LevelDTO>> getLevelsWithMedia(@PathVariable Long questId) {
        List<LevelDTO> levels = levelService.getLevelsWithMedia(questId);
        return ResponseEntity.ok(levels);
    }

    /**
     * Получить уровни с подсказками
     */
    @GetMapping("/with-hints")
    public ResponseEntity<List<LevelDTO>> getLevelsWithHints(@PathVariable Long questId) {
        List<LevelDTO> levels = levelService.getLevelsWithHints(questId);
        return ResponseEntity.ok(levels);
    }

    /**
     * Проверить целостность уровней квеста
     */
    @GetMapping("/integrity")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LevelIntegrityResult> checkLevelIntegrity(@PathVariable Long questId) {
        LevelIntegrityResult result = levelService.checkLevelIntegrity(questId);
        return ResponseEntity.ok(result);
    }

    /**
     * Переупорядочить уровни квеста
     */
    @PostMapping("/reorder")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<LevelDTO>> reorderLevels(
            @PathVariable Long questId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        List<LevelDTO> levels = levelService.reorderLevels(questId);
        
        log.info("Levels reordered successfully for quest: {}", questId);
        return ResponseEntity.ok(levels);
    }
}