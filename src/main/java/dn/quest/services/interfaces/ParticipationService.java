package dn.quest.services.interfaces;

import dn.quest.model.entities.enums.ApplicantType;
import dn.quest.model.entities.enums.ParticipationStatus;
import dn.quest.model.entities.quest.ParticipationRequest;

import java.util.List;

public interface ParticipationService {

    ParticipationRequest createRequest(Long questId, ApplicantType type, Long userIdOrNull, Long teamIdOrNull);
    ParticipationRequest changeStatus(Long requestId, ParticipationStatus newStatus);
    List<ParticipationRequest> listByQuest(Long questId, ParticipationStatus status);

}
