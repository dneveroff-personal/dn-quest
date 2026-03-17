package dn.quest.teammanagement.exception;

import java.util.Map;

/**
 * Исключение, выбрасываемое при ошибках валидации команды
 */
public class TeamValidationException extends RuntimeException {
    
    private final Map<String, String> errors;
    
    public TeamValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }
    
    public TeamValidationException(String message, Map<String, String> errors, Throwable cause) {
        super(message, cause);
        this.errors = errors;
    }
    
    public Map<String, String> getErrors() {
        return errors;
    }
    
    public static TeamValidationException invalidTeamData(Map<String, String> errors) {
        return new TeamValidationException("Invalid team data", errors);
    }
    
    public static TeamValidationException invalidTeamName(String error) {
        return new TeamValidationException("Invalid team name", Map.of("name", error));
    }
    
    public static TeamValidationException invalidTeamTag(String error) {
        return new TeamValidationException("Invalid team tag", Map.of("tag", error));
    }
    
    public static TeamValidationException invalidTeamDescription(String error) {
        return new TeamValidationException("Invalid team description", Map.of("description", error));
    }
    
    public static TeamValidationException invalidTeamSettings(Map<String, String> errors) {
        return new TeamValidationException("Invalid team settings", errors);
    }
}