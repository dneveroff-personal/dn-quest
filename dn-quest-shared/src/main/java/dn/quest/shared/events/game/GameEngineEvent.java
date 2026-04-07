package dn.quest.shared.events.game;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Базовый класс для всех событий Game Engine Service
 */
@Schema(description = "Базовое событие игрового движка")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = GameSessionEvent.class, name = "game-session-event"),
    @JsonSubTypes.Type(value = CodeAttemptEvent.class, name = "code-attempt-event"),
    @JsonSubTypes.Type(value = LevelProgressEvent.class, name = "level-progress-event"),
    @JsonSubTypes.Type(value = LevelCompletionEvent.class, name = "level-completion-event"),
    @JsonSubTypes.Type(value = LeaderboardEvent.class, name = "leaderboard-event"),
    @JsonSubTypes.Type(value = ParticipationRequestEvent.class, name = "participation-request-event"),
    @JsonSubTypes.Type(value = TeamGameEvent.class, name = "team-event"),
    @JsonSubTypes.Type(value = UserGameEvent.class, name = "user-event")
})
public abstract class GameEngineEvent {

    @Schema(description = "ID события", example = "evt_123456789")
    @Getter
    @Setter
    private String eventId;

    @Schema(description = "ID сессии", example = "789")
    @Getter
    @Setter
    private UUID sessionId;

    @Schema(description = "ID пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    @Getter
    @Setter
    private UUID userId;

    @Schema(description = "ID команды", example = "456")
    @Getter
    @Setter
    private UUID teamId;

    @Schema(description = "ID квеста", example = "101")
    @Getter
    @Setter
    private UUID questId;

    @Schema(description = "Время создания события")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @Getter
    @Setter
    private Instant timestamp;

    @Schema(description = "Версия события", example = "1.0")
    @Getter
    @Setter
    private String version;

    @Schema(description = "Источник события", example = "game-engine-service")
    @Getter
    @Setter
    private String source;

    @Schema(description = "Дополнительные метаданные")
    @Getter
    @Setter
    private Map<String, Object> metadata;

    protected abstract String getEventTypeForSubclass();

    /**
     * Returns the event type for JSON serialization.
     */
    @JsonValue
    public String getEventType() {
        return getEventTypeForSubclass();
    }
}