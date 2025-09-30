package dn.quest.services.interfaces;

import dn.quest.model.dto.LevelHintDTO;

import java.util.List;

public interface LevelHintService {
    List<LevelHintDTO> getHintsByLevel(Long levelId);
    LevelHintDTO createHint(Long levelId, LevelHintDTO dto);
    LevelHintDTO updateHint(Long levelId, Long id, LevelHintDTO dto);
    void deleteHint(Long levelId, Long id);

    void reorder(Long levelId, List<Long> orderedIds);
}
