package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.LevelProgress;
import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления прогрессом по уровням
 */
public interface LevelProgressService {

    // Базовые операции CRUD
    LevelProgress createProgress(LevelProgress progress);
    Optional<LevelProgress> getProgressById(Long id);
    LevelProgress updateProgress(LevelProgress progress);
    void deleteProgress(Long id);
    
    // Управление прогрессом
    LevelProgress startLevel(Long sessionId, Long userId, Long levelId);
    LevelProgress completeLevel(Long sessionId, Long userId, Long levelId);
    LevelProgress updateLevelProgress(Long sessionId, Long userId, Long levelId, Double progressPercentage);
    LevelProgress pauseLevel(Long sessionId, Long userId, Long levelId);
    LevelProgress resumeLevel(Long sessionId, Long userId, Long levelId);
    
    // Поиск и фильтрация прогресса
    Page<LevelProgress> getAllProgress(Pageable pageable);
    List<LevelProgress> getProgressBySession(Long sessionId);
    List<LevelProgress> getProgressByUser(Long userId);
    List<LevelProgress> getProgressByLevel(Long levelId);
    List<LevelProgress> getProgressBySessionAndUser(Long sessionId, Long userId);
    List<LevelProgress> getProgressBySessionAndLevel(Long sessionId, Long levelId);
    List<LevelProgress> getProgressByUserAndLevel(Long userId, Long levelId);
    
    // Статус прогресса
    List<LevelProgress> getInProgressLevels(Long sessionId, Long userId);
    List<LevelProgress> getCompletedLevels(Long sessionId, Long userId);
    List<LevelProgress> getPausedLevels(Long sessionId, Long userId);
    List<LevelProgress> getNotStartedLevels(Long sessionId, Long userId);
    
    // Статистика прогресса
    long getTotalProgressCount();
    long getProgressCountBySession(Long sessionId);
    long getProgressCountByUser(Long userId);
    long getProgressCountByLevel(Long levelId);
    long getCompletedLevelsCount(Long sessionId, Long userId);
    long getInProgressLevelsCount(Long sessionId, Long userId);
    double getOverallProgressPercentage(Long sessionId, Long userId);
    double getSessionProgressPercentage(Long sessionId);
    
    // Анализ прогресса
    List<LevelProgress> getRecentProgress(int limit);
    List<LevelProgress> getProgressByDateRange(Instant start, Instant end);
    List<LevelProgress> getSlowestProgress(Long sessionId, int limit);
    List<LevelProgress> getFastestProgress(Long sessionId, int limit);
    LevelProgress getCurrentLevelProgress(Long sessionId, Long userId);
    
    // Управление временем
    Instant getLevelStartTime(Long sessionId, Long userId, Long levelId);
    Instant getLevelCompletionTime(Long sessionId, Long userId, Long levelId);
    Long getLevelDuration(Long sessionId, Long userId, Long levelId);
    Long getTotalTimeSpent(Long sessionId, Long userId);
    Double getAverageTimePerLevel(Long sessionId, Long userId);
    
    // Управление очками и бонусами
    LevelProgress addPoints(Long progressId, Double points, String reason);
    LevelProgress addBonus(Long progressId, Double bonus, String reason);
    LevelProgress applyPenalty(Long progressId, Double penalty, String reason);
    Double calculateLevelScore(Long sessionId, Long userId, Long levelId);
    Double getTotalScore(Long sessionId, Long userId);
    Double getAverageScorePerLevel(Long sessionId, Long userId);
    
    // Валидация и бизнес-логика
    boolean canStartLevel(Long sessionId, Long userId, Long levelId);
    boolean canCompleteLevel(Long sessionId, Long userId, Long levelId);
    boolean canPauseLevel(Long sessionId, Long userId, Long levelId);
    boolean canResumeLevel(Long sessionId, Long userId, Long levelId);
    boolean isLevelAccessible(Long sessionId, Long userId, Long levelId);
    boolean arePrerequisitesMet(Long sessionId, Long userId, Long levelId);
    
    // Управление последовательностью уровней
    LevelProgress moveToNextLevel(Long sessionId, Long userId);
    LevelProgress skipLevel(Long sessionId, Long userId, Long levelId, String reason);
    LevelProgress restartLevel(Long sessionId, Long userId, Long levelId);
    List<Long> getAvailableLevels(Long sessionId, Long userId);
    List<Long> getCompletedLevelSequence(Long sessionId, Long userId);
    Long getNextLevelId(Long sessionId, Long userId);
    
    // Операции с сессиями
    List<LevelProgress> getSessionProgressSummary(Long sessionId);
    List<LevelProgress> getAllUsersProgressForLevel(Long sessionId, Long levelId);
    LevelProgress getBestProgressForLevel(Long sessionId, Long levelId);
    List<LevelProgress> getTopProgressForSession(Long sessionId, int limit);
    
    // Командные операции
    List<LevelProgress> getTeamProgress(Long sessionId, Long teamId);
    Double getTeamProgressPercentage(Long sessionId, Long teamId);
    List<LevelProgress> getTeamMembersProgress(Long sessionId, Long teamId, Long levelId);
    
    // Операции для администрирования
    List<LevelProgress> getAllProgressForAdmin();
    void resetProgress(Long sessionId, Long userId);
    void resetSessionProgress(Long sessionId);
    void deleteProgressOlderThan(Instant cutoffDate);
    List<LevelProgress> getStuckProgress(int hoursThreshold);
    
    // Операции с кэшированием
    void cacheProgress(LevelProgress progress);
    void evictProgressFromCache(Long progressId);
    Optional<LevelProgress> getCachedProgress(Long progressId);
    void cacheSessionProgress(Long sessionId, List<LevelProgress> progressList);
    void evictSessionProgressFromCache(Long sessionId);
    
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
    List<Object[]> getProgressStatisticsByHour(Long sessionId, Instant start, Instant end);
    List<Object[]> getProgressStatisticsByDay(Long sessionId, Instant start, Instant end);
    List<Object[]> getLevelCompletionRates(Long sessionId);
    List<Object[]> getUserProgressSummary(Long sessionId);
    List<Object[]> getLevelDifficultyAnalysis(Long levelId);
    
    // Операции для оптимизации
    void batchCreateProgress(List<LevelProgress> progressList);
    void batchUpdateProgress(List<LevelProgress> progressList);
    List<LevelProgress> getProgressForOptimization(int batchSize);
    
    // Вспомогательные методы
    Double calculateProgressPercentage(Long sessionId, Long userId, Long levelId);
    boolean isLevelCompleted(Long sessionId, Long userId, Long levelId);
    boolean isLevelInProgress(Long sessionId, Long userId, Long levelId);
    String generateProgressId();
    void logProgress(LevelProgress progress);
    
    // Операции с подсказками
    LevelProgress useHint(Long progressId, Long hintId);
    int getHintsUsed(Long sessionId, Long userId, Long levelId);
    Double calculateHintPenalty(Long sessionId, Long userId, Long levelId);
    
    // Операции с попытками
    void incrementAttempts(Long progressId);
    int getAttemptsCount(Long sessionId, Long userId, Long levelId);
    Double calculateAttemptPenalty(Long sessionId, Long userId, Long levelId);
    
    // Операции с достижениями
    void checkAchievements(Long sessionId, Long userId, Long levelId);
    List<String> getUnlockedAchievements(Long sessionId, Long userId);
    boolean hasAchievement(Long userId, String achievementCode);
}