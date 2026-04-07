package dn.quest.statistics.exception;

import java.util.Map;

/**
 * Исключение для ошибок агрегации статистических данных
 */
public class AggregationException extends StatisticsException {

    public AggregationException(String message) {
        super(message, "Aggregation Error", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public AggregationException(String message, Throwable cause) {
        super(message, "Aggregation Error", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    public AggregationException(String message, Map<String, Object> details) {
        super(message, "Aggregation Error", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, details);
    }

    public AggregationException(String operation, String entityType, String reason) {
        super(String.format("Failed to aggregate %s for %s: %s", operation, entityType, reason), 
              "Aggregation Error", 
              org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public AggregationException(String operation, String entityType, String reason, Throwable cause) {
        super(String.format("Failed to aggregate %s for %s: %s", operation, entityType, reason), 
              "Aggregation Error", 
              org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, 
              cause);
    }
}