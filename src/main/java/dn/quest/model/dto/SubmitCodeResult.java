package dn.quest.model.dto;

import dn.quest.model.entities.enums.AttemptResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitCodeResult {
    private AttemptResult result;       // SUCCESS / BONUS / PENALTY / WRONG
    private LevelViewDTO newLevel;   // если перешли на новый
    private String bonusMessage;     // если это бонус или штраф
}
