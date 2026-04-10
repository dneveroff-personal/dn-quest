package dn.quest.gateway.client;

import dn.quest.gateway.dto.TokenValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Клиент для взаимодействия с Authentication Service
 */
@Component
@Slf4j
public class AuthenticationServiceClient {

    private final WebClient webClient;
    private final WebClient healthWebClient;

    public AuthenticationServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.authentication.url:http://localhost:8081}") String authenticationServiceUrl,
            @Value("${services.authentication.health-url:${services.authentication.url:http://localhost:8081}}") String healthServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(authenticationServiceUrl)
                .build();

        // Создаем отдельный WebClient для health check с правильным URL
        String healthBaseUrl = resolveHealthUrl(healthServiceUrl);
        log.info("Инициализация health WebClient с URL: {}", healthBaseUrl);
        this.healthWebClient = webClientBuilder.clone()
                .baseUrl(healthBaseUrl)
                .build();
    }

    /**
     * Определение правильного URL для health check
     * Из Docker контейнера используем host.docker.internal, иначе localhost
     */
    private String resolveHealthUrl(String configuredUrl) {
        // Для Docker-compose всегда используем hostname сервиса внутри сети
        // URL уже содержит правильный хост (authentication-service-dev)
        log.info("Исходный URL для health check: {}", configuredUrl);
        
        // Не меняем URL - docker-compose уже настроил правильные хосты
        return configuredUrl;
    }

    /**
     * Валидация JWT токена через Authentication Service
     */
    public Mono<TokenValidationResponse> validateToken(String token) {
        return webClient.get()
                .uri("/api/auth/validate")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(throwable -> {
                    log.warn("Ошибка подключения к Authentication Service: {}", throwable.getMessage());
                    return Mono.just(TokenValidationResponse.builder()
                            .valid(false)
                            .error("Authentication service unavailable")
                            .build());
                });
    }

    /**
     * Обновление токена
     */
    public Mono<String> refreshToken(String refreshToken) {
        return webClient.post()
                .uri("/api/auth/refresh")
                .bodyValue("{\"refreshToken\":\"" + refreshToken + "\"}")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5));
    }

    /**
     * Проверка статуса Authentication Service
     * Использует отдельный WebClient с правильным URL для Docker
     */
    public Mono<Boolean> isServiceHealthy() {
        log.debug("Выполнение health check для Authentication Service");
        return healthWebClient.get()
                .uri("/api/auth/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    log.debug("Authentication Service здоров");
                    return true;
                })
                .timeout(Duration.ofSeconds(3))
                .onErrorReturn(false);
    }
}