package dn.quest.gateway.client;

import dn.quest.gateway.dto.TokenValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Клиент для взаимодействия с Authentication Service
 */
@Component
public class AuthenticationServiceClient {

    private final WebClient webClient;
    private final String authenticationServiceUrl;

    public AuthenticationServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.authentication.url:http://localhost:8081}") String authenticationServiceUrl) {
        this.authenticationServiceUrl = authenticationServiceUrl;
        this.webClient = webClientBuilder
                .baseUrl(authenticationServiceUrl)
                .build();
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
                    // В случае ошибки подключения к Authentication Service,
                    // выполняем локальную валидацию
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
     */
    public Mono<Boolean> isServiceHealthy() {
        return webClient.get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .timeout(Duration.ofSeconds(3))
                .onErrorReturn(false);
    }
}