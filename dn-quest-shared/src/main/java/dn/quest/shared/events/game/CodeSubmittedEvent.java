package dn.quest.shared.events.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Событие отправки кода
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeSubmittedEvent {

    private String eventId;
    private Long attemptId;
    private Long sessionId;
    private Long userId;
    private Long levelId;
    private String submittedCode;
    private String codeSector;
    private Boolean isCorrect;
    private Long submissionTime;
}
