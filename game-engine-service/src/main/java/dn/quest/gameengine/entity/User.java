package dn.quest.gameengine.entity;

import dn.quest.shared.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Пользователь системы
 */
@Data
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_user_role", columnList = "role"),
                @Index(name = "idx_user_created_at", columnList = "created_at"),
                @Index(name = "idx_user_public_name", columnList = "public_name")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(exclude = {"authoredQuests", "captainedTeams", "teamMemberships", "sessions", "codeAttempts", "levelCompletions"})
@ToString(exclude = {"authoredQuests", "captainedTeams", "teamMemberships", "sessions", "codeAttempts", "levelCompletions"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 255)
    private String email;

    @Column(length = 128)
    private String publicName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserRole role = UserRole.PLAYER;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "total_games_played")
    private Integer totalGamesPlayed = 0;

    @Column(name = "total_games_won")
    private Integer totalGamesWon = 0;

    @Column(name = "total_playtime_seconds")
    private Long totalPlaytimeSeconds = 0L;

    @ManyToMany(mappedBy = "authors")
    private Set<Quest> authoredQuests = new HashSet<>();

    @OneToMany(mappedBy = "captain")
    private Set<Team> captainedTeams = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<TeamMember> teamMemberships = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<GameSession> sessions = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<CodeAttempt> codeAttempts = new HashSet<>();

    @OneToMany(mappedBy = "passedByUser")
    private Set<LevelCompletion> levelCompletions = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Проверяет, активен ли пользователь
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Проверяет, верифицирован ли пользователь
     */
    public boolean isVerified() {
        return Boolean.TRUE.equals(isVerified);
    }

    /**
     * Проверяет, является ли пользователь администратором
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /**
     * Проверяет, является ли пользователем игроком
     */
    public boolean isPlayer() {
        return role == UserRole.PLAYER;
    }

    /**
     * Проверяет, является ли пользователь модератором
     */
    public boolean isModerator() {
        return role == UserRole.MODERATOR;
    }

    /**
     * Получает процент побед
     */
    public double getWinPercentage() {
        if (totalGamesPlayed == null || totalGamesPlayed == 0) {
            return 0.0;
        }
        int won = totalGamesWon != null ? totalGamesWon : 0;
        return (double) won / totalGamesPlayed * 100.0;
    }

    /**
     * Получает среднее время игры в минутах
     */
    public double getAveragePlaytimeMinutes() {
        if (totalGamesPlayed == null || totalGamesPlayed == 0 || totalPlaytimeSeconds == null) {
            return 0.0;
        }
        return (double) totalPlaytimeSeconds / totalGamesPlayed / 60.0;
    }

    /**
     * Получает отформатированное общее время игры
     */
    public String getFormattedTotalPlaytime() {
        if (totalPlaytimeSeconds == null || totalPlaytimeSeconds == 0) {
            return "0 мин";
        }
        
        long hours = totalPlaytimeSeconds / 3600;
        long minutes = (totalPlaytimeSeconds % 3600) / 60;
        
        if (hours > 0) {
            return hours + " ч " + minutes + " мин";
        } else {
            return minutes + " мин";
        }
    }

    /**
     * Получает уровень пользователя на основе рейтинга
     */
    public String getUserLevel() {
        if (rating == null) {
            return "Новичок";
        }
        
        if (rating >= 4.5) return "Легенда";
        if (rating >= 4.0) return "Мастер";
        if (rating >= 3.5) return "Эксперт";
        if (rating >= 3.0) return "Профи";
        if (rating >= 2.5) return "Опытный";
        if (rating >= 2.0) return "Уверенный";
        if (rating >= 1.5) return "Продвинутый";
        if (rating >= 1.0) return "Средний";
        return "Новичок";
    }

    /**
     * Обновляет статистику после завершения игры
     */
    public void updateGameStats(boolean won, long playtimeSeconds) {
        if (totalGamesPlayed == null) {
            totalGamesPlayed = 0;
        }
        if (totalGamesWon == null) {
            totalGamesWon = 0;
        }
        if (totalPlaytimeSeconds == null) {
            totalPlaytimeSeconds = 0L;
        }
        
        totalGamesPlayed++;
        if (won) {
            totalGamesWon++;
        }
        totalPlaytimeSeconds += playtimeSeconds;
    }

    /**
     * Получает отображаемое имя пользователя
     */
    public String getDisplayName() {
        return publicName != null && !publicName.trim().isEmpty() ? publicName : username;
    }

    /**
     * Обновляет время последнего входа
     */
    public void updateLastLogin() {
        lastLoginAt = Instant.now();
    }

    /**
     * Получает набор ролей пользователя (для совместимости с интерфейсами)
     */
    public Set<UserRole> getRoles() {
        Set<UserRole> roles = new HashSet<>();
        roles.add(role);
        return roles;
    }
}