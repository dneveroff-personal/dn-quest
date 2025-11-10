package dn.quest.shared.events.game;

import dn.quest.shared.events.BaseEvent;
import dn.quest.shared.enums.AttemptResult;
import dn.quest.shared.enums.CodeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие отправки кода для проверки
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CodeSubmittedEvent extends BaseEvent {

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
     * Отправленный код
     */
    private String submittedCode;

    /**
     * Тип кода
     */
    private CodeType codeType;

    /**
     * Результат проверки
     */
    private AttemptResult result;

    /**
     * Время отправки
     */
    private java.time.Instant submissionTime;

    /**
     * Время выполнения проверки в миллисекундах
     */
    private Long executionTimeMs;

    /**
     * Использованная память в байтах
     */
    private Long memoryUsedBytes;

    /**
     * Количество попыток для этого уровня
     */
    private Integer attemptNumber;

    /**
     * Общее количество попыток в сессии
     */
    private Integer totalAttempts;

    /**
     * Сообщение об ошибке (если есть)
     */
    private String errorMessage;

    /**
     * Тестовые данные (если есть)
     */
    private String testData;

    /**
     * Ожидаемый результат
     */
    private String expectedResult;

    /**
     * Фактический результат
     */
    private String actualResult;

    /**
     * IP адрес отправки
     */
    private String submissionIp;

    /**
     * User Agent при отправке
     */
    private String userAgent;

    /**
     * Создание события отправки кода
     */
    public static CodeSubmittedEvent create(String sessionId, String questId, String levelId,
                                          Integer levelNumber, String participantId,
                                          String participantName, String teamId, String teamName,
                                          String submittedCode, CodeType codeType,
                                          AttemptResult result, java.time.Instant submissionTime,
                                          Long executionTimeMs, Long memoryUsedBytes,
                                          Integer attemptNumber, Integer totalAttempts,
                                          String errorMessage, String testData,
                                          String expectedResult, String actualResult,
                                          String submissionIp, String userAgent,
                                          String correlationId) {
        return CodeSubmittedEvent.builder()
                .eventType("CodeSubmitted")
                .eventVersion("1.0")
                .source("game-engine-service")
                .correlationId(correlationId)
                .sessionId(sessionId)
                .questId(questId)
                .levelId(levelId)
                .levelNumber(levelNumber)
                .participantId(participantId)
                .participantName(participantName)
                .teamId(teamId)
                .teamName(teamName)
                .submittedCode(submittedCode)
                .codeType(codeType)
                .result(result)
                .submissionTime(submissionTime)
                .executionTimeMs(executionTimeMs)
                .memoryUsedBytes(memoryUsedBytes)
                .attemptNumber(attemptNumber)
                .totalAttempts(totalAttempts)
                .errorMessage(errorMessage)
                .testData(testData)
                .expectedResult(expectedResult)
                .actualResult(actualResult)
                .submissionIp(submissionIp)
                .userAgent(userAgent)
                .data(Map.of(
                        "sessionId", sessionId,
                        "questId", questId,
                        "levelId", levelId,
                        "levelNumber", levelNumber,
                        "participantId", participantId,
                        "participantName", participantName,
                        "teamId", teamId,
                        "teamName", teamName,
                        "submittedCode", submittedCode,
                        "codeType", codeType.name(),
                        "result", result.name(),
                        "submissionTime", submissionTime.toString(),
                        "executionTimeMs", executionTimeMs,
                        "memoryUsedBytes", memoryUsedBytes,
                        "attemptNumber", attemptNumber,
                        "totalAttempts", totalAttempts,
                        "errorMessage", errorMessage,
                        "testData", testData,
                        "expectedResult", expectedResult,
                        "actualResult", actualResult,
                        "submissionIp", submissionIp,
                        "userAgent", userAgent
                ))
                .build();
    }
}