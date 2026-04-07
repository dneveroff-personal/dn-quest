package dn.quest.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для статистики пользователей
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDTO {

    /**
     * ID записи статистики
     */
    private UUID id;

    /**
     * ID пользователя
     */
    private UUID userId;

    /**
     * Дата статистики
     */
    private LocalDate date;

    /**
     * Количество регистраций
     */
    private Integer registrations;

    /**
     * Количество логинов
     */
    private Integer logins;

    /**
     * Количество игровых сессий
     */
    private Integer gameSessions;

    /**
     * Количество завершенных квестов
     */
    private Integer completedQuests;

    /**
     * Количество созданных квестов
     */
    private Integer createdQuests;

    /**
     * Количество созданных команд
     */
    private Integer createdTeams;

    /**
     * Количество участий в командах
     */
    private Integer teamMemberships;

    /**
     * Общее время в игре (в минутах)
     */
    private Long totalGameTimeMinutes;

    /**
     * Количество загруженных файлов
     */
    private Integer uploadedFiles;

    /**
     * Общий размер загруженных файлов (в байтах)
     */
    private Long totalFileSizeBytes;

    /**
     * Количество успешных отправок кода
     */
    private Integer successfulCodeSubmissions;

    /**
     * Количество неудачных отправок кода
     */
    private Integer failedCodeSubmissions;

    /**
     * Количество завершенных уровней
     */
    private Integer completedLevels;

    /**
     * Среднее время завершения уровня (в секундах)
     */
    private Double avgLevelCompletionTimeSeconds;

    /**
     * Текущий рейтинг пользователя
     */
    private Double currentRating;

    /**
     * Изменение рейтинга за день
     */
    private Double ratingChange;

    /**
     * Последняя активность
     */
    private LocalDateTime lastActiveAt;

    /**
     * IP адрес последней активности
     */
    private String lastIp;

    /**
     * User Agent последней активности
     */
    private String lastUserAgent;

    /**
     * Время создания записи
     */
    private LocalDateTime createdAt;

    /**
     * Время обновления записи
     */
    private LocalDateTime updatedAt;
}