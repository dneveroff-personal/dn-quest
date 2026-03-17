package dn.quest.shared.events.user;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие регистрации нового пользователя
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserRegisteredEvent extends BaseEvent {

    /**
     * ID пользователя
     */
    private String userId;

    /**
     * Email пользователя
     */
    private String email;

    /**
     * Username пользователя
     */
    private String username;

    /**
     * Имя пользователя
     */
    private String firstName;

    /**
     * Фамилия пользователя
     */
    private String lastName;

    /**
     * Роль пользователя
     */
    private String role;

    /**
     * IP адрес регистрации
     */
    private String registrationIp;

    /**
     * User Agent при регистрации
     */
    private String userAgent;

    /**
     * Флаг верификации email
     */
    private Boolean emailVerified;

    /**
     * Создание события регистрации пользователя
     */
    public static UserRegisteredEvent create(String userId, String email, String username, 
                                           String firstName, String lastName, String role,
                                           String registrationIp, String userAgent,
                                           Boolean emailVerified, String correlationId) {
        return UserRegisteredEvent.builder()
                .eventType("UserRegistered")
                .eventVersion("1.0")
                .source("user-management-service")
                .correlationId(correlationId)
                .userId(userId)
                .email(email)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .registrationIp(registrationIp)
                .userAgent(userAgent)
                .emailVerified(emailVerified)
                .data(Map.of(
                        "userId", userId,
                        "email", email,
                        "username", username,
                        "firstName", firstName,
                        "lastName", lastName,
                        "role", role,
                        "registrationIp", registrationIp,
                        "userAgent", userAgent,
                        "emailVerified", emailVerified
                ))
                .build();
    }
}