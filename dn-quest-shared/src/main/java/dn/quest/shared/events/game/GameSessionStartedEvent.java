package dn.quest.shared.events.game;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Событие начала игровой сессии
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionStartedEvent extends BaseEvent {

    private Long sessionId;
    private Long userId;
    private Long teamId;
    private Long questId;
    private String difficulty;
}
