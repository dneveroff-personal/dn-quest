package dn.quest.gameengine.entity;

import dn.quest.shared.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Игровая сессия - основная сущность для управления игровым процессом
 */
@Data
@Entity
@Builder
@Table(name = "game_sessions",
        indexes = {
                @Index(name = "idx_session_quest", columnList = "quest_id"),
                @Index(name = "idx_session_status", columnList = "status"),
                @Index(name = "idx_session_user", columnList = "user_id"),
                @Index(name = "idx_session_team", columnList = "team_id"),
                @Index(name = "idx_session_started_at", columnList = "started_at"),
                @Index(name = "idx_session_finished_at", columnList = "finished_at")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(exclude = {"quest", "user", "team", "currentLevel", "levelProgresses", "codeAttempts", "levelCompletions"})
@ToString(exclude = {"quest", "user", "team", "levelProgresses", "codeAttempts", "levelCompletions"})
@AllArgsConstructor
@NoArgsConstructor
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Название сессии
     */
    @Column(name = "name", length = 200)
    private String name;

    /**
     * Описание сессии
     */
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Владелец сессии
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private SessionStatus status = SessionStatus.PENDING;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(nullable = false)
    @Builder.Default
    private int bonusTimeSumSec = 0;

    @Column(nullable = false)
    @Builder.Default
    private int penaltyTimeSumSec = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_level_id")
    private Level currentLevel;

    @Column(name = "current_level_id", insertable = false, updatable = false)
    private UUID currentLevelId;

    /**
     * Максимальное количество участников
     */
    @Column(name = "max_participants")
    private Integer maxParticipants;

    /**
     * Участники сессии
     */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Builder.Default
    private Set<User> participants = new HashSet<>();

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<LevelProgress> levelProgresses = new HashSet<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CodeAttempt> codeAttempts = new HashSet<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<LevelCompletion> levelCompletions = new HashSet<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ParticipationRequest> participationRequests = new HashSet<>();

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (lastActivityAt == null) {
            lastActivityAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastActivityAt = Instant.now();
    }

    /**
     * Проверяет, активна ли сессия
     */
    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }

    /**
     * Проверяет, завершена ли сессия
     */
    public boolean isFinished() {
        return status == SessionStatus.FINISHED;
    }

    /**
     * Проверяет, приостановлена ли сессия
     */
    public boolean isPaused() {
        return status == SessionStatus.PAUSED;
    }

    /**
     * Получает общее время сессии в секундах
     */
    public long getTotalDurationSeconds() {
        if (startedAt == null) {
            return 0;
        }
        Instant endTime = finishedAt != null ? finishedAt : Instant.now();
        return endTime.getEpochSecond() - startedAt.getEpochSecond();
    }

    /**
     * Получает скорректированное время с учетом бонусов и штрафов
     */
    public long getAdjustedDurationSeconds() {
        return getTotalDurationSeconds() + bonusTimeSumSec - penaltyTimeSumSec;
    }

    /**
     * Проверяет, является ли это командной сессией
     */
    public boolean isTeamSession() {
        return team != null;
    }

    /**
     * Проверяет, является ли это сольной сессией
     */
    public boolean isSoloSession() {
        return user != null;
    }
}