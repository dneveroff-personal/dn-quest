package dn.quest.shared.events.team;

import dn.quest.shared.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Событие удаления команды
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamDeletedEvent extends BaseEvent {

    /**
     * ID удалённой команды
     */
    private Long teamId;
}
