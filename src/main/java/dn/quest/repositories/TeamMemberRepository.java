package dn.quest.repositories;

import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.team.TeamMember;
import dn.quest.model.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByTeam(Team team);
    Optional<TeamMember> findByUser(User user);
    Optional<TeamMember> findByTeamAndUser(Team team, User user);

}
