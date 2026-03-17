package dn.quest.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Фильтр для логирования запросов и ответов
 */
@Component
@Slf4j
public class LoggingFilter implements GatewayFilter, Ordered {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String START_TIME_HEADER = "X-Start-Time";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Instant startTime = Instant.now();

        // Генерируем или получаем Correlation ID
        String correlationId = getOrCreateCorrelationId(request);
        
        // Добавляем Correlation ID в response
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(CORRELATION_ID_HEADER, correlationId);
        
        // Сохраняем время начала запроса
        exchange.getAttributes().put(START_TIME_HEADER, startTime.toEpochMilli());

        // Логируем начало запроса
        logRequest(request, correlationId);

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    // Логируем завершение запроса
                    logResponse(response, correlationId, startTime);
                }));
    }

    private String getOrCreateCorrelationId(ServerHttpRequest request) {
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = generateCorrelationId();
        }
        return correlationId;
    }

    private String generateCorrelationId() {
        return "gw-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private void logRequest(ServerHttpRequest request, String correlationId) {
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String clientIp = getClientIp(request);

        log.info("Request started - CorrelationId: {}, Method: {}, Path: {}, Query: {}, ClientIP: {}, UserAgent: {}",
                correlationId, method, path, query, clientIp, userAgent);
    }

    private void logResponse(ServerHttpResponse response, String correlationId, Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;

        log.info("Request completed - CorrelationId: {}, StatusCode: {}, Duration: {}ms",
                correlationId, statusCode, duration.toMillis());

        // Логируем медленные запросы
        if (duration.toMillis() > 1000) {
            log.warn("Slow request detected - CorrelationId: {}, Duration: {}ms",
                    correlationId, duration.toMillis());
        }

        // Логируем ошибки
        if (statusCode >= 400) {
            log.error("Error response - CorrelationId: {}, StatusCode: {}, Duration: {}ms",
                    correlationId, statusCode, duration.toMillis());
        }
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null 
                ? request.getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
    }

    @Override
    public int getOrder() {
        return -200; // Самый высокий приоритет
    }
}