package dn.quest.teammanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на ответ приглашение
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespondToInvitationRequest {

    @NotNull(message = "Ответ не может быть пустым")
    private Boolean accept;

    private String message;
}