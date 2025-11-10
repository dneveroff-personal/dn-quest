package dn.quest.teammanagement.exception;

import java.util.Map;

/**
 * Исключение, выбрасываемое при ошибках валидации приглашения
 */
public class InvitationValidationException extends RuntimeException {
    
    private final Map<String, String> errors;
    
    public InvitationValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }
    
    public InvitationValidationException(String message, Map<String, String> errors, Throwable cause) {
        super(message, cause);
        this.errors = errors;
    }
    
    public Map<String, String> getErrors() {
        return errors;
    }
    
    public static InvitationValidationException invalidInvitationData(Map<String, String> errors) {
        return new InvitationValidationException("Invalid invitation data", errors);
    }
    
    public static InvitationValidationException invalidInvitationMessage(String error) {
        return new InvitationValidationException("Invalid invitation message", Map.of("message", error));
    }
    
    public static InvitationValidationException invalidInvitationResponse(String error) {
        return new InvitationValidationException("Invalid invitation response", Map.of("response", error));
    }
    
    public static InvitationValidationException invitationExpired() {
        return new InvitationValidationException("Invitation has expired", Map.of("status", "expired"));
    }
    
    public static InvitationValidationException invitationAlreadyResponded() {
        return new InvitationValidationException("Invitation has already been responded to", Map.of("status", "already_responded"));
    }
    
    public static InvitationValidationException cannotInviteSelf() {
        return new InvitationValidationException("Cannot invite yourself to a team", Map.of("user", "self_invitation"));
    }
}