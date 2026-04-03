package dn.quest.questmanagement.service;

import dn.quest.questmanagement.dto.LevelHintDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    LevelHintDTO createHint(LevelHintDTO dto, UUID levelId);

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
    List<LevelHintDTO> getHintsByLevelId(UUID levelId);

    /**
     * Получить доступные подсказки уровня для пользователя
     *
     * @param levelId ID уровня
     * @param userId ID пользователя
     * @return список доступных подсказок
     */
    List<LevelHintDTO> getAvailableHintsByLevelId(UUID levelId, UUID userId);

    /**
     * Проверить доступность подсказки
     *
     * @param hintId ID подсказки
     * @param userId ID пользователя
     * @return результат проверки
     */
    HintAvailabilityResult checkHintAvailability(Long hintId, UUID userId);

    /**
     * Копировать подсказки из одного уровня в другой
     *
     * @param sourceLevelId ID исходного уровня
     * @param targetLevelId ID целевого уровня
     */
    void copyHintsForLevel(UUID sourceLevelId, UUID targetLevelId);

    /**
     * Активировать/деактивировать подсказку
     *
     * @param hintId ID подсказки
     * @param active флаг активности
     * @return обновленная подсказка
     */
    LevelHintDTO toggleHintActive(Long hintId, boolean active);

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