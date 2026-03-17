package dn.quest.teammanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на приглашение пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteUserRequest {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String username;

    @Size(max = 500, message = "Сообщение приглашения не должно превышать 500 символов")
    private String message;
}