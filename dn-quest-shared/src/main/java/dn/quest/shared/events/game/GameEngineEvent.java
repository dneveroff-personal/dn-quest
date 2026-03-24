package dn.quest.shared.events.game;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

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
    private String eventId;

    @Schema(description = "ID сессии", example = "789")
    private Long sessionId;

    @Schema(description = "ID пользователя", example = "123")
    private Long userId;

    @Schema(description = "ID команды", example = "456")
    private Long teamId;

    @Schema(description = "ID квеста", example = "101")
    private Long questId;

    @Schema(description = "Время создания события")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant timestamp;

    @Schema(description = "Версия события", example = "1.0")
    private String version;

    @Schema(description = "Источник события", example = "game-engine-service")
    private String source;

    @Schema(description = "Дополнительные метаданные")
    private Map<String, Object> metadata;

    // Getters and setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getQuestId() {
        return questId;
    }

    public void setQuestId(Long questId) {
        this.questId = questId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    protected abstract String getEventTypeForSubclass();

    /**
     * Returns the event type for JSON serialization.
     */
    @JsonValue
    public String getEventType() {
        return getEventTypeForSubclass();
    }
}