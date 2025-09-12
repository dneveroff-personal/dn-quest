package dn.quest.services.impl;

import dn.quest.model.dto.CodeDTO;
import dn.quest.model.entities.enums.CodeType;
import dn.quest.model.entities.quest.level.Code;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.repositories.CodeRepository;
import dn.quest.repositories.LevelRepository;
import dn.quest.services.interfaces.LevelCodeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LevelCodeServiceImpl implements LevelCodeService {

    private final CodeRepository codeRepository;
    private final LevelRepository levelRepository;

    @Override
    public CodeDTO create(CodeDTO dto) {
        Level level = levelRepository.findById(dto.getLevelId())
                .orElseThrow(() -> new EntityNotFoundException("Level not found: " + dto.getLevelId()));

        Code code = new Code();
        code.setLevel(level);
        code.setType(dto.getType() != null ? dto.getType() : CodeType.NORMAL);
        code.setSectorNo(dto.getSectorNo());
        code.setValue(dto.getValue().toLowerCase().trim());
        code.setShiftSeconds(dto.getShiftSeconds());

        return toDto(codeRepository.save(code));
    }

    @Override
    public void delete(Long id) {
        if (!codeRepository.existsById(id)) {
            throw new EntityNotFoundException("Code not found: " + id);
        }
        codeRepository.deleteById(id);
    }

    @Override
    public List<CodeDTO> getByLevel(Long levelId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new EntityNotFoundException("Level not found: " + levelId));

        return codeRepository.findByLevel(level)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private CodeDTO toDto(Code code) {
        return CodeDTO.builder()
                .id(code.getId())
                .levelId(code.getLevel().getId())
                .type(code.getType())
                .sectorNo(code.getSectorNo())
                .value(code.getValue())
                .shiftSeconds(code.getShiftSeconds())
                .build();
    }
}
