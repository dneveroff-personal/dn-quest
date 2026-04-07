package dn.quest.usermanagement.dto;

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
    
    @Schema(description = "Публичное имя", example = "Player One", maxLength = 128)
    @Size(max = 128, message = "Публичное имя не должно превышать 128 символов")
    private String publicName;
    
    @Schema(description = "Email пользователя", example = "player@example.com")
    @Email(message = "Некорректный формат email")
    private String email;
    
    @Schema(description = "URL аватара", example = "https://example.com/avatars/player123.jpg", maxLength = 512)
    @Size(max = 512, message = "URL аватара не должен превышать 512 символов")
    private String avatarUrl;
    
    @Schema(description = "Биография", example = "Люблю квесты и головоломки", maxLength = 1000)
    @Size(max = 1000, message = "Биография не должна превышать 1000 символов")
    private String bio;
    
    @Schema(description = "Местоположение", example = "Москва", maxLength = 128)
    @Size(max = 128, message = "Местоположение не должно превышать 128 символов")
    private String location;
    
    @Schema(description = "Веб-сайт", example = "https://player123.example.com", maxLength = 255)
    @Size(max = 255, message = "URL веб-сайта не должен превышать 255 символов")
    private String website;
}