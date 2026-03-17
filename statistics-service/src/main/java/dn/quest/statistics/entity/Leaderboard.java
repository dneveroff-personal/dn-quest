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

/**
 * Сущность для хранения лидербордов
 */
@Entity
@Table(name = "leaderboards", indexes = {
    @Index(name = "idx_leaderboards_type", columnList = "leaderboardType"),
    @Index(name = "idx_leaderboards_period", columnList = "period"),
    @Index(name = "idx_leaderboards_date", columnList = "date"),
    @Index(name = "idx_leaderboards_entity_id", columnList = "entityId"),
    @Index(name = "idx_leaderboards_type_period", columnList = "leaderboardType, period"),
    @Index(name = "idx_leaderboards_rank", columnList = "rank")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Leaderboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Тип лидерборда (users, quests, teams)
     */
    @Column(name = "leaderboard_type", nullable = false)
    private String leaderboardType;

    /**
     * Период лидерборда (daily, weekly, monthly, all_time)
     */
    @Column(name = "period", nullable = false)
    private String period;

    /**
     * Дата лидерборда
     */
    @Column(name = "date")
    private LocalDate date;

    /**
     * ID сущности (пользователя, квеста, команды)
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Название сущности
     */
    @Column(name = "entity_name")
    private String entityName;

    /**
     * Ранг в лидерборде
     */
    @Column(name = "rank", nullable = false)
    private Integer rank;

    /**
     * Предыдущий ранг
     */
    @Column(name = "previous_rank")
    private Integer previousRank;

    /**
     * Изменение ранга
     */
    @Column(name = "rank_change")
    private Integer rankChange;

    /**
     * Очки
     */
    @Column(name = "score")
    private Double score;

    /**
     * Предыдущие очки
     */
    @Column(name = "previous_score")
    private Double previousScore;

    /**
     * Изменение очков
     */
    @Column(name = "score_change")
    private Double scoreChange;

    /**
     * Категория лидерборда (для квестов)
     */
    @Column(name = "category")
    private String category;

    /**
     * Теги (JSON массив)
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    /**
     * Дополнительные метрики (JSON)
     */
    @Column(name = "metrics", columnDefinition = "TEXT")
    private String metrics;

    /**
     * Аватар сущности
     */
    @Column(name = "avatar_url")
    private String avatarUrl;

    /**
     * URL профиля сущности
     */
    @Column(name = "profile_url")
    private String profileUrl;

    /**
     * Количество достижений
     */
    @Column(name = "achievements_count")
    private Integer achievementsCount;

    /**
     * Уровень сущности
     */
    @Column(name = "level")
    private Integer level;

    /**
     * Процент прогресса
     */
    @Column(name = "progress_percentage")
    private Double progressPercentage;

    /**
     * Статус сущности
     */
    @Column(name = "status")
    private String status;

    /**
     * Активен ли в лидерборде
     */
    @Column(name = "is_active")
    private Boolean isActive;

    /**
     * Время последнего обновления
     */
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    /**
     * Количество participations (для пользователей)
     */
    @Column(name = "participations_count")
    private Integer participationsCount;

    /**
     * Количество побед (для пользователей/команд)
     */
    @Column(name = "wins_count")
    private Integer winsCount;

    /**
     * Процент побед
     */
    @Column(name = "win_rate")
    private Double winRate;

    /**
     * Среднее время выполнения (для квестов)
     */
    @Column(name = "avg_completion_time")
    private Double avgCompletionTime;

    /**
     * Количество оценок
     */
    @Column(name = "ratings_count")
    private Integer ratingsCount;

    /**
     * Средняя оценка
     */
    @Column(name = "avg_rating")
    private Double avgRating;

    /**
     * Количество просмотров
     */
    @Column(name = "views_count")
    private Integer viewsCount;

    /**
     * Количество лайков
     */
    @Column(name = "likes_count")
    private Integer likesCount;

    /**
     * Количество комментариев
     */
    @Column(name = "comments_count")
    private Integer commentsCount;

    /**
     * Дополнительные метаданные (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}