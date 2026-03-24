package dn.quest.shared.events.user;

import dn.quest.shared.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Событие удаления пользователя
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDeletedEvent extends BaseEvent {

    /**
     * ID удалённого пользователя
     */
    private Long userId;
}
