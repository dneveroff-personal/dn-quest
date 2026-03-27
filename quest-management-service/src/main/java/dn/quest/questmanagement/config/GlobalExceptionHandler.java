package dn.quest.questmanagement.config;

import dn.quest.questmanagement.exception.*;
import dn.quest.shared.dto.ErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Глобальный обработчик исключений для Quest Management Service
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка исключения QuestNotFoundException
     */
    @ExceptionHandler(QuestNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleQuestNotFoundException(
            QuestNotFoundException ex, WebRequest request) {
        
        log.error("Quest not found: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .error("Quest not found")
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }

    /**
     * Обработка исключения QuestAccessDeniedException
     */
    @ExceptionHandler(QuestAccessDeniedException.class)
    public ResponseEntity<ErrorDTO> handleQuestAccessDeniedException(
            QuestAccessDeniedException ex, WebRequest request) {
        
        log.error("Access denied to quest: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.FORBIDDEN.value()))
                .error("Access denied")
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.FORBIDDEN);
    }

    /**
     * Обработка исключения QuestValidationException
     */
    @ExceptionHandler(QuestValidationException.class)
    public ResponseEntity<ErrorDTO> handleQuestValidationException(
            QuestValidationException ex, WebRequest request) {
        
        log.error("Quest validation failed: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .error("Quest validation failed")
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка исключения LevelNotFoundException
     */
    @ExceptionHandler(LevelNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleLevelNotFoundException(
            LevelNotFoundException ex, WebRequest request) {
        
        log.error("Level not found: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .error("Level not found")
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }

    /**
     * Обработка исключения LevelValidationException
     */
    @ExceptionHandler(LevelValidationException.class)
    public ResponseEntity<ErrorDTO> handleLevelValidationException(
            LevelValidationException ex, WebRequest request) {
        
        log.error("Level validation failed: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .error("Level validation failed")
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка исключения CodeNotFoundException
     */
    @ExceptionHandler(CodeNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleCodeNotFoundException(
            CodeNotFoundException ex, WebRequest request) {
        
        log.error("Code not found: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .error("Code not found")
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }

    /**
     * Обработка исключения CodeValidationException
     */
    @ExceptionHandler(CodeValidationException.class)
    public ResponseEntity<ErrorDTO> handleCodeValidationException(
            CodeValidationException ex, WebRequest request) {
        
        log.error("Code validation failed: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .error("Code validation failed")
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка исключения LevelHintNotFoundException
     */
    @ExceptionHandler(LevelHintNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleLevelHintNotFoundException(
            LevelHintNotFoundException ex, WebRequest request) {
        
        log.error("Level hint not found: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .error("Level hint not found")
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }

    /**
     * Обработка исключения HintValidationException
     */
    @ExceptionHandler(HintValidationException.class)
    public ResponseEntity<ErrorDTO> handleHintValidationException(
            HintValidationException ex, WebRequest request) {
        
        log.error("Hint validation failed: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .error("Hint validation failed")
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка исключений валидации DTO (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.error("DTO validation failed: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .error("DTO validation failed")
                .message("Validation errors: " + errors.toString())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка исключений валидации BindException
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorDTO> handleBindException(
            BindException ex, WebRequest request) {
        
        log.error("Binding validation failed: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .error("Binding validation failed")
                .message("Validation errors: " + errors.toString())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка исключений валидации ConstraintViolationException
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        log.error("Constraint validation failed: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .error("Constraint validation failed")
                .message("Validation errors: " + errors.toString())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        log.error("Illegal argument: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .error("Illegal argument")
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDTO> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        log.error("Illegal state: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.CONFLICT.value()))
                .error("Illegal state")
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);
    }

    /**
     * Обработка всех остальных исключений
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGlobalException(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .error("Internal server error")
                .message("An unexpected error occurred. Please try again later.")
                .details(request.getDescription(false))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}