package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Событие лидерборда
 */
@Schema(description = "Событие лидерборда")
public class LeaderboardEvent extends GameEngineEvent {

    @Schema(description = "Тип события", example = "leaderboard-event", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Override
    protected String getEventTypeForSubclass() {
        return "leaderboard-event";
    }

    @Schema(description = "Тип лидерборда", example = "DAILY")
    private String leaderboardType;

    @Schema(description = "ID квеста", example = "101")
    private Long questId;

    @Schema(description = "Название квеста", example = "Тайна старого замка")
    private String questName;

    @Schema(description = "ID команды", example = "456")
    private Long teamId;

    @Schema(description = "Название команды", example = "Мстители")
    private String teamName;

    @Schema(description = "Текущая позиция", example = "5")
    private Integer currentPosition;

    @Schema(description = "Предыдущая позиция", example = "8")
    private Integer previousPosition;

    @Schema(description = "Количество участников", example = "100")
    private Integer totalParticipants;

    @Schema(description = "Текущий счет", example = "1500.0")
    private Double currentScore;

    @Schema(description = "Изменение позиции", example = "3")
    private Integer positionChange;

    @Schema(description = "Список лучших участников")
    private List<LeaderboardEntry> topEntries;

    @Schema(description = "Дополнительные метаданные")
    private Map<String, Object> metadata;

    // Getters and setters
    public String getLeaderboardType() {
        return leaderboardType;
    }

    public void setLeaderboardType(String leaderboardType) {
        this.leaderboardType = leaderboardType;
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

    public Integer getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Integer currentPosition) {
        this.currentPosition = currentPosition;
    }

    public Integer getPreviousPosition() {
        return previousPosition;
    }

    public void setPreviousPosition(Integer previousPosition) {
        this.previousPosition = previousPosition;
    }

    public Integer getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(Integer totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    public Double getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(Double currentScore) {
        this.currentScore = currentScore;
    }

    public Integer getPositionChange() {
        return positionChange;
    }

    public void setPositionChange(Integer positionChange) {
        this.positionChange = positionChange;
    }

    public List<LeaderboardEntry> getTopEntries() {
        return topEntries;
    }

    public void setTopEntries(List<LeaderboardEntry> topEntries) {
        this.topEntries = topEntries;
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
     * Запись лидерборда
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class LeaderboardEntry {
        private Integer rank;
        private Long teamId;
        private String teamName;
        private Double score;
        private Integer levelProgress;
    }
}