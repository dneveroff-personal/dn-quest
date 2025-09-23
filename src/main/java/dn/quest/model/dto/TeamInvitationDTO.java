package dn.quest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamInvitationDTO {

    private Long id;
    private TeamDTO team;
    private String status;
    private Instant createdAt;

}
