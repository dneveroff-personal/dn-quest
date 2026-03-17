package dn.quest.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса входа в систему
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на вход в систему")
public class LoginRequestDTO {
    
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(max = 50, message = "Имя пользователя не должно превышать 50 символов")
    @Schema(description = "Имя пользователя", example = "player123", required = true)
    private String username;
    
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(max = 100, message = "Пароль не должен превышать 100 символов")
    @Schema(description = "Пароль", example = "password123", required = true)
    private String password;
}