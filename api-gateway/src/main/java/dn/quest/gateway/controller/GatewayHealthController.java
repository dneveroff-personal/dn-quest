package dn.quest.gateway.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Контроллер для мониторинга состояния API Gateway
 */
@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
@Slf4j
public class GatewayHealthController implements HealthIndicator {

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
            cbInfo.put("numberOfBufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
            cbInfo.put("numberOfSuccessfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
            cbInfo.put("numberOfFailedCalls", cb.getMetrics().getNumberOfFailedCalls());
            circuitBreakers.put(cb.getName(), cbInfo);
        });
        health.put("circuitBreakers", circuitBreakers);
        
        // Проверяем статус всех сервисов через WebClient
        return checkAllServices()
                .map(services -> {
                    health.put("services", services);
                    return health;
                })
                .onErrorReturn(health);
    }
    
    /**
     * Проверка всех сервисов
     */
    private Mono<Map<String, String>> checkAllServices() {
        // Определяем базовый URL в зависимости от окружения
        // Приоритет: переменная GATEWAY_IN_DOCKER > env переменные > проверка /.dockerenv
        String dockerEnv = System.getenv().get("GATEWAY_IN_DOCKER");
        boolean isDocker = "true".equalsIgnoreCase(dockerEnv) 
                || "1".equals(dockerEnv)
                || System.getenv().get("DOCKER") != null
                || new java.io.File("/.dockerenv").exists();
        
        log.info("Определение окружения для health check: isDocker={}", isDocker);
        
        // Для Docker используем имена контейнеров с портами, для localhost - порты напрямую
        // Actuator endpoints находятся внутри context-path сервиса!
        // С context-path /api/auth -> actuator доступен по /api/auth/actuator/health
        List<ServiceCheck> services;
        if (isDocker) {
            log.info("Используем URL для Docker контейнеров (actuator В context-path)");
            services = List.of(
                new ServiceCheck("authentication-service", "http://authentication-service-dev", "8081", "/api/auth/actuator/health"),
                new ServiceCheck("user-management-service", "http://user-management-service-dev", "8082", "/api/users/actuator/health"),
                new ServiceCheck("quest-management-service", "http://quest-management-service-dev", "8083", "/api/quests/actuator/health"),
                new ServiceCheck("game-engine-service", "http://game-engine-service-dev", "8084", "/api/game/actuator/health"),
                new ServiceCheck("team-management-service", "http://team-management-service-dev", "8085", "/api/actuator/health"),
                new ServiceCheck("notification-service", "http://notification-service-dev", "8086", "/api/notifications/actuator/health"),
                new ServiceCheck("statistics-service", "http://statistics-service-dev", "8087", "/api/stats/actuator/health"),
                new ServiceCheck("file-storage-service", "http://file-storage-service-dev", "8088", "/api/files/actuator/health")
            );
        } else {
            log.info("Используем URL для localhost (actuator В context-path)");
            services = List.of(
                new ServiceCheck("authentication-service", "http://localhost", "8081", "/api/auth/actuator/health"),
                new ServiceCheck("user-management-service", "http://localhost", "8082", "/api/users/actuator/health"),
                new ServiceCheck("quest-management-service", "http://localhost", "8083", "/api/quests/actuator/health"),
                new ServiceCheck("game-engine-service", "http://localhost", "8084", "/api/game/actuator/health"),
                new ServiceCheck("team-management-service", "http://localhost", "8085", "/api/actuator/health"),
                new ServiceCheck("notification-service", "http://localhost", "8086", "/api/notifications/actuator/health"),
                new ServiceCheck("statistics-service", "http://localhost", "8087", "/api/stats/actuator/health"),
                new ServiceCheck("file-storage-service", "http://localhost", "8088", "/api/files/actuator/health")
            );
        }
        
        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofSeconds(3))))
                .build();
        
        return Flux.fromIterable(services)
                .flatMap(service -> {
                    String url = service.port.isEmpty() 
                        ? service.url + service.healthPath 
                        : service.url + ":" + service.port + service.healthPath;
                    log.debug("Проверка сервиса {}: {}", service.name, url);
                    return webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(response -> {
                            log.debug("Сервис {} доступен", service.name);
                            return Map.entry(service.name, "UP");
                        })
                        .doOnError(error -> log.warn("Ошибка проверки сервиса {}: {}", service.name, error.getMessage()))
                        .onErrorReturn(Map.entry(service.name, "DOWN"));
                })
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }
    
    private static class ServiceCheck {
        String name;
        String url;
        String port;
        String healthPath;
        
        ServiceCheck(String name, String baseUrl, String port, String healthPath) {
            this.name = name;
            this.url = baseUrl;
            this.port = port;
            this.healthPath = healthPath;
        }
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
            cbInfo.put("numberOfBufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
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
            boolean allCircuitBreakersHealthy = circuitBreakerRegistry.getAllCircuitBreakers()
                    .stream()
                    .allMatch(cb -> cb.getState().equals(CircuitBreaker.State.CLOSED));
            
            if (allCircuitBreakersHealthy) {
                builder.up();
            } else {
                builder.down();
            }
        } catch (Exception e) {
            builder.down(e);
        }
        
        return builder.build();
    }
}