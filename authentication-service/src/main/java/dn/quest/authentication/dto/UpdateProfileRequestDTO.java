package dn.quest.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса обновления профиля пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление профиля пользователя")
public class UpdateProfileRequestDTO {

    @Size(max = 100, message = "Публичное имя не должно превышать 100 символов")
    @Schema(description = "Публичное имя", example = "Player One")
    private String publicName;

    @Email(message = "Неверный формат email")
    @Size(max = 255, message = "Email не должен превышать 255 символов")
    @Schema(description = "Email пользователя", example = "player@example.com")
    private String email;
}