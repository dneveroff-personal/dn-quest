package dn.quest.services.interfaces;

import dn.quest.model.dto.QuestDTO;

import java.util.List;

public interface QuestService {

    QuestDTO create(QuestDTO questDTO);

    QuestDTO update(Long id, QuestDTO questDTO);

    void delete(Long id);

    QuestDTO getById(Long id);

    List<QuestDTO> getAll();

    List<QuestDTO> getPublished();

}
