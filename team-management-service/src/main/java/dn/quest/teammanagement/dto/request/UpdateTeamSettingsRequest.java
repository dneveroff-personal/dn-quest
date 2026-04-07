package dn.quest.teammanagement.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на обновление настроек команды
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeamSettingsRequest {

    private Boolean allowMemberInvites;

    private Boolean requireCaptainApproval;

    private Boolean autoAcceptInvites;

    @Min(value = 1, message = "Срок действия приглашения должен быть не менее 1 часа")
    @Max(value = 8760, message = "Срок действия приглашения не должен превышать 8760 часов (1 год)")
    private Integer invitationExpiryHours;

    @Min(value = 1, message = "Максимальное количество приглашений должно быть не менее 1")
    @Max(value = 100, message = "Максимальное количество приглашений не должно превышать 100")
    private Integer maxPendingInvitations;

    private Boolean allowMemberLeave;

    private Boolean requireCaptainForDisband;

    private Boolean enableTeamChat;

    private Boolean enableTeamStatistics;

    private Boolean publicProfile;

    private Boolean allowSearch;

    @Size(max = 500, message = "Теги команды не должны превышать 500 символов")
    private String teamTags;

    @Size(max = 1000, message = "Приветственное сообщение не должно превышать 1000 символов")
    private String welcomeMessage;
}