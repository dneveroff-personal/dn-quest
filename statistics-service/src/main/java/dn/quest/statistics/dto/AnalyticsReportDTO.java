package dn.quest.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO для аналитических отчетов
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsReportDTO {

    /**
     * ID отчета
     */
    private String reportId;

    /**
     * Название отчета
     */
    private String title;

    /**
     * Описание отчета
     */
    private String description;

    /**
     * Тип отчета
     */
    private String reportType;

    /**
     * Период отчета
     */
    private String period;

    /**
     * Начальная дата периода
     */
    private LocalDate startDate;

    /**
     * Конечная дата периода
     */
    private LocalDate endDate;

    /**
     * Ключевые метрики
     */
    private Map<String, Object> keyMetrics;

    /**
     * Графики и диаграммы
     */
    private List<ChartDTO> charts;

    /**
     * Таблицы с данными
     */
    private List<TableDTO> tables;

    /**
     * Сводная информация
     */
    private SummaryDTO summary;

    /**
     * Рекомендации на основе данных
     */
    private List<String> recommendations;

    /**
     * Сравнение с предыдущим периодом
     */
    private ComparisonDTO comparison;

    /**
     * Прогнозы
     */
    private ForecastDTO forecast;

    /**
     * Дополнительные метаданные
     */
    private Map<String, Object> metadata;

    /**
     * Время генерации отчета
     */
    private LocalDateTime generatedAt;

    /**
     * Время генерации в миллисекундах
     */
    private Long generationTimeMs;

    /**
     * Статус генерации
     */
    private String status;

    /**
     * Сообщение об ошибке (если есть)
     */
    private String errorMessage;

    /**
     * DTO для графиков
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartDTO {
        private String id;
        private String title;
        private String type; // line, bar, pie, area, etc.
        private List<String> labels;
        private List<DataSetDTO> datasets;
        private Map<String, Object> options;
    }

    /**
     * DTO для наборов данных графиков
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSetDTO {
        private String label;
        private List<Number> data;
        private String backgroundColor;
        private String borderColor;
        private List<Number> fill;
        private Map<String, Object> metadata;
    }

    /**
     * DTO для таблиц
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableDTO {
        private String id;
        private String title;
        private List<String> headers;
        private List<List<Object>> rows;
        private Map<String, Object> metadata;
    }

    /**
     * DTO для сводной информации
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDTO {
        private Long totalUsers;
        private Long activeUsers;
        private Long totalQuests;
        private Long completedQuests;
        private Long totalGameTime;
        private Double avgCompletionRate;
        private Long totalSessions;
        private Map<String, Object> additionalMetrics;
    }

    /**
     * DTO для сравнения
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonDTO {
        private LocalDate previousStartDate;
        private LocalDate previousEndDate;
        private Map<String, Object> previousMetrics;
        private Map<String, Double> percentageChanges;
        private Map<String, String> trends; // up, down, stable
    }

    /**
     * DTO для прогнозов
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastDTO {
        private String method;
        private LocalDate forecastStartDate;
        private LocalDate forecastEndDate;
        private Map<String, List<Number>> forecastData;
        private Double confidenceLevel;
        private Map<String, Object> assumptions;
    }
}