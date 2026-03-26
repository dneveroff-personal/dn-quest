package dn.quest.teammanagement.controller;

import dn.quest.teammanagement.dto.TeamDTO;
import dn.quest.teammanagement.dto.TeamMemberDTO;
import dn.quest.teammanagement.dto.TeamSettingsDTO;
import dn.quest.teammanagement.dto.TeamStatisticsDTO;
import dn.quest.teammanagement.dto.request.ChangeMemberRoleRequest;
import dn.quest.teammanagement.dto.request.CreateTeamRequest;
import dn.quest.teammanagement.dto.request.SearchTeamsRequest;
import dn.quest.teammanagement.dto.request.UpdateTeamRequest;
import dn.quest.teammanagement.dto.request.UpdateTeamSettingsRequest;
import dn.quest.teammanagement.dto.response.TeamListResponse;
import dn.quest.teammanagement.entity.TeamMember;
import dn.quest.teammanagement.enums.TeamRole;
import dn.quest.teammanagement.mapper.TeamMapper;
import dn.quest.teammanagement.service.TeamService;
import dn.quest.teammanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST контроллер для управления командами
 */
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;
    private final TeamMapper teamMapper;

    @GetMapping
    public ResponseEntity<TeamListResponse> getAllTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {
        
        log.debug("Getting all teams with pagination: page={}, size={}, sortBy={}, sortDir={}, search={}", 
                page, size, sortBy, sortDir, search);

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        TeamListResponse response = search != null && !search.trim().isEmpty() 
                ? teamService.searchTeams(search, pageable)
                : teamService.getTeams(pageable);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TeamDTO> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Creating team with name: {} by user: {}", request.getName(), userDetails.getUsername());
        
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        TeamDTO team = teamService.createTeam(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(team);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> getTeamById(@PathVariable Long id) {
        log.debug("Getting team by id: {}", id);
        
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TeamDTO> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeamRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Updating team: {} by user: {}", id, userDetails.getUsername());
        
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        TeamDTO updatedTeam = teamService.updateTeam(id, request, userId);
        
        return ResponseEntity.ok(updatedTeam);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteTeam(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Deleting team: {} by user: {}", id, userDetails.getUsername());
        
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        teamService.deleteTeam(id, userId);
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<TeamMemberDTO>> getTeamMembers(@PathVariable Long id) {
        log.debug("Getting members for team: {}", id);
        
        List<TeamMemberDTO> members = teamService.getTeamMembers(id);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TeamMemberDTO> addMember(
            @PathVariable Long id,
            @RequestParam Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Adding member {} to team: {} by user: {}", userId, id, userDetails.getUsername());
        
        Long requesterId = userService.getUserIdByUsername(userDetails.getUsername());
        TeamMember member = teamService.addMember(id, userId, requesterId);

        return ResponseEntity.status(HttpStatus.CREATED).body(teamMapper.toTeamMemberDTO(member));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Removing member {} from team: {} by user: {}", userId, id, userDetails.getUsername());
        
        Long requesterId = userService.getUserIdByUsername(userDetails.getUsername());
        teamService.removeMember(id, userId, requesterId);
        
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/members/{userId}/role")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TeamMemberDTO> changeMemberRole(
            @PathVariable Long id,
            @PathVariable Long userId,
            @Valid @RequestBody ChangeMemberRoleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Changing role for member {} in team: {} to {} by user: {}", 
                userId, id, request.getRole(), userDetails.getUsername());
        
        Long requesterId = userService.getUserIdByUsername(userDetails.getUsername());
        TeamMember member = teamService.changeMemberRole(id, userId, String.valueOf(request.getRole()), requesterId);
        
        return ResponseEntity.ok(teamMapper.toTeamMemberDTO(member));
    }

    @PutMapping("/{id}/captain/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TeamMemberDTO> transferCaptaincy(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Transferring captaincy to member {} in team: {} by user: {}", 
                userId, id, userDetails.getUsername());
        
        Long requesterId = userService.getUserIdByUsername(userDetails.getUsername());
        TeamMember member = teamService.transferCaptain(id, userId, requesterId);

        return ResponseEntity.ok(teamMapper.toTeamMemberDTO(member));
    }

    @GetMapping("/{id}/settings")
    public ResponseEntity<TeamSettingsDTO> getTeamSettings(@PathVariable Long id) {
        log.debug("Getting settings for team: {}", id);
        
        return ResponseEntity.ok(teamService.getTeamSettings(id));
    }

    @PutMapping("/{id}/settings")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TeamSettingsDTO> updateTeamSettings(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeamSettingsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Updating settings for team: {} by user: {}", id, userDetails.getUsername());
        
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        TeamSettingsDTO settings = teamService.updateTeamSettings(id, request, userId);
        
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<TeamStatisticsDTO> getTeamStatistics(@PathVariable Long id) {
        log.debug("Getting statistics for team: {}", id);
        
        return ResponseEntity.ok(teamService.getTeamStatistics(id));
    }

    @PostMapping("/search")
    public ResponseEntity<TeamListResponse> searchTeams(
            @Valid @RequestBody SearchTeamsRequest request) {
        
        log.debug("Searching teams with criteria: {}", request);
        
        Pageable pageable = PageRequest.of(
                request.getPage(), 
                request.getSize(), 
                Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy())
        );
        
        TeamListResponse response = teamService.searchTeams(request, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-tag/{tag}")
    public ResponseEntity<List<TeamDTO>> getTeamsByTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting teams by tag: {} with pagination: page={}, size={}", tag, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<TeamDTO> response = teamService.getTeamsByTag(tag);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalTeamsCount() {
        log.debug("Getting total teams count");
        
        long count = teamService.getTeamsCount();
        return ResponseEntity.ok(count);
    }

}