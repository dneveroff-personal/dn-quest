package dn.quest.gameengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.Instant;

/**
 * Прогресс прохождения уровня в игровой сессии
 */
@Data
@Entity
@Table(name = "game_level_progress",
        uniqueConstraints = @UniqueConstraint(name = "uk_progress_session_level", columnNames = {"session_id", "level_id"}),
        indexes = {
                @Index(name = "idx_progress_session", columnList = "session_id"),
                @Index(name = "idx_progress_level", columnList = "level_id"),
                @Index(name = "idx_progress_started_at", columnList = "started_at"),
                @Index(name = "idx_progress_closed_at", columnList = "closed_at")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(exclude = {"session", "level"})
@ToString(exclude = {"session", "level"})
public class LevelProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(nullable = false)
    private int sectorsClosed = 0;

    @Column(nullable = false)
    private int bonusOnLevelSec = 0;

    @Column(nullable = false)
    private int penaltyOnLevelSec = 0;

    @Column(name = "hints_used")
    private int hintsUsed = 0;

    @Column(name = "total_attempts")
    private int totalAttempts = 0;

    @Column(name = "successful_attempts")
    private int successfulAttempts = 0;

    @Column(name = "wrong_attempts")
    private int wrongAttempts = 0;

    @Column(name = "duplicate_attempts")
    private int duplicateAttempts = 0;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @PrePersist
    public void prePersist() {
        if (startedAt == null) {
            startedAt = Instant.now();
        }
        if (lastActivityAt == null) {
            lastActivityAt = startedAt;
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastActivityAt = Instant.now();
    }

    /**
     * Проверяет, активен ли уровень
     */
    public boolean isActive() {
        return closedAt == null;
    }

    /**
     * Проверяет, завершен ли уровень
     */
    public boolean isCompleted() {
        return closedAt != null;
    }

    /**
     * Получает длительность прохождения уровня в секундах
     */
    public long getDurationSeconds() {
        if (startedAt == null) {
            return 0;
        }
        Instant endTime = closedAt != null ? closedAt : Instant.now();
        return endTime.getEpochSecond() - startedAt.getEpochSecond();
    }

    /**
     * Получает скорректированное время с учетом бонусов и штрафов
     */
    public long getAdjustedDurationSeconds() {
        return getDurationSeconds() + bonusOnLevelSec - penaltyOnLevelSec;
    }

    /**
     * Получает процент закрытых секторов
     */
    public double getSectorsClosedPercentage() {
        if (level == null || level.getRequiredSectors() == null || level.getRequiredSectors() == 0) {
            return 0.0;
        }
        return (double) sectorsClosed / level.getRequiredSectors() * 100.0;
    }

    /**
     * Проверяет, достаточно ли секторов закрыто для завершения уровня
     */
    public boolean hasEnoughSectorsClosed() {
        if (level == null || level.getRequiredSectors() == null) {
            return false;
        }
        return sectorsClosed >= level.getRequiredSectors();
    }

    /**
     * Получает процент успешных попыток
     */
    public double getSuccessfulAttemptsPercentage() {
        if (totalAttempts == 0) {
            return 0.0;
        }
        return (double) successfulAttempts / totalAttempts * 100.0;
    }

    /**
     * Увеличивает счетчик общих попыток
     */
    public void incrementTotalAttempts() {
        this.totalAttempts++;
    }

    /**
     * Увеличивает счетчик успешных попыток
     */
    public void incrementSuccessfulAttempts() {
        this.successfulAttempts++;
    }

    /**
     * Увеличивает счетчик неправильных попыток
     */
    public void incrementWrongAttempts() {
        this.wrongAttempts++;
    }

    /**
     * Увеличивает счетчик дубликатных попыток
     */
    public void incrementDuplicateAttempts() {
        this.duplicateAttempts++;
    }

    /**
     * Добавляет бонусное время
     */
    public void addBonusTime(int seconds) {
        this.bonusOnLevelSec += Math.max(0, seconds);
    }

    /**
     * Добавляет штрафное время
     */
    public void addPenaltyTime(int seconds) {
        this.penaltyOnLevelSec += Math.max(0, seconds);
    }

    /**
     * Увеличивает счетчик использованных подсказок
     */
    public void incrementHintsUsed() {
        this.hintsUsed++;
    }
}