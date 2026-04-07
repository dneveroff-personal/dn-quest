package dn.quest.teammanagement.exception;

/**
 * Исключение, выбрасываемое при ошибках взаимодействия с внешними сервисами
 */
public class ExternalServiceException extends RuntimeException {
    
    private final String serviceName;
    private final String errorCode;
    
    public ExternalServiceException(String message) {
        super(message);
        this.serviceName = "unknown";
        this.errorCode = "UNKNOWN_ERROR";
    }
    
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
        this.serviceName = "unknown";
        this.errorCode = "UNKNOWN_ERROR";
    }
    
    public ExternalServiceException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
        this.errorCode = "SERVICE_ERROR";
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.errorCode = "SERVICE_ERROR";
    }
    
    public ExternalServiceException(String serviceName, String message, String errorCode) {
        super(message);
        this.serviceName = serviceName;
        this.errorCode = errorCode;
    }
    
    public ExternalServiceException(String serviceName, String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.errorCode = errorCode;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public static ExternalServiceException authenticationServiceError(String message) {
        return new ExternalServiceException("authentication-service", message, "AUTH_ERROR");
    }
    
    public static ExternalServiceException userManagementServiceError(String message) {
        return new ExternalServiceException("user-management-service", message, "USER_ERROR");
    }
    
    public static ExternalServiceException gameEngineServiceError(String message) {
        return new ExternalServiceException("game-engine-service", message, "GAME_ERROR");
    }
    
    public static ExternalServiceException statisticsServiceError(String message) {
        return new ExternalServiceException("statistics-service", message, "STATS_ERROR");
    }
    
    public static ExternalServiceException notificationServiceError(String message) {
        return new ExternalServiceException("notification-service", message, "NOTIFICATION_ERROR");
    }
    
    public static ExternalServiceException fileStorageServiceError(String message) {
        return new ExternalServiceException("file-storage-service", message, "FILE_ERROR");
    }
    
    public static ExternalServiceException serviceUnavailable(String serviceName) {
        return new ExternalServiceException(serviceName, "Service is temporarily unavailable", "SERVICE_UNAVAILABLE");
    }
    
    public static ExternalServiceException serviceTimeout(String serviceName) {
        return new ExternalServiceException(serviceName, "Service request timed out", "SERVICE_TIMEOUT");
    }
}