package dn.quest.gameengine.entity;

import dn.quest.shared.enums.CodeType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Код уровня
 */
@Data
@Entity
@Table(name = "level_codes",
        indexes = {
                @Index(name = "idx_code_level", columnList = "level_id"),
                @Index(name = "idx_code_type_sector", columnList = "type,sector_no"),
                @Index(name = "idx_code_value", columnList = "value")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EqualsAndHashCode(exclude = {"level"})
@ToString(exclude = {"level"})
public class Code {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CodeType type = CodeType.NORMAL;

    @Column(name = "sector_no")
    private Integer sectorNo;

    @Column(nullable = false, length = 200)
    private String value;

    @Column(nullable = false)
    private Integer shiftSeconds = 0;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "max_usage_count")
    private Integer maxUsageCount;

    /**
     * Проверяет, является ли код нормальным
     */
    public boolean isNormal() {
        return type == CodeType.NORMAL;
    }

    /**
     * Проверяет, является ли код бонусным
     */
    public boolean isBonus() {
        return type == CodeType.BONUS;
    }

    /**
     * Проверяет, является ли код штрафным
     */
    public boolean isPenalty() {
        return type == CodeType.PENALTY;
    }

    /**
     * Проверяет, активен ли код
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Проверяет, можно ли использовать код (проверка лимита использования)
     */
    public boolean canBeUsed() {
        if (!isActive()) {
            return false;
        }

        return maxUsageCount == null || usageCount == null || usageCount < maxUsageCount;
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
     * Получает бонусное время в секундах
     */
    public int getBonusTimeSeconds() {
        return isBonus() && shiftSeconds != null ? Math.max(0, shiftSeconds) : 0;
    }

    /**
     * Получает штрафное время в секундах
     */
    public int getPenaltyTimeSeconds() {
        return isPenalty() && shiftSeconds != null ? Math.max(0, Math.abs(shiftSeconds)) : 0;
    }

    /**
     * Получает эффективное время в секундах (положительное для бонуса, отрицательное для штрафа)
     */
    public int getEffectiveTimeSeconds() {
        if (shiftSeconds == null) {
            return 0;
        }
        return shiftSeconds;
    }

    /**
     * Проверяет, есть ли ограничение на использование
     */
    public boolean hasUsageLimit() {
        return maxUsageCount != null && maxUsageCount > 0;
    }

    /**
     * Получает оставшееся количество использований
     */
    public int getRemainingUsages() {
        if (!hasUsageLimit()) {
            return Integer.MAX_VALUE;
        }
        
        int used = usageCount != null ? usageCount : 0;
        return Math.max(0, maxUsageCount - used);
    }

    /**
     * Проверяет, является ли код секторным
     */
    public boolean isSectorCode() {
        return isNormal() && sectorNo != null;
    }

    /**
     * Получает тип кода в виде строки
     */
    public String getTypeDescription() {
        switch (type) {
            case NORMAL:
                return isSectorCode() ? "Секторный код" : "Нормальный код";
            case BONUS:
                return "Бонусный код";
            case PENALTY:
                return "Штрафной код";
            default:
                return "Неизвестный тип";
        }
    }
}