package dn.quest.usermanagement.dto;

import dn.quest.shared.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса поиска пользователей
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на поиск пользователей")
public class UserSearchRequestDTO {
    
    @Schema(description = "Имя пользователя для поиска", example = "player")
    private String username;
    
    @Schema(description = "Публичное имя для поиска", example = "Player")
    private String publicName;
    
    @Schema(description = "Email для поиска", example = "player@example.com")
    private String email;
    
    @Schema(description = "Роль пользователя", example = "PLAYER")
    private UserRole role;
    
    @Schema(description = "Флаг активности", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Флаг блокировки", example = "false")
    private Boolean isBlocked;
    
    @Schema(description = "Номер страницы", example = "0", defaultValue = "0")
    @Min(value = 0, message = "Номер страницы не может быть отрицательным")
    private Integer page = 0;
    
    @Schema(description = "Размер страницы", example = "20", defaultValue = "20")
    @Min(value = 1, message = "Размер страницы должен быть положительным")
    private Integer size = 20;
    
    @Schema(description = "Поле сортировки", example = "createdAt", defaultValue = "createdAt")
    private String sortBy = "createdAt";
    
    @Schema(description = "Направление сортировки", example = "desc", defaultValue = "desc", allowableValues = {"asc", "desc"})
    private String sortDirection = "desc";
}