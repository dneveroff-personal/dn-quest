package dn.quest.questmanagement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign клиент для интеграции с Statistics Service
 */
@FeignClient(name = "statistics-service", url = "${statistics.service.url:http://statistics-service:8084}")
public interface StatisticsServiceClient {

    /**
     * Обновить статистику квеста
     */
    @PostMapping("/api/quests/{questId}/statistics")
    ResponseEntity<Void> updateQuestStatistics(@PathVariable("questId") Long questId);

    /**
     * Получить статистику квеста
     */
    @GetMapping("/api/quests/{questId}/statistics")
    ResponseEntity<QuestStatisticsDTO> getQuestStatistics(@PathVariable("questId") Long questId);

    /**
     * Получить статистику уровня
     */
    @GetMapping("/api/levels/{levelId}/statistics")
    ResponseEntity<LevelStatisticsDTO> getLevelStatistics(@PathVariable("levelId") Long levelId);

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
    ResponseEntity<QuestRatingDTO> getQuestRating(@PathVariable("questId") Long questId);

    /**
     * Добавить рейтинг квесту
     */
    @PostMapping("/api/quests/{questId}/rating")
    ResponseEntity<Void> addQuestRating(
            @PathVariable("questId") Long questId,
            @RequestBody QuestRatingDTO rating
    );

    /**
     * DTO для статистики квеста
     */
    class QuestStatisticsDTO {
        private Long questId;
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

        // Getters and setters
        public Long getQuestId() { return questId; }
        public void setQuestId(Long questId) { this.questId = questId; }

        public Long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Long totalSessions) { this.totalSessions = totalSessions; }

        public Long getCompletedSessions() { return completedSessions; }
        public void setCompletedSessions(Long completedSessions) { this.completedSessions = completedSessions; }

        public Long getActiveSessions() { return activeSessions; }
        public void setActiveSessions(Long activeSessions) { this.activeSessions = activeSessions; }

        public Long getTotalParticipants() { return totalParticipants; }
        public void setTotalParticipants(Long totalParticipants) { this.totalParticipants = totalParticipants; }

        public Long getUniqueParticipants() { return uniqueParticipants; }
        public void setUniqueParticipants(Long uniqueParticipants) { this.uniqueParticipants = uniqueParticipants; }

        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }

        public Double getAverageDuration() { return averageDuration; }
        public void setAverageDuration(Double averageDuration) { this.averageDuration = averageDuration; }

        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

        public Long getTotalRatings() { return totalRatings; }
        public void setTotalRatings(Long totalRatings) { this.totalRatings = totalRatings; }

        public Map<String, Long> getParticipantsByDifficulty() { return participantsByDifficulty; }
        public void setParticipantsByDifficulty(Map<String, Long> participantsByDifficulty) { this.participantsByDifficulty = participantsByDifficulty; }

        public Map<String, Long> getCompletionsByDay() { return completionsByDay; }
        public void setCompletionsByDay(Map<String, Long> completionsByDay) { this.completionsByDay = completionsByDay; }
    }

    /**
     * DTO для статистики уровня
     */
    class LevelStatisticsDTO {
        private Long levelId;
        private Long totalCompletions;
        private Long uniqueCompletions;
        private Double averageCompletionTime;
        private Long totalHintsUsed;
        private Long totalCodesUsed;
        private Double hintUsageRate;
        private Double codeUsageRate;

        // Getters and setters
        public Long getLevelId() { return levelId; }
        public void setLevelId(Long levelId) { this.levelId = levelId; }

        public Long getTotalCompletions() { return totalCompletions; }
        public void setTotalCompletions(Long totalCompletions) { this.totalCompletions = totalCompletions; }

        public Long getUniqueCompletions() { return uniqueCompletions; }
        public void setUniqueCompletions(Long uniqueCompletions) { this.uniqueCompletions = uniqueCompletions; }

        public Double getAverageCompletionTime() { return averageCompletionTime; }
        public void setAverageCompletionTime(Double averageCompletionTime) { this.averageCompletionTime = averageCompletionTime; }

        public Long getTotalHintsUsed() { return totalHintsUsed; }
        public void setTotalHintsUsed(Long totalHintsUsed) { this.totalHintsUsed = totalHintsUsed; }

        public Long getTotalCodesUsed() { return totalCodesUsed; }
        public void setTotalCodesUsed(Long totalCodesUsed) { this.totalCodesUsed = totalCodesUsed; }

        public Double getHintUsageRate() { return hintUsageRate; }
        public void setHintUsageRate(Double hintUsageRate) { this.hintUsageRate = hintUsageRate; }

        public Double getCodeUsageRate() { return codeUsageRate; }
        public void setCodeUsageRate(Double codeUsageRate) { this.codeUsageRate = codeUsageRate; }
    }

    /**
     * DTO для статистики кода
     */
    class CodeStatisticsDTO {
        private Long codeId;
        private Long totalUsages;
        private Long uniqueUsages;
        private Double averageUsageTime;
        private Long usageLimit;
        private Double usageRate;

        // Getters and setters
        public Long getCodeId() { return codeId; }
        public void setCodeId(Long codeId) { this.codeId = codeId; }

        public Long getTotalUsages() { return totalUsages; }
        public void setTotalUsages(Long totalUsages) { this.totalUsages = totalUsages; }

        public Long getUniqueUsages() { return uniqueUsages; }
        public void setUniqueUsages(Long uniqueUsages) { this.uniqueUsages = uniqueUsages; }

        public Double getAverageUsageTime() { return averageUsageTime; }
        public void setAverageUsageTime(Double averageUsageTime) { this.averageUsageTime = averageUsageTime; }

        public Long getUsageLimit() { return usageLimit; }
        public void setUsageLimit(Long usageLimit) { this.usageLimit = usageLimit; }

        public Double getUsageRate() { return usageRate; }
        public void setUsageRate(Double usageRate) { this.usageRate = usageRate; }
    }

    /**
     * DTO для игрового события
     */
    class GameEventDTO {
        private Long questId;
        private Long sessionId;
        private Long userId;
        private String eventType;
        private Long timestamp;

        // Getters and setters
        public Long getQuestId() { return questId; }
        public void setQuestId(Long questId) { this.questId = questId; }

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }

    /**
     * DTO для события использования кода
     */
    class CodeUsedEventDTO extends GameEventDTO {
        private Long levelId;
        private Long codeId;
        private String codeValue;
        private Long usageTime;

        // Getters and setters
        public Long getLevelId() { return levelId; }
        public void setLevelId(Long levelId) { this.levelId = levelId; }

        public Long getCodeId() { return codeId; }
        public void setCodeId(Long codeId) { this.codeId = codeId; }

        public String getCodeValue() { return codeValue; }
        public void setCodeValue(String codeValue) { this.codeValue = codeValue; }

        public Long getUsageTime() { return usageTime; }
        public void setUsageTime(Long usageTime) { this.usageTime = usageTime; }
    }

    /**
     * DTO для события использования подсказки
     */
    class HintUsedEventDTO extends GameEventDTO {
        private Long levelId;
        private Long hintId;
        private Integer hintCost;
        private Long usageTime;

        // Getters and setters
        public Long getLevelId() { return levelId; }
        public void setLevelId(Long levelId) { this.levelId = levelId; }

        public Long getHintId() { return hintId; }
        public void setHintId(Long hintId) { this.hintId = hintId; }

        public Integer getHintCost() { return hintCost; }
        public void setHintCost(Integer hintCost) { this.hintCost = hintCost; }

        public Long getUsageTime() { return usageTime; }
        public void setUsageTime(Long usageTime) { this.usageTime = usageTime; }
    }

    /**
     * DTO для популярного квеста
     */
    class PopularQuestDTO {
        private Long questId;
        private String title;
        private String difficulty;
        private String category;
        private Long totalSessions;
        private Double averageRating;

        // Getters and setters
        public Long getQuestId() { return questId; }
        public void setQuestId(Long questId) { this.questId = questId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(Long totalSessions) { this.totalSessions = totalSessions; }

        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    }

    /**
     * DTO для рейтинга квеста
     */
    class QuestRatingDTO {
        private Long questId;
        private Long userId;
        private Integer rating;
        private String review;
        private String createdAt;

        // Getters and setters
        public Long getQuestId() { return questId; }
        public void setQuestId(Long questId) { this.questId = questId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getReview() { return review; }
        public void setReview(String review) { this.review = review; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}