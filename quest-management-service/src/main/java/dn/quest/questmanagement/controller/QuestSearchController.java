package dn.quest.questmanagement.controller;

import dn.quest.questmanagement.dto.QuestDTO;
import dn.quest.questmanagement.dto.QuestSearchRequestDTO;
import dn.quest.questmanagement.service.QuestSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Контроллер для поиска квестов
 */
@Slf4j
@RestController
@RequestMapping("/api/quests/search")
@RequiredArgsConstructor
@Tag(name = "Quest Search", description = "API для поиска квестов")
public class QuestSearchController {

    private final QuestSearchService questSearchService;

    /**
     * Расширенный поиск квестов
     */
    @PostMapping
    @Operation(summary = "Расширенный поиск квестов", description = "Поиск квестов с различными фильтрами")
    public ResponseEntity<Page<QuestDTO>> searchQuests(
            @Valid @RequestBody QuestSearchRequestDTO searchRequest) {
        
        log.info("Advanced search request: {}", searchRequest);
        
        Page<QuestDTO> result = questSearchService.searchQuests(searchRequest);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Полнотекстовый поиск квестов
     */
    @GetMapping("/full-text")
    @Operation(summary = "Полнотекстовый поиск", description = "Поиск квестов по тексту в названии, описании и других полях")
    public ResponseEntity<Page<QuestDTO>> fullTextSearch(
            @Parameter(description = "Поисковый запрос") @RequestParam String query,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поле сортировки") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Full-text search query: {}, page: {}, size: {}", query, page, size);
        
        org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by(
                                sortDirection.equalsIgnoreCase("desc") ? 
                                org.springframework.data.domain.Sort.Direction.DESC : 
                                org.springframework.data.domain.Sort.Direction.ASC, sortBy));
        
        Page<QuestDTO> result = questSearchService.fullTextSearch(query, pageable);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Поиск похожих квестов
     */
    @GetMapping("/{questId}/similar")
    @Operation(summary = "Поиск похожих квестов", description = "Находит квесты, похожие на указанный")
    public ResponseEntity<List<QuestDTO>> findSimilarQuests(
            @Parameter(description = "ID квеста") @PathVariable Long questId,
            @Parameter(description = "Лимит результатов") @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Finding similar quests for quest: {}, limit: {}", questId, limit);
        
        List<QuestDTO> result = questSearchService.findSimilarQuests(questId, limit);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Поиск квестов по геолокации
     */
    @GetMapping("/location")
    @Operation(summary = "Поиск по геолокации", description = "Находит квесты в указанном радиусе от точки")
    public ResponseEntity<List<QuestDTO>> findQuestsByLocation(
            @Parameter(description = "Широта") @RequestParam Double latitude,
            @Parameter(description = "Долгота") @RequestParam Double longitude,
            @Parameter(description = "Радиус в км") @RequestParam Double radiusKm) {
        
        log.info("Finding quests near location: {}, {} within radius: {}km", latitude, longitude, radiusKm);
        
        List<QuestDTO> result = questSearchService.findQuestsByLocation(latitude, longitude, radiusKm);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Поиск популярных квестов
     */
    @GetMapping("/popular")
    @Operation(summary = "Поиск популярных квестов", description = "Возвращает список популярных квестов")
    public ResponseEntity<List<QuestDTO>> findPopularQuests(
            @Parameter(description = "Лимит результатов") @RequestParam(defaultValue = "20") int limit) {
        
        log.info("Finding popular quests with limit: {}", limit);
        
        List<QuestDTO> result = questSearchService.findPopularQuests(limit);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Поиск рекомендуемых квестов для пользователя
     */
    @GetMapping("/recommended/{userId}")
    @Operation(summary = "Рекомендации для пользователя", description = "Возвращает рекомендуемые квесты для пользователя")
    public ResponseEntity<List<QuestDTO>> findRecommendedQuests(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "Лимит результатов") @RequestParam(defaultValue = "20") int limit) {
        
        log.info("Finding recommended quests for user: {} with limit: {}", userId, limit);
        
        List<QuestDTO> result = questSearchService.findRecommendedQuests(userId, limit);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Быстрый поиск по тегам
     */
    @GetMapping("/tags")
    @Operation(summary = "Поиск по тегам", description = "Находит квесты с указанными тегами")
    public ResponseEntity<Page<QuestDTO>> findByTags(
            @Parameter(description = "Список тегов") @RequestParam List<String> tags,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поле сортировки") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Finding quests by tags: {}, page: {}, size: {}", tags, page, size);
        
        QuestSearchRequestDTO searchRequest = new QuestSearchRequestDTO();
        searchRequest.setTags(tags);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        searchRequest.setStatus("PUBLISHED");
        searchRequest.setIsPublic(true);
        
        Page<QuestDTO> result = questSearchService.searchQuests(searchRequest);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Поиск по категории
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Поиск по категории", description = "Находит квесты в указанной категории")
    public ResponseEntity<Page<QuestDTO>> findByCategory(
            @Parameter(description = "Категория") @PathVariable String category,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поле сортировки") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Finding quests by category: {}, page: {}, size: {}", category, page, size);
        
        QuestSearchRequestDTO searchRequest = new QuestSearchRequestDTO();
        searchRequest.setCategory(category);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        searchRequest.setStatus("PUBLISHED");
        searchRequest.setIsPublic(true);
        
        Page<QuestDTO> result = questSearchService.searchQuests(searchRequest);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Поиск по сложности
     */
    @GetMapping("/difficulty/{difficulty}")
    @Operation(summary = "Поиск по сложности", description = "Находит квесты указанной сложности")
    public ResponseEntity<Page<QuestDTO>> findByDifficulty(
            @Parameter(description = "Сложность") @PathVariable String difficulty,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поле сортировки") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Finding quests by difficulty: {}, page: {}, size: {}", difficulty, page, size);
        
        QuestSearchRequestDTO searchRequest = new QuestSearchRequestDTO();
        searchRequest.setDifficulty(difficulty);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        searchRequest.setStatus("PUBLISHED");
        searchRequest.setIsPublic(true);
        
        Page<QuestDTO> result = questSearchService.searchQuests(searchRequest);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Поиск по автору
     */
    @GetMapping("/author/{authorId}")
    @Operation(summary = "Поиск по автору", description = "Находит квесты указанного автора")
    public ResponseEntity<Page<QuestDTO>> findByAuthor(
            @Parameter(description = "ID автора") @PathVariable Long authorId,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поле сортировки") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Finding quests by author: {}, page: {}, size: {}", authorId, page, size);
        
        QuestSearchRequestDTO searchRequest = new QuestSearchRequestDTO();
        searchRequest.setAuthorId(authorId);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        
        Page<QuestDTO> result = questSearchService.searchQuests(searchRequest);
        
        return ResponseEntity.ok(result);
    }
}