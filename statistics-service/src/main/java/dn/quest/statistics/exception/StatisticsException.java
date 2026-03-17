package dn.quest.statistics.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Базовое исключение для Statistics Service
 */
@Getter
public class StatisticsException extends RuntimeException {

    private final HttpStatus status;
    private final String error;
    private final Map<String, Object> details;

    public StatisticsException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.error = "Statistics Error";
        this.details = null;
    }

    public StatisticsException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.error = "Statistics Error";
        this.details = null;
    }

    public StatisticsException(String message, String error, HttpStatus status) {
        super(message);
        this.status = status;
        this.error = error;
        this.details = null;
    }

    public StatisticsException(String message, String error, HttpStatus status, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.error = error;
        this.details = details;
    }

    public StatisticsException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.error = "Statistics Error";
        this.details = null;
    }

    public StatisticsException(String message, String error, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.error = error;
        this.details = null;
    }
}