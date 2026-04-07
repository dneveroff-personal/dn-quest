package dn.quest.questmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое когда подсказка уровня не найдена
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class LevelHintNotFoundException extends RuntimeException {

    public LevelHintNotFoundException(String message) {
        super(message);
    }

    public LevelHintNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}