package dn.quest.questmanagement.service.impl;

import dn.quest.questmanagement.dto.LevelDTO;
import dn.quest.questmanagement.entity.Level;
import dn.quest.questmanagement.repository.LevelRepository;
import dn.quest.questmanagement.repository.QuestRepository;
import dn.quest.questmanagement.service.LevelService;
import dn.quest.questmanagement.service.CodeService;
import dn.quest.questmanagement.service.LevelHintService;
import dn.quest.questmanagement.exception.LevelNotFoundException;
import dn.quest.questmanagement.exception.QuestNotFoundException;
import dn.quest.questmanagement.exception.LevelValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления уровнями квестов
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LevelServiceImpl implements LevelService {

    private final LevelRepository levelRepository;
    private final QuestRepository questRepository;
    private final CodeService codeService;
    private final LevelHintService levelHintService;

    @Override
    @Transactional
    public LevelDTO createLevel(LevelDTO dto, Long questId) {
        log.info("Creating new level for quest with ID: {}", questId);

        // Проверка существования квеста
        if (!questRepository.existsById(questId)) {
            throw new QuestNotFoundException("Quest not found with ID: " + questId);
        }

        // Валидация DTO
        dto.validate();

        // Создание нового уровня
        Level level = new Level();
        level.setQuestId(questId);
        level.setTitle(dto.getTitle());
        level.setDescription(dto.getDescription());
        level.setOrder(dto.getOrder() != null ? dto.getOrder() : getNextOrderNumber(questId));
        level.setLocation(dto.getLocation());
        level.setLatitude(dto.getLatitude());
        level.setLongitude(dto.getLongitude());
        level.setRadius(dto.getRadius());
        level.setMediaIds(dto.getMediaIds());
        level.setCreatedAt(LocalDateTime.now());
        level.setUpdatedAt(LocalDateTime.now());

        Level savedLevel = levelRepository.save(level);
        log.info("Level created successfully with ID: {}", savedLevel.getId());

        return convertToDTO(savedLevel);
    }

    @Override
    @Transactional
    public LevelDTO updateLevel(Long id, LevelDTO dto) {
        log.info("Updating level with ID: {}", id);

        Level level = getLevelEntityById(id);
        
        // Валидация DTO
        dto.validate();

        // Обновление полей
        level.setTitle(dto.getTitle());
        level.setDescription(dto.getDescription());
        if (dto.getOrder() != null) {
            level.setOrder(dto.getOrder());
        }
        level.setLocation(dto.getLocation());
        level.setLatitude(dto.getLatitude());
        level.setLongitude(dto.getLongitude());
        level.setRadius(dto.getRadius());
        level.setMediaIds(dto.getMediaIds());
        level.setUpdatedAt(LocalDateTime.now());

        Level savedLevel = levelRepository.save(level);
        log.info("Level updated successfully with ID: {}", savedLevel.getId());

        return convertToDTO(savedLevel);
    }

    @Override
    @Transactional
    public void deleteLevel(Long id) {
        log.info("Deleting level with ID: {}", id);

        Level level = getLevelEntityById(id);

        // Проверка, что у уровня нет связанных кодов и подсказок
        if (!codeService.getCodesByLevelId(id).isEmpty()) {
            throw new LevelValidationException("Cannot delete level with existing codes");
        }

        if (!levelHintService.getHintsByLevelId(id).isEmpty()) {
            throw new LevelValidationException("Cannot delete level with existing hints");
        }

        levelRepository.delete(level);
        log.info("Level deleted successfully with ID: {}", id);

        // Переупорядочивание оставшихся уровней
        reorderLevels(level.getQuestId());
    }

    @Override
    @Transactional(readOnly = true)
    public LevelDTO getLevelById(Long id) {
        Level level = getLevelEntityById(id);
        return convertToDTO(level);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelDTO> getLevelsByQuestId(Long questId) {
        List<Level> levels = levelRepository.findByQuestIdOrderByOrderAsc(questId);
        return levels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LevelDTO getLevelByOrder(Long questId, Integer order) {
        Level level = levelRepository.findByQuestIdAndOrder(questId, order)
                .orElseThrow(() -> new LevelNotFoundException(
                        "Level not found with quest ID: " + questId + " and order: " + order));
        return convertToDTO(level);
    }

    @Override
    @Transactional(readOnly = true)
    public LevelDTO getNextLevel(Long questId, Integer currentOrder) {
        Optional<Level> nextLevel = levelRepository.findNextLevel(questId, currentOrder);
        return nextLevel.map(this::convertToDTO).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public LevelDTO getPreviousLevel(Long questId, Integer currentOrder) {
        Optional<Level> previousLevel = levelRepository.findPreviousLevel(questId, currentOrder);
        return previousLevel.map(this::convertToDTO).orElse(null);
    }

    @Override
    @Transactional
    public LevelDTO changeLevelOrder(Long levelId, Integer newOrder) {
        log.info("Changing order of level with ID: {} to {}", levelId, newOrder);

        Level level = getLevelEntityById(levelId);
        Integer oldOrder = level.getOrder();

        if (oldOrder.equals(newOrder)) {
            return convertToDTO(level);
        }

        // Получение всех уровней квеста
        List<Level> levels = levelRepository.findByQuestIdOrderByOrderAsc(level.getQuestId());

        // Перемещение уровней
        if (newOrder < oldOrder) {
            // Двигаем уровни вверх
            levels.stream()
                    .filter(l -> l.getOrder() >= newOrder && l.getOrder() < oldOrder)
                    .forEach(l -> l.setOrder(l.getOrder() + 1));
        } else {
            // Двигаем уровни вниз
            levels.stream()
                    .filter(l -> l.getOrder() > oldOrder && l.getOrder() <= newOrder)
                    .forEach(l -> l.setOrder(l.getOrder() - 1));
        }

        level.setOrder(newOrder);
        level.setUpdatedAt(LocalDateTime.now());

        // Сохранение всех измененных уровней
        levelRepository.saveAll(levels);
        levelRepository.save(level);

        log.info("Level order changed successfully with ID: {}", levelId);

        return convertToDTO(level);
    }

    @Override
    @Transactional
    public LevelDTO moveLevelUp(Long levelId) {
        Level level = getLevelEntityById(levelId);
        
        if (level.getOrder() <= 1) {
            throw new LevelValidationException("Level is already at the top");
        }

        return changeLevelOrder(levelId, level.getOrder() - 1);
    }

    @Override
    @Transactional
    public LevelDTO moveLevelDown(Long levelId) {
        Level level = getLevelEntityById(levelId);
        
        Integer maxOrder = levelRepository.findMaxOrder(level.getQuestId());
        if (level.getOrder() >= maxOrder) {
            throw new LevelValidationException("Level is already at the bottom");
        }

        return changeLevelOrder(levelId, level.getOrder() + 1);
    }

    @Override
    @Transactional
    public void copyLevelsForQuest(Long sourceQuestId, Long targetQuestId) {
        log.info("Copying levels from quest {} to quest {}", sourceQuestId, targetQuestId);

        List<Level> sourceLevels = levelRepository.findByQuestIdOrderByOrderAsc(sourceQuestId);
        
        for (Level sourceLevel : sourceLevels) {
            Level copy = new Level();
            copy.setQuestId(targetQuestId);
            copy.setTitle(sourceLevel.getTitle());
            copy.setDescription(sourceLevel.getDescription());
            copy.setOrder(sourceLevel.getOrder());
            copy.setLocation(sourceLevel.getLocation());
            copy.setLatitude(sourceLevel.getLatitude());
            copy.setLongitude(sourceLevel.getLongitude());
            copy.setRadius(sourceLevel.getRadius());
            copy.setMediaIds(sourceLevel.getMediaIds());
            copy.setCreatedAt(LocalDateTime.now());
            copy.setUpdatedAt(LocalDateTime.now());

            Level savedCopy = levelRepository.save(copy);
            
            // Копирование кодов уровня
            codeService.copyCodesForLevel(sourceLevel.getId(), savedCopy.getId());
            
            // Копирование подсказок уровня
            levelHintService.copyHintsForLevel(sourceLevel.getId(), savedCopy.getId());
        }

        log.info("Levels copied successfully from quest {} to quest {}", sourceQuestId, targetQuestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelDTO> getLevelsInRadius(Double latitude, Double longitude, Double radiusKm) {
        List<Level> levels = levelRepository.findLevelsInRadius(latitude, longitude, radiusKm);
        return levels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelDTO> getLevelsWithMedia(Long questId) {
        List<Level> levels = levelRepository.findLevelsWithMedia(questId);
        return levels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LevelDTO> getLevelsWithHints(Long questId) {
        List<Level> levels = levelRepository.findLevelsWithHints(questId);
        return levels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LevelIntegrityResult checkLevelIntegrity(Long questId) {
        List<Level> levels = levelRepository.findByQuestIdOrderByOrderAsc(questId);
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Проверка наличия уровней
        if (levels.isEmpty()) {
            errors.add("Quest has no levels");
            return new LevelIntegrityResult(false, errors, warnings);
        }

        // Проверка последовательности порядковых номеров
        Set<Integer> orders = new HashSet<>();
        for (int i = 0; i < levels.size(); i++) {
            Level level = levels.get(i);
            Integer expectedOrder = i + 1;
            
            if (!level.getOrder().equals(expectedOrder)) {
                errors.add("Level " + level.getId() + " has order " + level.getOrder() + 
                          " but should be " + expectedOrder);
            }
            
            if (orders.contains(level.getOrder())) {
                errors.add("Duplicate order " + level.getOrder() + " found");
            }
            orders.add(level.getOrder());
        }

        // Проверка наличия кодов для каждого уровня
        for (Level level : levels) {
            var codes = codeService.getCodesByLevelId(level.getId());
            if (codes.isEmpty()) {
                errors.add("Level " + level.getOrder() + " has no codes");
            }
        }

        // Проверка геолокации
        for (Level level : levels) {
            if (level.getLatitude() != null && level.getLongitude() == null) {
                warnings.add("Level " + level.getOrder() + " has latitude but no longitude");
            }
            if (level.getLongitude() != null && level.getLatitude() == null) {
                warnings.add("Level " + level.getOrder() + " has longitude but no latitude");
            }
        }

        // Проверка радиуса
        for (Level level : levels) {
            if (level.getLatitude() != null && level.getLongitude() != null && level.getRadius() == null) {
                warnings.add("Level " + level.getOrder() + " has location but no radius specified");
            }
        }

        return new LevelIntegrityResult(errors.isEmpty(), errors, warnings);
    }

    @Override
    @Transactional
    public List<LevelDTO> reorderLevels(Long questId) {
        log.info("Reordering levels for quest with ID: {}", questId);

        List<Level> levels = levelRepository.findByQuestIdOrderByOrderAsc(questId);
        
        for (int i = 0; i < levels.size(); i++) {
            Level level = levels.get(i);
            level.setOrder(i + 1);
            level.setUpdatedAt(LocalDateTime.now());
        }

        levelRepository.saveAll(levels);
        
        log.info("Levels reordered successfully for quest with ID: {}", questId);

        return levels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Вспомогательные методы

    private Level getLevelEntityById(Long id) {
        return levelRepository.findById(id)
                .orElseThrow(() -> new LevelNotFoundException("Level not found with ID: " + id));
    }

    private Integer getNextOrderNumber(Long questId) {
        Integer maxOrder = levelRepository.findMaxOrder(questId);
        return maxOrder != null ? maxOrder + 1 : 1;
    }

    private LevelDTO convertToDTO(Level level) {
        LevelDTO dto = new LevelDTO();
        dto.setId(level.getId());
        dto.setQuestId(level.getQuestId());
        dto.setTitle(level.getTitle());
        dto.setDescription(level.getDescription());
        dto.setOrder(level.getOrder());
        dto.setLocation(level.getLocation());
        dto.setLatitude(level.getLatitude());
        dto.setLongitude(level.getLongitude());
        dto.setRadius(level.getRadius());
        dto.setMediaIds(level.getMediaIds());
        dto.setCreatedAt(level.getCreatedAt());
        dto.setUpdatedAt(level.getUpdatedAt());
        
        return dto;
    }
}