package dn.quest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для отображения текущего уровня с кодами, подсказками и прогрессом.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelViewDTO {
    private Long sessionId;
    private LevelDTO level;
    private LevelProgressDTO progress;
    private List<LevelHintDTO> hints;
    private boolean finished;

    private List<CodeViewDTO> sectors;      // NORMAL codes
    private List<CodeViewDTO> bonusCodes;   // BONUS codes
    private List<CodeViewDTO> penaltyCodes; // PENALTY codes
}
