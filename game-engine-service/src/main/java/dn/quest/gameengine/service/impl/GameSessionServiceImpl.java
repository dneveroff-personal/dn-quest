package dn.quest.gameengine.service.impl;

import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.User;
import dn.quest.gameengine.entity.Quest;
import dn.quest.gameengine.entity.Team;
import dn.quest.shared.enums.SessionStatus;
import dn.quest.gameengine.repository.GameSessionRepository;
import dn.quest.gameengine.repository.UserRepository;
import dn.quest.gameengine.repository.QuestRepository;
import dn.quest.gameengine.repository.TeamRepository;
import dn.quest.gameengine.service.GameSessionService;
import dn.quest.gameengine.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса для управления игровыми сессиями
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GameSessionServiceImpl implements GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;
    private final QuestRepository questRepository;
    private final TeamRepository teamRepository;
    private final LeaderboardService leaderboardService;

    @Override
    public GameSession createSession(GameSession session) {
        log.info("Creating new game session: {}", session.getName());
        
        // Валидация данных
        validateSessionData(session);
        
        // Установка начальных значений
        session.setStatus(SessionStatus.CREATED);
        session.setCreatedAt(Instant.now());
        session.setLastActivityAt(Instant.now());
        
        GameSession savedSession = gameSessionRepository.save(session);
        log.info("Game session created successfully with ID: {}", savedSession.getId());
        
        // Публикация события
        publishSessionCreatedEvent(savedSession);
        
        return savedSession;
    }

    @Override
    @Cacheable(value = "gameSessions", key = "#id")
    @Transactional(readOnly = true)
    public Optional<GameSession> getSessionById(UUID id) {
        log.debug("Fetching game session by ID: {}", id);
        return gameSessionRepository.findById(id);
    }

    @Override
    @CachePut(value = "gameSessions", key = "#session.id")
    public GameSession updateSession(GameSession session) {
        log.info("Updating game session: {}", session.getId());
        
        validateSessionData(session);
        session.setLastActivityAt(Instant.now());
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("Game session updated successfully: {}", updatedSession.getId());
        
        // Публикация события
        publishSessionUpdatedEvent(updatedSession);
        
        return updatedSession;
    }

    @Override
    @CacheEvict(value = "gameSessions", key = "#id")
    public void deleteSession(UUID id) {
        log.info("Deleting game session: {}", id);
        
        GameSession session = gameSessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + id));
        
        gameSessionRepository.delete(session);
        log.info("Game session deleted successfully: {}", id);
        
        // Публикация события
        publishSessionDeletedEvent(session);
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession startSession(UUID sessionId, User startedBy) {
        log.info("Starting game session: {} by user: {}", sessionId, startedBy.getId());
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        // Проверка прав на запуск
        if (!canStartSession(sessionId, startedBy)) {
            throw new RuntimeException("User cannot start this session");
        }
        
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(Instant.now());
        session.setLastActivityAt(Instant.now());
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("Game session started successfully: {}", sessionId);
        
        // Обновление лидерборда
        leaderboardService.updateSessionLeaderboard(sessionId);
        
        // Публикация события
        handleSessionStartedEvent(updatedSession);
        
        return updatedSession;
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession pauseSession(UUID sessionId, User pausedBy) {
        log.info("Pausing game session: {} by user: {}", sessionId, pausedBy.getId());
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        if (!canPauseSession(sessionId, pausedBy)) {
            throw new RuntimeException("User cannot pause this session");
        }
        
        session.setStatus(SessionStatus.PAUSED);
        session.setLastActivityAt(Instant.now());
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("Game session paused successfully: {}", sessionId);
        
        // Публикация события
        handleSessionPausedEvent(updatedSession);
        
        return updatedSession;
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession resumeSession(UUID sessionId, User resumedBy) {
        log.info("Resuming game session: {} by user: {}", sessionId, resumedBy.getId());
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        if (!canResumeSession(sessionId, resumedBy)) {
            throw new RuntimeException("User cannot resume this session");
        }
        
        session.setStatus(SessionStatus.ACTIVE);
        session.setLastActivityAt(Instant.now());
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("Game session resumed successfully: {}", sessionId);
        
        // Публикация события
        handleSessionResumedEvent(updatedSession);
        
        return updatedSession;
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession finishSession(UUID sessionId, User finishedBy) {
        log.info("Finishing game session: {} by user: {}", sessionId, finishedBy.getId());
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        if (!canFinishSession(sessionId, finishedBy)) {
            throw new RuntimeException("User cannot finish this session");
        }
        
        session.setStatus(SessionStatus.COMPLETED);
        session.setFinishedAt(Instant.now());
        session.setLastActivityAt(Instant.now());
        
        // Расчет продолжительности
        if (session.getStartedAt() != null) {
            session.setDurationSeconds(session.getFinishedAt().getEpochSecond() - session.getStartedAt().getEpochSecond());
        }
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("Game session finished successfully: {}", sessionId);
        
        // Финальное обновление лидерборда
        leaderboardService.updateSessionLeaderboard(sessionId);
        
        // Публикация события
        handleSessionFinishedEvent(updatedSession);
        
        return updatedSession;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameSession> getAllSessions(Pageable pageable) {
        log.debug("Fetching all game sessions with pagination");
        return gameSessionRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameSession> getSessionsByStatus(SessionStatus status, Pageable pageable) {
        log.debug("Fetching game sessions by status: {}", status);
        return gameSessionRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> getSessionsByUser(User user) {
        log.debug("Fetching game sessions for user: {}", user.getId());
        return gameSessionRepository.findByParticipantsContaining(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> getActiveSessionsByUser(User user) {
        log.debug("Fetching active game sessions for user: {}", user.getId());
        return gameSessionRepository.findActiveByParticipant(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> getSessionsByQuest(UUID questId) {
        log.debug("Fetching game sessions for quest: {}", questId);
        return gameSessionRepository.findByQuestId(questId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> getActiveSessions() {
        log.debug("Fetching all active game sessions");
        return gameSessionRepository.findActiveSessions();
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession joinSession(UUID sessionId, User user) {
        log.info("User {} joining game session: {}", user.getId(), sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        if (!canJoinSession(sessionId, user)) {
            throw new RuntimeException("User cannot join this session");
        }
        
        // Добавление пользователя в участники
        if (!session.getParticipants().contains(user)) {
            session.getParticipants().add(user);
            session.setLastActivityAt(Instant.now());
        }
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("User {} joined game session successfully: {}", user.getId(), sessionId);
        
        // Публикация события
        handleSessionJoinedEvent(updatedSession, user);
        
        return updatedSession;
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession leaveSession(UUID sessionId, User user) {
        log.info("User {} leaving game session: {}", user.getId(), sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        if (!canLeaveSession(sessionId, user)) {
            throw new RuntimeException("User cannot leave this session");
        }
        
        // Удаление пользователя из участников
        session.getParticipants().remove(user);
        session.setLastActivityAt(Instant.now());
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("User {} left game session successfully: {}", user.getId(), sessionId);
        
        // Публикация события
        handleSessionLeftEvent(updatedSession, user);
        
        return updatedSession;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserInSession(UUID sessionId, User user) {
        log.debug("Checking if user {} is in session {}", user.getId(), sessionId);
        return gameSessionRepository.existsByIdAndParticipantsContaining(sessionId, user);
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession moveToNextLevel(UUID sessionId) {
        log.info("Moving to next level in session: {}", sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        // Логика перехода на следующий уровень
        UUID currentLevelId = session.getCurrentLevelId();
        if (currentLevelId != null) {
            // Получение следующего уровня из квеста
            Quest quest = session.getQuest();
            if (quest != null) {
                List<UUID> levelIds = quest.getLevelIds();
                int currentIndex = levelIds.indexOf(currentLevelId);
                if (currentIndex >= 0 && currentIndex < levelIds.size() - 1) {
                    session.setCurrentLevelId(levelIds.get(currentIndex + 1));
                    session.setLastActivityAt(Instant.now());
                }
            }
        }
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("Moved to next level in session: {}", sessionId);
        
        return updatedSession;
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession setCurrentLevel(UUID sessionId, UUID levelId) {
        log.info("Setting current level {} for session: {}", levelId, sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        session.setCurrentLevelId(levelId);
        session.setLastActivityAt(Instant.now());
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("Current level set successfully for session: {}", sessionId);
        
        return updatedSession;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> getCurrentLevelId(UUID sessionId) {
        log.debug("Getting current level ID for session: {}", sessionId);
        return gameSessionRepository.findById(sessionId)
            .map(GameSession::getCurrentLevelId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalSessionsCount() {
        log.debug("Getting total sessions count");
        return gameSessionRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveSessionsCount() {
        log.debug("Getting active sessions count");
        return gameSessionRepository.countByStatus(SessionStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public long getSessionsCountByStatus(SessionStatus status) {
        log.debug("Getting sessions count by status: {}", status);
        return gameSessionRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> getRecentSessions(int limit) {
        log.debug("Getting recent sessions with limit: {}", limit);
        return gameSessionRepository.findRecentSessions(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> getSessionsByDateRange(Instant start, Instant end) {
        log.debug("Getting sessions by date range: {} to {}", start, end);
        return gameSessionRepository.findByCreatedAtBetween(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canStartSession(UUID sessionId, User user) {
        log.debug("Checking if user {} can start session {}", user.getId(), sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return false;
        }
        
        // Проверка статуса сессии
        if (session.getStatus() != SessionStatus.CREATED) {
            return false;
        }
        
        // Проверка прав пользователя
        return session.getOwner().equals(user) || 
               session.getParticipants().contains(user) ||
               hasAdminRole(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canPauseSession(UUID sessionId, User user) {
        log.debug("Checking if user {} can pause session {}", user.getId(), sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return false;
        }
        
        // Проверка статуса сессии
        if (session.getStatus() != SessionStatus.ACTIVE) {
            return false;
        }
        
        // Проверка прав пользователя
        return session.getOwner().equals(user) || 
               session.getParticipants().contains(user) ||
               hasAdminRole(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canResumeSession(UUID sessionId, User user) {
        log.debug("Checking if user {} can resume session {}", user.getId(), sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return false;
        }
        
        // Проверка статуса сессии
        if (session.getStatus() != SessionStatus.PAUSED) {
            return false;
        }
        
        // Проверка прав пользователя
        return session.getOwner().equals(user) || 
               session.getParticipants().contains(user) ||
               hasAdminRole(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canFinishSession(UUID sessionId, User user) {
        log.debug("Checking if user {} can finish session {}", user.getId(), sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return false;
        }
        
        // Проверка статуса сессии
        if (session.getStatus() != SessionStatus.ACTIVE && session.getStatus() != SessionStatus.PAUSED) {
            return false;
        }
        
        // Проверка прав пользователя
        return session.getOwner().equals(user) || 
               session.getParticipants().contains(user) ||
               hasAdminRole(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canJoinSession(UUID sessionId, User user) {
        log.debug("Checking if user {} can join session {}", user.getId(), sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return false;
        }
        
        // Проверка статуса сессии
        if (session.getStatus() == SessionStatus.COMPLETED) {
            return false;
        }
        
        // Проверка лимита участников
        if (session.getMaxParticipants() != null && 
            session.getParticipants().size() >= session.getMaxParticipants()) {
            return false;
        }
        
        // Проверка, что пользователь уже не участвует
        return !session.getParticipants().contains(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canLeaveSession(UUID sessionId, User user) {
        log.debug("Checking if user {} can leave session {}", user.getId(), sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return false;
        }
        
        // Пользователь может покинуть сессию, если он участвует в ней
        return session.getParticipants().contains(user);
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession updateLastActivity(UUID sessionId) {
        log.debug("Updating last activity for session: {}", sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        session.setLastActivityAt(Instant.now());
        return gameSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Instant getSessionDuration(UUID sessionId) {
        log.debug("Getting session duration for: {}", sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return null;
        }
        
        if (session.getStartedAt() != null) {
            if (session.getFinishedAt() != null) {
                return session.getFinishedAt();
            } else {
                return Instant.now();
            }
        }
        
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSessionExpired(UUID sessionId) {
        log.debug("Checking if session {} is expired", sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return true;
        }
        
        // Сессия считается истекшей, если нет активности более 24 часов
        Instant threshold = Instant.now().minusSeconds(24 * 60 * 60);
        return session.getLastActivityAt().isBefore(threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> getSessionsByTeam(UUID teamId) {
        log.debug("Getting sessions for team: {}", teamId);
        return gameSessionRepository.findByTeamId(teamId);
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession assignTeamToSession(UUID sessionId, UUID teamId) {
        log.info("Assigning team {} to session: {}", teamId, sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));
        
        session.setTeam(team);
        session.setLastActivityAt(Instant.now());
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("Team assigned to session successfully: {}", sessionId);
        
        return updatedSession;
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession removeTeamFromSession(UUID sessionId) {
        log.info("Removing team from session: {}", sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        session.setTeam(null);
        session.setLastActivityAt(Instant.now());
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("Team removed from session successfully: {}", sessionId);
        
        return updatedSession;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> getSessionsByQuestType(String questType) {
        log.debug("Getting sessions by quest type: {}", questType);
        return gameSessionRepository.findByQuestType(questType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> getSessionsByDifficulty(String difficulty) {
        log.debug("Getting sessions by difficulty: {}", difficulty);
        return gameSessionRepository.findByDifficulty(difficulty);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameSession> searchSessions(String keyword, Pageable pageable) {
        log.debug("Searching sessions with keyword: {}", keyword);
        return gameSessionRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameSession> getSessionsWithFilters(
        SessionStatus status,
        UUID questId,
        UUID userId,
        UUID teamId,
        Instant startDate,
        Instant endDate,
        Pageable pageable
    ) {
        log.debug("Getting sessions with filters");
        return gameSessionRepository.findSessionsWithFilters(
            status, questId, userId, teamId, startDate, endDate, pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> getAllSessionsForAdmin() {
        log.debug("Getting all sessions for admin");
        return gameSessionRepository.findAll();
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession forceFinishSession(UUID sessionId, String reason) {
        log.info("Force finishing session {} with reason: {}", sessionId, reason);
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        session.setStatus(SessionStatus.COMPLETED);
        session.setFinishedAt(Instant.now());
        session.setLastActivityAt(Instant.now());
        
        if (session.getStartedAt() != null) {
            session.setDurationSeconds(session.getFinishedAt().getEpochSecond() - session.getStartedAt().getEpochSecond());
        }
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("Session force finished successfully: {}", sessionId);
        
        // Публикация события
        publishSessionForceFinishedEvent(updatedSession, reason);
        
        return updatedSession;
    }

    @Override
    @CachePut(value = "gameSessions", key = "#sessionId")
    public GameSession archiveSession(UUID sessionId) {
        log.info("Archiving session: {}", sessionId);
        
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Game session not found: " + sessionId));
        
        session.setStatus(SessionStatus.ARCHIVED);
        session.setLastActivityAt(Instant.now());
        
        GameSession updatedSession = gameSessionRepository.save(session);
        log.info("Session archived successfully: {}", sessionId);
        
        // Публикация события
        publishSessionArchivedEvent(updatedSession);
        
        return updatedSession;
    }

    @Override
    @CachePut(value = "gameSessions", key = "#session.id")
    public void cacheSession(GameSession session) {
        log.debug("Caching session: {}", session.getId());
        // Кэширование происходит автоматически через аннотации
    }

    @Override
    @CacheEvict(value = "gameSessions", key = "#sessionId")
    public void evictSessionFromCache(UUID sessionId) {
        log.debug("Evicting session from cache: {}", sessionId);
        // Кэширование происходит автоматически через аннотации
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameSession> getCachedSession(UUID sessionId) {
        log.debug("Getting cached session: {}", sessionId);
        return getSessionById(sessionId);
    }

    // Приватные вспомогательные методы

    private void validateSessionData(GameSession session) {
        if (session.getName() == null || session.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Session name cannot be empty");
        }
        
        if (session.getOwner() == null) {
            throw new IllegalArgumentException("Session owner cannot be null");
        }
        
        if (session.getQuest() == null) {
            throw new IllegalArgumentException("Session quest cannot be null");
        }
    }

    private boolean hasAdminRole(User user) {
        // Проверка административных прав пользователя
        return user.getRoles().stream()
            .anyMatch(role -> role.name().equals("ADMIN") || role.name().equals("MODERATOR"));
    }

    // Методы публикации событий (будут реализованы с Kafka)

    private void publishSessionCreatedEvent(GameSession session) {
        log.debug("Publishing session created event for session: {}", session.getId());
        // TODO: Реализация с Kafka
    }

    private void publishSessionUpdatedEvent(GameSession session) {
        log.debug("Publishing session updated event for session: {}", session.getId());
        // TODO: Реализация с Kafka
    }

    private void publishSessionDeletedEvent(GameSession session) {
        log.debug("Publishing session deleted event for session: {}", session.getId());
        // TODO: Реализация с Kafka
    }

    private void handleSessionStartedEvent(GameSession session) {
        log.debug("Publishing session started event for session: {}", session.getId());
        // TODO: Реализация с Kafka
    }

    private void handleSessionPausedEvent(GameSession session) {
        log.debug("Publishing session paused event for session: {}", session.getId());
        // TODO: Реализация с Kafka
    }

    private void handleSessionResumedEvent(GameSession session) {
        log.debug("Publishing session resumed event for session: {}", session.getId());
        // TODO: Реализация с Kafka
    }

    private void handleSessionFinishedEvent(GameSession session) {
        log.debug("Publishing session finished event for session: {}", session.getId());
        // TODO: Реализация с Kafka
    }

    private void handleSessionJoinedEvent(GameSession session, User user) {
        log.debug("Publishing session joined event for session: {}, user: {}", session.getId(), user.getId());
        // TODO: Реализация с Kafka
    }

    private void handleSessionLeftEvent(GameSession session, User user) {
        log.debug("Publishing session left event for session: {}, user: {}", session.getId(), user.getId());
        // TODO: Реализация с Kafka
    }

    private void publishSessionForceFinishedEvent(GameSession session, String reason) {
        log.debug("Publishing session force finished event for session: {}, reason: {}", session.getId(), reason);
        // TODO: Реализация с Kafka
    }

    private void publishSessionArchivedEvent(GameSession session) {
        log.debug("Publishing session archived event for session: {}", session.getId());
        // TODO: Реализация с Kafka
    }

    // Публичные методы для реализации интерфейса
    @Override
    public void publishSessionStartedEvent(GameSession session) {
        handleSessionStartedEvent(session);
    }

    @Override
    public void publishSessionPausedEvent(GameSession session) {
        handleSessionPausedEvent(session);
    }

    @Override
    public void publishSessionResumedEvent(GameSession session) {
        handleSessionResumedEvent(session);
    }

    @Override
    public void publishSessionFinishedEvent(GameSession session) {
        handleSessionFinishedEvent(session);
    }

    @Override
    public void publishSessionJoinedEvent(GameSession session, User user) {
        handleSessionJoinedEvent(session, user);
    }

    @Override
    public void publishSessionLeftEvent(GameSession session, User user) {
        handleSessionLeftEvent(session, user);
    }
}