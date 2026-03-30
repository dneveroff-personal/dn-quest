package dn.quest.teammanagement.client;

import dn.quest.shared.dto.UserDTO;
import dn.quest.teammanagement.dto.UserStatisticsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign клиент для взаимодействия с User Management Service
 */
@FeignClient(name = "user-management-service", url = "${app.services.user-management-service.url}")
public interface UserManagementServiceClient {

    /**
     * Получение информации о пользователе по ID
     */
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long userId);

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
    List<UserDTO> getUsersByIds(@RequestBody List<Long> userIds);

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
    boolean userExists(@PathVariable("id") Long userId);

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
    String getUserRole(@PathVariable("id") Long userId);

    /**
     * Проверка, является ли пользователь администратором
     */
    @GetMapping("/api/users/{id}/is-admin")
    boolean isUserAdmin(@PathVariable("id") Long userId);

    /**
     * Проверка, активен ли пользователь
     */
    @GetMapping("/api/users/{id}/is-active")
    boolean isUserActive(@PathVariable("id") Long userId);

    /**
     * Обновление последней активности пользователя
     */
    @PutMapping("/api/users/{id}/last-activity")
    void updateLastActivity(@PathVariable("id") Long userId);

    /**
     * Обновление информации о пользователе
     */
    @PutMapping("/api/users/{id}")
    UserDTO updateUser(@PathVariable("id") Long userId, @RequestBody UserDTO userDTO);

    /**
     * Обновление профиля пользователя
     */
    @PutMapping("/api/users/{id}/profile")
    UserDTO updateUserProfile(@PathVariable("id") Long userId, @RequestBody UserDTO userDTO);

    /**
     * Деактивация пользователя
     */
    @PutMapping("/api/users/{id}/deactivate")
    void deactivateUser(@PathVariable("id") Long userId);

    /**
     * Активация пользователя
     */
    @PutMapping("/api/users/{id}/activate")
    void activateUser(@PathVariable("id") Long userId);

    /**
     * Изменение роли пользователя
     */
    @PutMapping("/api/users/{id}/role")
    void changeUserRole(
            @PathVariable("id") Long userId,
            @RequestParam("role") String role,
            @RequestParam(value = "reason", required = false) String reason
    );

    /**
     * Получение статистики пользователя
     */
    @GetMapping("/api/users/{id}/statistics")
    UserStatisticsDTO getUserStatistics(@PathVariable("id") Long userId);

    /**
     * Получение команд пользователя
     */
    @GetMapping("/api/users/{id}/teams")
    List<UserTeamDTO> getUserTeams(@PathVariable("id") Long userId);

    /**
     * Получение достижений пользователя
     */
    @GetMapping("/api/users/{id}/achievements")
    List<UserAchievementDTO> getUserAchievements(@PathVariable("id") Long userId);

    /**
     * Получение истории активности пользователя
     */
    @GetMapping("/api/users/{id}/activity")
    List<UserActivityDTO> getUserActivity(
            @PathVariable("id") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    );

    class UserTeamDTO {
        private Long teamId;
        private String teamName;
        private String teamTag;
        private String role;
        private String joinedAt;
        private Boolean isActive;
        
        // Getters and setters
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        
        public String getTeamTag() { return teamTag; }
        public void setTeamTag(String teamTag) { this.teamTag = teamTag; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getJoinedAt() { return joinedAt; }
        public void setJoinedAt(String joinedAt) { this.joinedAt = joinedAt; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }

    class UserAchievementDTO {
        private Long achievementId;
        private String achievementName;
        private String achievementDescription;
        private String achievementType;
        private String unlockedAt;
        private Integer points;
        
        // Getters and setters
        public Long getAchievementId() { return achievementId; }
        public void setAchievementId(Long achievementId) { this.achievementId = achievementId; }
        
        public String getAchievementName() { return achievementName; }
        public void setAchievementName(String achievementName) { this.achievementName = achievementName; }
        
        public String getAchievementDescription() { return achievementDescription; }
        public void setAchievementDescription(String achievementDescription) { this.achievementDescription = achievementDescription; }
        
        public String getAchievementType() { return achievementType; }
        public void setAchievementType(String achievementType) { this.achievementType = achievementType; }
        
        public String getUnlockedAt() { return unlockedAt; }
        public void setUnlockedAt(String unlockedAt) { this.unlockedAt = unlockedAt; }
        
        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }
    }

    class UserActivityDTO {
        private String activityType;
        private String description;
        private String timestamp;
        private String relatedEntity;
        private Long relatedEntityId;
        
        // Getters and setters
        public String getActivityType() { return activityType; }
        public void setActivityType(String activityType) { this.activityType = activityType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public String getRelatedEntity() { return relatedEntity; }
        public void setRelatedEntity(String relatedEntity) { this.relatedEntity = relatedEntity; }
        
        public Long getRelatedEntityId() { return relatedEntityId; }
        public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    }
}