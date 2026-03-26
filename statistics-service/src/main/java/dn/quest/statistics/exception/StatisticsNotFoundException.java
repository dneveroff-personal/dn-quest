package dn.quest.statistics.exception;

/**
 * Исключение для случаев, когда статистические данные не найдены
 */
public class StatisticsNotFoundException extends StatisticsException {

    public StatisticsNotFoundException(String message) {
        super(message, "Statistics Not Found", org.springframework.http.HttpStatus.NOT_FOUND);
    }

    public StatisticsNotFoundException(String entityType, Long entityId) {
        super(String.format("%s with ID %d not found", entityType, entityId), 
              "Statistics Not Found", 
              org.springframework.http.HttpStatus.NOT_FOUND);
    }

    public StatisticsNotFoundException(String entityType, String identifier) {
        super(String.format("%s with identifier %s not found", entityType, identifier), 
              "Statistics Not Found", 
              org.springframework.http.HttpStatus.NOT_FOUND);
    }

    public StatisticsNotFoundException(String message, Throwable cause) {
        super(message, "Statistics Not Found", org.springframework.http.HttpStatus.NOT_FOUND, cause);
    }
}