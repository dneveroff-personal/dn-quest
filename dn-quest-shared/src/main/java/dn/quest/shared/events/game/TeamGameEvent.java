package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Событие команды в игровом движке
 */
@Schema(description = "Событие команды в игровом движке")
public class TeamGameEvent extends GameEngineEvent {

    @Schema(description = "Тип события", example = "team-event", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Override
    protected String getEventTypeForSubclass() {
        return "team-event";
    }

    @Schema(description = "ID команды", example = "456", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Getter
    @Setter
    private UUID teamId;

    @Schema(description = "Название команды", example = "Мстители")
    @Getter
    @Setter
    private String teamName;

    @Schema(description = "Тип события", example = "TEAM_JOINED")
    @Getter
    @Setter
    private String teamEventType;

    @Schema(description = "ID игрока", example = "123")
    @Getter
    @Setter
    private Long playerId;

    @Schema(description = "Имя игрока", example = "Player1")
    @Getter
    @Setter
    private String playerName;

    @Schema(description = "Роль игрока", example = "LEADER")
    @Getter
    @Setter
    private String playerRole;

    @Schema(description = "Счет команды", example = "1500.0")
    @Getter
    @Setter
    private Double teamScore;

    @Schema(description = "Позиция в лидерборде", example = "5")
    @Getter
    @Setter
    private Integer leaderboardPosition;

    @Schema(description = "Количество активных участников", example = "4")
    @Getter
    @Setter
    private Integer activeMembers;

    @Schema(description = "Время события")
    @Getter
    @Setter
    private Instant eventTime;

    @Schema(description = "Дополнительные метаданные")
    @Getter
    @Setter
    private Map<String, Object> metadata;
}