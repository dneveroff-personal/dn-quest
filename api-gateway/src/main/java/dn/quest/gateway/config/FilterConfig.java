package dn.quest.gateway.config;

import dn.quest.gateway.filter.AuthenticationFilter;
import dn.quest.gateway.filter.LoggingFilter;
import dn.quest.gateway.filter.SecurityHeadersFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация фильтров и маршрутов API Gateway
 */
@Configuration
public class FilterConfig {

    /**
     * Конфигурация маршрутов с фильтрами
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Authentication Service
                .route("authentication-service", r -> r.path("/api/auth/**")
                        .filters(f -> f
                                .filter(new LoggingFilter())
                                .filter(new SecurityHeadersFilter())
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("authenticationService")
                                        .setFallbackUri("forward:/fallback/authentication"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setBackoff(org.springframework.cloud.gateway.filter.factory.RetryBackoffConfig.exponential()))
                        )
                        .uri("http://localhost:8081"))

                // User Management Service
                .route("user-management-service", r -> r.path("/api/users/**")
                        .filters(f -> f
                                .filter(new LoggingFilter())
                                .filter(new AuthenticationFilter())
                                .filter(new SecurityHeadersFilter())
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("userManagementService")
                                        .setFallbackUri("forward:/fallback/users"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setBackoff(org.springframework.cloud.gateway.filter.factory.RetryBackoffConfig.exponential()))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("http://localhost:8082"))

                // Quest Management Service
                .route("quest-management-service", r -> r.path("/api/quests/**")
                        .filters(f -> f
                                .filter(new LoggingFilter())
                                .filter(new AuthenticationFilter())
                                .filter(new SecurityHeadersFilter())
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("questManagementService")
                                        .setFallbackUri("forward:/fallback/quests"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setBackoff(org.springframework.cloud.gateway.filter.factory.RetryBackoffConfig.exponential()))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("http://localhost:8083"))

                // Game Engine Service
                .route("game-engine-service", r -> r.path("/api/game/**")
                        .filters(f -> f
                                .filter(new LoggingFilter())
                                .filter(new AuthenticationFilter())
                                .filter(new SecurityHeadersFilter())
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("gameEngineService")
                                        .setFallbackUri("forward:/fallback/game"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setBackoff(org.springframework.cloud.gateway.filter.factory.RetryBackoffConfig.exponential()))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("http://localhost:8084"))

                // Team Management Service
                .route("team-management-service", r -> r.path("/api/teams/**")
                        .filters(f -> f
                                .filter(new LoggingFilter())
                                .filter(new AuthenticationFilter())
                                .filter(new SecurityHeadersFilter())
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("teamManagementService")
                                        .setFallbackUri("forward:/fallback/teams"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setBackoff(org.springframework.cloud.gateway.filter.factory.RetryBackoffConfig.exponential()))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("http://localhost:8085"))

                // Notification Service
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .filters(f -> f
                                .filter(new LoggingFilter())
                                .filter(new AuthenticationFilter())
                                .filter(new SecurityHeadersFilter())
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("notificationService")
                                        .setFallbackUri("forward:/fallback/notifications"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(2)
                                        .setBackoff(org.springframework.cloud.gateway.filter.factory.RetryBackoffConfig.exponential()))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("http://localhost:8086"))

                // Statistics Service
                .route("statistics-service", r -> r.path("/api/statistics/**")
                        .filters(f -> f
                                .filter(new LoggingFilter())
                                .filter(new AuthenticationFilter())
                                .filter(new SecurityHeadersFilter())
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("statisticsService")
                                        .setFallbackUri("forward:/fallback/statistics"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setBackoff(org.springframework.cloud.gateway.filter.factory.RetryBackoffConfig.exponential()))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("http://localhost:8087"))

                // File Storage Service
                .route("file-storage-service", r -> r.path("/api/files/**")
                        .filters(f -> f
                                .filter(new LoggingFilter())
                                .filter(new AuthenticationFilter())
                                .filter(new SecurityHeadersFilter())
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("fileStorageService")
                                        .setFallbackUri("forward:/fallback/files"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setBackoff(org.springframework.cloud.gateway.filter.factory.RetryBackoffConfig.exponential()))
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver()))
                        )
                        .uri("http://localhost:8088"))

                .build();
    }

    /**
     * Redis Rate Limiter
     */
    private org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(10, 20, 1);
    }

    /**
     * User Key Resolver для Rate Limiting
     */
    private org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> {
            String username = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Username");
            return reactor.core.publisher.Mono.just(username != null ? username : "anonymous");
        };
    }
}