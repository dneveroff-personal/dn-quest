package dn.quest.teammanagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO для запроса валидации токена
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenRequest {
    
    /**
     * JWT токен для валидации
     */
    @NotBlank(message = "Token cannot be blank")
    private String token;
    
    /**
     * ID пользователя (опционально)
     */
    private Long userId;
    
    /**
     * Имя пользователя (опционально)
     */
    private String username;
    
    /**
     * Требуемая роль (опционально)
     */
    private String requiredRole;
    
    /**
     * Требуемое разрешение (опционально)
     */
    private String requiredPermission;
}