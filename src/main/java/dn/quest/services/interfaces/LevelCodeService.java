package dn.quest.services.interfaces;

import dn.quest.model.dto.CodeDTO;

import java.util.List;

public interface LevelCodeService {
    CodeDTO create(CodeDTO dto);
    void delete(Long id);
    List<CodeDTO> getByLevel(Long levelId);
}
