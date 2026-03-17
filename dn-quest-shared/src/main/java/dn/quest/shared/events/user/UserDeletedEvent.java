package dn.quest.shared.events.user;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие удаления пользователя
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDeletedEvent extends BaseEvent {

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
     * Причина удаления
     */
    private String deletionReason;

    /**
     * Тип удаления (soft, hard)
     */
    private String deletionType;

    /**
     * IP адрес удаления
     */
    private String deletionIp;

    /**
     * User Agent при удалении
     */
    private String userAgent;

    /**
     * ID администратора, удалившего пользователя
     */
    private String deletedBy;

    /**
     * Создание события удаления пользователя
     */
    public static UserDeletedEvent create(String userId, String email, String username,
                                        String firstName, String lastName, String deletionReason,
                                        String deletionType, String deletionIp, String userAgent,
                                        String deletedBy, String correlationId) {
        return UserDeletedEvent.builder()
                .eventType("UserDeleted")
                .eventVersion("1.0")
                .source("user-management-service")
                .correlationId(correlationId)
                .userId(userId)
                .email(email)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .deletionReason(deletionReason)
                .deletionType(deletionType)
                .deletionIp(deletionIp)
                .userAgent(userAgent)
                .deletedBy(deletedBy)
                .data(Map.of(
                        "userId", userId,
                        "email", email,
                        "username", username,
                        "firstName", firstName,
                        "lastName", lastName,
                        "deletionReason", deletionReason,
                        "deletionType", deletionType,
                        "deletionIp", deletionIp,
                        "userAgent", userAgent,
                        "deletedBy", deletedBy
                ))
                .build();
    }
}