package dn.quest.gameengine.entity;

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
 * Команда
 */
@Data
@Entity
@Table(name = "teams",
        uniqueConstraints = @UniqueConstraint(name = "uk_team_name", columnNames = "name"),
        indexes = {
                @Index(name = "idx_team_captain", columnList = "captain_id"),
                @Index(name = "idx_team_created_at", columnList = "created_at"),
                @Index(name = "idx_team_is_active", columnList = "is_active")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(exclude = {"captain", "members", "sessions"})
@ToString(exclude = {"captain", "members", "sessions"})
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captain_id", nullable = false)
    private User captain;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "max_members")
    private Integer maxMembers = 10;

    @Column(name = "rating", columnDefinition = "numeric")
    private Double rating = 0.0;

    @Column(name = "total_games_played")
    private Integer totalGamesPlayed = 0;

    @Column(name = "total_games_won")
    private Integer totalGamesWon = 0;

    @Column(name = "total_playtime_seconds")
    private Long totalPlaytimeSeconds = 0L;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TeamMember> members = new HashSet<>();

    @OneToMany(mappedBy = "team")
    private Set<GameSession> sessions = new HashSet<>();

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
     * Проверяет, активна ли команда
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Получает текущее количество участников
     */
    public int getCurrentMemberCount() {
        return members != null ? members.size() : 0;
    }

    /**
     * Проверяет, есть ли место в команде
     */
    public boolean hasSpace() {
        if (maxMembers == null) {
            return true;
        }
        return getCurrentMemberCount() < maxMembers;
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
     * Получает уровень команды на основе рейтинга
     */
    public String getTeamLevel() {
        if (rating == null) {
            return "Новички";
        }
        
        if (rating >= 4.5) return "Легенды";
        if (rating >= 4.0) return "Мастера";
        if (rating >= 3.5) return "Эксперты";
        if (rating >= 3.0) return "Профи";
        if (rating >= 2.5) return "Опытные";
        if (rating >= 2.0) return "Уверенные";
        if (rating >= 1.5) return "Продвинутые";
        if (rating >= 1.0) return "Средние";
        return "Новички";
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
     * Проверяет, является ли пользователь капитаном команды
     */
    public boolean isCaptain(User user) {
        return captain != null && captain.equals(user);
    }

    /**
     * Проверяет, является ли пользователь участником команды
     */
    public boolean isMember(User user) {
        if (user == null) {
            return false;
        }
        
        return members.stream()
                .anyMatch(member -> member.getUser().equals(user));
    }

    /**
     * Получает роль пользователя в команде
     */
    public String getUserRole(User user) {
        if (isCaptain(user)) {
            return "Капитан";
        } else if (isMember(user)) {
            TeamMember member = members.stream()
                    .filter(m -> m.getUser().equals(user))
                    .findFirst()
                    .orElse(null);
            return member != null ? member.getRole().name() : "Участник";
        }
        return "Не участник";
    }

    /**
     * Получает список активных участников
     */
    public Set<TeamMember> getActiveMembers() {
        if (members == null) {
            return new HashSet<>();
        }
        
        return members.stream()
                .filter(TeamMember::isActive)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Получает количество активных участников
     */
    public int getActiveMemberCount() {
        return getActiveMembers().size();
    }

    /**
     * Проверяет, можно ли добавить участника в команду
     */
    public boolean canAddMember() {
        return isActive() && hasSpace();
    }

    /**
     * Получает средний рейтинг участников команды
     */
    public double getAverageMemberRating() {
        Set<TeamMember> activeMembers = getActiveMembers();
        if (activeMembers.isEmpty()) {
            return 0.0;
        }
        
        double totalRating = activeMembers.stream()
                .mapToDouble(member -> {
                    User user = member.getUser();
                    return user.getRating() != null ? user.getRating() : 0.0;
                })
                .sum();
        
        return totalRating / activeMembers.size();
    }
}