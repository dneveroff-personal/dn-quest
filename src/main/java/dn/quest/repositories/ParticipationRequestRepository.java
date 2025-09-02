package dn.quest.repositories;

import dn.quest.model.entities.enums.ParticipationStatus;
import dn.quest.model.entities.quest.ParticipationRequest;
import dn.quest.model.entities.quest.Quest;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findByQuestAndStatus(Quest quest, ParticipationStatus status);
    List<ParticipationRequest> findByQuestAndTeam(Quest quest, Team team);
    List<ParticipationRequest> findByQuestAndUser(Quest quest, User user);
    List<ParticipationRequest> findByQuest(Quest quest);
}
