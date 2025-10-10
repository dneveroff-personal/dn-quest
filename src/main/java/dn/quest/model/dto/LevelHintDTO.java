package dn.quest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelHintDTO {
    private Long id;
    private Long levelId;
    private Integer offsetSec;
    private String text;
    private Integer orderIndex;
}
