package dn.quest.shared.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса обновления токена
 */
@Data
public class RefreshTokenRequestDTO {
    
    /**
     * Refresh токен
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}