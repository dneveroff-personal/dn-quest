package dn.quest.services.impl;

import dn.quest.model.dto.CodeDTO;
import dn.quest.model.dto.LevelDTO;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.quest.level.Code;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.repositories.CodeRepository;
import dn.quest.repositories.LevelRepository;
import dn.quest.services.interfaces.CodeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CodeServiceImpl implements CodeService {

    private final CodeRepository codeRepo;
    private final LevelRepository levelRepo;

    @Override
    public List<CodeDTO> getAllByLevel(Level level) {
        return codeRepo.findByLevel(level).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CodeDTO> getAllByLevelId(Long levelId) {
        Level level = levelRepo.findById(levelId)
                .orElseThrow(() -> new EntityNotFoundException("Level not found: " + levelId));

        List<CodeDTO> resultList = getAllByLevel(level);

        return getAllByLevel(level);
    }

    @Override
    public CodeDTO create(CodeDTO dto) {
        Level level = levelRepo.findById(dto.getLevelId())
                .orElseThrow(() -> new EntityNotFoundException("Level not found: " + dto.getLevelId()));

        Code code = toEntity(dto);
        code.setLevel(level);

        return toDto(codeRepo.save(code));
    }

    @Override
    public CodeDTO update(Long id, CodeDTO dto) {
        Code code = codeRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Code not found: " + id));

        code.setValue(dto.getValue().toLowerCase());
        code.setType(dto.getType());
        code.setSectorNo(dto.getSectorNo());
        code.setShiftSeconds(dto.getShiftSeconds());

        return toDto(codeRepo.save(code));
    }

    @Override
    public void delete(Long id) {
        if (!codeRepo.existsById(id)) {
            throw new EntityNotFoundException("Code not found: " + id);
        }
        codeRepo.deleteById(id);
    }

    // ---------------- Mapping ----------------
    private CodeDTO toDto(Code code) {
        return CodeDTO.builder()
                .id(code.getId())
                .levelId(code.getLevel().getId())
                .value(code.getValue())
                .type(code.getType())
                .sectorNo(code.getSectorNo())
                .shiftSeconds(code.getShiftSeconds())
                .build();
    }

    private Code toEntity(CodeDTO dto) {
        Code code = new Code();
        code.setId(dto.getId());
        code.setValue(dto.getValue().toLowerCase());
        code.setType(dto.getType() != null ? dto.getType() : dn.quest.model.entities.enums.CodeType.NORMAL);
        code.setSectorNo(dto.getSectorNo());
        code.setShiftSeconds(dto.getShiftSeconds() != null ? dto.getShiftSeconds() : 0);
        return code;
    }
}
