package dn.quest.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для ответа валидации токена
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {
    
    private Boolean valid;
    
    private String username;
    
    private UUID userId;
    
    private String role;
    
    private String error;
    
    private Long expiresIn;
}