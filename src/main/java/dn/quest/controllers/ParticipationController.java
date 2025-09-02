package dn.quest.controllers;

import dn.quest.model.dto.ParticipationRequestDTO;
import dn.quest.model.entities.enums.ParticipationStatus;
import dn.quest.services.interfaces.ParticipationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Routes.PARTICIPATION)
@RequiredArgsConstructor
public class ParticipationController implements Routes {

    private final ParticipationService participationService;

    @PostMapping
    public ResponseEntity<ParticipationRequestDTO> createRequest(
            @RequestParam Long questId,
            @RequestParam String type,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long teamId
    ) {
        var request = participationService.createRequest(
                questId,
                Enum.valueOf(dn.quest.model.entities.enums.ApplicantType.class, type),
                userId,
                teamId
        );
        return ResponseEntity.ok(toDTO(request));
    }

    @PostMapping(PARTICIPATION_STATUS)
    public ResponseEntity<ParticipationRequestDTO> changeStatus(
            @PathVariable Long id,
            @RequestParam ParticipationStatus status
    ) {
        var updated = participationService.changeStatus(id, status);
        return ResponseEntity.ok(toDTO(updated));
    }

    @GetMapping(PARTICIPATORS)
    public ResponseEntity<List<ParticipationRequestDTO>> listByQuest(
            @PathVariable Long questId,
            @RequestParam(required = false) ParticipationStatus status
    ) {
        List<ParticipationRequestDTO> list = participationService.listByQuest(questId, status)
                .stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    private ParticipationRequestDTO toDTO(dn.quest.model.entities.quest.ParticipationRequest request) {
        return ParticipationRequestDTO.builder()
                .id(request.getId())
                .questId(request.getQuest().getId())
                .userId(request.getUser() != null ? request.getUser().getId() : null)
                .teamId(request.getTeam() != null ? request.getTeam().getId() : null)
                .type(request.getApplicantType())
                .status(request.getStatus())
                .build();
    }
}
