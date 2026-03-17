package dn.quest.teammanagement.dto.response;

import dn.quest.teammanagement.dto.TeamDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ответа со списком команд
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamListResponse {

    private List<TeamDTO> teams;
    private long totalCount;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    /**
     * Создать ответ с пагинацией
     */
    public static TeamListResponse of(List<TeamDTO> teams, long totalCount, int page, int size) {
        int totalPages = (int) Math.ceil((double) totalCount / size);
        
        return TeamListResponse.builder()
                .teams(teams)
                .totalCount(totalCount)
                .currentPage(page)
                .pageSize(size)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }
}