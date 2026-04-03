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
    Optional<CodeAttempt> getAttemptById(Long id);
    CodeAttempt updateAttempt(CodeAttempt attempt);
    void deleteAttempt(Long id);
    
    // Основная игровая логика
    CodeAttempt submitCode(Long sessionId, UUID userId, String code, String sector);
    CodeAttempt submitCodeWithBonus(Long sessionId, UUID userId, String code, String sector, Double bonusMultiplier);
    AttemptResult validateCode(String submittedCode, String correctCode, String sector);
    boolean isCodeCorrect(String submittedCode, String correctCode);
    
    // Поиск и фильтрация попыток
    Page<CodeAttempt> getAllAttempts(Pageable pageable);
    List<CodeAttempt> getAttemptsBySession(Long sessionId);
    List<CodeAttempt> getAttemptsByUser(UUID userId);
    List<CodeAttempt> getAttemptsBySessionAndUser(Long sessionId, UUID userId);
    List<CodeAttempt> getAttemptsByResult(AttemptResult result);
    List<CodeAttempt> getAttemptsByCode(String code);
    List<CodeAttempt> getAttemptsBySector(String sector);
    
    // Статистика попыток
    long getTotalAttemptsCount();
    long getAttemptsCountBySession(Long sessionId);
    long getAttemptsCountByUser(UUID userId);
    long getAttemptsCountByResult(AttemptResult result);
    long getCorrectAttemptsCount(Long sessionId);
    long getIncorrectAttemptsCount(Long sessionId);
    double getSuccessRate(Long sessionId);
    double getSuccessRateByUser(UUID userId);
    
    // Анализ попыток
    List<CodeAttempt> getRecentAttempts(int limit);
    List<CodeAttempt> getAttemptsByDateRange(Instant start, Instant end);
    List<CodeAttempt> getAttemptsBySessionAndDateRange(Long sessionId, Instant start, Instant end);
    List<CodeAttempt> getFailedAttempts(Long sessionId, int limit);
    List<CodeAttempt> getSuccessfulAttempts(Long sessionId, int limit);
    
    // Управление бонусами и штрафами
    CodeAttempt applyBonus(Long attemptId, Double bonusAmount, String reason);
    CodeAttempt applyPenalty(Long attemptId, Double penaltyAmount, String reason);
    Double calculateBonusMultiplier(String sector, String codeType);
    Double calculatePenaltyMultiplier(int consecutiveFailures);
    
    // Валидация и бизнес-логика
    boolean canSubmitAttempt(Long sessionId, UUID userId);
    boolean isAttemptLimitReached(Long sessionId, UUID userId);
    int getRemainingAttempts(Long sessionId, UUID userId);
    boolean isCooldownActive(Long sessionId, UUID userId);
    long getCooldownRemainingSeconds(Long sessionId, UUID userId);
    
    // Анализ паттернов
    List<String> getMostCommonIncorrectCodes(Long sessionId);
    List<String> getMostCommonSectors(Long sessionId);
    double getAverageAttemptsPerCode(Long sessionId);
    List<CodeAttempt> getConsecutiveFailures(Long sessionId, UUID userId);
    
    // Операции с сессиями
    List<CodeAttempt> getSessionAttemptsSummary(Long sessionId);
    CodeAttempt getFirstCorrectAttempt(Long sessionId, Long levelId);
    CodeAttempt getLastAttempt(Long sessionId, UUID userId);
    List<CodeAttempt> getAttemptsSinceLastCorrect(Long sessionId, UUID userId);
    
    // Управление временем
    CodeAttempt updateAttemptTimestamp(Long attemptId);
    Instant getAverageTimeBetweenAttempts(Long sessionId, UUID userId);
    List<CodeAttempt> getAttemptsInTimeWindow(Long sessionId, Instant start, Instant end);
    
    // Операции для администрирования
    List<CodeAttempt> getAllAttemptsForAdmin();
    void deleteAttemptsBySession(Long sessionId);
    void deleteAttemptsOlderThan(Instant cutoffDate);
    List<CodeAttempt> getSuspiciousAttempts(int limit);
    
    // Операции с кэшированием
    void cacheAttempt(CodeAttempt attempt);
    void evictAttemptFromCache(Long attemptId);
    Optional<CodeAttempt> getCachedAttempt(Long attemptId);
    void cacheSessionAttempts(Long sessionId, List<CodeAttempt> attempts);
    
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
    List<Object[]> getAttemptsStatisticsByHour(Long sessionId, Instant start, Instant end);
    List<Object[]> getAttemptsStatisticsByDay(Long sessionId, Instant start, Instant end);
    List<Object[]> getSectorSuccessRate(Long sessionId);
    List<Object[]> getUserAttemptSummary(Long sessionId);
    
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