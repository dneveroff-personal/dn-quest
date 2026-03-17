package dn.quest.gameengine.event;

import dn.quest.gameengine.entity.enums.AttemptResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Событие связанное с попыткой ввода кода
 */
@Schema(description = "Событие попытки ввода кода")
public class CodeAttemptEvent extends GameEngineEvent {

    @Schema(description = "Тип события", example = "code-attempt-event", required = true)
    @Override
    protected String getEventType() {
        return "code-attempt-event";
    }

    @Schema(description = "Подтип события", example = "CODE_SUBMITTED", required = true)
    private CodeAttemptEventType subType;

    @Schema(description = "ID попытки", example = "12345", required = true)
    private Long attemptId;

    @Schema(description = "ID сессии", example = "789", required = true)
    private Long sessionId;

    @Schema(description = "ID пользователя", example = "123", required = true)
    private Long userId;

    @Schema(description = "ID уровня", example = "101")
    private Long levelId;

    @Schema(description = "Название уровня", example = "Первый этаж")
    private String levelName;

    @Schema(description = "Отправленный код", example = "ABC123", required = true)
    private String submittedCode;

    @Schema(description = "Правильный код", example = "ABC123")
    private String correctCode;

    @Schema(description = "Сектор кода", example = "A", required = true)
    private String sector;

    @Schema(description = "Результат попытки", example = "CORRECT", required = true)
    private AttemptResult result;

    @Schema(description = "Полученные очки", example = "100.0")
    private Double points;

    @Schema(description = "Бонусные очки", example = "20.0")
    private Double bonusPoints;

    @Schema(description = "Штрафные очки", example = "-10.0")
    private Double penaltyPoints;

    @Schema(description = "Общий счет за попытку", example = "110.0")
    private Double totalScore;

    @Schema(description = "Номер попытки", example = "3")
    private Integer attemptNumber;

    @Schema(description = "Время затраченное на попытку в секундах", example = "45.5")
    private Double timeSpentSeconds;

    @Schema(description = "Тип кода", example = "MAIN")
    private String codeType;

    @Schema(description = "Был ли код бонусным", example = "false")
    private Boolean isBonus;

    @Schema(description = "Множитель сектора", example = "1.5")
    private Double sectorMultiplier;

    @Schema(description = "Множитель сложности", example = "1.2")
    private Double difficultyMultiplier;

    @Schema(description = "IP адрес пользователя", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "User agent", example = "Mozilla/5.0...")
    private String userAgent;

    @Schema(description = "Координаты (X)", example = "100.5")
    private Double coordinateX;

    @Schema(description = "Координаты (Y)", example = "200.3")
    private Double coordinateY;

    @Schema(description = "Дополнительные метаданные попытки")
    private Map<String, Object> attemptData;

    // Конструкторы

    public CodeAttemptEvent() {
        super();
    }

    public CodeAttemptEvent(Long sessionId, Long userId, Long teamId, Long questId, CodeAttemptEventType subType) {
        super(sessionId, userId, teamId, questId);
        this.sessionId = sessionId;
        this.userId = userId;
        this.subType = subType;
    }

    // Статические фабричные методы

    public static CodeAttemptEvent codeSubmitted(Long sessionId, Long userId, Long teamId, Long questId, 
                                               Long attemptId, String submittedCode, String sector) {
        CodeAttemptEvent event = new CodeAttemptEvent(sessionId, userId, teamId, questId, CodeAttemptEventType.CODE_SUBMITTED);
        event.setAttemptId(attemptId);
        event.setSubmittedCode(submittedCode);
        event.setSector(sector);
        event.addMetadata("action", "submit");
        return event;
    }

    public static CodeAttemptEvent codeCorrect(Long sessionId, Long userId, Long teamId, Long questId,
                                             Long attemptId, String submittedCode, String correctCode, 
                                             String sector, Double points) {
        CodeAttemptEvent event = new CodeAttemptEvent(sessionId, userId, teamId, questId, CodeAttemptEventType.CODE_CORRECT);
        event.setAttemptId(attemptId);
        event.setSubmittedCode(submittedCode);
        event.setCorrectCode(correctCode);
        event.setSector(sector);
        event.setResult(AttemptResult.CORRECT);
        event.setPoints(points);
        event.addMetadata("action", "correct");
        return event;
    }

    public static CodeAttemptEvent codeIncorrect(Long sessionId, Long userId, Long teamId, Long questId,
                                               Long attemptId, String submittedCode, String sector) {
        CodeAttemptEvent event = new CodeAttemptEvent(sessionId, userId, teamId, questId, CodeAttemptEventType.CODE_INCORRECT);
        event.setAttemptId(attemptId);
        event.setSubmittedCode(submittedCode);
        event.setSector(sector);
        event.setResult(AttemptResult.INCORRECT);
        event.addMetadata("action", "incorrect");
        return event;
    }

    public static CodeAttemptEvent bonusApplied(Long sessionId, Long userId, Long teamId, Long questId,
                                             Long attemptId, Double bonusAmount, String reason) {
        CodeAttemptEvent event = new CodeAttemptEvent(sessionId, userId, teamId, questId, CodeAttemptEventType.BONUS_APPLIED);
        event.setAttemptId(attemptId);
        event.setBonusPoints(bonusAmount);
        event.addMetadata("action", "bonus");
        event.addMetadata("bonusReason", reason);
        return event;
    }

    public static CodeAttemptEvent penaltyApplied(Long sessionId, Long userId, Long teamId, Long questId,
                                               Long attemptId, Double penaltyAmount, String reason) {
        CodeAttemptEvent event = new CodeAttemptEvent(sessionId, userId, teamId, questId, CodeAttemptEventType.PENALTY_APPLIED);
        event.setAttemptId(attemptId);
        event.setPenaltyPoints(penaltyAmount);
        event.addMetadata("action", "penalty");
        event.addMetadata("penaltyReason", reason);
        return event;
    }

    public static CodeAttemptEvent attemptLimitReached(Long sessionId, Long userId, Long teamId, Long questId,
                                                    Long levelId, String sector) {
        CodeAttemptEvent event = new CodeAttemptEvent(sessionId, userId, teamId, questId, CodeAttemptEventType.ATTEMPT_LIMIT_REACHED);
        event.setLevelId(levelId);
        event.setSector(sector);
        event.addMetadata("action", "limit_reached");
        return event;
    }

    public static CodeAttemptEvent cooldownStarted(Long sessionId, Long userId, Long teamId, Long questId,
                                                 Long levelId, String sector, Long cooldownSeconds) {
        CodeAttemptEvent event = new CodeAttemptEvent(sessionId, userId, teamId, questId, CodeAttemptEventType.COOLDOWN_STARTED);
        event.setLevelId(levelId);
        event.setSector(sector);
        event.addMetadata("action", "cooldown_started");
        event.addMetadata("cooldownSeconds", cooldownSeconds);
        return event;
    }

    // Getters and Setters

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

    public AttemptResult getResult() {
        return result;
    }

    public void setResult(AttemptResult result) {
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

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public Double getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public void setTimeSpentSeconds(Double timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public Boolean getIsBonus() {
        return isBonus;
    }

    public void setIsBonus(Boolean isBonus) {
        this.isBonus = isBonus;
    }

    public Double getSectorMultiplier() {
        return sectorMultiplier;
    }

    public void setSectorMultiplier(Double sectorMultiplier) {
        this.sectorMultiplier = sectorMultiplier;
    }

    public Double getDifficultyMultiplier() {
        return difficultyMultiplier;
    }

    public void setDifficultyMultiplier(Double difficultyMultiplier) {
        this.difficultyMultiplier = difficultyMultiplier;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Double getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(Double coordinateX) {
        this.coordinateX = coordinateX;
    }

    public Double getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(Double coordinateY) {
        this.coordinateY = coordinateY;
    }

    public Map<String, Object> getAttemptData() {
        return attemptData;
    }

    public void setAttemptData(Map<String, Object> attemptData) {
        this.attemptData = attemptData;
    }

    /**
     * Перечисление типов событий попыток кода
     */
    public enum CodeAttemptEventType {
        CODE_SUBMITTED("Код отправлен"),
        CODE_CORRECT("Код правильный"),
        CODE_INCORRECT("Код неправильный"),
        BONUS_APPLIED("Бонус применен"),
        PENALTY_APPLIED("Штраф применен"),
        ATTEMPT_LIMIT_REACHED("Лимит попыток достигнут"),
        COOLDOWN_STARTED("Охлаждение начато"),
        ATTEMPT_EXPIRED("Попытка истекла"),
        HINT_USED("Подсказка использована"),
        SECTOR_COMPLETED("Сектор завершен");

        private final String description;

        CodeAttemptEventType(String description) {
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
               attemptId != null && 
               sessionId != null && 
               userId != null && 
               submittedCode != null && 
               sector != null;
    }
}