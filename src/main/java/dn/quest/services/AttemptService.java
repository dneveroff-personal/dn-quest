package dn.quest.services;

import dn.quest.model.entities.enums.AttemptResult;
import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.level.Code;
import dn.quest.model.entities.quest.level.CodeAttempt;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.model.entities.quest.level.LevelCompletion;
import dn.quest.model.entities.quest.level.LevelProgress;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class AttemptService {

    private final GameSessionRepository sessionRepo;
    private final LevelRepository levelRepo;
    private final CodeRepository codeRepo;
    private final CodeAttemptRepository attemptRepo;
    private final LevelProgressRepository progressRepo;
    private final LevelCompletionRepository completionRepo;

    public AttemptService(GameSessionRepository sessionRepo,
                          LevelRepository levelRepo,
                          CodeRepository codeRepo,
                          CodeAttemptRepository attemptRepo,
                          LevelProgressRepository progressRepo,
                          LevelCompletionRepository completionRepo) {
        this.sessionRepo = sessionRepo;
        this.levelRepo = levelRepo;
        this.codeRepo = codeRepo;
        this.attemptRepo = attemptRepo;
        this.progressRepo = progressRepo;
        this.completionRepo = completionRepo;
    }

    @Transactional
    public CodeAttempt processAttempt(Long sessionId, Long userId, Long levelId, String rawSubmitted,
                                      String ip, String userAgent) {

        // 1. Загружаем session / level / progress
        GameSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        Level level = levelRepo.findById(levelId)
                .orElseThrow(() -> new IllegalArgumentException("Level not found"));

        LevelProgress progress = progressRepo.findBySessionAndLevel(session, level)
                .orElseThrow(() -> new IllegalStateException("Level not opened for this session"));

        // 2. Нормализация ввода
        String normalized = rawSubmitted == null ? "" : rawSubmitted.trim().toLowerCase(Locale.ROOT);

        // 3. Получаем список кодов для уровня
        List<Code> codes = codeRepo.findByLevel(level);

        // 4. Пытаемся найти совпадение
        Code matched = codes.stream()
                .filter(c -> c.getValue().equals(normalized))
                .findFirst()
                .orElse(null);

        // 5. Создаём попытку
        CodeAttempt attempt = new CodeAttempt();
        attempt.setSession(session);
        attempt.setLevel(level);
        if (userId != null) {
            User u = new User();
            u.setId(userId);
            attempt.setUser(u);
        }
        attempt.setSubmittedRaw(rawSubmitted);
        attempt.setSubmittedNormalized(normalized);
        attempt.setCreatedAt(Instant.now());
        attempt.setIp(ip);
        attempt.setUserAgent(userAgent);

        AttemptResult result;

        // --- если код не найден
        if (matched == null) {
            result = AttemptResult.WRONG;
            attempt.setResult(result);
            return attemptRepo.save(attempt);
        }

        // --- если код найден, проверяем DUPLICATE
        List<CodeAttempt> prevAccepted = attemptRepo.findBySessionAndMatchedCode(session, matched);
        if (!prevAccepted.isEmpty()) {
            result = AttemptResult.DUPLICATE;
            attempt.setResult(result);
            attempt.setMatchedCode(matched);
            attempt.setMatchedSectorNo(matched.getSectorNo());
            return attemptRepo.save(attempt);
        }

        // --- Новый корректный код
        switch (matched.getType()) {
            case NORMAL -> {
                result = AttemptResult.ACCEPTED_NORMAL;

                // увеличиваем счётчик закрытых секторов
                progress.setSectorsClosed(progress.getSectorsClosed() + 1);

                // проверяем, собраны ли все нужные сектора
                if (progress.getSectorsClosed() >= level.getRequiredSectors()) {
                    Instant passTime = Instant.now();
                    LevelCompletion completion = new LevelCompletion();
                    completion.setSession(session);
                    completion.setLevel(level);

                    if (userId != null) {
                        User u = new User();
                        u.setId(userId);
                        completion.setPassedByUser(u);
                    }

                    completion.setPassTime(passTime);
                    long duration = Duration.between(progress.getStartedAt(), passTime).getSeconds();
                    completion.setDurationSec(duration);
                    completion.setBonusOnLevelSec(progress.getBonusOnLevelSec());
                    completion.setPenaltyOnLevelSec(progress.getPenaltyOnLevelSec());

                    completionRepo.save(completion);

                    progress.setClosedAt(passTime);
                }

                progressRepo.save(progress);
            }

            case BONUS -> {
                result = AttemptResult.ACCEPTED_BONUS;
                progress.setBonusOnLevelSec(progress.getBonusOnLevelSec() + matched.getShiftSeconds());
                progressRepo.save(progress);
            }

            case PENALTY -> {
                result = AttemptResult.ACCEPTED_PENALTY;
                progress.setPenaltyOnLevelSec(progress.getPenaltyOnLevelSec() + Math.abs(matched.getShiftSeconds()));
                progressRepo.save(progress);
            }

            default -> result = AttemptResult.WRONG;
        }

        attempt.setMatchedCode(matched);
        attempt.setMatchedSectorNo(matched.getSectorNo());
        attempt.setResult(result);

        return attemptRepo.save(attempt);
    }
}
