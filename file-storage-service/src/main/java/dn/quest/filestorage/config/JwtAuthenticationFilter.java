package dn.quest.filestorage.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT фильтр аутентификации для File Storage Service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();
        final String clientIp = getClientIp(request);

        // Пропускаем публичные эндпоинты
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                final String token = authHeader.substring(7);
                
                if (validateToken(token)) {
                    final String username = extractUsername(token);
                    
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // Создаем аутентификацию с ролями
                        List<SimpleGrantedAuthority> authorities = getAuthorities(token);
                        
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                username, null, authorities);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        // Добавляем username в заголовки для использования в контроллерах
                        response.setHeader("X-Username", username);
                        
                        log.debug("Пользователь успешно аутентифицирован: {} для запроса: {} с IP: {}",
                                username, requestURI, clientIp);
                    }
                } else {
                    log.warn("Неверный JWT токен для запроса: {} с IP: {}", requestURI, clientIp);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка обработки JWT токена для запроса: {} с IP: {}", requestURI, clientIp, e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Проверяет, является ли эндпоинт публичным
     */
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/api/files/download/") ||
               requestURI.startsWith("/api/files/public/") ||
               requestURI.startsWith("/api-docs") ||
               requestURI.startsWith("/swagger-ui") ||
               requestURI.startsWith("/v3/api-docs") ||
               requestURI.equals("/actuator/health") ||
               requestURI.equals("/actuator/info") ||
               requestURI.equals("/actuator/metrics");
    }

    /**
     * Валидация токена через Authentication Service
     */
    private boolean validateToken(String token) {
        try {
            return authServiceClient.validateToken(token);
        } catch (Exception e) {
            log.debug("Ошибка валидации токена", e);
            return false;
        }
    }

    /**
     * Извлечение username из токена
     */
    private String extractUsername(String token) {
        try {
            return authServiceClient.extractUsername(token);
        } catch (Exception e) {
            log.debug("Ошибка извлечения username из токена", e);
            return null;
        }
    }

    /**
     * Извлекает роли из токена
     */
    private List<SimpleGrantedAuthority> getAuthorities(String token) {
        try {
            var userResponse = authServiceClient.getUserByToken(token);
            return userResponse.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        } catch (Exception e) {
            log.debug("Ошибка извлечения ролей из токена", e);
            return List.of();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}