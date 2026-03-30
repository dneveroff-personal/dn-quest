package dn.quest.teammanagement.dto;

import dn.quest.shared.dto.UserDTO;
import dn.quest.shared.enums.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO для приглашения в команду
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamInvitationDTO {

    private Long id;
    private TeamDTO team;
    private UserDTO user;
    private UserDTO invitedBy;
    private InvitationStatus status;
    private String invitationMessage;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant respondedAt;
    private Instant expiresAt;
    private String responseMessage;

    /**
     * Проверить, активно ли приглашение
     */
    public boolean isActive() {
        return InvitationStatus.PENDING.equals(status) && 
               (expiresAt == null || expiresAt.isAfter(Instant.now()));
    }

    /**
     * Проверить, истекло ли приглашение
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    /**
     * Получить статус в виде строки
     */
    public String getStatusDisplayName() {
        return status.getDisplayName();
    }

    /**
     * Получить время до истечения приглашения
     */
    public String getTimeToExpiry() {
        if (expiresAt == null) {
            return "Бессрочно";
        }
        
        Instant now = Instant.now();
        if (expiresAt.isBefore(now)) {
            return "Истекло";
        }
        
        long hours = java.time.Duration.between(now, expiresAt).toHours();
        if (hours > 24) {
            long days = hours / 24;
            return days + " дн.";
        } else if (hours > 1) {
            return hours + " ч.";
        } else {
            long minutes = java.time.Duration.between(now, expiresAt).toMinutes();
            return minutes + " мин.";
        }
    }

    /**
     * Получить время создания в удобном формате
     */
    public String getCreatedAtFormatted() {
        if (createdAt == null) {
            return "";
        }
        
        Instant now = Instant.now();
        long hours = java.time.Duration.between(createdAt, now).toHours();
        
        if (hours < 1) {
            long minutes = java.time.Duration.between(createdAt, now).toMinutes();
            return minutes + " мин. назад";
        } else if (hours < 24) {
            return hours + " ч. назад";
        } else {
            long days = hours / 24;
            return days + " дн. назад";
        }
    }

    /**
     * Получить время ответа в удобном формате
     */
    public String getRespondedAtFormatted() {
        if (respondedAt == null) {
            return "";
        }
        
        Instant now = Instant.now();
        long hours = java.time.Duration.between(respondedAt, now).toHours();
        
        if (hours < 1) {
            long minutes = java.time.Duration.between(respondedAt, now).toMinutes();
            return minutes + " мин. назад";
        } else if (hours < 24) {
            return hours + " ч. назад";
        } else {
            long days = hours / 24;
            return days + " дн. назад";
        }
    }

    /**
     * Получить название команды
     */
    public String getTeamName() {
        return team != null ? team.getName() : "Unknown Team";
    }

    /**
     * Получить имя пользователя
     */
    public String getUserName() {
        return user != null ? user.getPublicName() : "Unknown User";
    }

    /**
     * Получить имя пригласившего
     */
    public String getInvitedByName() {
        return invitedBy != null ? invitedBy.getPublicName() : "Unknown";
    }
}