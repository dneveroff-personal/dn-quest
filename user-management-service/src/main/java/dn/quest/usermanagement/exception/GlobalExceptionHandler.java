package dn.quest.usermanagement.exception;

import dn.quest.shared.dto.ErrorDTO;
import dn.quest.shared.exceptions.CommonExceptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для User Management Service
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Обработка исключения когда сущность не найдена
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        
        log.warn("Сущность не найдена: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Сущность не найдена")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDTO);
    }

    /**
     * Обработка исключения нарушения ограничений валидации
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        log.warn("Нарушение ограничений валидации: {}", ex.getMessage());
        
        Map<String, String> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Ошибка валидации")
                .message("Нарушены ограничения валидации")
                .validationErrors((List<ErrorDTO.ValidationError>) violations)
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    /**
     * Обработка исключения валидации аргументов метода
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.warn("Ошибка валидации аргументов метода: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Ошибка валидации")
                .message("Некорректные данные запроса")
                .validationErrors((List<ErrorDTO.ValidationError>) errors)
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    /**
     * Обработка исключения доступа запрещен
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDTO> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        log.warn("Доступ запрещен: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Доступ запрещен")
                .message("У вас нет прав для выполнения этой операции")
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDTO);
    }

    /**
     * Обработка исключения пользователь уже существует
     */
    @ExceptionHandler(UserManagementExceptions.UserAlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleUserAlreadyExistsException(
            UserManagementExceptions.UserAlreadyExistsException ex, WebRequest request) {
        
        log.warn("Пользователь уже существует: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Конфликт данных")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDTO);
    }

    /**
     * Обработка исключения пользователь не найден
     */
    @ExceptionHandler(UserManagementExceptions.UserNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleUserNotFoundException(
            UserManagementExceptions.UserNotFoundException ex, WebRequest request) {
        
        log.warn("Пользователь не найден: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Пользователь не найден")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDTO);
    }

    /**
     * Обработка исключения недопустимая операция
     */
    @ExceptionHandler(UserManagementExceptions.IllegalOperationException.class)
    public ResponseEntity<ErrorDTO> handleIllegalOperationException(
            UserManagementExceptions.IllegalOperationException ex, WebRequest request) {
        
        log.warn("Недопустимая операция: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Недопустимая операция")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    /**
     * Обработка исключения внешнего сервиса
     */
    @ExceptionHandler(CommonExceptions.ExternalServiceException.class)
    public ResponseEntity<ErrorDTO> handleExternalServiceException(
            CommonExceptions.ExternalServiceException ex, WebRequest request) {
        
        log.error("Ошибка внешнего сервиса: {}", ex.getMessage(), ex);
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("Ошибка внешнего сервиса")
                .message("Временная проблема с внешним сервисом. Попробуйте позже.")
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorDTO);
    }

    /**
     * Обработка исключения бизнес-логики
     */
    @ExceptionHandler(CommonExceptions.BusinessException.class)
    public ResponseEntity<ErrorDTO> handleBusinessException(
            CommonExceptions.BusinessException ex, WebRequest request) {
        
        log.warn("Ошибка бизнес-логики: {}", ex.getMessage());
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Ошибка бизнес-логики")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorDTO);
    }

    /**
     * Обработка всех остальных исключений
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGlobalException(
            Exception ex, WebRequest request) {
        
        log.error("Непредвиденная ошибка: {}", ex.getMessage(), ex);
        
        ErrorDTO errorDTO = ErrorDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Внутренняя ошибка сервера")
                .message("Произошла непредвиденная ошибка. Пожалуйста, обратитесь в поддержку.")
                .path(request.getDescription(false))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
    }
}