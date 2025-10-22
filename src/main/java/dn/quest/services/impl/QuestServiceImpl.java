package dn.quest.services.impl;

import dn.quest.model.dto.QuestCreateUpdateDTO;
import dn.quest.model.dto.QuestDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.enums.UserRole;
import dn.quest.model.entities.quest.GameSession;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.QuestRepository;
import dn.quest.repositories.UserRepository;
import dn.quest.services.interfaces.QuestService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestServiceImpl implements QuestService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;

    @Override
    public QuestDTO createQuest(QuestCreateUpdateDTO dto, String authorUsername) {
        Quest quest = toEntity(dto, authorUsername);
        quest.setCreatedAt(Instant.now());
        quest.setUpdatedAt(Instant.now());

        Quest saved = questRepository.save(quest);
        return toDTO(saved);
    }

    @Override
    public QuestDTO updateQuest(Long id, QuestCreateUpdateDTO dto, String username) {
        Quest existing = questRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quest not found: " + id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        // Проверка прав: автор квеста или админ
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        boolean isAuthor = existing.getAuthors().contains(user);
        if (!isAdmin && !isAuthor) {
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
                            .orElseThrow(() -> new RuntimeException("User not found: " + a.getId())))
                    .collect(Collectors.toSet());
            existing.setAuthors(newAuthors);
        }

        return toDTO(questRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        Quest quest = questRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quest not found: " + id));
        questRepository.delete(quest);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestDTO getById(Long id) {
        Quest quest = questRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quest not found: " + id));
        return toDTO(quest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getAll() {
        return questRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestDTO> getPublished() {
        return questRepository.findAll().stream()
                .filter(Quest::isPublished)
                .map(this::toDTO)
                .collect(Collectors.toList());
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
                .orElseThrow(() -> new EntityNotFoundException("Author not found: " + authorUsername));
        quest.getAuthors().add(user);

        return quest;
    }
}
