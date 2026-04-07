package dn.quest.shared.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для запроса регистрации пользователя
 */
@Data
public class RegisterDTO {
    
    /**
     * Имя пользователя (уникальное)
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,64}$", message = "Username can only contain letters, numbers and underscores")
    private String username;
    
    /**
     * Email пользователя
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;
    
    /**
     * Пароль
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", 
             message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, one special character and no spaces")
    private String password;
    
    /**
     * Подтверждение пароля
     */
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
    
    /**
     * Публичное имя (отображаемое)
     */
    @Size(max = 128, message = "Public name must be less than 128 characters")
    private String publicName;
    
    /**
     * Согласие на обработку персональных данных
     */
    private Boolean agreeToTerms = false;
}