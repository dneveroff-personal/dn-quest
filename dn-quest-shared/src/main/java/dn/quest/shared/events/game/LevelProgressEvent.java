package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Событие прогресса уровня
 */
@Schema(description = "Событие прогресса уровня")
public class LevelProgressEvent extends GameEngineEvent {

    @Schema(description = "Тип события", example = "level-progress-event", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Override
    protected String getEventTypeForSubclass() {
        return "level-progress-event";
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

    @Schema(description = "Прогресс в процентах", example = "75")
    @Getter
    @Setter
    private Integer progressPercent;

    @Schema(description = "Текущая позиция", example = "3")
    @Getter
    @Setter
    private Integer currentPosition;

    @Schema(description = "Общее количество позиций", example = "10")
    @Getter
    @Setter
    private Integer totalPositions;

    @Schema(description = "Количество подсказок", example = "2")
    @Getter
    @Setter
    private Integer hintsUsed;

    @Schema(description = "Очки за уровень", example = "500.0")
    @Getter
    @Setter
    private Double levelScore;

    @Schema(description = "Дополнительные метаданные")
    @Getter
    @Setter
    private Map<String, Object> metadata;
}