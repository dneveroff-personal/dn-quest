package dn.quest.shared.events.game;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Событие отправки кода
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CodeSubmittedEvent extends BaseEvent {

    private String eventId;
    private Long attemptId;
    private Long sessionId;
    private UUID userId;
    private Long levelId;
    private String submittedCode;
    private String codeSector;
    private Boolean isCorrect;
    private Long submissionTime;
}
