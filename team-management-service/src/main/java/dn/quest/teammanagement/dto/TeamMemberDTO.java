package dn.quest.teammanagement.dto;

import dn.quest.shared.dto.UserDTO;
import dn.quest.shared.enums.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO для участника команды
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberDTO {

    private Long id;
    private UserDTO user;
    private TeamRole role;
    private Instant joinedAt;
    private Instant leftAt;
    private Boolean isActive;

    /**
     * Проверить, является ли участник капитаном
     */
    public boolean isCaptain() {
        return TeamRole.CAPTAIN.equals(role);
    }

    /**
     * Проверить, является ли участник модератором или капитаном
     */
    public boolean isModeratorOrAbove() {
        return TeamRole.CAPTAIN.equals(role) || TeamRole.DEPUTY.equals(role);
    }

    /**
     * Проверить, может ли участник управлять командой
     */
    public boolean canManageTeam() {
        return isCaptain() || TeamRole.DEPUTY.equals(role);
    }

    /**
     * Проверить, может ли участник приглашать новых членов
     */
    public boolean canInviteMembers() {
        return isCaptain() || TeamRole.DEPUTY.equals(role);
    }

    /**
     * Проверить, может ли участник удалять других участников
     */
    public boolean canRemoveMembers() {
        return isCaptain() || TeamRole.DEPUTY.equals(role);
    }

    /**
     * Получить роль в виде строки
     */
    public String getRoleDisplayName() {
        if (role == null) {
            return "UNKNOWN";
        }
        switch (role) {
            case CAPTAIN:
                return "Капитан";
            case DEPUTY:
                return "Модератор";
            case MEMBER:
                return "Участник";
            default:
                return role.name();
        }
    }

    /**
     * Получить статус участника
     */
    public String getStatus() {
        if (!Boolean.TRUE.equals(isActive)) {
            return leftAt != null ? "LEFT" : "INACTIVE";
        } else {
            return "ACTIVE";
        }
    }

    /**
     * Получить время пребывания в команде в днях
     */
    public long getDaysInTeam() {
        Instant endTime = leftAt != null ? leftAt : Instant.now();
        if (joinedAt == null) {
            return 0;
        }
        return java.time.Duration.between(joinedAt, endTime).toDays();
    }

    /**
     * Получить отображаемое имя пользователя
     */
    public String getUserDisplayName() {
        return user != null ? user.getPublicName() : "Unknown";
    }
}