package dn.quest.shared.events.game;

import dn.quest.shared.events.BaseEvent;
import dn.quest.shared.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие завершения игровой сессии
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GameSessionFinishedEvent extends BaseEvent {

    /**
     * ID игровой сессии
     */
    private String sessionId;

    /**
     * ID квеста
     */
    private String questId;

    /**
     * Название квеста
     */
    private String questTitle;

    /**
     * ID команды (если командная игра)
     */
    private String teamId;

    /**
     * Название команды (если командная игра)
     */
    private String teamName;

    /**
     * ID участника/игрока
     */
    private String participantId;

    /**
     * Имя участника/игрока
     */
    private String participantName;

    /**
     * Время начала сессии
     */
    private java.time.Instant startTime;

    /**
     * Время завершения сессии
     */
    private java.time.Instant endTime;

    /**
     * Длительность сессии в секундах
     */
    private Long durationSeconds;

    /**
     * Финальный статус сессии
     */
    private SessionStatus finalStatus;

    /**
     * Количество пройденных уровней
     */
    private Integer completedLevels;

    /**
     * Общее количество уровней
     */
    private Integer totalLevels;

    /**
     * Финальный счет
     */
    private Integer finalScore;

    /**
     * Количество попыток кода
     */
    private Integer totalCodeAttempts;

    /**
     * Количество подсказок использовано
     */
    private Integer hintsUsed;

    /**
     * Причина завершения (completed, timeout, abandoned, etc.)
     */
    private String completionReason;

    /**
     * IP адрес завершения
     */
    private String finishIp;

    /**
     * User Agent при завершении
     */
    private String userAgent;

    /**
     * Создание события завершения игровой сессии
     */
    public static GameSessionFinishedEvent create(String sessionId, String questId, String questTitle,
                                                 String teamId, String teamName, String participantId,
                                                 String participantName, java.time.Instant startTime,
                                                 java.time.Instant endTime, Long durationSeconds,
                                                 SessionStatus finalStatus, Integer completedLevels,
                                                 Integer totalLevels, Integer finalScore,
                                                 Integer totalCodeAttempts, Integer hintsUsed,
                                                 String completionReason, String finishIp,
                                                 String userAgent, String correlationId) {
        return GameSessionFinishedEvent.builder()
                .eventType("GameSessionFinished")
                .eventVersion("1.0")
                .source("game-engine-service")
                .correlationId(correlationId)
                .sessionId(sessionId)
                .questId(questId)
                .questTitle(questTitle)
                .teamId(teamId)
                .teamName(teamName)
                .participantId(participantId)
                .participantName(participantName)
                .startTime(startTime)
                .endTime(endTime)
                .durationSeconds(durationSeconds)
                .finalStatus(finalStatus)
                .completedLevels(completedLevels)
                .totalLevels(totalLevels)
                .finalScore(finalScore)
                .totalCodeAttempts(totalCodeAttempts)
                .hintsUsed(hintsUsed)
                .completionReason(completionReason)
                .finishIp(finishIp)
                .userAgent(userAgent)
                .data(Map.of(
                        "sessionId", sessionId,
                        "questId", questId,
                        "questTitle", questTitle,
                        "teamId", teamId,
                        "teamName", teamName,
                        "participantId", participantId,
                        "participantName", participantName,
                        "startTime", startTime.toString(),
                        "endTime", endTime.toString(),
                        "durationSeconds", durationSeconds,
                        "finalStatus", finalStatus.name(),
                        "completedLevels", completedLevels,
                        "totalLevels", totalLevels,
                        "finalScore", finalScore,
                        "totalCodeAttempts", totalCodeAttempts,
                        "hintsUsed", hintsUsed,
                        "completionReason", completionReason,
                        "finishIp", finishIp,
                        "userAgent", userAgent
                ))
                .build();
    }
}