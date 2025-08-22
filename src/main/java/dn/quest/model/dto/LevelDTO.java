package dn.quest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelDTO {

    private Long id;
    private Integer orderIndex;
    private String title;
    private String descriptionHtml;
    private Integer apTime;         // auto-pass time (сек)
    private Integer requiredSectors;

}