package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.LevelProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления прогрессом по уровням
 */
public interface LevelProgressService {

    // Базовые операции CRUD
    LevelProgress createProgress(LevelProgress progress);
    Optional<LevelProgress> getProgressById(UUID id);
    LevelProgress updateProgress(LevelProgress progress);
    void deleteProgress(UUID id);
    
    // Управление прогрессом
    LevelProgress startLevel(UUID sessionId, UUID userId, UUID levelId);
    LevelProgress completeLevel(UUID sessionId, UUID userId, UUID levelId);
    LevelProgress updateLevelProgress(UUID sessionId, UUID userId, UUID levelId, Double progressPercentage);
    LevelProgress pauseLevel(UUID sessionId, UUID userId, UUID levelId);
    LevelProgress resumeLevel(UUID sessionId, UUID userId, UUID levelId);
    
    // Поиск и фильтрация прогресса
    Page<LevelProgress> getAllProgress(Pageable pageable);
    List<LevelProgress> getProgressBySession(UUID sessionId);
    List<LevelProgress> getProgressByUser(UUID userId);
    List<LevelProgress> getProgressByLevel(UUID levelId);
    List<LevelProgress> getProgressBySessionAndUser(UUID sessionId, UUID userId);
    List<LevelProgress> getProgressBySessionAndLevel(UUID sessionId, UUID levelId);
    List<LevelProgress> getProgressByUserAndLevel(UUID userId, UUID levelId);
    
    // Статус прогресса
    List<LevelProgress> getInProgressLevels(UUID sessionId, UUID userId);
    List<LevelProgress> getCompletedLevels(UUID sessionId, UUID userId);
    List<LevelProgress> getPausedLevels(UUID sessionId, UUID userId);
    List<LevelProgress> getNotStartedLevels(UUID sessionId, UUID userId);
    
    // Статистика прогресса
    long getTotalProgressCount();
    long getProgressCountBySession(UUID sessionId);
    long getProgressCountByUser(UUID userId);
    long getProgressCountByLevel(UUID levelId);
    long getCompletedLevelsCount(UUID sessionId, UUID userId);
    long getInProgressLevelsCount(UUID sessionId, UUID userId);
    double getOverallProgressPercentage(UUID sessionId, UUID userId);
    double getSessionProgressPercentage(UUID sessionId);
    
    // Анализ прогресса
    List<LevelProgress> getRecentProgress(int limit);
    List<LevelProgress> getProgressByDateRange(Instant start, Instant end);
    List<LevelProgress> getSlowestProgress(UUID sessionId, int limit);
    List<LevelProgress> getFastestProgress(UUID sessionId, int limit);
    LevelProgress getCurrentLevelProgress(UUID sessionId, UUID userId);
    
    // Управление временем
    Instant getLevelStartTime(UUID sessionId, UUID userId, UUID levelId);
    Instant getLevelCompletionTime(UUID sessionId, UUID userId, UUID levelId);
    Long getLevelDuration(UUID sessionId, UUID userId, UUID levelId);
    Long getTotalTimeSpent(UUID sessionId, UUID userId);
    Double getAverageTimePerLevel(UUID sessionId, UUID userId);
    
    // Управление очками и бонусами
    LevelProgress addPoints(Long progressId, Double points, String reason);
    LevelProgress addBonus(Long progressId, Double bonus, String reason);
    LevelProgress applyPenalty(Long progressId, Double penalty, String reason);
    Double calculateLevelScore(UUID sessionId, UUID userId, UUID levelId);
    Double getTotalScore(UUID sessionId, UUID userId);
    Double getAverageScorePerLevel(UUID sessionId, UUID userId);
    
    // Валидация и бизнес-логика
    boolean canStartLevel(UUID sessionId, UUID userId, UUID levelId);
    boolean canCompleteLevel(UUID sessionId, UUID userId, UUID levelId);
    boolean canPauseLevel(UUID sessionId, UUID userId, UUID levelId);
    boolean canResumeLevel(UUID sessionId, UUID userId, UUID levelId);
    boolean isLevelAccessible(UUID sessionId, UUID userId, UUID levelId);
    boolean arePrerequisitesMet(UUID sessionId, UUID userId, UUID levelId);
    
    // Управление последовательностью уровней
    LevelProgress moveToNextLevel(UUID sessionId, UUID userId);
    LevelProgress skipLevel(UUID sessionId, UUID userId, UUID levelId, String reason);
    LevelProgress restartLevel(UUID sessionId, UUID userId, UUID levelId);
    List<UUID> getAvailableLevels(UUID sessionId, UUID userId);
    List<UUID> getCompletedLevelSequence(UUID sessionId, UUID userId);
    Long getNextLevelId(UUID sessionId, UUID userId);
    
    // Операции с сессиями
    List<LevelProgress> getSessionProgressSummary(UUID sessionId);
    List<LevelProgress> getAllUsersProgressForLevel(UUID sessionId, UUID levelId);
    LevelProgress getBestProgressForLevel(UUID sessionId, UUID levelId);
    List<LevelProgress> getTopProgressForSession(UUID sessionId, int limit);
    
    // Командные операции
    List<LevelProgress> getTeamProgress(UUID sessionId, UUID teamId);
    Double getTeamProgressPercentage(UUID sessionId, UUID teamId);
    List<LevelProgress> getTeamMembersProgress(UUID sessionId, UUID teamId, UUID levelId);
    
    // Операции для администрирования
    List<LevelProgress> getAllProgressForAdmin();
    void resetProgress(UUID sessionId, UUID userId);
    void resetSessionProgress(UUID sessionId);
    void deleteProgressOlderThan(Instant cutoffDate);
    List<LevelProgress> getStuckProgress(int hoursThreshold);
    
    // Операции с кэшированием
    void cacheProgress(LevelProgress progress);
    void evictProgressFromCache(Long progressId);
    Optional<LevelProgress> getCachedProgress(Long progressId);
    void cacheSessionProgress(UUID sessionId, List<LevelProgress> progressList);
    void evictSessionProgressFromCache(UUID sessionId);
    
    // Операции с событиями
    void publishLevelStartedEvent(LevelProgress progress);
    void publishLevelCompletedEvent(LevelProgress progress);
    void publishLevelPausedEvent(LevelProgress progress);
    void publishLevelResumedEvent(LevelProgress progress);
    void publishProgressUpdatedEvent(LevelProgress progress);
    
    // Интеграция с другими сервисами
    void updateStatistics(LevelProgress progress);
    void updateAchievements(LevelProgress progress);
    void notifyUser(LevelProgress progress);
    void notifyTeam(LevelProgress progress);
    
    // Аналитика и отчеты
    List<Object[]> getProgressStatisticsByHour(UUID sessionId, Instant start, Instant end);
    List<Object[]> getProgressStatisticsByDay(UUID sessionId, Instant start, Instant end);
    List<Object[]> getLevelCompletionRates(UUID sessionId);
    List<Object[]> getUserProgressSummary(UUID sessionId);
    List<Object[]> getLevelDifficultyAnalysis(UUID levelId);
    
    // Операции для оптимизации
    void batchCreateProgress(List<LevelProgress> progressList);
    void batchUpdateProgress(List<LevelProgress> progressList);
    List<LevelProgress> getProgressForOptimization(int batchSize);
    
    // Вспомогательные методы
    Double calculateProgressPercentage(UUID sessionId, UUID userId, UUID levelId);
    boolean isLevelCompleted(UUID sessionId, UUID userId, UUID levelId);
    boolean isLevelInProgress(UUID sessionId, UUID userId, UUID levelId);
    String generateProgressId();
    void logProgress(LevelProgress progress);
    
    // Операции с подсказками
    LevelProgress useHint(Long progressId, UUID hintId);
    int getHintsUsed(UUID sessionId, UUID userId, UUID levelId);
    Double calculateHintPenalty(UUID sessionId, UUID userId, UUID levelId);
    
    // Операции с попытками
    void incrementAttempts(Long progressId);
    int getAttemptsCount(UUID sessionId, UUID userId, UUID levelId);
    Double calculateAttemptPenalty(UUID sessionId, UUID userId, UUID levelId);
    
    // Операции с достижениями
    void checkAchievements(UUID sessionId, UUID userId, UUID levelId);
    List<String> getUnlockedAchievements(UUID sessionId, UUID userId);
    boolean hasAchievement(UUID userId, String achievementCode);
}