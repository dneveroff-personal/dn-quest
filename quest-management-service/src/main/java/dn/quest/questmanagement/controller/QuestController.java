package dn.quest.questmanagement.controller;

import dn.quest.questmanagement.dto.QuestCreateUpdateDTO;
import dn.quest.questmanagement.dto.QuestDTO;
import dn.quest.questmanagement.dto.QuestSearchRequestDTO;
import dn.quest.questmanagement.entity.QuestStatus;
import dn.quest.questmanagement.service.QuestService;
import dn.quest.questmanagement.service.QuestService.QuestStatistics;
import dn.quest.questmanagement.service.QuestService.QuestValidationResult;
import dn.quest.shared.enums.Difficulty;
import dn.quest.shared.enums.QuestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * REST контроллер для управления квестами
 */
@Slf4j
@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;

    /**
     * Создать новый квест
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> createQuest(
            @Valid @RequestBody QuestCreateUpdateDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long authorId = getUserIdFromUserDetails(userDetails);
        QuestDTO createdQuest = questService.createQuest(dto, authorId);
        
        log.info("Quest created successfully with ID: {} by user: {}", createdQuest.getId(), authorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuest);
    }

    /**
     * Обновить существующий квест
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> updateQuest(
            @PathVariable Long id,
            @Valid @RequestBody QuestCreateUpdateDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        QuestDTO updatedQuest = questService.updateQuest(id, dto, userId);
        
        log.info("Quest updated successfully with ID: {} by user: {}", id, userId);
        return ResponseEntity.ok(updatedQuest);
    }

    /**
     * Удалить квест
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteQuest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        questService.deleteQuest(id, userId);
        
        log.info("Quest deleted successfully with ID: {} by user: {}", id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получить квест по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuestDTO> getQuestById(@PathVariable Long id) {
        QuestDTO quest = questService.getQuestById(id);
        return ResponseEntity.ok(quest);
    }

    /**
     * Получить квест по номеру
     */
    @GetMapping("/number/{number}")
    public ResponseEntity<QuestDTO> getQuestByNumber(@PathVariable Long number) {
        QuestDTO quest = questService.getQuestByNumber(number);
        return ResponseEntity.ok(quest);
    }

    /**
     * Получить все квесты с пагинацией
     */
    @GetMapping
    public ResponseEntity<Page<QuestDTO>> getAllQuests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<QuestDTO> quests = questService.getAllQuests(pageable);
        return ResponseEntity.ok(quests);
    }

    /**
     * Получить опубликованные квесты с пагинацией
     */
    @GetMapping("/published")
    public ResponseEntity<Page<QuestDTO>> getPublishedQuests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<QuestDTO> quests = questService.getPublishedQuests(pageable);
        return ResponseEntity.ok(quests);
    }

    /**
     * Получить активные квесты
     */
    @GetMapping("/active")
    public ResponseEntity<List<QuestDTO>> getActiveQuests() {
        List<QuestDTO> quests = questService.getActiveQuests();
        return ResponseEntity.ok(quests);
    }

    /**
     * Поиск квестов по параметрам
     */
    @PostMapping("/search")
    public ResponseEntity<Page<QuestDTO>> searchQuests(@Valid @RequestBody QuestSearchRequestDTO searchRequest) {
        Page<QuestDTO> quests = questService.searchQuests(searchRequest);
        return ResponseEntity.ok(quests);
    }

    /**
     * Получить квесты автора
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<Page<QuestDTO>> getQuestsByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<QuestDTO> quests = questService.getQuestsByAuthor(authorId, pageable);
        return ResponseEntity.ok(quests);
    }

    /**
     * Получить квесты по сложности
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<List<QuestDTO>> getQuestsByDifficulty(@PathVariable Difficulty difficulty) {
        List<QuestDTO> quests = questService.getQuestsByDifficulty(difficulty);
        return ResponseEntity.ok(quests);
    }

    /**
     * Получить квесты по типу
     */
    @GetMapping("/type/{questType}")
    public ResponseEntity<List<QuestDTO>> getQuestsByType(@PathVariable QuestType questType) {
        List<QuestDTO> quests = questService.getQuestsByType(questType);
        return ResponseEntity.ok(quests);
    }

    /**
     * Получить квесты по категории
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<QuestDTO>> getQuestsByCategory(@PathVariable String category) {
        List<QuestDTO> quests = questService.getQuestsByCategory(category);
        return ResponseEntity.ok(quests);
    }

    /**
     * Получить квесты по тегам
     */
    @PostMapping("/tags")
    public ResponseEntity<List<QuestDTO>> getQuestsByTags(@RequestBody Set<String> tags) {
        List<QuestDTO> quests = questService.getQuestsByTags(tags);
        return ResponseEntity.ok(quests);
    }

    /**
     * Получить шаблоны квестов
     */
    @GetMapping("/templates")
    public ResponseEntity<List<QuestDTO>> getQuestTemplates() {
        List<QuestDTO> templates = questService.getQuestTemplates();
        return ResponseEntity.ok(templates);
    }

    /**
     * Опубликовать квест
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> publishQuest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        QuestDTO quest = questService.publishQuest(id, userId);
        
        log.info("Quest published successfully with ID: {} by user: {}", id, userId);
        return ResponseEntity.ok(quest);
    }

    /**
     * Снять квест с публикации
     */
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> unpublishQuest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        QuestDTO quest = questService.unpublishQuest(id, userId);
        
        log.info("Quest unpublished successfully with ID: {} by user: {}", id, userId);
        return ResponseEntity.ok(quest);
    }

    /**
     * Архивировать квест
     */
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> archiveQuest(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        QuestDTO quest = questService.archiveQuest(id, reason, userId);
        
        log.info("Quest archived successfully with ID: {} by user: {} with reason: {}", id, userId, reason);
        return ResponseEntity.ok(quest);
    }

    /**
     * Разархивировать квест
     */
    @PostMapping("/{id}/unarchive")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> unarchiveQuest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        QuestDTO quest = questService.unarchiveQuest(id, userId);
        
        log.info("Quest unarchived successfully with ID: {} by user: {}", id, userId);
        return ResponseEntity.ok(quest);
    }

    /**
     * Копировать квест
     */
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> copyQuest(
            @PathVariable Long id,
            @RequestParam String newTitle,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long authorId = getUserIdFromUserDetails(userDetails);
        QuestDTO copiedQuest = questService.copyQuest(id, newTitle, authorId);
        
        log.info("Quest copied successfully with ID: {} from ID: {} by user: {}", 
                copiedQuest.getId(), id, authorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(copiedQuest);
    }

    /**
     * Создать шаблон из квеста
     */
    @PostMapping("/{id}/template")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> createTemplateFromQuest(
            @PathVariable Long id,
            @RequestParam String templateName,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        QuestDTO template = questService.createTemplateFromQuest(id, templateName, userId);
        
        log.info("Template created successfully with ID: {} from quest ID: {} by user: {}", 
                template.getId(), id, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(template);
    }

    /**
     * Создать квест из шаблона
     */
    @PostMapping("/template/{templateId}/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> createQuestFromTemplate(
            @PathVariable Long templateId,
            @RequestParam String title,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long authorId = getUserIdFromUserDetails(userDetails);
        QuestDTO quest = questService.createQuestFromTemplate(templateId, title, authorId);
        
        log.info("Quest created successfully with ID: {} from template ID: {} by user: {}", 
                quest.getId(), templateId, authorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(quest);
    }

    /**
     * Изменить статус квеста
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> changeQuestStatus(
            @PathVariable Long id,
            @RequestParam QuestStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        QuestDTO quest = questService.changeQuestStatus(id, status, userId);
        
        log.info("Quest status changed successfully with ID: {} to {} by user: {}", id, status, userId);
        return ResponseEntity.ok(quest);
    }

    /**
     * Добавить автора квесту
     */
    @PostMapping("/{id}/authors/{authorId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> addQuestAuthor(
            @PathVariable Long id,
            @PathVariable Long authorId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        QuestDTO quest = questService.addQuestAuthor(id, authorId, userId);
        
        log.info("Author added successfully to quest with ID: {} by user: {}", id, userId);
        return ResponseEntity.ok(quest);
    }

    /**
     * Удалить автора квеста
     */
    @DeleteMapping("/{id}/authors/{authorId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> removeQuestAuthor(
            @PathVariable Long id,
            @PathVariable Long authorId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        QuestDTO quest = questService.removeQuestAuthor(id, authorId, userId);
        
        log.info("Author removed successfully from quest with ID: {} by user: {}", id, userId);
        return ResponseEntity.ok(quest);
    }

    /**
     * Добавить теги квесту
     */
    @PostMapping("/{id}/tags")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> addQuestTags(
            @PathVariable Long id,
            @RequestBody Set<String> tags,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        QuestDTO quest = questService.addQuestTags(id, tags, userId);
        
        log.info("Tags added successfully to quest with ID: {} by user: {}", id, userId);
        return ResponseEntity.ok(quest);
    }

    /**
     * Удалить теги квеста
     */
    @DeleteMapping("/{id}/tags")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QuestDTO> removeQuestTags(
            @PathVariable Long id,
            @RequestBody Set<String> tags,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        QuestDTO quest = questService.removeQuestTags(id, tags, userId);
        
        log.info("Tags removed successfully from quest with ID: {} by user: {}", id, userId);
        return ResponseEntity.ok(quest);
    }

    /**
     * Проверить права доступа к квесту
     */
    @GetMapping("/{id}/access")
    public ResponseEntity<Boolean> hasQuestAccess(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        boolean hasAccess = questService.hasQuestAccess(id, userId);
        return ResponseEntity.ok(hasAccess);
    }

    /**
     * Проверить права на редактирование квеста
     */
    @GetMapping("/{id}/edit-access")
    public ResponseEntity<Boolean> canEditQuest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        boolean canEdit = questService.canEditQuest(id, userId);
        return ResponseEntity.ok(canEdit);
    }

    /**
     * Проверить права на публикацию квеста
     */
    @GetMapping("/{id}/publish-access")
    public ResponseEntity<Boolean> canPublishQuest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromUserDetails(userDetails);
        boolean canPublish = questService.canPublishQuest(id, userId);
        return ResponseEntity.ok(canPublish);
    }

    /**
     * Валидировать квест перед публикацией
     */
    @GetMapping("/{id}/validate")
    public ResponseEntity<QuestValidationResult> validateQuestForPublishing(@PathVariable Long id) {
        QuestValidationResult result = questService.validateQuestForPublishing(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Получить статистику по квестам
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestStatistics> getQuestStatistics() {
        QuestStatistics statistics = questService.getQuestStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Получить популярные квесты
     */
    @GetMapping("/popular")
    public ResponseEntity<List<QuestDTO>> getPopularQuests(
            @RequestParam(defaultValue = "10") int limit) {
        List<QuestDTO> quests = questService.getPopularQuests(limit);
        return ResponseEntity.ok(quests);
    }

    /**
     * Получить последние квесты
     */
    @GetMapping("/latest")
    public ResponseEntity<List<QuestDTO>> getLatestQuests(
            @RequestParam(defaultValue = "10") int limit) {
        List<QuestDTO> quests = questService.getLatestQuests(limit);
        return ResponseEntity.ok(quests);
    }

    /**
     * Получить просроченные квесты
     */
    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QuestDTO>> getExpiredQuests() {
        List<QuestDTO> quests = questService.getExpiredQuests();
        return ResponseEntity.ok(quests);
    }

    /**
     * Получить квесты, которые скоро начнутся
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<QuestDTO>> getUpcomingQuests(
            @RequestParam(defaultValue = "24") int hours) {
        List<QuestDTO> quests = questService.getUpcomingQuests(hours);
        return ResponseEntity.ok(quests);
    }

    // Вспомогательный метод для получения ID пользователя из UserDetails
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        // В реальном приложении здесь будет извлечение ID из JWT токена или БД
        // Для упрощения используем username как ID
        return Long.parseLong(userDetails.getUsername());
    }
}