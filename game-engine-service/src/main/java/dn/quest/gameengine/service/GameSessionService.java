package dn.quest.gameengine.service;

import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.User;
import dn.quest.shared.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления игровыми сессиями
 */
public interface GameSessionService {

    // Базовые операции CRUD
    GameSession createSession(GameSession session);
    Optional<GameSession> getSessionById(Long id);
    GameSession updateSession(GameSession session);
    void deleteSession(Long id);
    
    // Управление состоянием сессии
    GameSession startSession(Long sessionId, User startedBy);
    GameSession pauseSession(Long sessionId, User pausedBy);
    GameSession resumeSession(Long sessionId, User resumedBy);
    GameSession finishSession(Long sessionId, User finishedBy);
    
    // Поиск и фильтрация сессий
    Page<GameSession> getAllSessions(Pageable pageable);
    Page<GameSession> getSessionsByStatus(SessionStatus status, Pageable pageable);
    List<GameSession> getSessionsByUser(User user);
    List<GameSession> getActiveSessionsByUser(User user);
    List<GameSession> getSessionsByQuest(Long questId);
    List<GameSession> getActiveSessions();
    
    // Управление участниками
    GameSession joinSession(Long sessionId, User user);
    GameSession leaveSession(Long sessionId, User user);
    boolean isUserInSession(Long sessionId, User user);
    
    // Управление текущим уровнем
    GameSession moveToNextLevel(Long sessionId);
    GameSession setCurrentLevel(Long sessionId, Long levelId);
    Optional<Long> getCurrentLevelId(Long sessionId);
    
    // Статистика и аналитика
    long getTotalSessionsCount();
    long getActiveSessionsCount();
    long getSessionsCountByStatus(SessionStatus status);
    List<GameSession> getRecentSessions(int limit);
    List<GameSession> getSessionsByDateRange(Instant start, Instant end);
    
    // Валидация и бизнес-логика
    boolean canStartSession(Long sessionId, User user);
    boolean canPauseSession(Long sessionId, User user);
    boolean canResumeSession(Long sessionId, User user);
    boolean canFinishSession(Long sessionId, User user);
    boolean canJoinSession(Long sessionId, User user);
    boolean canLeaveSession(Long sessionId, User user);
    
    // Управление временем
    GameSession updateLastActivity(Long sessionId);
    Instant getSessionDuration(Long sessionId);
    boolean isSessionExpired(Long sessionId);
    
    // Командные операции
    List<GameSession> getSessionsByTeam(Long teamId);
    GameSession assignTeamToSession(Long sessionId, Long teamId);
    GameSession removeTeamFromSession(Long sessionId);
    
    // Операции с квестами
    List<GameSession> getSessionsByQuestType(String questType);
    List<GameSession> getSessionsByDifficulty(String difficulty);
    
    // Поиск и фильтрация
    Page<GameSession> searchSessions(String keyword, Pageable pageable);
    Page<GameSession> getSessionsWithFilters(
        SessionStatus status,
        Long questId,
        Long userId,
        Long teamId,
        Instant startDate,
        Instant endDate,
        Pageable pageable
    );
    
    // Операции для администрирования
    List<GameSession> getAllSessionsForAdmin();
    GameSession forceFinishSession(Long sessionId, String reason);
    GameSession archiveSession(Long sessionId);
    
    // Операции с кэшированием
    void cacheSession(GameSession session);
    void evictSessionFromCache(Long sessionId);
    Optional<GameSession> getCachedSession(Long sessionId);
    
    // Операции с событиями
    void publishSessionStartedEvent(GameSession session);
    void publishSessionPausedEvent(GameSession session);
    void publishSessionResumedEvent(GameSession session);
    void publishSessionFinishedEvent(GameSession session);
    void publishSessionJoinedEvent(GameSession session, User user);
    void publishSessionLeftEvent(GameSession session, User user);
}