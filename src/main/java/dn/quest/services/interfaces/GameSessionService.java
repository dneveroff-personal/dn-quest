package dn.quest.services.interfaces;

import dn.quest.model.entities.enums.AttemptResult;
import dn.quest.model.entities.enums.SessionStatus;
import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.level.CodeAttempt;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.model.entities.quest.level.LevelCompletion;

import java.util.List;

public interface GameSessionService {

    GameSession start(Long questId, Integer userId, Long teamId); // один из идентификаторов должен быть null в зависимости от quest.type

    Level getCurrentLevel(Long sessionId);

    // Ввод кода: нормализуем (toLower) и обрабатываем по твоим правилам (↻, бонус/штраф, REQUIRED_SECTORS)
    AttemptResult submitCode(Long sessionId, String rawCode, Integer userId);

    List<CodeAttempt> lastAttempts(Long sessionId, Long levelId, int limit); // для лога 10 последних

    List<LevelCompletion> leaderboard(Long questId);

    GameSession setStatus(Long sessionId, SessionStatus status);

}
