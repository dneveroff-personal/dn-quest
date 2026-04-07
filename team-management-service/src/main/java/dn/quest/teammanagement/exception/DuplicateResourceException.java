package dn.quest.teammanagement.exception;

import java.util.UUID;

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
    
    public static DuplicateResourceException teamMemberExists(UUID teamId, UUID userId) {
        return new DuplicateResourceException("User " + userId + " is already a member of team " + teamId);
    }
    
    public static DuplicateResourceException invitationExists(UUID teamId, UUID userId) {
        return new DuplicateResourceException("Invitation already exists for team " + teamId + " and user " + userId);
    }
    
    public static DuplicateResourceException teamSettingsExist(UUID teamId) {
        return new DuplicateResourceException("Team settings already exist for team " + teamId);
    }
    
    public static DuplicateResourceException teamStatisticsExist(UUID teamId) {
        return new DuplicateResourceException("Team statistics already exist for team " + teamId);
    }
}