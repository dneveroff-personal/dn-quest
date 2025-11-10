package dn.quest.shared.dto.auth;

import dn.quest.shared.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для ответа при успешном входе в систему
 */
@Data
public class LoginResponseDTO {
    
    /**
     * Access токен
     */
    private String accessToken;
    
    /**
     * Refresh токен
     */
    private String refreshToken;
    
    /**
     * Тип токена (обычно "Bearer")
     */
    private String tokenType = "Bearer";
    
    /**
     * Время жизни access токена в секундах
     */
    private Long expiresIn;
    
    /**
     * Время жизни refresh токена в секундах
     */
    private Long refreshExpiresIn;
    
    /**
     * Информация о пользователе
     */
    private UserDTO user;
    
    /**
     * Время выдачи токенов
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issuedAt;
    
    /**
     * Время истечения access токена
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime accessTokenExpiresAt;
    
    /**
     * Время истечения refresh токена
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refreshTokenExpiresAt;
}