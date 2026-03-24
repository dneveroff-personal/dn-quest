package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/**
 * Событие завершения уровня
 */
@Schema(description = "Событие завершения уровня")
public class LevelCompletionEvent extends GameEngineEvent {

    @Schema(description = "Тип события", example = "level-completion-event", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Override
    protected String getEventTypeForSubclass() {
        return "level-completion-event";
    }

    @Schema(description = "ID уровня", example = "101", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private Long levelId;

    @Schema(description = "Название уровня", example = "Первый этаж")
    private String levelName;

    @Schema(description = "Номер уровня", example = "1")
    private Integer levelNumber;

    @Schema(description = "Время завершения в секундах", example = "300")
    private Long completionTimeSeconds;

    @Schema(description = "Количество подсказок", example = "2")
    private Integer hintsUsed;

    @Schema(description = "Количество попыток", example = "5")
    private Integer attemptsCount;

    @Schema(description = "Очки за уровень", example = "500.0")
    private Double levelScore;

    @Schema(description = "Бонусные очки", example = "50.0")
    private Double bonusScore;

    @Schema(description = "Штрафные очки", example = "-20.0")
    private Double penaltyScore;

    @Schema(description = "Общий счет", example = "530.0")
    private Double totalScore;

    @Schema(description = "Время завершения")
    private Instant completedAt;

    @Schema(description = "Дополнительные метаданные")
    private Map<String, Object> metadata;

    // Getters and setters
    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public Integer getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(Integer levelNumber) {
        this.levelNumber = levelNumber;
    }

    public Long getCompletionTimeSeconds() {
        return completionTimeSeconds;
    }

    public void setCompletionTimeSeconds(Long completionTimeSeconds) {
        this.completionTimeSeconds = completionTimeSeconds;
    }

    public Integer getHintsUsed() {
        return hintsUsed;
    }

    public void setHintsUsed(Integer hintsUsed) {
        this.hintsUsed = hintsUsed;
    }

    public Integer getAttemptsCount() {
        return attemptsCount;
    }

    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public Double getLevelScore() {
        return levelScore;
    }

    public void setLevelScore(Double levelScore) {
        this.levelScore = levelScore;
    }

    public Double getBonusScore() {
        return bonusScore;
    }

    public void setBonusScore(Double bonusScore) {
        this.bonusScore = bonusScore;
    }

    public Double getPenaltyScore() {
        return penaltyScore;
    }

    public void setPenaltyScore(Double penaltyScore) {
        this.penaltyScore = penaltyScore;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
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