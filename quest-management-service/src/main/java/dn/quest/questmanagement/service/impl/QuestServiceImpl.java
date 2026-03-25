package dn.quest.questmanagement.service.impl;

import dn.quest.questmanagement.dto.QuestCreateUpdateDTO;
import dn.quest.questmanagement.dto.QuestDTO;
import dn.quest.questmanagement.dto.QuestSearchRequestDTO;
import dn.quest.questmanagement.entity.Quest;
import dn.quest.questmanagement.entity.QuestStatus;
import dn.quest.questmanagement.repository.QuestRepository;
import dn.quest.questmanagement.service.QuestService;
import dn.quest.questmanagement.service.LevelService;
import dn.quest.questmanagement.exception.QuestNotFoundException;
import dn.quest.questmanagement.exception.QuestAccessDeniedException;
import dn.quest.questmanagement.exception.QuestValidationException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления квестами
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestServiceImpl implements QuestService {

    private final QuestRepository questRepository;
    private final LevelService levelService;

    @Override
    @Transactional
    public QuestDTO createQuest(QuestCreateUpdateDTO dto, Long authorId) {
        log.info("Creating new quest with title: {} for author: {}", dto.getTitle(), authorId);

        // Валидация DTO
        dto.validate();

        // Создание нового квеста
        Quest quest = new Quest();
        quest.setTitle(dto.getTitle());
        quest.setDescription(dto.getDescription());
        quest.setDifficulty(dto.getDifficulty());
        quest.setQuestType(dto.getQuestType());
        quest.setCategory(dto.getCategory());
        quest.setEstimatedDuration(dto.getEstimatedDuration());
        quest.setMaxParticipants(dto.getMaxParticipants());
        quest.setMinParticipants(dto.getMinParticipants());
        quest.setStartLocation(dto.getStartLocation());
        quest.setEndLocation(dto.getEndLocation());
        quest.setRules(dto.getRules());
        quest.setPrizes(dto.getPrizes());
        quest.setRequirements(dto.getRequirements());
        quest.setTags(dto.getTags());
        quest.setIsPublic(dto.getIsPublic());
        quest.setIsTemplate(dto.getIsTemplate());
        quest.setAuthorIds(new HashSet<>(Set.of(authorId)));
        quest.setStatus(QuestStatus.DRAFT);
        quest.setVersion(1);
        quest.setCreatedAt(Instant.now());
        quest.setUpdatedAt(Instant.now());

        // Генерация уникального номера квеста
        quest.setNumber(generateQuestNumber());

        Quest savedQuest = questRepository.save(quest);
        log.info("Quest created successfully with ID: {}", savedQuest.getId());

        return convertToDTO(savedQuest);
    }

    @Override
    @Transactional
    public QuestDTO updateQuest(Long id, QuestCreateUpdateDTO dto, Long userId) {
        log.info("Updating quest with ID: {} by user: {}", id, userId);

        Quest quest = getQuestEntityById(id);
        
        // Проверка прав на редактирование
        if (!canEditQuest(id, userId)) {
            throw new QuestAccessDeniedException("User does not have permission to edit this quest");
        }

        // Валидация DTO
        dto.validate();

        // Обновление полей
        quest.setTitle(dto.getTitle());
        quest.setDescription(dto.getDescription());
        quest.setDifficulty(dto.getDifficulty());
        quest.setQuestType(dto.getQuestType());
        quest.setCategory(dto.getCategory());
        quest.setEstimatedDuration(dto.getEstimatedDuration());
        quest.setMaxParticipants(dto.getMaxParticipants());
        quest.setMinParticipants(dto.getMinParticipants());
        quest.setStartLocation(dto.getStartLocation());
        quest.setEndLocation(dto.getEndLocation());
        quest.setRules(dto.getRules());
        quest.setPrizes(dto.getPrizes());
        quest.setRequirements(dto.getRequirements());
        quest.setTags(dto.getTags());
        quest.setIsPublic(dto.getIsPublic());
        quest.setUpdatedAt(Instant.now());

        // Увеличение версии при изменении опубликованного квеста
        if (quest.getStatus() == QuestStatus.PUBLISHED) {
            quest.setVersion(quest.getVersion() + 1);
        }

        Quest savedQuest = questRepository.save(quest);
        log.info("Quest updated successfully with ID: {}", savedQuest.getId());

        return convertToDTO(savedQuest);
    }

    @Override
    @Transactional
    public void deleteQuest(Long id, Long userId) {
        log.info("Deleting quest with ID: {} by user: {}", id, userId);

        Quest quest = getQuestEntityById(id);
        
        // Проверка прав на удаление
        if (!canEditQuest(id, userId)) {
            throw new QuestAccessDeniedException("User does not have permission to delete this quest");
        }

        // Проверка, что квест не используется в активных сессиях
        if (quest.getStatus() == QuestStatus.ACTIVE) {
            throw new QuestValidationException("Cannot delete active quest");
        }

        questRepository.delete(quest);
        log.info("Quest deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestDTO getQuestById(Long id) {
        Quest quest = getQuestEntityById(id);
        return convertToDTO(quest);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestDTO getQuestByNumber(Long number) {
        Quest quest = questRepository.findByNumber(number)
                .orElseThrow(() -> new QuestNotFoundException("Quest not found with number: " + number));
        return convertToDTO(quest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestDTO> getAllQuests(Pageable pageable) {
        Page<Quest> quests = questRepository.findAll(pageable);
        return quests.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestDTO> getPublishedQuests(Pageable pageable) {
        Page<Quest> quests = questRepository.findByStatus(QuestStatus.PUBLISHED, pageable);
        return quests.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getActiveQuests() {
        List<Quest> quests = questRepository.findByStatus(QuestStatus.ACTIVE);
        return quests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestDTO> searchQuests(QuestSearchRequestDTO searchRequest) {
        Specification<Quest> spec = buildSearchSpecification(searchRequest);
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                Sort.by(Sort.Direction.fromString(searchRequest.getSortDirection()), 
                       searchRequest.getSortBy())
        );
        
        Page<Quest> quests = questRepository.findAll(spec, pageable);
        return quests.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestDTO> getQuestsByAuthor(Long authorId, Pageable pageable) {
        Page<Quest> quests = questRepository.findByAuthorIdsContaining(authorId, pageable);
        return quests.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getQuestsByDifficulty(String difficulty) {
        List<Quest> quests = questRepository.findByDifficulty(difficulty);
        return quests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getQuestsByType(String questType) {
        List<Quest> quests = questRepository.findByQuestType(questType);
        return quests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getQuestsByCategory(String category) {
        List<Quest> quests = questRepository.findByCategory(category);
        return quests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getQuestsByTags(Set<String> tags) {
        List<Quest> quests = questRepository.findByTagsContaining(tags);
        return quests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getQuestTemplates() {
        List<Quest> quests = questRepository.findByIsTemplateTrue();
        return quests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuestDTO publishQuest(Long id, Long userId) {
        log.info("Publishing quest with ID: {} by user: {}", id, userId);

        Quest quest = getQuestEntityById(id);
        
        // Проверка прав на публикацию
        if (!canPublishQuest(id, userId)) {
            throw new QuestAccessDeniedException("User does not have permission to publish this quest");
        }

        // Валидация квеста перед публикацией
        QuestValidationResult validationResult = validateQuestForPublishing(id);
        if (!validationResult.isValid()) {
            throw new QuestValidationException("Quest validation failed: " + 
                    String.join(", ", validationResult.getErrors()));
        }

        quest.setStatus(QuestStatus.PUBLISHED);
        quest.setPublishedAt(Instant.now());
        quest.setUpdatedAt(Instant.now());

        Quest savedQuest = questRepository.save(quest);
        log.info("Quest published successfully with ID: {}", savedQuest.getId());

        return convertToDTO(savedQuest);
    }

    @Override
    @Transactional
    public QuestDTO unpublishQuest(Long id, Long userId) {
        log.info("Unpublishing quest with ID: {} by user: {}", id, userId);

        Quest quest = getQuestEntityById(id);
        
        // Проверка прав на снятие с публикации
        if (!canEditQuest(id, userId)) {
            throw new QuestAccessDeniedException("User does not have permission to unpublish this quest");
        }

        // Проверка, что квест не активен
        if (quest.getStatus() == QuestStatus.ACTIVE) {
            throw new QuestValidationException("Cannot unpublish active quest");
        }

        quest.setStatus(QuestStatus.DRAFT);
        quest.setPublishedAt(null);
        quest.setUpdatedAt(Instant.now());

        Quest savedQuest = questRepository.save(quest);
        log.info("Quest unpublished successfully with ID: {}", savedQuest.getId());

        return convertToDTO(savedQuest);
    }

    @Override
    @Transactional
    public QuestDTO archiveQuest(Long id, String reason, Long userId) {
        log.info("Archiving quest with ID: {} by user: {} with reason: {}", id, userId, reason);

        Quest quest = getQuestEntityById(id);
        
        // Проверка прав на архивацию
        if (!canEditQuest(id, userId)) {
            throw new QuestAccessDeniedException("User does not have permission to archive this quest");
        }

        // Проверка, что квест не активен
        if (quest.getStatus() == QuestStatus.ACTIVE) {
            throw new QuestValidationException("Cannot archive active quest");
        }

        quest.setStatus(QuestStatus.ARCHIVED);
        quest.setArchivedAt(Instant.now());
        quest.setArchiveReason(reason);
        quest.setUpdatedAt(Instant.now());

        Quest savedQuest = questRepository.save(quest);
        log.info("Quest archived successfully with ID: {}", savedQuest.getId());

        return convertToDTO(savedQuest);
    }

    @Override
    @Transactional
    public QuestDTO unarchiveQuest(Long id, Long userId) {
        log.info("Unarchiving quest with ID: {} by user: {}", id, userId);

        Quest quest = getQuestEntityById(id);
        
        // Проверка прав на разархивацию
        if (!canEditQuest(id, userId)) {
            throw new QuestAccessDeniedException("User does not have permission to unarchive this quest");
        }

        quest.setStatus(QuestStatus.DRAFT);
        quest.setArchivedAt(null);
        quest.setArchiveReason(null);
        quest.setUpdatedAt(Instant.now());

        Quest savedQuest = questRepository.save(quest);
        log.info("Quest unarchived successfully with ID: {}", savedQuest.getId());

        return convertToDTO(savedQuest);
    }

    @Override
    @Transactional
    public QuestDTO copyQuest(Long id, String newTitle, Long authorId) {
        log.info("Copying quest with ID: {} with new title: {} for author: {}", id, newTitle, authorId);

        Quest originalQuest = getQuestEntityById(id);
        
        // Создание копии
        Quest copy = new Quest();
        copy.setTitle(newTitle);
        copy.setDescription(originalQuest.getDescription());
        copy.setDifficulty(originalQuest.getDifficulty());
        copy.setQuestType(originalQuest.getQuestType());
        copy.setCategory(originalQuest.getCategory());
        copy.setEstimatedDuration(originalQuest.getEstimatedDuration());
        copy.setMaxParticipants(originalQuest.getMaxParticipants());
        copy.setMinParticipants(originalQuest.getMinParticipants());
        copy.setStartLocation(originalQuest.getStartLocation());
        copy.setEndLocation(originalQuest.getEndLocation());
        copy.setRules(originalQuest.getRules());
        copy.setPrizes(originalQuest.getPrizes());
        copy.setRequirements(originalQuest.getRequirements());
        copy.setTags(originalQuest.getTags());
        copy.setIsPublic(false); // Копия по умолчанию не публичная
        copy.setIsTemplate(false);
        copy.setAuthorIds(new HashSet<>(Set.of(authorId)));
        copy.setStatus(QuestStatus.DRAFT);
        copy.setVersion(1);
        copy.setCreatedAt(Instant.now());
        copy.setUpdatedAt(Instant.now());
        copy.setNumber(generateQuestNumber());

        Quest savedCopy = questRepository.save(copy);
        
        // Копирование уровней
        levelService.copyLevelsForQuest(id, savedCopy.getId());
        
        log.info("Quest copied successfully with new ID: {}", savedCopy.getId());

        return convertToDTO(savedCopy);
    }

    @Override
    @Transactional
    public QuestDTO createTemplateFromQuest(Long id, String templateName, Long userId) {
        log.info("Creating template from quest with ID: {} with name: {} by user: {}", id, templateName, userId);

        Quest originalQuest = getQuestEntityById(id);
        
        // Проверка прав
        if (!canEditQuest(id, userId)) {
            throw new QuestAccessDeniedException("User does not have permission to create template from this quest");
        }

        // Создание шаблона
        Quest template = new Quest();
        template.setTitle(templateName);
        template.setDescription(originalQuest.getDescription());
        template.setDifficulty(originalQuest.getDifficulty());
        template.setQuestType(originalQuest.getQuestType());
        template.setCategory(originalQuest.getCategory());
        template.setEstimatedDuration(originalQuest.getEstimatedDuration());
        template.setMaxParticipants(originalQuest.getMaxParticipants());
        template.setMinParticipants(originalQuest.getMinParticipants());
        template.setStartLocation(originalQuest.getStartLocation());
        template.setEndLocation(originalQuest.getEndLocation());
        template.setRules(originalQuest.getRules());
        template.setPrizes(originalQuest.getPrizes());
        template.setRequirements(originalQuest.getRequirements());
        template.setTags(originalQuest.getTags());
        template.setIsPublic(false);
        template.setIsTemplate(true);
        template.setAuthorIds(new HashSet<>(Set.of(userId)));
        template.setStatus(QuestStatus.DRAFT);
        template.setVersion(1);
        template.setCreatedAt(Instant.now());
        template.setUpdatedAt(Instant.now());
        template.setNumber(generateQuestNumber());

        Quest savedTemplate = questRepository.save(template);
        
        // Копирование уровней
        levelService.copyLevelsForQuest(id, savedTemplate.getId());
        
        log.info("Template created successfully with ID: {}", savedTemplate.getId());

        return convertToDTO(savedTemplate);
    }

    @Override
    @Transactional
    public QuestDTO createQuestFromTemplate(Long templateId, String title, Long authorId) {
        log.info("Creating quest from template with ID: {} with title: {} for author: {}", templateId, title, authorId);

        Quest template = getQuestEntityById(templateId);
        
        // Проверка, что это шаблон
        if (!template.getIsTemplate()) {
            throw new QuestValidationException("Specified quest is not a template");
        }

        // Создание квеста из шаблона
        Quest quest = new Quest();
        quest.setTitle(title);
        quest.setDescription(template.getDescription());
        quest.setDifficulty(template.getDifficulty());
        quest.setQuestType(template.getQuestType());
        quest.setCategory(template.getCategory());
        quest.setEstimatedDuration(template.getEstimatedDuration());
        quest.setMaxParticipants(template.getMaxParticipants());
        quest.setMinParticipants(template.getMinParticipants());
        quest.setStartLocation(template.getStartLocation());
        quest.setEndLocation(template.getEndLocation());
        quest.setRules(template.getRules());
        quest.setPrizes(template.getPrizes());
        quest.setRequirements(template.getRequirements());
        quest.setTags(template.getTags());
        quest.setIsPublic(false);
        quest.setIsTemplate(false);
        quest.setAuthorIds(new HashSet<>(Set.of(authorId)));
        quest.setStatus(QuestStatus.DRAFT);
        quest.setVersion(1);
        quest.setCreatedAt(Instant.now());
        quest.setUpdatedAt(Instant.now());
        quest.setNumber(generateQuestNumber());

        Quest savedQuest = questRepository.save(quest);
        
        // Копирование уровней
        levelService.copyLevelsForQuest(templateId, savedQuest.getId());
        
        log.info("Quest created from template successfully with ID: {}", savedQuest.getId());

        return convertToDTO(savedQuest);
    }

    @Override
    @Transactional
    public QuestDTO changeQuestStatus(Long id, QuestStatus status, Long userId) {
        log.info("Changing status of quest with ID: {} to {} by user: {}", id, status, userId);

        Quest quest = getQuestEntityById(id);
        
        // Проверка прав
        if (!canEditQuest(id, userId)) {
            throw new QuestAccessDeniedException("User does not have permission to change status of this quest");
        }

        // Валидация перехода статуса
        validateStatusTransition(quest.getStatus(), status);

        quest.setStatus(status);
        quest.setUpdatedAt(Instant.now());

        if (status == QuestStatus.PUBLISHED && quest.getPublishedAt() == null) {
            quest.setPublishedAt(Instant.now());
        } else if (status == QuestStatus.ARCHIVED) {
            quest.setArchivedAt(Instant.now());
        }

        Quest savedQuest = questRepository.save(quest);
        log.info("Quest status changed successfully with ID: {}", savedQuest.getId());

        return convertToDTO(savedQuest);
    }

    @Override
    @Transactional
    public QuestDTO addQuestAuthor(Long questId, Long authorId, Long userId) {
        log.info("Adding author {} to quest {} by user: {}", authorId, questId, userId);

        Quest quest = getQuestEntityById(questId);
        
        // Проверка прав
        if (!canEditQuest(questId, userId)) {
            throw new QuestAccessDeniedException("User does not have permission to add authors to this quest");
        }

        quest.getAuthorIds().add(authorId);
        quest.setUpdatedAt(Instant.now());

        Quest savedQuest = questRepository.save(quest);
        log.info("Author added successfully to quest with ID: {}", savedQuest.getId());

        return convertToDTO(savedQuest);
    }

    @Override
    @Transactional
    public QuestDTO removeQuestAuthor(Long questId, Long authorId, Long userId) {
        log.info("Removing author {} from quest {} by user: {}", authorId, questId, userId);

        Quest quest = getQuestEntityById(questId);
        
        // Проверка прав
        if (!canEditQuest(questId, userId)) {
            throw new QuestAccessDeniedException("User does not have permission to remove authors from this quest");
        }

        // Проверка, что останется хотя бы один автор
        if (quest.getAuthorIds().size() <= 1) {
            throw new QuestValidationException("Quest must have at least one author");
        }

        quest.getAuthorIds().remove(authorId);
        quest.setUpdatedAt(Instant.now());

        Quest savedQuest = questRepository.save(quest);
        log.info("Author removed successfully from quest with ID: {}", savedQuest.getId());

        return convertToDTO(savedQuest);
    }

    @Override
    @Transactional
    public QuestDTO addQuestTags(Long questId, Set<String> tags, Long userId) {
        log.info("Adding tags {} to quest {} by user: {}", tags, questId, userId);

        Quest quest = getQuestEntityById(questId);
        
        // Проверка прав
        if (!canEditQuest(questId, userId)) {
            throw new QuestAccessDeniedException("User does not have permission to add tags to this quest");
        }

        if (quest.getTags() == null) {
            quest.setTags(new HashSet<>());
        }
        quest.getTags().addAll(tags);
        quest.setUpdatedAt(Instant.now());

        Quest savedQuest = questRepository.save(quest);
        log.info("Tags added successfully to quest with ID: {}", savedQuest.getId());

        return convertToDTO(savedQuest);
    }

    @Override
    @Transactional
    public QuestDTO removeQuestTags(Long questId, Set<String> tags, Long userId) {
        log.info("Removing tags {} from quest {} by user: {}", tags, questId, userId);

        Quest quest = getQuestEntityById(questId);
        
        // Проверка прав
        if (!canEditQuest(questId, userId)) {
            throw new QuestAccessDeniedException("User does not have permission to remove tags from this quest");
        }

        if (quest.getTags() != null) {
            quest.getTags().removeAll(tags);
            quest.setUpdatedAt(Instant.now());
        }

        Quest savedQuest = questRepository.save(quest);
        log.info("Tags removed successfully from quest with ID: {}", savedQuest.getId());

        return convertToDTO(savedQuest);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasQuestAccess(Long questId, Long userId) {
        Quest quest = getQuestEntityById(questId);
        
        // Публичные квесты доступны всем
        if (quest.getIsPublic()) {
            return true;
        }
        
        // Авторам доступ разрешен
        return quest.getAuthorIds().contains(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEditQuest(Long questId, Long userId) {
        Quest quest = getQuestEntityById(questId);
        
        // Только авторы могут редактировать
        return quest.getAuthorIds().contains(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canPublishQuest(Long questId, Long userId) {
        Quest quest = getQuestEntityById(questId);
        
        // Только авторы могут публиковать
        if (!quest.getAuthorIds().contains(userId)) {
            return false;
        }
        
        // Проверка, что квест готов к публикации
        QuestValidationResult validationResult = validateQuestForPublishing(questId);
        return validationResult.isValid();
    }

    @Override
    @Transactional(readOnly = true)
    public QuestValidationResult validateQuestForPublishing(Long questId) {
        Quest quest = getQuestEntityById(questId);
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Проверка обязательных полей
        if (quest.getTitle() == null || quest.getTitle().trim().isEmpty()) {
            errors.add("Title is required");
        }
        
        if (quest.getDescription() == null || quest.getDescription().trim().isEmpty()) {
            errors.add("Description is required");
        }
        
        if (quest.getDifficulty() == null) {
            errors.add("Difficulty is required");
        }
        
        if (quest.getQuestType() == null) {
            errors.add("Quest type is required");
        }

        // Проверка уровней
        var levels = levelService.getLevelsByQuestId(questId);
        if (levels.isEmpty()) {
            errors.add("Quest must have at least one level");
        } else {
            // Проверка последовательности уровней
            for (int i = 0; i < levels.size(); i++) {
                var level = levels.get(i);
                if (level.getOrder() != i + 1) {
                    warnings.add("Level order is not sequential");
                    break;
                }
            }
            
            // Проверка кодов для каждого уровня
            for (var level : levels) {
                var codes = levelService.getCodesByLevelId(level.getId());
                if (codes.isEmpty()) {
                    errors.add("Level " + level.getOrder() + " must have at least one code");
                }
            }
        }

        // Проверка участников
        if (quest.getMaxParticipants() != null && quest.getMinParticipants() != null) {
            if (quest.getMaxParticipants() < quest.getMinParticipants()) {
                errors.add("Max participants cannot be less than min participants");
            }
        }

        // Проверка времени
        if (quest.getStartTime() != null && quest.getEndTime() != null) {
            if (quest.getStartTime().isAfter(quest.getEndTime())) {
                errors.add("Start time cannot be after end time");
            }
        }

        // Предупреждения
        if (quest.getTags() == null || quest.getTags().isEmpty()) {
            warnings.add("Quest has no tags for better discoverability");
        }
        
        if (quest.getEstimatedDuration() == null) {
            warnings.add("Estimated duration is not specified");
        }

        return new QuestValidationResult(errors.isEmpty(), errors, warnings);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestStatistics getQuestStatistics() {
        long totalQuests = questRepository.count();
        long publishedQuests = questRepository.countByStatus(QuestStatus.PUBLISHED);
        long activeQuests = questRepository.countByStatus(QuestStatus.ACTIVE);
        long archivedQuests = questRepository.countByStatus(QuestStatus.ARCHIVED);
        long templateQuests = questRepository.countByIsTemplateTrue();

        return new QuestStatistics(totalQuests, publishedQuests, activeQuests, 
                                 archivedQuests, templateQuests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getPopularQuests(int limit) {
        List<Quest> quests = questRepository.findPopularQuests(PageRequest.of(0, limit));
        return quests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getLatestQuests(int limit) {
        List<Quest> quests = questRepository.findLatestQuests(PageRequest.of(0, limit));
        return quests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getExpiredQuests() {
        LocalDateTime now = LocalDateTime.now();
        List<Quest> quests = questRepository.findExpiredQuests(now);
        return quests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getUpcomingQuests(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusHours(hours);
        List<Quest> quests = questRepository.findUpcomingQuests(now, future);
        return quests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Вспомогательные методы

    private Quest getQuestEntityById(Long id) {
        return questRepository.findById(id)
                .orElseThrow(() -> new QuestNotFoundException("Quest not found with ID: " + id));
    }

    private Long generateQuestNumber() {
        Long maxNumber = questRepository.findMaxNumber();
        return maxNumber != null ? maxNumber + 1 : 1L;
    }

    private void validateStatusTransition(QuestStatus currentStatus, QuestStatus newStatus) {
        // Валидация переходов между статусами
        switch (currentStatus) {
            case DRAFT:
                if (newStatus != QuestStatus.PUBLISHED && newStatus != QuestStatus.ARCHIVED) {
                    throw new QuestValidationException("Invalid status transition from DRAFT to " + newStatus);
                }
                break;
            case PUBLISHED:
                if (newStatus != QuestStatus.ACTIVE && newStatus != QuestStatus.DRAFT && newStatus != QuestStatus.ARCHIVED) {
                    throw new QuestValidationException("Invalid status transition from PUBLISHED to " + newStatus);
                }
                break;
            case ACTIVE:
                if (newStatus != QuestStatus.PUBLISHED && newStatus != QuestStatus.ARCHIVED) {
                    throw new QuestValidationException("Invalid status transition from ACTIVE to " + newStatus);
                }
                break;
            case ARCHIVED:
                if (newStatus != QuestStatus.DRAFT) {
                    throw new QuestValidationException("Invalid status transition from ARCHIVED to " + newStatus);
                }
                break;
        }
    }

    private Specification<Quest> buildSearchSpecification(QuestSearchRequestDTO searchRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Поиск по названию
            if (searchRequest.getTitle() != null && !searchRequest.getTitle().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + searchRequest.getTitle().toLowerCase() + "%"
                ));
            }

            // Поиск по описанию
            if (searchRequest.getDescription() != null && !searchRequest.getDescription().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        "%" + searchRequest.getDescription().toLowerCase() + "%"
                ));
            }

            // Фильтр по статусу
            if (searchRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchRequest.getStatus()));
            }

            // Фильтр по сложности
            if (searchRequest.getDifficulty() != null) {
                predicates.add(criteriaBuilder.equal(root.get("difficulty"), searchRequest.getDifficulty()));
            }

            // Фильтр по типу
            if (searchRequest.getQuestType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("questType"), searchRequest.getQuestType()));
            }

            // Фильтр по категории
            if (searchRequest.getCategory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), searchRequest.getCategory()));
            }

            // Фильтр по автору
            if (searchRequest.getAuthorId() != null) {
                predicates.add(criteriaBuilder.isMember(searchRequest.getAuthorId(), root.get("authorIds")));
            }

            // Фильтр по тегам
            if (searchRequest.getTags() != null && !searchRequest.getTags().isEmpty()) {
                for (String tag : searchRequest.getTags()) {
                    predicates.add(criteriaBuilder.isMember(tag, root.get("tags")));
                }
            }

            // Фильтр по публичности
            if (searchRequest.getIsPublic() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPublic"), searchRequest.getIsPublic()));
            }

            // Фильтр по шаблонам
            if (searchRequest.getIsTemplate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isTemplate"), searchRequest.getIsTemplate()));
            }

            // Фильтр по дате создания
            if (searchRequest.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), searchRequest.getCreatedFrom()));
            }
            if (searchRequest.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), searchRequest.getCreatedTo()));
            }

            // Фильтр по дате публикации
            if (searchRequest.getPublishedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("publishedAt"), searchRequest.getPublishedFrom()));
            }
            if (searchRequest.getPublishedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("publishedAt"), searchRequest.getPublishedTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
    }

    private QuestDTO convertToDTO(Quest quest) {
        QuestDTO dto = new QuestDTO();
        dto.setId(quest.getId());
        dto.setNumber(quest.getNumber());
        dto.setTitle(quest.getTitle());
        dto.setDescriptionHtml(quest.getDescriptionHtml());
        dto.setDifficulty(quest.getDifficulty());
        dto.setQuestType(quest.getQuestType());
        dto.setCategory(quest.getCategory());
        dto.setEstimatedDurationMinutes(quest.getEstimatedDurationMinutes());
        dto.setMaxParticipants(quest.getMaxParticipants());
        dto.setMinParticipants(quest.getMinParticipants());
        dto.setStartLocation(quest.getStartLocation());
        dto.setEndLocation(quest.getEndLocation());
        dto.setRules(quest.getRules());
        dto.setPrizes(quest.getPrizes());
        dto.setRequirements(quest.getRequirements());
        dto.setTags(quest.getTags());
        dto.setIsPublic(quest.getIsPublic());
        dto.setIsTemplate(quest.getIsTemplate());
        dto.setAuthorIds(quest.getAuthorIds());
        dto.setStatus(quest.getStatus());
        dto.setVersion(quest.getVersion());
        dto.setStartTime(quest.getStartTime());
        dto.setEndTime(quest.getEndTime());
        dto.setCreatedAt(quest.getCreatedAt());
        dto.setUpdatedAt(quest.getUpdatedAt());
        dto.setPublishedAt(quest.getPublishedAt());
        dto.setArchivedAt(quest.getArchivedAt());
        dto.setArchiveReason(quest.getArchiveReason());
        
        return dto;
    }
}