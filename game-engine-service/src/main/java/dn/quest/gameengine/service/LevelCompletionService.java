package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.LevelCompletion;
import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления завершенными уровнями
 */
public interface LevelCompletionService {

    // Базовые операции CRUD
    LevelCompletion createCompletion(LevelCompletion completion);
    Optional<LevelCompletion> getCompletionById(UUID id);
    LevelCompletion updateCompletion(LevelCompletion completion);
    void deleteCompletion(UUID id);
    
    // Управление завершением уровней
    LevelCompletion completeLevel(UUID sessionId, UUID userId, UUID levelId, String code, Double timeSpent, Double score);
    LevelCompletion completeLevelWithBonus(UUID sessionId, UUID userId, UUID levelId, String code, Double timeSpent, Double score, Double bonus);
    LevelCompletion recordCompletion(UUID sessionId, UUID userId, UUID levelId, LevelCompletion completion);
    
    // Поиск и фильтрация завершений
    Page<LevelCompletion> getAllCompletions(Pageable pageable);
    List<LevelCompletion> getCompletionsBySession(UUID sessionId);
    List<LevelCompletion> getCompletionsByUser(UUID userId);
    List<LevelCompletion> getCompletionsByLevel(UUID levelId);
    List<LevelCompletion> getCompletionsBySessionAndUser(UUID sessionId, UUID userId);
    List<LevelCompletion> getCompletionsByUserAndLevel(UUID userId, UUID levelId);
    List<LevelCompletion> getCompletionsByQuest(UUID questId);
    
    // Статистика завершений
    long getTotalCompletionsCount();
    long getCompletionsCountBySession(UUID sessionId);
    long getCompletionsCountByUser(UUID userId);
    long getCompletionsCountByLevel(UUID levelId);
    long getCompletionsCountByQuest(UUID questId);
    long getUniqueUsersCompletedLevel(UUID levelId);
    long getUniqueSessionsCompletedLevel(UUID levelId);
    
    // Анализ производительности
    List<LevelCompletion> getFastestCompletions(UUID levelId, int limit);
    List<LevelCompletion> getSlowestCompletions(UUID levelId, int limit);
    List<LevelCompletion> getHighestScoringCompletions(UUID levelId, int limit);
    List<LevelCompletion> getLowestScoringCompletions(UUID levelId, int limit);
    Double getAverageCompletionTime(UUID levelId);
    Double getAverageCompletionScore(UUID levelId);
    Double getAverageCompletionTimeByUser(UUID userId, UUID levelId);
    
    // Анализ по времени
    List<LevelCompletion> getCompletionsByDateRange(Instant start, Instant end);
    List<LevelCompletion> getRecentCompletions(int limit);
    List<LevelCompletion> getCompletionsInTimeWindow(UUID sessionId, Instant start, Instant end);
    List<LevelCompletion> getCompletionsByHourOfDay(UUID levelId, int hour);
    List<LevelCompletion> getCompletionsByDayOfWeek(UUID levelId, int dayOfWeek);
    
    // Управление очками и бонусами
    LevelCompletion addBonusPoints(Long completionId, Double bonus, String reason);
    LevelCompletion applyPenalty(Long completionId, Double penalty, String reason);
    LevelCompletion adjustScore(Long completionId, Double newScore, String reason);
    Double calculateFinalScore(Long completionId);
    Double calculateTimeBonus(Double timeSpent, Double targetTime);
    Double calculateAttemptPenalty(int attempts, int maxAttempts);
    
    // Рейтинги и лидерборды
    List<LevelCompletion> getTopCompletionsByScore(UUID levelId, int limit);
    List<LevelCompletion> getTopCompletionsByTime(UUID levelId, int limit);
    List<LevelCompletion> getUserBestCompletions(UUID userId, int limit);
    List<LevelCompletion> getSessionBestCompletions(UUID sessionId, int limit);
    Integer getUserRanking(UUID userId, UUID levelId);
    Integer getSessionRanking(UUID sessionId, UUID userId, UUID levelId);
    
    // Валидация и бизнес-логика
    boolean canCompleteLevel(UUID sessionId, UUID userId, UUID levelId);
    boolean isLevelAlreadyCompleted(UUID sessionId, UUID userId, UUID levelId);
    boolean isValidCompletionTime(Double timeSpent, UUID levelId);
    boolean isValidCompletionScore(Double score, UUID levelId);
    boolean isFirstCompletion(UUID sessionId, UUID userId, UUID levelId);
    boolean isRecordCompletion(Long completionId);
    
    // Управление последовательностью
    List<LevelCompletion> getCompletionSequence(UUID sessionId, UUID userId);
    LevelCompletion getFirstCompletion(UUID sessionId, UUID userId);
    LevelCompletion getLastCompletion(UUID sessionId, UUID userId);
    LevelCompletion getNextLevelCompletion(UUID sessionId, UUID userId, Long currentLevelId);
    boolean isQuestCompleted(UUID sessionId, UUID userId, UUID questId);
    
    // Командные операции
    List<LevelCompletion> getTeamCompletions(UUID sessionId, UUID teamId);
    List<LevelCompletion> getTeamCompletionsForLevel(UUID sessionId, UUID teamId, UUID levelId);
    Double getTeamAverageCompletionTime(UUID sessionId, UUID teamId, UUID levelId);
    Double getTeamAverageScore(UUID sessionId, UUID teamId, UUID levelId);
    boolean isTeamLevelCompleted(UUID sessionId, UUID teamId, UUID levelId);
    
    // Операции для администрирования
    List<LevelCompletion> getAllCompletionsForAdmin();
    void deleteCompletionsBySession(UUID sessionId);
    void deleteCompletionsOlderThan(Instant cutoffDate);
    List<LevelCompletion> getSuspiciousCompletions(int limit);
    void recalculateScores(UUID levelId);
    void recalculateAllScores();
    
    // Операции с кэшированием
    void cacheCompletion(LevelCompletion completion);
    void evictCompletionFromCache(Long completionId);
    Optional<LevelCompletion> getCachedCompletion(Long completionId);
    void cacheLevelCompletions(UUID levelId, List<LevelCompletion> completions);
    void evictLevelCompletionsFromCache(UUID levelId);
    
    // Операции с событиями
    void publishLevelCompletedEvent(LevelCompletion completion);
    void publishScoreUpdatedEvent(LevelCompletion completion);
    void publishBonusAppliedEvent(LevelCompletion completion, Double bonus);
    void publishPenaltyAppliedEvent(LevelCompletion completion, Double penalty);
    void publishRecordCompletionEvent(LevelCompletion completion);
    
    // Интеграция с другими сервисами
    void updateStatistics(LevelCompletion completion);
    void updateLeaderboard(LevelCompletion completion);
    void updateAchievements(LevelCompletion completion);
    void notifyUser(LevelCompletion completion);
    void notifyTeam(LevelCompletion completion);
    
    // Аналитика и отчеты
    List<Object[]> getCompletionStatisticsByHour(UUID levelId, Instant start, Instant end);
    List<Object[]> getCompletionStatisticsByDay(UUID levelId, Instant start, Instant end);
    List<Object[]> getCompletionRateAnalysis(UUID levelId);
    List<Object[]> getUserCompletionSummary(UUID userId);
    List<Object[]> getLevelDifficultyAnalysis(UUID levelId);
    List<Object[]> getTimeDistributionAnalysis(UUID levelId);
    List<Object[]> getScoreDistributionAnalysis(UUID levelId);
    
    // Операции для оптимизации
    void batchCreateCompletions(List<LevelCompletion> completions);
    void batchUpdateCompletions(List<LevelCompletion> completions);
    List<LevelCompletion> getCompletionsForOptimization(int batchSize);
    
    // Вспомогательные методы
    String generateCompletionId();
    void logCompletion(LevelCompletion completion);
    Double calculateEfficiencyScore(Double timeSpent, Double score, int attempts);
    boolean isValidCompletionCode(String code, UUID levelId);
    
    // Операции с достижениями
    void checkCompletionAchievements(LevelCompletion completion);
    List<String> getCompletionAchievements(Long completionId);
    boolean hasCompletionAchievement(UUID userId, String achievementCode);
    
    // Операции с попытками
    int getCompletionAttempts(Long completionId);
    Double calculateAttemptEfficiency(int attempts, int maxAttempts);
    List<String> getCompletionAttemptHistory(Long completionId);
    
    // Операции с подсказками
    int getHintsUsedCount(Long completionId);
    Double calculateHintPenalty(int hintsUsed);
    List<UUID> getHintsUsedInCompletion(Long completionId);
    
    // Сравнительный анализ
    List<LevelCompletion> getSimilarCompletions(LevelCompletion completion, int limit);
    Double getCompletionPercentile(LevelCompletion completion);
    List<LevelCompletion> getBetterCompletions(LevelCompletion completion);
    List<LevelCompletion> getWorseCompletions(LevelCompletion completion);
    
    // Операции с сессиями
    List<LevelCompletion> getSessionCompletionSummary(UUID sessionId);
    Double getSessionCompletionPercentage(UUID sessionId);
    Long getSessionTotalCompletionTime(UUID sessionId);
    Double getSessionTotalScore(UUID sessionId);
    
    // Операции с квестами
    List<LevelCompletion> getQuestCompletionSummary(UUID questId, UUID userId);
    Double getQuestCompletionPercentage(UUID questId, UUID userId);
    Long getQuestTotalCompletionTime(UUID questId, UUID userId);
    Double getQuestTotalScore(UUID questId, UUID userId);
    boolean isQuestFullyCompleted(UUID questId, UUID userId);
}