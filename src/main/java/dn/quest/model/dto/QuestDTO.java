package dn.quest.model.dto;

import dn.quest.model.entities.enums.Difficulty;
import dn.quest.model.entities.enums.QuestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestDTO {

    private Long id;
    private Long number;
    private Difficulty difficulty;
    private QuestType type;
    private String title;
    private String descriptionHtml;
    private Set<UserDTO> authors;
    private Instant startAt;
    private Instant endAt;
    private boolean published;

}