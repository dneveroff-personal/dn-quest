package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.ParticipationRequest;
import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.User;
import dn.quest.shared.enums.ParticipationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления запросами на участие в игровых сессиях
 */
public interface ParticipationRequestService {

    // Базовые операции CRUD
    ParticipationRequest createRequest(ParticipationRequest request);
    Optional<ParticipationRequest> getRequestById(Long id);
    ParticipationRequest updateRequest(ParticipationRequest request);
    void deleteRequest(Long id);
    
    // Управление запросами
    ParticipationRequest submitRequest(Long sessionId, Long userId, String message);
    ParticipationRequest approveRequest(Long requestId, Long approvedBy);
    ParticipationRequest rejectRequest(Long requestId, Long rejectedBy, String reason);
    ParticipationRequest cancelRequest(Long requestId, Long cancelledBy);
    ParticipationRequest withdrawRequest(Long requestId, Long withdrawnBy);
    
    // Поиск и фильтрация запросов
    Page<ParticipationRequest> getAllRequests(Pageable pageable);
    List<ParticipationRequest> getRequestsBySession(Long sessionId);
    List<ParticipationRequest> getRequestsByUser(Long userId);
    List<ParticipationRequest> getRequestsByStatus(ParticipationStatus status);
    List<ParticipationRequest> getRequestsBySessionAndStatus(Long sessionId, ParticipationStatus status);
    List<ParticipationRequest> getRequestsByUserAndStatus(Long userId, ParticipationStatus status);
    
    // Статус запросов
    List<ParticipationRequest> getPendingRequests();
    List<ParticipationRequest> getApprovedRequests();
    List<ParticipationRequest> getRejectedRequests();
    List<ParticipationRequest> getCancelledRequests();
    List<ParticipationRequest> getWithdrawnRequests();
    
    // Статистика запросов
    long getTotalRequestsCount();
    long getRequestsCountBySession(Long sessionId);
    long getRequestsCountByUser(Long userId);
    long getRequestsCountByStatus(ParticipationStatus status);
    long getPendingRequestsCount();
    long getApprovedRequestsCount();
    long getRejectedRequestsCount();
    double getApprovalRate(Long sessionId);
    double getApprovalRateByUser(Long userId);
    
    // Анализ запросов
    List<ParticipationRequest> getRecentRequests(int limit);
    List<ParticipationRequest> getRequestsByDateRange(Instant start, Instant end);
    List<ParticipationRequest> getOldestPendingRequests(int limit);
    List<ParticipationRequest> getMostActiveRequesters(int limit);
    List<ParticipationRequest> getMostRequestedSessions(int limit);
    
    // Управление временем
    Instant getRequestSubmissionTime(Long requestId);
    Instant getRequestProcessingTime(Long requestId);
    Long getRequestProcessingDuration(Long requestId);
    Double getAverageProcessingTime(Long sessionId);
    Double getAverageProcessingTimeByStatus(ParticipationStatus status);
    
    // Валидация и бизнес-логика
    boolean canSubmitRequest(Long sessionId, Long userId);
    boolean canApproveRequest(Long requestId, Long userId);
    boolean canRejectRequest(Long requestId, Long userId);
    boolean canCancelRequest(Long requestId, Long userId);
    boolean canWithdrawRequest(Long requestId, Long userId);
    boolean isRequestPending(Long requestId);
    boolean isRequestProcessed(Long requestId);
    boolean hasActiveRequest(Long sessionId, Long userId);
    boolean hasPendingRequest(Long sessionId, Long userId);
    
    // Управление лимитами
    boolean isRequestLimitReached(Long sessionId, Long userId);
    int getRemainingRequests(Long sessionId, Long userId);
    boolean isSessionFull(Long sessionId);
    boolean isUserBanned(Long sessionId, Long userId);
    boolean isUserEligible(Long sessionId, Long userId);
    
    // Операции с сессиями
    List<ParticipationRequest> getSessionRequestsSummary(Long sessionId);
    ParticipationRequest getLatestRequestByUser(Long sessionId, Long userId);
    List<ParticipationRequest> getPendingRequestsForSession(Long sessionId);
    List<ParticipationRequest> getApprovedRequestsForSession(Long sessionId);
    int getApprovedParticipantsCount(Long sessionId);
    int getAvailableSlots(Long sessionId);
    
    // Командные операции
    List<ParticipationRequest> getTeamRequests(Long sessionId, Long teamId);
    ParticipationRequest submitTeamRequest(Long sessionId, Long teamId, Long submittedBy, String message);
    ParticipationRequest approveTeamRequest(Long requestId, Long approvedBy);
    List<ParticipationRequest> getPendingTeamRequests(Long sessionId);
    
    // Операции для администрирования
    List<ParticipationRequest> getAllRequestsForAdmin();
    void approveAllPendingRequests(Long sessionId, Long approvedBy);
    void rejectAllPendingRequests(Long sessionId, Long rejectedBy, String reason);
    void deleteRequestsOlderThan(Instant cutoffDate);
    void deleteRequestsBySession(Long sessionId);
    List<ParticipationRequest> getSuspiciousRequests(int limit);
    
    // Операции с кэшированием
    void cacheRequest(ParticipationRequest request);
    void evictRequestFromCache(Long requestId);
    Optional<ParticipationRequest> getCachedRequest(Long requestId);
    void cacheSessionRequests(Long sessionId, List<ParticipationRequest> requests);
    void evictSessionRequestsFromCache(Long sessionId);
    
    // Операции с событиями
    void publishRequestSubmittedEvent(ParticipationRequest request);
    void publishRequestApprovedEvent(ParticipationRequest request);
    void publishRequestRejectedEvent(ParticipationRequest request);
    void publishRequestCancelledEvent(ParticipationRequest request);
    void publishRequestWithdrawnEvent(ParticipationRequest request);
    
    // Интеграция с другими сервисами
    void notifySessionOwner(ParticipationRequest request);
    void notifyRequester(ParticipationRequest request);
    void notifyTeamMembers(ParticipationRequest request);
    void updateStatistics(ParticipationRequest request);
    void updateSessionCapacity(ParticipationRequest request);
    
    // Аналитика и отчеты
    List<Object[]> getRequestStatisticsByHour(Long sessionId, Instant start, Instant end);
    List<Object[]> getRequestStatisticsByDay(Long sessionId, Instant start, Instant end);
    List<Object[]> getRequestStatusAnalysis(Long sessionId);
    List<Object[]> getUserRequestSummary(Long userId);
    List<Object[]> getSessionRequestAnalysis(Long sessionId);
    List<Object[]> getProcessingTimeAnalysis(Long sessionId);
    
    // Операции для оптимизации
    void batchCreateRequests(List<ParticipationRequest> requests);
    void batchUpdateRequests(List<ParticipationRequest> requests);
    List<ParticipationRequest> getRequestsForOptimization(int batchSize);
    
    // Вспомогательные методы
    String generateRequestId();
    void logRequest(ParticipationRequest request);
    boolean isValidRequestMessage(String message);
    String sanitizeRequestMessage(String message);
    
    // Операции с приоритетами
    ParticipationRequest setRequestPriority(Long requestId, Integer priority);
    Integer getRequestPriority(Long requestId);
    List<ParticipationRequest> getRequestsByPriority(Long sessionId);
    List<ParticipationRequest> getHighPriorityRequests();
    
    // Операции с очередями
    List<ParticipationRequest> getRequestQueue(Long sessionId);
    int getRequestQueuePosition(Long requestId);
    ParticipationRequest moveToQueueFront(Long requestId);
    ParticipationRequest moveToQueueBack(Long requestId);
    
    // Операции с автоматической обработкой
    ParticipationRequest autoApproveRequest(Long requestId);
    ParticipationRequest autoRejectRequest(Long requestId, String reason);
    void enableAutoApproval(Long sessionId);
    void disableAutoApproval(Long sessionId);
    boolean isAutoApprovalEnabled(Long sessionId);
    
    // Операции с условиями
    boolean meetsParticipationConditions(Long sessionId, Long userId);
    List<String> getParticipationConditions(Long sessionId);
    boolean hasRequiredPrerequisites(Long sessionId, Long userId);
    List<String> getMissingPrerequisites(Long sessionId, Long userId);
    
    // Операции с историей
    List<String> getRequestHistory(Long requestId);
    void addRequestNote(Long requestId, String note, Long authorId);
    List<String> getRequestNotes(Long requestId);
    
    // Операции с пакетной обработкой
    void batchApproveRequests(List<Long> requestIds, Long approvedBy);
    void batchRejectRequests(List<Long> requestIds, Long rejectedBy, String reason);
    void batchCancelRequests(List<Long> requestIds, Long cancelledBy);
    
    // Операции с фильтрацией
    Page<ParticipationRequest> getRequestsWithFilters(
        Long sessionId,
        Long userId,
        ParticipationStatus status,
        Instant startDate,
        Instant endDate,
        String keyword,
        Pageable pageable
    );
    
    // Операции с экспортом
    List<ParticipationRequest> exportRequests(Long sessionId);
    List<ParticipationRequest> exportUserRequests(Long userId);
    String generateRequestReport(Long sessionId);
    String generateUserRequestReport(Long userId);
}