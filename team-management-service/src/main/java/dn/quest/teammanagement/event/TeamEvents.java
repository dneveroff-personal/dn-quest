package dn.quest.teammanagement.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * События связанные с командами
 */
public class TeamEvents {

    /**
     * Событие создания команды
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamCreatedEvent extends BaseEvent {
        private Long teamId;
        private String teamName;
        private String teamDescription;
        private String teamTag;
        private String teamCategory;
        private Long captainId;
        private String captainUsername;
        private Boolean isPrivate;
        private Integer maxMembers;
    }

    /**
     * Событие обновления команды
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamUpdatedEvent extends BaseEvent {
        private Long teamId;
        private String teamName;
        private String teamDescription;
        private String teamTag;
        private String teamCategory;
        private String previousName;
        private String previousDescription;
        private String previousTag;
        private String previousCategory;
    }

    /**
     * Событие удаления команды
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamDeletedEvent extends BaseEvent {
        private Long teamId;
        private String teamName;
        private String teamTag;
        private Long captainId;
        private String captainUsername;
        private Integer memberCount;
        private String deletionReason;
    }

    /**
     * Событие добавления участника в команду
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamMemberAddedEvent extends BaseEvent {
        private Long teamId;
        private String teamName;
        private Long memberId;
        private String memberUsername;
        private String memberRole;
        private Long addedBy;
        private String addedByUsername;
        private String joinMethod; // INVITATION, DIRECT, REQUEST
    }

    /**
     * Событие удаления участника из команды
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamMemberRemovedEvent extends BaseEvent {
        private Long teamId;
        private String teamName;
        private Long memberId;
        private String memberUsername;
        private String memberRole;
        private Long removedBy;
        private String removedByUsername;
        private String removalReason;
    }

    /**
     * Событие изменения роли участника команды
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamMemberRoleChangedEvent extends BaseEvent {
        private Long teamId;
        private String teamName;
        private Long memberId;
        private String memberUsername;
        private String previousRole;
        private String newRole;
        private Long changedBy;
        private String changedByUsername;
    }

    /**
     * Событие передачи капитанства
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamCaptainChangedEvent extends BaseEvent {
        private Long teamId;
        private String teamName;
        private Long previousCaptainId;
        private String previousCaptainUsername;
        private Long newCaptainId;
        private String newCaptainUsername;
        private Long changedBy;
        private String changedByUsername;
        private String transferReason;
    }

    /**
     * Событие обновления настроек команды
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamSettingsUpdatedEvent extends BaseEvent {
        private Long teamId;
        private String teamName;
        private Boolean previousPrivacy;
        private Boolean newPrivacy;
        private Integer previousMaxMembers;
        private Integer newMaxMembers;
        private Boolean previousAllowInvites;
        private Boolean newAllowInvites;
        private Boolean previousRequireApproval;
        private Boolean newRequireApproval;
        private Long updatedBy;
        private String updatedByUsername;
    }

    /**
     * Событие отправки приглашения в команду
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamInvitationSentEvent extends BaseEvent {
        private Long invitationId;
        private Long teamId;
        private String teamName;
        private Long invitedUserId;
        private String invitedUsername;
        private Long invitedBy;
        private String invitedByUsername;
        private String invitationMessage;
        private String expiresAt;
    }

    /**
     * Событие принятия приглашения в команду
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamInvitationAcceptedEvent extends BaseEvent {
        private Long invitationId;
        private Long teamId;
        private String teamName;
        private Long userId;
        private String username;
        private String responseMessage;
        private String acceptedAt;
    }

    /**
     * Событие отклонения приглашения в команду
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamInvitationDeclinedEvent extends BaseEvent {
        private Long invitationId;
        private Long teamId;
        private String teamName;
        private Long userId;
        private String username;
        private String responseMessage;
        private String declinedAt;
    }

    /**
     * Событие отзыва приглашения в команду
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamInvitationRevokedEvent extends BaseEvent {
        private Long invitationId;
        private Long teamId;
        private String teamName;
        private Long userId;
        private String username;
        private Long revokedBy;
        private String revokedByUsername;
        private String revocationReason;
        private String revokedAt;
    }

    /**
     * Событие статистики команды
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamStatisticsUpdatedEvent extends BaseEvent {
        private Long teamId;
        private String teamName;
        private Integer memberCount;
        private Integer activeMemberCount;
        private Long totalInvitationsSent;
        private Long totalInvitationsAccepted;
        private Long totalInvitationsDeclined;
        private Double acceptanceRate;
        private String lastActivityAt;
    }
}