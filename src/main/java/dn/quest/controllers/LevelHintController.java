package dn.quest.controllers;

import dn.quest.model.dto.LevelHintDTO;
import dn.quest.services.interfaces.LevelHintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Routes.API_HINTS)
@RequiredArgsConstructor
public class LevelHintController implements Routes {

    private final LevelHintService levelHintService;

    @GetMapping
    public ResponseEntity<List<LevelHintDTO>> list(@PathVariable Long levelId) {
        return ResponseEntity.ok(levelHintService.getHintsByLevel(levelId));
    }

    @PostMapping
    public ResponseEntity<LevelHintDTO> create(@PathVariable Long levelId, @RequestBody LevelHintDTO dto) {
        return ResponseEntity.ok(levelHintService.createHint(levelId, dto));
    }

    @PutMapping(ID)
    public ResponseEntity<LevelHintDTO> update(@PathVariable Long levelId, @PathVariable Long id, @RequestBody LevelHintDTO dto) {
        return ResponseEntity.ok(levelHintService.updateHint(levelId, id, dto));
    }

    @DeleteMapping(ID)
    public ResponseEntity<Void> delete(@PathVariable Long levelId, @PathVariable Long id) {
        levelHintService.deleteHint(levelId, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(REORDER)
    public ResponseEntity<Void> reorder(@PathVariable Long levelId, @RequestBody List<Long> orderedIds) {
        levelHintService.reorder(levelId, orderedIds);
        return ResponseEntity.noContent().build();
    }
}