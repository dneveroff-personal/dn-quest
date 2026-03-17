package dn.quest.usermanagement.dto;

import dn.quest.shared.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO для профиля пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Профиль пользователя")
public class UserProfileDTO {
    
    @Schema(description = "ID профиля", example = "1")
    private Long id;
    
    @Schema(description = "ID пользователя", example = "1")
    private Long userId;
    
    @Schema(description = "Имя пользователя", example = "player123")
    private String username;
    
    @Schema(description = "Email пользователя", example = "player@example.com")
    private String email;
    
    @Schema(description = "Публичное имя", example = "Player One")
    private String publicName;
    
    @Schema(description = "Роль пользователя", example = "PLAYER")
    private UserRole role;
    
    @Schema(description = "URL аватара", example = "https://example.com/avatars/player123.jpg")
    private String avatarUrl;
    
    @Schema(description = "Биография", example = "Люблю квесты и головоломки")
    private String bio;
    
    @Schema(description = "Местоположение", example = "Москва")
    private String location;
    
    @Schema(description = "Веб-сайт", example = "https://player123.example.com")
    private String website;
    
    @Schema(description = "Флаг активности", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Флаг блокировки", example = "false")
    private Boolean isBlocked;
    
    @Schema(description = "Время до окончания блокировки", example = "2024-12-31T23:59:59Z")
    private Instant blockedUntil;
    
    @Schema(description = "Причина блокировки", example = "Нарушение правил сообщества")
    private String blockReason;
    
    @Schema(description = "Дата создания профиля", example = "2024-01-01T12:00:00Z")
    private Instant createdAt;
    
    @Schema(description = "Дата обновления профиля", example = "2024-01-15T10:30:00Z")
    private Instant updatedAt;
    
    @Schema(description = "Дата последней активности", example = "2024-01-20T15:45:00Z")
    private Instant lastActivityAt;
    
    @Schema(description = "Статистика пользователя")
    private UserStatisticsDTO statistics;
    
    @Schema(description = "Настройки пользователя")
    private UserSettingsDTO settings;
}