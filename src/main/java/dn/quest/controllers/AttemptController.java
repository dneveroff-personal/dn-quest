package dn.quest.controllers;

import dn.quest.model.dto.CodeAttemptDTO;
import dn.quest.services.interfaces.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Routes.ATTEMPTS)
@RequiredArgsConstructor
public class AttemptController implements Routes {

    private final AttemptService attemptService;

    @GetMapping(ID)
    public ResponseEntity<CodeAttemptDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attemptService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CodeAttemptDTO>> getAll() {
        return ResponseEntity.ok(attemptService.getAll());
    }

    @GetMapping(LAST_ATTEMPTS)
    public ResponseEntity<List<CodeAttemptDTO>> getLastAttempts(
            @RequestParam Long sessionId,
            @RequestParam Long levelId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                attemptService.getLastAttempts(sessionId, levelId, limit)
        );
    }
}
