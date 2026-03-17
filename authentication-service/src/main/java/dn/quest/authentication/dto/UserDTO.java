package dn.quest.authentication.dto;

import dn.quest.shared.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO для информации о пользователе
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о пользователе")
public class UserDTO {
    
    @Schema(description = "ID пользователя", example = "1")
    private Long id;
    
    @Schema(description = "Имя пользователя", example = "player123")
    private String username;
    
    @Schema(description = "Email пользователя", example = "player@example.com")
    private String email;
    
    @Schema(description = "Публичное имя", example = "Player One")
    private String publicName;
    
    @Schema(description = "Роль пользователя", example = "PLAYER")
    private UserRole role;
    
    @Schema(description = "Флаг активности", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Флаг верификации email", example = "false")
    private Boolean isEmailVerified;
    
    @Schema(description = "Дата регистрации", example = "2024-01-01T12:00:00Z")
    private Instant createdAt;
    
    @Schema(description = "Дата последнего входа", example = "2024-01-01T12:30:00Z")
    private Instant lastLoginAt;
    
    @Schema(description = "Список разрешений пользователя")
    private List<String> permissions;
}