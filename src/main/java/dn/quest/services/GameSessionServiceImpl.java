package dn.quest.services;

import dn.quest.model.entities.enums.AttemptResult;
import dn.quest.model.entities.enums.CodeType;
import dn.quest.model.entities.enums.QuestType;
import dn.quest.model.entities.enums.SessionStatus;
import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.quest.level.*;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.*;
import dn.quest.services.interfaces.GameSessionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GameSessionServiceImpl implements GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    private final LevelRepository levelRepository;
    private final CodeRepository codeRepository;
    private final CodeAttemptRepository codeAttemptRepository;
    private final LevelProgressRepository levelProgressRepository;
    private final LevelCompletionRepository levelCompletionRepository;

    @Override
    public GameSession start(Long questId, Integer userId, Long teamId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new EntityNotFoundException("Quest not found: " + questId));

        GameSession session = new GameSession();
        session.setQuest(quest);

        if (quest.getType() == QuestType.SOLO) {
            if (userId == null) throw new IllegalArgumentException("userId required for SOLO quest");
            User user = userRepository.findById(userId.longValue())
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
            session.setUser(user);
        } else { // TEAM
            if (teamId == null) throw new IllegalArgumentException("teamId required for TEAM quest");
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new EntityNotFoundException("Team not found: " + teamId));
            session.setTeam(team);
        }

        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(Instant.now());
        session = gameSessionRepository.save(session);

        // создать прогресс по первому уровню
        Level first = levelRepository.findFirstInQuest(quest);
        if (first != null) {
            LevelProgress lp = new LevelProgress();
            lp.setSession(session);
            lp.setLevel(first);
            lp.setStartedAt(Instant.now());
            levelProgressRepository.save(lp);
        }
        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public Level getCurrentLevel(Long sessionId) {
        GameSession session = findSession(sessionId);
        return levelProgressRepository.findCurrentBySession(session)
                .map(LevelProgress::getLevel)
                .orElseThrow(() -> new EntityNotFoundException("No active level for session " + sessionId));
    }

    @Override
    public AttemptResult submitCode(Long sessionId, String rawCode, Integer userId) {
        GameSession session = findSession(sessionId);
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session status is not ACTIVE");
        }

        LevelProgress progress = levelProgressRepository.findCurrentBySession(session)
                .orElseThrow(() -> new EntityNotFoundException("No active level for session " + sessionId));
        Level level = progress.getLevel();

        User actor = null;
        if (userId != null) {
            actor = userRepository.findById(userId.longValue())
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        }

        String normalized = normalize(rawCode);
        AttemptResult result;
        Code matched = codeRepository.findByLevelAndNormalized(level, normalized).orElse(null);

        CodeAttempt attempt = new CodeAttempt();
        attempt.setSession(session);
        attempt.setLevel(level);
        attempt.setUser(actor);
        attempt.setSubmittedRaw(rawCode == null ? "" : rawCode);
        attempt.setSubmittedNormalized(normalized);
        attempt.setCreatedAt(Instant.now());

        if (matched == null) {
            result = AttemptResult.WRONG;
            attempt.setResult(result);
            codeAttemptRepository.save(attempt);
            return result;
        }

        // duplicate?
        boolean usedBefore = !codeAttemptRepository.findBySessionAndMatchedCode(session, matched).isEmpty();
        if (usedBefore) {
            result = AttemptResult.DUPLICATE;
            attempt.setResult(result);
            attempt.setMatchedCode(matched);
            attempt.setMatchedSectorNo(matched.getSectorNo());
            codeAttemptRepository.save(attempt);
            return result;
        }

        // accepted flow
        attempt.setMatchedCode(matched);
        attempt.setMatchedSectorNo(matched.getSectorNo());

        if (matched.getType() == CodeType.NORMAL) {
            result = AttemptResult.ACCEPTED_NORMAL;

            // увеличим счётчик уникальных закрытых секторов (факт считаем через репозиторий)
            long closedBefore = codeAttemptRepository.countDistinctClosedSectors(session, level);
            long closedAfter = (matched.getSectorNo() == null) ? closedBefore : closedBefore + 1;
            progress.setSectorsClosed((int) closedAfter);

        } else if (matched.getType() == CodeType.BONUS) {
            result = AttemptResult.ACCEPTED_BONUS;
            int shift = Math.max(0, matched.getShiftSeconds());
            session.setBonusTimeSumSec(session.getBonusTimeSumSec() + shift);
            progress.setBonusOnLevelSec(progress.getBonusOnLevelSec() + shift);

        } else { // PENALTY
            result = AttemptResult.ACCEPTED_PENALTY;
            int penalty = Math.abs(Math.min(0, matched.getShiftSeconds()));
            session.setPenaltyTimeSumSec(session.getPenaltyTimeSumSec() + penalty);
            progress.setPenaltyOnLevelSec(progress.getPenaltyOnLevelSec() + penalty);
        }

        attempt.setResult(result);
        codeAttemptRepository.save(attempt);
        levelProgressRepository.save(progress);
        gameSessionRepository.save(session);

        // проверка завершения уровня (только по NORMAL-секторам)
        if (level.getRequiredSectors() != null
                && level.getRequiredSectors() > 0
                && progress.getSectorsClosed() >= level.getRequiredSectors()) {
            closeLevelAndAdvance(session, progress, actor);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeAttempt> lastAttempts(Long sessionId, Long levelId, int limit) {
        GameSession session = findSession(sessionId);
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new EntityNotFoundException("Level not found: " + levelId));
        return codeAttemptRepository.findLastAttempts(session, level, PageRequest.of(0, Math.max(1, limit)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelCompletion> leaderboard(Long questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new EntityNotFoundException("Quest not found: " + questId));
        return levelCompletionRepository.findByQuest(quest);
    }

    @Override
    public GameSession setStatus(Long sessionId, SessionStatus status) {
        GameSession session = findSession(sessionId);
        session.setStatus(status);
        Instant now = Instant.now();
        if (status == SessionStatus.ACTIVE && session.getStartedAt() == null) {
            session.setStartedAt(now);
        }
        if ((status == SessionStatus.ABORTED || status == SessionStatus.FINISHED) && session.getFinishedAt() == null) {
            session.setFinishedAt(now);
        }
        return gameSessionRepository.save(session);
    }

    // --------- helpers ---------

    private GameSession findSession(Long id) {
        return gameSessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + id));
    }

    private String normalize(String raw) {
        if (raw == null) return "";
        return raw.trim().toLowerCase();
    }

    private void closeLevelAndAdvance(GameSession session, LevelProgress progress, User passedBy) {
        Instant now = Instant.now();
        progress.setClosedAt(now);
        levelProgressRepository.save(progress);

        Level level = progress.getLevel();

        // фиксация завершения уровня
        LevelCompletion lc = new LevelCompletion();
        lc.setSession(session);
        lc.setLevel(level);
        lc.setPassedByUser(passedBy);
        lc.setPassTime(now);
        long raw = Duration.between(progress.getStartedAt(), now).getSeconds();
        lc.setDurationSec(raw);
        lc.setBonusOnLevelSec(progress.getBonusOnLevelSec());
        lc.setPenaltyOnLevelSec(progress.getPenaltyOnLevelSec());
        levelCompletionRepository.save(lc);

        // следующий уровень
        Level next = levelRepository.findNext(session.getQuest(), level.getOrderIndex());
        if (next == null) {
            session.setStatus(SessionStatus.FINISHED);
            session.setFinishedAt(now);
            gameSessionRepository.save(session);
        } else {
            LevelProgress nextProgress = new LevelProgress();
            nextProgress.setSession(session);
            nextProgress.setLevel(next);
            nextProgress.setStartedAt(now);
            levelProgressRepository.save(nextProgress);
        }
    }
}
