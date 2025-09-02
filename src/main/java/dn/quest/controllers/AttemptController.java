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

    @PostMapping
    public ResponseEntity<CodeAttemptDTO> submit(@RequestBody CodeAttemptDTO attemptDTO) {
        return ResponseEntity.ok(attemptService.submit(attemptDTO));
    }

    @GetMapping(ID)
    public ResponseEntity<CodeAttemptDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attemptService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CodeAttemptDTO>> getAll() {
        return ResponseEntity.ok(attemptService.getAll());
    }
}
