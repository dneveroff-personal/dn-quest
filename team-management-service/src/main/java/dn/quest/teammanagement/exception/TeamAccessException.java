package dn.quest.teammanagement.exception;

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
    
    public TeamAccessException(Long teamId, Long userId) {
        super("User " + userId + " does not have access to team " + teamId);
    }
    
    public static TeamAccessException insufficientPermissions(Long teamId, Long userId) {
        return new TeamAccessException("User " + userId + " has insufficient permissions for team " + teamId);
    }
    
    public static TeamAccessException notMember(Long teamId, Long userId) {
        return new TeamAccessException("User " + userId + " is not a member of team " + teamId);
    }
}