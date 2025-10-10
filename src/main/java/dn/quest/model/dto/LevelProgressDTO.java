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
public class LevelProgressDTO {
    private Long id;
    private Long sessionId;
    private Long levelId;
    private Instant startedAt;
    private Instant closedAt;
    private int sectorsClosed;
    private int bonusOnLevelSec;
    private int penaltyOnLevelSec;
}
