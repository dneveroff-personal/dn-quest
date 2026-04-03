package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.LevelProgress;
import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.User;
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
    Optional<LevelProgress> getProgressById(Long id);
    LevelProgress updateProgress(LevelProgress progress);
    void deleteProgress(Long id);
    
    // Управление прогрессом
    LevelProgress startLevel(Long sessionId, UUID userId, Long levelId);
    LevelProgress completeLevel(Long sessionId, UUID userId, Long levelId);
    LevelProgress updateLevelProgress(Long sessionId, UUID userId, Long levelId, Double progressPercentage);
    LevelProgress pauseLevel(Long sessionId, UUID userId, Long levelId);
    LevelProgress resumeLevel(Long sessionId, UUID userId, Long levelId);
    
    // Поиск и фильтрация прогресса
    Page<LevelProgress> getAllProgress(Pageable pageable);
    List<LevelProgress> getProgressBySession(Long sessionId);
    List<LevelProgress> getProgressByUser(UUID userId);
    List<LevelProgress> getProgressByLevel(Long levelId);
    List<LevelProgress> getProgressBySessionAndUser(Long sessionId, UUID userId);
    List<LevelProgress> getProgressBySessionAndLevel(Long sessionId, Long levelId);
    List<LevelProgress> getProgressByUserAndLevel(UUID userId, Long levelId);
    
    // Статус прогресса
    List<LevelProgress> getInProgressLevels(Long sessionId, UUID userId);
    List<LevelProgress> getCompletedLevels(Long sessionId, UUID userId);
    List<LevelProgress> getPausedLevels(Long sessionId, UUID userId);
    List<LevelProgress> getNotStartedLevels(Long sessionId, UUID userId);
    
    // Статистика прогресса
    long getTotalProgressCount();
    long getProgressCountBySession(Long sessionId);
    long getProgressCountByUser(UUID userId);
    long getProgressCountByLevel(Long levelId);
    long getCompletedLevelsCount(Long sessionId, UUID userId);
    long getInProgressLevelsCount(Long sessionId, UUID userId);
    double getOverallProgressPercentage(Long sessionId, UUID userId);
    double getSessionProgressPercentage(Long sessionId);
    
    // Анализ прогресса
    List<LevelProgress> getRecentProgress(int limit);
    List<LevelProgress> getProgressByDateRange(Instant start, Instant end);
    List<LevelProgress> getSlowestProgress(Long sessionId, int limit);
    List<LevelProgress> getFastestProgress(Long sessionId, int limit);
    LevelProgress getCurrentLevelProgress(Long sessionId, UUID userId);
    
    // Управление временем
    Instant getLevelStartTime(Long sessionId, UUID userId, Long levelId);
    Instant getLevelCompletionTime(Long sessionId, UUID userId, Long levelId);
    Long getLevelDuration(Long sessionId, UUID userId, Long levelId);
    Long getTotalTimeSpent(Long sessionId, UUID userId);
    Double getAverageTimePerLevel(Long sessionId, UUID userId);
    
    // Управление очками и бонусами
    LevelProgress addPoints(Long progressId, Double points, String reason);
    LevelProgress addBonus(Long progressId, Double bonus, String reason);
    LevelProgress applyPenalty(Long progressId, Double penalty, String reason);
    Double calculateLevelScore(Long sessionId, UUID userId, Long levelId);
    Double getTotalScore(Long sessionId, UUID userId);
    Double getAverageScorePerLevel(Long sessionId, UUID userId);
    
    // Валидация и бизнес-логика
    boolean canStartLevel(Long sessionId, UUID userId, Long levelId);
    boolean canCompleteLevel(Long sessionId, UUID userId, Long levelId);
    boolean canPauseLevel(Long sessionId, UUID userId, Long levelId);
    boolean canResumeLevel(Long sessionId, UUID userId, Long levelId);
    boolean isLevelAccessible(Long sessionId, UUID userId, Long levelId);
    boolean arePrerequisitesMet(Long sessionId, UUID userId, Long levelId);
    
    // Управление последовательностью уровней
    LevelProgress moveToNextLevel(Long sessionId, UUID userId);
    LevelProgress skipLevel(Long sessionId, UUID userId, Long levelId, String reason);
    LevelProgress restartLevel(Long sessionId, UUID userId, Long levelId);
    List<Long> getAvailableLevels(Long sessionId, UUID userId);
    List<Long> getCompletedLevelSequence(Long sessionId, UUID userId);
    Long getNextLevelId(Long sessionId, UUID userId);
    
    // Операции с сессиями
    List<LevelProgress> getSessionProgressSummary(Long sessionId);
    List<LevelProgress> getAllUsersProgressForLevel(Long sessionId, Long levelId);
    LevelProgress getBestProgressForLevel(Long sessionId, Long levelId);
    List<LevelProgress> getTopProgressForSession(Long sessionId, int limit);
    
    // Командные операции
    List<LevelProgress> getTeamProgress(Long sessionId, UUID teamId);
    Double getTeamProgressPercentage(Long sessionId, UUID teamId);
    List<LevelProgress> getTeamMembersProgress(Long sessionId, UUID teamId, Long levelId);
    
    // Операции для администрирования
    List<LevelProgress> getAllProgressForAdmin();
    void resetProgress(Long sessionId, UUID userId);
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
    Double calculateProgressPercentage(Long sessionId, UUID userId, Long levelId);
    boolean isLevelCompleted(Long sessionId, UUID userId, Long levelId);
    boolean isLevelInProgress(Long sessionId, UUID userId, Long levelId);
    String generateProgressId();
    void logProgress(LevelProgress progress);
    
    // Операции с подсказками
    LevelProgress useHint(Long progressId, Long hintId);
    int getHintsUsed(Long sessionId, UUID userId, Long levelId);
    Double calculateHintPenalty(Long sessionId, UUID userId, Long levelId);
    
    // Операции с попытками
    void incrementAttempts(Long progressId);
    int getAttemptsCount(Long sessionId, UUID userId, Long levelId);
    Double calculateAttemptPenalty(Long sessionId, UUID userId, Long levelId);
    
    // Операции с достижениями
    void checkAchievements(Long sessionId, UUID userId, Long levelId);
    List<String> getUnlockedAchievements(Long sessionId, UUID userId);
    boolean hasAchievement(UUID userId, String achievementCode);
}