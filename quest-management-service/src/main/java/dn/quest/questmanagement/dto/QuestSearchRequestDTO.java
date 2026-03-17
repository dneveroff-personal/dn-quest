package dn.quest.questmanagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для запроса поиска квестов
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestSearchRequestDTO {

    /**
     * Текстовый поиск по названию
     */
    private String title;

    /**
     * Текстовый поиск по описанию
     */
    private String description;

    /**
     * Фильтр по статусу квеста
     */
    private String status;

    /**
     * Фильтр по сложности
     */
    private String difficulty;

    /**
     * Фильтр по типу квеста
     */
    private String questType;

    /**
     * Фильтр по категории
     */
    private String category;

    /**
     * Фильтр по ID автора
     */
    private Long authorId;

    /**
     * Фильтр по тегам
     */
    private List<String> tags;

    /**
     * Фильтр по публичности
     */
    private Boolean isPublic;

    /**
     * Фильтр по шаблонам
     */
    private Boolean isTemplate;

    /**
     * Фильтр по дате создания (от)
     */
    private LocalDateTime createdFrom;

    /**
     * Фильтр по дате создания (до)
     */
    private LocalDateTime createdTo;

    /**
     * Фильтр по дате публикации (от)
     */
    private LocalDateTime publishedFrom;

    /**
     * Фильтр по дате публикации (до)
     */
    private LocalDateTime publishedTo;

    /**
     * Фильтр по времени начала (от)
     */
    private LocalDateTime startTimeFrom;

    /**
     * Фильтр по времени начала (до)
     */
    private LocalDateTime startTimeTo;

    /**
     * Фильтр по минимальному количеству участников
     */
    @Min(value = 1, message = "Минимальное количество участников должно быть не менее 1")
    private Integer minParticipants;

    /**
     * Фильтр по максимальному количеству участников
     */
    @Min(value = 1, message = "Максимальное количество участников должно быть не менее 1")
    private Integer maxParticipants;

    /**
     * Фильтр по минимальной длительности (в минутах)
     */
    @Min(value = 1, message = "Минимальная длительность должна быть не менее 1 минуты")
    private Integer minDuration;

    /**
     * Фильтр по максимальной длительности (в минутах)
     */
    @Min(value = 1, message = "Максимальная длительность должна быть не менее 1 минуты")
    private Integer maxDuration;

    /**
     * Номер страницы (начиная с 0)
     */
    @Min(value = 0, message = "Номер страницы должен быть не менее 0")
    private int page = 0;

    /**
     * Размер страницы
     */
    @Min(value = 1, message = "Размер страницы должен быть не менее 1")
    @Max(value = 100, message = "Размер страницы не должен превышать 100")
    private int size = 20;

    /**
     * Поле для сортировки
     */
    private String sortBy = "createdAt";

    /**
     * Направление сортировки (asc/desc)
     */
    private String sortDirection = "desc";

    /**
     * Геолокация - широта для поиска квестов рядом
     */
    private Double latitude;

    /**
     * Геолокация - долгота для поиска квестов рядом
     */
    private Double longitude;

    /**
     * Радиус поиска в километрах
     */
    @Min(value = 0.1, message = "Радиус поиска должен быть не менее 0.1 км")
    @Max(value = 1000, message = "Радиус поиска не должен превышать 1000 км")
    private Double radiusKm;

    /**
     * ID пользователя для получения рекомендаций
     */
    private Long userId;

    /**
     * Тип поиска
     */
    private SearchType searchType = SearchType.ADVANCED;

    /**
     * Типы поиска
     */
    public enum SearchType {
        ADVANCED,    // Расширенный поиск с фильтрами
        FULL_TEXT,   // Полнотекстовый поиск
        LOCATION,    // Поиск по геолокации
        SIMILAR,     // Поиск похожих квестов
        POPULAR,     // Поиск популярных квестов
        RECOMMENDED  // Рекомендации для пользователя
    }
}