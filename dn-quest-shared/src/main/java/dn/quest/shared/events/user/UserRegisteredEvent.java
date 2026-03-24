package dn.quest.shared.events.user;

import dn.quest.shared.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Событие регистрации пользователя
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserRegisteredEvent extends BaseEvent {

    /**
     * ID зарегистрированного пользователя
     */
    private Long userId;

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
