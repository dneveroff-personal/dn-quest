package dn.quest.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * Конфигурация WebClient для взаимодействия с микросервисами
 */
@Configuration
@Slf4j
public class WebClientConfig {

    /**
     * Базовый WebClient с настройками производительности
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        // Настройка Connection Pool
        ConnectionProvider connectionProvider = ConnectionProvider.builder("gateway-pool")
                .maxConnections(200)
                .pendingAcquireTimeout(Duration.ofSeconds(45))
                .pendingAcquireMaxCount(-1)
                .build();

        // Настройка HttpClient
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofSeconds(30))
                .compress(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse());
    }

    /**
     * WebClient для Authentication Service с Circuit Breaker
     */
    @Bean
    public WebClient authenticationServiceWebClient(
            WebClient.Builder webClientBuilder,
            CircuitBreaker authenticationServiceCircuitBreaker,
            Retry authenticationServiceRetry) {
        
        return webClientBuilder
                .baseUrl("http://localhost:8081")
                .filter(circuitBreakerFilter(authenticationServiceCircuitBreaker))
                .filter(retryFilter(authenticationServiceRetry))
                .build();
    }

    /**
     * Фильтр Circuit Breaker для WebClient
     */
    private ExchangeFilterFunction circuitBreakerFilter(CircuitBreaker circuitBreaker) {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            return Mono.defer(() -> {
                if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
                    log.warn("Circuit breaker OPEN for service, rejecting request");
                    return Mono.error(new java.io.IOException("Circuit breaker is OPEN"));
                }
                return Mono.just(clientRequest);
            });
        });
    }

    /**
     * Фильтр Retry для WebClient
     */
    private ExchangeFilterFunction retryFilter(Retry retry) {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                log.warn("Request failed with status: {}, will retry", clientResponse.statusCode());
                return Mono.error(new RuntimeException("Service returned error status: " + clientResponse.statusCode().value()));
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * Фильтр для логирования запросов
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (clientRequest.url().getPath().contains("/actuator")) {
                return Mono.just(clientRequest);
            }
            
            StringBuilder sb = new StringBuilder("Request: ")
                    .append(clientRequest.method())
                    .append(" ")
                    .append(clientRequest.url());
                    
            clientRequest.headers().forEach((name, values) -> 
                    values.forEach(value -> sb.append("\n  ").append(name).append(": ").append(value)));
            
            System.out.println(sb.toString());
            return Mono.just(clientRequest);
        });
    }

    /**
     * Фильтр для логирования ответов
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is2xxSuccessful()) {
                System.out.println("Response Status: " + clientResponse.statusCode());
            } else {
                System.out.println("Response Status: " + clientResponse.statusCode() + " (Error)");
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * WebClient с Circuit Breaker для указанного сервиса
     */
    public WebClient createWebClientWithCircuitBreaker(
            WebClient.Builder webClientBuilder,
            String baseUrl,
            CircuitBreaker circuitBreaker,
            Retry retry) {
        
        return webClientBuilder
                .baseUrl(baseUrl)
                .filter(circuitBreakerFilter(circuitBreaker))
                .filter(retryFilter(retry))
                .build();
    }
}