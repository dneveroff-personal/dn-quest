package dn.quest.gameengine.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.Instant;

/**
 * Подсказка для уровня
 */
@Data
@Entity
@Table(name = "level_hints",
        indexes = {
                @Index(name = "idx_hint_level", columnList = "level_id"),
                @Index(name = "idx_hint_order", columnList = "order_index"),
                @Index(name = "idx_hint_offset", columnList = "offset_sec")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(exclude = {"level"})
@ToString(exclude = {"level"})
public class LevelHint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(name = "offset_sec", nullable = false)
    private Integer offsetSec;

    @Column(name = "penalty_seconds")
    private Integer penaltySeconds = 60;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "hint_type")
    private String hintType; // "DIRECTION", "LOCATION", "CODE_FORMAT", "GENERAL"

    @Column(name = "difficulty_level")
    private String difficultyLevel; // "EASY", "MEDIUM", "HARD"

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Проверяет, активна ли подсказка
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Проверяет, доступна ли подсказка по времени
     */
    public boolean isAvailable(long levelDurationSeconds) {
        return offsetSec != null && levelDurationSeconds >= offsetSec;
    }

    /**
     * Получает время до доступности подсказки
     */
    public long getTimeToAvailability(long levelDurationSeconds) {
        if (offsetSec == null) {
            return 0;
        }
        return Math.max(0, offsetSec - levelDurationSeconds);
    }

    /**
     * Увеличивает счетчик использования
     */
    public void incrementUsageCount() {
        if (usageCount == null) {
            usageCount = 0;
        }
        usageCount++;
    }

    /**
     * Получает штрафное время в секундах
     */
    public int getPenaltySeconds() {
        return penaltySeconds != null ? penaltySeconds : 0;
    }

    /**
     * Получает тип подсказки с описанием
     */
    public String getHintTypeDescription() {
        switch (hintType != null ? hintType.toUpperCase() : "GENERAL") {
            case "DIRECTION":
                return "Направление";
            case "LOCATION":
                return "Местоположение";
            case "CODE_FORMAT":
                return "Формат кода";
            case "GENERAL":
                return "Общая подсказка";
            default:
                return "Подсказка";
        }
    }

    /**
     * Получает уровень сложности подсказки
     */
    public String getDifficultyDescription() {
        switch (difficultyLevel != null ? difficultyLevel.toUpperCase() : "MEDIUM") {
            case "EASY":
                return "Легкая";
            case "MEDIUM":
                return "Средняя";
            case "HARD":
                return "Сложная";
            default:
                return "Средняя";
        }
    }

    /**
     * Проверяет, является ли подсказка легкой
     */
    public boolean isEasyHint() {
        return "EASY".equalsIgnoreCase(difficultyLevel);
    }

    /**
     * Проверяет, является ли подсказка сложной
     */
    public boolean isHardHint() {
        return "HARD".equalsIgnoreCase(difficultyLevel);
    }

    /**
     * Получает приоритет подсказки (чем меньше, тем раньше доступна)
     */
    public int getPriority() {
        return orderIndex != null ? orderIndex : Integer.MAX_VALUE;
    }

    /**
     * Получает "стоимость" подсказки в секундах штрафа
     */
    public int getCostInSeconds() {
        int baseCost = getPenaltySeconds();
        
        // Увеличиваем стоимость для сложных подсказок
        if (isHardHint()) {
            baseCost = (int) (baseCost * 1.5);
        }
        
        // Уменьшаем стоимость для легких подсказок
        if (isEasyHint()) {
            baseCost = (int) (baseCost * 0.7);
        }
        
        return baseCost;
    }

    /**
     * Получает описание подсказки с информацией о доступности
     */
    public String getFullDescription(long levelDurationSeconds) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHintTypeDescription());
        
        if (difficultyLevel != null) {
            sb.append(" (").append(getDifficultyDescription()).append(")");
        }
        
        if (!isAvailable(levelDurationSeconds)) {
            long timeToAvailability = getTimeToAvailability(levelDurationSeconds);
            sb.append(" - доступна через ").append(formatDuration(timeToAvailability));
        } else {
            sb.append(" - доступна");
        }
        
        return sb.toString();
    }

    /**
     * Форматирует длительность в человекочитаемый формат
     */
    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " сек";
        } else if (seconds < 3600) {
            return (seconds / 60) + " мин " + (seconds % 60) + " сек";
        } else {
            return (seconds / 3600) + " ч " + ((seconds % 3600) / 60) + " мин";
        }
    }
}