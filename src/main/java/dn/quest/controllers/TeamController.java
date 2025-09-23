package dn.quest.controllers;

import dn.quest.model.dto.TeamDTO;
import dn.quest.model.dto.TeamInvitationDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.services.interfaces.TeamService;
import dn.quest.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(Routes.TEAMS)
@RequiredArgsConstructor
public class TeamController implements Routes {

    private final TeamService teamService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<TeamDTO> createTeam(
            @RequestParam String name,
            @RequestParam Long captainUserId
    ) {
        return ResponseEntity.ok(teamService.createTeam(name, captainUserId));
    }

    @GetMapping(ID)
    public ResponseEntity<TeamDTO> getTeam(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeamDTO(id));
    }

    @GetMapping(TEAM_MEMBERS)
    public ResponseEntity<Set<UserDTO>> listMembers(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.listMembers(id));
    }

    @PostMapping(TEAM_MEMBER_ADD)
    public ResponseEntity<Void> addMember(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        teamService.addMember(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(TEAM_MEMBER_ADD)
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        teamService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(TEAM_TRANSFER_CAPTAIN)
    public ResponseEntity<Void> transferCaptain(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        teamService.transferCaptain(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(ID)
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(TEAM_INVITE_USER)
    public ResponseEntity<Void> inviteUser(@PathVariable Long teamId,
                                           @PathVariable String username) {
        teamService.inviteUser(teamId, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping(TEAM_INVITE_RESPONSE)
    public ResponseEntity<Void> respond(@PathVariable Long id,
                                        @PathVariable String action) {
        boolean accept = action.equalsIgnoreCase("accept");
        teamService.respondToInvite(id, accept);
        return ResponseEntity.ok().build();
    }

}
