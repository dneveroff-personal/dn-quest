package dn.quest.usermanagement.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Базовый класс для всех событий в User Management Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent {
    
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    
    private String eventType;
    
    private Object data;
    
    private String correlationId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    private String source = "user-management-service";
    
    private String version = "1.0";
    
    private Map<String, Object> metadata;
}