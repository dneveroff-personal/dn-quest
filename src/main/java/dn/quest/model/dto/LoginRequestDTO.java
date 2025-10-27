package dn.quest.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDTO {
    
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(max = 50, message = "Имя пользователя не должно превышать 50 символов")
    private String username;
    
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(max = 100, message = "Пароль не должен превышать 100 символов")
    private String password;
}
