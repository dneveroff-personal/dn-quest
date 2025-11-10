package dn.quest.teammanagement.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на обновление команды
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeamRequest {

    @Size(min = 3, max = 120, message = "Название команды должно содержать от 3 до 120 символов")
    private String name;

    @Size(max = 500, message = "Описание команды не должно превышать 500 символов")
    private String description;

    @Size(max = 500, message = "URL логотипа не должен превышать 500 символов")
    private String logoUrl;

    private Integer maxMembers;

    private Boolean isPrivate;

    private Boolean isActive;
}