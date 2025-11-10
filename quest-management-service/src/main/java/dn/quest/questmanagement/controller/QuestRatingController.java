package dn.quest.questmanagement.controller;

import dn.quest.questmanagement.dto.QuestRatingDTO;
import dn.quest.questmanagement.dto.QuestReviewDTO;
import dn.quest.questmanagement.service.QuestRatingService;
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
 * Контроллер для управления рейтингами и отзывами на квесты
 */
@Slf4j
@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
@Tag(name = "Quest Ratings & Reviews", description = "API для управления рейтингами и отзывами на квесты")
public class QuestRatingController {

    private final QuestRatingService questRatingService;

    /**
     * Добавить или обновить рейтинг квеста
     */
    @PostMapping("/{questId}/ratings")
    @Operation(summary = "Добавить/обновить рейтинг", description = "Добавляет новый рейтинг или обновляет существующий")
    public ResponseEntity<QuestRatingDTO> rateQuest(
            @Parameter(description = "ID квеста") @PathVariable Long questId,
            @Parameter(description = "ID пользователя") @RequestParam Long userId,
            @Parameter(description = "Рейтинг от 1 до 5") @RequestParam Integer rating) {
        
        log.info("User {} rating quest {} with {}", userId, questId, rating);
        
        QuestRatingDTO result = questRatingService.rateQuest(questId, userId, rating);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Добавить отзыв на квест
     */
    @PostMapping("/{questId}/reviews")
    @Operation(summary = "Добавить отзыв", description = "Добавляет новый отзыв на квест")
    public ResponseEntity<QuestReviewDTO> reviewQuest(
            @Parameter(description = "ID квеста") @PathVariable Long questId,
            @Parameter(description = "ID пользователя") @RequestParam Long userId,
            @Valid @RequestBody QuestReviewRequestDTO reviewRequest) {
        
        log.info("User {} reviewing quest {}", userId, questId);
        
        QuestReviewDTO result = questRatingService.reviewQuest(
                questId, userId, reviewRequest.getTitle(), 
                reviewRequest.getContent(), reviewRequest.getRating());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Получить рейтинг пользователя для квеста
     */
    @GetMapping("/{questId}/ratings/users/{userId}")
    @Operation(summary = "Получить рейтинг пользователя", description = "Возвращает рейтинг указанного пользователя для квеста")
    public ResponseEntity<QuestRatingDTO> getUserRating(
            @Parameter(description = "ID квеста") @PathVariable Long questId,
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        log.debug("Getting user {} rating for quest {}", userId, questId);
        
        QuestRatingDTO result = questRatingService.getUserRating(questId, userId);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Получить отзыв пользователя для квеста
     */
    @GetMapping("/{questId}/reviews/users/{userId}")
    @Operation(summary = "Получить отзыв пользователя", description = "Возвращает отзыв указанного пользователя для квеста")
    public ResponseEntity<QuestReviewDTO> getUserReview(
            @Parameter(description = "ID квеста") @PathVariable Long questId,
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        log.debug("Getting user {} review for quest {}", userId, questId);
        
        QuestReviewDTO result = questRatingService.getUserReview(questId, userId);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Получить все рейтинги квеста
     */
    @GetMapping("/{questId}/ratings")
    @Operation(summary = "Получить все рейтинги", description = "Возвращает все рейтинги для указанного квеста")
    public ResponseEntity<List<QuestRatingDTO>> getQuestRatings(
            @Parameter(description = "ID квеста") @PathVariable Long questId) {
        
        log.debug("Getting all ratings for quest {}", questId);
        
        List<QuestRatingDTO> result = questRatingService.getQuestRatings(questId);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Получить все отзывы квеста с пагинацией
     */
    @GetMapping("/{questId}/reviews")
    @Operation(summary = "Получить отзывы", description = "Возвращает отзывы для указанного квеста с пагинацией")
    public ResponseEntity<Page<QuestReviewDTO>> getQuestReviews(
            @Parameter(description = "ID квеста") @PathVariable Long questId,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поле сортировки") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.debug("Getting reviews for quest {} with pagination", questId);
        
        org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by(
                                sortDirection.equalsIgnoreCase("desc") ? 
                                org.springframework.data.domain.Sort.Direction.DESC : 
                                org.springframework.data.domain.Sort.Direction.ASC, sortBy));
        
        Page<QuestReviewDTO> result = questRatingService.getQuestReviews(questId, pageable);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Получить статистику рейтингов квеста
     */
    @GetMapping("/{questId}/ratings/stats")
    @Operation(summary = "Получить статистику рейтингов", description = "Возвращает подробную статистику рейтингов для квеста")
    public ResponseEntity<QuestRatingService.QuestRatingStats> getQuestRatingStats(
            @Parameter(description = "ID квеста") @PathVariable Long questId) {
        
        log.debug("Getting rating stats for quest {}", questId);
        
        QuestRatingService.QuestRatingStats result = questRatingService.getQuestRatingStats(questId);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Удалить рейтинг
     */
    @DeleteMapping("/{questId}/ratings/users/{userId}")
    @Operation(summary = "Удалить рейтинг", description = "Удаляет рейтинг пользователя для указанного квеста")
    public ResponseEntity<Void> deleteRating(
            @Parameter(description = "ID квеста") @PathVariable Long questId,
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        log.info("Deleting rating for quest {} by user {}", questId, userId);
        
        questRatingService.deleteRating(questId, userId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Удалить отзыв
     */
    @DeleteMapping("/{questId}/reviews/users/{userId}")
    @Operation(summary = "Удалить отзыв", description = "Удаляет отзыв пользователя для указанного квеста")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "ID квеста") @PathVariable Long questId,
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        
        log.info("Deleting review for quest {} by user {}", questId, userId);
        
        questRatingService.deleteReview(questId, userId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Скрыть/показать отзыв (для модерации)
     */
    @PutMapping("/reviews/{reviewId}/visibility")
    @Operation(summary = "Управление видимостью отзыва", description = "Скрывает или показывает отзыв (для модерации)")
    public ResponseEntity<Void> toggleReviewVisibility(
            @Parameter(description = "ID отзыва") @PathVariable Long reviewId,
            @Parameter(description = "Видимость отзыва") @RequestParam Boolean isVisible) {
        
        log.info("Toggling review {} visibility to {}", reviewId, isVisible);
        
        questRatingService.toggleReviewVisibility(reviewId, isVisible);
        
        return ResponseEntity.ok().build();
    }

    /**
     * Получить лучшие квесты по рейтингу
     */
    @GetMapping("/top-rated")
    @Operation(summary = "Лучшие квесты", description = "Возвращает квесты с самым высоким рейтингом")
    public ResponseEntity<List<QuestDTO>> getTopRatedQuests(
            @Parameter(description = "Лимит результатов") @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting top {} rated quests", limit);
        
        List<QuestDTO> result = questRatingService.getTopRatedQuests(limit);
        
        return ResponseEntity.ok(result);
    }

    /**
     * DTO для запроса создания отзыва
     */
    public static class QuestReviewRequestDTO {
        private String title;
        private String content;
        private Integer rating;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
    }
}