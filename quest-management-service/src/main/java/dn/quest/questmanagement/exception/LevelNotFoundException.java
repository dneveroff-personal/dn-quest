package dn.quest.questmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое когда уровень не найден
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LevelNotFoundException extends RuntimeException {

    public LevelNotFoundException(String message) {
        super(message);
    }

    public LevelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}