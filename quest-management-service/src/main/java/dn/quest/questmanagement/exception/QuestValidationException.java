package dn.quest.questmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при валидации квеста
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class QuestValidationException extends RuntimeException {

    public QuestValidationException(String message) {
        super(message);
    }

    public QuestValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}