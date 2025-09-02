package dn.quest.controllers;

import dn.quest.model.dto.LevelDTO;
import dn.quest.services.interfaces.LevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Routes.LEVELS)
@RequiredArgsConstructor
public class LevelController {

    private final LevelService levelService;

    @PostMapping
    public ResponseEntity<LevelDTO> create(@RequestBody LevelDTO dto) {
        return ResponseEntity.ok(levelService.create(dto));
    }

    @PutMapping(Routes.ID)
    public ResponseEntity<LevelDTO> update(@PathVariable Long id, @RequestBody LevelDTO dto) {
        return ResponseEntity.ok(levelService.update(id, dto));
    }

    @DeleteMapping(Routes.ID)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        levelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(Routes.ID)
    public ResponseEntity<LevelDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(levelService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<LevelDTO>> getAll() {
        return ResponseEntity.ok(levelService.getAll());
    }
}
