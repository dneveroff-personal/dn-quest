package dn.quest.controllers;

import dn.quest.model.dto.*;
import dn.quest.model.entities.quest.GameSession;
import dn.quest.services.interfaces.GameSessionService;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Routes.SESSIONS)
@RequiredArgsConstructor
public class GameSessionController implements Routes {

    private final GameSessionService gameSessionService;

    @PostMapping(START)
    public ResponseEntity<GameSessionDTO> startSession(@RequestBody StartSessionRequest req) {
        GameSession session = gameSessionService.start(req.questId(), req.userId(), req.teamId());
        return ResponseEntity.ok(toDTO(session));
    }

    @GetMapping(SESSION_CURRENT)
    public ResponseEntity<LevelViewDTO> getCurrentLevel(@PathVariable Long sessionId) {
        return ResponseEntity.ok(gameSessionService.getCurrentLevelView(sessionId));
    }

    @PostMapping(SESSION_CODE)
    public ResponseEntity<SubmitCodeResponse> submitCode(
            @PathVariable Long sessionId,
            @RequestBody SubmitCodeRequest req) {
        var result = gameSessionService.submitCode(sessionId, req.rawCode(), req.userId());
        return ResponseEntity.ok(new SubmitCodeResponse(result.name()));
    }

    @GetMapping(LAST_ATTEMPTS)
    public ResponseEntity<List<CodeAttemptDTO>> getLastAttempts(
            @PathVariable Long sessionId,
            @RequestParam Long levelId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                gameSessionService.lastAttempts(sessionId, levelId, limit)
                        .stream()
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
                        .toList()
        );
    }

    private GameSessionDTO toDTO(GameSession s) {
        return GameSessionDTO.builder()
                .id(s.getId())
                .questId(s.getQuest() != null ? s.getQuest().getId() : null)
                .userId(s.getUser() != null ? s.getUser().getId() : null)
                .teamId(s.getTeam() != null ? s.getTeam().getId() : null)
                .startedAt(s.getStartedAt())
                .finishedAt(s.getFinishedAt())
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .build();
    }

    public record StartSessionRequest(@NotNull Long questId, Integer userId, Long teamId) {}
    public record SubmitCodeRequest(@NotNull String rawCode, Integer userId) {}

    @Setter
    @Getter
    public static class SubmitCodeResponse {
        private String result;
        public SubmitCodeResponse() {}
        public SubmitCodeResponse(String result) { this.result = result; }

    }
}

