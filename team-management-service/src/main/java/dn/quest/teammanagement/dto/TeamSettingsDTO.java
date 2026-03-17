package dn.quest.teammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * DTO для настроек команды
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamSettingsDTO {

    private Long id;
    private Long teamId;
    private Boolean allowMemberInvites;
    private Boolean requireCaptainApproval;
    private Boolean autoAcceptInvites;
    private Integer invitationExpiryHours;
    private Integer maxPendingInvitations;
    private Boolean allowMemberLeave;
    private Boolean requireCaptainForDisband;
    private Boolean enableTeamChat;
    private Boolean enableTeamStatistics;
    private Boolean publicProfile;
    private Boolean allowSearch;
    private String teamTags;
    private String welcomeMessage;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Получить теги в виде списка
     */
    public List<String> getTagsList() {
        if (teamTags == null || teamTags.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(teamTags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }

    /**
     * Установить теги из списка
     */
    public void setTagsList(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            this.teamTags = null;
        } else {
            this.teamTags = String.join(", ", tags);
        }
    }

    /**
     * Получить срок действия приглашения в днях
     */
    public Integer getInvitationExpiryDays() {
        return invitationExpiryHours != null ? invitationExpiryHours / 24 : 7;
    }

    /**
     * Установить срок действия приглашения в днях
     */
    public void setInvitationExpiryDays(Integer days) {
        this.invitationExpiryHours = days != null ? days * 24 : 168;
    }

    /**
     * Получить описание срока действия приглашения
     */
    public String getInvitationExpiryDescription() {
        if (invitationExpiryHours == null) {
            return "7 дней (по умолчанию)";
        }
        
        if (invitationExpiryHours < 24) {
            return invitationExpiryHours + " часов";
        } else {
            int days = invitationExpiryHours / 24;
            int hours = invitationExpiryHours % 24;
            
            if (hours == 0) {
                return days + " " + getDayWord(days);
            } else {
                return days + " " + getDayWord(days) + " " + hours + " " + getHourWord(hours);
            }
        }
    }

    /**
     * Получить правильное слово для "день"
     */
    private String getDayWord(int days) {
        if (days % 10 == 1 && days % 100 != 11) {
            return "день";
        } else if (days % 10 >= 2 && days % 10 <= 4 && (days % 100 < 10 || days % 100 >= 20)) {
            return "дня";
        } else {
            return "дней";
        }
    }

    /**
     * Получить правильное слово для "час"
     */
    private String getHourWord(int hours) {
        if (hours % 10 == 1 && hours % 100 != 11) {
            return "час";
        } else if (hours % 10 >= 2 && hours % 10 <= 4 && (hours % 100 < 10 || hours % 100 >= 20)) {
            return "часа";
        } else {
            return "часов";
        }
    }

    /**
     * Проверить, являются ли настройки стандартными
     */
    public boolean isDefaultSettings() {
        return Boolean.FALSE.equals(allowMemberInvites) &&
               Boolean.TRUE.equals(requireCaptainApproval) &&
               Boolean.FALSE.equals(autoAcceptInvites) &&
               (invitationExpiryHours == null || invitationExpiryHours.equals(168)) &&
               (maxPendingInvitations == null || maxPendingInvitations.equals(10)) &&
               Boolean.TRUE.equals(allowMemberLeave) &&
               Boolean.TRUE.equals(requireCaptainForDisband) &&
               Boolean.TRUE.equals(enableTeamChat) &&
               Boolean.TRUE.equals(enableTeamStatistics) &&
               Boolean.FALSE.equals(publicProfile) &&
               Boolean.TRUE.equals(allowSearch) &&
               (teamTags == null || teamTags.trim().isEmpty()) &&
               (welcomeMessage == null || welcomeMessage.trim().isEmpty());
    }
}