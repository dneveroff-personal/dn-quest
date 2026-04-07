package dn.quest.statistics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность для хранения системной статистики
 */
@Entity
@Table(name = "system_statistics", indexes = {
    @Index(name = "idx_system_statistics_date", columnList = "date"),
    @Index(name = "idx_system_statistics_metric", columnList = "metric"),
    @Index(name = "idx_system_statistics_date_metric", columnList = "date, metric")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Дата статистики
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * Метрика статистики
     */
    @Column(name = "metric", nullable = false)
    private String metric;

    /**
     * Значение метрики
     */
    @Column(name = "value")
    private Double value;

    /**
     * Текстовое значение метрики
     */
    @Column(name = "text_value", columnDefinition = "TEXT")
    private String textValue;

    /**
     * Количество
     */
    @Column(name = "count")
    private Long count;

    /**
     * Категория метрики
     */
    @Column(name = "category")
    private String category;

    /**
     * Подкатегория метрики
     */
    @Column(name = "subcategory")
    private String subcategory;

    /**
     * Единица измерения
     */
    @Column(name = "unit")
    private String unit;

    /**
     * Минимальное значение за день
     */
    @Column(name = "min_value")
    private Double minValue;

    /**
     * Максимальное значение за день
     */
    @Column(name = "max_value")
    private Double maxValue;

    /**
     * Среднее значение за день
     */
    @Column(name = "avg_value")
    private Double avgValue;

    /**
     * Суммарное значение за день
     */
    @Column(name = "total_value")
    private Double totalValue;

    /**
     * Предыдущее значение (для сравнения)
     */
    @Column(name = "previous_value")
    private Double previousValue;

    /**
     * Изменение в процентах
     */
    @Column(name = "percentage_change")
    private Double percentageChange;

    /**
     * Дополнительные метаданные (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Статус метрики
     */
    @Column(name = "status")
    private String status;

    /**
     * Является ли метрика активной
     */
    @Column(name = "is_active")
    private Boolean isActive;

    /**
     * Время последнего обновления
     */
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    /**
     * Источник данных
     */
    @Column(name = "data_source")
    private String dataSource;

    /**
     * Частота обновления (minutes, hours, days)
     */
    @Column(name = "update_frequency")
    private String updateFrequency;

    /**
     * Приоритет метрики
     */
    @Column(name = "priority")
    private Integer priority;

    /**
     * Общее количество запросов
     */
    @Column(name = "total_requests")
    private Long totalRequests;

    /**
     * Количество успешных запросов
     */
    @Column(name = "successful_requests")
    private Long successfulRequests;

    /**
     * Количество неудачных запросов
     */
    @Column(name = "failed_requests")
    private Long failedRequests;

    /**
     * Среднее время ответа в миллисекундах
     */
    @Column(name = "average_response_time_ms")
    private Double averageResponseTimeMs;

    /**
     * Использование CPU в процентах
     */
    @Column(name = "cpu_usage_percent")
    private Double cpuUsagePercent;

    /**
     * Использование памяти в процентах
     */
    @Column(name = "memory_usage_percent")
    private Double memoryUsagePercent;

    /**
     * Использование диска в процентах
     */
    @Column(name = "disk_usage_percent")
    private Double diskUsagePercent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}