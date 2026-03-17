package dn.quest.questmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Сущность для хранения отзывов на квесты
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quest_reviews", indexes = {
    @Index(name = "idx_quest_review_quest_id", columnList = "questId"),
    @Index(name = "idx_quest_review_user_id", columnList = "userId"),
    @Index(name = "idx_quest_review_visible", columnList = "isVisible"),
    @Index(name = "idx_quest_review_composite", columnList = "questId, userId"),
    @Index(name = "idx_quest_review_created", columnList = "createdAt")
})
public class QuestReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID квеста
     */
    @Column(name = "quest_id", nullable = false)
    private Long questId;

    /**
     * ID пользователя, оставившего отзыв
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Заголовок отзыва
     */
    @Column(name = "title", length = 200)
    private String title;

    /**
     * Содержание отзыва
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * Рейтинг от 1 до 5 (может быть null, если это просто отзыв без рейтинга)
     */
    @Column(name = "rating")
    private Integer rating;

    /**
     * Видимость отзыва (для модерации)
     */
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

    /**
     * Дата создания отзыва
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Дата обновления отзыва
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}