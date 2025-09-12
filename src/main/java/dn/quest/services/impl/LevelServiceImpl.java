package dn.quest.services.impl;

import dn.quest.model.dto.LevelDTO;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.repositories.LevelRepository;
import dn.quest.repositories.QuestRepository;
import dn.quest.services.interfaces.LevelService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LevelServiceImpl implements LevelService {

    private final LevelRepository levelRepository;
    private final QuestRepository questRepository;

    @Override
    public LevelDTO getById(Long id) {
        return levelRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Level not found: " + id));
    }

    @Override
    public List<LevelDTO> getAll() {
        return levelRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LevelDTO> getAllByQuest(Quest quest) {
        return levelRepository.findAllOrdered(quest).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LevelDTO create(LevelDTO dto) {
        Level level = toEntity(dto);
        Level saved = levelRepository.save(level);
        return toDto(saved);
    }

    @Override
    public LevelDTO update(Long id, LevelDTO dto) {
        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Level not found: " + id));

        level.setTitle(dto.getTitle());
        level.setDescriptionHtml(dto.getDescriptionHtml());
        level.setOrderIndex(dto.getOrderIndex());
        level.setApTime(dto.getApTime());
        level.setRequiredSectors(dto.getRequiredSectors());

        return toDto(levelRepository.save(level));
    }

    @Override
    public void delete(Long id) {
        if (!levelRepository.existsById(id)) {
            throw new EntityNotFoundException("Level not found: " + id);
        }
        levelRepository.deleteById(id);
    }

    @Override
    public LevelDTO getFirstInQuest(Quest quest) {
        Level first = levelRepository.findFirstInQuest(quest);
        if (first == null) {
            throw new EntityNotFoundException("First level not found for quest: " + quest.getId());
        }
        return toDto(first);
    }

    @Override
    public LevelDTO getNext(Quest quest, Integer orderIndex) {
        Level next = levelRepository.findNext(quest, orderIndex);
        if (next == null) {
            throw new EntityNotFoundException("Next level not found for quest: " + quest.getId() + " after orderIndex=" + orderIndex);
        }
        return toDto(next);
    }

    @Override
    public List<LevelDTO> getAllByQuestId(Long questId) {
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new EntityNotFoundException("Квест не найден: " + questId));

            // вернуть все заявки по квесту — добавим соответствующий метод в репозиторий
            return levelRepository.findAllOrdered(quest);
        }
    }

    // ---------------- Mapping ----------------

    private LevelDTO toDto(Level level) {
        return LevelDTO.builder()
                .id(level.getId())
                .questId(level.getQuest().getId())
                .orderIndex(level.getOrderIndex())
                .title(level.getTitle())
                .descriptionHtml(level.getDescriptionHtml())
                .apTime(level.getApTime())
                .requiredSectors(level.getRequiredSectors())
                .build();
    }

    private Level toEntity(LevelDTO dto) {
        Level level = new Level();
        level.setId(dto.getId());
        // Quest подтянем снаружи (например, в контроллере или сервисе QuestService)
        level.setOrderIndex(dto.getOrderIndex());
        level.setTitle(dto.getTitle());
        level.setDescriptionHtml(dto.getDescriptionHtml());
        level.setApTime(dto.getApTime());
        level.setRequiredSectors(dto.getRequiredSectors());
        return level;
    }
}