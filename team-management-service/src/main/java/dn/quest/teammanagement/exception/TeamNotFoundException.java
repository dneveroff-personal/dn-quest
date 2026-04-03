package dn.quest.teammanagement.exception;

import java.util.UUID;

/**
 * Исключение, выбрасываемое когда команда не найдена
 */
public class TeamNotFoundException extends RuntimeException {
    
    public TeamNotFoundException(String message) {
        super(message);
    }
    
    public TeamNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TeamNotFoundException(UUID teamId) {
        super("Team not found with id: " + teamId);
    }
    
    public TeamNotFoundException(String teamName, String teamTag) {
        super("Team not found with name: " + teamName + " and tag: " + teamTag);
    }
}