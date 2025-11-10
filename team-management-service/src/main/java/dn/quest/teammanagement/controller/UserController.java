package dn.quest.teammanagement.controller;

import dn.quest.teammanagement.dto.UserDTO;
import dn.quest.teammanagement.dto.TeamDTO;
import dn.quest.teammanagement.dto.TeamMemberDTO;
import dn.quest.teammanagement.dto.response.TeamListResponse;
import dn.quest.teammanagement.service.UserService;
import dn.quest.teammanagement.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления пользователями
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final TeamService teamService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        log.debug("Getting user by id: {}", userId);
        
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        log.debug("Getting user by username: {}", username);
        
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/teams")
    public ResponseEntity<TeamListResponse> getUserTeams(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "joinedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.debug("Getting teams for user: {} with pagination: page={}, size={}, sortBy={}, sortDir={}", 
                userId, page, size, sortBy, sortDir);

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        TeamListResponse response = userService.getUserTeams(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/teams/active")
    public ResponseEntity<List<TeamDTO>> getUserActiveTeams(@PathVariable Long userId) {
        log.debug("Getting active teams for user: {}", userId);
        
        List<TeamDTO> teams = userService.getUserActiveTeams(userId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{userId}/teams/captain")
    public ResponseEntity<List<TeamDTO>> getUserCaptainTeams(@PathVariable Long userId) {
        log.debug("Getting captain teams for user: {}", userId);
        
        List<TeamDTO> teams = userService.getUserCaptainTeams(userId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{userId}/teams/memberships")
    public ResponseEntity<List<TeamMemberDTO>> getUserTeamMemberships(@PathVariable Long userId) {
        log.debug("Getting team memberships for user: {}", userId);
        
        List<TeamMemberDTO> memberships = userService.getUserTeamMemberships(userId);
        return ResponseEntity.ok(memberships);
    }

    @GetMapping("/{userId}/teams/count")
    public ResponseEntity<Long> getUserTeamsCount(@PathVariable Long userId) {
        log.debug("Getting teams count for user: {}", userId);
        
        long count = userService.getUserTeamsCount(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{userId}/teams/active/count")
    public ResponseEntity<Long> getUserActiveTeamsCount(@PathVariable Long userId) {
        log.debug("Getting active teams count for user: {}", userId);
        
        long count = userService.getUserActiveTeamsCount(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{userId}/is-member/{teamId}")
    public ResponseEntity<Boolean> isUserTeamMember(
            @PathVariable Long userId,
            @PathVariable Long teamId) {
        
        log.debug("Checking if user {} is member of team {}", userId, teamId);
        
        boolean isMember = userService.isUserTeamMember(userId, teamId);
        return ResponseEntity.ok(isMember);
    }

    @GetMapping("/{userId}/is-captain/{teamId}")
    public ResponseEntity<Boolean> isUserTeamCaptain(
            @PathVariable Long userId,
            @PathVariable Long teamId) {
        
        log.debug("Checking if user {} is captain of team {}", userId, teamId);
        
        boolean isCaptain = userService.isUserTeamCaptain(userId, teamId);
        return ResponseEntity.ok(isCaptain);
    }

    @GetMapping("/{userId}/can-join/{teamId}")
    public ResponseEntity<Boolean> canUserJoinTeam(
            @PathVariable Long userId,
            @PathVariable Long teamId) {
        
        log.debug("Checking if user {} can join team {}", userId, teamId);
        
        boolean canJoin = userService.canUserJoinTeam(userId, teamId);
        return ResponseEntity.ok(canJoin);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.debug("Searching users with query: {} and limit: {}", query, limit);
        
        List<UserDTO> users = userService.searchUsers(query, limit);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active")
    public ResponseEntity<List<UserDTO>> getActiveUsers(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.debug("Getting active users with limit: {}", limit);
        
        List<UserDTO> users = userService.getActiveUsers(limit);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalUsersCount() {
        log.debug("Getting total users count");
        
        long count = userService.getTotalUsersCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/active/count")
    public ResponseEntity<Long> getActiveUsersCount() {
        log.debug("Getting active users count");
        
        long count = userService.getActiveUsersCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting current user: {}", userDetails.getUsername());
        
        UserDTO user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
        
        return ResponseEntity.ok(user);
    }

    @GetMapping("/current/teams")
    public ResponseEntity<TeamListResponse> getCurrentUserTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "joinedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting current user teams with pagination: page={}, size={}, sortBy={}, sortDir={}", 
                page, size, sortBy, sortDir);

        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        TeamListResponse response = userService.getUserTeams(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current/teams/active")
    public ResponseEntity<List<TeamDTO>> getCurrentUserActiveTeams(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting current user active teams");
        
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        List<TeamDTO> teams = userService.getUserActiveTeams(userId);
        
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/current/teams/captain")
    public ResponseEntity<List<TeamDTO>> getCurrentUserCaptainTeams(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting current user captain teams");
        
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        List<TeamDTO> teams = userService.getUserCaptainTeams(userId);
        
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/current/teams/count")
    public ResponseEntity<Long> getCurrentUserTeamsCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting current user teams count");
        
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        long count = userService.getUserTeamsCount(userId);
        
        return ResponseEntity.ok(count);
    }

    @GetMapping("/current/teams/active/count")
    public ResponseEntity<Long> getCurrentUserActiveTeamsCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting current user active teams count");
        
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        long count = userService.getUserActiveTeamsCount(userId);
        
        return ResponseEntity.ok(count);
    }

    @GetMapping("/current/can-create-team")
    public ResponseEntity<Boolean> canCurrentUserCreateTeam(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Checking if current user can create team");
        
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        boolean canCreate = userService.canUserCreateTeam(userId);
        
        return ResponseEntity.ok(canCreate);
    }

    @GetMapping("/current/team-limit")
    public ResponseEntity<Integer> getCurrentUserTeamLimit(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting current user team limit");
        
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        int limit = userService.getUserTeamLimit(userId);
        
        return ResponseEntity.ok(limit);
    }

    @GetMapping("/current/teams-left")
    public ResponseEntity<Integer> getCurrentUserTeamsLeft(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting current user teams left");
        
        Long userId = userService.getUserIdByUsername(userDetails.getUsername());
        int teamsLeft = userService.getUserTeamsLeft(userId);
        
        return ResponseEntity.ok(teamsLeft);
    }

    @GetMapping("/by-email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        log.debug("Getting user by email: {}", email);
        
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting users by role: {} with pagination: page={}, size={}", role, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "username"));
        List<UserDTO> users = userService.getUsersByRole(role, pageable);
        
        return ResponseEntity.ok(users);
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getInactiveUsers(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.debug("Getting inactive users for {} days with limit: {}", days, limit);
        
        List<UserDTO> users = userService.getInactiveUsers(days, limit);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/top-contributors")
    public ResponseEntity<List<UserDTO>> getTopContributors(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.debug("Getting top contributors with limit: {}", limit);
        
        List<UserDTO> users = userService.getTopContributors(limit);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/most-active")
    public ResponseEntity<List<UserDTO>> getMostActiveUsers(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.debug("Getting most active users with limit: {}", limit);
        
        List<UserDTO> users = userService.getMostActiveUsers(limit);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/new")
    public ResponseEntity<List<UserDTO>> getNewUsers(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.debug("Getting new users with limit: {}", limit);
        
        List<UserDTO> users = userService.getNewUsers(limit);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/statistics/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics() {
        log.debug("Getting user statistics");
        
        UserStatisticsDTO statistics = userService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }
}