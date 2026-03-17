package dn.quest.gameengine.event;

import dn.quest.gameengine.entity.enums.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Событие связанное с игровой сессией
 */
@Schema(description = "Событие игровой сессии")
public class GameSessionEvent extends GameEngineEvent {

    @Schema(description = "Тип события", example = "game-session-event", required = true)
    @Override
    protected String getEventType() {
        return "game-session-event";
    }

    @Schema(description = "Подтип события", example = "SESSION_STARTED", required = true)
    private GameSessionEventType subType;

    @Schema(description = "ID сессии", example = "789", required = true)
    private Long sessionId;

    @Schema(description = "Название сессии", example = "Вечерний квест")
    private String sessionName;

    @Schema(description = "Статус сессии", example = "ACTIVE")
    private SessionStatus status;

    @Schema(description = "Предыдущий статус сессии")
    private SessionStatus previousStatus;

    @Schema(description = "ID владельца сессии", example = "123")
    private Long ownerId;

    @Schema(description = "ID квеста", example = "101")
    private Long questId;

    @Schema(description = "Название квеста", example = "Тайна старого замка")
    private String questName;

    @Schema(description = "ID команды", example = "456")
    private Long teamId;

    @Schema(description = "Название команды", example = "Мстители")
    private String teamName;

    @Schema(description = "Количество участников", example = "5")
    private Integer participantCount;

    @Schema(description = "Максимальное количество участников", example = "10")
    private Integer maxParticipants;

    @Schema(description = "Продолжительность в секундах", example = "3600")
    private Long durationSeconds;

    @Schema(description = "Общий счет", example = "1500.5")
    private Double totalScore;

    @Schema(description = "Причина события", example = "Пользователь запустил сессию")
    private String reason;

    @Schema(description = "Дополнительные данные сессии")
    private Map<String, Object> sessionData;

    // Конструкторы

    public GameSessionEvent() {
        super();
    }

    public GameSessionEvent(Long sessionId, Long userId, Long teamId, Long questId, GameSessionEventType subType) {
        super(sessionId, userId, teamId, questId);
        this.sessionId = sessionId;
        this.subType = subType;
    }

    // Статические фабричные методы для удобства

    public static GameSessionEvent sessionStarted(Long sessionId, Long userId, Long teamId, Long questId) {
        GameSessionEvent event = new GameSessionEvent(sessionId, userId, teamId, questId, GameSessionEventType.SESSION_STARTED);
        event.addMetadata("action", "start");
        return event;
    }

    public static GameSessionEvent sessionPaused(Long sessionId, Long userId, Long teamId, Long questId) {
        GameSessionEvent event = new GameSessionEvent(sessionId, userId, teamId, questId, GameSessionEventType.SESSION_PAUSED);
        event.addMetadata("action", "pause");
        return event;
    }

    public static GameSessionEvent sessionResumed(Long sessionId, Long userId, Long teamId, Long questId) {
        GameSessionEvent event = new GameSessionEvent(sessionId, userId, teamId, questId, GameSessionEventType.SESSION_RESUMED);
        event.addMetadata("action", "resume");
        return event;
    }

    public static GameSessionEvent sessionFinished(Long sessionId, Long userId, Long teamId, Long questId) {
        GameSessionEvent event = new GameSessionEvent(sessionId, userId, teamId, questId, GameSessionEventType.SESSION_FINISHED);
        event.addMetadata("action", "finish");
        return event;
    }

    public static GameSessionEvent sessionCreated(Long sessionId, Long userId, Long teamId, Long questId) {
        GameSessionEvent event = new GameSessionEvent(sessionId, userId, teamId, questId, GameSessionEventType.SESSION_CREATED);
        event.addMetadata("action", "create");
        return event;
    }

    public static GameSessionEvent sessionUpdated(Long sessionId, Long userId, Long teamId, Long questId) {
        GameSessionEvent event = new GameSessionEvent(sessionId, userId, teamId, questId, GameSessionEventType.SESSION_UPDATED);
        event.addMetadata("action", "update");
        return event;
    }

    public static GameSessionEvent sessionDeleted(Long sessionId, Long userId, Long teamId, Long questId) {
        GameSessionEvent event = new GameSessionEvent(sessionId, userId, teamId, questId, GameSessionEventType.SESSION_DELETED);
        event.addMetadata("action", "delete");
        return event;
    }

    public static GameSessionEvent userJoined(Long sessionId, Long userId, Long teamId, Long questId) {
        GameSessionEvent event = new GameSessionEvent(sessionId, userId, teamId, questId, GameSessionEventType.USER_JOINED);
        event.addMetadata("action", "join");
        return event;
    }

    public static GameSessionEvent userLeft(Long sessionId, Long userId, Long teamId, Long questId) {
        GameSessionEvent event = new GameSessionEvent(sessionId, userId, teamId, questId, GameSessionEventType.USER_LEFT);
        event.addMetadata("action", "leave");
        return event;
    }

    public static GameSessionEvent levelChanged(Long sessionId, Long userId, Long teamId, Long questId, Long newLevelId) {
        GameSessionEvent event = new GameSessionEvent(sessionId, userId, teamId, questId, GameSessionEventType.LEVEL_CHANGED);
        event.addMetadata("action", "level_change");
        event.addMetadata("newLevelId", newLevelId);
        return event;
    }

    // Getters and Setters

    public GameSessionEventType getSubType() {
        return subType;
    }

    public void setSubType(GameSessionEventType subType) {
        this.subType = subType;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public SessionStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(SessionStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getQuestId() {
        return questId;
    }

    public void setQuestId(Long questId) {
        this.questId = questId;
    }

    public String getQuestName() {
        return questName;
    }

    public void setQuestName(String questName) {
        this.questName = questName;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Map<String, Object> getSessionData() {
        return sessionData;
    }

    public void setSessionData(Map<String, Object> sessionData) {
        this.sessionData = sessionData;
    }

    /**
     * Перечисление типов событий игровой сессии
     */
    public enum GameSessionEventType {
        SESSION_CREATED("Сессия создана"),
        SESSION_STARTED("Сессия запущена"),
        SESSION_PAUSED("Сессия приостановлена"),
        SESSION_RESUMED("Сессия возобновлена"),
        SESSION_FINISHED("Сессия завершена"),
        SESSION_UPDATED("Сессия обновлена"),
        SESSION_DELETED("Сессия удалена"),
        USER_JOINED("Пользователь присоединился"),
        USER_LEFT("Пользователь покинул"),
        LEVEL_CHANGED("Уровень изменен"),
        SCORE_UPDATED("Счет обновлен"),
        PARTICIPANTS_CHANGED("Участники изменены");

        private final String description;

        GameSessionEventType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() && 
               subType != null && 
               sessionId != null;
    }
}