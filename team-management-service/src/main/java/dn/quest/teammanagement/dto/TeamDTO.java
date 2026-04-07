package dn.quest.teammanagement.dto;

import dn.quest.shared.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO для команды
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {

    private UUID id;
    private String name;
    private String description;
    private String logoUrl;
    private UserDTO captain;
    private Integer maxMembers;
    private Boolean isPrivate;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private List<TeamMemberDTO> members;
    private TeamSettingsDTO settings;
    private TeamStatisticsDTO statistics;

    /**
     * Получить количество участников
     */
    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }

    /**
     * Получить количество активных участников
     */
    public long getActiveMemberCount() {
        if (members == null) {
            return 0;
        }
        return members.stream()
                .filter(TeamMemberDTO::getIsActive)
                .count();
    }

    /**
     * Проверить, можно ли добавить нового участника
     */
    public boolean canAddMember() {
        return maxMembers == null || getActiveMemberCount() < maxMembers;
    }

    /**
     * Получить количество свободных слотов
     */
    public int getAvailableSlots() {
        if (maxMembers == null) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, maxMembers - (int) getActiveMemberCount());
    }

    /**
     * Проверить, является ли команда публичной
     */
    public boolean isPublicTeam() {
        return !Boolean.TRUE.equals(isPrivate);
    }

    /**
     * Получить статус команды в виде строки
     */
    public String getStatus() {
        if (!Boolean.TRUE.equals(isActive)) {
            return "INACTIVE";
        } else if (Boolean.TRUE.equals(isPrivate)) {
            return "PRIVATE";
        } else {
            return "PUBLIC";
        }
    }
}