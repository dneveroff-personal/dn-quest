package dn.quest.shared.events.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Событие завершения уровня
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelCompletedEvent {

    private String eventId;
    private Long sessionId;
    private Long userId;
    private Long levelId;
    private Integer score;
    private Long completionTime;
    private Integer attemptsUsed;
    private Boolean isPerfect;
}
