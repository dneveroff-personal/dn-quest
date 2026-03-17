package dn.quest.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса сброса пароля
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на сброс пароля")
public class ResetPasswordRequestDTO {

    @NotBlank(message = "Токен сброса пароля не может быть пустым")
    @Schema(description = "Токен сброса пароля", example = "reset-token-123", required = true)
    private String token;

    @NotBlank(message = "Новый пароль не может быть пустым")
    @Size(min = 6, max = 100, message = "Новый пароль должен содержать от 6 до 100 символов")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Новый пароль должен содержать как минимум одну строчную букву, одну заглавную букву и одну цифру")
    @Schema(description = "Новый пароль", example = "NewPassword123", required = true)
    private String newPassword;
}