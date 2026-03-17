package dn.quest.teammanagement.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса поиска команд
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchTeamsRequest {

    @Size(max = 120, message = "Название команды не должно превышать 120 символов")
    private String name;

    private String tag;

    private Boolean isPrivate;

    private Boolean publicProfile;

    private Boolean allowSearch;

    @Min(value = 0, message = "Номер страницы должен быть не менее 0")
    private Integer page = 0;

    @Min(value = 1, message = "Размер страницы должен быть не менее 1")
    @Max(value = 100, message = "Размер страницы не должен превышать 100")
    private Integer size = 20;

    private String sortBy = "name";

    private String sortDirection = "asc";
}