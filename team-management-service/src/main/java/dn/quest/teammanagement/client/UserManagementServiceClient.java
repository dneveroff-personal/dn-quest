package dn.quest.teammanagement.client;

import dn.quest.shared.dto.UserDTO;
import dn.quest.teammanagement.dto.UserStatisticsDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Feign клиент для взаимодействия с User Management Service
 */
@FeignClient(name = "user-management-service", url = "${app.services.user-management-service.url}")
public interface UserManagementServiceClient {

    /**
     * Получение информации о пользователе по ID
     */
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") UUID userId);

    /**
     * Получение информации о пользователе по имени пользователя
     */
    @GetMapping("/api/users/username/{username}")
    UserDTO getUserByUsername(@PathVariable("username") String username);

    /**
     * Получение информации о пользователе по email
     */
    @GetMapping("/api/users/email/{email}")
    UserDTO getUserByEmail(@PathVariable("email") String email);

    /**
     * Получение списка пользователей по IDs
     */
    @PostMapping("/api/users/batch")
    List<UserDTO> getUsersByIds(@RequestBody List<UUID> userIds);

    /**
     * Поиск пользователей
     */
    @GetMapping("/api/users/search")
    List<UserDTO> searchUsers(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    );

    /**
     * Получение активных пользователей
     */
    @GetMapping("/api/users/active")
    List<UserDTO> getActiveUsers(
            @RequestParam(value = "limit", defaultValue = "10") int limit
    );

    /**
     * Получение пользователей по роли
     */
    @GetMapping("/api/users/role/{role}")
    List<UserDTO> getUsersByRole(
            @PathVariable("role") String role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    );

    /**
     * Получение новых пользователей
     */
    @GetMapping("/api/users/new")
    List<UserDTO> getNewUsers(
            @RequestParam(value = "limit", defaultValue = "10") int limit
    );

    /**
     * Получение количества пользователей
     */
    @GetMapping("/api/users/count")
    long getTotalUsersCount();

    /**
     * Получение количества активных пользователей
     */
    @GetMapping("/api/users/active/count")
    long getActiveUsersCount();

    /**
     * Проверка существования пользователя по ID
     */
    @GetMapping("/api/users/{id}/exists")
    boolean userExists(@PathVariable("id") UUID userId);

    /**
     * Проверка существования пользователя по имени пользователя
     */
    @GetMapping("/api/users/username/{username}/exists")
    boolean userExistsByUsername(@PathVariable("username") String username);

    /**
     * Проверка существования пользователя по email
     */
    @GetMapping("/api/users/email/{email}/exists")
    boolean userExistsByEmail(@PathVariable("email") String email);

    /**
     * Получение роли пользователя
     */
    @GetMapping("/api/users/{id}/role")
    String getUserRole(@PathVariable("id") UUID userId);

    /**
     * Проверка, является ли пользователь администратором
     */
    @GetMapping("/api/users/{id}/is-admin")
    boolean isUserAdmin(@PathVariable("id") UUID userId);

    /**
     * Проверка, активен ли пользователь
     */
    @GetMapping("/api/users/{id}/is-active")
    boolean isUserActive(@PathVariable("id") UUID userId);

    /**
     * Обновление последней активности пользователя
     */
    @PutMapping("/api/users/{id}/last-activity")
    void updateLastActivity(@PathVariable("id") UUID userId);

    /**
     * Обновление информации о пользователе
     */
    @PutMapping("/api/users/{id}")
    UserDTO updateUser(@PathVariable("id") UUID userId, @RequestBody UserDTO userDTO);

    /**
     * Обновление профиля пользователя
     */
    @PutMapping("/api/users/{id}/profile")
    UserDTO updateUserProfile(@PathVariable("id") UUID userId, @RequestBody UserDTO userDTO);

    /**
     * Деактивация пользователя
     */
    @PutMapping("/api/users/{id}/deactivate")
    void deactivateUser(@PathVariable("id") UUID userId);

    /**
     * Активация пользователя
     */
    @PutMapping("/api/users/{id}/activate")
    void activateUser(@PathVariable("id") UUID userId);

    /**
     * Изменение роли пользователя
     */
    @PutMapping("/api/users/{id}/role")
    void changeUserRole(
            @PathVariable("id") UUID userId,
            @RequestParam("role") String role,
            @RequestParam(value = "reason", required = false) String reason
    );

    /**
     * Получение статистики пользователя
     */
    @GetMapping("/api/users/{id}/statistics")
    UserStatisticsDTO getUserStatistics(@PathVariable("id") UUID userId);

    /**
     * Получение команд пользователя
     */
    @GetMapping("/api/users/{id}/teams")
    List<UserTeamDTO> getUserTeams(@PathVariable("id") UUID userId);

    /**
     * Получение достижений пользователя
     */
    @GetMapping("/api/users/{id}/achievements")
    List<UserAchievementDTO> getUserAchievements(@PathVariable("id") UUID userId);

    /**
     * Получение истории активности пользователя
     */
    @GetMapping("/api/users/{id}/activity")
    List<UserActivityDTO> getUserActivity(
            @PathVariable("id") UUID userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    );

    @Setter
    @Getter
    class UserTeamDTO {
        private UUID teamId;
        private String teamName;
        private String teamTag;
        private String role;
        private String joinedAt;
        private Boolean isActive;
    }

    @Setter
    @Getter
    class UserAchievementDTO {
        private Long achievementId;
        private String achievementName;
        private String achievementDescription;
        private String achievementType;
        private String unlockedAt;
        private Integer points;
    }

    @Setter
    @Getter
    class UserActivityDTO {
        private String activityType;
        private String description;
        private String timestamp;
        private String relatedEntity;
        private Long relatedEntityId;
    }
}