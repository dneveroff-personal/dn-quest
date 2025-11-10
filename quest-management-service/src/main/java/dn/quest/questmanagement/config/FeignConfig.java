package dn.quest.questmanagement.config;

import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Feign клиентов
 */
@Configuration
public class FeignConfig {

    /**
     * Настройка уровня логирования для Feign клиентов
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Настройка политики повторных попыток
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, 3000, 3);
    }

    /**
     * Настройка декодера ошибок
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    /**
     * Пользовательский декодер ошибок
     */
    public static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            // Логирование ошибок
            System.err.println("Feign client error: " + methodKey + " - Status: " + response.status());
            
            // Можно добавить кастомную обработку ошибок
            switch (response.status()) {
                case 400:
                    return new BadRequestException("Bad request for " + methodKey);
                case 401:
                    return new UnauthorizedException("Unauthorized for " + methodKey);
                case 403:
                    return new ForbiddenException("Forbidden for " + methodKey);
                case 404:
                    return new NotFoundException("Not found for " + methodKey);
                case 500:
                    return new InternalServerErrorException("Internal server error for " + methodKey);
                default:
                    return defaultErrorDecoder.decode(methodKey, response);
            }
        }
    }

    // Пользовательские исключения
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

    public static class InternalServerErrorException extends RuntimeException {
        public InternalServerErrorException(String message) {
            super(message);
        }
    }
}