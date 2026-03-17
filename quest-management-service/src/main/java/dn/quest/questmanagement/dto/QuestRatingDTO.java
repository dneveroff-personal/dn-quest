package dn.quest.questmanagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO для рейтинга квеста
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestRatingDTO {

    private Long id;

    /**
     * ID квеста
     */
    @NotNull(message = "ID квеста не может быть пустым")
    private Long questId;

    /**
     * ID пользователя
     */
    @NotNull(message = "ID пользователя не может быть пустым")
    private Long userId;

    /**
     * Рейтинг от 1 до 5
     */
    @NotNull(message = "Рейтинг не может быть пустым")
    @Min(value = 1, message = "Рейтинг должен быть не менее 1")
    @Max(value = 5, message = "Рейтинг не должен превышать 5")
    private Integer rating;

    /**
     * Дата создания рейтинга
     */
    private LocalDateTime createdAt;

    /**
     * Дата обновления рейтинга
     */
    private LocalDateTime updatedAt;
}