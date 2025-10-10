package dn.quest.services.impl;

import dn.quest.model.dto.CodeAttemptDTO;
import dn.quest.model.entities.quest.level.CodeAttempt;
import dn.quest.model.entities.quest.level.Level;
import dn.quest.repositories.CodeAttemptRepository;
import dn.quest.repositories.GameSessionRepository;
import dn.quest.repositories.LevelRepository;
import dn.quest.services.interfaces.AttemptService;
import dn.quest.services.interfaces.GameSessionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttemptServiceImpl implements AttemptService {

    private final CodeAttemptRepository codeAttemptRepository;
    private final GameSessionRepository gameSessionRepository;
    private final LevelRepository levelRepository;
    private final GameSessionService gameSessionService;

    @Override
    public CodeAttemptDTO submit(CodeAttemptDTO attemptDTO) {
        if (attemptDTO.getSessionId() == null) {
            throw new IllegalArgumentException("sessionId is required");
        }

        var result = gameSessionService.submitCode(
                attemptDTO.getSessionId(),
                attemptDTO.getSubmittedRaw(),
                attemptDTO.getUserId() != null ? attemptDTO.getUserId().intValue() : null
        );

        var session = gameSessionRepository.findById(attemptDTO.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));
        Level level = levelRepository.findById(attemptDTO.getLevelId()).orElse(null);

        return CodeAttemptDTO.builder()
                .sessionId(session.getId())
                .levelId(level != null ? level.getId() : null)
                .userId(attemptDTO.getUserId())
                .submittedRaw(attemptDTO.getSubmittedRaw())
                .submittedNormalized(attemptDTO.getSubmittedRaw() != null ? attemptDTO.getSubmittedRaw().trim().toLowerCase() : "")
                .result(result)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CodeAttemptDTO getById(Long id) {
        return codeAttemptRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("CodeAttempt not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeAttemptDTO> getAll() {
        return codeAttemptRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeAttemptDTO> getLastAttempts(Long sessionId, Long levelId, int limit) {
        return codeAttemptRepository.findLastAttempts(sessionId, levelId, PageRequest.of(0, limit))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private CodeAttemptDTO toDTO(CodeAttempt attempt) {
        return CodeAttemptDTO.builder()
                .id(attempt.getId())
                .sessionId(attempt.getSession() != null ? attempt.getSession().getId() : null)
                .levelId(attempt.getLevel() != null ? attempt.getLevel().getId() : null)
                .userId(attempt.getUser() != null ? attempt.getUser().getId() : null)
                .submittedRaw(attempt.getSubmittedRaw())
                .submittedNormalized(attempt.getSubmittedNormalized())
                .result(attempt.getResult())
                .createdAt(attempt.getCreatedAt())
                .build();
    }
}
