package dn.quest.teammanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Сущность настроек команды
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team_settings",
       indexes = {
           @Index(name = "idx_settings_team", columnList = "team_id")
       })
public class TeamSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false, unique = true)
    private Team team;

    @Column(name = "allow_member_invites")
    @Builder.Default
    private Boolean allowMemberInvites = false;

    @Column(name = "require_captain_approval")
    @Builder.Default
    private Boolean requireCaptainApproval = true;

    @Column(name = "auto_accept_invites")
    @Builder.Default
    private Boolean autoAcceptInvites = false;

    @Column(name = "invitation_expiry_hours")
    @Builder.Default
    private Integer invitationExpiryHours = 168; // 7 дней

    @Column(name = "max_pending_invitations")
    @Builder.Default
    private Integer maxPendingInvitations = 10;

    @Column(name = "allow_member_leave")
    @Builder.Default
    private Boolean allowMemberLeave = true;

    @Column(name = "require_captain_for_disband")
    @Builder.Default
    private Boolean requireCaptainForDisband = true;

    @Column(name = "enable_team_chat")
    @Builder.Default
    private Boolean enableTeamChat = true;

    @Column(name = "enable_team_statistics")
    @Builder.Default
    private Boolean enableTeamStatistics = true;

    @Column(name = "public_profile")
    @Builder.Default
    private Boolean publicProfile = false;

    @Column(name = "allow_search")
    @Builder.Default
    private Boolean allowSearch = true;

    @Column(name = "team_tags", length = 500)
    private String teamTags;

    @Column(name = "welcome_message", length = 1000)
    private String welcomeMessage;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Проверяет, могут ли участники приглашать новых членов
     */
    public boolean canMembersInvite() {
        return Boolean.TRUE.equals(allowMemberInvites);
    }

    /**
     * Проверяет, требуется ли одобрение капитана для вступления
     */
    public boolean requiresCaptainApproval() {
        return Boolean.TRUE.equals(requireCaptainApproval);
    }

    /**
     * Проверяет, принимаются ли приглашения автоматически
     */
    public boolean autoAcceptInvitations() {
        return Boolean.TRUE.equals(autoAcceptInvites);
    }

    /**
     * Получает срок действия приглашения в часах
     */
    public int getInvitationExpiryHours() {
        return invitationExpiryHours != null ? invitationExpiryHours : 168;
    }

    /**
     * Получает максимальное количество ожидающих приглашений
     */
    public int getMaxPendingInvitations() {
        return maxPendingInvitations != null ? maxPendingInvitations : 10;
    }

    /**
     * Проверяет, могут ли участники покидать команду
     */
    public boolean canMembersLeave() {
        return Boolean.TRUE.equals(allowMemberLeave);
    }

    /**
     * Проверяет, требуется ли капитан для расформирования команды
     */
    public boolean requiresCaptainForDisband() {
        return Boolean.TRUE.equals(requireCaptainForDisband);
    }

    /**
     * Проверяет, включен ли командный чат
     */
    public boolean isTeamChatEnabled() {
        return Boolean.TRUE.equals(enableTeamChat);
    }

    /**
     * Проверяет, включена ли статистика команды
     */
    public boolean isTeamStatisticsEnabled() {
        return Boolean.TRUE.equals(enableTeamStatistics);
    }

    /**
     * Проверяет, является ли профиль команды публичным
     */
    public boolean isPublicProfile() {
        return Boolean.TRUE.equals(publicProfile);
    }

    /**
     * Проверяет, разрешен ли поиск команды
     */
    public boolean isSearchAllowed() {
        return Boolean.TRUE.equals(allowSearch);
    }
}