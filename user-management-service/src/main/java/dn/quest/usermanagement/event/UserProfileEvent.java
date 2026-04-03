package dn.quest.usermanagement.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * События связанные с профилями пользователей
 */
public class UserProfileEvent {

    /**
     * Данные события создания профиля пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileCreatedData {
        private UUID userId;
        private String username;
        private String email;
        private String publicName;
        private String role;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant createdAt;
    }

    /**
     * Данные события обновления профиля пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileUpdatedData {
        private UUID userId;
        private String username;
        private String email;
        private String publicName;
        private String avatarUrl;
        private String bio;
        private String location;
        private String website;
        private Boolean isActive;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant updatedAt;
    }

    /**
     * Данные события удаления профиля пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileDeletedData {
        private UUID userId;
        private String username;
        private String email;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant deletedAt;
    }

    /**
     * Данные события блокировки пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBlockedData {
        private UUID userId;
        private String username;
        private String email;
        private String reason;
        private Boolean permanent;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant blockedUntil;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant blockedAt;
    }

    /**
     * Данные события разблокировки пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserUnblockedData {
        private UUID userId;
        private String username;
        private String email;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant unblockedAt;
    }

    /**
     * Данные события изменения роли пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoleChangedData {
        private UUID userId;
        private String username;
        private String email;
        private String oldRole;
        private String newRole;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant changedAt;
    }

    /**
     * Данные события обновления аватара пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAvatarUpdatedData {
        private UUID userId;
        private String username;
        private String oldAvatarUrl;
        private String newAvatarUrl;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant updatedAt;
    }

    /**
     * Данные события активности пользователя
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserActivityData {
        private UUID userId;
        private String username;
        private String activityType;
        private String description;
        private String clientIp;
        private String userAgent;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        private Instant activityAt;
    }
}