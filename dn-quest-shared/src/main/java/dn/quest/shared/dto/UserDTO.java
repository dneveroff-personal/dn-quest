package dn.quest.shared.dto;

import dn.quest.shared.dto.BaseDTO;
import dn.quest.shared.enums.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO для представления пользователя
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends BaseDTO {
    
    /**
     * Имя пользователя (уникальное)
     */
    private String username;
    
    /**
     * Email пользователя
     */
    private String email;
    
    /**
     * Публичное имя (отображаемое)
     */
    private String publicName;
    
    /**
     * Роль пользователя
     */
    private UserRole role;
    
    /**
     * URL аватара
     */
    private String avatarUrl;
    
    /**
     * Описание пользователя
     */
    private String bio;
    
    /**
     * Дата последнего входа
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;
    
    /**
     * Активен ли пользователь
     */
    private Boolean active;
    
    /**
     * Подтвержден ли email
     */
    private Boolean emailVerified;
    
    /**
     * Дата верификации email
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime emailVerifiedAt;
}