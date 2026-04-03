package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

/**
 * Событие связанное с игровой сессией
 */
@Schema(description = "Событие игровой сессии")
public class GameSessionEvent extends GameEngineEvent {

    @Schema(description = "Тип события", example = "game-session-event", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Override
    protected String getEventTypeForSubclass() {
        return "game-session-event";
    }

    @Getter
    @Setter
    @Schema(description = "Подтип события", example = "SESSION_STARTED", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private GameSessionEventType subType;

    @Getter
    @Setter
    @Schema(description = "ID сессии", example = "789", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    @Getter
    @Setter
    @Schema(description = "Название сессии", example = "Вечерний квест")
    private String sessionName;

    @Getter
    @Setter
    @Schema(description = "Статус сессии", example = "ACTIVE")
    private String status;

    @Getter
    @Setter
    @Schema(description = "Предыдущий статус сессии")
    private String previousStatus;

    @Getter
    @Setter
    @Schema(description = "ID владельца сессии", example = "123")
    private UUID ownerId;

    @Getter
    @Setter
    @Schema(description = "ID квеста", example = "101")
    private Long questId;

    @Getter
    @Setter
    @Schema(description = "Название квеста", example = "Тайна старого замка")
    private String questName;

    @Getter
    @Setter
    @Schema(description = "ID команды", example = "456")
    private UUID teamId;

    @Getter
    @Setter
    @Schema(description = "Название команды", example = "Мстители")
    private String teamName;

    @Getter
    @Setter
    @Schema(description = "Количество участников", example = "5")
    private Integer participantCount;

    @Getter
    @Setter
    @Schema(description = "Максимальное количество участников", example = "10")
    private Integer maxParticipants;

    @Getter
    @Setter
    @Schema(description = "Продолжительность в секундах", example = "3600")
    private Long durationSeconds;

    @Getter
    @Setter
    @Schema(description = "Общий счет", example = "1500.5")
    private Double totalScore;

    @Getter
    @Setter
    @Schema(description = "Дополнительные метаданные")
    private Map<String, Object> metadata;

    /**
     * Типы событий игровой сессии
     */
    public enum GameSessionEventType {
        SESSION_STARTED,
        SESSION_FINISHED,
        SESSION_CANCELLED,
        SESSION_PAUSED,
        SESSION_RESUMED,
        PARTICIPANT_JOINED,
        PARTICIPANT_LEFT,
        LEVEL_COMPLETED,
        HINT_USED,
        ATTEMPT_SUBMITTED
    }
}