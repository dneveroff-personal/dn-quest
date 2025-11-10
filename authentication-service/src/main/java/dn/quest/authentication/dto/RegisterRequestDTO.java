package dn.quest.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса регистрации пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на регистрацию пользователя")
public class RegisterRequestDTO {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Имя пользователя может содержать только буквы, цифры, подчеркивания и дефисы")
    @Schema(description = "Имя пользователя", example = "player123", required = true)
    private String username;

    @Size(max = 100, message = "Публичное имя не должно превышать 100 символов")
    @Schema(description = "Публичное имя", example = "Player One")
    private String publicName;

    @Email(message = "Неверный формат email")
    @Size(max = 255, message = "Email не должен превышать 255 символов")
    @Schema(description = "Email пользователя", example = "player@example.com")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, max = 100, message = "Пароль должен содержать от 6 до 100 символов")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Пароль должен содержать как минимум одну строчную букву, одну заглавную букву и одну цифру")
    @Schema(description = "Пароль", example = "Password123", required = true)
    private String password;
}