package dn.quest.questmanagement.service.impl;

import dn.quest.questmanagement.dto.LevelHintDTO;
import dn.quest.questmanagement.entity.LevelHint;
import dn.quest.questmanagement.repository.LevelHintRepository;
import dn.quest.questmanagement.repository.LevelRepository;
import dn.quest.questmanagement.service.LevelHintService;
import dn.quest.questmanagement.exception.LevelHintNotFoundException;
import dn.quest.questmanagement.exception.LevelNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления подсказками уровней
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LevelHintServiceImpl implements LevelHintService {

    private final LevelHintRepository levelHintRepository;
    private final LevelRepository levelRepository;

    @Override
    @Transactional
    public LevelHintDTO createHint(LevelHintDTO dto, UUID levelId) {
        log.info("Creating new hint for level with ID: {}", levelId);

        // Проверка существования уровня
        if (!levelRepository.existsById(levelId)) {
            throw new LevelNotFoundException("Level not found with ID: " + levelId);
        }

        // Валидация DTO
        dto.isValid();

        // Создание новой подсказки
        LevelHint hint = new LevelHint();
        hint.setLevelId(levelId);
        hint.setHintText(dto.getHintText());
        hint.setCost(dto.getCost() != null ? dto.getCost() : 0);
        hint.setActive(dto.getActive() != null ? dto.getActive() : true);
        hint.setOffsetSec(dto.getOffsetSec() != null ? dto.getOffsetSec() : 0);
        hint.setTitle(dto.getTitle());
        hint.setHintType(dto.getHintType());
        hint.setOrderIndex(Optional.ofNullable(dto.getOrderIndex()).orElse(0));
        hint.setCreatedAt(LocalDateTime.now());
        hint.setUpdatedAt(LocalDateTime.now());

        LevelHint savedHint = levelHintRepository.save(hint);
        log.info("Hint created successfully with ID: {}", savedHint.getId());

        return convertToDTO(savedHint);
    }

    @Override
    @Transactional
    public LevelHintDTO updateHint(UUID id, LevelHintDTO dto) {
        log.info("Updating hint with ID: {}", id);

        LevelHint hint = getHintEntityById(id);

        // Валидация DTO
        dto.isValid();

        // Обновление полей
        hint.setHintText(dto.getHintText());
        hint.setCost(dto.getCost());
        if (dto.getActive() != null) {
            hint.setActive(dto.getActive());
        }
        hint.setActive(dto.getActive());
        hint.setOffsetSec(dto.getOffsetSec() != null ? dto.getOffsetSec() : 0);
        hint.setTitle(dto.getTitle());
        hint.setHintType(dto.getHintType());
        hint.setOrderIndex(Optional.ofNullable(dto.getOrderIndex()).orElse(0));
        hint.setUpdatedAt(LocalDateTime.now());

        LevelHint savedHint = levelHintRepository.save(hint);
        log.info("Hint updated successfully with ID: {}", savedHint.getId());

        return convertToDTO(savedHint);
    }

    @Override
    @Transactional
    public void deleteHint(UUID id) {
        log.info("Deleting hint with ID: {}", id);

        LevelHint hint = getHintEntityById(id);
        levelHintRepository.delete(hint);
        log.info("Hint deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public LevelHintDTO getHintById(UUID id) {
        LevelHint hint = getHintEntityById(id);
        return convertToDTO(hint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelHintDTO> getHintsByLevelId(UUID levelId) {
        List<LevelHint> hints = levelHintRepository.findByLevelIdOrderByOrderIndex(levelId);
        return hints.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelHintDTO> getAvailableHintsByLevelId(UUID levelId, UUID userId) {
        List<LevelHint> hints = levelHintRepository.findByLevelIdOrderByOrderIndex(levelId);
        LocalDateTime now = LocalDateTime.now();

        return hints.stream()
                .filter(LevelHint::getActive)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HintAvailabilityResult checkHintAvailability(UUID hintId, UUID userId) {
        LevelHint hint = getHintEntityById(hintId);
        LocalDateTime now = LocalDateTime.now();

        // Проверка, что подсказка активна
        if (!hint.getActive()) {
            return new HintAvailabilityResult(false, "Hint is not active", null);
        }

        return new HintAvailabilityResult(true, "Hint is available", null);
    }

    @Override
    @Transactional
    public void copyHintsForLevel(UUID sourceLevelId, UUID targetLevelId) {
        log.info("Copying hints from level {} to level {}", sourceLevelId, targetLevelId);

        List<LevelHint> sourceHints = levelHintRepository.findByLevelIdOrderByOrderIndex(sourceLevelId);

        for (LevelHint sourceHint : sourceHints) {
            LevelHint copy = new LevelHint();
            copy.setLevelId(targetLevelId);
            copy.setHintText(sourceHint.getHintText());
            copy.setCost(sourceHint.getCost());
            copy.setActive(sourceHint.getActive());
            copy.setCreatedAt(LocalDateTime.now());
            copy.setUpdatedAt(LocalDateTime.now());
            copy.setOffsetSec(sourceHint.getOffsetSec() != null ? sourceHint.getOffsetSec() : 0);
            copy.setTitle(sourceHint.getTitle());
            copy.setHintType(sourceHint.getHintType());
            copy.setOrderIndex(Optional.ofNullable(sourceHint.getOrderIndex()).orElse(0));

            levelHintRepository.save(copy);
        }

        log.info("Hints copied successfully from level {} to level {}", sourceLevelId, targetLevelId);
    }

    @Override
    @Transactional
    public LevelHintDTO toggleHintActive(UUID hintId, boolean active) {
        log.info("Toggling hint with ID: {} to active: {}", hintId, active);

        LevelHint hint = getHintEntityById(hintId);
        hint.setActive(active);
        hint.setUpdatedAt(LocalDateTime.now());

        LevelHint savedHint = levelHintRepository.save(hint);
        log.info("Hint active status changed successfully with ID: {}", savedHint.getId());

        return convertToDTO(savedHint);
    }

    // Вспомогательные методы

    private LevelHint getHintEntityById(UUID id) {
        return levelHintRepository.findById(id)
                .orElseThrow(() -> new LevelHintNotFoundException("Hint not found with ID: " + id));
    }

    private LevelHintDTO convertToDTO(LevelHint hint) {
        LevelHintDTO dto = new LevelHintDTO();
        dto.setId(hint.getId());
        dto.setLevelId(hint.getLevelId());
        dto.setHintText(hint.getHintText());
        dto.setCost(hint.getCost());
        dto.setActive(hint.getActive());
        dto.setCreatedAt(hint.getCreatedAt().toInstant(ZoneOffset.UTC));
        dto.setUpdatedAt(hint.getUpdatedAt().toInstant(ZoneOffset.UTC));
        dto.setOffsetSec(hint.getOffsetSec() != null ? hint.getOffsetSec() : 0);
        dto.setTitle(hint.getTitle());
        dto.setHintType(hint.getHintType());
        dto.setOrderIndex(Optional.ofNullable(hint.getOrderIndex()).orElse(0));

        return dto;
    }
}