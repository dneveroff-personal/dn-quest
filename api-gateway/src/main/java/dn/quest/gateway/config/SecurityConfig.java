package dn.quest.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Конфигурация безопасности для API Gateway
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtSecurityContextRepository jwtSecurityContextRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Swagger endpoints должны быть доступны без аутентификации для удобства dev
                        .pathMatchers("/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**", "/api-docs").permitAll()
                        // Health endpoints должны быть доступны без аутентификации для мониторинга
                        .pathMatchers("/actuator/health", "/actuator/health/**", "/actuator/gateway-health", "/actuator/gateway-health/**").permitAll()
                        // Публичные auth эндпоинты (без аутентификации)
                        .pathMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/validate"
                        ).permitAll()
                        // WebSocket эндпоинт (аутентификация происходит в обработчике)
                        .pathMatchers("/ws/**").permitAll()
                        // Остальные actuator endpoints требуют аутентификацию
                        .pathMatchers("/actuator/**").authenticated()
                        // Все остальные запросы требуют аутентификации
                        .anyExchange().authenticated()
                )
                .securityContextRepository(jwtSecurityContextRepository);

        return http.build();
    }
}