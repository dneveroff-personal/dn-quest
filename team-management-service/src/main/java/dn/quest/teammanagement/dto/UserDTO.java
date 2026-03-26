package dn.quest.teammanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO для пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private Boolean isActive;
    private String role;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Получить полное имя пользователя
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username;
        }
    }

    /**
     * Получить отображаемое имя (полное имя или username)
     */
    public String getDisplayName() {
        String fullName = getFullName();
        return !fullName.equals(username) ? fullName : username;
    }
}