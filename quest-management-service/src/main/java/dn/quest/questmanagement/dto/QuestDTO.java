package dn.quest.questmanagement.dto;

import dn.quest.questmanagement.entity.QuestStatus;
import dn.quest.shared.dto.BaseDTO;
import dn.quest.shared.dto.UserDTO;
import dn.quest.shared.enums.Difficulty;
import dn.quest.shared.enums.QuestType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO для представления квеста
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestDTO {

    /**
     * Идентификатор сущности
     */
    private UUID id;

    /**
     * Версия для оптимистичной блокировки
     */
    private Long version;

    /**
     * Уникальный номер квеста
     */
    private Long number;

    /**
     * Название квеста
     */
    private String title;

    /**
     * HTML описание квеста
     */
    private String descriptionHtml;

    /**
     * Сложность квеста
     */
    private Difficulty difficulty;

    /**
     * Тип квеста
     */
    private QuestType questType;

    /**
     * Авторы квеста
     */
    private List<UserDTO> authors;

    /**
     * ID авторов квеста (для микросервисной архитектуры)
     */
    private Set<UUID> authorIds;

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
     * Опубликован ли квест
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
    private String category;

    /**
     * Максимальное количество участников
     */
    private Integer maxParticipants;

    /**
     * Минимальное количество участников
     */
    private Integer minParticipants;

    /**
     * Оценочное время прохождения в минутах
     */
    private Integer estimatedDurationMinutes;

    /**
     * URL изображения квеста
     */
    private String imageUrl;

    /**

    /**
     * ID родительского квеста
     */
    private Long parentQuestId;

    /**
     * Является ли квест шаблоном
     */
    private Boolean isTemplate;

    /**
     * Дата создания
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    /**
     * Дата обновления
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;

    /**
     * Дата публикации
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant publishedAt;

    /**
     * ID пользователя, создавшего квест
     */
    private Long createdBy;

    /**
     * ID пользователя, обновившего квест
     */
    private Long updatedBy;

    /**
     * Архивирован ли квест
     */
    private Boolean archived;

    /**
     * Дата архивации
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Instant archivedAt;

    /**
     * Причина архивации
     */
    private String archiveReason;

    /**
     * Количество уровней в квесте
     */
    private Integer levelsCount;

    /**
     * Активен ли квест в текущий момент
     */
    private Boolean active;

    /**
     * Можно ли редактировать квест
     */
    private Boolean editable;

    /**
     * Рейтинг квеста
     */
    private Double rating;

    /**
     * Количество оценок
     */
    private Integer ratingsCount;

    /**
     * Количество завершенных игр
     */
    private Integer completedGamesCount;

    /**
     * Среднее время прохождения в минутах
     */
    private Double averageCompletionTimeMinutes;

    /**
     * Проверяет, является ли квест активным
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Проверяет, можно ли редактировать квест
     */
    public boolean isEditable() {
        return editable != null && editable;
    }

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
     * Проверяет, опубликован ли квест
     */
    public boolean isPublished() {
        return published != null && published;
    }

    /**
     * Проверяет, архивирован ли квест
     */
    public boolean isArchived() {
        return archived != null && archived;
    }

    /**
     * Проверяет, является ли квест шаблоном
     */
    public boolean isTemplate() {
        return isTemplate != null && isTemplate;
    }

    /**
     * Получает отображаемое имя статуса
     */
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }

    /**
     * Получает отображаемое имя сложности
     */
    public String getDifficultyDisplayName() {
        return difficulty != null ? difficulty.getDisplayName() : null;
    }

    /**
     * Получает отображаемое имя типа
     */
    public String getQuestTypeDisplayName() {
        return questType != null ? questType.getDisplayName() : null;
    }

    /**
     * Проверяет, есть ли у квеста авторы
     */
    public boolean hasAuthors() {
        return (authorIds != null && !authorIds.isEmpty()) || 
               (authors != null && !authors.isEmpty());
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
     * Проверяет, есть ли у квеста ограничения по участникам
     */
    public boolean hasParticipantLimits() {
        return (minParticipants != null && minParticipants > 0) || 
               (maxParticipants != null && maxParticipants > 0);
    }

    /**
     * Проверяет, есть ли у квеста оценочное время прохождения
     */
    public boolean hasEstimatedDuration() {
        return estimatedDurationMinutes != null && estimatedDurationMinutes > 0;
    }

    /**
     * Проверяет, есть ли у квеста рейтинг
     */
    public boolean hasRating() {
        return rating != null && rating > 0;
    }

    /**
     * Проверяет, есть ли у квеста статистика по играм
     */
    public boolean hasGameStatistics() {
        return (completedGamesCount != null && completedGamesCount > 0) || 
               (averageCompletionTimeMinutes != null && averageCompletionTimeMinutes > 0);
    }
}