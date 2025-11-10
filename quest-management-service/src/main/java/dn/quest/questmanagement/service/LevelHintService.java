package dn.quest.questmanagement.service;

import dn.quest.questmanagement.dto.LevelHintDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для управления подсказками уровней
 */
public interface LevelHintService {

    /**
     * Создать новую подсказку
     *
     * @param dto DTO для создания подсказки
     * @param levelId ID уровня
     * @return созданная подсказка
     */
    LevelHintDTO createHint(LevelHintDTO dto, Long levelId);

    /**
     * Обновить существующую подсказку
     *
     * @param id ID подсказки
     * @param dto DTO для обновления подсказки
     * @return обновленная подсказка
     */
    LevelHintDTO updateHint(Long id, LevelHintDTO dto);

    /**
     * Удалить подсказку
     *
     * @param id ID подсказки
     */
    void deleteHint(Long id);

    /**
     * Получить подсказку по ID
     *
     * @param id ID подсказки
     * @return подсказка
     */
    LevelHintDTO getHintById(Long id);

    /**
     * Получить все подсказки уровня
     *
     * @param levelId ID уровня
     * @return список подсказок
     */
    List<LevelHintDTO> getHintsByLevelId(Long levelId);

    /**
     * Получить доступные подсказки уровня для пользователя
     *
     * @param levelId ID уровня
     * @param userId ID пользователя
     * @return список доступных подсказок
     */
    List<LevelHintDTO> getAvailableHintsByLevelId(Long levelId, Long userId);

    /**
     * Получить использованные подсказки уровня для пользователя
     *
     * @param levelId ID уровня
     * @param userId ID пользователя
     * @return список использованных подсказок
     */
    List<LevelHintDTO> getUsedHintsByLevelId(Long levelId, Long userId);

    /**
     * Получить неиспользованные подсказки уровня для пользователя
     *
     * @param levelId ID уровня
     * @param userId ID пользователя
     * @return список неиспользованных подсказок
     */
    List<LevelHintDTO> getUnusedHintsByLevelId(Long levelId, Long userId);

    /**
     * Использовать подсказку
     *
     * @param hintId ID подсказки
     * @param userId ID пользователя
     * @return результат использования
     */
    HintUsageResult useHint(Long hintId, Long userId);

    /**
     * Проверить доступность подсказки
     *
     * @param hintId ID подсказки
     * @param userId ID пользователя
     * @return результат проверки
     */
    HintAvailabilityResult checkHintAvailability(Long hintId, Long userId);

    /**
     * Получить подсказки по времени
     *
     * @param levelId ID уровня
     * @param availableAfter время после которого подсказка доступна
     * @return список подсказок
     */
    List<LevelHintDTO> getHintsByAvailableAfter(Long levelId, LocalDateTime availableAfter);

    /**
     * Получить бесплатные подсказки уровня
     *
     * @param levelId ID уровня
     * @return список бесплатных подсказок
     */
    List<LevelHintDTO> getFreeHintsByLevelId(Long levelId);

    /**
     * Получить платные подсказки уровня
     *
     * @param levelId ID уровня
     * @return список платных подсказок
     */
    List<LevelHintDTO> getPaidHintsByLevelId(Long levelId);

    /**
     * Копировать подсказки из одного уровня в другой
     *
     * @param sourceLevelId ID исходного уровня
     * @param targetLevelId ID целевого уровня
     */
    void copyHintsForLevel(Long sourceLevelId, Long targetLevelId);

    /**
     * Получить статистику использования подсказок уровня
     *
     * @param levelId ID уровня
     * @return статистика
     */
    HintUsageStatistics getHintUsageStatistics(Long levelId);

    /**
     * Сбросить использование подсказок уровня
     *
     * @param levelId ID уровня
     */
    void resetHintUsage(Long levelId);

    /**
     * Активировать/деактивировать подсказку
     *
     * @param hintId ID подсказки
     * @param active флаг активности
     * @return обновленная подсказка
     */
    LevelHintDTO toggleHintActive(Long hintId, boolean active);

    /**
     * Результат использования подсказки
     */
    class HintUsageResult {
        private final boolean success;
        private final String message;
        private final boolean wasAlreadyUsed;
        private final Integer cost;

        public HintUsageResult(boolean success, String message, boolean wasAlreadyUsed, Integer cost) {
            this.success = success;
            this.message = message;
            this.wasAlreadyUsed = wasAlreadyUsed;
            this.cost = cost;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public boolean wasAlreadyUsed() {
            return wasAlreadyUsed;
        }

        public Integer getCost() {
            return cost;
        }
    }

    /**
     * Результат проверки доступности подсказки
     */
    class HintAvailabilityResult {
        private final boolean available;
        private final String reason;
        private final LocalDateTime availableAt;

        public HintAvailabilityResult(boolean available, String reason, LocalDateTime availableAt) {
            this.available = available;
            this.reason = reason;
            this.availableAt = availableAt;
        }

        public boolean isAvailable() {
            return available;
        }

        public String getReason() {
            return reason;
        }

        public LocalDateTime getAvailableAt() {
            return availableAt;
        }
    }

    /**
     * Статистика использования подсказок
     */
    class HintUsageStatistics {
        private final long totalHints;
        private final long activeHints;
        private final long freeHints;
        private final long paidHints;
        private final long usedHints;
        private final long uniqueUsers;
        private final Integer totalCost;

        public HintUsageStatistics(long totalHints, long activeHints, long freeHints, 
                                  long paidHints, long usedHints, long uniqueUsers, Integer totalCost) {
            this.totalHints = totalHints;
            this.activeHints = activeHints;
            this.freeHints = freeHints;
            this.paidHints = paidHints;
            this.usedHints = usedHints;
            this.uniqueUsers = uniqueUsers;
            this.totalCost = totalCost;
        }

        public long getTotalHints() {
            return totalHints;
        }

        public long getActiveHints() {
            return activeHints;
        }

        public long getFreeHints() {
            return freeHints;
        }

        public long getPaidHints() {
            return paidHints;
        }

        public long getUsedHints() {
            return usedHints;
        }

        public long getUniqueUsers() {
            return uniqueUsers;
        }

        public Integer getTotalCost() {
            return totalCost;
        }
    }
}