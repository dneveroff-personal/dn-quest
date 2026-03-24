package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

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

    @Schema(description = "ID пользователя", example = "123", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "Имя пользователя", example = "Player1")
    private String username;

    @Schema(description = "Тип события", example = "PLAYER_JOINED")
    private String userEventType;

    @Schema(description = "ID команды")
    private Long teamId;

    @Schema(description = "Название команды")
    private String teamName;

    @Schema(description = "Счет пользователя", example = "500.0")
    private Double userScore;

    @Schema(description = "Позиция в лидерборде команды", example = "2")
    private Integer teamPosition;

    @Schema(description = "Текущий уровень", example = "5")
    private Integer currentLevel;

    @Schema(description = "Количество очков опыта", example = "1500")
    private Integer experiencePoints;

    @Schema(description = "Время события")
    private Instant eventTime;

    @Schema(description = "Дополнительные метаданные")
    private Map<String, Object> metadata;

    // Getters and setters
    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserEventType() {
        return userEventType;
    }

    public void setUserEventType(String userEventType) {
        this.userEventType = userEventType;
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

    public Double getUserScore() {
        return userScore;
    }

    public void setUserScore(Double userScore) {
        this.userScore = userScore;
    }

    public Integer getTeamPosition() {
        return teamPosition;
    }

    public void setTeamPosition(Integer teamPosition) {
        this.teamPosition = teamPosition;
    }

    public Integer getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(Integer currentLevel) {
        this.currentLevel = currentLevel;
    }

    public Integer getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(Integer experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}