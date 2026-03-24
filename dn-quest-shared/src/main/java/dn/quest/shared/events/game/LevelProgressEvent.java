package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Событие прогресса уровня
 */
@Schema(description = "Событие прогресса уровня")
public class LevelProgressEvent extends GameEngineEvent {

    @Schema(description = "Тип события", example = "level-progress-event", required = true)
    @Override
    protected String getEventTypeForSubclass() {
        return "level-progress-event";
    }

    @Schema(description = "ID уровня", example = "101", required = true)
    private Long levelId;

    @Schema(description = "Название уровня", example = "Первый этаж")
    private String levelName;

    @Schema(description = "Номер уровня", example = "1")
    private Integer levelNumber;

    @Schema(description = "Прогресс в процентах", example = "75")
    private Integer progressPercent;

    @Schema(description = "Текущая позиция", example = "3")
    private Integer currentPosition;

    @Schema(description = "Общее количество позиций", example = "10")
    private Integer totalPositions;

    @Schema(description = "Количество подсказок", example = "2")
    private Integer hintsUsed;

    @Schema(description = "Очки за уровень", example = "500.0")
    private Double levelScore;

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

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Integer getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Integer currentPosition) {
        this.currentPosition = currentPosition;
    }

    public Integer getTotalPositions() {
        return totalPositions;
    }

    public void setTotalPositions(Integer totalPositions) {
        this.totalPositions = totalPositions;
    }

    public Integer getHintsUsed() {
        return hintsUsed;
    }

    public void setHintsUsed(Integer hintsUsed) {
        this.hintsUsed = hintsUsed;
    }

    public Double getLevelScore() {
        return levelScore;
    }

    public void setLevelScore(Double levelScore) {
        this.levelScore = levelScore;
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