package dn.quest.questmanagement.dto;

import dn.quest.questmanagement.entity.QuestStatus;
import dn.quest.shared.enums.Difficulty;
import dn.quest.shared.enums.QuestType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

/**
 * DTO для создания и обновления квеста
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestCreateUpdateDTO {

    /**
     * Название квеста
     */
    @NotBlank(message = "Название квеста не может быть пустым")
    @Size(min = 3, max = 300, message = "Название квеста должно содержать от 3 до 300 символов")
    private String title;

    /**
     * HTML описание квеста
     */
    @Size(max = 10000, message = "Описание квеста не должно превышать 10000 символов")
    private String descriptionHtml;

    /**
     * Сложность квеста
     */
    @NotNull(message = "Сложность квеста обязательна")
    private Difficulty difficulty;

    /**
     * Тип квеста
     */
    @NotNull(message = "Тип квеста обязателен")
    private QuestType questType;

    /**
     * ID авторов квеста
     */
    private Set<Long> authorIds;

    /**
     * Дата начала квеста
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant startAt;

    /**
     * Дата окончания квеста
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant endAt;

    /**
     * Опубликован ли квест (только для обновления)
     */
    private Boolean published;

    /**
     * Статус квеста
     */
    private QuestStatus status;

    /**
     * Теги квеста
     */
    private Set<String> tags;

    /**
     * Категория квеста
     */
    @Size(max = 100, message = "Категория не должна превышать 100 символов")
    private String category;

    /**
     * Максимальное количество участников
     */
    @Min(value = 1, message = "Максимальное количество участников должно быть положительным")
    private Integer maxParticipants;

    /**
     * Минимальное количество участников
     */
    @Min(value = 1, message = "Минимальное количество участников должно быть положительным")
    private Integer minParticipants;

    /**
     * Оценочное время прохождения в минутах
     */
    @Min(value = 1, message = "Оценочное время прохождения должно быть положительным")
    private Integer estimatedDurationMinutes;

    /**
     * URL изображения квеста
     */
    @Size(max = 500, message = "URL изображения не должен превышать 500 символов")
    private String imageUrl;

    /**
     * ID родительского квеста (для копий)
     */
    private Long parentQuestId;

    /**
     * Является ли квест шаблоном
     */
    private Boolean isTemplate;

    /**
     * Причина архивации (только для архивации)
     */
    @Size(max = 500, message = "Причина архивации не должна превышать 500 символов")
    private String archiveReason;

    /**
     * Проверяет, является ли квест командным
     */
    public boolean isTeamQuest() {
        return questType != null && questType.isTeam();
    }

    /**
     * Проверяет, является ли квест соло
     */
    public boolean isSoloQuest() {
        return questType != null && questType.isSolo();
    }

    /**
     * Проверяет, есть ли у квеста ограничения по участникам
     */
    public boolean hasParticipantLimits() {
        return (minParticipants != null && minParticipants > 0) || 
               (maxParticipants != null && maxParticipants > 0);
    }

    /**
     * Проверяет, корректны ли ограничения по участникам
     */
    public boolean hasValidParticipantLimits() {
        if (minParticipants != null && maxParticipants != null) {
            return minParticipants <= maxParticipants;
        }
        return true;
    }

    /**
     * Проверяет, есть ли у квеста временные ограничения
     */
    public boolean hasTimeRestrictions() {
        return startAt != null || endAt != null;
    }

    /**
     * Проверяет, корректны ли временные ограничения
     */
    public boolean hasValidTimeRestrictions() {
        if (startAt != null && endAt != null) {
            return startAt.isBefore(endAt);
        }
        return true;
    }

    /**
     * Проверяет, есть ли у квеста теги
     */
    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    /**
     * Проверяет, есть ли у квеста изображение
     */
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    /**
     * Проверяет, есть ли у квеста оценочное время прохождения
     */
    public boolean hasEstimatedDuration() {
        return estimatedDurationMinutes != null && estimatedDurationMinutes > 0;
    }

    /**
     * Проверяет, является ли квест шаблоном
     */
    public boolean isTemplate() {
        return isTemplate != null && isTemplate;
    }

    /**
     * Проверяет, опубликован ли квест
     */
    public boolean isPublished() {
        return published != null && published;
    }

    /**
     * Проверяет, есть ли у квеста авторы
     */
    public boolean hasAuthors() {
        return authorIds != null && !authorIds.isEmpty();
    }

    /**
     * Проверяет, есть ли у квеста категория
     */
    public boolean hasCategory() {
        return category != null && !category.trim().isEmpty();
    }

    /**
     * Проверяет, есть ли причина архивации
     */
    public boolean hasArchiveReason() {
        return archiveReason != null && !archiveReason.trim().isEmpty();
    }

    /**
     * Валидирует DTO перед сохранением
     */
    public boolean isValid() {
        // Проверка обязательных полей
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        
        if (difficulty == null) {
            return false;
        }
        
        if (questType == null) {
            return false;
        }
        
        // Проверка ограничений по участникам
        if (!hasValidParticipantLimits()) {
            return false;
        }
        
        // Проверка временных ограничений
        if (!hasValidTimeRestrictions()) {
            return false;
        }
        
        // Проверка длины полей
        if (title.length() > 300) {
            return false;
        }
        
        if (descriptionHtml != null && descriptionHtml.length() > 10000) {
            return false;
        }
        
        if (category != null && category.length() > 100) {
            return false;
        }
        
        if (imageUrl != null && imageUrl.length() > 500) {
            return false;
        }

        return archiveReason == null || archiveReason.length() <= 500;
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
        
        if (category != null) {
            category = category.trim();
        }
        
        if (imageUrl != null) {
            imageUrl = imageUrl.trim();
        }
        
        if (archiveReason != null) {
            archiveReason = archiveReason.trim();
        }
        
        // Очистка тегов
        if (tags != null) {
            tags = tags.stream()
                    .map(tag -> tag != null ? tag.trim().toLowerCase() : null)
                    .filter(tag -> tag != null && !tag.isEmpty())
                    .collect(java.util.stream.Collectors.toSet());
        }
        
        // Очистка ID авторов
        if (authorIds != null) {
            authorIds = authorIds.stream()
                    .filter(authorId -> authorId != null && authorId > 0)
                    .collect(java.util.stream.Collectors.toSet());
        }
    }
}