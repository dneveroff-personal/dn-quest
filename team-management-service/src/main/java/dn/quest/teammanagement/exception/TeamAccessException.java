package dn.quest.teammanagement.exception;

import java.util.UUID;

/**
 * Исключение, выбрасываемое когда нет доступа к команде
 */
public class TeamAccessException extends RuntimeException {
    
    public TeamAccessException(String message) {
        super(message);
    }
    
    public TeamAccessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TeamAccessException(UUID teamId, UUID userId) {
        super("User " + userId + " does not have access to team " + teamId);
    }
    
    public static TeamAccessException insufficientPermissions(UUID teamId, UUID userId) {
        return new TeamAccessException("User " + userId + " has insufficient permissions for team " + teamId);
    }
    
    public static TeamAccessException notMember(UUID teamId, UUID userId) {
        return new TeamAccessException("User " + userId + " is not a member of team " + teamId);
    }
}