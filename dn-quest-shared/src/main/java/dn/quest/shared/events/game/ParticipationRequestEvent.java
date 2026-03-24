package dn.quest.shared.events.game;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

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
    private Long requestId;

    @Schema(description = "ID сессии", example = "789", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    @Schema(description = "ID команды", example = "456", requiredMode = io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
    private Long teamId;

    @Schema(description = "Название команды", example = "Мстители")
    private String teamName;

    @Schema(description = "ID квеста", example = "101")
    private Long questId;

    @Schema(description = "Название квеста", example = "Тайна старого замка")
    private String questName;

    @Schema(description = "Тип запроса", example = "JOIN_REQUEST")
    private String requestType;

    @Schema(description = "Статус запроса", example = "PENDING")
    private String status;

    @Schema(description = "Сообщение от команды", example = "Хотим присоединиться!")
    private String teamMessage;

    @Schema(description = "Ответ организатора", example = "Принято")
    private String organizerResponse;

    @Schema(description = "Время создания запроса")
    private Instant requestedAt;

    @Schema(description = "Время обработки запроса")
    private Instant processedAt;

    @Schema(description = "Дополнительные метаданные")
    private Map<String, Object> metadata;

    // Getters and setters
    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
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
    public Long getTeamId() {
        return teamId;
    }

    @Override
    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    @Override
    public Long getQuestId() {
        return questId;
    }

    @Override
    public void setQuestId(Long questId) {
        this.questId = questId;
    }

    public String getQuestName() {
        return questName;
    }

    public void setQuestName(String questName) {
        this.questName = questName;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTeamMessage() {
        return teamMessage;
    }

    public void setTeamMessage(String teamMessage) {
        this.teamMessage = teamMessage;
    }

    public String getOrganizerResponse() {
        return organizerResponse;
    }

    public void setOrganizerResponse(String organizerResponse) {
        this.organizerResponse = organizerResponse;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}