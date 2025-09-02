package service;

import dn.quest.model.entities.enums.ApplicantType;
import dn.quest.model.entities.enums.ParticipationStatus;
import dn.quest.model.entities.quest.ParticipationRequest;
import dn.quest.repositories.ParticipationRequestRepository;
import dn.quest.repositories.QuestRepository;
import dn.quest.repositories.TeamRepository;
import dn.quest.repositories.UserRepository;
import dn.quest.services.impl.ParticipationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ParticipationServiceImplTest {

    private ParticipationServiceImpl service;
    private ParticipationRequestRepository participationRepo;
    private QuestRepository questRepo;
    private UserRepository userRepo;
    private TeamRepository teamRepo;

    @BeforeEach
    void setup() {
        participationRepo = Mockito.mock(ParticipationRequestRepository.class);
        questRepo = Mockito.mock(QuestRepository.class);
        userRepo = Mockito.mock(UserRepository.class);
        teamRepo = Mockito.mock(TeamRepository.class);

        service = new ParticipationServiceImpl(participationRepo, questRepo, userRepo, teamRepo);
    }

    @Test
    void testChangeStatus() {
        var request = new ParticipationRequest();
        request.setStatus(ParticipationStatus.PENDING);

        Mockito.when(participationRepo.findById(1L)).thenReturn(Optional.of(request));
        Mockito.when(participationRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = service.changeStatus(1L, ParticipationStatus.APPROVED);

        assertEquals(ParticipationStatus.APPROVED, updated.getStatus());
    }
}
