package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.LevelCompletion;
import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления завершенными уровнями
 */
public interface LevelCompletionService {

    // Базовые операции CRUD
    LevelCompletion createCompletion(LevelCompletion completion);
    Optional<LevelCompletion> getCompletionById(Long id);
    LevelCompletion updateCompletion(LevelCompletion completion);
    void deleteCompletion(Long id);
    
    // Управление завершением уровней
    LevelCompletion completeLevel(Long sessionId, Long userId, Long levelId, String code, Double timeSpent, Double score);
    LevelCompletion completeLevelWithBonus(Long sessionId, Long userId, Long levelId, String code, Double timeSpent, Double score, Double bonus);
    LevelCompletion recordCompletion(Long sessionId, Long userId, Long levelId, LevelCompletion completion);
    
    // Поиск и фильтрация завершений
    Page<LevelCompletion> getAllCompletions(Pageable pageable);
    List<LevelCompletion> getCompletionsBySession(Long sessionId);
    List<LevelCompletion> getCompletionsByUser(Long userId);
    List<LevelCompletion> getCompletionsByLevel(Long levelId);
    List<LevelCompletion> getCompletionsBySessionAndUser(Long sessionId, Long userId);
    List<LevelCompletion> getCompletionsByUserAndLevel(Long userId, Long levelId);
    List<LevelCompletion> getCompletionsByQuest(Long questId);
    
    // Статистика завершений
    long getTotalCompletionsCount();
    long getCompletionsCountBySession(Long sessionId);
    long getCompletionsCountByUser(Long userId);
    long getCompletionsCountByLevel(Long levelId);
    long getCompletionsCountByQuest(Long questId);
    long getUniqueUsersCompletedLevel(Long levelId);
    long getUniqueSessionsCompletedLevel(Long levelId);
    
    // Анализ производительности
    List<LevelCompletion> getFastestCompletions(Long levelId, int limit);
    List<LevelCompletion> getSlowestCompletions(Long levelId, int limit);
    List<LevelCompletion> getHighestScoringCompletions(Long levelId, int limit);
    List<LevelCompletion> getLowestScoringCompletions(Long levelId, int limit);
    Double getAverageCompletionTime(Long levelId);
    Double getAverageCompletionScore(Long levelId);
    Double getAverageCompletionTimeByUser(Long userId, Long levelId);
    
    // Анализ по времени
    List<LevelCompletion> getCompletionsByDateRange(Instant start, Instant end);
    List<LevelCompletion> getRecentCompletions(int limit);
    List<LevelCompletion> getCompletionsInTimeWindow(Long sessionId, Instant start, Instant end);
    List<LevelCompletion> getCompletionsByHourOfDay(Long levelId, int hour);
    List<LevelCompletion> getCompletionsByDayOfWeek(Long levelId, int dayOfWeek);
    
    // Управление очками и бонусами
    LevelCompletion addBonusPoints(Long completionId, Double bonus, String reason);
    LevelCompletion applyPenalty(Long completionId, Double penalty, String reason);
    LevelCompletion adjustScore(Long completionId, Double newScore, String reason);
    Double calculateFinalScore(Long completionId);
    Double calculateTimeBonus(Double timeSpent, Double targetTime);
    Double calculateAttemptPenalty(int attempts, int maxAttempts);
    
    // Рейтинги и лидерборды
    List<LevelCompletion> getTopCompletionsByScore(Long levelId, int limit);
    List<LevelCompletion> getTopCompletionsByTime(Long levelId, int limit);
    List<LevelCompletion> getUserBestCompletions(Long userId, int limit);
    List<LevelCompletion> getSessionBestCompletions(Long sessionId, int limit);
    Integer getUserRanking(Long userId, Long levelId);
    Integer getSessionRanking(Long sessionId, Long userId, Long levelId);
    
    // Валидация и бизнес-логика
    boolean canCompleteLevel(Long sessionId, Long userId, Long levelId);
    boolean isLevelAlreadyCompleted(Long sessionId, Long userId, Long levelId);
    boolean isValidCompletionTime(Double timeSpent, Long levelId);
    boolean isValidCompletionScore(Double score, Long levelId);
    boolean isFirstCompletion(Long sessionId, Long userId, Long levelId);
    boolean isRecordCompletion(Long completionId);
    
    // Управление последовательностью
    List<LevelCompletion> getCompletionSequence(Long sessionId, Long userId);
    LevelCompletion getFirstCompletion(Long sessionId, Long userId);
    LevelCompletion getLastCompletion(Long sessionId, Long userId);
    LevelCompletion getNextLevelCompletion(Long sessionId, Long userId, Long currentLevelId);
    boolean isQuestCompleted(Long sessionId, Long userId, Long questId);
    
    // Командные операции
    List<LevelCompletion> getTeamCompletions(Long sessionId, Long teamId);
    List<LevelCompletion> getTeamCompletionsForLevel(Long sessionId, Long teamId, Long levelId);
    Double getTeamAverageCompletionTime(Long sessionId, Long teamId, Long levelId);
    Double getTeamAverageScore(Long sessionId, Long teamId, Long levelId);
    boolean isTeamLevelCompleted(Long sessionId, Long teamId, Long levelId);
    
    // Операции для администрирования
    List<LevelCompletion> getAllCompletionsForAdmin();
    void deleteCompletionsBySession(Long sessionId);
    void deleteCompletionsOlderThan(Instant cutoffDate);
    List<LevelCompletion> getSuspiciousCompletions(int limit);
    void recalculateScores(Long levelId);
    void recalculateAllScores();
    
    // Операции с кэшированием
    void cacheCompletion(LevelCompletion completion);
    void evictCompletionFromCache(Long completionId);
    Optional<LevelCompletion> getCachedCompletion(Long completionId);
    void cacheLevelCompletions(Long levelId, List<LevelCompletion> completions);
    void evictLevelCompletionsFromCache(Long levelId);
    
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
    List<Object[]> getCompletionStatisticsByHour(Long levelId, Instant start, Instant end);
    List<Object[]> getCompletionStatisticsByDay(Long levelId, Instant start, Instant end);
    List<Object[]> getCompletionRateAnalysis(Long levelId);
    List<Object[]> getUserCompletionSummary(Long userId);
    List<Object[]> getLevelDifficultyAnalysis(Long levelId);
    List<Object[]> getTimeDistributionAnalysis(Long levelId);
    List<Object[]> getScoreDistributionAnalysis(Long levelId);
    
    // Операции для оптимизации
    void batchCreateCompletions(List<LevelCompletion> completions);
    void batchUpdateCompletions(List<LevelCompletion> completions);
    List<LevelCompletion> getCompletionsForOptimization(int batchSize);
    
    // Вспомогательные методы
    String generateCompletionId();
    void logCompletion(LevelCompletion completion);
    Double calculateEfficiencyScore(Double timeSpent, Double score, int attempts);
    boolean isValidCompletionCode(String code, Long levelId);
    
    // Операции с достижениями
    void checkCompletionAchievements(LevelCompletion completion);
    List<String> getCompletionAchievements(Long completionId);
    boolean hasCompletionAchievement(Long userId, String achievementCode);
    
    // Операции с попытками
    int getCompletionAttempts(Long completionId);
    Double calculateAttemptEfficiency(int attempts, int maxAttempts);
    List<String> getCompletionAttemptHistory(Long completionId);
    
    // Операции с подсказками
    int getHintsUsedCount(Long completionId);
    Double calculateHintPenalty(int hintsUsed);
    List<Long> getHintsUsedInCompletion(Long completionId);
    
    // Сравнительный анализ
    List<LevelCompletion> getSimilarCompletions(LevelCompletion completion, int limit);
    Double getCompletionPercentile(LevelCompletion completion);
    List<LevelCompletion> getBetterCompletions(LevelCompletion completion);
    List<LevelCompletion> getWorseCompletions(LevelCompletion completion);
    
    // Операции с сессиями
    List<LevelCompletion> getSessionCompletionSummary(Long sessionId);
    Double getSessionCompletionPercentage(Long sessionId);
    Long getSessionTotalCompletionTime(Long sessionId);
    Double getSessionTotalScore(Long sessionId);
    
    // Операции с квестами
    List<LevelCompletion> getQuestCompletionSummary(Long questId, Long userId);
    Double getQuestCompletionPercentage(Long questId, Long userId);
    Long getQuestTotalCompletionTime(Long questId, Long userId);
    Double getQuestTotalScore(Long questId, Long userId);
    boolean isQuestFullyCompleted(Long questId, Long userId);
}