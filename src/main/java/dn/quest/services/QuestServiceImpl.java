package dn.quest.services;

import dn.quest.model.dto.QuestDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.QuestRepository;
import dn.quest.repositories.UserRepository;
import dn.quest.services.interfaces.QuestService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestServiceImpl implements QuestService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;

    @Override
    public QuestDTO create(QuestDTO questDTO) {
        Quest quest = toEntity(questDTO);
        quest.setCreatedAt(Instant.now());
        quest.setUpdatedAt(Instant.now());

        Quest saved = questRepository.save(quest);
        return toDTO(saved);
    }

    @Override
    public QuestDTO update(Long id, QuestDTO questDTO) {
        Quest existing = questRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quest not found: " + id));

        existing.setTitle(questDTO.getTitle());
        existing.setDescriptionHtml(questDTO.getDescriptionHtml());
        existing.setDifficulty(questDTO.getDifficulty());
        existing.setType(questDTO.getType());
        existing.setStartAt(questDTO.getStartAt());
        existing.setEndAt(questDTO.getEndAt());
        existing.setPublished(questDTO.isPublished());

        // Обновляем авторов
        if (questDTO.getAuthors() != null) {
            existing.getAuthors().clear();
            for (UserDTO authorDTO : questDTO.getAuthors()) {
                User user = userRepository.findById(authorDTO.getId())
                        .orElseThrow(() -> new EntityNotFoundException("User not found: " + authorDTO.getId()));
                existing.getAuthors().add(user);
            }
        }

        existing.setUpdatedAt(Instant.now());
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
                        .map(u -> new UserDTO(u.getId(), u.getPublicName()))
                        .collect(Collectors.toSet()))
                .build();
    }

    private Quest toEntity(QuestDTO dto) {
        Quest quest = new Quest();
        quest.setId(dto.getId());
        quest.setTitle(dto.getTitle());
        quest.setDescriptionHtml(dto.getDescriptionHtml());
        quest.setDifficulty(dto.getDifficulty());
        quest.setType(dto.getType());
        quest.setStartAt(dto.getStartAt());
        quest.setEndAt(dto.getEndAt());
        quest.setPublished(dto.isPublished());

        if (dto.getAuthors() != null) {
            for (UserDTO authorDTO : dto.getAuthors()) {
                User user = userRepository.findById(authorDTO.getId())
                        .orElseThrow(() -> new EntityNotFoundException("User not found: " + authorDTO.getId()));
                quest.getAuthors().add(user);
            }
        }

        return quest;
    }
}
