package dn.quest.services.interfaces;

import dn.quest.model.dto.TeamDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.team.Team;

import java.util.Set;

public interface TeamService {

    Team createTeam(String name, Integer captainUserId);
    Team getById(Long id);

    TeamDTO getTeamDTO(Long teamId);
    Set<UserDTO> listMembers(Long teamId);

    void addMember(Long teamId, Integer userId);
    void removeMember(Long teamId, Integer userId);

    void transferCaptain(Long teamId, Integer newCaptainUserId);

    void deleteTeam(Long teamId);

}
