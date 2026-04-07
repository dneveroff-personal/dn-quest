package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

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
    @Getter
    @Setter
    private CodeAttemptEventType subType;

    @Schema(description = "ID попытки", example = "12345", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Getter
    @Setter
    private Long attemptId;

    @Schema(description = "ID сессии", example = "789", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Getter
    @Setter
    private UUID sessionId;

    @Schema(description = "ID пользователя", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Getter
    @Setter
    private UUID userId;

    @Schema(description = "ID уровня", example = "101")
    @Getter
    @Setter
    private UUID levelId;

    @Schema(description = "Название уровня", example = "Первый этаж")
    @Getter
    @Setter
    private String levelName;

    @Schema(description = "Отправленный код", example = "ABC123", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Getter
    @Setter
    private String submittedCode;

    @Schema(description = "Правильный код", example = "ABC123")
    @Getter
    @Setter
    private String correctCode;

    @Schema(description = "Сектор кода", example = "A", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Getter
    @Setter
    private String sector;

    @Schema(description = "Результат попытки", example = "CORRECT", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Getter
    @Setter
    private String result;

    @Schema(description = "Полученные очки", example = "100.0")
    @Getter
    @Setter
    private Double points;

    @Schema(description = "Бонусные очки", example = "20.0")
    @Getter
    @Setter
    private Double bonusPoints;

    @Schema(description = "Штрафные очки", example = "-10.0")
    @Getter
    @Setter
    private Double penaltyPoints;

    @Schema(description = "Общий счет за попытку", example = "110.0")
    @Getter
    @Setter
    private Double totalScore;

    @Schema(description = "Дополнительные метаданные")
    @Getter
    @Setter
    private Map<String, Object> metadata;

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