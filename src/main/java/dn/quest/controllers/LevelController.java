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
public class LevelController implements Routes {

    private final LevelService levelService;

    @GetMapping
    public ResponseEntity<List<LevelDTO>> getAll() {
        return ResponseEntity.ok(levelService.getAll());
    }

    @PostMapping
    public ResponseEntity<LevelDTO> create(@RequestBody LevelDTO dto) {
        return ResponseEntity.ok(levelService.create(dto));
    }

    @PutMapping(ID)
    public ResponseEntity<LevelDTO> update(@PathVariable Long id, @RequestBody LevelDTO dto) {
        return ResponseEntity.ok(levelService.update(id, dto));
    }

    @DeleteMapping(ID)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        levelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(ID)
    public ResponseEntity<LevelDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(levelService.getById(id));
    }

    @GetMapping(LEVELS_BY_QUEST)
    public ResponseEntity<List<LevelDTO>> getByQuest(@PathVariable Long questId) {
        return ResponseEntity.ok(levelService.getAllByQuestId(questId));
    }
}
