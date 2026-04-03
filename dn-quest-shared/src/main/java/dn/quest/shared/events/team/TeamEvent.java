package dn.quest.shared.events.team;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * События связанные с командами
 */
public class TeamEvent {

    /**
     * Событие создания команды
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamCreatedEvent extends BaseEvent {
        private UUID teamId;
        private String teamName;
        private String teamDescription;
        private String teamTag;
        private String teamCategory;
        private UUID captainId;
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
        private UUID teamId;
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
        private UUID teamId;
        private String teamName;
        private String teamTag;
        private UUID captainId;
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
        private UUID teamId;
        private String teamName;
        private Long memberId;
        private String memberUsername;
        private String memberRole;
        private Long addedBy;
        private String addedByUsername;
        private String joinMethod;
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
        private UUID teamId;
        private String teamName;
        private Long memberId;
        private String memberUsername;
        private String removalReason;
        private Long removedBy;
        private String removedByUsername;
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
        private UUID teamId;
        private String teamName;
        private Long memberId;
        private String memberUsername;
        private String oldRole;
        private String newRole;
        private Long changedBy;
        private String changedByUsername;
    }

    /**
     * Событие смены капитана команды
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamCaptainChangedEvent extends BaseEvent {
        private UUID teamId;
        private String teamName;
        private Long oldCaptainId;
        private String oldCaptainUsername;
        private Long newCaptainId;
        private String newCaptainUsername;
        private Long changedBy;
        private String changedByUsername;
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
        private UUID teamId;
        private String teamName;
        private Boolean isPrivate;
        private Integer maxMembers;
        private Boolean allowMemberInvite;
        private Boolean allowMemberKick;
        private String previousSettings;
        private String newSettings;
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
        private UUID teamId;
        private String teamName;
        private Long invitedUserId;
        private String invitedUserUsername;
        private String invitedUserEmail;
        private Long invitedBy;
        private String invitedByUsername;
        private String invitationType;
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
        private UUID teamId;
        private String teamName;
        private UUID userId;
        private String username;
        private String memberRole;
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
        private UUID teamId;
        private String teamName;
        private UUID userId;
        private String username;
        private String declineReason;
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
        private UUID teamId;
        private String teamName;
        private Long revokedUserId;
        private String revokedUserUsername;
        private Long revokedBy;
        private String revokedByUsername;
        private String revocationReason;
    }

    /**
     * Событие обновления статистики команды
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TeamStatisticsUpdatedEvent extends BaseEvent {
        private UUID teamId;
        private String teamName;
        private Integer totalGames;
        private Integer wins;
        private Integer losses;
        private Double winRate;
        private Integer totalScore;
        private Integer rank;
    }
}