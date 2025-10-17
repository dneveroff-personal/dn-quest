package dn.quest.services.impl;

import dn.quest.model.dto.*;
import dn.quest.model.entities.enums.*;
import dn.quest.model.entities.quest.*;
import dn.quest.model.entities.quest.level.*;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.*;
import dn.quest.services.interfaces.GameSessionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    @PersistenceContext
    private EntityManager entityManager;

    // --- Основная логика ---
    @Override
    public GameSession start(Long questId, Integer userId, Long teamId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new EntityNotFoundException("Quest not found: " + questId));

        if (quest.getType() == QuestType.SOLO && userId != null) {
            User user = userRepository.findById(userId.longValue())
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

            Optional<GameSession> active = gameSessionRepository.findByQuestAndUser(quest, user)
                    .filter(s -> s.getStatus() == SessionStatus.ACTIVE);
            if (active.isPresent()) return active.get();

            // 🚫 запрет повторного участия
            boolean finished = gameSessionRepository.findByQuestAndUser(quest, user).stream()
                    .anyMatch(s -> s.getStatus() == SessionStatus.FINISHED);
            if (finished)
                throw new IllegalStateException("Вы уже закончили эту игру");

            return createNewSession(quest, user, null);

        } else if (quest.getType() == QuestType.TEAM && teamId != null) {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new EntityNotFoundException("Team not found: " + teamId));

            Optional<GameSession> active = gameSessionRepository.findByQuestAndTeam(quest, team)
                    .filter(s -> s.getStatus() == SessionStatus.ACTIVE);
            if (active.isPresent()) return active.get();

            // 🚫 запрет повторного участия
            boolean finishedTeam = gameSessionRepository.findByQuestAndTeam(quest, team).stream()
                    .anyMatch(s -> s.getStatus() == SessionStatus.FINISHED);
            if (finishedTeam)
                throw new IllegalStateException("Ваша команда уже закончила эту игру");

            return createNewSession(quest, null, team);
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

        AttemptResult result = getAttemptResult(sessionId, matched, level, normalized, attempt, session, progress);

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

    private AttemptResult getAttemptResult(Long sessionId, Code matched, Level level, String normalized, CodeAttempt attempt, GameSession session, LevelProgress progress) {
        AttemptResult result;
        if (matched == null) {
            return AttemptResult.WRONG;
        } else {
            boolean usedBefore = codeAttemptRepository.existsBySessionAndSubmittedNormalized(sessionId, level, normalized);
            if (usedBefore) {
              return AttemptResult.DUPLICATE;
            }

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
                default -> {
                    return AttemptResult.WRONG;
                }
            }
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
        if (session.getStatus() == SessionStatus.FINISHED) {
            return LevelViewDTO.builder()
                    .sessionId(session.getId())
                    .finished(true)
                    .build();
        }

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

        // --- собираем коды уровня ---
        List<Code> codes = codeRepository.findByLevel(level);

        // вычисляем количество секторов (максимальный sectorNo среди NORMAL-кодов или requiredSectors как запас)
        int maxSectorNo = codes.stream()
                .filter(c -> c.getType() == CodeType.NORMAL && c.getSectorNo() != null)
                .mapToInt(Code::getSectorNo)
                .max()
                .orElse(0);

        if (maxSectorNo == 0 && level.getRequiredSectors() != null) {
            maxSectorNo = Math.max(maxSectorNo, level.getRequiredSectors());
        }

        // Получаем все принятые попытки (NORMAL/BONUS/PENALTY) для данной сессии+уровня, упорядоченные по createdAt desc
        List<CodeAttempt> acceptedAttempts = codeAttemptRepository
                .findAcceptedAttempts(session, level,
                        List.of(AttemptResult.ACCEPTED_NORMAL, AttemptResult.ACCEPTED_BONUS, AttemptResult.ACCEPTED_PENALTY));

        Set<Long> enteredCodeIds = acceptedAttempts.stream()
                .filter(a -> a.getMatchedCode() != null)
                .map(a -> a.getMatchedCode().getId())
                .collect(Collectors.toSet());

        // Map: sectorNo -> latest accepted CodeAttempt (новые записи идут первыми, поэтому first wins)
        java.util.Map<Integer, CodeAttempt> latestAcceptedPerSector = new java.util.HashMap<>();
        for (CodeAttempt a : acceptedAttempts) {
            if (a.getResult() == AttemptResult.ACCEPTED_NORMAL && a.getMatchedSectorNo() != null) {
                latestAcceptedPerSector.computeIfAbsent(a.getMatchedSectorNo(), k -> a);
            }
        }

        // --- собираем NORMAL ---
        List<CodeViewDTO> sectors = codes.stream()
                .filter(c -> c.getType() == CodeType.NORMAL)
                .map(c -> CodeViewDTO.builder()
                        .id(c.getId())
                        .levelId(level.getId())
                        .type(CodeType.NORMAL)
                        .sectorNo(c.getSectorNo())
                        .value(c.getValue())
                        .shiftSeconds(0)
                        .closed(enteredCodeIds.contains(c.getId()))
                        .matchedCodeValue(enteredCodeIds.contains(c.getId()) ? c.getValue() : null)
                        .build())
                .toList();

        // --- BONUS ---
        List<CodeViewDTO> bonusCodes = codes.stream()
                .filter(c -> c.getType() == CodeType.BONUS)
                .map(c -> CodeViewDTO.builder()
                        .id(c.getId())
                        .levelId(level.getId())
                        .type(CodeType.BONUS)
                        .value(c.getValue())
                        .shiftSeconds(c.getShiftSeconds())
                        .closed(enteredCodeIds.contains(c.getId()))
                        .matchedCodeValue(enteredCodeIds.contains(c.getId()) ? c.getValue() : null)
                        .build())
                .toList();

        // --- PENALTY ---
        List<CodeViewDTO> penaltyCodes = codes.stream()
                .filter(c -> c.getType() == CodeType.PENALTY)
                .map(c -> CodeViewDTO.builder()
                        .id(c.getId())
                        .levelId(level.getId())
                        .type(CodeType.PENALTY)
                        .value(c.getValue())
                        .shiftSeconds(c.getShiftSeconds())
                        .closed(enteredCodeIds.contains(c.getId()))
                        .matchedCodeValue(enteredCodeIds.contains(c.getId()) ? c.getValue() : null)
                        .build())
                .toList();

        System.out.println("Before return getCurrentLevelView");

        return LevelViewDTO.builder()
                .level(levelDto)
                .progress(progressDto)
                .sessionId(session.getId())
                .hints(hints)
                .sectors(sectors)
                .bonusCodes(bonusCodes)
                .penaltyCodes(penaltyCodes)
                .build();
    }

    @Override
    @Transactional
    public boolean autoPassLevel(Long sessionId) {
        GameSession session = findSession(sessionId);

        if (session.getStatus() != SessionStatus.ACTIVE) return false;

        // Получаем прогресс текущего уровня
        Optional<LevelProgress> optProgress = levelProgressRepository.findCurrentBySession(session);
        if (optProgress.isEmpty()) return false;

        LevelProgress progress = optProgress.get();
        if (progress.getClosedAt() != null) return false; // уже закрыт

        Level level = progress.getLevel();
        if (level == null || level.getApTime() == null || level.getApTime() <= 0) return false;

        Instant startedAt = progress.getStartedAt();
        if (startedAt == null) return false;

        long elapsedSec = Duration.between(startedAt, Instant.now()).getSeconds();

        System.out.println("elapsedSec = " + elapsedSec);
        System.out.println("level.getApTime() = " + level.getApTime());

        // Если время автоперехода вышло
        if (elapsedSec >= level.getApTime()) {
            closeLevelAndAdvance(session, progress, null);
            // 💥 вот это ключ:
            gameSessionRepository.flush(); // чтобы изменения реально записались
            return true;
        }

        return false;
    }

    // --- небольшая маппер-функция ---
    private CodeDTO mapCodeToDto(Code c) {
        return CodeDTO.builder()
                .id(c.getId())
                .levelId(c.getLevel() != null ? c.getLevel().getId() : null)
                .type(c.getType())
                .sectorNo(c.getSectorNo())
                .value(c.getValue())
                .shiftSeconds(c.getShiftSeconds())
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
        System.out.println("ENTER closeLevelAndAdvance");
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

    /**
     * Выполняет авто-переход (если можно) и возвращает актуальный view текущего уровня/сессии.
     * Это поможет клиенту получить свежие данные в одном ответе.
     */
    @Override
    @Transactional
    public LevelViewDTO autoPassLevelAndGetView(Long sessionId) {
        // Выполняем автопереход
        autoPassLevel(sessionId);

        // Сохраняем изменения и очищаем кэш, чтобы загрузить свежие данные
        entityManager.flush();
        entityManager.clear();

        // Загружаем актуальную сессию заново
        GameSession refreshed = findSession(sessionId);

        // Возвращаем текущее состояние уровня после перехода
        return getCurrentLevelView(refreshed.getId());
    }

}
