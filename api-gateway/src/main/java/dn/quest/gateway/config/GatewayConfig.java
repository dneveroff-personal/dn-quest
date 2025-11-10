package dn.quest.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

/**
 * Конфигурация API Gateway
 */
@Configuration
public class GatewayConfig {

    /**
     * Key resolver для rate limiting по IP адресу
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress()
        );
    }

    /**
     * Key resolver для rate limiting по пользователю
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String username = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Username");
            return Mono.just(username != null ? username : "anonymous");
        };
    }

    /**
     * Password encoder для безопасности
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}