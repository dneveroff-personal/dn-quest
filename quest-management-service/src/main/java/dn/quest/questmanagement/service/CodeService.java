package dn.quest.questmanagement.service;

import dn.quest.questmanagement.dto.CodeDTO;
import dn.quest.shared.enums.CodeType;

import java.util.List;

/**
 * Сервис для управления кодами уровней
 */
public interface CodeService {

    /**
     * Создать новый код
     *
     * @param dto DTO для создания кода
     * @param levelId ID уровня
     * @return созданный код
     */
    CodeDTO createCode(CodeDTO dto, Long levelId);

    /**
     * Обновить существующий код
     *
     * @param id ID кода
     * @param dto DTO для обновления кода
     * @return обновленный код
     */
    CodeDTO updateCode(Long id, CodeDTO dto);

    /**
     * Удалить код
     *
     * @param id ID кода
     */
    void deleteCode(Long id);

    /**
     * Получить код по ID
     *
     * @param id ID кода
     * @return код
     */
    CodeDTO getCodeById(Long id);

    /**
     * Получить все коды уровня
     *
     * @param levelId ID уровня
     * @return список кодов
     */
    List<CodeDTO> getCodesByLevelId(Long levelId);

    /**
     * Получить активные коды уровня
     *
     * @param levelId ID уровня
     * @return список активных кодов
     */
    List<CodeDTO> getActiveCodesByLevelId(Long levelId);

    /**
     * Получить коды по типу
     *
     * @param levelId ID уровня
     * @param type тип кода
     * @return список кодов
     */
    List<CodeDTO> getCodesByType(Long levelId, CodeType type);

    /**
     * Проверить код
     *
     * @param levelId ID уровня
     * @param codeValue значение кода
     * @return результат проверки
     */
    CodeValidationResult validateCode(Long levelId, String codeValue);

    /**
     * Использовать код
     *
     * @param codeId ID кода
     * @param userId ID пользователя
     * @return результат использования
     */
    CodeUsageResult useCode(Long codeId, Long userId);

    /**
     * Получить неиспользованные коды уровня
     *
     * @param levelId ID уровня
     * @param userId ID пользователя
     * @return список неиспользованных кодов
     */
    List<CodeDTO> getUnusedCodesByLevelId(Long levelId, Long userId);

    /**
     * Получить использованные коды уровня
     *
     * @param levelId ID уровня
     * @param userId ID пользователя
     * @return список использованных кодов
     */
    List<CodeDTO> getUsedCodesByLevelId(Long levelId, Long userId);

    /**
     * Проверить уникальность кода в рамках квеста
     *
     * @param questId ID квеста
     * @param codeValue значение кода
     * @param excludeCodeId ID кода для исключения из проверки (при обновлении)
     * @return true если код уникален
     */
    boolean isCodeUniqueInQuest(Long questId, String codeValue, Long excludeCodeId);

    /**
     * Копировать коды из одного уровня в другой
     *
     * @param sourceLevelId ID исходного уровня
     * @param targetLevelId ID целевого уровня
     */
    void copyCodesForLevel(Long sourceLevelId, Long targetLevelId);

    /**
     * Получить статистику использования кодов уровня
     *
     * @param levelId ID уровня
     * @return статистика
     */
    CodeUsageStatistics getCodeUsageStatistics(Long levelId);

    /**
     * Сбросить использование кодов уровня
     *
     * @param levelId ID уровня
     */
    void resetCodeUsage(Long levelId);

    /**
     * Активировать/деактивировать код
     *
     * @param codeId ID кода
     * @param active флаг активности
     * @return обновленный код
     */
    CodeDTO toggleCodeActive(Long codeId, boolean active);

    /**
     * Результат проверки кода
     */
    class CodeValidationResult {
        private final boolean valid;
        private final String message;
        private final Long codeId;

        public CodeValidationResult(boolean valid, String message, Long codeId) {
            this.valid = valid;
            this.message = message;
            this.codeId = codeId;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public Long getCodeId() {
            return codeId;
        }
    }

    /**
     * Результат использования кода
     */
    class CodeUsageResult {
        private final boolean success;
        private final String message;
        private final boolean wasAlreadyUsed;

        public CodeUsageResult(boolean success, String message, boolean wasAlreadyUsed) {
            this.success = success;
            this.message = message;
            this.wasAlreadyUsed = wasAlreadyUsed;
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
    }

    /**
     * Статистика использования кодов
     */
    class CodeUsageStatistics {
        private final long totalCodes;
        private final long activeCodes;
        private final long usedCodes;
        private final long unusedCodes;
        private final long uniqueUsers;

        public CodeUsageStatistics(long totalCodes, long activeCodes, long usedCodes, 
                                  long unusedCodes, long uniqueUsers) {
            this.totalCodes = totalCodes;
            this.activeCodes = activeCodes;
            this.usedCodes = usedCodes;
            this.unusedCodes = unusedCodes;
            this.uniqueUsers = uniqueUsers;
        }

        public long getTotalCodes() {
            return totalCodes;
        }

        public long getActiveCodes() {
            return activeCodes;
        }

        public long getUsedCodes() {
            return usedCodes;
        }

        public long getUnusedCodes() {
            return unusedCodes;
        }

        public long getUniqueUsers() {
            return uniqueUsers;
        }
    }
}