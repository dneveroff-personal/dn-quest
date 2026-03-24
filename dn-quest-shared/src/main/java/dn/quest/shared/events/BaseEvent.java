package dn.quest.shared.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.Map;

/**
 * Базовый класс для всех событий в системе
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseEvent {

    /**
     * Уникальный идентификатор события
     */
    protected String eventId;

    /**
     * Тип события (например, "user.deleted", "quest.created")
     */
    private String eventType;

    /**
     * Источник события (имя сервиса)
     */
    private String source;

    /**
     * Время создания события
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Версия события
     */
    @Builder.Default
    private String eventVersion = "1.0";

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
}
