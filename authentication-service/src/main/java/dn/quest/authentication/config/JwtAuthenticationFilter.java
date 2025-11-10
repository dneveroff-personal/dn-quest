package dn.quest.authentication.config;

import dn.quest.authentication.service.AuthService;
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
 * JWT фильтр аутентификации
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;

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
                
                if (authService.validateToken(token)) {
                    final String username = authService.extractUsername(token);
                    
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
        return requestURI.equals("/api/auth/register") ||
               requestURI.equals("/api/auth/login") ||
               requestURI.equals("/api/auth/refresh") ||
               requestURI.equals("/api/auth/forgot-password") ||
               requestURI.equals("/api/auth/reset-password") ||
               requestURI.equals("/api/auth/validate") ||
               requestURI.startsWith("/api-docs") ||
               requestURI.startsWith("/swagger-ui") ||
               requestURI.startsWith("/v3/api-docs") ||
               requestURI.equals("/actuator/health") ||
               requestURI.equals("/actuator/info");
    }

    /**
     * Извлекает роли из токена
     */
    private List<SimpleGrantedAuthority> getAuthorities(String token) {
        try {
            // TODO: Извлечь роли из токена или получить из базы данных
            // Временно возвращаем базовую роль
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
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