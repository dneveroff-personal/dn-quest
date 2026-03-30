package dn.quest.gateway.filter;

import dn.quest.gateway.client.AuthenticationServiceClient;
import dn.quest.shared.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Фильтр аутентификации для проверки JWT токенов
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements GatewayFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final AuthenticationServiceClient authenticationServiceClient;
    
    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/validate",
            "/actuator/health",
            "/actuator/info",
            "/api-docs",
            "/swagger-ui.html",
            "/swagger-ui",
            "/v3/api-docs"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Пропускаем исключенные пути
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        // Проверяем наличие Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Отсутствует или некорректный Authorization header для пути: {}", path);
            return handleUnauthorized(exchange);
        }

        String token = authHeader.substring(7);

        // Сначала выполняем локальную валидацию токена
        if (!JwtUtil.validateToken(token, jwtSecret)) {
            log.warn("Невалидный JWT токен для пути: {}", path);
            return handleUnauthorized(exchange);
        }

        // Извлекаем информацию из токена
        String username = JwtUtil.extractUsername(token, jwtSecret);
        Long userId = JwtUtil.extractUserId(token, jwtSecret);
        String role = JwtUtil.extractRole(token, jwtSecret);

        if (username == null) {
            log.warn("Не удалось извлечь имя пользователя из токена для пути: {}", path);
            return handleUnauthorized(exchange);
        }

        // Добавляем информацию о пользователе в headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Username", username)
                .header("X-User-Id", userId != null ? userId.toString() : "")
                .header("X-User-Role", role != null ? role : "USER")
                .build();

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();

        // Асинхронная валидация через Authentication Service
        return authenticationServiceClient.validateToken(token)
                .flatMap(validationResponse -> {
                    if (validationResponse.getValid() == null || !validationResponse.getValid()) {
                        log.warn("Токен не прошел валидацию в Authentication Service для пользователя: {}", username);
                        return handleUnauthorized(exchange);
                    }
                    return chain.filter(modifiedExchange);
                })
                .onErrorResume(throwable -> {
                    log.error("Ошибка при валидации токена через Authentication Service", throwable);
                    // Если Authentication Service недоступен, продолжаем с локальной валидацией
                    return chain.filter(modifiedExchange);
                });
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Unauthorized\",\"message\":\"Требуется аутентификация\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100; // Высокий приоритет
    }
}