package dn.quest.questmanagement.service;

import dn.quest.questmanagement.dto.QuestDTO;
import dn.quest.questmanagement.dto.LevelDTO;
import dn.quest.questmanagement.dto.CodeDTO;
import dn.quest.questmanagement.dto.LevelHintDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для валидации квестов
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestValidationService {

    private final QuestService questService;
    private final LevelService levelService;
    private final CodeService codeService;
    private final LevelHintService levelHintService;

    /**
     * Комплексная валидация квеста
     */
    public ValidationResult validateQuest(Long questId) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            // Получение квеста
            QuestDTO quest = questService.getQuestById(questId);
            
            // Валидация основных полей квеста
            validateQuestBasicFields(quest, errors, warnings);
            
            // Валидация уровней
            validateQuestLevels(questId, errors, warnings);
            
            // Валидация кодов
            validateQuestCodes(questId, errors, warnings);
            
            // Валидация подсказок
            validateQuestHints(questId, errors, warnings);
            
            // Валидация бизнес-правил
            validateBusinessRules(quest, errors, warnings);
            
        } catch (Exception e) {
            errors.add("Error during validation: " + e.getMessage());
            log.error("Error validating quest {}", questId, e);
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

    /**
     * Валидация основных полей квеста
     */
    private void validateQuestBasicFields(QuestDTO quest, List<String> errors, List<String> warnings) {
        // Проверка обязательных полей
        if (quest.getTitle() == null || quest.getTitle().trim().isEmpty()) {
            errors.add("Quest title is required");
        } else if (quest.getTitle().length() > 255) {
            errors.add("Quest title must be less than 255 characters");
        }

        if (quest.getDescription() == null || quest.getDescription().trim().isEmpty()) {
            errors.add("Quest description is required");
        }

        if (quest.getDifficulty() == null) {
            errors.add("Quest difficulty is required");
        }

        if (quest.getQuestType() == null) {
            errors.add("Quest quest type is required");
        }

        // Проверка участников
        if (quest.getMaxParticipants() != null && quest.getMinParticipants() != null) {
            if (quest.getMaxParticipants() < quest.getMinParticipants()) {
                errors.add("Max participants cannot be less than min participants");
            }
            if (quest.getMinParticipants() < 1) {
                errors.add("Min participants must be at least 1");
            }
            if (quest.getMaxParticipants() > 1000) {
                warnings.add("Max participants is very large (>1000)");
            }
        }

        // Проверка времени
        if (quest.getStartTime() != null && quest.getEndTime() != null) {
            if (quest.getStartTime().isAfter(quest.getEndTime())) {
                errors.add("Start time cannot be after end time");
            }
            if (quest.getStartTime().isBefore(java.time.LocalDateTime.now().minusDays(1))) {
                warnings.add("Start time is in the past");
            }
        }

        // Проверка длительности
        if (quest.getEstimatedDuration() != null) {
            if (quest.getEstimatedDuration() < 5) {
                warnings.add("Estimated duration is very short (<5 minutes)");
            }
            if (quest.getEstimatedDuration() > 1440) { // 24 часа
                warnings.add("Estimated duration is very long (>24 hours)");
            }
        }

        // Проверка тегов
        if (quest.getTags() == null || quest.getTags().isEmpty()) {
            warnings.add("Quest has no tags for better discoverability");
        } else {
            for (String tag : quest.getTags()) {
                if (tag.length() > 50) {
                    errors.add("Tag '" + tag + "' is too long (max 50 characters)");
                }
            }
        }
    }

    /**
     * Валидация уровней квеста
     */
    private void validateQuestLevels(Long questId, List<String> errors, List<String> warnings) {
        List<LevelDTO> levels = levelService.getLevelsByQuestId(questId);
        
        if (levels.isEmpty()) {
            errors.add("Quest must have at least one level");
            return;
        }

        // Проверка последовательности
        for (int i = 0; i < levels.size(); i++) {
            LevelDTO level = levels.get(i);
            Integer expectedOrder = i + 1;
            
            if (!level.getOrder().equals(expectedOrder)) {
                errors.add("Level " + level.getId() + " has order " + level.getOrder() + 
                          " but should be " + expectedOrder);
            }
            
            // Валидация полей уровня
            validateLevelFields(level, errors, warnings);
        }

        // Проверка геолокации
        boolean hasLocation = levels.stream().anyMatch(level -> 
                level.getLatitude() != null && level.getLongitude() != null);
        boolean allHaveLocation = levels.stream().allMatch(level -> 
                level.getLatitude() != null && level.getLongitude() != null);
        
        if (hasLocation && !allHaveLocation) {
            warnings.add("Some levels have location while others don't");
        }

        // Проверка радиуса
        levels.stream()
                .filter(level -> level.getLatitude() != null && level.getLongitude() != null)
                .forEach(level -> {
                    if (level.getRadius() == null) {
                        warnings.add("Level " + level.getOrder() + " has location but no radius specified");
                    } else if (level.getRadius() < 1) {
                        errors.add("Level " + level.getOrder() + " radius is too small (<1 meter)");
                    } else if (level.getRadius() > 10000) {
                        warnings.add("Level " + level.getOrder() + " radius is very large (>10km)");
                    }
                });
    }

    /**
     * Валидация полей уровня
     */
    private void validateLevelFields(LevelDTO level, List<String> errors, List<String> warnings) {
        if (level.getTitle() == null || level.getTitle().trim().isEmpty()) {
            errors.add("Level " + level.getOrder() + " title is required");
        }

        if (level.getDescription() == null || level.getDescription().trim().isEmpty()) {
            warnings.add("Level " + level.getOrder() + " has no description");
        }

        // Проверка координат
        if ((level.getLatitude() != null && level.getLongitude() == null) ||
            (level.getLatitude() == null && level.getLongitude() != null)) {
            errors.add("Level " + level.getOrder() + " must have both latitude and longitude or neither");
        }

        // Проверка диапазона координат
        if (level.getLatitude() != null) {
            if (level.getLatitude() < -90 || level.getLatitude() > 90) {
                errors.add("Level " + level.getOrder() + " latitude is out of range (-90 to 90)");
            }
        }
        if (level.getLongitude() != null) {
            if (level.getLongitude() < -180 || level.getLongitude() > 180) {
                errors.add("Level " + level.getOrder() + " longitude is out of range (-180 to 180)");
            }
        }
    }

    /**
     * Валидация кодов квеста
     */
    private void validateQuestCodes(Long questId, List<String> errors, List<String> warnings) {
        List<LevelDTO> levels = levelService.getLevelsByQuestId(questId);
        
        for (LevelDTO level : levels) {
            List<CodeDTO> codes = codeService.getCodesByLevelId(level.getId());
            
            if (codes.isEmpty()) {
                errors.add("Level " + level.getOrder() + " must have at least one code");
                continue;
            }

            // Проверка уникальности кодов в рамках квеста
            for (CodeDTO code : codes) {
                if (!codeService.isCodeUniqueInQuest(questId, code.getValue(), code.getId())) {
                    errors.add("Code '" + code.getValue() + "' is not unique within the quest");
                }
                
                // Валидация полей кода
                validateCodeFields(code, level.getOrder(), errors, warnings);
            }

            // Проверка наличия активных кодов
            long activeCodes = codes.stream().filter(CodeDTO::getActive).count();
            if (activeCodes == 0) {
                errors.add("Level " + level.getOrder() + " has no active codes");
            }

            // Проверка лимитов использования
            codes.stream()
                    .filter(code -> code.getUsageLimit() != null)
                    .forEach(code -> {
                        if (code.getUsageLimit() < 1) {
                            errors.add("Code '" + code.getValue() + "' usage limit must be at least 1");
                        }
                        if (code.getUsageLimit() > 10000) {
                            warnings.add("Code '" + code.getValue() + "' usage limit is very large (>10000)");
                        }
                    });
        }
    }

    /**
     * Валидация полей кода
     */
    private void validateCodeFields(CodeDTO code, Integer levelOrder, List<String> errors, List<String> warnings) {
        if (code.getValue() == null || code.getValue().trim().isEmpty()) {
            errors.add("Code value is required for level " + levelOrder);
        } else if (code.getValue().length() > 255) {
            errors.add("Code value must be less than 255 characters for level " + levelOrder);
        }

        if (code.getType() == null) {
            errors.add("Code type is required for level " + levelOrder);
        }

        if (code.getPoints() != null && code.getPoints() < 0) {
            errors.add("Code points cannot be negative for level " + levelOrder);
        }

        if (code.getPoints() != null && code.getPoints() > 10000) {
            warnings.add("Code points are very large (>10000) for level " + levelOrder);
        }
    }

    /**
     * Валидация подсказок квеста
     */
    private void validateQuestHints(Long questId, List<String> errors, List<String> warnings) {
        List<LevelDTO> levels = levelService.getLevelsByQuestId(questId);
        
        for (LevelDTO level : levels) {
            List<LevelHintDTO> hints = levelHintService.getHintsByLevelId(level.getId());
            
            for (LevelHintDTO hint : hints) {
                // Валидация полей подсказки
                validateHintFields(hint, level.getOrder(), errors, warnings);
            }

            // Проверка стоимости подсказок
            long totalHintCost = hints.stream()
                    .filter(LevelHintDTO::getActive)
                    .mapToInt(hint -> hint.getCost() != null ? hint.getCost() : 0)
                    .sum();
            
            if (totalHintCost > 1000) {
                warnings.add("Total hint cost for level " + level.getOrder() + " is very high (>1000 points)");
            }

            // Проверка времени доступности
            hints.stream()
                    .filter(hint -> hint.getAvailableAfter() != null)
                    .forEach(hint -> {
                        if (hint.getAvailableAfter().isBefore(java.time.LocalDateTime.now().minusDays(1))) {
                            warnings.add("Hint availability time is in the past for level " + level.getOrder());
                        }
                    });
        }
    }

    /**
     * Валидация полей подсказки
     */
    private void validateHintFields(LevelHintDTO hint, Integer levelOrder, List<String> errors, List<String> warnings) {
        if (hint.getText() == null || hint.getText().trim().isEmpty()) {
            errors.add("Hint text is required for level " + levelOrder);
        } else if (hint.getText().length() > 1000) {
            warnings.add("Hint text is very long (>1000 characters) for level " + levelOrder);
        }

        if (hint.getCost() != null && hint.getCost() < 0) {
            errors.add("Hint cost cannot be negative for level " + levelOrder);
        }

        if (hint.getCost() != null && hint.getCost() > 500) {
            warnings.add("Hint cost is very high (>500 points) for level " + levelOrder);
        }
    }

    /**
     * Валидация бизнес-правил
     */
    private void validateBusinessRules(QuestDTO quest, List<String> errors, List<String> warnings) {
        // Проверка статуса
        if (quest.getStatus() != null) {
            switch (quest.getStatus()) {
                case "PUBLISHED":
                    if (quest.getIsPublic() == null || !quest.getIsPublic()) {
                        warnings.add("Published quest is not public");
                    }
                    break;
                case "ACTIVE":
                    if (quest.getStartTime() == null || quest.getEndTime() == null) {
                        errors.add("Active quest must have start and end time");
                    }
                    break;
            }
        }

        // Проверка шаблона
        if (Boolean.TRUE.equals(quest.getIsTemplate())) {
            if (quest.getIsPublic() != null && quest.getIsPublic()) {
                warnings.add("Template quest is marked as public");
            }
        }
    }

    /**
     * Результат валидации
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public int getErrorCount() {
            return errors.size();
        }

        public int getWarningCount() {
            return warnings.size();
        }
    }
}