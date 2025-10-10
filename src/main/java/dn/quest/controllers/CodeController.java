package dn.quest.controllers;

import dn.quest.model.dto.CodeDTO;
import dn.quest.model.dto.LevelDTO;
import dn.quest.services.interfaces.CodeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(Routes.LEVEL_CODES)
@RequiredArgsConstructor
public class CodeController implements Routes {

    private final CodeService codeService;

    @GetMapping(CODES_BY_LEVEL)
    public ResponseEntity<List<CodeDTO>> getByLevel(@PathVariable Long levelId) {
        return ResponseEntity.ok(codeService.getAllByLevelId(levelId));
    }

    @PostMapping
    public ResponseEntity<CodeDTO> create(@RequestBody CodeDTO dto) {
        return ResponseEntity.ok(codeService.create(dto));
    }

    @PutMapping(ID)
    public ResponseEntity<CodeDTO> update(@PathVariable Long id, @RequestBody CodeDTO dto) {
        return ResponseEntity.ok(codeService.update(id, dto));
    }

    @DeleteMapping(ID)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        codeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
