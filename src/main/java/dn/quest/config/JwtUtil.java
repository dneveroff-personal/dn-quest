package dn.quest.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Утилитарный класс для работы с JWT токенами
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;
    /**
     * Срок токена аутентификации
     */
    private final long expirationMs;

    public JwtUtil() {
        long expirationMs1;
        // Используем секрет из переменных окружения или генерируем новый
        String secret = System.getenv(ApplicationConstants.JWT_SECRET_PROPERTY);
        if (secret == null || secret.isEmpty()) {
            log.warn("JWT_SECRET не указан, генерируем случайный ключ");
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            this.key = Keys.hmacShaKeyFor(secret.getBytes());
            log.info("JWT ключ загружен из переменных окружения");
        }
        
        // Используем время истечения из переменных окружения или по умолчанию
        String expiration = System.getenv(ApplicationConstants.JWT_EXPIRATION_PROPERTY);
        if (expiration != null && !expiration.isEmpty()) {
            try {
                expirationMs1 = Long.parseLong(expiration);
                log.info("JWT время истечения загружено из переменных окружения: {} мс", expirationMs1);
            } catch (NumberFormatException e) {
                log.warn("Неверный формат JWT_EXPIRATION_MS, используем значение по умолчанию");
                expirationMs1 = ApplicationConstants.DEFAULT_JWT_EXPIRATION_MS;
            }
        } else {
            expirationMs1 = ApplicationConstants.DEFAULT_JWT_EXPIRATION_MS;
            log.info("Используем время истечения JWT по умолчанию: {} мс", expirationMs1);
        }
        this.expirationMs = expirationMs1;
    }

    /**
     * Генерирует JWT токен для пользователя
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Извлекает имя пользователя из JWT токена
     */
    public String extractUsername(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException e) {
            log.debug("Ошибка извлечения имени пользователя из токена: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Валидирует JWT токен
     */
    public boolean validateToken(String token) {
        try {
            extractUsername(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT токен невалиден: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, истек ли срок действия токена
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true; // Если токен невалиден, считаем его истекшим
        }
    }

    /**
     * Возвращает время истечения токена в миллисекундах
     */
    public long getExpirationMs() {
        return expirationMs;
    }
}
