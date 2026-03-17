package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.Quest;
import dn.quest.gameengine.entity.Level;
import dn.quest.gameengine.entity.Code;
import dn.quest.gameengine.entity.LevelHint;
import dn.quest.gameengine.entity.enums.QuestType;
import dn.quest.gameengine.entity.enums.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис для управления квестами
 */
public interface QuestService {

    // Базовые операции CRUD
    Quest createQuest(Quest quest);
    Optional<Quest> getQuestById(Long id);
    Quest updateQuest(Quest quest);
    void deleteQuest(Long id);
    
    // Управление уровнями
    Level createLevel(Level level);
    Optional<Level> getLevelById(Long id);
    Level updateLevel(Level level);
    void deleteLevel(Long id);
    List<Level> getQuestLevels(Long questId);
    
    // Управление кодами
    Code createCode(Code code);
    Optional<Code> getCodeById(Long id);
    Code updateCode(Code code);
    void deleteCode(Long id);
    List<Code> getLevelCodes(Long levelId);
    
    // Управление подсказками
    LevelHint createHint(LevelHint hint);
    Optional<LevelHint> getHintById(Long id);
    LevelHint updateHint(LevelHint hint);
    void deleteHint(Long id);
    List<LevelHint> getLevelHints(Long levelId);
    
    // Поиск и фильтрация квестов
    Page<Quest> getAllQuests(Pageable pageable);
    List<Quest> getActiveQuests();
    List<Quest> getQuestsByType(QuestType type);
    List<Quest> getQuestsByDifficulty(Difficulty difficulty);
    List<Quest> getQuestsByAuthor(Long authorId);
    List<Quest> getQuestsByNameContaining(String name);
    List<Quest> getQuestsByRating(Double minRating, Double maxRating);
    
    // Поиск уровней
    List<Level> getLevelsByDifficulty(Difficulty difficulty);
    List<Level> getLevelsByQuest(Long questId);
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
    boolean canCreateQuest(Long userId);
    boolean canUpdateQuest(Long questId, Long userId);
    boolean canDeleteQuest(Long questId, Long userId);
    boolean canPublishQuest(Long questId, Long userId);
    boolean isValidQuestStructure(Quest quest);
    boolean isValidLevelSequence(List<Level> levels);
    
    // Управление публикацией
    Quest publishQuest(Long questId);
    Quest unpublishQuest(Long questId);
    Quest archiveQuest(Long questId);
    List<Quest> getPublishedQuests();
    List<Quest> getArchivedQuests();
    
    // Управление рейтингами
    Quest updateQuestRating(Long questId, Double newRating);
    Level updateLevelRating(Long levelId, Double newRating);
    Double calculateQuestRating(Long questId);
    Double calculateLevelRating(Long levelId);
    
    // Управление сложностью
    Difficulty calculateQuestDifficulty(Long questId);
    Difficulty calculateLevelDifficulty(Long levelId);
    List<Quest> getQuestsByCalculatedDifficulty(Difficulty difficulty);
    
    // Операции с кодами
    boolean validateCode(String code, Long levelId);
    List<Code> getActiveCodes(Long levelId);
    List<Code> getCodesByType(String codeType);
    Code addCodeToLevel(Long levelId, String codeValue, String codeType, Integer points);
    void removeCodeFromLevel(Long levelId, Long codeId);
    
    // Операции с подсказками
    LevelHint addHintToLevel(Long levelId, String hintText, Integer penaltyPoints);
    void removeHintFromLevel(Long levelId, Long hintId);
    List<LevelHint> getOrderedHints(Long levelId);
    Integer calculateTotalHintPenalty(Long levelId);
    
    // Операции для администрирования
    List<Quest> getAllQuestsForAdmin();
    void forceDeleteQuest(Long questId, String reason);
    void suspendQuest(Long questId, String reason);
    void unsuspendQuest(Long questId);
    List<Quest> getSuspiciousQuests(int limit);
    
    // Операции с кэшированием
    void cacheQuest(Quest quest);
    void evictQuestFromCache(Long questId);
    Optional<Quest> getCachedQuest(Long questId);
    void cacheQuestLevels(Long questId, List<Level> levels);
    void evictQuestLevelsFromCache(Long questId);
    
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
    List<Object[]> getQuestCompletionAnalysis(Long questId);
    List<Object[]> getLevelCompletionAnalysis(Long levelId);
    List<Object[]> getQuestDifficultyAnalysis();
    List<Object[]> getCodeUsageStatistics(Long levelId);
    
    // Операции для оптимизации
    void batchCreateQuests(List<Quest> quests);
    void batchUpdateQuests(List<Quest> quests);
    List<Quest> getQuestsForOptimization(int batchSize);
    
    // Вспомогательные методы
    String generateQuestId();
    String generateLevelId();
    void logQuestOperation(String operation, Long questId, Long userId);
    boolean isValidQuestName(String name);
    String sanitizeQuestDescription(String description);
    
    // Операции с поиском
    Page<Quest> searchQuests(String keyword, Pageable pageable);
    List<Quest> getRecommendedQuests(Long userId, int limit);
    List<Quest> getSimilarQuests(Long questId, int limit);
    List<Quest> getQuestsByTags(List<String> tags);
    
    // Операции с версиями
    Quest createQuestVersion(Long questId, String versionDescription);
    List<Quest> getQuestVersions(Long questId);
    Quest getQuestVersion(Long questId, String version);
    Quest rollbackToVersion(Long questId, String version);
    
    // Операции с шаблонами
    Quest createQuestTemplate(Quest quest);
    List<Quest> getQuestTemplates();
    Quest createQuestFromTemplate(Long templateId, Map<String, Object> parameters);
    Quest saveAsTemplate(Long questId, String templateName);
    
    // Операции с экспортом/импортом
    String exportQuest(Long questId);
    Quest importQuest(String questData);
    List<Quest> bulkImport(List<String> questDataList);
    String exportQuestTemplate(Long templateId);
    
    // Операции с валидацией
    Map<String, List<String>> validateQuestComplete(Quest quest);
    Map<String, List<String>> validateLevelComplete(Level level);
    List<String> getQuestValidationErrors(Long questId);
    List<String> getLevelValidationErrors(Long levelId);
    
    // Операции с метаданными
    Quest updateQuestMetadata(Long questId, Map<String, Object> metadata);
    Map<String, Object> getQuestMetadata(Long questId);
    Level updateLevelMetadata(Long levelId, Map<String, Object> metadata);
    Map<String, Object> getLevelMetadata(Long levelId);
    
    // Операции с медиа
    Quest addQuestMedia(Long questId, String mediaUrl, String mediaType);
    void removeQuestMedia(Long questId, String mediaUrl);
    List<String> getQuestMedia(Long questId);
    Level addLevelMedia(Long levelId, String mediaUrl, String mediaType);
    void removeLevelMedia(Long levelId, String mediaUrl);
    List<String> getLevelMedia(Long levelId);
    
    // Операции с тегами
    Quest addQuestTag(Long questId, String tag);
    void removeQuestTag(Long questId, String tag);
    List<String> getQuestTags(Long questId);
    List<Quest> getQuestsByTag(String tag);
    List<String> getPopularTags(int limit);
    
    // Операции с категориями
    Quest setQuestCategory(Long questId, String category);
    List<Quest> getQuestsByCategory(String category);
    List<String> getAllCategories();
    Map<String, Long> getCategoryStatistics();
    
    // Операции с языками
    Quest setQuestLanguage(Long questId, String language);
    List<Quest> getQuestsByLanguage(String language);
    List<String> getSupportedLanguages();
    Quest translateQuest(Long questId, String targetLanguage);
    
    // Операции с доступом
    Quest setQuestAccessLevel(Long questId, String accessLevel);
    boolean isQuestAccessible(Long questId, Long userId);
    List<Quest> getAccessibleQuests(Long userId);
    Quest grantQuestAccess(Long questId, Long userId);
    void revokeQuestAccess(Long questId, Long userId);
    
    // Операции с безопасностью
    boolean isQuestModificationAllowed(Long userId, Long questId);
    void validateQuestData(Quest quest);
    void sanitizeQuestData(Quest quest);
    List<String> detectSuspiciousContent(Quest quest);
    
    // Операции с мониторингом
    Map<String, Object> getQuestHealthMetrics(Long questId);
    List<String> getQuestErrors(Long questId);
    Map<String, Object> getQuestPerformanceStats(Long questId);
    void monitorQuestActivity(Long questId);
    
    // Операции с отчетами
    String generateQuestReport(Long questId);
    String generateLevelReport(Long levelId);
    String generateQuestsReport();
    Map<String, Object> getQuestAnalytics(Long questId);
    Map<String, Object> getLevelAnalytics(Long levelId);
}