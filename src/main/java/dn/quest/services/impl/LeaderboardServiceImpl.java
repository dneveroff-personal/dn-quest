package dn.quest.services.impl;

import dn.quest.config.ApplicationConstants;
import dn.quest.config.DateTimeUtils;
import dn.quest.model.dto.LevelCompletionDTO;
import dn.quest.model.dto.QuestStatsDTO;
import dn.quest.repositories.LevelCompletionRepository;
import dn.quest.services.interfaces.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LevelCompletionRepository levelCompletionRepository;
    private final DateTimeUtils dateTimeUtils;

    @Override
    public List<LevelCompletionDTO> getLeaderboardByQuest(Long questId) {
        if (questId == null) {
            log.warn("Попытка получить таблицу лидеров для null questId");
            return List.of();
        }

        List<LevelCompletionDTO> leaderboard = levelCompletionRepository.findLeaderboardByQuestId(questId);
        log.debug("Получено {} записей в таблице лидеров для квеста {}", leaderboard.size(), questId);

        // Форматируем durationHHMMSS используя утилитарный класс
        leaderboard.forEach(lc -> {
            int totalSec = lc.getBonusOnLevelSec() + lc.getPenaltyOnLevelSec();
            String durationStr = dateTimeUtils.formatDuration(totalSec);
            lc.setDurationHHMMSS(durationStr);
        });

        return leaderboard;
    }

    @Override
    public QuestStatsDTO getQuestStats(Long questId) {
        if (questId == null) {
            log.warn("Попытка получить статистику для null questId");
            return QuestStatsDTO.builder()
                    .questId(null)
                    .questTitle(ApplicationConstants.QUEST_NOT_FOUND)
                    .totalSessions(0L)
                    .completedSessions(0)
                    .avgCompletionTimeMin(0.0)
                    .leaderboard(List.of())
                    .build();
        }

        log.debug("Получение статистики для квеста {}", questId);

        // Сразу получаем leaderboard
        List<LevelCompletionDTO> leaderboard = levelCompletionRepository.findLeaderboardByQuestId(questId);

        // Считаем количество уникальных сессий
        long completedSessions = levelCompletionRepository.countDistinctSessionsByQuestId(questId);

        // Средняя длительность в секундах
        Double avgDurationSec = levelCompletionRepository.averageDurationSecByQuestId(questId);
        double avgDurationMin = avgDurationSec != null ? avgDurationSec / 60.0 : 0.0;

        // Получаем название квеста (берём из первого элемента leaderboard, если есть)
        String questTitle = leaderboard.isEmpty() ? "Квест #" + questId : leaderboard.get(0).getLevelTitle();

        QuestStatsDTO stats = QuestStatsDTO.builder()
                .questId(questId)
                .questTitle(questTitle)
                .totalSessions(completedSessions)
                .completedSessions((int) completedSessions)
                .avgCompletionTimeMin(avgDurationMin)
                .leaderboard(leaderboard)
                .build();

        log.debug("Статистика для квеста {}: {} завершенных сессий, среднее время: {:.2f} мин",
                questId, completedSessions, avgDurationMin);

        return stats;
    }

}
