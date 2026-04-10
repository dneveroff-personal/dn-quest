package dn.quest.shared.exceptions;

import dn.quest.shared.constants.ApplicationConstants;
import dn.quest.shared.dto.ErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для всех микросервисов
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Обработка ошибок валидации DTO
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest webRequest) {
        
        List<ErrorDTO.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> this.createValidationError(fieldError))
                .collect(Collectors.toList());
        
        ErrorDTO errorDTO = ErrorDTO.ofValidation(
                "Validation failed",
                validationErrors
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Validation error: {}", errorDTO);
        return ResponseEntity.badRequest().body(errorDTO);
    }
    
    /**
     * Обработка ошибок валидации с BindException
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorDTO> handleBindException(
            BindException ex,
            WebRequest webRequest) {
        
        List<ErrorDTO.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> this.createValidationError(fieldError))
                .collect(Collectors.toList());
        
        ErrorDTO errorDTO = ErrorDTO.ofValidation(
                "Validation failed",
                validationErrors
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Bind validation error: {}", errorDTO);
        return ResponseEntity.badRequest().body(errorDTO);
    }
    
    /**
     * Обработка ошибок валидации constraint violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest webRequest) {
        
        List<ErrorDTO.ValidationError> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> this.createValidationError(violation))
                .collect(Collectors.toList());
        
        ErrorDTO errorDTO = ErrorDTO.ofValidation(
                "Constraint validation failed",
                validationErrors
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Constraint validation error: {}", errorDTO);
        return ResponseEntity.badRequest().body(errorDTO);
    }
    
    /**
     * Обработка ошибок аутентификации Spring Security
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDTO> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                ApplicationConstants.ErrorCodes.AUTHENTICATION_ERROR,
                "Authentication failed: " + ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Authentication error: {}", errorDTO.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDTO);
    }
    
    /**
     * Обработка ошибок авторизации Spring Security
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDTO> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                ApplicationConstants.ErrorCodes.AUTHORIZATION_ERROR,
                "Access denied: " + ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Authorization error: {}", errorDTO.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDTO);
    }
    
    /**
     * Обработка кастомных исключений валидации
     */
    @ExceptionHandler(CommonExceptions.ValidationException.class)
    public ResponseEntity<ErrorDTO> handleValidationException(
            CommonExceptions.ValidationException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                ApplicationConstants.ErrorCodes.VALIDATION_ERROR,
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Custom validation error: {}", errorDTO.getMessage());
        return ResponseEntity.badRequest().body(errorDTO);
    }
    
    /**
     * Обработка исключения "ресурс не найден"
     */
    @ExceptionHandler(CommonExceptions.ResourceNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleResourceNotFoundException(
            CommonExceptions.ResourceNotFoundException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                ApplicationConstants.ErrorCodes.NOT_FOUND_ERROR,
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Resource not found: {}", errorDTO.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDTO);
    }
    
    /**
     * Обработка исключения конфликта
     */
    @ExceptionHandler(CommonExceptions.ConflictException.class)
    public ResponseEntity<ErrorDTO> handleConflictException(
            CommonExceptions.ConflictException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                ApplicationConstants.ErrorCodes.CONFLICT_ERROR,
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Conflict error: {}", errorDTO.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDTO);
    }
    
    /**
     * Обработка бизнес-исключений
     */
    @ExceptionHandler(CommonExceptions.BusinessException.class)
    public ResponseEntity<ErrorDTO> handleBusinessException(
            CommonExceptions.BusinessException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                ApplicationConstants.ErrorCodes.BUSINESS_ERROR,
                ex.getMessage(),
                HttpStatus.UNPROCESSABLE_ENTITY.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Business error: {}", errorDTO.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorDTO);
    }
    
    /**
     * Обработка исключения превышения лимита запросов
     */
    @ExceptionHandler(CommonExceptions.RateLimitException.class)
    public ResponseEntity<ErrorDTO> handleRateLimitException(
            CommonExceptions.RateLimitException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                ApplicationConstants.ErrorCodes.RATE_LIMIT_ERROR,
                ex.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Rate limit error: {}", errorDTO.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorDTO);
    }
    
    /**
     * Обработка ошибок HTTP метода
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDTO> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                "METHOD_NOT_SUPPORTED",
                "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
                HttpStatus.METHOD_NOT_ALLOWED.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Method not supported: {}", errorDTO.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorDTO);
    }
    
    /**
     * Обработка ошибок парсинга JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                "INVALID_JSON",
                "Invalid JSON format: " + ex.getMostSpecificCause().getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Invalid JSON: {}", errorDTO.getMessage());
        return ResponseEntity.badRequest().body(errorDTO);
    }
    
    /**
     * Обработка ошибок отсутствия параметров запроса
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDTO> handleMissingParameter(
            MissingServletRequestParameterException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                "MISSING_PARAMETER",
                "Required parameter '" + ex.getParameterName() + "' is missing",
                HttpStatus.BAD_REQUEST.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Missing parameter: {}", errorDTO.getMessage());
        return ResponseEntity.badRequest().body(errorDTO);
    }
    
    /**
     * Обработка ошибок несоответствия типов аргументов
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDTO> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                "TYPE_MISMATCH",
                "Parameter '" + ex.getName() + "' should be of type " + ex.getRequiredType().getSimpleName(),
                HttpStatus.BAD_REQUEST.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Type mismatch: {}", errorDTO.getMessage());
        return ResponseEntity.badRequest().body(errorDTO);
    }
    
    /**
     * Обработка ошибок 404 (эндпоинт не найден)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorDTO> handleNoHandlerFound(
            NoHandlerFoundException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                "NOT_FOUND",
                "Endpoint not found: " + ex.getRequestURL(),
                HttpStatus.NOT_FOUND.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Endpoint not found: {}", errorDTO.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDTO);
    }
    
    /**
     * Обработка IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                "ILLEGAL_ARGUMENT",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(errorDTO);
    }
    
    /**
     * Обработка IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDTO> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                "ILLEGAL_STATE",
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDTO);
    }
    
    /**
     * Обработка EntityNotFoundException
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleEntityNotFoundException(
            EntityNotFoundException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                ApplicationConstants.ErrorCodes.NOT_FOUND_ERROR,
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDTO);
    }
    
    /**
     * Обработка BadCredentialsException
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDTO> handleBadCredentialsException(
            BadCredentialsException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                ApplicationConstants.ErrorCodes.AUTHENTICATION_ERROR,
                "Invalid credentials",
                HttpStatus.UNAUTHORIZED.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDTO);
    }
    
    /**
     * Обработка MaxUploadSizeExceededException
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorDTO> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                "FILE_TOO_LARGE",
                "Maximum upload size exceeded",
                HttpStatus.PAYLOAD_TOO_LARGE.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        
        log.warn("Max upload size exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorDTO);
    }
    
    /**
     * Обработка всех остальных исключений
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGenericException(
            Exception ex,
            WebRequest webRequest) {
        
        ErrorDTO errorDTO = ErrorDTO.of(
                ApplicationConstants.ErrorCodes.SYSTEM_ERROR,
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        errorDTO.setPath(getPathFromWebRequest(webRequest));
        errorDTO.setDetails(ex.getMessage());
        
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
    }
    
    /**
     * Извлекает путь запроса из WebRequest
     */
    private String getPathFromWebRequest(WebRequest webRequest) {
        String description = webRequest.getDescription(false);
        if (description != null && description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description;
    }
    
    /**
     * Создает ошибку валидации из FieldError
     */
    private ErrorDTO.ValidationError createValidationError(FieldError fieldError) {
        return ErrorDTO.ValidationError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }
    
    /**
     * Создает ошибку валидации из ConstraintViolation
     */
    private ErrorDTO.ValidationError createValidationError(ConstraintViolation<?> violation) {
        String fieldName = violation.getPropertyPath().toString();
        if (fieldName.contains(".")) {
            fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
        }
        
        return ErrorDTO.ValidationError.builder()
                .field(fieldName)
                .message(violation.getMessage())
                .rejectedValue(violation.getInvalidValue())
                .build();
    }
}