package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.Quest;
import dn.quest.gameengine.entity.Level;
import dn.quest.gameengine.entity.Code;
import dn.quest.gameengine.entity.LevelHint;
import dn.quest.shared.enums.QuestType;
import dn.quest.shared.enums.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления квестами
 */
public interface QuestService {

    // Базовые операции CRUD
    Quest createQuest(Quest quest);
    Optional<Quest> getQuestById(UUID id);
    Quest updateQuest(Quest quest);
    void deleteQuest(UUID id);
    
    // Управление уровнями
    Level createLevel(Level level);
    Optional<Level> getLevelById(UUID id);
    Level updateLevel(Level level);
    void deleteLevel(UUID id);
    List<Level> getQuestLevels(UUID questId);
    
    // Управление кодами
    Code createCode(Code code);
    Optional<Code> getCodeById(UUID id);
    Code updateCode(Code code);
    void deleteCode(UUID id);
    List<Code> getLevelCodes(UUID levelId);
    
    // Управление подсказками
    LevelHint createHint(LevelHint hint);
    Optional<LevelHint> getHintById(UUID id);
    LevelHint updateHint(LevelHint hint);
    void deleteHint(UUID id);
    List<LevelHint> getLevelHints(UUID levelId);
    
    // Поиск и фильтрация квестов
    Page<Quest> getAllQuests(Pageable pageable);
    List<Quest> getActiveQuests();
    List<Quest> getQuestsByType(QuestType type);
    List<Quest> getQuestsByDifficulty(Difficulty difficulty);
    List<Quest> getQuestsByAuthor(UUID authorId);
    List<Quest> getQuestsByNameContaining(String name);
    List<Quest> getQuestsByRating(Double minRating, Double maxRating);
    
    // Поиск уровней
    List<Level> getLevelsByDifficulty(Difficulty difficulty);
    List<Level> getLevelsByQuest(UUID questId);
    List<Level> getLevelsByNameContaining(String name);
    List<Level> getLevelsByEstimatedTime(Long minTime, Long maxTime);
    
    // Статистика квестов
    long getTotalQuestsCount();
    long getActiveQuestsCount();
    long getQuestsCountByType(QuestType type);
    long getQuestsCountByDifficulty(Difficulty difficulty);
    double getAverageQuestRating();
    List<Quest> getMostPopularQuests(int limit);
    List<Quest> getHighestRatedQuests(int limit);
    
    // Статистика уровней
    long getTotalLevelsCount();
    long getLevelsCountByDifficulty(Difficulty difficulty);
    double getAverageLevelRating();
    List<Level> getMostPlayedLevels(int limit);
    List<Level> getHighestRatedLevels(int limit);
    
    // Валидация и бизнес-логика
    boolean canCreateQuest(UUID userId);
    boolean canUpdateQuest(UUID questId, UUID userId);
    boolean canDeleteQuest(UUID questId, UUID userId);
    boolean canPublishQuest(UUID questId, UUID userId);
    boolean isValidQuestStructure(Quest quest);
    boolean isValidLevelSequence(List<Level> levels);
    
    // Управление публикацией
    Quest publishQuest(UUID questId);
    Quest unpublishQuest(UUID questId);
    Quest archiveQuest(UUID questId);
    List<Quest> getPublishedQuests();
    List<Quest> getArchivedQuests();
    
    // Управление рейтингами
    Quest updateQuestRating(UUID questId, Double newRating);
    Level updateLevelRating(UUID levelId, Double newRating);
    Double calculateQuestRating(UUID questId);
    Double calculateLevelRating(UUID levelId);
    
    // Управление сложностью
    Difficulty calculateQuestDifficulty(UUID questId);
    Difficulty calculateLevelDifficulty(UUID levelId);
    List<Quest> getQuestsByCalculatedDifficulty(Difficulty difficulty);
    
    // Операции с кодами
    boolean validateCode(String code, UUID levelId);
    List<Code> getActiveCodes(UUID levelId);
    List<Code> getCodesByType(String codeType);
    Code addCodeToLevel(UUID levelId, String codeValue, String codeType, Integer points);
    void removeCodeFromLevel(UUID levelId, Long codeId);
    
    // Операции с подсказками
    LevelHint addHintToLevel(UUID levelId, String hintText, Integer penaltyPoints);
    void removeHintFromLevel(UUID levelId, UUID hintId);
    List<LevelHint> getOrderedHints(UUID levelId);
    Integer calculateTotalHintPenalty(UUID levelId);
    
    // Операции для администрирования
    List<Quest> getAllQuestsForAdmin();
    void forceDeleteQuest(UUID questId, String reason);
    void suspendQuest(UUID questId, String reason);
    void unsuspendQuest(UUID questId);
    List<Quest> getSuspiciousQuests(int limit);
    
    // Операции с кэшированием
    void cacheQuest(Quest quest);
    void evictQuestFromCache(UUID questId);
    Optional<Quest> getCachedQuest(UUID questId);
    void cacheQuestLevels(UUID questId, List<Level> levels);
    void evictQuestLevelsFromCache(UUID questId);
    
    // Операции с событиями
    void publishQuestCreatedEvent(Quest quest);
    void publishQuestUpdatedEvent(Quest quest);
    void publishQuestDeletedEvent(Quest quest);
    void publishQuestPublishedEvent(Quest quest);
    void publishLevelCreatedEvent(Level level);
    void publishLevelUpdatedEvent(Level level);
    void publishLevelDeletedEvent(Level level);
    
    // Интеграция с другими сервисами
    void notifyQuestAuthor(Quest quest, String message);
    void updateQuestStatistics(Quest quest);
    void syncQuestWithExternalServices(Quest quest);
    void validateQuestDependencies(Quest quest);
    
    // Аналитика и отчеты
    List<Object[]> getQuestStatisticsByDay(Instant start, Instant end);
    List<Object[]> getLevelStatisticsByDay(Instant start, Instant end);
    List<Object[]> getQuestCompletionAnalysis(UUID questId);
    List<Object[]> getLevelCompletionAnalysis(UUID levelId);
    List<Object[]> getQuestDifficultyAnalysis();
    List<Object[]> getCodeUsageStatistics(UUID levelId);
    
    // Операции для оптимизации
    void batchCreateQuests(List<Quest> quests);
    void batchUpdateQuests(List<Quest> quests);
    List<Quest> getQuestsForOptimization(int batchSize);
    
    // Вспомогательные методы
    String generateQuestId();
    String generateLevelId();
    void logQuestOperation(String operation, UUID questId, UUID userId);
    boolean isValidQuestName(String name);
    String sanitizeQuestDescription(String description);
    
    // Операции с поиском
    Page<Quest> searchQuests(String keyword, Pageable pageable);
    List<Quest> getRecommendedQuests(UUID userId, int limit);
    List<Quest> getSimilarQuests(UUID questId, int limit);
    List<Quest> getQuestsByTags(List<String> tags);
    
    // Операции с версиями
    Quest createQuestVersion(UUID questId, String versionDescription);
    List<Quest> getQuestVersions(UUID questId);
    Quest getQuestVersion(UUID questId, String version);
    Quest rollbackToVersion(UUID questId, String version);
    
    // Операции с шаблонами
    Quest createQuestTemplate(Quest quest);
    List<Quest> getQuestTemplates();
    Quest createQuestFromTemplate(Long templateId, Map<String, Object> parameters);
    Quest saveAsTemplate(UUID questId, String templateName);
    
    // Операции с экспортом/импортом
    String exportQuest(UUID questId);
    Quest importQuest(String questData);
    List<Quest> bulkImport(List<String> questDataList);
    String exportQuestTemplate(Long templateId);
    
    // Операции с валидацией
    Map<String, List<String>> validateQuestComplete(Quest quest);
    Map<String, List<String>> validateLevelComplete(Level level);
    List<String> getQuestValidationErrors(UUID questId);
    List<String> getLevelValidationErrors(UUID levelId);
    
    // Операции с метаданными
    Quest updateQuestMetadata(UUID questId, Map<String, Object> metadata);
    Map<String, Object> getQuestMetadata(UUID questId);
    Level updateLevelMetadata(UUID levelId, Map<String, Object> metadata);
    Map<String, Object> getLevelMetadata(UUID levelId);
    
    // Операции с медиа
    Quest addQuestMedia(UUID questId, String mediaUrl, String mediaType);
    void removeQuestMedia(UUID questId, String mediaUrl);
    List<String> getQuestMedia(UUID questId);
    Level addLevelMedia(UUID levelId, String mediaUrl, String mediaType);
    void removeLevelMedia(UUID levelId, String mediaUrl);
    List<String> getLevelMedia(UUID levelId);
    
    // Операции с тегами
    Quest addQuestTag(UUID questId, String tag);
    void removeQuestTag(UUID questId, String tag);
    List<String> getQuestTags(UUID questId);
    List<Quest> getQuestsByTag(String tag);
    List<String> getPopularTags(int limit);
    
    // Операции с категориями
    Quest setQuestCategory(UUID questId, String category);
    List<Quest> getQuestsByCategory(String category);
    List<String> getAllCategories();
    Map<String, UUID> getCategoryStatistics();
    
    // Операции с языками
    Quest setQuestLanguage(UUID questId, String language);
    List<Quest> getQuestsByLanguage(String language);
    List<String> getSupportedLanguages();
    Quest translateQuest(UUID questId, String targetLanguage);
    
    // Операции с доступом
    Quest setQuestAccessLevel(UUID questId, String accessLevel);
    boolean isQuestAccessible(UUID questId, UUID userId);
    List<Quest> getAccessibleQuests(UUID userId);
    Quest grantQuestAccess(UUID questId, UUID userId);
    void revokeQuestAccess(UUID questId, UUID userId);
    
    // Операции с безопасностью
    boolean isQuestModificationAllowed(UUID userId, UUID questId);
    void validateQuestData(Quest quest);
    void sanitizeQuestData(Quest quest);
    List<String> detectSuspiciousContent(Quest quest);
    
    // Операции с мониторингом
    Map<String, Object> getQuestHealthMetrics(UUID questId);
    List<String> getQuestErrors(UUID questId);
    Map<String, Object> getQuestPerformanceStats(UUID questId);
    void monitorQuestActivity(UUID questId);
    
    // Операции с отчетами
    String generateQuestReport(UUID questId);
    String generateLevelReport(UUID levelId);
    String generateQuestsReport();
    Map<String, Object> getQuestAnalytics(UUID questId);
    Map<String, Object> getLevelAnalytics(UUID levelId);
}