package dn.quest.teammanagement.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * События связанные с пользователями
 */
public class UserEvents {

    /**
     * Событие создания пользователя
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserCreatedEvent extends BaseEvent {
        private Long userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private Boolean isActive;
        private String createdAt;
    }

    /**
     * Событие обновления пользователя
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserUpdatedEvent extends BaseEvent {
        private Long userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String previousUsername;
        private String previousEmail;
        private String previousFirstName;
        private String previousLastName;
        private String role;
        private Boolean isActive;
        private String updatedAt;
    }

    /**
     * Событие удаления пользователя
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserDeletedEvent extends BaseEvent {
        private Long userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private Integer teamCount;
        private Integer captainTeamCount;
        private String deletionReason;
        private String deletedAt;
    }

    /**
     * Событие активации пользователя
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserActivatedEvent extends BaseEvent {
        private Long userId;
        private String username;
        private String email;
        private String activatedBy;
        private String activatedByUsername;
        private String activationReason;
        private String activatedAt;
    }

    /**
     * Событие деактивации пользователя
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserDeactivatedEvent extends BaseEvent {
        private Long userId;
        private String username;
        private String email;
        private String deactivatedBy;
        private String deactivatedByUsername;
        private String deactivationReason;
        private String deactivatedAt;
    }

    /**
     * Событие изменения роли пользователя
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserRoleChangedEvent extends BaseEvent {
        private Long userId;
        private String username;
        private String email;
        private String previousRole;
        private String newRole;
        private Long changedBy;
        private String changedByUsername;
        private String changeReason;
        private String changedAt;
    }

    /**
     * Событие входа пользователя в систему
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserLoggedInEvent extends BaseEvent {
        private Long userId;
        private String username;
        private String email;
        private String ipAddress;
        private String userAgent;
        private String loginTime;
        private Boolean successfulLogin;
        private String failureReason;
    }

    /**
     * Событие выхода пользователя из системы
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserLoggedOutEvent extends BaseEvent {
        private Long userId;
        private String username;
        private String email;
        private String logoutTime;
        private String sessionDuration;
    }

    /**
     * Событие изменения пароля пользователя
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserPasswordChangedEvent extends BaseEvent {
        private Long userId;
        private String username;
        private String email;
        private String changedBy;
        private String changedByUsername;
        private String changeReason;
        private String changedAt;
        private Boolean passwordReset;
    }

    /**
     * Событие сброса пароля пользователя
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserPasswordResetRequestedEvent extends BaseEvent {
        private Long userId;
        private String username;
        private String email;
        private String resetToken;
        private String resetTokenExpiresAt;
        private String ipAddress;
        private String userAgent;
        private String requestedAt;
    }

    /**
     * Событие обновления профиля пользователя
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserProfileUpdatedEvent extends BaseEvent {
        private Long userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String avatar;
        private String bio;
        private String previousFirstName;
        private String previousLastName;
        private String previousAvatar;
        private String previousBio;
        private String updatedAt;
    }

    /**
     * Событие статистики пользователя
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UserStatisticsUpdatedEvent extends BaseEvent {
        private Long userId;
        private String username;
        private Integer teamCount;
        private Integer activeTeamCount;
        private Integer captainTeamCount;
        private Integer invitationCount;
        private Integer acceptedInvitationCount;
        private Integer declinedInvitationCount;
        private Double teamParticipationRate;
        private String lastActivityAt;
        private String statisticsPeriod;
    }
}