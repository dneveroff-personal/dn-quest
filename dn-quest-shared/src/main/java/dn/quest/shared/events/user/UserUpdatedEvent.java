package dn.quest.shared.events.user;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие обновления данных пользователя
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserUpdatedEvent extends BaseEvent {

    /**
     * ID пользователя
     */
    private String userId;

    /**
     * Email пользователя (если изменился)
     */
    private String email;

    /**
     * Username пользователя (если изменился)
     */
    private String username;

    /**
     * Имя пользователя (если изменилось)
     */
    private String firstName;

    /**
     * Фамилия пользователя (если изменилась)
     */
    private String lastName;

    /**
     * Роль пользователя (если изменилась)
     */
    private String role;

    /**
     * Статус пользователя (active, blocked, etc.)
     */
    private String status;

    /**
     * IP адрес обновления
     */
    private String updateIp;

    /**
     * User Agent при обновлении
     */
    private String userAgent;

    /**
     * Список измененных полей
     */
    private java.util.List<String> changedFields;

    /**
     * Создание события обновления пользователя
     */
    public static UserUpdatedEvent create(String userId, String email, String username,
                                        String firstName, String lastName, String role,
                                        String status, String updateIp, String userAgent,
                                        java.util.List<String> changedFields, String correlationId) {
        return UserUpdatedEvent.builder()
                .eventType("UserUpdated")
                .eventVersion("1.0")
                .source("user-management-service")
                .correlationId(correlationId)
                .userId(userId)
                .email(email)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .status(status)
                .updateIp(updateIp)
                .userAgent(userAgent)
                .changedFields(changedFields)
                .data(Map.of(
                        "userId", userId,
                        "email", email,
                        "username", username,
                        "firstName", firstName,
                        "lastName", lastName,
                        "role", role,
                        "status", status,
                        "updateIp", updateIp,
                        "userAgent", userAgent,
                        "changedFields", changedFields
                ))
                .build();
    }
}