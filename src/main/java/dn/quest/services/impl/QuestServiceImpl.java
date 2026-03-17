package dn.quest.services.impl;

import dn.quest.config.ApplicationConstants;
import dn.quest.model.dto.QuestCreateUpdateDTO;
import dn.quest.model.dto.QuestDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.enums.UserRole;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.QuestRepository;
import dn.quest.repositories.UserRepository;
import dn.quest.services.interfaces.QuestService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class QuestServiceImpl implements QuestService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;

    @Override
    public QuestDTO createQuest(QuestCreateUpdateDTO dto, String authorUsername) {
        log.debug("Creating new quest by user: {}", authorUsername);
        
        Quest quest = toEntity(dto, authorUsername);
        quest.setCreatedAt(Instant.now());
        quest.setUpdatedAt(Instant.now());

        Quest saved = questRepository.save(quest);
        log.info("Quest created successfully: {} by user: {}", saved.getId(), authorUsername);
        return toDTO(saved);
    }

    @Override
    public QuestDTO updateQuest(Long id, QuestCreateUpdateDTO dto, String username) {
        log.debug("Updating quest: {} by user: {}", id, username);
        
        Quest existing = questRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Quest not found for update: {}", id);
                    return new EntityNotFoundException(ApplicationConstants.QUEST_NOT_FOUND + ": " + id);
                });

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for quest update: {}", username);
                    return new EntityNotFoundException(ApplicationConstants.USER_NOT_FOUND + ": " + username);
                });

        // Проверка прав: автор квеста или админ
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        boolean isAuthor = existing.getAuthors().contains(user);
        if (!isAdmin && !isAuthor) {
            log.warn("Unauthorized attempt to update quest: {} by user: {}", id, username);
            throw new RuntimeException("You are not allowed to edit this quest");
        }

        existing.setTitle(dto.getTitle());
        existing.setDescriptionHtml(dto.getDescriptionHtml());
        existing.setDifficulty(dto.getDifficulty());
        existing.setType(dto.getType());
        existing.setStartAt(dto.getStartAt());
        existing.setEndAt(dto.getEndAt());
        if (dto.getPublished() != null) {
            existing.setPublished(dto.getPublished());
        }
        existing.setUpdatedAt(Instant.now());

        // обновляем авторов, если переданы и если админ или автор
        if (dto.getAuthors() != null) {
            Set<User> newAuthors = dto.getAuthors().stream()
                    .map(a -> userRepository.findById(a.getId())
                            .orElseThrow(() -> {
                                log.warn("Author not found for quest update: {}", a.getId());
                                return new RuntimeException(ApplicationConstants.USER_NOT_FOUND + ": " + a.getId());
                            }))
                    .collect(Collectors.toSet());
            existing.setAuthors(newAuthors);
        }

        Quest updated = questRepository.save(existing);
        log.info("Quest updated successfully: {} by user: {}", updated.getId(), username);
        return toDTO(updated);
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting quest: {}", id);
        
        Quest quest = questRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Quest not found for deletion: {}", id);
                    return new EntityNotFoundException(ApplicationConstants.QUEST_NOT_FOUND + ": " + id);
                });
        
        questRepository.delete(quest);
        log.info("Quest deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestDTO getById(Long id) {
        log.debug("Getting quest by id: {}", id);
        
        Quest quest = questRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Quest not found: {}", id);
                    return new EntityNotFoundException(ApplicationConstants.QUEST_NOT_FOUND + ": " + id);
                });
        
        return toDTO(quest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getAll() {
        log.debug("Getting all quests");
        
        List<QuestDTO> quests = questRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        log.debug("Found {} quests", quests.size());
        return quests;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getPublished() {
        log.debug("Getting published quests");
        
        List<QuestDTO> quests = questRepository.findAll().stream()
                .filter(Quest::isPublished)
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        log.debug("Found {} published quests", quests.size());
        return quests;
    }

    // ===== Mapping =====
    private QuestDTO toDTO(Quest quest) {
        return QuestDTO.builder()
                .id(quest.getId())
                .number(quest.getNumber())
                .title(quest.getTitle())
                .descriptionHtml(quest.getDescriptionHtml())
                .difficulty(quest.getDifficulty())
                .type(quest.getType())
                .startAt(quest.getStartAt())
                .endAt(quest.getEndAt())
                .published(quest.isPublished())
                .authors(quest.getAuthors().stream()
                        .map(UserDTO::fromEntity)   // ✅ теперь удобно
                        .collect(Collectors.toSet()))
                .build();
    }

    private Quest toEntity(QuestCreateUpdateDTO dto, String authorUsername) {
        Quest quest = new Quest();
        quest.setTitle(dto.getTitle());
        quest.setDescriptionHtml(dto.getDescriptionHtml());
        quest.setDifficulty(dto.getDifficulty());
        quest.setType(dto.getType());
        quest.setStartAt(dto.getStartAt());
        quest.setEndAt(dto.getEndAt());
        quest.setPublished(dto.getPublished() != null ? dto.getPublished() : false);

        User user = userRepository.findByUsername(authorUsername)
                .orElseThrow(() -> {
                    log.error("Author not found during quest creation: {}", authorUsername);
                    return new EntityNotFoundException(ApplicationConstants.USER_NOT_FOUND + ": " + authorUsername);
                });
        quest.getAuthors().add(user);

        return quest;
    }
}
