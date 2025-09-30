package dn.quest.services.impl;

import dn.quest.model.dto.*;
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
import java.util.Optional;

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
    private final LevelHintRepository levelHintRepository;

    @Override
    public GameSession start(Long questId, Integer userId, Long teamId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new EntityNotFoundException("Quest not found: " + questId));

        // Проверка на существующую активную сессию
        if (quest.getType() == QuestType.SOLO && userId != null) {
            User user = userRepository.findById(userId.longValue())
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
            Optional<GameSession> existing = gameSessionRepository.findByQuestAndUser(quest, user)
                    .filter(s -> s.getStatus() == SessionStatus.ACTIVE);
            if (existing.isPresent()) return existing.get();
        } else if (quest.getType() == QuestType.TEAM && teamId != null) {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new EntityNotFoundException("Team not found: " + teamId));
            Optional<GameSession> existing = gameSessionRepository.findByQuestAndTeam(quest, team)
                    .filter(s -> s.getStatus() == SessionStatus.ACTIVE);
            if (existing.isPresent()) return existing.get();
        }

        // Создание новой сессии
        GameSession session = new GameSession();
        session.setQuest(quest);

        if (quest.getType() == QuestType.SOLO) {
            if (userId == null) throw new IllegalArgumentException("userId required for SOLO quest");
            session.setUser(userRepository.findById(userId.longValue())
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId)));
        } else {
            if (teamId == null) throw new IllegalArgumentException("teamId required for TEAM quest");
            session.setTeam(teamRepository.findById(teamId)
                    .orElseThrow(() -> new EntityNotFoundException("Team not found: " + teamId)));
        }

        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(Instant.now());

        // Сохраняем сессию перед зависимыми сущностями
        session = gameSessionRepository.save(session);

        // Создание первого уровня
        Level first = levelRepository.findFirstInQuest(quest);
        if (first != null) {
            LevelProgress lp = new LevelProgress();
            lp.setSession(session);
            lp.setLevel(first);
            lp.setStartedAt(Instant.now());
            levelProgressRepository.save(lp);

            session.setCurrentLevel(first);
            session = gameSessionRepository.save(session);
        }

        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public Level getCurrentLevel(Long sessionId) {
        GameSession session = findSession(sessionId);
        Level level = session.getCurrentLevel();
        if (level == null) {
            throw new EntityNotFoundException("No active level for session " + sessionId);
        }
        return level;
    }

    @Override
    public AttemptResult submitCode(Long sessionId, String rawCode, Integer userId) {
        GameSession session = findSession(sessionId);
        if (session.getStatus() != SessionStatus.ACTIVE)
            throw new IllegalStateException("Session status is not ACTIVE");

        LevelProgress progress = levelProgressRepository.findCurrentBySession(session)
                .orElseThrow(() -> new EntityNotFoundException("No active level for session " + sessionId));
        Level level = progress.getLevel();

        User actor = null;
        if (userId != null) actor = userRepository.findById(userId.longValue())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        String normalized = normalize(rawCode);
        Code matched = codeRepository.findByLevelAndNormalized(level, normalized).orElse(null);

        CodeAttempt attempt = new CodeAttempt();
        attempt.setSession(session);
        attempt.setLevel(level);
        attempt.setUser(actor);
        attempt.setSubmittedRaw(rawCode == null ? "" : rawCode);
        attempt.setSubmittedNormalized(normalized);
        attempt.setCreatedAt(Instant.now());

        AttemptResult result;
        if (matched == null) {
            result = AttemptResult.WRONG;
            attempt.setResult(result);
            codeAttemptRepository.save(attempt);
            return result;
        }

        boolean usedBefore = !codeAttemptRepository.findBySessionAndMatchedCode(session, matched).isEmpty();
        if (usedBefore) {
            result = AttemptResult.DUPLICATE;
            attempt.setResult(result);
            attempt.setMatchedCode(matched);
            attempt.setMatchedSectorNo(matched.getSectorNo());
            codeAttemptRepository.save(attempt);
            return result;
        }

        attempt.setMatchedCode(matched);
        attempt.setMatchedSectorNo(matched.getSectorNo());

        if (matched.getType() == CodeType.NORMAL) {
            result = AttemptResult.ACCEPTED_NORMAL;
            long closedBefore = codeAttemptRepository.countDistinctClosedSectors(session, level);
            long closedAfter = (matched.getSectorNo() == null) ? closedBefore : closedBefore + 1;
            progress.setSectorsClosed((int) closedAfter);
        } else if (matched.getType() == CodeType.BONUS) {
            result = AttemptResult.ACCEPTED_BONUS;
            int shift = Math.max(0, matched.getShiftSeconds());
            session.setBonusTimeSumSec(session.getBonusTimeSumSec() + shift);
            progress.setBonusOnLevelSec(progress.getBonusOnLevelSec() + shift);
        } else {
            result = AttemptResult.ACCEPTED_PENALTY;
            int penalty = Math.abs(Math.min(0, matched.getShiftSeconds()));
            session.setPenaltyTimeSumSec(session.getPenaltyTimeSumSec() + penalty);
            progress.setPenaltyOnLevelSec(progress.getPenaltyOnLevelSec() + penalty);
        }

        attempt.setResult(result);
        codeAttemptRepository.save(attempt);
        levelProgressRepository.save(progress);
        gameSessionRepository.save(session);

        // Переход к следующему уровню, если закрыты все requiredSectors
        if (level.getRequiredSectors() != null && level.getRequiredSectors() > 0
                && progress.getSectorsClosed() >= level.getRequiredSectors()) {
            closeLevelAndAdvance(session, progress, actor);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeAttempt> lastAttempts(Long sessionId, Long levelId, int limit) {
        return codeAttemptRepository.findLastAttempts(sessionId, levelId, PageRequest.of(0, Math.max(1, limit)));
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
        Instant now = Instant.now();
        session.setStatus(status);
        if (status == SessionStatus.ACTIVE && session.getStartedAt() == null)
            session.setStartedAt(now);
        if ((status == SessionStatus.ABORTED || status == SessionStatus.FINISHED) && session.getFinishedAt() == null)
            session.setFinishedAt(now);
        return gameSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public LevelViewDTO getCurrentLevelView(Long sessionId) {
        GameSession session = findSession(sessionId);
        Level level = session.getCurrentLevel();
        if (level == null)
            throw new EntityNotFoundException("No active level for session " + sessionId);

        LevelProgress progress = levelProgressRepository.findCurrentBySessionAndLevel(session, level)
                .orElseThrow(() -> new EntityNotFoundException("No progress found for current level"));

        LevelDTO levelDto = new LevelDTO(
                level.getId(),
                level.getQuest().getId(),
                level.getOrderIndex(),
                level.getTitle(),
                level.getDescriptionHtml(),
                level.getRequiredSectors(),
                level.getApTime()
        );

        LevelProgressDTO progressDto = LevelProgressDTO.builder()
                .id(progress.getId())
                .sessionId(session.getId())
                .levelId(level.getId())
                .startedAt(progress.getStartedAt())
                .closedAt(progress.getClosedAt())
                .sectorsClosed(progress.getSectorsClosed())
                .bonusOnLevelSec(progress.getBonusOnLevelSec())
                .penaltyOnLevelSec(progress.getPenaltyOnLevelSec())
                .build();

        List<LevelHintDTO> hints = levelHintRepository.findByLevelOrderByOrderIndexAsc(level)
                .stream().map(h -> LevelHintDTO.builder()
                        .id(h.getId())
                        .levelId(level.getId())
                        .offsetSec(h.getOffsetSec())
                        .text(h.getText())
                        .orderIndex(h.getOrderIndex())
                        .build())
                .toList();

        return LevelViewDTO.builder()
                .level(levelDto)
                .progress(progressDto)
                .sessionId(session.getId())
                .hints(hints)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> getSessionsByQuest(Long questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new EntityNotFoundException("Quest not found: " + questId));
        return gameSessionRepository.findByQuest(quest);
    }

    // --- Вспомогательные методы ---
    private GameSession findSession(Long id) {
        return gameSessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + id));
    }

    private String normalize(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase();
    }

    private void closeLevelAndAdvance(GameSession session, LevelProgress progress, User passedBy) {
        Instant now = Instant.now();

        // Закрываем текущий прогресс
        progress.setClosedAt(now);
        levelProgressRepository.save(progress);

        // Создаём LevelCompletion
        Level level = progress.getLevel();
        LevelCompletion lc = new LevelCompletion();
        lc.setSession(session);
        lc.setLevel(level);
        lc.setPassedByUser(passedBy);
        lc.setPassTime(now);
        lc.setDurationSec(Duration.between(progress.getStartedAt(), now).getSeconds());
        lc.setBonusOnLevelSec(progress.getBonusOnLevelSec());
        lc.setPenaltyOnLevelSec(progress.getPenaltyOnLevelSec());
        levelCompletionRepository.save(lc);

        // Определяем следующий уровень
        Level next = levelRepository.findNext(session.getQuest(), level.getOrderIndex());
        if (next == null) {
            session.setStatus(SessionStatus.FINISHED);
            session.setFinishedAt(now);
            session.setCurrentLevel(null);
            gameSessionRepository.save(session);
        } else {
            session.setCurrentLevel(next);
            session = gameSessionRepository.save(session);

            LevelProgress nextProgress = new LevelProgress();
            nextProgress.setSession(session);
            nextProgress.setLevel(next);
            nextProgress.setStartedAt(now);
            levelProgressRepository.save(nextProgress);
        }
    }
}
