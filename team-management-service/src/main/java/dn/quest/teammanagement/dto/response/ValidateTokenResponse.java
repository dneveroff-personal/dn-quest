package dn.quest.teammanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO для ответа валидации токена
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponse {
    
    /**
     * Результат валидации токена
     */
    private Boolean valid;
    
    /**
     * ID пользователя
     */
    private UUID userId;
    
    /**
     * Имя пользователя
     */
    private String username;
    
    /**
     * Email пользователя
     */
    private String email;
    
    /**
     * Роли пользователя
     */
    private List<String> roles;
    
    /**
     * Разрешения пользователя
     */
    private List<String> permissions;
    
    /**
     * Время истечения токена
     */
    private Instant expiresAt;
    
    /**
     * Время выпуска токена
     */
    private Instant issuedAt;
    
    /**
     * Является ли токен обновления
     */
    private Boolean refreshToken;
    
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
     * Сообщение об ошибке (если токен невалиден)
     */
    private String errorMessage;
    
    /**
     * Код ошибки
     */
    private String errorCode;
    
    /**
     * Дополнительные метаданные
     */
    private String metadata;
}