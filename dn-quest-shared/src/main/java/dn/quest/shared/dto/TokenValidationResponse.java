package dn.quest.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для ответа валидации токена
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {

    @Schema(description = "Флаг валидности токена", example = "true")
    private Boolean valid;

    @Schema(description = "Имя пользователя", example = "player123")
    private String username;

    @Schema(description = "ID пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "Роль пользователя", example = "PLAYER")
    private String role;

    @Schema(description = "Сообщение об ошибке", example = "Token expired")
    private String error;

    @Schema(description = "Время до истечения токена в секундах", example = "3600")
    private Long expiresIn;
}