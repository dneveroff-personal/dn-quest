package dn.quest.shared.events.quest;

import dn.quest.shared.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Событие удаления квеста
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestDeletedEvent extends BaseEvent {

    /**
     * ID удалённого квеста
     */
    private UUID questId;
}
