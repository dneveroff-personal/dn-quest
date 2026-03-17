package dn.quest.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса восстановления пароля
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на восстановление пароля")
public class ForgotPasswordRequestDTO {

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат email")
    @Schema(description = "Email пользователя", example = "player@example.com", required = true)
    private String email;
}