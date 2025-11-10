package dn.quest.teammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для глобальной статистики приглашений
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalInvitationStatisticsDTO {
    
    /**
     * Общее количество активных команд
     */
    private Long totalActiveTeams;
    
    /**
     * Общее количество приглашений в системе
     */
    private Long totalInvitations;
    
    /**
     * Количество активных приглашений
     */
    private Long activeInvitations;
    
    /**
     * Средний процент принятия приглашений
     */
    private Double averageAcceptanceRate;
    
    /**
     * ID самой активной команды
     */
    private Long mostActiveTeamId;
    
    /**
     * Название самой активной команды
     */
    private String mostActiveTeamName;
}