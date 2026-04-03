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
import java.util.UUID;

/**
 * Сервис для управления лидербордами и рейтингами
 */
public interface LeaderboardService {

    // Базовые операции с лидербордами
    Map<String, Object> getSessionLeaderboard(UUID sessionId, Pageable pageable);
    Map<String, Object> getQuestLeaderboard(UUID questId, Pageable pageable);
    Map<String, Object> getGlobalLeaderboard(Pageable pageable);
    Map<String, Object> getTeamLeaderboard(UUID teamId, Pageable pageable);
    
    // Лидерборды по разным метрикам
    Map<String, Object> getSessionLeaderboardByScore(UUID sessionId, Pageable pageable);
    Map<String, Object> getSessionLeaderboardByTime(UUID sessionId, Pageable pageable);
    Map<String, Object> getSessionLeaderboardByEfficiency(UUID sessionId, Pageable pageable);
    Map<String, Object> getSessionLeaderboardByCompletionRate(UUID sessionId, Pageable pageable);
    
    // Пользовательские рейтинги
    Integer getUserRankingInSession(UUID sessionId, UUID userId);
    Integer getUserRankingInQuest(UUID questId, UUID userId);
    Integer getUserGlobalRanking(UUID userId);
    Integer getUserRankingByScore(UUID sessionId, UUID userId);
    Integer getUserRankingByTime(UUID sessionId, UUID userId);
    
    // Командные рейтинги
    Integer getTeamRankingInSession(UUID sessionId, UUID teamId);
    Integer getTeamRankingInQuest(UUID questId, UUID teamId);
    Integer getTeamGlobalRanking(UUID teamId);
    Map<String, Object> getTeamLeaderboardByScore(UUID sessionId, Pageable pageable);
    Map<String, Object> getTeamLeaderboardByTime(UUID sessionId, Pageable pageable);
    
    // Статистика лидербордов
    long getTotalParticipantsInSession(UUID sessionId);
    long getTotalParticipantsInQuest(UUID questId);
    double getAverageScoreInSession(UUID sessionId);
    double getAverageTimeInSession(UUID sessionId);
    double getAverageCompletionRateInSession(UUID sessionId);
    
    // Исторические лидерборды
    Map<String, Object> getSessionLeaderboardAtTime(UUID sessionId, Instant timestamp, Pageable pageable);
    Map<String, Object> getQuestLeaderboardAtTime(UUID questId, Instant timestamp, Pageable pageable);
    List<Map<String, Object>> getSessionLeaderboardHistory(UUID sessionId, Instant start, Instant end);
    List<Map<String, Object>> getQuestLeaderboardHistory(UUID questId, Instant start, Instant end);
    
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
    Map<String, Object> getUserRelativeLeaderboard(UUID userId, UUID sessionId, int radius);
    Map<String, Object> getUserFriendsLeaderboard(UUID userId, UUID sessionId, Pageable pageable);
    Map<String, Object> getUserSimilarLevelLeaderboard(UUID userId, UUID sessionId, Pageable pageable);
    
    // Операции с кэшированием лидербордов
    void cacheSessionLeaderboard(UUID sessionId, Map<String, Object> leaderboard);
    void cacheQuestLeaderboard(UUID questId, Map<String, Object> leaderboard);
    void cacheGlobalLeaderboard(Map<String, Object> leaderboard);
    Optional<Map<String, Object>> getCachedSessionLeaderboard(UUID sessionId);
    Optional<Map<String, Object>> getCachedQuestLeaderboard(UUID questId);
    Optional<Map<String, Object>> getCachedGlobalLeaderboard();
    void evictLeaderboardCache(UUID sessionId);
    void evictAllLeaderboardCache();
    
    // Обновление лидербордов
    void updateSessionLeaderboard(UUID sessionId);
    void updateQuestLeaderboard(UUID questId);
    void updateGlobalLeaderboard();
    void updateTeamLeaderboard(UUID teamId);
    void updateUserRanking(UUID userId, UUID sessionId);
    void updateTeamRanking(UUID teamId, UUID sessionId);
    
    // Операции с событиями
    void publishLeaderboardUpdatedEvent(String leaderboardType, Long entityId);
    void publishRankingChangedEvent(UUID userId, UUID sessionId, Integer oldRank, Integer newRank);
    void publishTeamRankingChangedEvent(UUID teamId, UUID sessionId, Integer oldRank, Integer newRank);
    
    // Аналитика лидербордов
    List<Object[]> getLeaderboardStatistics(UUID sessionId);
    List<Object[]> getRankingDistribution(UUID sessionId);
    List<Object[]> getScoreDistribution(UUID sessionId);
    List<Object[]> getTimeDistribution(UUID sessionId);
    List<Object[]> getUserRankingHistory(UUID userId, UUID sessionId, Instant start, Instant end);
    
    // Операции для администрирования
    List<Map<String, Object>> getAllLeaderboards();
    void resetLeaderboard(UUID sessionId);
    void resetAllLeaderboards();
    void recalculateLeaderboard(UUID sessionId);
    void recalculateAllLeaderboards();
    List<Map<String, Object>> getLeaderboardPerformanceMetrics();
    
    // Валидация и бизнес-логика
    boolean isUserEligibleForLeaderboard(UUID userId, UUID sessionId);
    boolean isTeamEligibleForLeaderboard(UUID teamId, UUID sessionId);
    boolean isValidLeaderboardEntry(Map<String, Object> entry);
    boolean isLeaderboardStale(UUID sessionId, Instant lastUpdate);
    
    // Операции с фильтрацией
    Map<String, Object> getFilteredLeaderboard(UUID sessionId, List<String> filters, Pageable pageable);
    Map<String, Object> getLeaderboardWithFilters(
        UUID sessionId,
        List<Long> userIds,
        List<Long> teamIds,
        String difficulty,
        String questType,
        Instant startDate,
        Instant endDate,
        Pageable pageable
    );
    
    // Операции с экспортом
    List<Map<String, Object>> exportLeaderboard(UUID sessionId);
    List<Map<String, Object>> exportQuestLeaderboard(UUID questId);
    List<Map<String, Object>> exportGlobalLeaderboard();
    String generateLeaderboardReport(UUID sessionId);
    String generateQuestLeaderboardReport(UUID questId);
    
    // Операции с достижениями
    List<Map<String, Object>> getTopAchievers(UUID sessionId, int limit);
    List<Map<String, Object>> getMostImprovedUsers(UUID sessionId, Instant start, Instant end, int limit);
    List<Map<String, Object>> getConsistentPerformers(UUID sessionId, int limit);
    
    // Операции с предсказаниями
    Map<String, Object> predictUserRanking(UUID userId, UUID sessionId);
    Map<String, Object> predictTeamRanking(UUID teamId, UUID sessionId);
    List<Map<String, Object>> getTrendingUsers(UUID sessionId, int limit);
    List<Map<String, Object>> getTrendingTeams(UUID sessionId, int limit);
    
    // Операции с сравнением
    Map<String, Object> compareUsers(UUID userId1, UUID userId2, UUID sessionId);
    Map<String, Object> compareTeams(UUID teamId1, UUID teamId2, UUID sessionId);
    Map<String, Object> compareSessions(UUID sessionId1, UUID sessionId2);
    
    // Операции с агрегацией
    Map<String, Object> getAggregatedLeaderboard(List<Long> sessionIds, Pageable pageable);
    Map<String, Object> getMultiQuestLeaderboard(List<Long> questIds, Pageable pageable);
    Map<String, Object> getCrossPlatformLeaderboard(Pageable pageable);
    
    // Операции с реальным временем
    void enableRealTimeUpdates(UUID sessionId);
    void disableRealTimeUpdates(UUID sessionId);
    boolean isRealTimeUpdatesEnabled(UUID sessionId);
    void subscribeToLeaderboardUpdates(UUID sessionId, String subscriptionId);
    void unsubscribeFromLeaderboardUpdates(String subscriptionId);
    
    // Вспомогательные методы
    double calculateUserScore(UUID userId, UUID sessionId);
    double calculateTeamScore(UUID teamId, UUID sessionId);
    double calculateEfficiencyScore(UUID userId, UUID sessionId);
    double calculateCompletionRate(UUID userId, UUID sessionId);
    String generateLeaderboardKey(String type, Long entityId);
    void logLeaderboardUpdate(String operation, Long entityId);
    
    // Операции с оптимизацией
    void optimizeLeaderboardCalculation(UUID sessionId);
    void precomputeLeaderboards(List<Long> sessionIds);
    List<Long> getPopularLeaderboards(int limit);
    void scheduleLeaderboardUpdate(UUID sessionId, Instant nextUpdate);
    
    // Операции с безопасностью
    boolean isLeaderboardAccessAllowed(UUID userId, UUID sessionId);
    boolean isLeaderboardModificationAllowed(UUID userId, UUID sessionId);
    void validateLeaderboardData(Map<String, Object> data);
    void sanitizeLeaderboardData(Map<String, Object> data);
    
    // Операции с мониторингом
    Map<String, Object> getLeaderboardHealthMetrics();
    List<String> getLeaderboardErrors(UUID sessionId);
    Map<String, Object> getLeaderboardPerformanceStats();
    void monitorLeaderboardUpdates();
}