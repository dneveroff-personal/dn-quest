package dn.quest.teammanagement.config;

import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Конфигурация Feign клиентов
 */
@Configuration
public class FeignConfig {

    /**
     * Настройка логирования для Feign клиентов
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Настройка стратегии повторных попыток
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
                100, // Начальная задержка в миллисекундах
                TimeUnit.SECONDS.toMillis(1), // Максимальная задержка
                3 // Максимальное количество попыток
        );
    }

    /**
     * Настройка декодера ошибок
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    /**
     * Пользовательский декодер ошибок для Feign клиентов
     */
    public static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            // Здесь можно добавить кастомную логику обработки ошибок
            // Например, для разных HTTP статусов возвращать разные исключения
            
            switch (response.status()) {
                case 400:
                    return new BadRequestException("Bad request for method: " + methodKey);
                case 401:
                    return new UnauthorizedException("Unauthorized for method: " + methodKey);
                case 403:
                    return new ForbiddenException("Forbidden for method: " + methodKey);
                case 404:
                    return new NotFoundException("Resource not found for method: " + methodKey);
                case 429:
                    return new RateLimitExceededException("Rate limit exceeded for method: " + methodKey);
                case 500:
                    return new InternalServerErrorException("Internal server error for method: " + methodKey);
                case 502:
                    return new BadGatewayException("Bad gateway for method: " + methodKey);
                case 503:
                    return new ServiceUnavailableException("Service unavailable for method: " + methodKey);
                default:
                    return defaultErrorDecoder.decode(methodKey, response);
            }
        }
    }

    // Пользовательские исключения для Feign клиентов

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }

    public static class InternalServerErrorException extends RuntimeException {
        public InternalServerErrorException(String message) {
            super(message);
        }
    }

    public static class BadGatewayException extends RuntimeException {
        public BadGatewayException(String message) {
            super(message);
        }
    }

    public static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) {
            super(message);
        }
    }
}