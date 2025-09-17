package dn.quest.services.interfaces;

import dn.quest.model.dto.CodeDTO;
import dn.quest.model.entities.quest.level.Level;

import java.util.List;

public interface CodeService {

    List<CodeDTO> getAllByLevel(Level level);

    List<CodeDTO> getAllByLevelId(Long levelId);

    CodeDTO create(CodeDTO dto);

    CodeDTO update(Long id, CodeDTO dto);

    void delete(Long id);
}
