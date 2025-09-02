package dn.quest.model.dto;

import dn.quest.model.entities.enums.AttemptResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeAttemptDTO {

    private Long id;

    private Long sessionId;           // GameSession
    private Long levelId;             // Level
    private Long userId;              // кто отправил

    private String submittedRaw;      // то, что ввёл пользователь
    private String submittedNormalized; // нормализованная версия

    private Long matchedCodeId;       // если совпало с каким-то кодом
    private Integer matchedSectorNo;  // сектор, к которому относится код

    private AttemptResult result;     // ACCEPTED/DUPLICATE/WRONG и т.п.
    private Instant createdAt;        // когда отправлено
}
