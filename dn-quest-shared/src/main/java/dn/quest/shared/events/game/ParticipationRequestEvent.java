package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Событие запроса на участие
 */
@Schema(description = "Событие запроса на участие")
public class ParticipationRequestEvent extends GameEngineEvent {

    @Schema(description = "Тип события", example = "participation-request-event", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Override
    protected String getEventTypeForSubclass() {
        return "participation-request-event";
    }

    @Schema(description = "ID запроса", example = "12345", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Getter
    @Setter
    private Long requestId;

    @Schema(description = "ID сессии", example = "789", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Getter
    @Setter
    private UUID sessionId;

    @Schema(description = "ID команды", example = "456", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    @Getter
    @Setter
    private UUID teamId;

    @Schema(description = "Название команды", example = "Мстители")
    @Getter
    @Setter
    private String teamName;

    @Schema(description = "ID квеста", example = "101")
    @Getter
    @Setter
    private UUID questId;

    @Schema(description = "Название квеста", example = "Тайна старого замка")
    @Getter
    @Setter
    private String questName;

    @Schema(description = "Тип запроса", example = "JOIN_REQUEST")
    @Getter
    @Setter
    private String requestType;

    @Schema(description = "Статус запроса", example = "PENDING")
    @Getter
    @Setter
    private String status;

    @Schema(description = "Сообщение от команды", example = "Хотим присоединиться!")
    @Getter
    @Setter
    private String teamMessage;

    @Schema(description = "Ответ организатора", example = "Принято")
    @Getter
    @Setter
    private String organizerResponse;

    @Schema(description = "Время создания запроса")
    @Getter
    @Setter
    private Instant requestedAt;

    @Schema(description = "Время обработки запроса")
    @Getter
    @Setter
    private Instant processedAt;

    @Schema(description = "Дополнительные метаданные")
    @Getter
    @Setter
    private Map<String, Object> metadata;
}