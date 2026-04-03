package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.User;
import dn.quest.shared.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления игровыми сессиями
 */
public interface GameSessionService {

    // Базовые операции CRUD
    GameSession createSession(GameSession session);
    Optional<GameSession> getSessionById(UUID id);
    GameSession updateSession(GameSession session);
    void deleteSession(UUID id);
    
    // Управление состоянием сессии
    GameSession startSession(UUID sessionId, User startedBy);
    GameSession pauseSession(UUID sessionId, User pausedBy);
    GameSession resumeSession(UUID sessionId, User resumedBy);
    GameSession finishSession(UUID sessionId, User finishedBy);
    
    // Поиск и фильтрация сессий
    Page<GameSession> getAllSessions(Pageable pageable);
    Page<GameSession> getSessionsByStatus(SessionStatus status, Pageable pageable);
    List<GameSession> getSessionsByUser(User user);
    List<GameSession> getActiveSessionsByUser(User user);
    List<GameSession> getSessionsByQuest(UUID questId);
    List<GameSession> getActiveSessions();
    
    // Управление участниками
    GameSession joinSession(UUID sessionId, User user);
    GameSession leaveSession(UUID sessionId, User user);
    boolean isUserInSession(UUID sessionId, User user);
    
    // Управление текущим уровнем
    GameSession moveToNextLevel(UUID sessionId);
    GameSession setCurrentLevel(UUID sessionId, UUID levelId);
    Optional<UUID> getCurrentLevelId(UUID sessionId);
    
    // Статистика и аналитика
    long getTotalSessionsCount();
    long getActiveSessionsCount();
    long getSessionsCountByStatus(SessionStatus status);
    List<GameSession> getRecentSessions(int limit);
    List<GameSession> getSessionsByDateRange(Instant start, Instant end);
    
    // Валидация и бизнес-логика
    boolean canStartSession(UUID sessionId, User user);
    boolean canPauseSession(UUID sessionId, User user);
    boolean canResumeSession(UUID sessionId, User user);
    boolean canFinishSession(UUID sessionId, User user);
    boolean canJoinSession(UUID sessionId, User user);
    boolean canLeaveSession(UUID sessionId, User user);
    
    // Управление временем
    GameSession updateLastActivity(UUID sessionId);
    Instant getSessionDuration(UUID sessionId);
    boolean isSessionExpired(UUID sessionId);
    
    // Командные операции
    List<GameSession> getSessionsByTeam(UUID teamId);
    GameSession assignTeamToSession(UUID sessionId, UUID teamId);
    GameSession removeTeamFromSession(UUID sessionId);
    
    // Операции с квестами
    List<GameSession> getSessionsByQuestType(String questType);
    List<GameSession> getSessionsByDifficulty(String difficulty);
    
    // Поиск и фильтрация
    Page<GameSession> searchSessions(String keyword, Pageable pageable);
    Page<GameSession> getSessionsWithFilters(
        SessionStatus status,
        UUID questId,
        UUID userId,
        UUID teamId,
        Instant startDate,
        Instant endDate,
        Pageable pageable
    );
    
    // Операции для администрирования
    List<GameSession> getAllSessionsForAdmin();
    GameSession forceFinishSession(UUID sessionId, String reason);
    GameSession archiveSession(UUID sessionId);
    
    // Операции с кэшированием
    void cacheSession(GameSession session);
    void evictSessionFromCache(UUID sessionId);
    Optional<GameSession> getCachedSession(UUID sessionId);
    
    // Операции с событиями
    void publishSessionStartedEvent(GameSession session);
    void publishSessionPausedEvent(GameSession session);
    void publishSessionResumedEvent(GameSession session);
    void publishSessionFinishedEvent(GameSession session);
    void publishSessionJoinedEvent(GameSession session, User user);
    void publishSessionLeftEvent(GameSession session, User user);
}