package dn.quest.questmanagement.dto;

import dn.quest.shared.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для представления уровня квеста
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LevelDTO extends BaseDTO {

    /**
     * ID квеста, к которому относится уровень
     */
    @NotNull(message = "ID квеста обязателен")
    private Long questId;

    /**
     * Порядковый номер уровня в квесте
     */
    @NotNull(message = "Порядковый номер уровня обязателен")
    @Min(value = 1, message = "Порядковый номер должен быть положительным")
    private Integer orderIndex;

    /**
     * Название уровня
     */
    @NotBlank(message = "Название уровня не может быть пустым")
    @Size(min = 3, max = 200, message = "Название уровня должно содержать от 3 до 200 символов")
    private String title;

    /**
     * HTML описание уровня
     */
    @Size(max = 5000, message = "Описание уровня не должно превышать 5000 символов")
    private String descriptionHtml;

    /**
     * Максимальное время на прохождение уровня в секундах
     */
    @Min(value = 0, message = "Время на прохождение не может быть отрицательным")
    private Integer apTime;

    /**
     * Количество секторов, которые нужно закрыть для прохождения уровня
     */
    @NotNull(message = "Количество требуемых секторов обязательно")
    @Min(value = 0, message = "Количество требуемых секторов не может быть отрицательным")
    private Integer requiredSectors;

    /**
     * Координаты уровня (для геолокационных квестов)
     */
    private Double latitude;

    private Double longitude;

    /**
     * Дополнительные параметры уровня в формате JSON
     */
    private String additionalParams;

    /**
     * Активен ли уровень
     */
    private Boolean active;

    /**
     * ID пользователя, создавшего уровень
     */
    private Long createdBy;

    /**
     * ID пользователя, обновившего уровень
     */
    private Long updatedBy;

    /**
     * Версия уровня
     */
    private Long version;

    /**
     * Количество кодов в уровне
     */
    private Integer codesCount;

    /**
     * Количество подсказок в уровне
     */
    private Integer hintsCount;

    /**
     * Количество медиа файлов в уровне
     */
    private Integer mediaCount;

    /**
     * Список кодов уровня
     */
    private List<CodeDTO> codes;

    /**
     * Список подсказок уровня
     */
    private List<LevelHintDTO> hints;

    /**
     * Список медиа файлов уровня
     */
    private List<QuestMediaDTO> media;

    /**
     * Проверяет, является ли уровень геолокационным
     */
    public boolean isGeolocationLevel() {
        return latitude != null && longitude != null;
    }

    /**
     * Проверяет, ограничено ли время на прохождение уровня
     */
    public boolean hasTimeLimit() {
        return apTime != null && apTime > 0;
    }

    /**
     * Проверяет, требуются ли секторы для прохождения уровня
     */
    public boolean requiresSectors() {
        return requiredSectors != null && requiredSectors > 0;
    }

    /**
     * Проверяет, активен ли уровень
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Проверяет, есть ли у уровня дополнительные параметры
     */
    public boolean hasAdditionalParams() {
        return additionalParams != null && !additionalParams.trim().isEmpty();
    }

    /**
     * Проверяет, есть ли у уровня коды
     */
    public boolean hasCodes() {
        return (codesCount != null && codesCount > 0) ||
               (codes != null && !codes.isEmpty());
    }

    /**
     * Проверяет, есть ли у уровня подсказки
     */
    public boolean hasHints() {
        return (hintsCount != null && hintsCount > 0) ||
               (hints != null && !hints.isEmpty());
    }

    /**
     * Проверяет, есть ли у уровня медиа файлы
     */
    public boolean hasMedia() {
        return (mediaCount != null && mediaCount > 0) ||
               (media != null && !media.isEmpty());
    }

    /**
     * Получает форматированное время на прохождение
     */
    public String getFormattedApTime() {
        if (apTime == null || apTime <= 0) {
            return "Без ограничений";
        }
        
        int hours = apTime / 3600;
        int minutes = (apTime % 3600) / 60;
        int seconds = apTime % 60;
        
        if (hours > 0) {
            return String.format("%dч %dм %dс", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dм %dс", minutes, seconds);
        } else {
            return String.format("%dс", seconds);
        }
    }

    /**
     * Получает координаты в формате строки
     */
    public String getCoordinatesString() {
        if (!isGeolocationLevel()) {
            return null;
        }
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    /**
     * Валидирует DTO перед сохранением
     */
    public boolean isValid() {
        // Проверка обязательных полей
        if (questId == null || questId <= 0) {
            return false;
        }
        
        if (orderIndex == null || orderIndex <= 0) {
            return false;
        }
        
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        
        if (requiredSectors == null || requiredSectors < 0) {
            return false;
        }
        
        // Проверка длины полей
        if (title.length() > 200) {
            return false;
        }
        
        if (descriptionHtml != null && descriptionHtml.length() > 5000) {
            return false;
        }

        // Проверка геолокационных данных
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            return false;
        }
        
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            return false;
        }

        // Проверка времени
        return apTime == null || apTime >= 0;
    }

    /**
     * Подготавливает DTO к сохранению (очистка данных)
     */
    public void prepareForSave() {
        if (title != null) {
            title = title.trim();
        }
        
        if (descriptionHtml != null) {
            descriptionHtml = descriptionHtml.trim();
        }

        if (additionalParams != null) {
            additionalParams = additionalParams.trim();
        }
    }
}