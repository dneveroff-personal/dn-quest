package dn.quest.teammanagement.exception;

import java.util.UUID;

/**
 * Исключение, выбрасываемое при нарушении бизнес-правил
 */
public class BusinessRuleException extends RuntimeException {
    
    public BusinessRuleException(String message) {
        super(message);
    }
    
    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static BusinessRuleException captainCannotLeaveTeam(UUID teamId) {
        return new BusinessRuleException("Captain cannot leave team " + teamId + " without transferring captaincy");
    }
    
    public static BusinessRuleException teamMustHaveCaptain(UUID teamId) {
        return new BusinessRuleException("Team " + teamId + " must have at least one captain");
    }
    
    public static BusinessRuleException userAlreadyInTeam(UUID userId, UUID teamId) {
        return new BusinessRuleException("User " + userId + " is already a member of team " + teamId);
    }
    
    public static BusinessRuleException userNotInTeam(UUID userId, UUID teamId) {
        return new BusinessRuleException("User " + userId + " is not a member of team " + teamId);
    }
    
    public static BusinessRuleException cannotRemoveLastMember(UUID teamId) {
        return new BusinessRuleException("Cannot remove last member from team " + teamId);
    }
    
    public static BusinessRuleException invitationAlreadyExists(UUID teamId, UUID userId) {
        return new BusinessRuleException("Invitation already exists for team " + teamId + " and user " + userId);
    }
    
    public static BusinessRuleException teamLimitExceeded(UUID userId, int limit) {
        return new BusinessRuleException("User " + userId + " has exceeded the team limit of " + limit);
    }
    
    public static BusinessRuleException memberLimitExceeded(UUID teamId, int limit) {
        return new BusinessRuleException("Team " + teamId + " has exceeded the member limit of " + limit);
    }
}