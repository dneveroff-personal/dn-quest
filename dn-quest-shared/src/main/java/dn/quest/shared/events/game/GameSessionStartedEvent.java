package dn.quest.shared.events.game;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие начала игровой сессии
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GameSessionStartedEvent extends BaseEvent {

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
     * Тип сессии (individual, team)
     */
    private String sessionType;

    /**
     * Текущий уровень
     */
    private Integer currentLevel;

    /**
     * Общее количество уровней
     */
    private Integer totalLevels;

    /**
     * IP адрес начала сессии
     */
    private String startIp;

    /**
     * User Agent при начале сессии
     */
    private String userAgent;

    /**
     * Создание события начала игровой сессии
     */
    public static GameSessionStartedEvent create(String sessionId, String questId, String questTitle,
                                               String teamId, String teamName, String participantId,
                                               String participantName, java.time.Instant startTime,
                                               String sessionType, Integer currentLevel,
                                               Integer totalLevels, String startIp,
                                               String userAgent, String correlationId) {
        return GameSessionStartedEvent.builder()
                .eventType("GameSessionStarted")
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
                .sessionType(sessionType)
                .currentLevel(currentLevel)
                .totalLevels(totalLevels)
                .startIp(startIp)
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
                        "sessionType", sessionType,
                        "currentLevel", currentLevel,
                        "totalLevels", totalLevels,
                        "startIp", startIp,
                        "userAgent", userAgent
                ))
                .build();
    }
}