package dn.quest.questmanagement.dto;

import dn.quest.shared.dto.BaseDTO;
import dn.quest.shared.enums.CodeType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для представления кода уровня
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeDTO extends BaseDTO {

    /**
     * ID уровня, к которому относится код
     */
    @NotNull(message = "ID уровня обязателен")
    private UUID levelId;

    /**
     * Тип кода
     */
    @NotNull(message = "Тип кода обязателен")
    private CodeType codeType;

    /**
     * Номер сектора (только для NORMAL кодов)
     */
    private Integer sectorNo;

    /**
     * Значение кода
     */
    @NotBlank(message = "Значение кода не может быть пустым")
    @Size(min = 1, max = 200, message = "Длина кода должна быть от 1 до 200 символов")
    private String codeValue;

    /**
     * Сдвиг времени в секундах
     */
    private Integer shiftSeconds;

    /**
     * Описание кода
     */
    @Size(max = 500, message = "Описание кода не должно превышать 500 символов")
    private String description;

    /**
     * Активен ли код
     */
    private Boolean active;

    /**
     * Количество использований кода
     */
    private Integer usageCount;

    /**
     * Максимальное количество использований
     */
    private Integer maxUsageCount;

    /**

    /**

    /**
     * ID пользователя, создавшего код
     */
    private Long createdBy;

    /**
     * ID пользователя, обновившего код
     */
    private Long updatedBy;

    /**
     * Версия кода
     */
    private Long version;

    /**
     * Можно ли использовать код
     */
    private Boolean usable;

    /**
     * Осталось использований
     */
    private Integer remainingUsages;

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
     * Проверяет, активен ли код
     */
    public boolean isActive() {
        return active != null && active;
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
    public boolean isUsable() {
        if (usable != null) {
            return usable;
        }
        
        if (!isActive()) {
            return false;
        }

        return !hasUsageLimit() || getRemainingUsages() > 0;
    }

    /**
     * Получает оставшееся количество использований
     */
    public Integer getRemainingUsages() {
        if (!hasUsageLimit()) {
            return null;
        }
        
        int used = usageCount != null ? usageCount : 0;
        return Math.max(0, maxUsageCount - used);
    }

    /**
     * Проверяет, есть ли у кода описание
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    /**
     * Проверяет, использовался ли код
     */
    public boolean isUsed() {
        return usageCount != null && usageCount > 0;
    }

    /**
     * Проверяет, достигнут ли лимит использований
     */
    public boolean isUsageLimitReached() {
        if (!hasUsageLimit()) {
            return false;
        }
        
        int used = usageCount != null ? usageCount : 0;
        return used >= maxUsageCount;
    }

    /**
     * Получает отображаемое имя типа кода
     */
    public String getCodeTypeDisplayName() {
        return codeType != null ? codeType.getDisplayName() : null;
    }

    /**
     * Получает информацию о сдвиге времени в человекочитаемом формате
     */
    public String getShiftTimeDisplay() {
        if (shiftSeconds == null || shiftSeconds == 0) {
            return "Без изменения времени";
        }
        
        int absSeconds = Math.abs(shiftSeconds);
        int minutes = absSeconds / 60;
        int seconds = absSeconds % 60;
        
        String timeStr;
        if (minutes > 0) {
            timeStr = String.format("%dм %dс", minutes, seconds);
        } else {
            timeStr = String.format("%dс", seconds);
        }
        
        return shiftSeconds > 0 ? 
               String.format("Бонус +%s", timeStr) : 
               String.format("Штраф -%s", timeStr);
    }

    /**
     * Получает информацию об использовании
     */
    public String getUsageInfo() {
        if (!hasUsageLimit()) {
            return String.format("Использовано: %d раз", usageCount != null ? usageCount : 0);
        }
        
        return String.format("Использовано: %d/%d раз", 
                           usageCount != null ? usageCount : 0, 
                           maxUsageCount);
    }

    /**
     * Валидирует DTO перед сохранением
     */
    public boolean isValid() {
        // Проверка обязательных полей
        if (levelId == null) {
            return false;
        }
        
        if (codeType == null) {
            return false;
        }
        
        if (codeValue == null || codeValue.trim().isEmpty()) {
            return false;
        }
        
        // Проверка длины полей
        if (codeValue.length() > 200) {
            return false;
        }
        
        if (description != null && description.length() > 500) {
            return false;
        }
        
        // Проверка логики полей
        if (isNormalCode() && sectorNo == null) {
            return false;
        }
        
        if (!isNormalCode() && sectorNo != null) {
            return false;
        }
        
        if (sectorNo != null && sectorNo <= 0) {
            return false;
        }
        
        if (maxUsageCount != null && maxUsageCount <= 0) {
            return false;
        }

        return usageCount == null || usageCount >= 0;
    }

    /**
     * Подготавливает DTO к сохранению (очистка данных)
     */
    public void prepareForSave() {
        if (codeValue != null) {
            codeValue = codeValue.toLowerCase().trim();
        }
        
        if (description != null) {
            description = description.trim();
        }
        
        // Установка значений по умолчанию
        if (active == null) {
            active = true;
        }
        
        if (usageCount == null) {
            usageCount = 0;
        }
        
        if (shiftSeconds == null) {
            shiftSeconds = 0;
        }
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
     * Увеличивает счетчик использований
     */
    public void incrementUsage() {
        if (usageCount == null) {
            usageCount = 0;
        }
        usageCount++;
    }

    /**
     * Создает копию DTO
     */
    public CodeDTO copy() {
        return CodeDTO.builder()
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
}