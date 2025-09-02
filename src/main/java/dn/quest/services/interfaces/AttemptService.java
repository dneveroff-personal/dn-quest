package dn.quest.services.interfaces;

import dn.quest.model.dto.CodeAttemptDTO;

import java.util.List;

public interface AttemptService {

    CodeAttemptDTO submit(CodeAttemptDTO attemptDTO);

    CodeAttemptDTO getById(Long id);

    List<CodeAttemptDTO> getAll();

    List<CodeAttemptDTO> getLastAttempts(Long sessionId, Long levelId, int limit);
}
