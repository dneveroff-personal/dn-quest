package dn.quest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelViewDTO {
    private Long sessionId;
    private LevelDTO level;
    private LevelProgressDTO progress;
    private List<LevelHintDTO> hints;    // список подсказок (с offsetSec и text)

}
