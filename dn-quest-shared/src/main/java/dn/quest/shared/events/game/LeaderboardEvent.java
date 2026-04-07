package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Событие лидерборда
 */
@Schema(description = "Событие лидерборда")
public class LeaderboardEvent extends GameEngineEvent {

    @Schema(description = "Тип события", example = "leaderboard-event", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Override
    protected String getEventTypeForSubclass() {
        return "leaderboard-event";
    }

    @Schema(description = "Тип лидерборда", example = "DAILY")
    @Getter
    @Setter
    private String leaderboardType;

    @Schema(description = "ID квеста", example = "101")
    @Getter
    @Setter
    private UUID questId;

    @Schema(description = "Название квеста", example = "Тайна старого замка")
    @Getter
    @Setter
    private String questName;

    @Schema(description = "ID команды", example = "456")
    @Getter
    @Setter
    private UUID teamId;

    @Schema(description = "Название команды", example = "Мстители")
    @Getter
    @Setter
    private String teamName;

    @Schema(description = "Текущая позиция", example = "5")
    @Getter
    @Setter
    private Integer currentPosition;

    @Schema(description = "Предыдущая позиция", example = "8")
    @Getter
    @Setter
    private Integer previousPosition;

    @Schema(description = "Количество участников", example = "100")
    @Getter
    @Setter
    private Integer totalParticipants;

    @Schema(description = "Текущий счет", example = "1500.0")
    @Getter
    @Setter
    private Double currentScore;

    @Schema(description = "Изменение позиции", example = "3")
    @Getter
    @Setter
    private Integer positionChange;

    @Schema(description = "Список лучших участников")
    @Getter
    @Setter
    private List<LeaderboardEntry> topEntries;

    @Schema(description = "Дополнительные метаданные")
    @Getter
    @Setter
    private Map<String, Object> metadata;

    /**
     * Запись лидерборда
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class LeaderboardEntry {
        private Integer rank;
        private UUID teamId;
        private String teamName;
        private Double score;
        private Integer levelProgress;
    }
}