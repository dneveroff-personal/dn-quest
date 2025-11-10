package dn.quest.questmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Сущность для хранения рейтингов квестов
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quest_ratings", indexes = {
    @Index(name = "idx_quest_rating_quest_id", columnList = "questId"),
    @Index(name = "idx_quest_rating_user_id", columnList = "userId"),
    @Index(name = "idx_quest_rating_composite", columnList = "questId, userId")
})
public class QuestRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID квеста
     */
    @Column(name = "quest_id", nullable = false)
    private Long questId;

    /**
     * ID пользователя, поставившего рейтинг
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Рейтинг от 1 до 5
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /**
     * Дата создания рейтинга
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Дата обновления рейтинга
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