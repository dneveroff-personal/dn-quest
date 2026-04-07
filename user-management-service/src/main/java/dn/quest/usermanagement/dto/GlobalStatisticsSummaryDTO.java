package dn.quest.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO для общей статистики сервиса
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Общая статистика сервиса")
public class GlobalStatisticsSummaryDTO {

    @Schema(description = "Общее количество пользователей", example = "1500")
    private long totalUsers;

    @Schema(description = "Количество активных пользователей (за последние 30 дней)", example = "450")
    private long activeUsers;

    @Schema(description = "Средний счет пользователей", example = "2500.5")
    private Double averageScore;

    @Schema(description = "Средний уровень пользователей", example = "12.3")
    private Double averageLevel;

    @Schema(description = "Среднее количество завершенных квестов", example = "8.7")
    private Double averageQuestsCompleted;

    @Schema(description = "Общий счет всех пользователей", example = "3750750")
    private Long totalScore;

    @Schema(description = "Общее время игры в минутах", example = "125000")
    private Long totalPlaytimeMinutes;

    @Schema(description = "Количество пользователей по диапазонам уровней")
    private Map<String, Long> usersByLevelRange;

    @Schema(description = "Количество пользователей по диапазонам квестов")
    private Map<String, Long> usersByQuestRange;
}
