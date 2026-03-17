package dn.quest.questmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при валидации подсказки
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class HintValidationException extends RuntimeException {

    public HintValidationException(String message) {
        super(message);
    }

    public HintValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}