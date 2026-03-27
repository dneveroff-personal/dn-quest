package dn.quest.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO для запроса блокировки пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на блокировку пользователя")
public class BlockUserRequestDTO {
    
    @Schema(description = "Причина блокировки", example = "Нарушение правил сообщества", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Причина блокировки обязательна")
    @Size(max = 500, message = "Причина блокировки не должна превышать 500 символов")
    private String reason;
    
    @Schema(description = "Время окончания блокировки", example = "2024-12-31T23:59:59Z")
    private Instant blockedUntil;
    
    @Schema(description = "Постоянная блокировка", example = "false")
    @NotNull(message = "Необходимо указать тип блокировки")
    private Boolean permanent;
    
    /**
     * Получает время окончания блокировки
     */
    public Instant getBlockedUntil() {
        if (Boolean.TRUE.equals(permanent)) {
            return null;
        }
        return blockedUntil;
    }
}