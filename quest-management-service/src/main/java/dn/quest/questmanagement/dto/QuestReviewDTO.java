package dn.quest.questmanagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для отзыва на квест
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestReviewDTO {

    private UUID id;

    /**
     * ID квеста
     */
    private UUID questId;

    /**
     * ID пользователя
     */
    private UUID userId;

    /**
     * Заголовок отзыва
     */
    @Size(max = 200, message = "Заголовок отзыва не должен превышать 200 символов")
    private String title;

    /**
     * Содержание отзыва
     */
    @Size(max = 5000, message = "Содержание отзыва не должно превышать 5000 символов")
    private String content;

    /**
     * Рейтинг от 1 до 5
     */
    @Min(value = 1, message = "Рейтинг должен быть не менее 1")
    @Max(value = 5, message = "Рейтинг не должен превышать 5")
    private Integer rating;

    /**
     * Видимость отзыва
     */
    private Boolean isVisible;

    /**
     * Дата создания отзыва
     */
    private LocalDateTime createdAt;

    /**
     * Дата обновления отзыва
     */
    private LocalDateTime updatedAt;
}