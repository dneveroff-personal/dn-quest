package dn.quest.questmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при валидации уровня
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LevelValidationException extends RuntimeException {

    public LevelValidationException(String message) {
        super(message);
    }

    public LevelValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}