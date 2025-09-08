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
public class QuestCreateUpdateDTO {
    private String title;
    private String descriptionHtml;
    private Difficulty difficulty;
    private QuestType type;
    private Instant startAt;
    private Instant endAt;
    private Boolean published; // nullable, только для редактирования
    private Set<UserDTO> authors; // nullable, только для редактирования
}
