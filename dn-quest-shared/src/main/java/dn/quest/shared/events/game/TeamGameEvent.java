package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

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
    private Long teamId;

    @Schema(description = "Название команды", example = "Мстители")
    private String teamName;

    @Schema(description = "Тип события", example = "TEAM_JOINED")
    private String teamEventType;

    @Schema(description = "ID игрока", example = "123")
    private Long playerId;

    @Schema(description = "Имя игрока", example = "Player1")
    private String playerName;

    @Schema(description = "Роль игрока", example = "LEADER")
    private String playerRole;

    @Schema(description = "Счет команды", example = "1500.0")
    private Double teamScore;

    @Schema(description = "Позиция в лидерборде", example = "5")
    private Integer leaderboardPosition;

    @Schema(description = "Количество активных участников", example = "4")
    private Integer activeMembers;

    @Schema(description = "Время события")
    private Instant eventTime;

    @Schema(description = "Дополнительные метаданные")
    private Map<String, Object> metadata;

    // Getters and setters
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

    public String getTeamEventType() {
        return teamEventType;
    }

    public void setTeamEventType(String teamEventType) {
        this.teamEventType = teamEventType;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerRole() {
        return playerRole;
    }

    public void setPlayerRole(String playerRole) {
        this.playerRole = playerRole;
    }

    public Double getTeamScore() {
        return teamScore;
    }

    public void setTeamScore(Double teamScore) {
        this.teamScore = teamScore;
    }

    public Integer getLeaderboardPosition() {
        return leaderboardPosition;
    }

    public void setLeaderboardPosition(Integer leaderboardPosition) {
        this.leaderboardPosition = leaderboardPosition;
    }

    public Integer getActiveMembers() {
        return activeMembers;
    }

    public void setActiveMembers(Integer activeMembers) {
        this.activeMembers = activeMembers;
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