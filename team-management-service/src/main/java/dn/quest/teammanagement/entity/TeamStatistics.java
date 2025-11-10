package dn.quest.teammanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Сущность статистики команды
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team_statistics",
       indexes = {
           @Index(name = "idx_statistics_team", columnList = "team_id"),
           @Index(name = "idx_statistics_rating", columnList = "rating"),
           @Index(name = "idx_statistics_updated_at", columnList = "updated_at")
       })
public class TeamStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false, unique = true)
    private Team team;

    @Column(name = "total_members")
    @Builder.Default
    private Integer totalMembers = 0;

    @Column(name = "active_members")
    @Builder.Default
    private Integer activeMembers = 0;

    @Column(name = "total_invitations_sent")
    @Builder.Default
    private Long totalInvitationsSent = 0L;

    @Column(name = "total_invitations_accepted")
    @Builder.Default
    private Long totalInvitationsAccepted = 0L;

    @Column(name = "total_invitations_declined")
    @Builder.Default
    private Long totalInvitationsDeclined = 0L;

    @Column(name = "total_games_played")
    @Builder.Default
    private Long totalGamesPlayed = 0L;

    @Column(name = "total_games_won")
    @Builder.Default
    private Long totalGamesWon = 0L;

    @Column(name = "total_games_lost")
    @Builder.Default
    private Long totalGamesLost = 0L;

    @Column(name = "total_quests_completed")
    @Builder.Default
    private Long totalQuestsCompleted = 0L;

    @Column(name = "total_score")
    @Builder.Default
    private Long totalScore = 0L;

    @Column(name = "average_score")
    @Builder.Default
    private Double averageScore = 0.0;

    @Column(name = "rating")
    @Builder.Default
    private Double rating = 1000.0;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "win_rate")
    @Builder.Default
    private Double winRate = 0.0;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
        calculateDerivedStats();
    }

    /**
     * Обновляет статистику при добавлении участника
     */
    public void incrementTotalMembers() {
        this.totalMembers = (this.totalMembers != null ? this.totalMembers : 0) + 1;
        this.activeMembers = (this.activeMembers != null ? this.activeMembers : 0) + 1;
        this.lastActivityAt = Instant.now();
    }

    /**
     * Обновляет статистику при удалении участника
     */
    public void decrementActiveMembers() {
        if (this.activeMembers != null && this.activeMembers > 0) {
            this.activeMembers--;
        }
        this.lastActivityAt = Instant.now();
    }

    /**
     * Увеличивает счетчик отправленных приглашений
     */
    public void incrementInvitationsSent() {
        this.totalInvitationsSent = (this.totalInvitationsSent != null ? this.totalInvitationsSent : 0L) + 1;
        this.lastActivityAt = Instant.now();
    }

    /**
     * Увеличивает счетчик принятых приглашений
     */
    public void incrementInvitationsAccepted() {
        this.totalInvitationsAccepted = (this.totalInvitationsAccepted != null ? this.totalInvitationsAccepted : 0L) + 1;
        this.lastActivityAt = Instant.now();
    }

    /**
     * Увеличивает счетчик отклоненных приглашений
     */
    public void incrementInvitationsDeclined() {
        this.totalInvitationsDeclined = (this.totalInvitationsDeclined != null ? this.totalInvitationsDeclined : 0L) + 1;
        this.lastActivityAt = Instant.now();
    }

    /**
     * Добавляет сыгранную игру
     */
    public void addGamePlayed(boolean won, Long score) {
        this.totalGamesPlayed = (this.totalGamesPlayed != null ? this.totalGamesPlayed : 0L) + 1;
        if (won) {
            this.totalGamesWon = (this.totalGamesWon != null ? this.totalGamesWon : 0L) + 1;
        } else {
            this.totalGamesLost = (this.totalGamesLost != null ? this.totalGamesLost : 0L) + 1;
        }
        if (score != null) {
            this.totalScore = (this.totalScore != null ? this.totalScore : 0L) + score;
        }
        this.lastActivityAt = Instant.now();
        calculateDerivedStats();
    }

    /**
     * Добавляет выполненный квест
     */
    public void addQuestCompleted(Long score) {
        this.totalQuestsCompleted = (this.totalQuestsCompleted != null ? this.totalQuestsCompleted : 0L) + 1;
        if (score != null) {
            this.totalScore = (this.totalScore != null ? this.totalScore : 0L) + score;
        }
        this.lastActivityAt = Instant.now();
        calculateDerivedStats();
    }

    /**
     * Обновляет рейтинг команды
     */
    public void updateRating(Double newRating) {
        this.rating = newRating != null ? newRating : 1000.0;
        this.lastActivityAt = Instant.now();
    }

    /**
     * Обновляет ранг команды
     */
    public void updateRank(Integer newRank) {
        this.rank = newRank;
        this.lastActivityAt = Instant.now();
    }

    /**
     * Рассчитывает производные статистики
     */
    private void calculateDerivedStats() {
        // Расчет винрейта
        if (totalGamesPlayed != null && totalGamesPlayed > 0) {
            long wins = totalGamesWon != null ? totalGamesWon : 0L;
            this.winRate = (double) wins / totalGamesPlayed * 100.0;
        } else {
            this.winRate = 0.0;
        }

        // Расчет среднего счета
        if (totalGamesPlayed != null && totalGamesPlayed > 0 && totalScore != null) {
            this.averageScore = (double) totalScore / totalGamesPlayed;
        } else {
            this.averageScore = 0.0;
        }
    }

    /**
     * Получает процент принятия приглашений
     */
    public Double getInvitationAcceptanceRate() {
        if (totalInvitationsSent != null && totalInvitationsSent > 0) {
            long accepted = totalInvitationsAccepted != null ? totalInvitationsAccepted : 0L;
            return (double) accepted / totalInvitationsSent * 100.0;
        }
        return 0.0;
    }
}