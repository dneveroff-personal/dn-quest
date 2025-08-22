package dn.quest.controllers;

import dn.quest.model.dto.TeamDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.team.Team;
import dn.quest.services.interfaces.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;


@RestController
@RequestMapping(Routes.TEAMS)
@RequiredArgsConstructor
public class TeamController implements Routes {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamDTO> createTeam(
            @RequestParam String name,
            @RequestParam Integer captainUserId
    ) {
        Team team = teamService.createTeam(name, captainUserId);
        return ResponseEntity.ok(teamService.getTeamDTO(team.getId()));
    }

    @GetMapping(Routes.ID)
    public ResponseEntity<TeamDTO> getTeam(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeamDTO(id));
    }

    @GetMapping(Routes.TEAM_MEMBERS)
    public ResponseEntity<Set<UserDTO>> listMembers(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.listMembers(id));
    }

    @PostMapping(Routes.TEAM_MEMBER_ADD)
    public ResponseEntity<Void> addMember(
            @PathVariable Long id,
            @PathVariable Integer userId
    ) {
        teamService.addMember(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(Routes.TEAM_MEMBER_ADD)
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Integer userId
    ) {
        teamService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(Routes.TEAM_TRANSFER_CAPTAIN)
    public ResponseEntity<Void> transferCaptain(
            @PathVariable Long id,
            @PathVariable Integer userId
    ) {
        teamService.transferCaptain(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(Routes.ID)
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

}
