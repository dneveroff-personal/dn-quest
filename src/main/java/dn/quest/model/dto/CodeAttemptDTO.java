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
    private Long sessionId;
    private Long levelId;
    private Long userId;
    private String submittedRaw;
    private String submittedNormalized;
    private AttemptResult result;
    private Instant createdAt;

}
