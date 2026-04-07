package dn.quest.gameengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Завершение уровня в игровой сессии
 */
@Data
@Entity
@Table(name = "game_level_completions",
        uniqueConstraints = @UniqueConstraint(name = "uk_session_level_complete", columnNames = {"session_id", "level_id"}),
        indexes = {
                @Index(name = "idx_completion_session", columnList = "session_id"),
                @Index(name = "idx_completion_level", columnList = "level_id"),
                @Index(name = "idx_completion_pass_time", columnList = "pass_time"),
                @Index(name = "idx_completion_passed_by", columnList = "passed_by_user_id")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(exclude = {"session", "level", "passedByUser"})
@ToString(exclude = {"session", "level", "passedByUser"})
public class LevelCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passed_by_user_id")
    private User passedByUser;

    @Column(name = "pass_time", nullable = false)
    private Instant passTime;

    @Column(name = "duration_sec", nullable = false)
    private long durationSec;

    @Column(name = "bonus_on_level_sec", nullable = false)
    private int bonusOnLevelSec = 0;

    @Column(name = "penalty_on_level_sec", nullable = false)
    private int penaltyOnLevelSec = 0;

    @Column(name = "total_attempts")
    private int totalAttempts = 0;

    @Column(name = "successful_attempts")
    private int successfulAttempts = 0;

    @Column(name = "hints_used")
    private int hintsUsed = 0;

    @Column(name = "sectors_closed")
    private int sectorsClosed = 0;

    @Column(name = "completion_method")
    private String completionMethod; // "MANUAL", "AUTO_PASS", "TIMEOUT"

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @PrePersist
    public void prePersist() {
        if (passTime == null) {
            passTime = Instant.now();
        }
    }

    /**
     * Получает длительность прохождения уровня как Duration
     */
    public Duration getDuration() {
        return Duration.ofSeconds(durationSec);
    }

    /**
     * Получает скорректированную длительность с учетом бонусов и штрафов
     */
    public long getAdjustedDurationSec() {
        return durationSec + bonusOnLevelSec - penaltyOnLevelSec;
    }

    /**
     * Получает скорректированную длительность как Duration
     */
    public Duration getAdjustedDuration() {
        return Duration.ofSeconds(getAdjustedDurationSec());
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
     * Получает эффективность прохождения (учитывает попытки и подсказки)
     */
    public double getEfficiencyScore() {
        if (totalAttempts == 0) {
            return 0.0;
        }
        
        double attemptsScore = (double) successfulAttempts / totalAttempts;
        double hintsPenalty = Math.max(0, 1.0 - (hintsUsed * 0.1)); // Каждая подсказка снижает на 10%
        
        return attemptsScore * hintsPenalty;
    }

    /**
     * Проверяет, было ли завершение автоматическим
     */
    public boolean isAutoCompleted() {
        return "AUTO_PASS".equals(completionMethod) || "TIMEOUT".equals(completionMethod);
    }

    /**
     * Проверяет, было ли завершение ручным
     */
    public boolean isManuallyCompleted() {
        return "MANUAL".equals(completionMethod);
    }

    /**
     * Получает оценку качества прохождения (от 1 до 5)
     */
    public int getQualityRating() {
        double efficiency = getEfficiencyScore();
        
        if (efficiency >= 0.9) return 5; // Отлично
        if (efficiency >= 0.7) return 4; // Хорошо
        if (efficiency >= 0.5) return 3; // Удовлетворительно
        if (efficiency >= 0.3) return 2; // Плохо
        return 1; // Очень плохо
    }

    /**
     * Получает бонус за скорость (если уровень пройден быстро)
     */
    public int getSpeedBonus() {
        if (level == null || level.getApTime() == null) {
            return 0;
        }

        int timeLimit = level.getApTime();
        if (durationSec < timeLimit * 0.5) {
            return 300; // 5 минут бонуса за очень быстрое прохождение
        } else if (durationSec < timeLimit * 0.75) {
            return 180; // 3 минуты бонуса за быстрое прохождение
        } else if (durationSec < timeLimit) {
            return 60;  // 1 минута бонуса за прохождение в пределах лимита
        }
        
        return 0;
    }
}