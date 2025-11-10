package dn.quest.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Фильтр для добавления security headers
 */
@Component
@Slf4j
public class SecurityHeadersFilter implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();

        // Prevent clickjacking
        headers.add("X-Frame-Options", "DENY");
        
        // Prevent MIME type sniffing
        headers.add("X-Content-Type-Options", "nosniff");
        
        // Enable XSS protection
        headers.add("X-XSS-Protection", "1; mode=block");
        
        // Referrer policy
        headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Content Security Policy
        headers.add("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' data:; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self'");
        
        // Strict Transport Security (только для HTTPS)
        if (exchange.getRequest().getURI().getScheme().equals("https")) {
            headers.add("Strict-Transport-Security", 
                    "max-age=31536000; includeSubDomains; preload");
        }
        
        // Permissions Policy
        headers.add("Permissions-Policy", 
                "geolocation=(), " +
                "microphone=(), " +
                "camera=(), " +
                "payment=(), " +
                "usb=(), " +
                "magnetometer=(), " +
                "gyroscope=(), " +
                "accelerometer=()");
        
        // Remove server information
        headers.remove("Server");
        headers.add("Server", "DN-Quest-Gateway");
        
        // Cache control for sensitive endpoints
        String path = exchange.getRequest().getURI().getPath();
        if (isSensitiveEndpoint(path)) {
            headers.add("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
        }

        return chain.filter(exchange);
    }

    private boolean isSensitiveEndpoint(String path) {
        return path.contains("/api/auth/") ||
               path.contains("/api/users/") ||
               path.contains("/actuator/") ||
               path.endsWith("/profile");
    }

    @Override
    public int getOrder() {
        return -50; // Выполняется после аутентификации
    }
}