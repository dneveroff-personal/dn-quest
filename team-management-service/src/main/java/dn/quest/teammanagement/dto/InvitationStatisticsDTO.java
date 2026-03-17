package dn.quest.teammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для статистики приглашений команды
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationStatisticsDTO {
    
    /**
     * Общее количество отправленных приглашений
     */
    private Long totalSent;
    
    /**
     * Общее количество принятых приглашений
     */
    private Long totalAccepted;
    
    /**
     * Общее количество отклоненных приглашений
     */
    private Long totalDeclined;
    
    /**
     * Общее количество истекших приглашений
     */
    private Long totalExpired;
    
    /**
     * Общее количество ожидающих приглашений
     */
    private Long totalPending;
    
    /**
     * Процент принятия приглашений
     */
    private Double acceptanceRate;
    
    /**
     * Процент отклонения приглашений
     */
    private Double declineRate;
}