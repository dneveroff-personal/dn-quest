package dn.quest.shared.events.game;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие завершения уровня
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LevelCompletedEvent extends BaseEvent {

    /**
     * ID игровой сессии
     */
    private String sessionId;

    /**
     * ID квеста
     */
    private String questId;

    /**
     * ID уровня
     */
    private String levelId;

    /**
     * Номер уровня
     */
    private Integer levelNumber;

    /**
     * Название уровня
     */
    private String levelTitle;

    /**
     * ID участника/игрока
     */
    private String participantId;

    /**
     * Имя участника/игрока
     */
    private String participantName;

    /**
     * ID команды (если командная игра)
     */
    private String teamId;

    /**
     * Название команды (если командная игра)
     */
    private String teamName;

    /**
     * Время начала уровня
     */
    private java.time.Instant levelStartTime;

    /**
     * Время завершения уровня
     */
    private java.time.Instant levelCompletionTime;

    /**
     * Длительность прохождения уровня в секундах
     */
    private Long levelDurationSeconds;

    /**
     * Количество попыток на уровне
     */
    private Integer attemptsCount;

    /**
     * Количество использованных подсказок
     */
    private Integer hintsUsed;

    /**
     * Полученные баллы за уровень
     */
    private Integer levelScore;

    /**
     * Максимальные баллы за уровень
     */
    private Integer maxLevelScore;

    /**
     * Финальный код решения
     */
    private String finalSolutionCode;

    /**
     * Время выполнения финального решения в миллисекундах
     */
    private Long finalExecutionTimeMs;

    /**
     * Использованная память финального решения в байтах
     */
    private Long finalMemoryUsedBytes;

    /**
     * Был ли уровень пройден с первой попытки
     */
    private Boolean firstAttemptSuccess;

    /**
     * IP адрес завершения уровня
     */
    private String completionIp;

    /**
     * User Agent при завершении уровня
     */
    private String userAgent;

    /**
     * Создание события завершения уровня
     */
    public static LevelCompletedEvent create(String sessionId, String questId, String levelId,
                                           Integer levelNumber, String levelTitle,
                                           String participantId, String participantName,
                                           String teamId, String teamName,
                                           java.time.Instant levelStartTime,
                                           java.time.Instant levelCompletionTime,
                                           Long levelDurationSeconds, Integer attemptsCount,
                                           Integer hintsUsed, Integer levelScore,
                                           Integer maxLevelScore, String finalSolutionCode,
                                           Long finalExecutionTimeMs, Long finalMemoryUsedBytes,
                                           Boolean firstAttemptSuccess, String completionIp,
                                           String userAgent, String correlationId) {
        return LevelCompletedEvent.builder()
                .eventType("LevelCompleted")
                .eventVersion("1.0")
                .source("game-engine-service")
                .correlationId(correlationId)
                .sessionId(sessionId)
                .questId(questId)
                .levelId(levelId)
                .levelNumber(levelNumber)
                .levelTitle(levelTitle)
                .participantId(participantId)
                .participantName(participantName)
                .teamId(teamId)
                .teamName(teamName)
                .levelStartTime(levelStartTime)
                .levelCompletionTime(levelCompletionTime)
                .levelDurationSeconds(levelDurationSeconds)
                .attemptsCount(attemptsCount)
                .hintsUsed(hintsUsed)
                .levelScore(levelScore)
                .maxLevelScore(maxLevelScore)
                .finalSolutionCode(finalSolutionCode)
                .finalExecutionTimeMs(finalExecutionTimeMs)
                .finalMemoryUsedBytes(finalMemoryUsedBytes)
                .firstAttemptSuccess(firstAttemptSuccess)
                .completionIp(completionIp)
                .userAgent(userAgent)
                .data(Map.of(
                        "sessionId", sessionId,
                        "questId", questId,
                        "levelId", levelId,
                        "levelNumber", levelNumber,
                        "levelTitle", levelTitle,
                        "participantId", participantId,
                        "participantName", participantName,
                        "teamId", teamId,
                        "teamName", teamName,
                        "levelStartTime", levelStartTime.toString(),
                        "levelCompletionTime", levelCompletionTime.toString(),
                        "levelDurationSeconds", levelDurationSeconds,
                        "attemptsCount", attemptsCount,
                        "hintsUsed", hintsUsed,
                        "levelScore", levelScore,
                        "maxLevelScore", maxLevelScore,
                        "finalSolutionCode", finalSolutionCode,
                        "finalExecutionTimeMs", finalExecutionTimeMs,
                        "finalMemoryUsedBytes", finalMemoryUsedBytes,
                        "firstAttemptSuccess", firstAttemptSuccess,
                        "completionIp", completionIp,
                        "userAgent", userAgent
                ))
                .build();
    }
}