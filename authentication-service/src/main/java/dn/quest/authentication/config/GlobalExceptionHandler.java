package dn.quest.authentication.config;

import dn.quest.shared.dto.ErrorDTO;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "400", description = "Неверный запрос",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ErrorDTO handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("Неверный запрос: {}", ex.getMessage());
        return ErrorDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponse(responseCode = "404", description = "Ресурс не найден",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ErrorDTO handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        log.warn("Ресурс не найден: {}", ex.getMessage());
        return ErrorDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ApiResponse(responseCode = "401", description = "Неверные учетные данные",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ErrorDTO handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        log.warn("Неверные учетные данные: {}", ex.getMessage());
        return ErrorDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Неверные учетные данные")
                .path(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ApiResponse(responseCode = "401", description = "Ошибка аутентификации",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ErrorDTO handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("Ошибка аутентификации: {}", ex.getMessage());
        return ErrorDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Ошибка аутентификации")
                .path(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ApiResponse(responseCode = "403", description = "Доступ запрещен",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ErrorDTO handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.warn("Доступ запрещен: {}", ex.getMessage());
        return ErrorDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("Доступ запрещен")
                .path(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ErrorDTO handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? 
                                fieldError.getDefaultMessage() : "Invalid value"
                ));

        log.warn("Ошибка валидации: {}", errors);
        
        return ErrorDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Ошибка валидации данных")
                .validationErrors(errors)
                .path(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "400", description = "Ошибка привязки данных",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ErrorDTO handleBindException(BindException ex, WebRequest request) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? 
                                fieldError.getDefaultMessage() : "Invalid value"
                ));

        log.warn("Ошибка привязки данных: {}", errors);
        
        return ErrorDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Binding Error")
                .message("Ошибка привязки данных")
                .validationErrors(errors)
                .path(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "400", description = "Ошибка валидации ограничений",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ErrorDTO handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        log.warn("Ошибка валидации ограничений: {}", errors);
        
        return ErrorDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .message("Ошибка валидации ограничений")
                .validationErrors(errors)
                .path(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ErrorDTO handleGenericException(Exception ex, WebRequest request) {
        log.error("Внутренняя ошибка сервера", ex);
        
        return ErrorDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Внутренняя ошибка сервера")
                .path(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(responseCode = "500", description = "Ошибка выполнения",
            content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    public ErrorDTO handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Ошибка выполнения", ex);
        
        return ErrorDTO.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Runtime Error")
                .message("Ошибка выполнения")
                .path(request.getDescription(false))
                .build();
    }
}