package dn.quest.services.impl;

import dn.quest.model.dto.*;
import dn.quest.model.entities.enums.*;
import dn.quest.model.entities.quest.*;
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

    // --- Основная логика ---

    @Override
    public GameSession start(Long questId, Integer userId, Long teamId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new EntityNotFoundException("Quest not found: " + questId));

        // Проверка на активную сессию
        if (quest.getType() == QuestType.SOLO && userId != null) {
            User user = userRepository.findById(userId.longValue())
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
            return gameSessionRepository.findByQuestAndUser(quest, user)
                    .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
                    .orElseGet(() -> createNewSession(quest, user, null));
        } else if (quest.getType() == QuestType.TEAM && teamId != null) {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new EntityNotFoundException("Team not found: " + teamId));
            return gameSessionRepository.findByQuestAndTeam(quest, team)
                    .filter(s -> s.getStatus() == SessionStatus.ACTIVE)
                    .orElseGet(() -> createNewSession(quest, null, team));
        } else {
            throw new IllegalArgumentException("Invalid quest type or missing identifiers");
        }
    }

    private GameSession createNewSession(Quest quest, User user, Team team) {
        GameSession session = new GameSession();
        session.setQuest(quest);
        session.setUser(user);
        session.setTeam(team);
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(Instant.now());
        session = gameSessionRepository.save(session);

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
    public AttemptResult submitCode(Long sessionId, String rawCode, Integer userId) {
        GameSession session = findSession(sessionId);
        if (session.getStatus() != SessionStatus.ACTIVE)
            throw new IllegalStateException("Session is not ACTIVE");

        LevelProgress progress = levelProgressRepository.findCurrentBySession(session)
                .orElseThrow(() -> new EntityNotFoundException("No active level for session " + sessionId));
        Level level = progress.getLevel();

        User actor = (userId != null)
                ? userRepository.findById(userId.longValue())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId))
                : null;

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
            boolean usedBefore = codeAttemptRepository.existsBySessionAndSubmittedNormalized(sessionId, normalized);
            result = usedBefore ? AttemptResult.DUPLICATE : AttemptResult.WRONG;
        } else {
            attempt.setMatchedCode(matched);
            attempt.setMatchedSectorNo(matched.getSectorNo());

            switch (matched.getType()) {
                case NORMAL -> {
                    result = AttemptResult.ACCEPTED_NORMAL;
                    long closedBefore = codeAttemptRepository.countDistinctClosedSectors(session, level);
                    progress.setSectorsClosed((int) (matched.getSectorNo() == null ? closedBefore : closedBefore + 1));
                }
                case BONUS -> {
                    result = AttemptResult.ACCEPTED_BONUS;
                    int shift = Math.max(0, matched.getShiftSeconds());
                    session.setBonusTimeSumSec(session.getBonusTimeSumSec() + shift);
                    progress.setBonusOnLevelSec(progress.getBonusOnLevelSec() + shift);
                }
                case PENALTY -> {
                    result = AttemptResult.ACCEPTED_PENALTY;
                    int penalty = Math.abs(Math.min(0, matched.getShiftSeconds()));
                    session.setPenaltyTimeSumSec(session.getPenaltyTimeSumSec() + penalty);
                    progress.setPenaltyOnLevelSec(progress.getPenaltyOnLevelSec() + penalty);
                }
                default -> result = AttemptResult.WRONG;
            }
        }

        attempt.setResult(result);
        codeAttemptRepository.save(attempt);
        levelProgressRepository.save(progress);
        gameSessionRepository.save(session);

        if (level.getRequiredSectors() != null
                && progress.getSectorsClosed() >= level.getRequiredSectors()) {
            closeLevelAndAdvance(session, progress, actor);
        }

        return result;
    }

    // --- Возвращаем DTO и поддерживаем offset (для подгрузки истории) ---
    @Override
    @Transactional(readOnly = true)
    public List<CodeAttemptDTO> lastAttempts(Long sessionId, Long levelId, int limit, int offset) {
        int safeLimit = Math.max(1, limit);
        int page = Math.max(0, offset / safeLimit);
        List<CodeAttempt> attempts = codeAttemptRepository.findLastAttempts(sessionId, levelId, PageRequest.of(page, safeLimit));
        return attempts.stream()
                .map(a -> CodeAttemptDTO.builder()
                        .id(a.getId())
                        .sessionId(a.getSession() != null ? a.getSession().getId() : null)
                        .levelId(a.getLevel() != null ? a.getLevel().getId() : null)
                        .userId(a.getUser() != null ? a.getUser().getId() : null)
                        .submittedRaw(a.getSubmittedRaw())
                        .submittedNormalized(a.getSubmittedNormalized())
                        .result(a.getResult())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
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

        LevelDTO levelDto = LevelDTO.builder()
                .id(level.getId())
                .questId(level.getQuest().getId())
                .orderIndex(level.getOrderIndex())
                .title(level.getTitle())
                .descriptionHtml(level.getDescriptionHtml())
                .apTime(level.getApTime())
                .requiredSectors(level.getRequiredSectors())
                .build();

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
                .stream()
                .map(h -> LevelHintDTO.builder()
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

    // --- Доп. методы интерфейса, которые были не реализованы в файле ранее ---

    @Override
    @Transactional(readOnly = true)
    public Level getCurrentLevel(Long sessionId) {
        GameSession session = findSession(sessionId);
        Level level = session.getCurrentLevel();
        if (level == null) throw new EntityNotFoundException("No active level for session " + sessionId);
        return level;
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
        if (status == SessionStatus.ACTIVE && session.getStartedAt() == null) session.setStartedAt(now);
        if ((status == SessionStatus.ABORTED || status == SessionStatus.FINISHED) && session.getFinishedAt() == null)
            session.setFinishedAt(now);
        return gameSessionRepository.save(session);
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

        progress.setClosedAt(now);
        levelProgressRepository.save(progress);

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

        Level next = levelRepository.findNext(session.getQuest(), level.getOrderIndex());
        if (next == null) {
            session.setStatus(SessionStatus.FINISHED);
            session.setFinishedAt(now);
            session.setCurrentLevel(null);
        } else {
            session.setCurrentLevel(next);
            LevelProgress nextProgress = new LevelProgress();
            nextProgress.setSession(session);
            nextProgress.setLevel(next);
            nextProgress.setStartedAt(now);
            levelProgressRepository.save(nextProgress);
        }
        gameSessionRepository.save(session);
    }
}
