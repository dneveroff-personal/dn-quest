package dn.quest.questmanagement.dto;

import dn.quest.questmanagement.entity.LevelHint;
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

/**
 * DTO для представления подсказки уровня
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LevelHintDTO extends BaseDTO {

    /**
     * ID уровня, к которому относится подсказка
     */
    @NotNull(message = "ID уровня обязателен")
    private Long levelId;

    /**
     * Порядковый номер подсказки в уровне
     */
    @NotNull(message = "Порядковый номер подсказки обязателен")
    @Min(value = 1, message = "Порядковый номер должен быть положительным")
    private Integer orderIndex;

    /**
     * Смещение от начала уровня в секундах
     */
    @NotNull(message = "Смещение времени обязательно")
    @Min(value = 0, message = "Смещение времени не может быть отрицательным")
    private Integer offsetSec;

    /**
     * Текст подсказки
     */
    @NotBlank(message = "Текст подсказки не может быть пустым")
    @Size(max = 2000, message = "Текст подсказки не должен превышать 2000 символов")
    private String hintText;

    /**
     * Заголовок подсказки
     */
    @Size(max = 200, message = "Заголовок подсказки не должен превышать 200 символов")
    private String title;

    /**
     * Тип подсказки
     */
    private LevelHint.HintType hintType;

    /**
     * URL файла подсказки
     */
    @Size(max = 500, message = "URL файла не должен превышать 500 символов")
    private String fileUrl;

    /**
     * Тип файла подсказки
     */
    private LevelHint.FileType fileType;

    /**
     * Стоимость подсказки
     */
    @Min(value = 0, message = "Стоимость подсказки не может быть отрицательной")
    private Integer cost;

    /**
     * Активна ли подсказка
     */
    private Boolean active;

    /**
     * Обязательна ли подсказка
     */
    private Boolean mandatory;

    /**
     * Количество использований подсказки
     */
    private Integer usageCount;

    /**

    /**

    /**
     * ID пользователя, создавшего подсказку
     */
    private Long createdBy;

    /**
     * ID пользователя, обновившего подсказку
     */
    private Long updatedBy;

    /**
     * Версия подсказки
     */
    private Long version;

    /**
     * Доступна ли подсказка сейчас (на основе времени)
     */
    private Boolean available;

    /**
     * Проверяет, является ли подсказка текстовой
     */
    public boolean isTextHint() {
        return hintType == null || hintType == LevelHint.HintType.TEXT;
    }

    /**
     * Проверяет, является ли подсказка файловой
     */
    public boolean isFileHint() {
        return hintType != null && hintType == LevelHint.HintType.FILE;
    }

    /**
     * Проверяет, является ли подсказка изображением
     */
    public boolean isImageHint() {
        return isFileHint() && fileType == LevelHint.FileType.IMAGE;
    }

    /**
     * Проверяет, является ли подсказка аудио
     */
    public boolean isAudioHint() {
        return isFileHint() && fileType == LevelHint.FileType.AUDIO;
    }

    /**
     * Проверяет, является ли подсказка видео
     */
    public boolean isVideoHint() {
        return isFileHint() && fileType == LevelHint.FileType.VIDEO;
    }

    /**
     * Проверяет, является ли подсказка документом
     */
    public boolean isDocumentHint() {
        return isFileHint() && fileType == LevelHint.FileType.DOCUMENT;
    }

    /**
     * Проверяет, активна ли подсказка
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Проверяет, обязательна ли подсказка
     */
    public boolean isMandatory() {
        return mandatory != null && mandatory;
    }

    /**
     * Проверяет, бесплатная ли подсказка
     */
    public boolean isFree() {
        return cost == null || cost == 0;
    }

    /**
     * Проверяет, платная ли подсказка
     */
    public boolean isPaid() {
        return !isFree();
    }

    /**
     * Проверяет, использовалась ли подсказка
     */
    public boolean isUsed() {
        return usageCount != null && usageCount > 0;
    }

    /**
     * Проверяет, есть ли у подсказки заголовок
     */
    public boolean hasTitle() {
        return title != null && !title.trim().isEmpty();
    }

    /**
     * Проверяет, есть ли у подсказки файл
     */
    public boolean hasFile() {
        return fileUrl != null && !fileUrl.trim().isEmpty();
    }

    /**
     * Проверяет, доступна ли подсказка по времени
     */
    public boolean isAvailable() {
        if (available != null) {
            return available;
        }

        return offsetSec == null || offsetSec <= 0;// Нужно передавать время начала уровня для проверки
    }

    /**
     * Получает отображаемое имя типа подсказки
     */
    public String getHintTypeDisplayName() {
        if (hintType == null) {
            return "Текст";
        }
        return hintType.getDisplayName();
    }

    /**
     * Получает отображаемое имя типа файла
     */
    public String getFileTypeDisplayName() {
        return fileType != null ? fileType.getDisplayName() : null;
    }

    /**
     * Получает информацию о стоимости
     */
    public String getCostDisplay() {
        if (isFree()) {
            return "Бесплатно";
        }
        return String.format("%d баллов", cost);
    }

    /**
     * Получает информацию о смещении времени
     */
    public String getOffsetDisplay() {
        if (offsetSec == null || offsetSec == 0) {
            return "Сразу";
        }
        
        int minutes = offsetSec / 60;
        int seconds = offsetSec % 60;
        
        if (minutes > 0) {
            return String.format("Через %dм %dс", minutes, seconds);
        } else {
            return String.format("Через %dс", seconds);
        }
    }

    /**
     * Получает информацию об использовании
     */
    public String getUsageInfo() {
        if (!isUsed()) {
            return "Не использовалась";
        }
        return String.format("Использована %d раз", usageCount);
    }

    /**
     * Валидирует DTO перед сохранением
     */
    public boolean isValid() {
        // Проверка обязательных полей
        if (levelId == null || levelId <= 0) {
            return false;
        }
        
        if (orderIndex == null || orderIndex <= 0) {
            return false;
        }
        
        if (offsetSec == null || offsetSec < 0) {
            return false;
        }
        
        if (hintText == null || hintText.trim().isEmpty()) {
            return false;
        }
        
        // Проверка длины полей
        if (hintText.length() > 2000) {
            return false;
        }
        
        if (title != null && title.length() > 200) {
            return false;
        }
        
        if (fileUrl != null && fileUrl.length() > 500) {
            return false;
        }
        
        // Проверка логики полей
        if (isFileHint() && !hasFile()) {
            return false;
        }
        
        if (isTextHint() && hasFile()) {
            return false;
        }
        
        if (isFileHint() && fileType == null) {
            return false;
        }
        
        if (cost != null && cost < 0) {
            return false;
        }

        return usageCount == null || usageCount >= 0;
    }

    /**
     * Подготавливает DTO к сохранению (очистка данных)
     */
    public void prepareForSave() {
        if (hintText != null) {
            hintText = hintText.trim();
        }
        
        if (title != null) {
            title = title.trim();
        }
        
        if (fileUrl != null) {
            fileUrl = fileUrl.trim();
        }
        
        // Установка значений по умолчанию
        if (hintType == null) {
            hintType = LevelHint.HintType.TEXT;
        }
        
        if (active == null) {
            active = true;
        }
        
        if (mandatory == null) {
            mandatory = false;
        }
        
        if (usageCount == null) {
            usageCount = 0;
        }
        
        if (cost == null) {
            cost = 0;
        }
        
        if (offsetSec == null) {
            offsetSec = 0;
        }
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
    public LevelHintDTO copy() {
        return LevelHintDTO.builder()
                .levelId(this.levelId)
                .orderIndex(this.orderIndex)
                .offsetSec(this.offsetSec)
                .hintText(this.hintText)
                .title(this.title)
                .hintType(this.hintType)
                .fileUrl(this.fileUrl)
                .fileType(this.fileType)
                .cost(this.cost)
                .active(this.active)
                .mandatory(this.mandatory)
                .build();
    }

    /**
     * Проверяет, доступна ли подсказка через указанное количество секунд
     */
    public boolean isAvailableAfter(int secondsFromLevelStart) {
        if (offsetSec == null || offsetSec <= 0) {
            return true;
        }
        return secondsFromLevelStart >= offsetSec;
    }
}