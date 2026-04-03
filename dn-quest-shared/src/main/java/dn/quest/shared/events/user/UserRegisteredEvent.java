package dn.quest.shared.events.user;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Событие регистрации пользователя
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class UserRegisteredEvent extends BaseEvent {

    /**
     * ID зарегистрированного пользователя
     */
    private UUID userId;

    /**
     * Имя пользователя
     */
    private String username;

    /**
     * Email пользователя
     */
    private String email;

    /**
     * Публичное имя
     */
    private String publicName;

    /**
     * Роль пользователя
     */
    private String role;
}
