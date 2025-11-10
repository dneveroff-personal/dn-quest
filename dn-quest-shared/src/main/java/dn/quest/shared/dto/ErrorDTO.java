package dn.quest.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для представления ошибок в API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {
    
    /**
     * Код ошибки
     */
    private String code;
    
    /**
     * Сообщение об ошибке
     */
    private String message;
    
    /**
     * Детальное описание ошибки
     */
    private String details;
    
    /**
     * Время возникновения ошибки
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * Путь запроса, вызвавшего ошибку
     */
    private String path;
    
    /**
     * HTTP статус
     */
    private Integer status;
    
    /**
     * Список ошибок валидации
     */
    private List<ValidationError> validationErrors;
    
    /**
     * Внутренний класс для ошибок валидации
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        
        /**
         * Поле с ошибкой
         */
        private String field;
        
        /**
         * Сообщение об ошибке для поля
         */
        private String message;
        
        /**
         * Отклоненное значение
         */
        private Object rejectedValue;
    }
    
    /**
     * Создает базовую ошибку
     */
    public static ErrorDTO of(String code, String message, Integer status) {
        return ErrorDTO.builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(status)
                .build();
    }
    
    /**
     * Создает ошибку с деталями
     */
    public static ErrorDTO of(String code, String message, String details, Integer status) {
        return ErrorDTO.builder()
                .code(code)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .status(status)
                .build();
    }
    
    /**
     * Создает ошибку валидации
     */
    public static ErrorDTO ofValidation(String message, List<ValidationError> validationErrors) {
        return ErrorDTO.builder()
                .code("VALIDATION_ERROR")
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(400)
                .validationErrors(validationErrors)
                .build();
    }
}