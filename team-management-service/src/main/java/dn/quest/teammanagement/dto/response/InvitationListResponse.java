package dn.quest.teammanagement.dto.response;

import dn.quest.teammanagement.dto.TeamInvitationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ответа со списком приглашений
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationListResponse {

    private List<TeamInvitationDTO> invitations;
    private long totalCount;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private long pendingCount;
    private long acceptedCount;
    private long declinedCount;
    private long expiredCount;

    /**
     * Создать ответ с пагинацией
     */
    public static InvitationListResponse of(List<TeamInvitationDTO> invitations, long totalCount, int page, int size) {
        int totalPages = (int) Math.ceil((double) totalCount / size);
        
        // Подсчет статистики по статусам
        long pendingCount = invitations.stream()
                .filter(inv -> "PENDING".equals(inv.getStatus().name()))
                .count();
        long acceptedCount = invitations.stream()
                .filter(inv -> "ACCEPTED".equals(inv.getStatus().name()))
                .count();
        long declinedCount = invitations.stream()
                .filter(inv -> "DECLINED".equals(inv.getStatus().name()))
                .count();
        long expiredCount = invitations.stream()
                .filter(inv -> "EXPIRED".equals(inv.getStatus().name()))
                .count();
        
        return InvitationListResponse.builder()
                .invitations(invitations)
                .totalCount(totalCount)
                .currentPage(page)
                .pageSize(size)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .pendingCount(pendingCount)
                .acceptedCount(acceptedCount)
                .declinedCount(declinedCount)
                .expiredCount(expiredCount)
                .build();
    }
}