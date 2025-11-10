package dn.quest.questmanagement.controller;

import dn.quest.questmanagement.dto.CodeDTO;
import dn.quest.questmanagement.entity.CodeType;
import dn.quest.questmanagement.service.CodeService;
import dn.quest.questmanagement.service.CodeService.CodeUsageResult;
import dn.quest.questmanagement.service.CodeService.CodeValidationResult;
import dn.quest.questmanagement.service.CodeService.CodeUsageStatistics;
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
 * REST контроллер для управления кодами уровней
 */
@Slf4j
@RestController
@RequestMapping("/api/quests/{questId}/levels/{levelId}/codes")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    /**
     * Создать новый код
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CodeDTO> createCode(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @Valid @RequestBody CodeDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        CodeDTO createdCode = codeService.createCode(dto, levelId);
        
        log.info("Code created successfully with ID: {} for level: {}", createdCode.getId(), levelId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCode);
    }

    /**
     * Обновить существующий код
     */
    @PutMapping("/{codeId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CodeDTO> updateCode(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @PathVariable Long codeId,
            @Valid @RequestBody CodeDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        CodeDTO updatedCode = codeService.updateCode(codeId, dto);
        
        log.info("Code updated successfully with ID: {} for level: {}", codeId, levelId);
        return ResponseEntity.ok(updatedCode);
    }

    /**
     * Удалить код
     */
    @DeleteMapping("/{codeId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteCode(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @PathVariable Long codeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        codeService.deleteCode(codeId);
        
        log.info("Code deleted successfully with ID: {} for level: {}", codeId, levelId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получить код по ID
     */
    @GetMapping("/{codeId}")
    public ResponseEntity<CodeDTO> getCodeById(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @PathVariable Long codeId) {
        
        CodeDTO code = codeService.getCodeById(codeId);
        return ResponseEntity.ok(code);
    }

    /**
     * Получить все коды уровня
     */
    @GetMapping
    public ResponseEntity<List<CodeDTO>> getCodesByLevelId(
            @PathVariable Long questId,
            @PathVariable Long levelId) {
        
        List<CodeDTO> codes = codeService.getCodesByLevelId(levelId);
        return ResponseEntity.ok(codes);
    }

    /**
     * Получить активные коды уровня
     */
    @GetMapping("/active")
    public ResponseEntity<List<CodeDTO>> getActiveCodesByLevelId(
            @PathVariable Long questId,
            @PathVariable Long levelId) {
        
        List<CodeDTO> codes = codeService.getActiveCodesByLevelId(levelId);
        return ResponseEntity.ok(codes);
    }

    /**
     * Получить коды по типу
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<CodeDTO>> getCodesByType(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @PathVariable CodeType type) {
        
        List<CodeDTO> codes = codeService.getCodesByType(levelId, type);
        return ResponseEntity.ok(codes);
    }

    /**
     * Проверить код
     */
    @PostMapping("/validate")
    public ResponseEntity<CodeValidationResult> validateCode(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @RequestParam String codeValue) {
        
        CodeValidationResult result = codeService.validateCode(levelId, codeValue);
        return ResponseEntity.ok(result);
    }

    /**
     * Использовать код
     */
    @PostMapping("/{codeId}/use")
    public ResponseEntity<CodeUsageResult> useCode(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @PathVariable Long codeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        CodeUsageResult result = codeService.useCode(codeId, userId);
        
        log.info("Code used successfully with ID: {} by user: {}", codeId, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Получить неиспользованные коды уровня для пользователя
     */
    @GetMapping("/unused")
    public ResponseEntity<List<CodeDTO>> getUnusedCodesByLevelId(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        List<CodeDTO> codes = codeService.getUnusedCodesByLevelId(levelId, userId);
        return ResponseEntity.ok(codes);
    }

    /**
     * Получить использованные коды уровня для пользователя
     */
    @GetMapping("/used")
    public ResponseEntity<List<CodeDTO>> getUsedCodesByLevelId(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        List<CodeDTO> codes = codeService.getUsedCodesByLevelId(levelId, userId);
        return ResponseEntity.ok(codes);
    }

    /**
     * Проверить уникальность кода в рамках квеста
     */
    @GetMapping("/check-unique")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> isCodeUniqueInQuest(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @RequestParam String codeValue,
            @RequestParam(required = false) Long excludeCodeId) {
        
        boolean isUnique = codeService.isCodeUniqueInQuest(questId, codeValue, excludeCodeId);
        return ResponseEntity.ok(isUnique);
    }

    /**
     * Получить статистику использования кодов уровня
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CodeUsageStatistics> getCodeUsageStatistics(
            @PathVariable Long questId,
            @PathVariable Long levelId) {
        
        CodeUsageStatistics statistics = codeService.getCodeUsageStatistics(levelId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Сбросить использование кодов уровня
     */
    @PostMapping("/reset-usage")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> resetCodeUsage(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        codeService.resetCodeUsage(levelId);
        
        log.info("Code usage reset successfully for level: {} by user: {}", levelId, getUserIdFromUserDetails(userDetails));
        return ResponseEntity.ok().build();
    }

    /**
     * Активировать/деактивировать код
     */
    @PutMapping("/{codeId}/active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CodeDTO> toggleCodeActive(
            @PathVariable Long questId,
            @PathVariable Long levelId,
            @PathVariable Long codeId,
            @RequestParam boolean active,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        CodeDTO updatedCode = codeService.toggleCodeActive(codeId, active);
        
        log.info("Code active status changed successfully with ID: {} to active: {} by user: {}", 
                codeId, active, getUserIdFromUserDetails(userDetails));
        return ResponseEntity.ok(updatedCode);
    }

    // Вспомогательный метод для получения ID пользователя из UserDetails
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        // В реальном приложении здесь будет извлечение ID из JWT токена или БД
        // Для упрощения используем username как ID
        return Long.parseLong(userDetails.getUsername());
    }
}