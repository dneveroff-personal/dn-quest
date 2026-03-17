package dn.quest.shared.exceptions;

import dn.quest.shared.constants.ApplicationConstants;

/**
 * Общие исключения для микросервисов DN Quest
 */
public final class CommonExceptions {
    
    private CommonExceptions() {
        // Утилитарный класс
    }
    
    /**
     * Исключение валидации
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
        
        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Исключение аутентификации
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }
        
        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Исключение авторизации
     */
    public static class AuthorizationException extends RuntimeException {
        public AuthorizationException(String message) {
            super(message);
        }
        
        public AuthorizationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Исключение "ресурс не найден"
     */
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String resource, Long id) {
            super(String.format("%s with id %d not found", resource, id));
        }
        
        public ResourceNotFoundException(String resource, String identifier) {
            super(String.format("%s with identifier '%s' not found", resource, identifier));
        }
        
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
    
    /**
     * Исключение конфликта (дубликат)
     */
    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }
        
        public ConflictException(String resource, String field, Object value) {
            super(String.format("%s with %s '%s' already exists", resource, field, value));
        }
    }
    
    /**
     * Бизнес-исключение
     */
    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
        
        public BusinessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Исключение системной ошибки
     */
    public static class SystemException extends RuntimeException {
        public SystemException(String message) {
            super(message);
        }
        
        public SystemException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Исключение превышения лимита запросов
     */
    public static class RateLimitException extends RuntimeException {
        public RateLimitException(String message) {
            super(message);
        }
        
        public RateLimitException(String resource, int limit) {
            super(String.format("Rate limit exceeded for %s. Maximum allowed: %d", resource, limit));
        }
    }
    
    /**
     * Исключение внешнего сервиса
     */
    public static class ExternalServiceException extends RuntimeException {
        private final String serviceName;
        private final int statusCode;
        
        public ExternalServiceException(String serviceName, int statusCode, String message) {
            super(String.format("External service '%s' returned status %d: %s", serviceName, statusCode, message));
            this.serviceName = serviceName;
            this.statusCode = statusCode;
        }
        
        public ExternalServiceException(String serviceName, String message) {
            super(String.format("External service '%s' error: %s", serviceName, message));
            this.serviceName = serviceName;
            this.statusCode = -1;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
    }
    
    /**
     * Исключение недопустимого состояния
     */
    public static class IllegalStateException extends RuntimeException {
        public IllegalStateException(String message) {
            super(message);
        }
        
        public IllegalStateException(String resource, String currentState, String expectedState) {
            super(String.format("%s is in '%s' state, expected '%s'", resource, currentState, expectedState));
        }
    }
    
    /**
     * Исключение таймаута операции
     */
    public static class TimeoutException extends RuntimeException {
        public TimeoutException(String operation, long timeoutMs) {
            super(String.format("Operation '%s' timed out after %d ms", operation, timeoutMs));
        }
        
        public TimeoutException(String message) {
            super(message);
        }
    }
    
    /**
     * Фабрика для создания стандартных исключений
     */
    public static class ExceptionFactory {
        
        public static ValidationException validation(String message) {
            return new ValidationException(message);
        }
        
        public static AuthenticationException authentication(String message) {
            return new AuthenticationException(message);
        }
        
        public static AuthorizationException authorization(String message) {
            return new AuthorizationException(message);
        }
        
        public static ResourceNotFoundException notFound(String resource, Long id) {
            return new ResourceNotFoundException(resource, id);
        }
        
        public static ResourceNotFoundException notFound(String resource, String identifier) {
            return new ResourceNotFoundException(resource, identifier);
        }
        
        public static ConflictException conflict(String message) {
            return new ConflictException(message);
        }
        
        public static ConflictException conflict(String resource, String field, Object value) {
            return new ConflictException(resource, field, value);
        }
        
        public static BusinessException business(String message) {
            return new BusinessException(message);
        }
        
        public static SystemException system(String message) {
            return new SystemException(message);
        }
        
        public static SystemException system(String message, Throwable cause) {
            return new SystemException(message, cause);
        }
        
        public static RateLimitException rateLimit(String resource, int limit) {
            return new RateLimitException(resource, limit);
        }
        
        public static ExternalServiceException externalService(String serviceName, int statusCode, String message) {
            return new ExternalServiceException(serviceName, statusCode, message);
        }
        
        public static IllegalStateException illegalState(String resource, String currentState, String expectedState) {
            return new IllegalStateException(resource, currentState, expectedState);
        }
        
        public static TimeoutException timeout(String operation, long timeoutMs) {
            return new TimeoutException(operation, timeoutMs);
        }
    }
}