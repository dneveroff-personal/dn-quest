package dn.quest.shared.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Утилитарный класс для работы с JWT токенами
 */
@Slf4j
public final class JwtUtil {
    
    private static final String ISSUER = "dn-quest";
    private static final long DEFAULT_ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000; // 15 минут
    private static final long DEFAULT_REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 дней
    
    private JwtUtil() {
        // Утилитарный класс
    }
    
    /**
     * Генерирует секретный ключ из строки
     */
    public static SecretKey getSigningKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Генерирует access токен
     */
    public static String generateAccessToken(String secret, String subject, Map<String, Object> claims) {
        return generateToken(secret, subject, claims, DEFAULT_ACCESS_TOKEN_EXPIRATION);
    }
    
    /**
     * Генерирует access токен с указанным временем жизни
     */
    public static String generateAccessToken(String secret, String subject, Map<String, Object> claims, long expiration) {
        return generateToken(secret, subject, claims, expiration);
    }
    
    /**
     * Генерирует refresh токен
     */
    public static String generateRefreshToken(String secret, String subject) {
        return generateToken(secret, subject, Map.of(), DEFAULT_REFRESH_TOKEN_EXPIRATION);
    }
    
    /**
     * Генерирует refresh токен с указанным временем жизни
     */
    public static String generateRefreshToken(String secret, String subject, long expiration) {
        return generateToken(secret, subject, Map.of(), expiration);
    }
    
    /**
     * Генерирует JWT токен
     */
    public static String generateToken(String secret, String subject, Map<String, Object> claims, long expiration) {
        try {
            SecretKey signingKey = getSigningKey(secret);
            LocalDateTime now = LocalDateTime.now();
            Date issuedAt = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
            Date expirationDate = Date.from(now.plusSeconds(expiration / 1000).atZone(ZoneId.systemDefault()).toInstant());
            
            JwtBuilder builder = Jwts.builder()
                    .subject(subject)
                    .issuer(ISSUER)
                    .issuedAt(issuedAt)
                    .expiration(expirationDate)
                    .signWith(signingKey);
            
            if (claims != null && !claims.isEmpty()) {
                builder.claims(claims);
            }
            
            return builder.compact();
        } catch (Exception e) {
            log.error("Error generating JWT token", e);
            throw new JwtException("Failed to generate JWT token: " + e.getMessage());
        }
    }
    
    /**
     * Извлекает subject из токена
     */
    public static String extractSubject(String token, String secret) {
        return extractClaim(token, secret, Claims::getSubject);
    }
    
    /**
     * Извлекает claim из токена
     */
    public static <T> T extractClaim(String token, String secret, Function<Claims, T> claimsResolver) {
        try {
            Claims claims = extractAllClaims(token, secret);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            log.error("Error extracting claim from token", e);
            throw new JwtException("Failed to extract claim: " + e.getMessage());
        }
    }
    
    /**
     * Извлекает все claims из токена
     */
    public static Claims extractAllClaims(String token, String secret) {
        try {
            SecretKey signingKey = getSigningKey(secret);
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token expired", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtException("Unsupported token", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("Malformed token", e);
        } catch (SecurityException e) {
            throw new JwtException("Invalid token signature", e);
        } catch (IllegalArgumentException e) {
            throw new JwtException("Invalid token", e);
        }
    }
    
    /**
     * Проверяет валидность токена
     */
    public static boolean isTokenValid(String token, String secret) {
        try {
            return !isTokenExpired(token, secret);
        } catch (Exception e) {
            log.debug("Token validation failed", e);
            return false;
        }
    }

    /**
     * Проверяет валидность токена
     */
    public static boolean isTokenValid(String token, String secret, String expectedSubject) {
        try {
            String subject = extractSubject(token, secret);
            return subject.equals(expectedSubject) && !isTokenExpired(token, secret);
        } catch (Exception e) {
            log.debug("Token validation failed", e);
            return false;
        }
    }
    
    /**
     * Проверяет, истек ли срок действия токена
     */
    public static boolean isTokenExpired(String token, String secret) {
        try {
            Date expiration = extractClaim(token, secret, Claims::getExpiration);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Получает время истечения токена
     */
    public static Date getExpirationDate(String token, String secret) {
        return extractClaim(token, secret, Claims::getExpiration);
    }
    
    /**
     * Получает время выдачи токена
     */
    public static Date getIssuedAtDate(String token, String secret) {
        return extractClaim(token, secret, Claims::getIssuedAt);
    }
    
    /**
     * Получает издателя токена
     */
    public static String getIssuer(String token, String secret) {
        return extractClaim(token, secret, Claims::getIssuer);
    }
    
    /**
     * Получает кастомный claim из токена
     */
    public static Object getCustomClaim(String token, String secret, String claimName) {
        return extractClaim(token, secret, claims -> claims.get(claimName));
    }
    
    /**
     * Получает кастомный claim с указанным типом
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCustomClaim(String token, String secret, String claimName, Class<T> type) {
        Object claim = getCustomClaim(token, secret, claimName);
        if (claim != null && type.isInstance(claim)) {
            return (T) claim;
        }
        return null;
    }
    
    /**
     * Обновляет access токен используя refresh токен
     */
    public static String refreshAccessToken(String refreshToken, String secret, String subject, Map<String, Object> claims) {
        if (!isTokenValid(refreshToken, secret, subject)) {
            throw new JwtException("Invalid refresh token");
        }
        return generateAccessToken(secret, subject, claims);
    }
}