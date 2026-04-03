package dn.quest.teammanagement.exception;

import java.util.UUID;

/**
 * Исключение, выбрасываемое когда приглашение не найдено
 */
public class InvitationNotFoundException extends RuntimeException {
    
    public InvitationNotFoundException(String message) {
        super(message);
    }
    
    public InvitationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvitationNotFoundException(Long invitationId) {
        super("Invitation not found with id: " + invitationId);
    }
    
    public static InvitationNotFoundException byTeamAndUser(UUID teamId, UUID userId) {
        return new InvitationNotFoundException("Invitation not found for team id: " + teamId + " and user id: " + userId);
    }
}