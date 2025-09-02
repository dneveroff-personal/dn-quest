package dn.quest.services.impl;

import dn.quest.model.dto.CodeAttemptDTO;
import dn.quest.model.entities.quest.level.CodeAttempt;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.CodeAttemptRepository;
import dn.quest.repositories.UserRepository;
import dn.quest.services.interfaces.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttemptServiceImpl implements AttemptService {

    private final CodeAttemptRepository codeAttemptRepository;
    private final UserRepository userRepository;

    @Override
    public CodeAttemptDTO submit(CodeAttemptDTO dto) {
        CodeAttempt entity = toEntity(dto);
        entity = codeAttemptRepository.save(entity);
        return toDTO(entity);
    }

    @Override
    public CodeAttemptDTO getById(Long id) {
        return codeAttemptRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
    }

    @Override
    public List<CodeAttemptDTO> getAll() {
        return codeAttemptRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ===== Маппинг =====
    private CodeAttemptDTO toDTO(CodeAttempt entity) {
        return CodeAttemptDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .submittedRaw(entity.getSubmittedRaw())
                .submittedNormalized(entity.getSubmittedNormalized())
                .result(entity.getResult())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private CodeAttempt toEntity(CodeAttemptDTO dto) {
        CodeAttempt entity = new CodeAttempt();
        entity.setId(dto.getId());

        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id " + dto.getUserId()));
            entity.setUser(user);
        }

        entity.setSubmittedRaw(dto.getSubmittedRaw());
        entity.setSubmittedNormalized(dto.getSubmittedNormalized());
        entity.setResult(dto.getResult());
        entity.setCreatedAt(dto.getCreatedAt());

        return entity;
    }
}
