package dn.quest.questmanagement.service;

import dn.quest.questmanagement.dto.LevelDTO;
import dn.quest.questmanagement.entity.Level;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления уровнями квестов
 */
public interface LevelService {

    /**
     * Создать новый уровень
     *
     * @param dto DTO для создания уровня
     * @param questId ID квеста
     * @return созданный уровень
     */
    LevelDTO createLevel(LevelDTO dto, Long questId);

    /**
     * Обновить существующий уровень
     *
     * @param id ID уровня
     * @param dto DTO для обновления уровня
     * @return обновленный уровень
     */
    LevelDTO updateLevel(UUID id, LevelDTO dto);

    /**
     * Удалить уровень
     *
     * @param id ID уровня
     */
    void deleteLevel(UUID id);

    /**
     * Получить уровень по ID
     *
     * @param id ID уровня
     * @return уровень
     */
    LevelDTO getLevelById(UUID id);

    /**
     * Получить все уровни квеста
     *
     * @param questId ID квеста
     * @return список уровней
     */
    List<LevelDTO> getLevelsByQuestId(Long questId);

    /**
     * Получить уровень по порядковому номеру в квесте
     *
     * @param questId ID квеста
     * @param order порядковый номер
     * @return уровень
     */
    LevelDTO getLevelByOrder(Long questId, Integer order);

    /**
     * Получить следующий уровень
     *
     * @param questId ID квеста
     * @param currentOrder текущий порядковый номер
     * @return следующий уровень или null
     */
    LevelDTO getNextLevel(Long questId, Integer currentOrder);

    /**
     * Получить предыдущий уровень
     *
     * @param questId ID квеста
     * @param currentOrder текущий порядковый номер
     * @return предыдущий уровень или null
     */
    LevelDTO getPreviousLevel(Long questId, Integer currentOrder);

    /**
     * Изменить порядок уровня
     *
     * @param levelId ID уровня
     * @param newOrder новый порядковый номер
     * @return обновленный уровень
     */
    LevelDTO changeLevelOrder(UUID levelId, Integer newOrder);

    /**
     * Переместить уровень вверх
     *
     * @param levelId ID уровня
     * @return обновленный уровень
     */
    LevelDTO moveLevelUp(UUID levelId);

    /**
     * Переместить уровень вниз
     *
     * @param levelId ID уровня
     * @return обновленный уровень
     */
    LevelDTO moveLevelDown(UUID levelId);

    /**
     * Копировать уровни из одного квеста в другой
     *
     * @param sourceQuestId ID исходного квеста
     * @param targetQuestId ID целевого квеста
     */
    void copyLevelsForQuest(Long sourceQuestId, Long targetQuestId);

    /**
     * Проверить целостность уровней квеста
     *
     * @param questId ID квеста
     * @return результат проверки
     */
    LevelIntegrityResult checkLevelIntegrity(Long questId);

    /**
     * Переупорядочить уровни квеста
     *
     * @param questId ID квеста
     * @return список обновленных уровней
     */
    List<LevelDTO> reorderLevels(Long questId);

    /**
     * Результат проверки целостности уровней
     */
    class LevelIntegrityResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        public LevelIntegrityResult(boolean valid, List<String> errors, List<String> warnings) {
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
    }
}