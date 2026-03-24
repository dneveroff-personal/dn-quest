package dn.quest.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Базовый DTO класс с общими полями
 */
@Data
public abstract class BaseDTO {

    /**
     * Конструктор по умолчанию
     */
    protected BaseDTO() {
    }

    /**
     * Создает новый BaseDTO с указанным ID
     * @param id идентификатор
     * @return новый объект BaseDTO
     */
    public static BaseDTO create(Long id) {
        return new BaseDTO() {{ setId(id); }};
    }

    /**
     * Идентификатор сущности
     */
    private Long id;
    
    /**
     * Дата и время создания
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * Дата и время последнего обновления
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * Версия для оптимистичной блокировки
     */
    private Integer version;
}