package dn.quest.teammanagement.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Базовый класс для всех событий в системе
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    
    /**
     * Уникальный идентификатор события
     */
    private String eventId;
    
    /**
     * Время создания события
     */
    private Instant timestamp;
    
    /**
     * Тип события
     */
    private String eventType;
    
    /**
     * Версия события
     */
    private String version;
    
    /**
     * ID пользователя, инициировавшего событие
     */
    private Long userId;
    
    /**
     * ID сервиса, сгенерировавшего событие
     */
    private String serviceId;
    
    /**
     * Дополнительные метаданные
     */
    private String metadata;
    
    protected BaseEvent(String eventType, Long userId) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.eventType = eventType;
        this.version = "1.0";
        this.userId = userId;
        this.serviceId = "team-management-service";
    }
}