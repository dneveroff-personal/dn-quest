package dn.quest.services.interfaces;

import dn.quest.model.dto.LevelCompletionDTO;
import dn.quest.model.dto.QuestStatsDTO;

import java.util.List;

public interface LeaderboardService {

    List<LevelCompletionDTO> getLeaderboardByQuest(Long questId);

    QuestStatsDTO getQuestStats(Long questId);
}
