package dn.quest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSessionDTO {
    private Long id;
    private Long questId;
    private Long teamId;
    private Instant startedAt;
    private Instant finishedAt;
}
