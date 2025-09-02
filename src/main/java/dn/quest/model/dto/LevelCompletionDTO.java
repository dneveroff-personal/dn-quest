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
public class LevelCompletionDTO {

    private Long levelId;
    private Long teamId;                // или null если соло
    private String teamName;
    private Long passedByUserId;
    private String passedByPublicName;
    private Instant passTime;
    private String durationHHMMSS;      // уже отформатированная строка
    private int bonusOnLevelSec;
    private int penaltyOnLevelSec;

}