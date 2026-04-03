package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Событие пользователя в игровом движке
 */
@Schema(description = "Событие пользователя в игровом движке")
public class UserGameEvent extends GameEngineEvent {

    @Schema(description = "Тип события", example = "user-event", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Override
    protected String getEventTypeForSubclass() {
        return "user-event";
    }

    @Schema(description = "ID пользователя", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Getter
    @Setter
    private UUID userId;

    @Schema(description = "Имя пользователя", example = "Player1")
    @Getter
    @Setter
    private String username;

    @Schema(description = "Тип события", example = "PLAYER_JOINED")
    @Getter
    @Setter
    private String userEventType;

    @Schema(description = "ID команды")
    @Getter
    @Setter
    private UUID teamId;

    @Schema(description = "Название команды")
    @Getter
    @Setter
    private String teamName;

    @Schema(description = "Счет пользователя", example = "500.0")
    @Getter
    @Setter
    private Double userScore;

    @Schema(description = "Позиция в лидерборде команды", example = "2")
    @Getter
    @Setter
    private Integer teamPosition;

    @Schema(description = "Текущий уровень", example = "5")
    @Getter
    @Setter
    private Integer currentLevel;

    @Schema(description = "Количество очков опыта", example = "1500")
    @Getter
    @Setter
    private Integer experiencePoints;

    @Schema(description = "Время события")
    @Getter
    @Setter
    private Instant eventTime;

    @Schema(description = "Дополнительные метаданные")
    @Getter
    @Setter
    private Map<String, Object> metadata;
}