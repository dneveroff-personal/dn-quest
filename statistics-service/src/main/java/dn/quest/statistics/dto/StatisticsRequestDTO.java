package dn.quest.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO для запросов статистики
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsRequestDTO {

    /**
     * Начальная дата периода
     */
    @NotNull(message = "Начальная дата не может быть пустой")
    private LocalDate startDate;

    /**
     * Конечная дата периода
     */
    @NotNull(message = "Конечная дата не может быть пустой")
    private LocalDate endDate;

    /**
     * ID пользователя (для фильтрации по пользователю)
     */
    private UUID userId;

    /**
     * ID квеста (для фильтрации по квесту)
     */
    private Long questId;

    /**
     * ID команды (для фильтрации по команде)
     */
    private UUID teamId;

    /**
     * Категория (для фильтрации по категории)
     */
    private String category;

    /**
     * Тип статистики (users, quests, teams, games, system)
     */
    private String statisticsType;

    /**
     * Период агрегации (daily, weekly, monthly)
     */
    private String aggregationPeriod;

    /**
     * Метрики для включения в отчет
     */
    private String[] metrics;

    /**
     * Группировка по полям
     */
    private String[] groupBy;

    /**
     * Сортировка по полю
     */
    private String sortBy;

    /**
     * Направление сортировки (asc, desc)
     */
    private String sortDirection;

    /**
     * Номер страницы (для пагинации)
     */
    @Min(value = 0, message = "Номер страницы не может быть отрицательным")
    private Integer page;

    /**
     * Размер страницы (для пагинации)
     */
    @Min(value = 1, message = "Размер страницы должен быть положительным")
    private Integer size;

    /**
     * Включать ли детальную информацию
     */
    private Boolean includeDetails;

    /**
     * Включать ли сравнение с предыдущим периодом
     */
    private Boolean includeComparison;

    /**
     * Формат ответа (json, csv, excel)
     */
    private String format;

    /**
     * Таймзона для расчетов
     */
    private String timezone;
}