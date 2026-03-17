package dn.quest.gateway.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация метрик производительности
 */
@Configuration
public class MetricsConfig {

    /**
     * Счетчик для общего количества запросов
     */
    @Bean
    public Counter requestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.requests.total")
                .description("Общее количество запросов к gateway")
                .tag("component", "api-gateway")
                .register(meterRegistry);
    }

    /**
     * Счетчик для успешных запросов
     */
    @Bean
    public Counter successRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.requests.success")
                .description("Количество успешных запросов")
                .tag("component", "api-gateway")
                .tag("status", "success")
                .register(meterRegistry);
    }

    /**
     * Счетчик для ошибочных запросов
     */
    @Bean
    public Counter errorRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.requests.error")
                .description("Количество ошибочных запросов")
                .tag("component", "api-gateway")
                .tag("status", "error")
                .register(meterRegistry);
    }

    /**
     * Таймер для измерения времени обработки запросов
     */
    @Bean
    public Timer requestTimer(MeterRegistry meterRegistry) {
        return Timer.builder("gateway.requests.duration")
                .description("Время обработки запросов")
                .tag("component", "api-gateway")
                .register(meterRegistry);
    }

    /**
     * Счетчик для аутентификационных запросов
     */
    @Bean
    public Counter authenticationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.authentication.total")
                .description("Количество аутентификационных запросов")
                .tag("component", "api-gateway")
                .tag("type", "authentication")
                .register(meterRegistry);
    }

    /**
     * Счетчик для успешных аутентификаций
     */
    @Bean
    public Counter authenticationSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.authentication.success")
                .description("Количество успешных аутентификаций")
                .tag("component", "api-gateway")
                .tag("type", "authentication")
                .tag("status", "success")
                .register(meterRegistry);
    }

    /**
     * Счетчик для неудачных аутентификаций
     */
    @Bean
    public Counter authenticationFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.authentication.failure")
                .description("Количество неудачных аутентификаций")
                .tag("component", "api-gateway")
                .tag("type", "authentication")
                .tag("status", "failure")
                .register(meterRegistry);
    }

    /**
     * Счетчик для срабатываний Circuit Breaker
     */
    @Bean
    public Counter circuitBreakerCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.circuitbreaker.trips")
                .description("Количество срабатываний Circuit Breaker")
                .tag("component", "api-gateway")
                .tag("type", "circuit-breaker")
                .register(meterRegistry);
    }

    /**
     * Счетчик для Rate Limiting
     */
    @Bean
    public Counter rateLimitCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.ratelimit.blocks")
                .description("Количество заблокированных запросов из-за Rate Limiting")
                .tag("component", "api-gateway")
                .tag("type", "rate-limit")
                .register(meterRegistry);
    }

    /**
     * Таймер для измерения времени валидации JWT
     */
    @Bean
    public Timer jwtValidationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("gateway.jwt.validation.duration")
                .description("Время валидации JWT токенов")
                .tag("component", "api-gateway")
                .tag("operation", "jwt-validation")
                .register(meterRegistry);
    }

    /**
     * Счетчик для запросов к микросервисам
     */
    @Bean
    public Counter microserviceRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.microservice.requests.total")
                .description("Количество запросов к микросервисам")
                .tag("component", "api-gateway")
                .tag("type", "microservice")
                .register(meterRegistry);
    }

    /**
     * Таймер для измерения времени ответа микросервисов
     */
    @Bean
    public Timer microserviceResponseTimer(MeterRegistry meterRegistry) {
        return Timer.builder("gateway.microservice.response.duration")
                .description("Время ответа микросервисов")
                .tag("component", "api-gateway")
                .tag("operation", "microservice-response")
                .register(meterRegistry);
    }

    /**
     * Счетчик для активных сессий
     */
    @Bean
    public Counter activeSessionsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.sessions.active")
                .description("Количество активных сессий")
                .tag("component", "api-gateway")
                .tag("type", "sessions")
                .register(meterRegistry);
    }

    /**
     * Таймер для измерения времени обработки fallback
     */
    @Bean
    public Timer fallbackTimer(MeterRegistry meterRegistry) {
        return Timer.builder("gateway.fallback.duration")
                .description("Время обработки fallback сценариев")
                .tag("component", "api-gateway")
                .tag("operation", "fallback")
                .register(meterRegistry);
    }
}