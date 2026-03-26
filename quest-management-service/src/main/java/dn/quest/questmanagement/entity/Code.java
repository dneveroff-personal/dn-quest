package dn.quest.questmanagement.entity;

import dn.quest.shared.enums.CodeType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность кода уровня
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "level_codes",
       indexes = {
           @Index(name = "idx_code_level_id", columnList = "level_id"),
           @Index(name = "idx_code_type", columnList = "code_type"),
           @Index(name = "idx_code_sector_no", columnList = "sector_no"),
           @Index(name = "idx_code_value", columnList = "code_value"),
           @Index(name = "idx_code_level_type", columnList = "level_id, code_type"),
           @Index(name = "idx_code_level_sector", columnList = "level_id, sector_no"),
           @Index(name = "idx_code_created_at", columnList = "created_at")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_code_level_sector", columnNames = {"level_id", "sector_no"})
       })
public class Code {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID уровня, к которому относится код
     */
    @NotNull(message = "ID уровня обязателен")
    @Column(name = "level_id", nullable = false)
    private Long levelId;

    /**
     * Тип кода
     */
    @NotNull(message = "Тип кода обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "code_type", nullable = false, length = 16)
    private CodeType codeType;

    /**
     * Номер сектора (только для NORMAL кодов)
     */
    @Column(name = "sector_no")
    private Integer sectorNo;

    /**
     * Значение кода (нормализованное - в нижнем регистре)
     */
    @NotBlank(message = "Значение кода не может быть пустым")
    @Size(min = 1, max = 200, message = "Длина кода должна быть от 1 до 200 символов")
    @Column(name = "code_value", nullable = false, length = 200)
    private String codeValue;

    /**
     * Сдвиг времени в секундах (>0 для бонуса, <0 для штрафа, 0 для обычного)
     */
    @Column(name = "shift_seconds", nullable = false)
    @Builder.Default
    private Integer shiftSeconds = 0;

    /**
     * Описание кода (для бонусных и штрафных кодов)
     */
    @Size(max = 500, message = "Описание кода не должно превышать 500 символов")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Активен ли код
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Количество использований кода
     */
    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    /**
     * Максимальное количество использований (null - без ограничений)
     */
    @Column(name = "max_usage_count")
    private Integer maxUsageCount;

    /**
     * Дата создания
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Дата обновления
     */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * ID пользователя, создавшего код
     */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /**
     * ID пользователя, обновившего код
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * Версия кода для оптимистичной блокировки
     */
    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        
        // Нормализуем значение кода
        if (codeValue != null) {
            codeValue = codeValue.toLowerCase().trim();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        // Нормализуем значение кода
        if (codeValue != null) {
            codeValue = codeValue.toLowerCase().trim();
        }
    }

    /**
     * Проверяет, является ли код обычным
     */
    public boolean isNormalCode() {
        return codeType != null && codeType.isNormal();
    }

    /**
     * Проверяет, является ли код бонусным
     */
    public boolean isBonusCode() {
        return codeType != null && codeType.isBonus();
    }

    /**
     * Проверяет, является ли код штрафным
     */
    public boolean isPenaltyCode() {
        return codeType != null && codeType.isPenalty();
    }

    /**
     * Проверяет, есть ли ограничение на количество использований
     */
    public boolean hasUsageLimit() {
        return maxUsageCount != null && maxUsageCount > 0;
    }

    /**
     * Проверяет, можно ли использовать код
     */
    public boolean canBeUsed() {
        if (!active) {
            return false;
        }
        
        if (hasUsageLimit() && usageCount >= maxUsageCount) {
            return false;
        }
        
        return true;
    }

    /**
     * Увеличивает счетчик использований кода
     */
    public void incrementUsageCount() {
        if (usageCount == null) {
            usageCount = 0;
        }
        usageCount++;
    }

    /**
     * Проверяет, соответствует ли код указанному значению
     */
    public boolean matches(String inputCode) {
        if (inputCode == null || codeValue == null) {
            return false;
        }
        return codeValue.equals(inputCode.toLowerCase().trim());
    }

    /**
     * Получает копию кода с новыми параметрами
     */
    public Code copy() {
        return Code.builder()
                .levelId(this.levelId)
                .codeType(this.codeType)
                .sectorNo(this.sectorNo)
                .codeValue(this.codeValue)
                .shiftSeconds(this.shiftSeconds)
                .description(this.description)
                .active(this.active)
                .maxUsageCount(this.maxUsageCount)
                .build();
    }

    /**
     * Обновляет код из другого кода
     */
    public void updateFrom(Code other) {
        this.codeType = other.getCodeType();
        this.sectorNo = other.getSectorNo();
        this.codeValue = other.getCodeValue();
        this.shiftSeconds = other.getShiftSeconds();
        this.description = other.getDescription();
        this.active = other.getActive();
        this.maxUsageCount = other.getMaxUsageCount();
    }

    /**
     * Валидирует код перед сохранением
     */
    public boolean isValid() {
        // Для обычных кодов должен быть указан номер сектора
        if (isNormalCode() && sectorNo == null) {
            return false;
        }
        
        // Для бонусных и штрафных кодов номер сектора не нужен
        if (!isNormalCode() && sectorNo != null) {
            return false;
        }
        
        // Номер сектора должен быть положительным
        if (sectorNo != null && sectorNo <= 0) {
            return false;
        }
        
        // Значение кода не должно быть пустым
        if (codeValue == null || codeValue.trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
}