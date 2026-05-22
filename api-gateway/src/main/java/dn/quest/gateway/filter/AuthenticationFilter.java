package dn.quest.gateway.filter;

import dn.quest.shared.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * WebFilter для добавления заголовков пользователя после JWT аутентификации.
 * Работает после Spring Security, которая уже проверила токен.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class AuthenticationFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Пропускаем публичные эндпоинты
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        // Получаем токен из заголовка
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);
        
        // Извлекаем информацию из токена для заголовков
        String username = JwtUtil.extractUsername(token, jwtSecret);
        UUID userId = JwtUtil.extractUserId(token, jwtSecret);
        String role = JwtUtil.extractRole(token, jwtSecret);

        if (username != null) {
            // Добавляем заголовки для downstream сервисов
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Username", username)
                    .header("X-User-Id", userId != null ? userId.toString() : "")
                    .header("X-User-Role", role != null ? role : "USER")
                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            return chain.filter(modifiedExchange);
        }

        return chain.filter(exchange);
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/api/auth/forgot-password") ||
               path.startsWith("/api/auth/reset-password") ||
               path.startsWith("/api/auth/validate") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/info") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/swagger-ui.html") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator/gateway-health");
    }
}