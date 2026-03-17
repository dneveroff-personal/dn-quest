package dn.quest.questmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое при валидации кода
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CodeValidationException extends RuntimeException {

    public CodeValidationException(String message) {
        super(message);
    }

    public CodeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}