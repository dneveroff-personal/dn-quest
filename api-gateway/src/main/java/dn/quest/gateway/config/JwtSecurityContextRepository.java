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
        return Mono.empty();
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }
}