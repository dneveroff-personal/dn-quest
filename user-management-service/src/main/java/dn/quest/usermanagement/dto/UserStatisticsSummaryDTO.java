package dn.quest.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO для сводной статистики пользователей
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Сводная статистика пользователей")
public class UserStatisticsSummaryDTO {

    @Schema(description = "Общее количество пользователей", example = "1500")
    private long totalUsers;

    @Schema(description = "Количество активных пользователей", example = "450")
    private long activeUsers;

    @Schema(description = "Количество заблокированных пользователей", example = "25")
    private long blockedUsers;

    @Schema(description = "Количество пользователей по ролям")
    private Map<String, Long> usersByRole;
}
