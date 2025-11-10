package dn.quest.gameengine.entity;

import dn.quest.shared.enums.TeamRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.Instant;

/**
 * Участник команды
 */
@Data
@Entity
@Table(name = "team_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_team_member_user_team", columnNames = {"user_id", "team_id"}),
        indexes = {
                @Index(name = "idx_team_member_user", columnList = "user_id"),
                @Index(name = "idx_team_member_team", columnList = "team_id"),
                @Index(name = "idx_team_member_role", columnList = "role"),
                @Index(name = "idx_team_member_joined_at", columnList = "joined_at")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(exclude = {"user", "team"})
@ToString(exclude = {"user", "team"})
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamRole role = TeamRole.MEMBER;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();

    @Column(name = "left_at")
    private Instant leftAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "invitation_code")
    private String invitationCode;

    @Column(name = "contribution_score")
    private Integer contributionScore = 0;

    @Column(name = "games_played")
    private Integer gamesPlayed = 0;

    @Column(name = "games_won")
    private Integer gamesWon = 0;

    @Column(name = "total_playtime_seconds")
    private Long totalPlaytimeSeconds = 0L;

    @PrePersist
    public void prePersist() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }

    /**
     * Проверяет, активен ли участник в команде
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive) && leftAt == null;
    }

    /**
     * Проверяет, является ли участник капитаном
     */
    public boolean isCaptain() {
        return role == TeamRole.CAPTAIN;
    }

    /**
     * Проверяет, является ли участник обычным членом команды
     */
    public boolean isRegularMember() {
        return role == TeamRole.MEMBER;
    }

    /**
     * Проверяет, является ли участник заместителем
     */
    public boolean isDeputy() {
        return role == TeamRole.DEPUTY;
    }

    /**
     * Исключает участника из команды
     */
    public void leaveTeam() {
        this.leftAt = Instant.now();
        this.isActive = false;
    }

    /**
     * Восстанавливает участника в команде
     */
    public void rejoinTeam() {
        this.leftAt = null;
        this.isActive = true;
    }

    /**
     * Получает процент побед в команде
     */
    public double getWinPercentage() {
        if (gamesPlayed == null || gamesPlayed == 0) {
            return 0.0;
        }
        int won = gamesWon != null ? gamesWon : 0;
        return (double) won / gamesPlayed * 100.0;
    }

    /**
     * Получает среднее время игры в минутах
     */
    public double getAveragePlaytimeMinutes() {
        if (gamesPlayed == null || gamesPlayed == 0 || totalPlaytimeSeconds == null) {
            return 0.0;
        }
        return (double) totalPlaytimeSeconds / gamesPlayed / 60.0;
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
     * Получает длительность участия в команде в днях
     */
    public long getMembershipDays() {
        Instant endTime = leftAt != null ? leftAt : Instant.now();
        return (endTime.getEpochSecond() - joinedAt.getEpochSecond()) / (24 * 60 * 60);
    }

    /**
     * Обновляет статистику после завершения игры
     */
    public void updateGameStats(boolean won, long playtimeSeconds) {
        if (gamesPlayed == null) {
            gamesPlayed = 0;
        }
        if (gamesWon == null) {
            gamesWon = 0;
        }
        if (totalPlaytimeSeconds == null) {
            totalPlaytimeSeconds = 0L;
        }
        
        gamesPlayed++;
        if (won) {
            gamesWon++;
        }
        totalPlaytimeSeconds += playtimeSeconds;
    }

    /**
     * Увеличивает счетчик вклада
     */
    public void incrementContributionScore(int points) {
        if (contributionScore == null) {
            contributionScore = 0;
        }
        contributionScore += Math.max(0, points);
    }

    /**
     * Получает уровень участника на основе вклада
     */
    public String getContributionLevel() {
        if (contributionScore == null) {
            return "Новичок";
        }
        
        if (contributionScore >= 1000) return "Легенда";
        if (contributionScore >= 500) return "Мастер";
        if (contributionScore >= 250) return "Эксперт";
        if (contributionScore >= 100) return "Профи";
        if (contributionScore >= 50) return "Опытный";
        if (contributionScore >= 25) return "Уверенный";
        if (contributionScore >= 10) return "Продвинутый";
        if (contributionScore >= 5) return "Средний";
        return "Новичок";
    }

    /**
     * Получает описание роли
     */
    public String getRoleDescription() {
        switch (role) {
            case CAPTAIN:
                return "Капитан";
            case DEPUTY:
                return "Заместитель капитана";
            case MEMBER:
                return "Участник";
            default:
                return "Участник";
        }
    }

    /**
     * Получает статус участника
     */
    public String getMemberStatus() {
        if (!isActive()) {
            return "Неактивен";
        }
        if (leftAt != null) {
            return "Покинул команду";
        }
        return "Активен";
    }

    /**
     * Получает эффективность участника (соотношение побед к играм)
     */
    public double getEfficiency() {
        double winPercentage = getWinPercentage();
        double contributionFactor = contributionScore != null ? Math.min(contributionScore / 100.0, 1.0) : 0.0;
        
        return (winPercentage * 0.7) + (contributionFactor * 0.3);
    }

    /**
     * Получает рейтинг участника (от 1 до 5)
     */
    public int getMemberRating() {
        double efficiency = getEfficiency();
        
        if (efficiency >= 0.9) return 5;
        if (efficiency >= 0.7) return 4;
        if (efficiency >= 0.5) return 3;
        if (efficiency >= 0.3) return 2;
        return 1;
    }
}