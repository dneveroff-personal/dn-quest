package dn.quest.shared.events.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Событие начала игровой сессии
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionStartedEvent {

    private String eventId;
    private Long sessionId;
    private Long userId;
    private Long teamId;
    private String startTime;
    private String questId;
    private Integer difficulty;
}
