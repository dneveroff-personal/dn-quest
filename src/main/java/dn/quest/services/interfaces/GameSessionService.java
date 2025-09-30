package dn.quest.services.interfaces;

import dn.quest.model.dto.LevelViewDTO;
import dn.quest.model.entities.enums.AttemptResult;
import dn.quest.model.entities.enums.SessionStatus;
import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.level.CodeAttempt;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.model.entities.quest.level.LevelCompletion;

import java.util.List;

public interface GameSessionService {
    GameSession start(Long questId, Integer userId, Long teamId);

    AttemptResult submitCode(Long sessionId, String rawCode, Integer userId);

    Level getCurrentLevel(Long sessionId);

    LevelViewDTO getCurrentLevelView(Long sessionId);

    List<CodeAttempt> lastAttempts(Long sessionId, Long levelId, int limit);

    List<LevelCompletion> leaderboard(Long questId);

    GameSession setStatus(Long sessionId, SessionStatus status);

    List<GameSession> getSessionsByQuest(Long questId);
}