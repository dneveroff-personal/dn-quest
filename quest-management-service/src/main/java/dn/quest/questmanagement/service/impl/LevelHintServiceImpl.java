package dn.quest.questmanagement.service.impl;

import dn.quest.questmanagement.dto.LevelHintDTO;
import dn.quest.questmanagement.entity.LevelHint;
import dn.quest.questmanagement.repository.LevelHintRepository;
import dn.quest.questmanagement.repository.LevelRepository;
import dn.quest.questmanagement.service.LevelHintService;
import dn.quest.questmanagement.exception.LevelHintNotFoundException;
import dn.quest.questmanagement.exception.LevelNotFoundException;
import dn.quest.questmanagement.exception.HintValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public LevelHintDTO createHint(LevelHintDTO dto, Long levelId) {
        log.info("Creating new hint for level with ID: {}", levelId);

        // Проверка существования уровня
        if (!levelRepository.existsById(levelId)) {
            throw new LevelNotFoundException("Level not found with ID: " + levelId);
        }

        // Валидация DTO
        dto.validate();

        // Создание новой подсказки
        LevelHint hint = new LevelHint();
        hint.setLevelId(levelId);
        hint.setText(dto.getText());
        hint.setCost(dto.getCost() != null ? dto.getCost() : 0);
        hint.setAvailableAfter(dto.getAvailableAfter());
        hint.setActive(dto.getActive() != null ? dto.getActive() : true);
        hint.setUsedBy(new HashMap<>());
        hint.setCreatedAt(LocalDateTime.now());
        hint.setUpdatedAt(LocalDateTime.now());

        LevelHint savedHint = levelHintRepository.save(hint);
        log.info("Hint created successfully with ID: {}", savedHint.getId());

        return convertToDTO(savedHint);
    }

    @Override
    @Transactional
    public LevelHintDTO updateHint(Long id, LevelHintDTO dto) {
        log.info("Updating hint with ID: {}", id);

        LevelHint hint = getHintEntityById(id);
        
        // Валидация DTO
        dto.validate();

        // Обновление полей
        hint.setText(dto.getText());
        hint.setCost(dto.getCost());
        hint.setAvailableAfter(dto.getAvailableAfter());
        if (dto.getActive() != null) {
            hint.setActive(dto.getActive());
        }
        hint.setUpdatedAt(LocalDateTime.now());

        LevelHint savedHint = levelHintRepository.save(hint);
        log.info("Hint updated successfully with ID: {}", savedHint.getId());

        return convertToDTO(savedHint);
    }

    @Override
    @Transactional
    public void deleteHint(Long id) {
        log.info("Deleting hint with ID: {}", id);

        LevelHint hint = getHintEntityById(id);
        levelHintRepository.delete(hint);
        log.info("Hint deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public LevelHintDTO getHintById(Long id) {
        LevelHint hint = getHintEntityById(id);
        return convertToDTO(hint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelHintDTO> getHintsByLevelId(Long levelId) {
        List<LevelHint> hints = levelHintRepository.findByLevelId(levelId);
        return hints.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelHintDTO> getAvailableHintsByLevelId(Long levelId, Long userId) {
        List<LevelHint> hints = levelHintRepository.findByLevelId(levelId);
        LocalDateTime now = LocalDateTime.now();
        
        return hints.stream()
                .filter(hint -> hint.getActive())
                .filter(hint -> hint.getAvailableAfter() == null || hint.getAvailableAfter().isBefore(now))
                .filter(hint -> hint.getUsedBy().containsKey(userId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelHintDTO> getUsedHintsByLevelId(Long levelId, Long userId) {
        List<LevelHint> hints = levelHintRepository.findByLevelId(levelId);
        
        return hints.stream()
                .filter(hint -> hint.getUsedBy().containsKey(userId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelHintDTO> getUnusedHintsByLevelId(Long levelId, Long userId) {
        List<LevelHint> hints = levelHintRepository.findByLevelId(levelId);
        LocalDateTime now = LocalDateTime.now();
        
        return hints.stream()
                .filter(hint -> hint.getActive())
                .filter(hint -> hint.getAvailableAfter() == null || hint.getAvailableAfter().isBefore(now))
                .filter(hint -> !hint.getUsedBy().containsKey(userId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HintUsageResult useHint(Long hintId, Long userId) {
        log.info("Using hint with ID: {} by user: {}", hintId, userId);

        LevelHint hint = getHintEntityById(hintId);

        // Проверка доступности подсказки
        HintAvailabilityResult availability = checkHintAvailability(hintId, userId);
        if (!availability.isAvailable()) {
            return new HintUsageResult(false, availability.getReason(), false, hint.getCost());
        }

        // Проверка, что пользователь еще не использовал эту подсказку
        boolean wasAlreadyUsed = hint.getUsedBy().containsKey(userId);
        if (wasAlreadyUsed) {
            return new HintUsageResult(false, "Hint already used by this user", true, hint.getCost());
        }

        // Использование подсказки
        hint.getUsedBy().put(userId, LocalDateTime.now());
        hint.setUpdatedAt(LocalDateTime.now());

        levelHintRepository.save(hint);

        log.info("Hint used successfully with ID: {} by user: {}", hintId, userId);

        return new HintUsageResult(true, "Hint used successfully", false, hint.getCost());
    }

    @Override
    @Transactional(readOnly = true)
    public HintAvailabilityResult checkHintAvailability(Long hintId, Long userId) {
        LevelHint hint = getHintEntityById(hintId);
        LocalDateTime now = LocalDateTime.now();

        // Проверка, что подсказка активна
        if (!hint.getActive()) {
            return new HintAvailabilityResult(false, "Hint is not active", null);
        }

        // Проверка времени доступности
        if (hint.getAvailableAfter() != null && hint.getAvailableAfter().isAfter(now)) {
            return new HintAvailabilityResult(false, "Hint is not yet available", hint.getAvailableAfter());
        }

        return new HintAvailabilityResult(true, "Hint is available", null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelHintDTO> getHintsByAvailableAfter(Long levelId, LocalDateTime availableAfter) {
        List<LevelHint> hints = levelHintRepository.findByLevelIdAndAvailableAfterBefore(levelId, availableAfter);
        return hints.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelHintDTO> getFreeHintsByLevelId(Long levelId) {
        List<LevelHint> hints = levelHintRepository.findByLevelIdAndCost(levelId, 0);
        return hints.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelHintDTO> getPaidHintsByLevelId(Long levelId) {
        List<LevelHint> hints = levelHintRepository.findByLevelIdAndCostGreaterThan(levelId, 0);
        return hints.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void copyHintsForLevel(Long sourceLevelId, Long targetLevelId) {
        log.info("Copying hints from level {} to level {}", sourceLevelId, targetLevelId);

        List<LevelHint> sourceHints = levelHintRepository.findByLevelId(sourceLevelId);
        
        for (LevelHint sourceHint : sourceHints) {
            LevelHint copy = new LevelHint();
            copy.setLevelId(targetLevelId);
            copy.setText(sourceHint.getText());
            copy.setCost(sourceHint.getCost());
            copy.setAvailableAfter(sourceHint.getAvailableAfter());
            copy.setActive(sourceHint.getActive());
            copy.setUsedBy(new HashMap<>());
            copy.setCreatedAt(LocalDateTime.now());
            copy.setUpdatedAt(LocalDateTime.now());

            levelHintRepository.save(copy);
        }

        log.info("Hints copied successfully from level {} to level {}", sourceLevelId, targetLevelId);
    }

    @Override
    @Transactional(readOnly = true)
    public HintUsageStatistics getHintUsageStatistics(Long levelId) {
        List<LevelHint> hints = levelHintRepository.findByLevelId(levelId);
        
        long totalHints = hints.size();
        long activeHints = hints.stream().filter(LevelHint::getActive).count();
        long freeHints = hints.stream().filter(hint -> hint.getCost() == 0).count();
        long paidHints = hints.stream().filter(hint -> hint.getCost() > 0).count();
        
        Set<Long> uniqueUsers = new HashSet<>();
        int totalCost = 0;
        long usedHints = 0;

        for (LevelHint hint : hints) {
            if (!hint.getUsedBy().isEmpty()) {
                usedHints++;
                uniqueUsers.addAll(hint.getUsedBy().keySet());
                totalCost += hint.getCost() * hint.getUsedBy().size();
            }
        }

        return new HintUsageStatistics(totalHints, activeHints, freeHints, paidHints, 
                                     usedHints, uniqueUsers.size(), totalCost);
    }

    @Override
    @Transactional
    public void resetHintUsage(Long levelId) {
        log.info("Resetting hint usage for level with ID: {}", levelId);

        List<LevelHint> hints = levelHintRepository.findByLevelId(levelId);
        
        for (LevelHint hint : hints) {
            hint.setUsedBy(new HashMap<>());
            hint.setUpdatedAt(LocalDateTime.now());
        }

        levelHintRepository.saveAll(hints);
        
        log.info("Hint usage reset successfully for level with ID: {}", levelId);
    }

    @Override
    @Transactional
    public LevelHintDTO toggleHintActive(Long hintId, boolean active) {
        log.info("Toggling hint with ID: {} to active: {}", hintId, active);

        LevelHint hint = getHintEntityById(hintId);
        hint.setActive(active);
        hint.setUpdatedAt(LocalDateTime.now());

        LevelHint savedHint = levelHintRepository.save(hint);
        log.info("Hint active status changed successfully with ID: {}", savedHint.getId());

        return convertToDTO(savedHint);
    }

    // Вспомогательные методы

    private LevelHint getHintEntityById(Long id) {
        return levelHintRepository.findById(id)
                .orElseThrow(() -> new LevelHintNotFoundException("Hint not found with ID: " + id));
    }

    private LevelHintDTO convertToDTO(LevelHint hint) {
        LevelHintDTO dto = new LevelHintDTO();
        dto.setId(hint.getId());
        dto.setLevelId(hint.getLevelId());
        dto.setText(hint.getText());
        dto.setCost(hint.getCost());
        dto.setAvailableAfter(hint.getAvailableAfter());
        dto.setActive(hint.getActive());
        dto.setUsedBy(hint.getUsedBy());
        dto.setCreatedAt(hint.getCreatedAt());
        dto.setUpdatedAt(hint.getUpdatedAt());
        
        return dto;
    }
}