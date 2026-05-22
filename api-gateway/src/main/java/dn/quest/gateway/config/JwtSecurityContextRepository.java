package dn.quest.gateway.config;

import dn.quest.shared.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для хранения контекста безопасности на основе JWT токенов
 */
@Component
@Slf4j
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        
        // Пропускаем публичные эндпоинты - возвращаем пустой контекст вместо Mono.empty()
        // чтобы не блокировать запросы к публичным ресурсам
        if (isPublicEndpoint(path)) {
            return Mono.just(new SecurityContextImpl());
        }
        
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (JwtUtil.validateToken(token, jwtSecret)) {
                String username = JwtUtil.extractUsername(token, jwtSecret);
                UUID userId = JwtUtil.extractUserId(token, jwtSecret);
                String role = JwtUtil.extractRole(token, jwtSecret);
                
                if (username != null) {
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER"))
                    );
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(username, token, authorities);
                    authentication.setDetails(userId != null ? userId.toString() : null);
                    return Mono.just(new SecurityContextImpl(authentication));
                }
            }
        }
        // Для неавторизованных запросов к защищенным эндпоинтам возвращаем пустой контекст
        // Spring Security сам обработает это как 401 Unauthorized
        return Mono.just(new SecurityContextImpl());
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }
    
    private boolean isPublicEndpoint(String path) {
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
               path.startsWith("/actuator/gateway-health") ||
               path.equals("/") ||
               path.startsWith("/webjars");
    }
}