package dn.quest.authentication.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * События связанные с пользователями
 */
public class UserEvent {

    /**
     * Данные события регистрации пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRegisteredData {
        private Long userId;
        private String username;
        private String email;
        private String publicName;
        private String role;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant registeredAt;
    }

    /**
     * Данные события обновления пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserUpdatedData {
        private Long userId;
        private String username;
        private String email;
        private String publicName;
        private String role;
        private Boolean isActive;
        private Boolean isEmailVerified;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant updatedAt;
    }

    /**
     * Данные события удаления пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDeletedData {
        private Long userId;
        private String username;
        private String email;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant deletedAt;
    }

    /**
     * Данные события смены пароля
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPasswordChangedData {
        private Long userId;
        private String username;
        private String email;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant changedAt;
    }

    /**
     * Данные события изменения роли
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoleChangedData {
        private Long userId;
        private String username;
        private String email;
        private String oldRole;
        private String newRole;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant changedAt;
    }

    /**
     * Данные события входа пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserLoggedInData {
        private Long userId;
        private String username;
        private String email;
        private String clientIp;
        private String userAgent;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant loggedInAt;
    }

    /**
     * Данные события выхода пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserLoggedOutData {
        private Long userId;
        private String username;
        private String email;
        private String clientIp;
        private String userAgent;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant loggedOutAt;
    }

    /**
     * Данные события обновления разрешений пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPermissionsUpdatedData {
        private Long userId;
        private String username;
        private String email;
        private List<String> addedPermissions;
        private List<String> removedPermissions;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant updatedAt;
    }
}