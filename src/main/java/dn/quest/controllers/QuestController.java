package dn.quest.controllers;

import dn.quest.model.dto.QuestDTO;
import dn.quest.model.entities.enums.SessionStatus;
import dn.quest.model.entities.quest.GameSession;
import dn.quest.services.interfaces.GameSessionService;
import dn.quest.services.interfaces.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Routes.QUESTS)
@RequiredArgsConstructor
public class QuestController implements Routes {

    private final QuestService questService;
    private final GameSessionService gameSessionService;

    // ---------- Quest CRUD ----------
    @PostMapping
    public ResponseEntity<QuestDTO> create(@RequestBody QuestDTO questDTO) {
        return ResponseEntity.ok(questService.create(questDTO));
    }

    @PutMapping(ID)
    public ResponseEntity<QuestDTO> update(@PathVariable Long id, @RequestBody QuestDTO questDTO) {
        return ResponseEntity.ok(questService.update(id, questDTO));
    }

    @DeleteMapping(ID)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(ID)
    public ResponseEntity<QuestDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(questService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<QuestDTO>> getAll() {
        return ResponseEntity.ok(questService.getAll());
    }

    @GetMapping(PUBLISHED)
    public ResponseEntity<List<QuestDTO>> getPublished() {
        return ResponseEntity.ok(questService.getPublished());
    }

    // ---------- GameSession ----------
    @PostMapping(QUEST_START)
    public ResponseEntity<GameSession> startSession(
            @PathVariable Long questId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Long teamId
    ) {
        GameSession session = gameSessionService.start(questId, userId, teamId);
        return ResponseEntity.ok(session);
    }

    @GetMapping(QUEST_LEADERBOARD)
    public ResponseEntity<List<?>> leaderboard(@PathVariable Long questId) {
        return ResponseEntity.ok(gameSessionService.leaderboard(questId));
    }

    @PostMapping(SESSION_STATUS)
    public ResponseEntity<GameSession> setStatus(
            @PathVariable Long sessionId,
            @RequestParam SessionStatus status
    ) {
        return ResponseEntity.ok(gameSessionService.setStatus(sessionId, status));
    }
}
