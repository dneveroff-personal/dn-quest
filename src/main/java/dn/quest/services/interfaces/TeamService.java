package dn.quest.services.interfaces;

import dn.quest.model.dto.TeamDTO;
import dn.quest.model.dto.TeamInvitationDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.team.Team;

import java.util.List;
import java.util.Set;

public interface TeamService {

    TeamDTO createTeam(String name, Long captainUserId);

    Team getById(Long id);

    TeamDTO getTeamDTO(Long teamId);

    Set<UserDTO> listMembers(Long teamId);

    void addMember(Long teamId, Long userId);

    void inviteUser(Long teamId, String username);

    void respondToInvite(Long invitationId, boolean accept);

    void removeMember(Long teamId, Long userId);

    void transferCaptain(Long teamId, Long newCaptainUserId);

    void deleteTeam(Long teamId);

}
