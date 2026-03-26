package dn.quest.teammanagement.controller;

import dn.quest.teammanagement.dto.UserDTO;
import dn.quest.teammanagement.dto.UserStatisticsDTO;
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

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        log.debug("Getting user by username: {}", username);
        
        return ResponseEntity.ok(userService.getUserByUsername(username));
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
        
        TeamListResponse response = teamService.getUserTeams(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/teams/active")
    public ResponseEntity<List<TeamDTO>> getUserActiveTeams(@PathVariable Long userId) {
        log.debug("Getting active teams for user: {}", userId);
        
        List<TeamDTO> teams = teamService.getUserActiveTeams(userId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{userId}/is-member/{teamId}")
    public ResponseEntity<Boolean> isUserTeamMember(
            @PathVariable Long userId,
            @PathVariable Long teamId) {
        
        log.debug("Checking if user {} is member of team {}", userId, teamId);
        
        boolean isMember = teamService.isTeamMember(teamId, userId);
        return ResponseEntity.ok(isMember);
    }

    @GetMapping("/{userId}/is-captain/{teamId}")
    public ResponseEntity<Boolean> isUserTeamCaptain(
            @PathVariable Long userId,
            @PathVariable Long teamId) {
        
        log.debug("Checking if user {} is captain of team {}", userId, teamId);
        
        boolean isCaptain = teamService.isTeamCaptain(teamId, userId);
        return ResponseEntity.ok(isCaptain);
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
        
        long count = userService.getUserStatistics().getTotalUsers();
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
        
        UserDTO user = userService.getUserByUsername(userDetails.getUsername());
        if (user == null) {
            throw new RuntimeException("User not found: " + userDetails.getUsername());
        }
        
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
        
        TeamListResponse response = teamService.getUserTeams(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        log.debug("Getting user by email: {}", email);
        
        return ResponseEntity.ok(userService.getUserByEmail(email));
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

    @GetMapping("/new")
    public ResponseEntity<List<UserDTO>> getNewUsers(@RequestParam(defaultValue = "10") int limit) {
        return null; //Todo - написать метод для сервиса
    }

    @GetMapping("/statistics/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics() {
        log.debug("Getting user statistics");
        
        UserStatisticsDTO statistics = userService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }
}