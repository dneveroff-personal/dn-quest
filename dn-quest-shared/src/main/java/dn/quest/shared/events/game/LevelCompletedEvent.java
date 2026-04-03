package dn.quest.shared.events.game;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Событие завершения уровня
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LevelCompletedEvent extends BaseEvent {

    private Long questId;
    private Long sessionId;
    private UUID userId;
    private Long levelId;
    private Integer levelNumber;
    private Integer score;
    private Long completionTime;
    private Integer attemptsUsed;
    private Boolean isPerfect;
}
