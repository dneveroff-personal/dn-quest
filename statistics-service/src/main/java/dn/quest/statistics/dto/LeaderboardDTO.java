package dn.quest.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO для лидербордов
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardDTO {

    /**
     * ID записи
     */
    private Long id;

    /**
     * Тип лидерборда (users, quests, teams)
     */
    private String leaderboardType;

    /**
     * Период лидерборда (daily, weekly, monthly, all_time)
     */
    private String period;

    /**
     * Дата лидерборда
     */
    private LocalDate date;

    /**
     * ID сущности (пользователя, квеста, команды)
     */
    private Long entityId;

    /**
     * Название сущности
     */
    private String entityName;

    /**
     * Ранг в лидерборде
     */
    private Integer rank;

    /**
     * Предыдущий ранг
     */
    private Integer previousRank;

    /**
     * Изменение ранга
     */
    private Integer rankChange;

    /**
     * Очки
     */
    private Double score;

    /**
     * Предыдущие очки
     */
    private Double previousScore;

    /**
     * Изменение очков
     */
    private Double scoreChange;

    /**
     * Категория лидерборда
     */
    private String category;

    /**
     * Теги
     */
    private String tags;

    /**
     * Дополнительные метрики
     */
    private String metrics;

    /**
     * Аватар сущности
     */
    private String avatarUrl;

    /**
     * URL профиля сущности
     */
    private String profileUrl;

    /**
     * Количество достижений
     */
    private Integer achievementsCount;

    /**
     * Уровень сущности
     */
    private Integer level;

    /**
     * Процент прогресса
     */
    private Double progressPercentage;

    /**
     * Статус сущности
     */
    private String status;

    /**
     * Активен ли в лидерборде
     */
    private Boolean isActive;

    /**
     * Количество participations
     */
    private Integer participationsCount;

    /**
     * Количество побед
     */
    private Integer winsCount;

    /**
     * Процент побед
     */
    private Double winRate;

    /**
     * Среднее время выполнения
     */
    private Double avgCompletionTime;

    /**
     * Количество оценок
     */
    private Integer ratingsCount;

    /**
     * Средняя оценка
     */
    private Double avgRating;

    /**
     * Количество просмотров
     */
    private Integer viewsCount;

    /**
     * Количество лайков
     */
    private Integer likesCount;

    /**
     * Количество комментариев
     */
    private Integer commentsCount;

    /**
     * Дополнительные метаданные
     */
    private String metadata;

    /**
     * Время создания
     */
    private LocalDateTime createdAt;

    /**
     * Время обновления
     */
    private LocalDateTime updatedAt;
}