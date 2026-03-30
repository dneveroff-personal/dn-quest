package dn.quest.teammanagement.dto.request;

import dn.quest.shared.enums.TeamRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на изменение роли участника
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeMemberRoleRequest {

    @NotNull(message = "Роль не может быть пустой")
    private TeamRole role;
}