package dn.quest.model.dto;

import dn.quest.model.entities.enums.ApplicantType;
import dn.quest.model.entities.enums.ParticipationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDTO {

    private Long id;

    @NotNull
    private Long questId;

    private Long userId;

    private Long teamId;

    @NotNull
    private ApplicantType type;

    @NotNull
    private ParticipationStatus status;

}
