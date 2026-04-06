package dn.quest.questmanagement.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Feign клиент для интеграции с Statistics Service
 */
@FeignClient(name = "statistics-service", url = "${statistics.service.url:http://statistics-service:8084}")
public interface StatisticsServiceClient {

    /**
     * Обновить статистику квеста
     */
    @PostMapping("/api/quests/{questId}/statistics")
    ResponseEntity<Void> updateQuestStatistics(@PathVariable("questId") UUID questId);

    /**
     * Получить статистику квеста
     */
    @GetMapping("/api/quests/{questId}/statistics")
    ResponseEntity<QuestStatisticsDTO> getQuestStatistics(@PathVariable("questId") UUID questId);

    /**
     * Получить статистику уровня
     */
    @GetMapping("/api/levels/{levelId}/statistics")
    ResponseEntity<LevelStatisticsDTO> getLevelStatistics(@PathVariable("levelId") UUID levelId);

    /**
     * Получить статистику кода
     */
    @GetMapping("/api/codes/{codeId}/statistics")
    ResponseEntity<CodeStatisticsDTO> getCodeStatistics(@PathVariable("codeId") Long codeId);

    /**
     * Записать событие начала игры
     */
    @PostMapping("/api/events/game-start")
    ResponseEntity<Void> recordGameStart(@RequestBody GameEventDTO event);

    /**
     * Записать событие завершения игры
     */
    @PostMapping("/api/events/game-complete")
    ResponseEntity<Void> recordGameComplete(@RequestBody GameEventDTO event);

    /**
     * Записать событие использования кода
     */
    @PostMapping("/api/events/code-used")
    ResponseEntity<Void> recordCodeUsed(@RequestBody CodeUsedEventDTO event);

    /**
     * Записать событие использования подсказки
     */
    @PostMapping("/api/events/hint-used")
    ResponseEntity<Void> recordHintUsed(@RequestBody HintUsedEventDTO event);

    /**
     * Получить популярные квесты
     */
    @GetMapping("/api/quests/popular")
    ResponseEntity<java.util.List<PopularQuestDTO>> getPopularQuests(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit
    );

    /**
     * Получить рейтинг квестов
     */
    @GetMapping("/api/quests/{questId}/rating")
    ResponseEntity<QuestRatingDTO> getQuestRating(@PathVariable("questId") UUID questId);

    /**
     * Добавить рейтинг квесту
     */
    @PostMapping("/api/quests/{questId}/rating")
    ResponseEntity<Void> addQuestRating(
            @PathVariable("questId") UUID questId,
            @RequestBody QuestRatingDTO rating
    );

    /**
     * DTO для статистики квеста
     */
    @Setter
    @Getter
    class QuestStatisticsDTO {
        private UUID questId;
        private Long totalSessions;
        private Long completedSessions;
        private Long activeSessions;
        private Long totalParticipants;
        private Long uniqueParticipants;
        private Double completionRate;
        private Double averageDuration;
        private Double averageRating;
        private Long totalRatings;
        private Map<String, Long> participantsByDifficulty;
        private Map<String, Long> completionsByDay;
    }

    /**
     * DTO для статистики уровня
     */
    @Setter
    @Getter
    class LevelStatisticsDTO {
        private UUID levelId;
        private Long totalCompletions;
        private Long uniqueCompletions;
        private Double averageCompletionTime;
        private Long totalHintsUsed;
        private Long totalCodesUsed;
        private Double hintUsageRate;
        private Double codeUsageRate;
    }

    /**
     * DTO для статистики кода
     */
    @Setter
    @Getter
    class CodeStatisticsDTO {
        private Long codeId;
        private Long totalUsages;
        private Long uniqueUsages;
        private Double averageUsageTime;
        private Long usageLimit;
        private Double usageRate;
    }

    /**
     * DTO для игрового события
     */
    @Setter
    @Getter
    class GameEventDTO {
        private UUID questId;
        private UUID sessionId;
        private UUID userId;
        private String eventType;
        private Long timestamp;
    }

    /**
     * DTO для события использования кода
     */
    @Setter
    @Getter
    class CodeUsedEventDTO extends GameEventDTO {
        private UUID levelId;
        private Long codeId;
        private String codeValue;
        private Long usageTime;
    }

    /**
     * DTO для события использования подсказки
     */
    @Setter
    @Getter
    class HintUsedEventDTO extends GameEventDTO {
        private UUID levelId;
        private UUID hintId;
        private Integer hintCost;
        private Long usageTime;
    }

    /**
     * DTO для популярного квеста
     */
    @Setter
    @Getter
    class PopularQuestDTO {
        private UUID questId;
        private String title;
        private String difficulty;
        private String category;
        private Long totalSessions;
        private Double averageRating;
    }

    /**
     * DTO для рейтинга квеста
     */
    @Setter
    @Getter
    class QuestRatingDTO {
        private UUID questId;
        private UUID userId;
        private Integer rating;
        private String review;
        private String createdAt;
    }
}