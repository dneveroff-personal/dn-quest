package dn.quest.shared.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
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
    private String eventId;

    /**
     * Тип события
     */
    private String eventType;

    /**
     * Версия события
     */
    private String eventVersion;

    /**
     * Время создания события
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    /**
     * Источник события (название сервиса)
     */
    private String source;

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
    private Map<String, Object> data;

    /**
     * Метаданные события
     */
    private EventMetadata metadata;

    /**
     * Вложенный класс для метаданных
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EventMetadata {

        /**
         * ID пользователя
         */
        private String userId;

        /**
         * ID сессии
         */
        private String sessionId;

        /**
         * IP адрес
         */
        private String ipAddress;

        /**
         * User Agent
         */
        private String userAgent;

        /**
         * Дополнительные метаданные
         */
        private Map<String, Object> additional;
    }
}