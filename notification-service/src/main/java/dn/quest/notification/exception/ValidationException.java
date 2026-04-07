package dn.quest.notification.exception;

/**
 * Исключение для ошибок валидации
 */
public class ValidationException extends NotificationException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}