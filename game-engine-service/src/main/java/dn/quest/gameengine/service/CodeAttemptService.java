package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.CodeAttempt;
import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.User;
import dn.quest.shared.enums.AttemptResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления попытками ввода кодов
 */
public interface CodeAttemptService {

    // Базовые операции CRUD
    CodeAttempt createAttempt(CodeAttempt attempt);
    Optional<CodeAttempt> getAttemptById(UUID id);
    CodeAttempt updateAttempt(CodeAttempt attempt);
    void deleteAttempt(UUID id);
    
    // Основная игровая логика
    CodeAttempt submitCode(UUID sessionId, UUID userId, String code, String sector);
    CodeAttempt submitCodeWithBonus(UUID sessionId, UUID userId, String code, String sector, Double bonusMultiplier);
    AttemptResult validateCode(String submittedCode, String correctCode, String sector);
    boolean isCodeCorrect(String submittedCode, String correctCode);
    
    // Поиск и фильтрация попыток
    Page<CodeAttempt> getAllAttempts(Pageable pageable);
    List<CodeAttempt> getAttemptsBySession(UUID sessionId);
    List<CodeAttempt> getAttemptsByUser(UUID userId);
    List<CodeAttempt> getAttemptsBySessionAndUser(UUID sessionId, UUID userId);
    List<CodeAttempt> getAttemptsByResult(AttemptResult result);
    List<CodeAttempt> getAttemptsByCode(String code);
    List<CodeAttempt> getAttemptsBySector(String sector);
    
    // Статистика попыток
    long getTotalAttemptsCount();
    long getAttemptsCountBySession(UUID sessionId);
    long getAttemptsCountByUser(UUID userId);
    long getAttemptsCountByResult(AttemptResult result);
    long getCorrectAttemptsCount(UUID sessionId);
    long getIncorrectAttemptsCount(UUID sessionId);
    double getSuccessRate(UUID sessionId);
    double getSuccessRateByUser(UUID userId);
    
    // Анализ попыток
    List<CodeAttempt> getRecentAttempts(int limit);
    List<CodeAttempt> getAttemptsByDateRange(Instant start, Instant end);
    List<CodeAttempt> getAttemptsBySessionAndDateRange(UUID sessionId, Instant start, Instant end);
    List<CodeAttempt> getFailedAttempts(UUID sessionId, int limit);
    List<CodeAttempt> getSuccessfulAttempts(UUID sessionId, int limit);
    
    // Управление бонусами и штрафами
    CodeAttempt applyBonus(Long attemptId, Double bonusAmount, String reason);
    CodeAttempt applyPenalty(Long attemptId, Double penaltyAmount, String reason);
    Double calculateBonusMultiplier(String sector, String codeType);
    Double calculatePenaltyMultiplier(int consecutiveFailures);
    
    // Валидация и бизнес-логика
    boolean canSubmitAttempt(UUID sessionId, UUID userId);
    boolean isAttemptLimitReached(UUID sessionId, UUID userId);
    int getRemainingAttempts(UUID sessionId, UUID userId);
    boolean isCooldownActive(UUID sessionId, UUID userId);
    long getCooldownRemainingSeconds(UUID sessionId, UUID userId);
    
    // Анализ паттернов
    List<String> getMostCommonIncorrectCodes(UUID sessionId);
    List<String> getMostCommonSectors(UUID sessionId);
    double getAverageAttemptsPerCode(UUID sessionId);
    List<CodeAttempt> getConsecutiveFailures(UUID sessionId, UUID userId);
    
    // Операции с сессиями
    List<CodeAttempt> getSessionAttemptsSummary(UUID sessionId);
    CodeAttempt getFirstCorrectAttempt(UUID sessionId, UUID levelId);
    CodeAttempt getLastAttempt(UUID sessionId, UUID userId);
    List<CodeAttempt> getAttemptsSinceLastCorrect(UUID sessionId, UUID userId);
    
    // Управление временем
    CodeAttempt updateAttemptTimestamp(Long attemptId);
    Instant getAverageTimeBetweenAttempts(UUID sessionId, UUID userId);
    List<CodeAttempt> getAttemptsInTimeWindow(UUID sessionId, Instant start, Instant end);
    
    // Операции для администрирования
    List<CodeAttempt> getAllAttemptsForAdmin();
    void deleteAttemptsBySession(UUID sessionId);
    void deleteAttemptsOlderThan(Instant cutoffDate);
    List<CodeAttempt> getSuspiciousAttempts(int limit);
    
    // Операции с кэшированием
    void cacheAttempt(CodeAttempt attempt);
    void evictAttemptFromCache(Long attemptId);
    Optional<CodeAttempt> getCachedAttempt(Long attemptId);
    void cacheSessionAttempts(UUID sessionId, List<CodeAttempt> attempts);
    
    // Операции с событиями
    void publishCodeSubmittedEvent(CodeAttempt attempt);
    void publishCodeCorrectEvent(CodeAttempt attempt);
    void publishCodeIncorrectEvent(CodeAttempt attempt);
    void publishBonusAppliedEvent(CodeAttempt attempt, Double bonusAmount);
    void publishPenaltyAppliedEvent(CodeAttempt attempt, Double penaltyAmount);
    
    // Интеграция с другими сервисами
    void updateStatistics(CodeAttempt attempt);
    void updateLeaderboard(CodeAttempt attempt);
    void notifyUser(CodeAttempt attempt);
    void notifyTeam(CodeAttempt attempt);
    
    // Аналитика и отчеты
    List<Object[]> getAttemptsStatisticsByHour(UUID sessionId, Instant start, Instant end);
    List<Object[]> getAttemptsStatisticsByDay(UUID sessionId, Instant start, Instant end);
    List<Object[]> getSectorSuccessRate(UUID sessionId);
    List<Object[]> getUserAttemptSummary(UUID sessionId);
    
    // Операции для оптимизации
    void batchCreateAttempts(List<CodeAttempt> attempts);
    void batchUpdateAttempts(List<CodeAttempt> attempts);
    List<CodeAttempt> getAttemptsForOptimization(int batchSize);
    
    // Вспомогательные методы
    String normalizeCode(String code);
    boolean isValidCodeFormat(String code);
    boolean isValidSector(String sector);
    String generateAttemptId();
    void logAttempt(CodeAttempt attempt);
}