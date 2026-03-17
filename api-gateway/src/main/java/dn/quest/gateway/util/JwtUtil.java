package dn.quest.gateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Утилиты для работы с JWT токенами
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Извлечение имени пользователя из токена
     */
    public String extractUsername(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            log.debug("Ошибка извлечения имени пользователя из токена: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Извлечение времени истечения токена
     */
    public Date extractExpiration(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
        } catch (Exception e) {
            log.debug("Ошибка извлечения времени истечения токена: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Проверка валидности токена
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.warn("Неверная подпись JWT токена: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Некорректный JWT токен: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT токен истек: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Неподдерживаемый JWT токен: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Пустой JWT токен: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Проверка истечения токена
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            log.debug("Ошибка проверки истечения токена: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Извлечение claims из токена
     */
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.debug("Ошибка извлечения claims из токена: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Извлечение роли пользователя из токена
     */
    public String extractRole(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims != null ? claims.get("role", String.class) : null;
        } catch (Exception e) {
            log.debug("Ошибка извлечения роли из токена: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Извлечение ID пользователя из токена
     */
    public Long extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims != null ? claims.get("userId", Long.class) : null;
        } catch (Exception e) {
            log.debug("Ошибка извлечения ID пользователя из токена: {}", e.getMessage());
            return null;
        }
    }
}