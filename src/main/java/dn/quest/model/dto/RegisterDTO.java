package dn.quest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDTO {

    private String username;    // обязателен сейчас
    private String publicName;  // необязателен; по умолчанию = username
    private String email;       // пока не сохраняем (DTO UserDTO его не несёт)
    private String password;    // пока не сохраняем (пароли добавим, когда расширим модель)

}