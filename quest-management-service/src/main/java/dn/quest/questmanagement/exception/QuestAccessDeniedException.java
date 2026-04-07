package dn.quest.questmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение, выбрасываемое когда доступ к квесту запрещен
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class QuestAccessDeniedException extends RuntimeException {

    public QuestAccessDeniedException(String message) {
        super(message);
    }

    public QuestAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}