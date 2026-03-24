package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Событие связанное с попыткой ввода кода
 */
@Schema(description = "Событие попытки ввода кода")
public class CodeAttemptEvent extends GameEngineEvent {

    @Schema(description = "Тип события", example = "code-attempt-event", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Override
    protected String getEventTypeForSubclass() {
        return "code-attempt-event";
    }

    @Schema(description = "Подтип события", example = "CODE_SUBMITTED", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private CodeAttemptEventType subType;

    @Schema(description = "ID попытки", example = "12345", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private Long attemptId;

    @Schema(description = "ID сессии", example = "789", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    @Schema(description = "ID пользователя", example = "123", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "ID уровня", example = "101")
    private Long levelId;

    @Schema(description = "Название уровня", example = "Первый этаж")
    private String levelName;

    @Schema(description = "Отправленный код", example = "ABC123", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private String submittedCode;

    @Schema(description = "Правильный код", example = "ABC123")
    private String correctCode;

    @Schema(description = "Сектор кода", example = "A", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private String sector;

    @Schema(description = "Результат попытки", example = "CORRECT", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private String result;

    @Schema(description = "Полученные очки", example = "100.0")
    private Double points;

    @Schema(description = "Бонусные очки", example = "20.0")
    private Double bonusPoints;

    @Schema(description = "Штрафные очки", example = "-10.0")
    private Double penaltyPoints;

    @Schema(description = "Общий счет за попытку", example = "110.0")
    private Double totalScore;

    @Schema(description = "Дополнительные метаданные")
    private Map<String, Object> metadata;

    // Getters and setters
    public CodeAttemptEventType getSubType() {
        return subType;
    }

    public void setSubType(CodeAttemptEventType subType) {
        this.subType = subType;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    @Override
    public Long getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public String getSubmittedCode() {
        return submittedCode;
    }

    public void setSubmittedCode(String submittedCode) {
        this.submittedCode = submittedCode;
    }

    public String getCorrectCode() {
        return correctCode;
    }

    public void setCorrectCode(String correctCode) {
        this.correctCode = correctCode;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public Double getBonusPoints() {
        return bonusPoints;
    }

    public void setBonusPoints(Double bonusPoints) {
        this.bonusPoints = bonusPoints;
    }

    public Double getPenaltyPoints() {
        return penaltyPoints;
    }

    public void setPenaltyPoints(Double penaltyPoints) {
        this.penaltyPoints = penaltyPoints;
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
     * Типы событий попытки ввода кода
     */
    public enum CodeAttemptEventType {
        CODE_SUBMITTED,
        CODE_VERIFIED,
        CODE_TIMEOUT,
        CODE_INVALID,
        HINT_REQUESTED
    }
}