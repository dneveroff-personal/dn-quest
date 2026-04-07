package dn.quest.questmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое когда квест не найден
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class QuestNotFoundException extends RuntimeException {

    public QuestNotFoundException(String message) {
        super(message);
    }

    public QuestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}