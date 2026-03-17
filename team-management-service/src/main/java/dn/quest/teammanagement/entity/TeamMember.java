package dn.quest.teammanagement.entity;

import dn.quest.teammanagement.enums.TeamRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Сущность участника команды
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team_members",
       uniqueConstraints = @UniqueConstraint(name = "uk_team_user", columnNames = {"team_id", "user_id"}),
       indexes = {
           @Index(name = "idx_team_member_user", columnList = "user_id"),
           @Index(name = "idx_team_member_role", columnList = "role"),
           @Index(name = "idx_team_member_joined_at", columnList = "joined_at")
       })
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private TeamRole role = TeamRole.MEMBER;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private Instant joinedAt = Instant.now();

    @Column(name = "left_at")
    private Instant leftAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Проверяет, является ли участник капитаном
     */
    public boolean isCaptain() {
        return TeamRole.CAPTAIN.equals(role);
    }

    /**
     * Проверяет, является ли участник модератором или капитаном
     */
    public boolean isModeratorOrAbove() {
        return TeamRole.CAPTAIN.equals(role) || TeamRole.MODERATOR.equals(role);
    }

    /**
     * Проверяет, может ли участник управлять командой
     */
    public boolean canManageTeam() {
        return isCaptain() || TeamRole.MODERATOR.equals(role);
    }

    /**
     * Проверяет, может ли участник приглашать новых членов
     */
    public boolean canInviteMembers() {
        return isCaptain() || TeamRole.MODERATOR.equals(role);
    }

    /**
     * Проверяет, может ли участник удалять других участников
     */
    public boolean canRemoveMembers() {
        return isCaptain() || TeamRole.MODERATOR.equals(role);
    }

    /**
     * Деактивирует участника (устанавливает дату ухода)
     */
    public void deactivate() {
        this.isActive = false;
        this.leftAt = Instant.now();
    }

    /**
     * Активирует участника
     */
    public void activate() {
        this.isActive = true;
        this.leftAt = null;
    }
}