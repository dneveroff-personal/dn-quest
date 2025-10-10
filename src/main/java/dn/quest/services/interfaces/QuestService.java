package dn.quest.services.interfaces;

import dn.quest.model.dto.QuestCreateUpdateDTO;
import dn.quest.model.dto.QuestDTO;

import java.util.List;

public interface QuestService {

    QuestDTO createQuest(QuestCreateUpdateDTO dto, String authorUsername);

    QuestDTO updateQuest(Long id, QuestCreateUpdateDTO dto, String authorUsername);

    void delete(Long id);

    QuestDTO getById(Long id);

    List<QuestDTO> getAll();

    List<QuestDTO> getPublished();
}
