package dn.quest.shared.events.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Событие окончания игровой сессии
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionFinishedEvent {

    private String eventId;
    private Long sessionId;
    private Long userId;
    private Long teamId;
    private String finishTime;
    private Integer totalScore;
    private Integer levelsCompleted;
    private String finishReason;
}
