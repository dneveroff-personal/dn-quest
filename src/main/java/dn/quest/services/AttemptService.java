package dn.quest.services;

import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.level.Code;
import dn.quest.model.entities.quest.level.CodeAttempt;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.model.entities.quest.level.LevelCompletion;
import dn.quest.model.entities.quest.level.LevelProgress;
import dn.quest.model.entities.user.User;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.enums.AttemptResult;
import dn.quest.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AttemptService {

    private final GameSessionRepository sessionRepo;
    private final LevelRepository levelRepo;
    private final CodeRepository codeRepo;
    private final CodeAttemptRepository attemptRepo;
    private final LevelProgressRepository progressRepo;
    private final LevelCompletionRepository completionRepo;
    private final GameSessionRepository gameSessionRepo;

    public AttemptService(GameSessionRepository sessionRepo,
                          LevelRepository levelRepo,
                          CodeRepository codeRepo,
                          CodeAttemptRepository attemptRepo,
                          LevelProgressRepository progressRepo,
                          LevelCompletionRepository completionRepo,
                          GameSessionRepository gameSessionRepo) {
        this.sessionRepo = sessionRepo;
        this.levelRepo = levelRepo;
        this.codeRepo = codeRepo;
        this.attemptRepo = attemptRepo;
        this.progressRepo = progressRepo;
        this.completionRepo = completionRepo;
        this.gameSessionRepo = gameSessionRepo;
    }

    /**
     * Основная процедура обработки попытки.
     *
     * @param sessionId id сессии
     * @param userId id пользователя, который ввёл (nullable для соло?)
     * @param levelId id текущего уровня
     * @param rawSubmitted входной код от UI
     * @param ip опционально
     * @param userAgent опционально
     * @return созданный CodeAttempt
     */
    @Transactional
    public CodeAttempt processAttempt(Long sessionId, Integer userId, Long levelId, String rawSubmitted,
                                      String ip, String userAgent) {

        // 1) загрузить session / level / progress
        GameSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        Level level = levelRepo.findById(levelId)
                .orElseThrow(() -> new IllegalArgumentException("Level not found"));

        LevelProgress progress = progressRepo.findBySessionAndLevel(session, level)
                .orElseThrow(() -> new IllegalStateException("Level not opened for this session"));

        // 2) нормализовать (по ТЗ: только lower-case). Я также обрезаю пробелы минимально.
        String normalized = rawSubmitted == null ? "" : rawSubmitted.trim().toLowerCase(Locale.ROOT);

        // 3) получить коды для уровня
        List<Code> codes = codeRepo.findByLevel(level);

        // 4) попытка - ищем совпадение среди codes (value stored already normalized)
        Code matched = null;
        for (Code c : codes) {
            if (c.getValue().equals(normalized)) { matched = c; break; }
        }

        // 5) детект duplicate: если matched != null и уже был ACCEPTED для этого session и того же matched (или sector)
        boolean isDuplicate = false;
        AttemptResult result;
        CodeAttempt attempt = new CodeAttempt();
        attempt.setSession(session);
        attempt.setLevel(level);
        if (userId != null) {
            User u = new User(); u.setId(userId); // можно загрузить пользователя при необходимости
            attempt.setUser(u);
        }
        attempt.setSubmittedRaw(rawSubmitted);
        attempt.setSubmittedNormalized(normalized);
        attempt.setCreatedAt(Instant.now());
        attempt.setIp(ip);
        attempt.setUserAgent(userAgent);

        if (matched == null) {
            // wrong
            result = AttemptResult.WRONG;
            attempt.setResult(result);
            attemptRepo.save(attempt);
            return attempt;
        }

        // matched != null
        // проверяем, была ли раньше запись ACCEPTED_NORMAL/ACCEPTED_BONUS/... по тому же matchedCode в этой сессии
        List<CodeAttempt> prevAccepted = attemptRepo.findBySessionAndMatchedCode(session, matched);
        if (!prevAccepted.isEmpty()) {
            // уже были — считаем DUPLICATE (повтор)
            isDuplicate = true;
        }

        if (isDuplicate) {
            result = AttemptResult.DUPLICATE;
            attempt.setResult(result);
            attempt.setMatchedCode(matched);
            attempt.setMatchedSectorNo(matched.getSectorNo());
            attemptRepo.save(attempt);
            return attempt;
        }

        // если не повтор: в зависимости от типа кода — применяем
        switch (matched.getType()) {
            case NORMAL:
                // помечаем принятой нормальной попыткой
                result = AttemptResult.ACCEPTED_NORMAL;
                // update progress: если сектор новый — увеличиваем completedSectors и, возможно, пометим сектор закрытым
                // Todo: Здесь нужна логика: отслеживать какие sectorNo уже закрыты — можно хранить в отдельной таблице/поле.
                progress.setCompletedSectors(progress.getCompletedSectors() + 1);
                if (/* достигли requiredSectors */ progress.getCompletedSectors() >= level.getRequiredSectors()) {
                    // закрытие уровня: создаём LevelCompletion и отмечаем progress.completedAt
                    LevelCompletion completion = new LevelCompletion();
                    completion.setSession(session);
                    completion.setLevel(level);
                    // passedByUser — если есть userId — ставим
                    if (userId != null) {
                        User u = new User(); u.setId(userId);
                        completion.setPassedByUser(u);
                    }
                    Instant passTime = Instant.now();
                    completion.setPassTime(passTime);
                    long duration = Duration.between(progress.getStartedAt(), passTime).getSeconds();
                    completion.setDurationSec(duration);
                    // бонус/штраф на уровне: для простоты — суммируем shiftSeconds от matched codes
                    completion.setBonusOnLevelSec(0);
                    completion.setPenaltyOnLevelSec(0);

                    completionRepo.save(completion);

                    progress.setCompletedAt(passTime);
                }
                progressRepo.save(progress);
                break;

            case BONUS:
                result = AttemptResult.ACCEPTED_BONUS;
                // применить бонус к сессии
                session.setBonusTimeSumSec(session.getBonusTimeSumSec() + matched.getShiftSeconds());
                gameSessionRepo.save(session);
                break;

            case PENALTY:
                result = AttemptResult.ACCEPTED_PENALTY;
                session.setPenaltyTimeSumSec(session.getPenaltyTimeSumSec() + Math.abs(matched.getShiftSeconds()));
                gameSessionRepo.save(session);
                break;

            default:
                result = AttemptResult.WRONG;
        }

        attempt.setMatchedCode(matched);
        attempt.setMatchedSectorNo(matched.getSectorNo());
        attempt.setResult(result);
        attemptRepo.save(attempt);

        return attempt;
    }
}