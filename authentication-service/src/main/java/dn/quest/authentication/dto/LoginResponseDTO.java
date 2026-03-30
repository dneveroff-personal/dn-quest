package dn.quest.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import dn.quest.shared.dto.UserDTO;

/**
 * DTO для ответа при успешном входе в систему
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ при успешном входе в систему")
public class LoginResponseDTO {
    
    @Schema(description = "JWT access токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "JWT refresh токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "Тип токена", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";
    
    @Schema(description = "Время истечения access токена в секундах", example = "900")
    private Long expiresIn;
    
    @Schema(description = "Время истечения access токена", example = "2024-01-01T12:15:00Z")
    private Instant expiresAt;
    
    @Schema(description = "Информация о пользователе")
    private UserDTO user;
}