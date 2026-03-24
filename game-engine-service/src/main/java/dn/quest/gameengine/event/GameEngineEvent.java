package dn.quest.gameengine.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dn.quest.shared.events.game.CodeAttemptEvent;
import dn.quest.shared.events.game.GameSessionEvent;
import dn.quest.shared.events.game.LeaderboardEvent;
import dn.quest.shared.events.game.LevelCompletionEvent;
import dn.quest.shared.events.game.LevelProgressEvent;
import dn.quest.shared.events.game.ParticipationRequestEvent;
import dn.quest.shared.events.team.TeamEvent;
import dn.quest.shared.events.user.UserEvent;
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
    @JsonSubTypes.Type(value = TeamEvent.class, name = "team-event"),
    @JsonSubTypes.Type(value = UserEvent.class, name = "user-event")
})
public abstract class GameEngineEvent {

    @Schema(description = "Тип события", example = "game-session-event", required = true)
    protected abstract String getEventType();

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

    protected GameEngineEvent() {
        this.timestamp = Instant.now();
        this.version = "1.0";
        this.source = "game-engine-service";
        this.eventId = generateEventId();
    }

    protected GameEngineEvent(Long sessionId, Long userId, Long teamId, Long questId) {
        this();
        this.sessionId = sessionId;
        this.userId = userId;
        this.teamId = teamId;
        this.questId = questId;
    }

    // Getters and Setters

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

    /**
     * Генерация уникального ID события
     */
    private String generateEventId() {
        return "evt_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    /**
     * Добавление метаданных
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
    }

    /**
     * Проверка валидности события
     */
    public boolean isValid() {
        return eventId != null && !eventId.trim().isEmpty() && 
               timestamp != null && 
               getEventType() != null && !getEventType().trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.format("%s{eventId='%s', sessionId=%d, userId=%d, timestamp=%s}",
            getClass().getSimpleName(), eventId, sessionId, userId, timestamp);
    }
}