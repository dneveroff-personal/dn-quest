package dn.quest.services.interfaces;

import dn.quest.model.dto.LevelDTO;
import dn.quest.model.entities.quest.Quest;

import java.util.List;

public interface LevelService {

    LevelDTO getById(Long id);

    List<LevelDTO> getAll();

    List<LevelDTO> getAllByQuest(Quest quest);

    LevelDTO create(LevelDTO dto);

    LevelDTO update(Long id, LevelDTO dto);

    void delete(Long id);

    LevelDTO getFirstInQuest(Quest quest);

    LevelDTO getNext(Quest quest, Integer orderIndex);
}
