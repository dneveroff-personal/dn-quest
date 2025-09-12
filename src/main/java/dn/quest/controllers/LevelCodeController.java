package dn.quest.controllers;

import dn.quest.model.dto.CodeDTO;
import dn.quest.services.interfaces.LevelCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Routes.LEVEL_CODES)
@RequiredArgsConstructor
public class LevelCodeController implements Routes {

    private final LevelCodeService levelCodeService;

    @PostMapping
    public ResponseEntity<CodeDTO> create(@RequestBody CodeDTO dto) {
        return ResponseEntity.ok(levelCodeService.create(dto));
    }

    @DeleteMapping(ID)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        levelCodeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(CODES_BY_LEVEL)
    public ResponseEntity<List<CodeDTO>> getByLevel(@PathVariable Long levelId) {
        return ResponseEntity.ok(levelCodeService.getByLevel(levelId));
    }
}
