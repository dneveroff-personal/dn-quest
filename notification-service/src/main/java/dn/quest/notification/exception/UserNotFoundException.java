package dn.quest.notification.exception;

import java.util.UUID;

/**
 * Исключение для случаев, когда пользователь не найден
 */
public class UserNotFoundException extends NotificationException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(UUID userId) {
        super(String.format("Пользователь не найден: id=%d", userId));
    }
}