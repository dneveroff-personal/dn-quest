package dn.quest.gateway.controller;

import dn.quest.gateway.client.AuthenticationServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для мониторинга состояния API Gateway
 */
@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
@Slf4j
public class GatewayHealthController implements HealthIndicator {

    private final AuthenticationServiceClient authenticationServiceClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Проверка здоровья API Gateway
     */
    @GetMapping("/gateway-health")
    public Mono<Map<String, Object>> gatewayHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("component", "api-gateway");
        health.put("timestamp", System.currentTimeMillis());
        
        // Проверяем состояние Circuit Breaker
        Map<String, Object> circuitBreakers = new HashMap<>();
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            Map<String, Object> cbInfo = new HashMap<>();
            cbInfo.put("state", cb.getState().name());
            cbInfo.put("failureRate", cb.getMetrics().getFailureRate());
            cbInfo.put("numberOfCalls", cb.getMetrics().getNumberOfCalls());
            cbInfo.put("numberOfSuccessfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
            cbInfo.put("numberOfFailedCalls", cb.getMetrics().getNumberOfFailedCalls());
            circuitBreakers.put(cb.getName(), cbInfo);
        });
        health.put("circuitBreakers", circuitBreakers);
        
        // Проверяем доступность Authentication Service
        return authenticationServiceClient.isServiceHealthy()
                .map(isHealthy -> {
                    Map<String, Object> services = new HashMap<>();
                    services.put("authentication-service", isHealthy ? "UP" : "DOWN");
                    health.put("services", services);
                    return health;
                })
                .onErrorReturn(health);
    }

    /**
     * Получение детальной информации о Circuit Breaker
     */
    @GetMapping("/circuit-breakers")
    public Map<String, Object> getCircuitBreakersStatus() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> circuitBreakers = new HashMap<>();
        
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            Map<String, Object> cbInfo = new HashMap<>();
            cbInfo.put("state", cb.getState().name());
            cbInfo.put("failureRate", cb.getMetrics().getFailureRate());
            cbInfo.put("numberOfCalls", cb.getMetrics().getNumberOfCalls());
            cbInfo.put("numberOfSuccessfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
            cbInfo.put("numberOfFailedCalls", cb.getMetrics().getNumberOfFailedCalls());
            cbInfo.put("notPermittedCalls", cb.getMetrics().getNumberOfNotPermittedCalls());
            cbInfo.put("bufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
            cbInfo.put("slowCalls", cb.getMetrics().getNumberOfSlowCalls());
            circuitBreakers.put(cb.getName(), cbInfo);
        });
        
        result.put("circuitBreakers", circuitBreakers);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }

    /**
     * Health Indicator для Spring Boot Actuator
     */
    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        try {
            // Проверяем состояние Circuit Breaker
            boolean allCircuitBreakersHealthy = circuitBreakerRegistry.getAllCircuitBreakers()
                    .stream()
                    .noneMatch(cb -> cb.getState() == CircuitBreaker.State.OPEN);
            
            if (allCircuitBreakersHealthy) {
                builder.up();
            } else {
                builder.down();
            }
            
            // Добавляем детальную информацию
            Map<String, Object> details = new HashMap<>();
            circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
                details.put(cb.getName() + "_state", cb.getState().name());
                details.put(cb.getName() + "_failureRate", cb.getMetrics().getFailureRate());
            });
            
            builder.withDetails(details);
            
        } catch (Exception e) {
            log.error("Ошибка при проверке здоровья API Gateway", e);
            builder.down().withDetail("error", e.getMessage());
        }
        
        return builder.build();
    }

    /**
     * Получение информации о нагрузке на Gateway
     */
    @GetMapping("/gateway-metrics")
    public Map<String, Object> getGatewayMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Базовая информация
        metrics.put("timestamp", System.currentTimeMillis());
        metrics.put("uptime", System.currentTimeMillis() - managementStartTime);
        
        // Информация о Circuit Breaker
        Map<String, Object> cbMetrics = new HashMap<>();
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            Map<String, Object> cbInfo = new HashMap<>();
            cbInfo.put("state", cb.getState().name());
            cbInfo.put("failureRate", cb.getMetrics().getFailureRate());
            cbInfo.put("totalCalls", cb.getMetrics().getNumberOfCalls());
            cbMetrics.put(cb.getName(), cbInfo);
        });
        metrics.put("circuitBreakers", cbMetrics);
        
        return metrics;
    }

    private final long managementStartTime = System.currentTimeMillis();
}