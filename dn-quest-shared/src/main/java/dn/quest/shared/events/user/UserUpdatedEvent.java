package dn.quest.shared.events.user;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

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

    /**
     * Активен ли пользователь
     */
    private Boolean isActive;

    /**
     * Подтверждён ли email
     */
    private Boolean isEmailVerified;
}