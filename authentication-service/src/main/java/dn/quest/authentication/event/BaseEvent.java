package dn.quest.authentication.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Базовый класс для всех событий системы
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseEvent {

    /**
     * Уникальный идентификатор события
     */
    private String eventId;

    /**
     * Тип события
     */
    private String eventType;

    /**
     * Версия события
     */
    private String eventVersion = "1.0";

    /**
     * Время создания события
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    /**
     * Источник события (название сервиса)
     */
    private String source = "authentication-service";

    /**
     * Идентификатор корреляции для трассировки
     */
    private String correlationId;

    /**
     * Идентификатор причинно-следственной связи
     */
    private String causationId;

    /**
     * Данные события
     */
    private Object data;

    /**
     * Метаданные события
     */
    private Map<String, Object> metadata;

    public BaseEvent(String eventType, Object data) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = Instant.now();
        this.data = data;
    }

    public BaseEvent(String eventType, Object data, String correlationId) {
        this(eventType, data);
        this.correlationId = correlationId;
    }
}