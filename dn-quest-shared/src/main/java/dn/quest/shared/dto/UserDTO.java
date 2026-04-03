package dn.quest.shared.dto;

import dn.quest.shared.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO для представления пользователя
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends BaseDTO {

    @Schema(description = "ID пользователя", example = "1")
    private UUID id;

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

    @Schema(description = "Дата регистрации", example = "2024-01-01T12:00:00Z")
    private Instant createdAt;

    @Schema(description = "Дата последнего входа", example = "2024-01-01T12:30:00Z")
    private Instant lastLoginAt;

    /**
     * URL аватара
     */
    private String avatarUrl;
    
    /**
     * Описание пользователя
     */
    private String bio;

    /**
     * Активен ли пользователь
     */
    private Boolean active;

    @Schema(description = "Флаг верификации email", example = "false")
    private Boolean isEmailVerified;
    
    /**
     * Дата верификации email
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime emailVerifiedAt;

    @Schema(description = "Список разрешений пользователя")
    private List<String> permissions;
}