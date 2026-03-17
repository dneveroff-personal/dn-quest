package dn.quest.teammanagement.exception;

/**
 * Исключение, выбрасываемое при попытке создать дублирующийся ресурс
 */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static DuplicateResourceException teamNameExists(String name) {
        return new DuplicateResourceException("Team with name '" + name + "' already exists");
    }
    
    public static DuplicateResourceException teamTagExists(String tag) {
        return new DuplicateResourceException("Team with tag '" + tag + "' already exists");
    }
    
    public static DuplicateResourceException teamMemberExists(Long teamId, Long userId) {
        return new DuplicateResourceException("User " + userId + " is already a member of team " + teamId);
    }
    
    public static DuplicateResourceException invitationExists(Long teamId, Long userId) {
        return new DuplicateResourceException("Invitation already exists for team " + teamId + " and user " + userId);
    }
    
    public static DuplicateResourceException teamSettingsExist(Long teamId) {
        return new DuplicateResourceException("Team settings already exist for team " + teamId);
    }
    
    public static DuplicateResourceException teamStatisticsExist(Long teamId) {
        return new DuplicateResourceException("Team statistics already exist for team " + teamId);
    }
}