package dn.quest.services.impl;

import dn.quest.model.dto.LevelHintDTO;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.model.entities.quest.level.LevelHint;
import dn.quest.repositories.LevelHintRepository;
import dn.quest.repositories.LevelRepository;
import dn.quest.services.interfaces.LevelHintService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LevelHintServiceImpl implements LevelHintService {

    private final LevelRepository levelRepository;
    private final LevelHintRepository hintRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LevelHintDTO> getHintsByLevel(Long levelId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new EntityNotFoundException("Уровень не найден"));

        return hintRepository.findByLevelOrderByOrderIndexAsc(level).stream()
                .map(this::toDTO)
                .sorted(Comparator.comparing(LevelHintDTO::getOffsetSec)) // сортируем по времени выдачи
                .toList();
    }

    @Override
    @Transactional
    public LevelHintDTO createHint(Long levelId, LevelHintDTO dto) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new EntityNotFoundException("Уровень не найден"));

        // Авто-присвоение orderIndex, если не задано
        if (dto.getOrderIndex() == null) {
            int nextOrder = hintRepository.findByLevelOrderByOrderIndexAsc(level).size() + 1;
            dto.setOrderIndex(nextOrder);
        }

        LevelHint hint = new LevelHint();
        hint.setLevel(level);
        hint.setText(dto.getText());
        hint.setOffsetSec(dto.getOffsetSec());
        hint.setOrderIndex(dto.getOrderIndex());

        return toDTO(hintRepository.save(hint));
    }

    @Override
    @Transactional
    public LevelHintDTO updateHint(Long levelId, Long id, LevelHintDTO dto) {
        LevelHint hint = hintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Подсказка не найдена"));

        if (!hint.getLevel().getId().equals(levelId)) {
            throw new IllegalArgumentException("Подсказка не принадлежит этому уровню");
        }

        hint.setText(dto.getText());
        hint.setOffsetSec(dto.getOffsetSec());
        // orderIndex не меняем, только через reorder
        return toDTO(hintRepository.save(hint));
    }

    @Override
    @Transactional
    public void deleteHint(Long levelId, Long id) {
        LevelHint hint = hintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Подсказка не найдена"));
        if (!hint.getLevel().getId().equals(levelId)) {
            throw new IllegalArgumentException("Подсказка не принадлежит этому уровню");
        }
        hintRepository.delete(hint);
    }

    @Override
    @Transactional
    public void reorder(Long levelId, List<Long> orderedIds) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new EntityNotFoundException("Уровень не найден"));
        List<LevelHint> hints = hintRepository.findByLevelOrderByOrderIndexAsc(level);

        if (hints.size() != orderedIds.size()) {
            throw new IllegalArgumentException("Размер списка не совпадает с количеством подсказок");
        }

        for (int i = 0; i < orderedIds.size(); i++) {
            Long id = orderedIds.get(i);
            LevelHint hint = hints.stream()
                    .filter(h -> h.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Подсказка " + id + " не найдена"));
            hint.setOrderIndex(i + 1);
            hintRepository.save(hint);
        }
    }

    private LevelHintDTO toDTO(LevelHint h) {
        return LevelHintDTO.builder()
                .id(h.getId())
                .levelId(h.getLevel().getId())
                .text(h.getText())
                .offsetSec(h.getOffsetSec())
                .orderIndex(h.getOrderIndex())
                .build();
    }
}
