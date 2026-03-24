package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

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

    @Schema(description = "Подтип события", example = "SESSION_STARTED", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private GameSessionEventType subType;

    @Schema(description = "ID сессии", example = "789", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    @Schema(description = "Название сессии", example = "Вечерний квест")
    private String sessionName;

    @Schema(description = "Статус сессии", example = "ACTIVE")
    private String status;

    @Schema(description = "Предыдущий статус сессии")
    private String previousStatus;

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

    @Schema(description = "Дополнительные метаданные")
    private Map<String, Object> metadata;

    // Getters and setters
    public GameSessionEventType getSubType() {
        return subType;
    }

    public void setSubType(GameSessionEventType subType) {
        this.subType = subType;
    }

    @Override
    public Long getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public Long getQuestId() {
        return questId;
    }

    @Override
    public void setQuestId(Long questId) {
        this.questId = questId;
    }

    public String getQuestName() {
        return questName;
    }

    public void setQuestName(String questName) {
        this.questName = questName;
    }

    @Override
    public Long getTeamId() {
        return teamId;
    }

    @Override
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

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

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