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
import java.util.concurrent.atomic.AtomicInteger;
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
        Quest quest = questRepository.findById(dto.getQuestId())
                .orElseThrow(() -> new EntityNotFoundException("Quest not found: " + dto.getQuestId()));

        Level level = toEntity(dto);
        level.setQuest(quest); // 🔹 важный момент

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
                .orElseThrow(() -> new EntityNotFoundException("Quest not found: " + questId));
        return getAllByQuest(quest);
    }

    @Override
    @Transactional
    public void reorder(List<Long> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new IllegalArgumentException("Список ID уровней пустой");
        }

        // Загружаем все уровни
        List<Level> levels = levelRepository.findAllById(orderedIds);

        if (levels.size() != orderedIds.size()) {
            List<Long> foundIds = levels.stream().map(Level::getId).toList();
            List<Long> missing = orderedIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new EntityNotFoundException("Не найдены уровни с ID: " + missing);
        }

        // Проверяем, что все уровни принадлежат одному квесту
        Long questId = levels.get(0).getQuest().getId();
        boolean allSameQuest = levels.stream().allMatch(l -> l.getQuest().getId().equals(questId));
        if (!allSameQuest) {
            throw new IllegalArgumentException("Все уровни должны принадлежать одному квесту");
        }

        // 🔹 Присваиваем временные уникальные индексы, чтобы избежать конфликта
        for (int i = 0; i < levels.size(); i++) {
            levels.get(i).setOrderIndex(i + 1000); // временный большой offset
        }
        levelRepository.saveAll(levels);
        levelRepository.flush(); // сразу применяем изменения

        // 🔹 Присваиваем окончательные порядковые номера
        AtomicInteger counter = new AtomicInteger(1);
        orderedIds.forEach(id -> {
            Level l = levels.stream().filter(x -> x.getId().equals(id)).findFirst().orElseThrow();
            l.setOrderIndex(counter.getAndIncrement());
        });
        levelRepository.saveAll(levels);
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