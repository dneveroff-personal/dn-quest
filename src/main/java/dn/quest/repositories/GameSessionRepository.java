package dn.quest.repositories;

import dn.quest.model.entities.enums.SessionStatus;
import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

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

    Optional<GameSession> findByQuestAndTeamAndStatus(Quest quest, Team team, SessionStatus status);
    Optional<GameSession> findByQuestAndUserAndStatus(Quest quest, User user, SessionStatus status);
    
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
    @Query("SELECT gs FROM GameSession gs WHERE gs.user = :user OR gs.team IN (SELECT tm.team FROM TeamMember tm WHERE tm.user = :user)")
    List<GameSession> findByUserOrTeam(@Param("user") User user);
    
    // Активные сессии для квеста
    @Query("SELECT gs FROM GameSession gs WHERE gs.quest = :quest AND gs.status = 'ACTIVE'")
    List<GameSession> findActiveSessionsByQuest(@Param("quest") Quest quest);

}
