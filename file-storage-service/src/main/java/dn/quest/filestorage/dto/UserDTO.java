package dn.quest.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO для представления пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /**
     * ID пользователя
     */
    private UUID id;

    /**
     * Имя пользователя
     */
    private String username;

    /**
     * Email пользователя
     */
    private String email;

    /**
     * Полное имя
     */
    private String fullName;

    /**
     * Роли пользователя
     */
    private List<String> roles;

    /**
     * Активен ли пользователь
     */
    private Boolean isActive;

    /**
     * Время создания
     */
    private LocalDateTime createdAt;

    /**
     * Время последнего обновления
     */
    private LocalDateTime updatedAt;

    /**
     * URL аватара
     */
    private String avatarUrl;
}