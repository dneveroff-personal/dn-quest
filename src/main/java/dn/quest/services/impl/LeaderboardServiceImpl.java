package dn.quest.services.impl;

import dn.quest.model.dto.LevelCompletionDTO;
import dn.quest.model.dto.QuestStatsDTO;
import dn.quest.model.entities.quest.level.LevelCompletion;
import dn.quest.repositories.LevelCompletionRepository;
import dn.quest.services.interfaces.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LevelCompletionRepository levelCompletionRepository;

    @Override
    public List<LevelCompletionDTO> getLeaderboardByQuest(Long questId) {
        List<LevelCompletionDTO> leaderboard = levelCompletionRepository.findLeaderboardByQuestId(questId);

        // Форматируем durationHHMMSS
        leaderboard.forEach(lc -> {
            int totalSec = (lc.getBonusOnLevelSec() + lc.getPenaltyOnLevelSec()); // можно заменить на durationSec, если есть
            String durationStr = String.format("%02d:%02d:%02d",
                    totalSec / 3600,
                    (totalSec % 3600) / 60,
                    totalSec % 60);
            lc.setDurationHHMMSS(durationStr);
        });

        return leaderboard;
    }

    @Override
    public QuestStatsDTO getQuestStats(Long questId) {
        // Сразу получаем leaderboard
        List<LevelCompletionDTO> leaderboard = levelCompletionRepository.findLeaderboardByQuestId(questId);

        // Считаем количество уникальных сессий
        long completedSessions = levelCompletionRepository.countDistinctSessionsByQuestId(questId);

        // Средняя длительность в секундах
        double avgDurationSec = levelCompletionRepository.averageDurationSecByQuestId(questId);

        // Преобразуем в минуты
        double avgDurationMin = avgDurationSec / 60.0;

        // Получаем название квеста (берём из первого элемента leaderboard, если есть)
        String questTitle = leaderboard.isEmpty() ? "-" : leaderboard.get(0).getLevelTitle();

        return QuestStatsDTO.builder()
                .questId(questId)
                .questTitle(questTitle)
                .totalSessions(completedSessions)
                .completedSessions((int) completedSessions)
                .avgCompletionTimeMin(avgDurationMin)
                .leaderboard(leaderboard)
                .build();
    }

}
