package dn.quest.shared.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для запроса входа в систему
 */
@Data
public class LoginRequestDTO {
    
    /**
     * Имя пользователя или email
     */
    @NotBlank(message = "Username or email is required")
    @Size(min = 3, max = 255, message = "Username or email must be between 3 and 255 characters")
    private String usernameOrEmail;
    
    /**
     * Пароль
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String password;
    
    /**
     * Запомнить меня (для продления сессии)
     */
    private Boolean rememberMe = false;
}