package dn.quest.shared.events.user;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Событие обновления пользователя
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class UserUpdatedEvent extends BaseEvent {

    /**
     * ID обновлённого пользователя
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

    /**
     * Активен ли пользователь
     */
    private Boolean isActive;

    /**
     * Подтверждён ли email
     */
    private Boolean isEmailVerified;
}