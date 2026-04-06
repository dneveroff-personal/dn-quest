package dn.quest.shared.events.game;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Событие окончания игровой сессии
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionFinishedEvent extends BaseEvent {

    private UUID questId;
    private UUID sessionId;
    private UUID userId;
    private UUID teamId;
    private Integer totalScore;
    private Integer levelsCompleted;
    private Boolean isCompleted;
}
