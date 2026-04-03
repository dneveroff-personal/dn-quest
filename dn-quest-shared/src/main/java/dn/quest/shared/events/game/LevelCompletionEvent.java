package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

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
    @Getter
    @Setter
    private Long levelId;

    @Schema(description = "Название уровня", example = "Первый этаж")
    @Getter
    @Setter
    private String levelName;

    @Schema(description = "Номер уровня", example = "1")
    @Getter
    @Setter
    private Integer levelNumber;

    @Schema(description = "Время завершения в секундах", example = "300")
    @Getter
    @Setter
    private Long completionTimeSeconds;

    @Schema(description = "Количество подсказок", example = "2")
    @Getter
    @Setter
    private Integer hintsUsed;

    @Schema(description = "Количество попыток", example = "5")
    @Getter
    @Setter
    private Integer attemptsCount;

    @Schema(description = "Очки за уровень", example = "500.0")
    @Getter
    @Setter
    private Double levelScore;

    @Schema(description = "Бонусные очки", example = "50.0")
    @Getter
    @Setter
    private Double bonusScore;

    @Schema(description = "Штрафные очки", example = "-20.0")
    @Getter
    @Setter
    private Double penaltyScore;

    @Schema(description = "Общий счет", example = "530.0")
    @Getter
    @Setter
    private Double totalScore;

    @Schema(description = "Время завершения")
    @Getter
    @Setter
    private Instant completedAt;

    @Schema(description = "Дополнительные метаданные")
    @Getter
    @Setter
    private Map<String, Object> metadata;
}