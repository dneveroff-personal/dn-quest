package dn.quest.gameengine.repository;

import dn.quest.gameengine.entity.GameSession;
import dn.quest.gameengine.entity.Quest;
import dn.quest.gameengine.entity.Team;
import dn.quest.gameengine.entity.User;
import dn.quest.shared.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с игровыми сессиями
 */
@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {

    // Базовые запросы
    List<GameSession> findByQuest(Quest quest);
    List<GameSession> findByUser(User user);
    List<GameSession> findByTeam(Team team);

    // Оптимизированные запросы с JOIN FETCH
    @Query("SELECT gs FROM GameSession gs LEFT JOIN FETCH gs.quest LEFT JOIN FETCH gs.user LEFT JOIN FETCH gs.team WHERE gs.id = :id")
    Optional<GameSession> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT gs FROM GameSession gs LEFT JOIN FETCH gs.quest LEFT JOIN FETCH gs.user LEFT JOIN FETCH gs.team WHERE gs.quest = :quest")
    List<GameSession> findByQuestWithDetails(@Param("quest") Quest quest);
    
    @Query("SELECT gs FROM GameSession gs LEFT JOIN FETCH gs.quest LEFT JOIN FETCH gs.user LEFT JOIN FETCH gs.team WHERE gs.user = :user")
    List<GameSession> findByUserWithDetails(@Param("user") User user);
    
    @Query("SELECT gs FROM GameSession gs LEFT JOIN FETCH gs.quest LEFT JOIN FETCH gs.user LEFT JOIN FETCH gs.team WHERE gs.team = :team")
    List<GameSession> findByTeamWithDetails(@Param("team") Team team);

    // Запросы по статусу
    Optional<GameSession> findByQuestAndUser(Quest quest, User user);
    Optional<GameSession> findByQuestAndTeam(Quest quest, Team team);
    Optional<GameSession> findByQuestAndUserAndStatus(Quest quest, User user, SessionStatus status);
    Optional<GameSession> findByQuestAndTeamAndStatus(Quest quest, Team team, SessionStatus status);
    
    // Активные сессии
    @Query("SELECT gs FROM GameSession gs WHERE gs.status = :status ORDER BY gs.startedAt DESC")
    List<GameSession> findByStatus(@Param("status") SessionStatus status);
    
    @Query("SELECT gs FROM GameSession gs WHERE gs.status = :status ORDER BY gs.startedAt DESC")
    Page<GameSession> findByStatusPaged(@Param("status") SessionStatus status, Pageable pageable);
    
    // Сессии по времени
    @Query("SELECT gs FROM GameSession gs WHERE gs.startedAt >= :since ORDER BY gs.startedAt DESC")
    List<GameSession> findByStartedAfter(@Param("since") Instant since);
    
    @Query("SELECT gs FROM GameSession gs WHERE gs.finishedAt >= :since ORDER BY gs.finishedAt DESC")
    List<GameSession> findByFinishedAfter(@Param("since") Instant since);
    
    // Статистические запросы
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.quest = :quest AND gs.status = :status")
    long countByQuestAndStatus(@Param("quest") Quest quest, @Param("status") SessionStatus status);
    
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.quest = :quest")
    long countByQuest(@Param("quest") Quest quest);
    
    // Запросы для лидербордов
    @Query("SELECT gs FROM GameSession gs WHERE gs.quest = :quest AND gs.status = 'FINISHED' ORDER BY gs.finishedAt ASC")
    List<GameSession> findFinishedSessionsByQuestOrderByTime(@Param("quest") Quest quest);
    
    @Query("SELECT gs FROM GameSession gs WHERE gs.quest = :quest AND gs.status = 'FINISHED' ORDER BY gs.finishedAt ASC")
    Page<GameSession> findFinishedSessionsByQuestOrderByTimePaged(@Param("quest") Quest quest, Pageable pageable);
    
    // Поиск сессий пользователя
    @Query("SELECT gs FROM GameSession gs WHERE gs.user = :user OR gs.team IN (SELECT tm.team FROM TeamMember tm WHERE tm.user = :user AND tm.isActive = true)")
    List<GameSession> findByUserOrTeam(@Param("user") User user);
    
    // Активные сессии для квеста
    @Query("SELECT gs FROM GameSession gs WHERE gs.quest = :quest AND gs.status = 'ACTIVE'")
    List<GameSession> findActiveSessionsByQuest(@Param("quest") Quest quest);

    // Запросы для анализа активности
    @Query("SELECT gs FROM GameSession gs WHERE gs.lastActivityAt >= :since ORDER BY gs.lastActivityAt DESC")
    List<GameSession> findByLastActivityAfter(@Param("since") Instant since);
    
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.status = 'ACTIVE' AND gs.lastActivityAt < :threshold")
    long countInactiveSessions(@Param("threshold") Instant threshold);

    // Запросы для командных сессий
    @Query("SELECT gs FROM GameSession gs WHERE gs.team IS NOT NULL AND gs.status = :status")
    List<GameSession> findTeamSessionsByStatus(@Param("status") SessionStatus status);
    
    @Query("SELECT gs FROM GameSession gs WHERE gs.user IS NOT NULL AND gs.status = :status")
    List<GameSession> findSoloSessionsByStatus(@Param("status") SessionStatus status);

    // Запросы для статистики по пользователям
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.user = :user AND gs.status = 'FINISHED'")
    long countFinishedSessionsByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.team = :team AND gs.status = 'FINISHED'")
    long countFinishedSessionsByTeam(@Param("team") Team team);

    // Запросы для поиска по IP
    @Query("SELECT gs FROM GameSession gs WHERE gs.ipAddress = :ipAddress ORDER BY gs.startedAt DESC")
    List<GameSession> findByIpAddress(@Param("ipAddress") String ipAddress);
    
    @Query("SELECT COUNT(DISTINCT gs.user) FROM GameSession gs WHERE gs.ipAddress = :ipAddress AND gs.user IS NOT NULL")
    long countDistinctUsersByIpAddress(@Param("ipAddress") String ipAddress);

    // Запросы для анализа производительности
    @Query("SELECT gs FROM GameSession gs WHERE gs.startedAt BETWEEN :start AND :end ORDER BY gs.startedAt DESC")
    List<GameSession> findByStartedBetween(@Param("start") Instant start, @Param("end") Instant end);
    
    @Query("SELECT AVG(gs.bonusTimeSumSec) FROM GameSession gs WHERE gs.quest = :quest AND gs.status = 'FINISHED'")
    Double getAverageBonusTimeByQuest(@Param("quest") Quest quest);
    
    @Query("SELECT AVG(gs.penaltyTimeSumSec) FROM GameSession gs WHERE gs.quest = :quest AND gs.status = 'FINISHED'")
    Double getAveragePenaltyTimeByQuest(@Param("quest") Quest quest);

    // Запросы для мониторинга
    @Query("SELECT gs FROM GameSession gs WHERE gs.status IN ('ACTIVE', 'PAUSED') ORDER BY gs.lastActivityAt ASC")
    List<GameSession> findActiveSessionsOrderByLastActivity();
    
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.status = 'ACTIVE'")
    long countActiveSessions();
    
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.status = 'PAUSED'")
    long countPausedSessions();

    // Запросы для анализа по типам квестов
    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.quest.type = :questType AND gs.status = :status")
    long countByQuestTypeAndStatus(@Param("questType") String questType, @Param("status") SessionStatus status);

    // Дополнительные методы для сервиса
    Page<GameSession> findByStatus(@Param("status") SessionStatus status, Pageable pageable);

    @Query("SELECT gs FROM GameSession gs WHERE gs.user = :user OR EXISTS (SELECT 1 FROM TeamMember tm WHERE tm.team = gs.team AND tm.user = :user AND tm.isActive = true)")
    List<GameSession> findByParticipantsContaining(@Param("user") User user);

    @Query("SELECT gs FROM GameSession gs WHERE (gs.user = :user OR EXISTS (SELECT 1 FROM TeamMember tm WHERE tm.team = gs.team AND tm.user = :user AND tm.isActive = true)) AND gs.status IN ('ACTIVE', 'PAUSED', 'IN_PROGRESS')")
    List<GameSession> findActiveByParticipant(@Param("user") User user);

    List<GameSession> findByQuestId(UUID questId);

    @Query("SELECT gs FROM GameSession gs WHERE gs.status IN ('ACTIVE', 'PAUSED', 'IN_PROGRESS')")
    List<GameSession> findActiveSessions();

    long countByStatus(SessionStatus status);

    @Query("SELECT gs FROM GameSession gs ORDER BY gs.createdAt DESC")
    List<GameSession> findRecentSessions(@Param("limit") int limit);

    @Query("SELECT CASE WHEN COUNT(gs) > 0 THEN true ELSE false END FROM GameSession gs WHERE gs.id = :sessionId AND (gs.user = :user OR EXISTS (SELECT 1 FROM TeamMember tm WHERE tm.team = gs.team AND tm.user = :user AND tm.isActive = true))")
    boolean existsByIdAndParticipantsContaining(@Param("sessionId") UUID sessionId, @Param("user") User user);

    List<GameSession> findByCreatedAtBetween(Instant start, Instant end);

    // Дополнительные методы
    List<GameSession> findByTeamId(UUID teamId);

    @Query("SELECT gs FROM GameSession gs WHERE gs.quest.type = :questType")
    List<GameSession> findByQuestType(@Param("questType") String questType);

    @Query("SELECT gs FROM GameSession gs WHERE gs.quest.difficulty = :difficulty")
    List<GameSession> findByDifficulty(@Param("difficulty") String difficulty);

    // Методы для поиска
    @Query("SELECT gs FROM GameSession gs WHERE LOWER(gs.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<GameSession> findByNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT gs FROM GameSession gs WHERE " +
           "(:status IS NULL OR gs.status = :status) AND " +
           "(:questId IS NULL OR gs.quest.id = :questId) AND " +
           "(:userId IS NULL OR gs.user.id = :userId) AND " +
           "(:teamId IS NULL OR gs.team.id = :teamId) AND " +
           "(:startDate IS NULL OR gs.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR gs.createdAt <= :endDate)")
    Page<GameSession> findSessionsWithFilters(
        @Param("status") SessionStatus status,
        @Param("questId") UUID questId,
        @Param("userId") UUID userId,
        @Param("teamId") UUID teamId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        Pageable pageable
    );
}