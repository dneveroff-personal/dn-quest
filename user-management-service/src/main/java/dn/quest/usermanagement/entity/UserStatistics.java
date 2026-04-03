package dn.quest.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Сущность статистики пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_statistics",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_statistics_user_id", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_user_statistics_user_id", columnList = "user_id"),
                @Index(name = "idx_user_statistics_total_score", columnList = "total_score"),
                @Index(name = "idx_user_statistics_level", columnList = "level"),
                @Index(name = "idx_user_statistics_last_activity_at", columnList = "last_activity_at")
        })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    // Общая статистика
    @Column(name = "total_score", nullable = false)
    @Builder.Default
    private Long totalScore = 0L;

    @Column(name = "level", nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(name = "experience_points", nullable = false)
    @Builder.Default
    private Long experiencePoints = 0L;

    @Column(name = "experience_to_next_level", nullable = false)
    @Builder.Default
    private Long experienceToNextLevel = 100L;

    // Статистика квестов
    @Column(name = "quests_completed", nullable = false)
    @Builder.Default
    private Integer questsCompleted = 0;

    @Column(name = "quests_started", nullable = false)
    @Builder.Default
    private Integer questsStarted = 0;

    @Column(name = "quests_abandoned", nullable = false)
    @Builder.Default
    private Integer questsAbandoned = 0;

    @Column(name = "total_playtime_minutes", nullable = false)
    @Builder.Default
    private Long totalPlaytimeMinutes = 0L;

    // Статистика уровней
    @Column(name = "levels_completed", nullable = false)
    @Builder.Default
    private Integer levelsCompleted = 0;

    @Column(name = "codes_solved", nullable = false)
    @Builder.Default
    private Integer codesSolved = 0;

    @Column(name = "hints_used", nullable = false)
    @Builder.Default
    private Integer hintsUsed = 0;

    @Column(name = "attempts_made", nullable = false)
    @Builder.Default
    private Integer attemptsMade = 0;

    // Статистика команд
    @Column(name = "teams_joined", nullable = false)
    @Builder.Default
    private Integer teamsJoined = 0;

    @Column(name = "teams_created", nullable = false)
    @Builder.Default
    private Integer teamsCreated = 0;

    @Column(name = "teams_led", nullable = false)
    @Builder.Default
    private Integer teamsLed = 0;

    @Column(name = "invitations_sent", nullable = false)
    @Builder.Default
    private Integer invitationsSent = 0;

    @Column(name = "invitations_received", nullable = false)
    @Builder.Default
    private Integer invitationsReceived = 0;

    // Достижения
    @Column(name = "achievements_unlocked", nullable = false)
    @Builder.Default
    private Integer achievementsUnlocked = 0;

    @Column(name = "rare_achievements", nullable = false)
    @Builder.Default
    private Integer rareAchievements = 0;

    @Column(name = "legendary_achievements", nullable = false)
    @Builder.Default
    private Integer legendaryAchievements = 0;

    // Активность
    @Column(name = "login_count", nullable = false)
    @Builder.Default
    private Integer loginCount = 0;

    @Column(name = "current_streak_days", nullable = false)
    @Builder.Default
    private Integer currentStreakDays = 0;

    @Column(name = "longest_streak_days", nullable = false)
    @Builder.Default
    private Integer longestStreakDays = 0;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(name = "first_login_at")
    private Instant firstLoginAt;

    // Временные метки
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Добавляет опыт и проверяет переход на новый уровень
     */
    public void addExperience(Long experience) {
        this.experiencePoints += experience;
        checkLevelUp();
    }

    /**
     * Проверяет и выполняет переход на новый уровень
     */
    public void checkLevelUp() {
        while (experiencePoints >= experienceToNextLevel) {
            experiencePoints -= experienceToNextLevel;
            level++;
            experienceToNextLevel = calculateExperienceForNextLevel(level);
        }
    }

    /**
     * Рассчитывает опыт для следующего уровня
     */
    private Long calculateExperienceForNextLevel(Integer level) {
        return 100L + (level - 1) * 50L;
    }

    /**
     * Увеличивает счетчик входов
     */
    public void incrementLoginCount() {
        this.loginCount++;
        this.lastLoginAt = Instant.now();
        if (this.firstLoginAt == null) {
            this.firstLoginAt = this.lastLoginAt;
        }
    }

    /**
     * Обновляет время последней активности
     */
    public void updateLastActivity() {
        this.lastActivityAt = Instant.now();
    }

    /**
     * Добавляет очки к общему счету
     */
    public void addScore(Long score) {
        this.totalScore += score;
    }

    /**
     * Увеличивает счетчик завершенных квестов
     */
    public void incrementQuestsCompleted() {
        this.questsCompleted++;
    }

    /**
     * Увеличивает счетчик начатых квестов
     */
    public void incrementQuestsStarted() {
        this.questsStarted++;
    }

    /**
     * Увеличивает счетчик брошенных квестов
     */
    public void incrementQuestsAbandoned() {
        this.questsAbandoned++;
    }

    /**
     * Добавляет время игры в минутах
     */
    public void addPlaytime(Long minutes) {
        this.totalPlaytimeMinutes += minutes;
    }
}