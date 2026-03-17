package dn.quest.questmanagement.service;

import dn.quest.questmanagement.dto.QuestDTO;
import dn.quest.questmanagement.dto.QuestSearchRequestDTO;
import dn.quest.questmanagement.repository.QuestRepository;
import dn.quest.questmanagement.entity.Quest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для поиска квестов
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestSearchService {

    private final QuestRepository questRepository;
    private final QuestService questService;

    /**
     * Расширенный поиск квестов
     */
    public Page<QuestDTO> searchQuests(QuestSearchRequestDTO searchRequest) {
        log.info("Searching quests with criteria: {}", searchRequest);

        Specification<Quest> spec = buildAdvancedSearchSpecification(searchRequest);
        Pageable pageable = createPageable(searchRequest);
        
        Page<Quest> questPage = questRepository.findAll(spec, pageable);
        
        log.info("Found {} quests matching search criteria", questPage.getTotalElements());
        
        return questPage.map(this::convertToDTO);
    }

    /**
     * Полнотекстовый поиск квестов
     */
    public Page<QuestDTO> fullTextSearch(String query, Pageable pageable) {
        log.info("Performing full-text search for query: {}", query);

        Specification<Quest> spec = (root, queryBuilder, criteriaBuilder) -> {
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            String searchPattern = "%" + query.toLowerCase() + "%";
            
            // Поиск в названии
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), searchPattern));
            
            // Поиск в описании
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchPattern));
            
            // Поиск в правилах
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("rules")), searchPattern));
            
            // Поиск в требованиях
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("requirements")), searchPattern));
            
            // Поиск в призах
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("prizes")), searchPattern));
            
            // Поиск в тегах
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.function("array_to_string", String.class, 
                            root.get("tags"), criteriaBuilder.literal(" ")), searchPattern));
            
            return criteriaBuilder.or(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
        
        Page<Quest> questPage = questRepository.findAll(spec, pageable);
        
        log.info("Full-text search found {} quests for query: {}", questPage.getTotalElements(), query);
        
        return questPage.map(this::convertToDTO);
    }

    /**
     * Поиск похожих квестов
     */
    public List<QuestDTO> findSimilarQuests(Long questId, int limit) {
        log.info("Finding similar quests for quest: {}", questId);

        try {
            QuestDTO sourceQuest = questService.getQuestById(questId);
            
            Specification<Quest> spec = (root, queryBuilder, criteriaBuilder) -> {
                List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();
                
                // Исключаем исходный квест
                predicates.add(criteriaBuilder.notEqual(root.get("id"), questId));
                
                // Только опубликованные квесты
                predicates.add(criteriaBuilder.equal(root.get("status"), "PUBLISHED"));
                predicates.add(criteriaBuilder.equal(root.get("isPublic"), true));
                
                // Похожая сложность
                if (sourceQuest.getDifficulty() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("difficulty"), sourceQuest.getDifficulty()));
                }
                
                // Похожий тип
                if (sourceQuest.getQuestType() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("questType"), sourceQuest.getQuestType()));
                }
                
                // Похожая категория
                if (sourceQuest.getCategory() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("category"), sourceQuest.getCategory()));
                }
                
                // Общие теги
                if (sourceQuest.getTags() != null && !sourceQuest.getTags().isEmpty()) {
                    for (String tag : sourceQuest.getTags()) {
                        predicates.add(criteriaBuilder.isMember(tag, root.get("tags")));
                    }
                }
                
                return criteriaBuilder.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
            };
            
            Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
            Page<Quest> similarQuests = questRepository.findAll(spec, pageable);
            
            log.info("Found {} similar quests for quest: {}", similarQuests.getContent().size(), questId);
            
            return similarQuests.getContent().stream()
                    .map(this::convertToDTO)
                    .collect(java.util.stream.Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error finding similar quests for quest: {}", questId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Поиск квестов по геолокации
     */
    public List<QuestDTO> findQuestsByLocation(Double latitude, Double longitude, Double radiusKm) {
        log.info("Finding quests near location: {}, {} within radius: {}km", latitude, longitude, radiusKm);

        // В реальном приложении здесь был бы геопространственный запрос
        // Для примера используем упрощенный подход
        
        Specification<Quest> spec = (root, queryBuilder, criteriaBuilder) -> {
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            // Только опубликованные квесты
            predicates.add(criteriaBuilder.equal(root.get("status"), "PUBLISHED"));
            predicates.add(criteriaBuilder.equal(root.get("isPublic"), true));
            
            // Квесты с указанной геолокацией
            predicates.add(criteriaBuilder.isNotNull(root.get("startLocation")));
            
            return criteriaBuilder.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
        
        List<Quest> quests = questRepository.findAll(spec);
        
        // Фильтрация по расстоянию (упрощенная логика)
        List<QuestDTO> result = quests.stream()
                .map(this::convertToDTO)
                .filter(quest -> isWithinRadius(quest, latitude, longitude, radiusKm))
                .collect(java.util.stream.Collectors.toList());
        
        log.info("Found {} quests within radius: {}km", result.size(), radiusKm);
        
        return result;
    }

    /**
     * Поиск популярных квестов
     */
    public List<QuestDTO> findPopularQuests(int limit) {
        log.info("Finding popular quests with limit: {}", limit);

        Specification<Quest> spec = (root, queryBuilder, criteriaBuilder) -> {
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            // Только опубликованные квесты
            predicates.add(criteriaBuilder.equal(root.get("status"), "PUBLISHED"));
            predicates.add(criteriaBuilder.equal(root.get("isPublic"), true));
            
            // Квесты с высоким рейтингом (если есть поле рейтинга)
            // predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), 4.0));
            
            return criteriaBuilder.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
        
        org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(0, limit);
        
        Page<Quest> popularQuests = questRepository.findAll(spec, pageable);
        
        return popularQuests.getContent().stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Поиск рекомендуемых квестов для пользователя
     */
    public List<QuestDTO> findRecommendedQuests(Long userId, int limit) {
        log.info("Finding recommended quests for user: {} with limit: {}", userId, limit);

        // В реальном приложении здесь был бы алгоритм рекомендаций на основе:
        // - Истории пользователя
        // - Предпочтений по сложности
        // - Пройденных категорий
        // - Рейтингов похожих пользователей
        
        Specification<Quest> spec = (root, queryBuilder, criteriaBuilder) -> {
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            // Только опубликованные квесты
            predicates.add(criteriaBuilder.equal(root.get("status"), "PUBLISHED"));
            predicates.add(criteriaBuilder.equal(root.get("isPublic"), true));
            
            // Исключаем квесты, созданные пользователем
            predicates.add(criteriaBuilder.not(criteriaBuilder.isMember(userId, root.get("authorIds"))));
            
            return criteriaBuilder.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
        
        org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(0, limit);
        
        Page<Quest> recommendedQuests = questRepository.findAll(spec, pageable);
        
        return recommendedQuests.getContent().stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Построение расширенной спецификации поиска
     */
    private Specification<Quest> buildAdvancedSearchSpecification(QuestSearchRequestDTO searchRequest) {
        return (root, queryBuilder, criteriaBuilder) -> {
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Базовые фильтры
            if (searchRequest.getTitle() != null && !searchRequest.getTitle().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + searchRequest.getTitle().toLowerCase() + "%"
                ));
            }

            if (searchRequest.getDescription() != null && !searchRequest.getDescription().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        "%" + searchRequest.getDescription().toLowerCase() + "%"
                ));
            }

            if (searchRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchRequest.getStatus()));
            }

            if (searchRequest.getDifficulty() != null) {
                predicates.add(criteriaBuilder.equal(root.get("difficulty"), searchRequest.getDifficulty()));
            }

            if (searchRequest.getQuestType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("questType"), searchRequest.getQuestType()));
            }

            if (searchRequest.getCategory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), searchRequest.getCategory()));
            }

            if (searchRequest.getAuthorId() != null) {
                predicates.add(criteriaBuilder.isMember(searchRequest.getAuthorId(), root.get("authorIds")));
            }

            if (searchRequest.getTags() != null && !searchRequest.getTags().isEmpty()) {
                for (String tag : searchRequest.getTags()) {
                    predicates.add(criteriaBuilder.isMember(tag, root.get("tags")));
                }
            }

            if (searchRequest.getIsPublic() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPublic"), searchRequest.getIsPublic()));
            }

            if (searchRequest.getIsTemplate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isTemplate"), searchRequest.getIsTemplate()));
            }

            // Фильтры по дате
            if (searchRequest.getCreatedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), searchRequest.getCreatedFrom()));
            }
            if (searchRequest.getCreatedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), searchRequest.getCreatedTo()));
            }

            if (searchRequest.getPublishedFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("publishedAt"), searchRequest.getPublishedFrom()));
            }
            if (searchRequest.getPublishedTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("publishedAt"), searchRequest.getPublishedTo()));
            }

            // Фильтры по времени
            if (searchRequest.getStartTimeFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), searchRequest.getStartTimeFrom()));
            }
            if (searchRequest.getStartTimeTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), searchRequest.getStartTimeTo()));
            }

            // Фильтры по участникам
            if (searchRequest.getMinParticipants() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("minParticipants"), searchRequest.getMinParticipants()));
            }
            if (searchRequest.getMaxParticipants() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("maxParticipants"), searchRequest.getMaxParticipants()));
            }

            // Фильтры по длительности
            if (searchRequest.getMinDuration() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("estimatedDuration"), searchRequest.getMinDuration()));
            }
            if (searchRequest.getMaxDuration() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("estimatedDuration"), searchRequest.getMaxDuration()));
            }

            return criteriaBuilder.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * Создание объекта Pageable из запроса
     */
    private Pageable createPageable(QuestSearchRequestDTO searchRequest) {
        org.springframework.data.domain.Sort.Direction direction = 
                searchRequest.getSortDirection().equalsIgnoreCase("desc") ? 
                org.springframework.data.domain.Sort.Direction.DESC : 
                org.springframework.data.domain.Sort.Direction.ASC;
        
        return org.springframework.data.domain.PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                org.springframework.data.domain.Sort.by(direction, searchRequest.getSortBy())
        );
    }

    /**
     * Проверка, находится ли квест в указанном радиусе
     */
    private boolean isWithinRadius(QuestDTO quest, Double latitude, Double longitude, Double radiusKm) {
        // Упрощенная логика - в реальном приложении здесь был бы расчет расстояния
        // с использованием формулы гаверсинуса или геопространственных функций БД
        
        if (quest.getStartLocation() == null || quest.getStartLocation().isEmpty()) {
            return false;
        }
        
        // Для примера возвращаем true для всех квестов с указанной локацией
        return true;
    }

    /**
     * Конвертация Entity в DTO
     */
    private QuestDTO convertToDTO(Quest quest) {
        QuestDTO dto = new QuestDTO();
        dto.setId(quest.getId());
        dto.setNumber(quest.getNumber());
        dto.setTitle(quest.getTitle());
        dto.setDescription(quest.getDescription());
        dto.setDifficulty(quest.getDifficulty());
        dto.setQuestType(quest.getQuestType());
        dto.setCategory(quest.getCategory());
        dto.setEstimatedDuration(quest.getEstimatedDuration());
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