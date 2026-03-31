package dn.quest.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Конфигурация Circuit Breaker с использованием Resilience4j
 */
@Configuration
@Slf4j
public class Resilience4jConfig {

    /**
     * Конфигурация Circuit Breaker для микросервисов
     */
    @Bean
    public io.github.resilience4j.circuitbreaker.CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // Процент отказов для открытия цепи
                .waitDurationInOpenState(Duration.ofSeconds(30)) // Время ожидания в открытом состоянии
                .slidingWindowSize(10) // Размер окна для расчета процента отказов
                .minimumNumberOfCalls(5) // Минимальное количество вызовов для расчета
                .permittedNumberOfCallsInHalfOpenState(3) // Количество вызовов в полуоткрытом состоянии
                .automaticTransitionFromOpenToHalfOpenEnabled(true) // Автоматический переход в полуоткрытое состояние
                .recordExceptions(Exception.class) // Записывать все исключения
                .ignoreExceptions(IllegalArgumentException.class) // Игнорировать определенные исключения
                .build();
    }

    /**
     * Конфигурация Circuit Breaker для Authentication Service
     */
    @Bean
    public io.github.resilience4j.circuitbreaker.CircuitBreakerConfig authenticationServiceCircuitBreakerConfig() {
        return io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(30) // Более низкий порог для критичного сервиса
                .waitDurationInOpenState(Duration.ofSeconds(15)) // Быстрее восстанавливаемся
                .slidingWindowSize(5)
                .minimumNumberOfCalls(3)
                .permittedNumberOfCallsInHalfOpenState(2)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .build();
    }

    /**
     * Конфигурация Retry для микросервисов
     */
    @Bean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(attempt -> {
                    // Экспоненциальный backoff начиная со 100ms
                    long delay = (long) (100 * Math.pow(2, attempt - 1));
                    return Math.min(delay, 2000);
                })
                .retryExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();
    }

    /**
     * Конфигурация Time Limiter
     */
    @Bean
    public TimeLimiterConfig defaultTimeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5)) // Таймаут выполнения
                .cancelRunningFuture(true) // Отменять выполняющиеся задачи
                .build();
    }

    /**
     * Registry для Circuit Breaker
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(
            io.github.resilience4j.circuitbreaker.CircuitBreakerConfig defaultCircuitBreakerConfig,
            io.github.resilience4j.circuitbreaker.CircuitBreakerConfig authenticationServiceCircuitBreakerConfig) {
        
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        
        // Регистрируем Circuit Breaker для разных сервисов
        registry.circuitBreaker("authenticationService", authenticationServiceCircuitBreakerConfig);
        registry.circuitBreaker("userManagementService", defaultCircuitBreakerConfig);
        registry.circuitBreaker("questManagementService", defaultCircuitBreakerConfig);
        registry.circuitBreaker("gameEngineService", defaultCircuitBreakerConfig);
        registry.circuitBreaker("teamManagementService", defaultCircuitBreakerConfig);
        registry.circuitBreaker("notificationService", defaultCircuitBreakerConfig);
        registry.circuitBreaker("statisticsService", defaultCircuitBreakerConfig);
        registry.circuitBreaker("fileStorageService", defaultCircuitBreakerConfig);
        
        // Добавляем слушатели событий
        registry.getAllCircuitBreakers().forEach(this::addCircuitBreakerListeners);
        
        return registry;
    }

    /**
     * Добавление слушателей событий для Circuit Breaker
     */
    private void addCircuitBreakerListeners(CircuitBreaker circuitBreaker) {
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> 
                    log.info("CircuitBreaker {} state transition from {} to {}",
                            circuitBreaker.getName(),
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState()))
                .onFailureRateExceeded(event -> 
                    log.warn("CircuitBreaker {} failure rate exceeded: {}%",
                            circuitBreaker.getName(),
                            event.getFailureRate()))
                .onCallNotPermitted(event -> 
                    log.warn("CircuitBreaker {} call not permitted - circuit is open",
                            circuitBreaker.getName()))
                .onError(event -> 
                    log.error("CircuitBreaker {} error: {}",
                            circuitBreaker.getName(),
                            event.getThrowable().getMessage()));
    }

    /**
     * Circuit Breaker для Authentication Service
     */
    @Bean
    public CircuitBreaker authenticationServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("authenticationService");
    }

    /**
     * Retry для Authentication Service
     */
    @Bean
    public Retry authenticationServiceRetry(RetryConfig defaultRetryConfig) {
        return Retry.of("authenticationService", defaultRetryConfig);
    }

    /**
     * Time Limiter для Authentication Service
     */
    @Bean
    public TimeLimiter authenticationServiceTimeLimiter(TimeLimiterConfig defaultTimeLimiterConfig) {
        return TimeLimiter.of(defaultTimeLimiterConfig);
    }
}