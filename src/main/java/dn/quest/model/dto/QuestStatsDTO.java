package dn.quest.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestStatsDTO {
    private Long questId;
    private String questTitle;
    private Long totalSessions;
    private int completedSessions;
    private double avgCompletionTimeMin;
    private List<LevelCompletionDTO> leaderboard;
}
