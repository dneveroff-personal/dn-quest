package dn.quest.teammanagement.exception;

/**
 * Исключение, выбрасываемое при превышении лимитов ресурсов
 */
public class ResourceLimitExceededException extends RuntimeException {
    
    public ResourceLimitExceededException(String message) {
        super(message);
    }
    
    public ResourceLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static ResourceLimitExceededException teamLimitExceeded(Long userId, int current, int limit) {
        return new ResourceLimitExceededException(
                "User " + userId + " has exceeded team limit. Current: " + current + ", Limit: " + limit);
    }
    
    public static ResourceLimitExceededException memberLimitExceeded(Long teamId, int current, int limit) {
        return new ResourceLimitExceededException(
                "Team " + teamId + " has exceeded member limit. Current: " + current + ", Limit: " + limit);
    }
    
    public static ResourceLimitExceededException invitationLimitExceeded(Long teamId, int current, int limit) {
        return new ResourceLimitExceededException(
                "Team " + teamId + " has exceeded invitation limit. Current: " + current + ", Limit: " + limit);
    }
    
    public static ResourceLimitExceededException dailyInvitationLimitExceeded(Long userId, int current, int limit) {
        return new ResourceLimitExceededException(
                "User " + userId + " has exceeded daily invitation limit. Current: " + current + ", Limit: " + limit);
    }
}