package dn.quest.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для API Gateway
 */
@Component
@Order(-2)
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = determineHttpStatus(ex);
        String message = getErrorMessage(ex);
        String path = exchange.getRequest().getPath().value();
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");

        log.error("Error occurred - CorrelationId: {}, Path: {}, Status: {}, Message: {}",
                correlationId, path, status.value(), message, ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        errorResponse.put("correlationId", correlationId);

        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            errorResponse.put("message", "Внутренняя ошибка сервера. Пожалуйста, обратитесь в поддержку.");
        }

        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse))
                .flatMap(response -> response.writeTo(exchange, null));
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof WebClientResponseException) {
            WebClientResponseException webEx = (WebClientResponseException) ex;
            return HttpStatus.resolve(webEx.getStatusCode().value()) 
                    ? HttpStatus.resolve(webEx.getStatusCode().value()) 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        if (ex instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        
        if (ex instanceof org.springframework.web.server.ResponseStatusException) {
            return ((org.springframework.web.server.ResponseStatusException) ex).getStatusCode();
        }
        
        if (ex instanceof java.security.SignatureException || 
            ex instanceof io.jsonwebtoken.security.SecurityException) {
            return HttpStatus.UNAUTHORIZED;
        }
        
        if (ex instanceof io.jsonwebtoken.MalformedJwtException ||
            ex instanceof io.jsonwebtoken.ExpiredJwtException) {
            return HttpStatus.UNAUTHORIZED;
        }
        
        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String getErrorMessage(Throwable ex) {
        if (ex instanceof WebClientResponseException) {
            WebClientResponseException webEx = (WebClientResponseException) ex;
            return webEx.getResponseBodyAsString();
        }
        
        if (ex instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            return "Запрошенный ресурс не найден";
        }
        
        if (ex instanceof org.springframework.web.server.ResponseStatusException) {
            return ex.getMessage();
        }
        
        if (ex instanceof java.security.SignatureException || 
            ex instanceof io.jsonwebtoken.security.SecurityException) {
            return "Неверная подпись токена";
        }
        
        if (ex instanceof io.jsonwebtoken.MalformedJwtException) {
            return "Некорректный формат токена";
        }
        
        if (ex instanceof io.jsonwebtoken.ExpiredJwtException) {
            return "Срок действия токена истек";
        }
        
        if (ex instanceof IllegalArgumentException) {
            return ex.getMessage();
        }
        
        return "Внутренняя ошибка сервера";
    }
}