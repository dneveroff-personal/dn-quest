package dn.quest.shared.events.game;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Событие начала игровой сессии
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionStartedEvent extends BaseEvent {

    private UUID sessionId;
    private UUID userId;
    private UUID teamId;
    private UUID questId;
    private String difficulty;
}
