package dn.quest.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Swagger endpoints должны быть доступны без аутентификации для удобства dev
                        .pathMatchers("/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**", "/api-docs").permitAll()
                        // Health endpoints должны быть доступны без аутентификации для мониторинга
                        .pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        // Остальные actuator endpoints требуют аутентификацию
                        .pathMatchers("/actuator/**").authenticated()
                        // Все остальные запросы требуют аутентификации
                        .anyExchange().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // In-memory пользователь для Basic Auth
    // Удалён - используется стандартный пользователь из application.yml
    // GatewayConfig предоставляет PasswordEncoder (BCrypt) для Spring Security
}