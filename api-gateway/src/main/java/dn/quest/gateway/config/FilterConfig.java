package dn.quest.gateway.config;

import dn.quest.gateway.filter.LoggingFilter;
import dn.quest.gateway.filter.SecurityHeadersFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация фильтров и маршрутов API Gateway
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class FilterConfig {

    private final LoggingFilter loggingFilter;
    private final SecurityHeadersFilter securityHeadersFilter;

    // Docker service URLs
    @Value("${AUTHENTICATION_SERVICE_URL:http://localhost:8081}")
    private String authenticationServiceUrl;

    @Value("${USER_MANAGEMENT_SERVICE_URL:http://localhost:8082}")
    private String userManagementServiceUrl;

    @Value("${QUEST_MANAGEMENT_SERVICE_URL:http://localhost:8083}")
    private String questManagementServiceUrl;

    @Value("${GAME_ENGINE_SERVICE_URL:http://localhost:8084}")
    private String gameEngineServiceUrl;

    @Value("${TEAM_MANAGEMENT_SERVICE_URL:http://localhost:8085}")
    private String teamManagementServiceUrl;

    @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8086}")
    private String notificationServiceUrl;

    @Value("${STATISTICS_SERVICE_URL:http://localhost:8087}")
    private String statisticsServiceUrl;

    @Value("${FILE_STORAGE_SERVICE_URL:http://localhost:8088}")
    private String fileStorageServiceUrl;

    /**
     * Конфигурация маршрутов с фильтрами
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Authentication Service
                .route("authentication-service", r -> r.path("/api/auth/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(securityHeadersFilter)
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("authenticationService")
                                        .setFallbackUri("forward:/fallback/authentication"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3))
                        )
                        .uri(authenticationServiceUrl))

                // User Management Service
                .route("user-management-service", r -> r.path("/api/users/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(securityHeadersFilter)
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("userManagementService")
                                        .setFallbackUri("forward:/fallback/users"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3))
                        )
                        .uri(userManagementServiceUrl))

                // Quest Management Service
                .route("quest-management-service", r -> r.path("/api/quests/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(securityHeadersFilter)
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("questManagementService")
                                        .setFallbackUri("forward:/fallback/quests"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3))
                        )
                        .uri(questManagementServiceUrl))

                // Game Engine Service
                .route("game-engine-service", r -> r.path("/api/game/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(securityHeadersFilter)
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("gameEngineService")
                                        .setFallbackUri("forward:/fallback/game"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3))
                        )
                        .uri(gameEngineServiceUrl))

                // Team Management Service
                .route("team-management-service", r -> r.path("/api/teams/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(securityHeadersFilter)
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("teamManagementService")
                                        .setFallbackUri("forward:/fallback/teams"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3))
                        )
                        .uri(teamManagementServiceUrl))

                // Notification Service
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(securityHeadersFilter)
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("notificationService")
                                        .setFallbackUri("forward:/fallback/notifications"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(2))
                        )
                        .uri(notificationServiceUrl))

                // Statistics Service
                .route("statistics-service", r -> r.path("/api/statistics/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(securityHeadersFilter)
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("statisticsService")
                                        .setFallbackUri("forward:/fallback/statistics"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3))
                        )
                        .uri(statisticsServiceUrl))

                // File Storage Service
                .route("file-storage-service", r -> r.path("/api/files/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(securityHeadersFilter)
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("fileStorageService")
                                        .setFallbackUri("forward:/fallback/files"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3))
                        )
                        .uri(fileStorageServiceUrl))

                .build();
    }

    /**
     * Redis Rate Limiter - опциональный, если Redis недоступен
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        try {
            return new RedisRateLimiter(10, 20);
        } catch (Exception e) {
            log.warn("Failed to create RedisRateLimiter, rate limiting will be disabled", e);
            return null;
        }
    }
}