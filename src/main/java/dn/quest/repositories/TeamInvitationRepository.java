package dn.quest.repositories;

import dn.quest.model.entities.enums.InvitationStatus;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.team.TeamInvitation;
import dn.quest.model.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {

    List<TeamInvitation> findByUserAndStatus(User user, InvitationStatus status);
    Optional<TeamInvitation> findByTeamAndUserAndStatus(Team team, User user, InvitationStatus status);
    Optional<TeamInvitation> findByTeamAndUser(Team team, User user);

}
