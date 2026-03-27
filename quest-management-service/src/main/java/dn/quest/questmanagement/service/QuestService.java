package dn.quest.questmanagement.service;

import dn.quest.questmanagement.dto.QuestCreateUpdateDTO;
import dn.quest.questmanagement.dto.QuestDTO;
import dn.quest.questmanagement.dto.QuestSearchRequestDTO;
import dn.quest.questmanagement.entity.Quest;
import dn.quest.questmanagement.entity.QuestStatus;
import dn.quest.shared.enums.Difficulty;
import dn.quest.shared.enums.QuestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Сервис для управления квестами
 */
public interface QuestService {

    /**
     * Создать новый квест
     *
     * @param dto DTO для создания квеста
     * @param authorId ID автора квеста
     * @return созданный квест
     */
    QuestDTO createQuest(QuestCreateUpdateDTO dto, Long authorId);

    /**
     * Обновить существующий квест
     *
     * @param id ID квеста
     * @param dto DTO для обновления квеста
     * @param userId ID пользователя, выполняющего обновление
     * @return обновленный квест
     */
    QuestDTO updateQuest(Long id, QuestCreateUpdateDTO dto, Long userId);

    /**
     * Удалить квест
     *
     * @param id ID квеста
     * @param userId ID пользователя, выполняющего удаление
     */
    void deleteQuest(Long id, Long userId);

    /**
     * Получить квест по ID
     *
     * @param id ID квеста
     * @return квест
     */
    QuestDTO getQuestById(Long id);

    /**
     * Получить квест по номеру
     *
     * @param number номер квеста
     * @return квест
     */
    QuestDTO getQuestByNumber(Long number);

    /**
     * Получить все квесты с пагинацией
     *
     * @param pageable параметры пагинации
     * @return страница квестов
     */
    Page<QuestDTO> getAllQuests(Pageable pageable);

    /**
     * Получить опубликованные квесты с пагинацией
     *
     * @param pageable параметры пагинации
     * @return страница опубликованных квестов
     */
    Page<QuestDTO> getPublishedQuests(Pageable pageable);

    /**
     * Получить активные квесты
     *
     * @return список активных квестов
     */
    List<QuestDTO> getActiveQuests();

    /**
     * Поиск квестов по параметрам
     *
     * @param searchRequest параметры поиска
     * @return страница найденных квестов
     */
    Page<QuestDTO> searchQuests(QuestSearchRequestDTO searchRequest);

    /**
     * Получить квесты автора
     *
     * @param authorId ID автора
     * @param pageable параметры пагинации
     * @return страница квестов автора
     */
    Page<QuestDTO> getQuestsByAuthor(Long authorId, Pageable pageable);

    /**
     * Получить квесты по сложности
     *
     * @param difficulty сложность
     * @return список квестов
     */
    List<QuestDTO> getQuestsByDifficulty(Difficulty difficulty);

    /**
     * Получить квесты по типу
     *
     * @param questType тип квеста
     * @return список квестов
     */
    List<QuestDTO> getQuestsByType(QuestType questType);

    /**
     * Получить квесты по категории
     *
     * @param category категория
     * @return список квестов
     */
    List<QuestDTO> getQuestsByCategory(String category);

    /**
     * Получить квесты по тегам
     *
     * @param tags набор тегов
     * @return список квестов
     */
    List<QuestDTO> getQuestsByTags(Set<String> tags);

    /**
     * Получить шаблоны квестов
     *
     * @return список шаблонов
     */
    List<QuestDTO> getQuestTemplates();

    /**
     * Опубликовать квест
     *
     * @param id ID квеста
     * @param userId ID пользователя
     * @return опубликованный квест
     */
    QuestDTO publishQuest(Long id, Long userId);

    /**
     * Снять квест с публикации
     *
     * @param id ID квеста
     * @param userId ID пользователя
     * @return квест снятый с публикации
     */
    QuestDTO unpublishQuest(Long id, Long userId);

    /**
     * Архивировать квест
     *
     * @param id ID квеста
     * @param reason причина архивации
     * @param userId ID пользователя
     * @return архивированный квест
     */
    QuestDTO archiveQuest(Long id, String reason, Long userId);

    /**
     * Разархивировать квест
     *
     * @param id ID квеста
     * @param userId ID пользователя
     * @return разархивированный квест
     */
    QuestDTO unarchiveQuest(Long id, Long userId);

    /**
     * Копировать квест
     *
     * @param id ID исходного квеста
     * @param newTitle новое название
     * @param authorId ID автора копии
     * @return скопированный квест
     */
    QuestDTO copyQuest(Long id, String newTitle, Long authorId);

    /**
     * Создать шаблон из квеста
     *
     * @param id ID квеста
     * @param templateName название шаблона
     * @param userId ID пользователя
     * @return созданный шаблон
     */
    QuestDTO createTemplateFromQuest(Long id, String templateName, Long userId);

    /**
     * Создать квест из шаблона
     *
     * @param templateId ID шаблона
     * @param title название нового квеста
     * @param authorId ID автора
     * @return созданный квест
     */
    QuestDTO createQuestFromTemplate(Long templateId, String title, Long authorId);

    /**
     * Изменить статус квеста
     *
     * @param id ID квеста
     * @param status новый статус
     * @param userId ID пользователя
     * @return квест с измененным статусом
     */
    QuestDTO changeQuestStatus(Long id, QuestStatus status, Long userId);

    /**
     * Добавить автора квесту
     *
     * @param questId ID квеста
     * @param authorId ID автора для добавления
     * @param userId ID пользователя, выполняющего операцию
     * @return обновленный квест
     */
    QuestDTO addQuestAuthor(Long questId, Long authorId, Long userId);

    /**
     * Удалить автора квеста
     *
     * @param questId ID квеста
     * @param authorId ID автора для удаления
     * @param userId ID пользователя, выполняющего операцию
     * @return обновленный квест
     */
    QuestDTO removeQuestAuthor(Long questId, Long authorId, Long userId);

    /**
     * Добавить теги квесту
     *
     * @param questId ID квеста
     * @param tags набор тегов для добавления
     * @param userId ID пользователя
     * @return обновленный квест
     */
    QuestDTO addQuestTags(Long questId, Set<String> tags, Long userId);

    /**
     * Удалить теги квеста
     *
     * @param questId ID квеста
     * @param tags набор тегов для удаления
     * @param userId ID пользователя
     * @return обновленный квест
     */
    QuestDTO removeQuestTags(Long questId, Set<String> tags, Long userId);

    /**
     * Проверить права доступа к квесту
     *
     * @param questId ID квеста
     * @param userId ID пользователя
     * @return true если есть доступ
     */
    boolean hasQuestAccess(Long questId, Long userId);

    /**
     * Проверить права на редактирование квеста
     *
     * @param questId ID квеста
     * @param userId ID пользователя
     * @return true если можно редактировать
     */
    boolean canEditQuest(Long questId, Long userId);

    /**
     * Проверить права на публикацию квеста
     *
     * @param questId ID квеста
     * @param userId ID пользователя
     * @return true если можно публиковать
     */
    boolean canPublishQuest(Long questId, Long userId);

    /**
     * Валидировать квест перед публикацией
     *
     * @param questId ID квеста
     * @return результат валидации
     */
    QuestValidationResult validateQuestForPublishing(Long questId);

    /**
     * Получить статистику по квестам
     *
     * @return статистика
     */
    QuestStatistics getQuestStatistics();

    /**
     * Получить популярные квесты
     *
     * @param limit ограничение количества
     * @return список популярных квестов
     */
    List<QuestDTO> getPopularQuests(int limit);

    /**
     * Получить последние квесты
     *
     * @param limit ограничение количества
     * @return список последних квестов
     */
    List<QuestDTO> getLatestQuests(int limit);

    /**
     * Получить просроченные квесты
     *
     * @return список просроченных квестов
     */
    List<QuestDTO> getExpiredQuests();

    /**
     * Получить квесты, которые скоро начнутся
     *
     * @param hours количество часов вперед
     * @return список квестов
     */
    List<QuestDTO> getUpcomingQuests(int hours);

    /**
     * Результат валидации квеста
     */
    class QuestValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        public QuestValidationResult(boolean valid, List<String> errors, List<String> warnings) {
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

    /**
     * Статистика по квестам
     */
    class QuestStatistics {
        private final long totalQuests;
        private final long publishedQuests;
        private final long activeQuests;
        private final long archivedQuests;
        private final long templateQuests;

        public QuestStatistics(long totalQuests, long publishedQuests, long activeQuests, 
                             long archivedQuests, long templateQuests) {
            this.totalQuests = totalQuests;
            this.publishedQuests = publishedQuests;
            this.activeQuests = activeQuests;
            this.archivedQuests = archivedQuests;
            this.templateQuests = templateQuests;
        }

        public long getTotalQuests() {
            return totalQuests;
        }

        public long getPublishedQuests() {
            return publishedQuests;
        }

        public long getActiveQuests() {
            return activeQuests;
        }

        public long getArchivedQuests() {
            return archivedQuests;
        }

        public long getTemplateQuests() {
            return templateQuests;
        }
    }
}