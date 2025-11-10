package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.User;
import dn.quest.gameengine.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис для управления лидербордами и рейтингами
 */
public interface LeaderboardService {

    // Базовые операции с лидербордами
    Map<String, Object> getSessionLeaderboard(Long sessionId, Pageable pageable);
    Map<String, Object> getQuestLeaderboard(Long questId, Pageable pageable);
    Map<String, Object> getGlobalLeaderboard(Pageable pageable);
    Map<String, Object> getTeamLeaderboard(Long teamId, Pageable pageable);
    
    // Лидерборды по разным метрикам
    Map<String, Object> getSessionLeaderboardByScore(Long sessionId, Pageable pageable);
    Map<String, Object> getSessionLeaderboardByTime(Long sessionId, Pageable pageable);
    Map<String, Object> getSessionLeaderboardByEfficiency(Long sessionId, Pageable pageable);
    Map<String, Object> getSessionLeaderboardByCompletionRate(Long sessionId, Pageable pageable);
    
    // Пользовательские рейтинги
    Integer getUserRankingInSession(Long sessionId, Long userId);
    Integer getUserRankingInQuest(Long questId, Long userId);
    Integer getUserGlobalRanking(Long userId);
    Integer getUserRankingByScore(Long sessionId, Long userId);
    Integer getUserRankingByTime(Long sessionId, Long userId);
    
    // Командные рейтинги
    Integer getTeamRankingInSession(Long sessionId, Long teamId);
    Integer getTeamRankingInQuest(Long questId, Long teamId);
    Integer getTeamGlobalRanking(Long teamId);
    Map<String, Object> getTeamLeaderboardByScore(Long sessionId, Pageable pageable);
    Map<String, Object> getTeamLeaderboardByTime(Long sessionId, Pageable pageable);
    
    // Статистика лидербордов
    long getTotalParticipantsInSession(Long sessionId);
    long getTotalParticipantsInQuest(Long questId);
    double getAverageScoreInSession(Long sessionId);
    double getAverageTimeInSession(Long sessionId);
    double getAverageCompletionRateInSession(Long sessionId);
    
    // Исторические лидерборды
    Map<String, Object> getSessionLeaderboardAtTime(Long sessionId, Instant timestamp, Pageable pageable);
    Map<String, Object> getQuestLeaderboardAtTime(Long questId, Instant timestamp, Pageable pageable);
    List<Map<String, Object>> getSessionLeaderboardHistory(Long sessionId, Instant start, Instant end);
    List<Map<String, Object>> getQuestLeaderboardHistory(Long questId, Instant start, Instant end);
    
    // Лидерборды по периодам
    Map<String, Object> getDailyLeaderboard(Instant date, Pageable pageable);
    Map<String, Object> getWeeklyLeaderboard(Instant startOfWeek, Pageable pageable);
    Map<String, Object> getMonthlyLeaderboard(Instant startOfMonth, Pageable pageable);
    Map<String, Object> getYearlyLeaderboard(Instant startOfYear, Pageable pageable);
    
    // Лидерборды по категориям
    Map<String, Object> getLeaderboardByDifficulty(String difficulty, Pageable pageable);
    Map<String, Object> getLeaderboardByQuestType(String questType, Pageable pageable);
    Map<String, Object> getLeaderboardByRegion(String region, Pageable pageable);
    Map<String, Object> getLeaderboardByUserLevel(String userLevel, Pageable pageable);
    
    // Персонализированные лидерборды
    Map<String, Object> getUserRelativeLeaderboard(Long userId, Long sessionId, int radius);
    Map<String, Object> getUserFriendsLeaderboard(Long userId, Long sessionId, Pageable pageable);
    Map<String, Object> getUserSimilarLevelLeaderboard(Long userId, Long sessionId, Pageable pageable);
    
    // Операции с кэшированием лидербордов
    void cacheSessionLeaderboard(Long sessionId, Map<String, Object> leaderboard);
    void cacheQuestLeaderboard(Long questId, Map<String, Object> leaderboard);
    void cacheGlobalLeaderboard(Map<String, Object> leaderboard);
    Optional<Map<String, Object>> getCachedSessionLeaderboard(Long sessionId);
    Optional<Map<String, Object>> getCachedQuestLeaderboard(Long questId);
    Optional<Map<String, Object>> getCachedGlobalLeaderboard();
    void evictLeaderboardCache(Long sessionId);
    void evictAllLeaderboardCache();
    
    // Обновление лидербордов
    void updateSessionLeaderboard(Long sessionId);
    void updateQuestLeaderboard(Long questId);
    void updateGlobalLeaderboard();
    void updateTeamLeaderboard(Long teamId);
    void updateUserRanking(Long userId, Long sessionId);
    void updateTeamRanking(Long teamId, Long sessionId);
    
    // Операции с событиями
    void publishLeaderboardUpdatedEvent(String leaderboardType, Long entityId);
    void publishRankingChangedEvent(Long userId, Long sessionId, Integer oldRank, Integer newRank);
    void publishTeamRankingChangedEvent(Long teamId, Long sessionId, Integer oldRank, Integer newRank);
    
    // Аналитика лидербордов
    List<Object[]> getLeaderboardStatistics(Long sessionId);
    List<Object[]> getRankingDistribution(Long sessionId);
    List<Object[]> getScoreDistribution(Long sessionId);
    List<Object[]> getTimeDistribution(Long sessionId);
    List<Object[]> getUserRankingHistory(Long userId, Long sessionId, Instant start, Instant end);
    
    // Операции для администрирования
    List<Map<String, Object>> getAllLeaderboards();
    void resetLeaderboard(Long sessionId);
    void resetAllLeaderboards();
    void recalculateLeaderboard(Long sessionId);
    void recalculateAllLeaderboards();
    List<Map<String, Object>> getLeaderboardPerformanceMetrics();
    
    // Валидация и бизнес-логика
    boolean isUserEligibleForLeaderboard(Long userId, Long sessionId);
    boolean isTeamEligibleForLeaderboard(Long teamId, Long sessionId);
    boolean isValidLeaderboardEntry(Map<String, Object> entry);
    boolean isLeaderboardStale(Long sessionId, Instant lastUpdate);
    
    // Операции с фильтрацией
    Map<String, Object> getFilteredLeaderboard(Long sessionId, List<String> filters, Pageable pageable);
    Map<String, Object> getLeaderboardWithFilters(
        Long sessionId,
        List<Long> userIds,
        List<Long> teamIds,
        String difficulty,
        String questType,
        Instant startDate,
        Instant endDate,
        Pageable pageable
    );
    
    // Операции с экспортом
    List<Map<String, Object>> exportLeaderboard(Long sessionId);
    List<Map<String, Object>> exportQuestLeaderboard(Long questId);
    List<Map<String, Object>> exportGlobalLeaderboard();
    String generateLeaderboardReport(Long sessionId);
    String generateQuestLeaderboardReport(Long questId);
    
    // Операции с достижениями
    List<Map<String, Object>> getTopAchievers(Long sessionId, int limit);
    List<Map<String, Object>> getMostImprovedUsers(Long sessionId, Instant start, Instant end, int limit);
    List<Map<String, Object>> getConsistentPerformers(Long sessionId, int limit);
    
    // Операции с предсказаниями
    Map<String, Object> predictUserRanking(Long userId, Long sessionId);
    Map<String, Object> predictTeamRanking(Long teamId, Long sessionId);
    List<Map<String, Object>> getTrendingUsers(Long sessionId, int limit);
    List<Map<String, Object>> getTrendingTeams(Long sessionId, int limit);
    
    // Операции с сравнением
    Map<String, Object> compareUsers(Long userId1, Long userId2, Long sessionId);
    Map<String, Object> compareTeams(Long teamId1, Long teamId2, Long sessionId);
    Map<String, Object> compareSessions(Long sessionId1, Long sessionId2);
    
    // Операции с агрегацией
    Map<String, Object> getAggregatedLeaderboard(List<Long> sessionIds, Pageable pageable);
    Map<String, Object> getMultiQuestLeaderboard(List<Long> questIds, Pageable pageable);
    Map<String, Object> getCrossPlatformLeaderboard(Pageable pageable);
    
    // Операции с реальным временем
    void enableRealTimeUpdates(Long sessionId);
    void disableRealTimeUpdates(Long sessionId);
    boolean isRealTimeUpdatesEnabled(Long sessionId);
    void subscribeToLeaderboardUpdates(Long sessionId, String subscriptionId);
    void unsubscribeFromLeaderboardUpdates(String subscriptionId);
    
    // Вспомогательные методы
    double calculateUserScore(Long userId, Long sessionId);
    double calculateTeamScore(Long teamId, Long sessionId);
    double calculateEfficiencyScore(Long userId, Long sessionId);
    double calculateCompletionRate(Long userId, Long sessionId);
    String generateLeaderboardKey(String type, Long entityId);
    void logLeaderboardUpdate(String operation, Long entityId);
    
    // Операции с оптимизацией
    void optimizeLeaderboardCalculation(Long sessionId);
    void precomputeLeaderboards(List<Long> sessionIds);
    List<Long> getPopularLeaderboards(int limit);
    void scheduleLeaderboardUpdate(Long sessionId, Instant nextUpdate);
    
    // Операции с безопасностью
    boolean isLeaderboardAccessAllowed(Long userId, Long sessionId);
    boolean isLeaderboardModificationAllowed(Long userId, Long sessionId);
    void validateLeaderboardData(Map<String, Object> data);
    void sanitizeLeaderboardData(Map<String, Object> data);
    
    // Операции с мониторингом
    Map<String, Object> getLeaderboardHealthMetrics();
    List<String> getLeaderboardErrors(Long sessionId);
    Map<String, Object> getLeaderboardPerformanceStats();
    void monitorLeaderboardUpdates();
}