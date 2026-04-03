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
 * Сущность для хранения статистики квестов
 */
@Entity
@Table(name = "quest_statistics", indexes = {
    @Index(name = "idx_quest_statistics_quest_id", columnList = "questId"),
    @Index(name = "idx_quest_statistics_date", columnList = "date"),
    @Index(name = "idx_quest_statistics_author_id", columnList = "authorId"),
    @Index(name = "idx_quest_statistics_quest_date", columnList = "questId, date"),
    @Index(name = "idx_quest_statistics_author_date", columnList = "authorId, date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID квеста
     */
    @Column(name = "quest_id", nullable = false)
    private Long questId;

    /**
     * Название квеста
     */
    @Column(name = "quest_title")
    private String questTitle;

    /**
     * ID автора квеста
     */
    @Column(name = "author_id")
    private UUID authorId;

    /**
     * Дата статистики
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * Количество созданий квеста (для агрегированных записей)
     */
    @Column(name = "creations")
    private Integer creations;

    /**
     * Количество обновлений квеста
     */
    @Column(name = "updates")
    private Integer updates;

    /**
     * Количество публикаций квеста
     */
    @Column(name = "publications")
    private Integer publications;

    /**
     * Количество удалений квеста
     */
    @Column(name = "deletions")
    private Integer deletions;

    /**
     * Количество просмотров квеста
     */
    @Column(name = "views")
    private Integer views;

    /**
     * Количество уникальных просмотров
     */
    @Column(name = "unique_views")
    private Integer uniqueViews;

    /**
     * Количество стартов квеста
     */
    @Column(name = "starts")
    private Integer starts;

    /**
     * Количество завершений квеста
     */
    @Column(name = "completions")
    private Integer completions;

    /**
     * Количество уникальных участников
     */
    @Column(name = "unique_participants")
    private Integer uniqueParticipants;

    /**
     * Среднее время прохождения (в минутах)
     */
    @Column(name = "avg_completion_time_minutes")
    private Double avgCompletionTimeMinutes;

    /**
     * Коэффициент завершения (completions / starts)
     */
    @Column(name = "completion_rate")
    private Double completionRate;

    /**
     * Текущий рейтинг квеста
     */
    @Column(name = "current_rating")
    private Double currentRating;

    /**
     * Количество оценок
     */
    @Column(name = "rating_count")
    private Integer ratingCount;

    /**
     * Средняя оценка
     */
    @Column(name = "avg_rating")
    private Double avgRating;

    /**
     * Количество комментариев
     */
    @Column(name = "comments_count")
    private Integer commentsCount;

    /**
     * Количество лайков
     */
    @Column(name = "likes_count")
    private Integer likesCount;

    /**
     * Количество добавлений в избранное
     */
    @Column(name = "favorites_count")
    private Integer favoritesCount;

    /**
     * Количество репостов
     */
    @Column(name = "shares_count")
    private Integer sharesCount;

    /**
     * Сложность квеста (1-10)
     */
    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    /**
     * Категория квеста
     */
    @Column(name = "category")
    private String category;

    /**
     * Теги квеста (JSON массив)
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    /**
     * Статус квеста
     */
    @Column(name = "status")
    private String status;

    /**
     * Максимальное количество участников
     */
    @Column(name = "max_participants")
    private Integer maxParticipants;

    /**
     * Текущее количество участников
     */
    @Column(name = "current_participants")
    private Integer currentParticipants;

    /**
     * Количество уровней в квесте
     */
    @Column(name = "levels_count")
    private Integer levelsCount;

    /**
     * Общее время игры в квесте (в минутах)
     */
    @Column(name = "total_game_time_minutes")
    private Long totalGameTimeMinutes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}