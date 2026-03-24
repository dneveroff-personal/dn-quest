package dn.quest.gameengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Уровень квеста
 */
@Data
@Entity
@Table(name = "levels",
        uniqueConstraints = @UniqueConstraint(name = "uk_level_order_in_quest", columnNames = {"quest_id", "order_index"}),
        indexes = {
                @Index(name = "idx_level_quest", columnList = "quest_id"),
                @Index(name = "idx_level_order", columnList = "order_index"),
                @Index(name = "idx_level_ap_time", columnList = "ap_time")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(exclude = {"quest", "codes", "hints", "completions", "attempts", "progresses"})
@ToString(exclude = {"quest", "codes", "hints", "completions", "attempts", "progresses"})
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    private String descriptionHtml;

    @Column(name = "ap_time")
    private Integer apTime;

    @Column(nullable = false)
    private Integer requiredSectors = 0;

    @Column(name = "max_attempts_per_minute")
    private Integer maxAttemptsPerMinute = 30;

    @Column(name = "max_hints")
    private Integer maxHints = 5;

    @Column(name = "hint_penalty_seconds")
    private Integer hintPenaltySeconds = 60;

    @Column(name = "wrong_attempt_penalty_seconds")
    private Integer wrongAttemptPenaltySeconds = 30;

    @Column(name = "bonus_code_seconds")
    private Integer bonusCodeSeconds = 300;

    @Column(name = "penalty_code_seconds")
    private Integer penaltyCodeSeconds = 180;

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Code> codes = new HashSet<>();

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LevelHint> hints = new HashSet<>();

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LevelCompletion> completions = new HashSet<>();

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CodeAttempt> attempts = new HashSet<>();

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LevelProgress> progresses = new HashSet<>();

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Проверяет, есть ли ограничение по времени
     */
    public boolean hasTimeLimit() {
        return apTime != null && apTime > 0;
    }

    /**
     * Проверяет, есть ли ограничение на количество попыток
     */
    public boolean hasAttemptLimit() {
        return maxAttemptsPerMinute != null && maxAttemptsPerMinute > 0;
    }

    /**
     * Проверяет, есть ли ограничение на количество подсказок
     */
    public boolean hasHintLimit() {
        return maxHints != null && maxHints > 0;
    }

    /**
     * Получает количество кодов уровня
     */
    public int getCodeCount() {
        return codes != null ? codes.size() : 0;
    }

    /**
     * Получает количество подсказок уровня
     */
    public int getHintCount() {
        return hints != null ? hints.size() : 0;
    }

    /**
     * Получает количество нормальных кодов
     */
    public long getNormalCodeCount() {
        return codes.stream()
                .filter(code -> code.getType() == dn.quest.shared.enums.CodeType.NORMAL)
                .count();
    }

    /**
     * Получает количество бонусных кодов
     */
    public long getBonusCodeCount() {
        return codes.stream()
                .filter(code -> code.getType() == dn.quest.shared.enums.CodeType.BONUS)
                .count();
    }

    /**
     * Получает количество штрафных кодов
     */
    public long getPenaltyCodeCount() {
        return codes.stream()
                .filter(code -> code.getType() == dn.quest.shared.enums.CodeType.PENALTY)
                .count();
    }

    /**
     * Проверяет, является ли уровень первым в квесте
     */
    public boolean isFirstLevel() {
        return orderIndex != null && orderIndex == 1;
    }

    /**
     * Проверяет, является ли уровень последним (предполагается, что уровни пронумерованы последовательно)
     */
    public boolean isLastLevel() {
        if (quest == null || quest.getLevels() == null) {
            return false;
        }
        return orderIndex != null && orderIndex.equals(quest.getLevels().size());
    }

    /**
     * Получает сложность уровня на основе параметров
     */
    public String getComplexityLevel() {
        int complexityScore = 0;
        
        // Учитываем количество требуемых секторов
        if (requiredSectors != null) {
            complexityScore += requiredSectors * 2;
        }
        
        // Учитываем ограничение по времени
        if (hasTimeLimit()) {
            complexityScore += Math.max(0, 10 - (apTime / 600)); // Чем меньше время, тем выше сложность
        }
        
        // Учитываем количество кодов
        complexityScore += getCodeCount();
        
        if (complexityScore <= 5) return "EASY";
        if (complexityScore <= 10) return "MEDIUM";
        if (complexityScore <= 15) return "HARD";
        return "EXPERT";
    }

    /**
     * Получает максимальный бонус за уровень
     */
    public int getMaxBonusSeconds() {
        int maxBonus = 0;
        
        if (bonusCodeSeconds != null) {
            maxBonus += bonusCodeSeconds * getBonusCodeCount();
        }
        
        return maxBonus;
    }

    /**
     * Получает максимальный штраф за уровень
     */
    public int getMaxPenaltySeconds() {
        int maxPenalty = 0;
        
        if (penaltyCodeSeconds != null) {
            maxPenalty += penaltyCodeSeconds * getPenaltyCodeCount();
        }
        
        if (wrongAttemptPenaltySeconds != null) {
            // Предполагаем максимум 10 неправильных попыток
            maxPenalty += wrongAttemptPenaltySeconds * 10;
        }
        
        return maxPenalty;
    }
}