package dn.quest.shared.events.game;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Событие окончания игровой сессии
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionFinishedEvent extends BaseEvent {

    private Long sessionId;
    private Long userId;
    private Long teamId;
    private Integer totalScore;
    private Integer levelsCompleted;
    private Boolean isCompleted;
}
