package dn.quest.teammanagement.controller;

import dn.quest.teammanagement.dto.TeamInvitationDTO;
import dn.quest.teammanagement.dto.InvitationStatisticsDTO;
import dn.quest.teammanagement.dto.GlobalInvitationStatisticsDTO;
import dn.quest.teammanagement.dto.request.InviteUserRequest;
import dn.quest.teammanagement.dto.request.RespondToInvitationRequest;
import dn.quest.teammanagement.dto.response.InvitationListResponse;
import dn.quest.teammanagement.service.TeamInvitationService;
import dn.quest.teammanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST контроллер для управления приглашениями в команды
 */
@RestController
@RequestMapping("/api/teams/{teamId}/invitations")
@RequiredArgsConstructor
@Slf4j
public class TeamInvitationController {

    private final TeamInvitationService invitationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<InvitationListResponse> getTeamInvitations(
            @PathVariable UUID teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting invitations for team: {} with pagination: page={}, size={}, sortBy={}, sortDir={}", 
                teamId, page, size, sortBy, sortDir);

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        InvitationListResponse response = invitationService.getTeamInvitations(teamId, userId, pageable);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<TeamInvitationDTO>> getTeamActiveInvitations(
            @PathVariable UUID teamId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting active invitations for team: {}", teamId);
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        List<TeamInvitationDTO> invitations = invitationService.getTeamActiveInvitations(teamId, userId);
        
        return ResponseEntity.ok(invitations);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TeamInvitationDTO> inviteUser(
            @PathVariable UUID teamId,
            @Valid @RequestBody InviteUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Inviting user {} to team: {} by user: {}", 
                request.getUsername(), teamId, userDetails.getUsername());
        
        UUID inviterId = userService.getUserIdByUsername(userDetails.getUsername());
        TeamInvitationDTO invitation = invitationService.inviteUser(teamId, request, inviterId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
    }

    @PostMapping("/by-id/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TeamInvitationDTO> inviteUserById(
            @PathVariable UUID teamId,
            @PathVariable UUID userId,
            @RequestParam(required = false) String message,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Inviting user {} to team: {} by user: {}", 
                userId, teamId, userDetails.getUsername());
        
        UUID inviterId = userService.getUserIdByUsername(userDetails.getUsername());
        TeamInvitationDTO invitation = invitationService.inviteUserById(teamId, userId, message, inviterId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TeamInvitationDTO>> bulkInviteUsers(
            @PathVariable UUID teamId,
            @RequestParam List<UUID> userIds,
            @RequestParam(required = false) String message,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Bulk inviting users {} to team: {} by user: {}", 
                userIds, teamId, userDetails.getUsername());
        
        UUID inviterId = userService.getUserIdByUsername(userDetails.getUsername());
        List<TeamInvitationDTO> invitations = invitationService.bulkInviteUsers(teamId, userIds, message, inviterId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(invitations);
    }

    @GetMapping("/statistics")
    public ResponseEntity<InvitationStatisticsDTO> getInvitationStatistics(
            @PathVariable UUID teamId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting invitation statistics for team: {}", teamId);
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        InvitationStatisticsDTO statistics = invitationService.getInvitationStatistics(teamId, userId);
        
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveInvitationsCount(
            @PathVariable UUID teamId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting active invitations count for team: {}", teamId);
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        long count = invitationService.getTeamActiveInvitationsCount(teamId, userId);
        
        return ResponseEntity.ok(count);
    }

    @GetMapping("/check-limit")
    public ResponseEntity<Boolean> checkInvitationLimit(@PathVariable UUID teamId) {
        log.debug("Checking invitation limit for team: {}", teamId);
        
        boolean canInvite = invitationService.checkTeamInvitationLimit(teamId);
        return ResponseEntity.ok(canInvite);
    }
}

/**
 * REST контроллер для управления приглашениями пользователя
 */
@RestController
@RequestMapping("/api/users/invitations")
@RequiredArgsConstructor
@Slf4j
class UserInvitationController {

    private final TeamInvitationService invitationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<InvitationListResponse> getUserInvitations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting invitations for user: {} with pagination: page={}, size={}, sortBy={}, sortDir={}", 
                userDetails.getUsername(), page, size, sortBy, sortDir);

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        InvitationListResponse response = invitationService.getUserInvitations(userId, pageable);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<TeamInvitationDTO>> getUserActiveInvitations(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting active invitations for user: {}", userDetails.getUsername());
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        List<TeamInvitationDTO> invitations = invitationService.getUserActiveInvitations(userId);
        
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveInvitationsCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting active invitations count for user: {}", userDetails.getUsername());
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        long count = invitationService.getUserActiveInvitationsCount(userId);
        
        return ResponseEntity.ok(count);
    }

    @GetMapping("/sent")
    public ResponseEntity<List<TeamInvitationDTO>> getInvitationsSentByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting invitations sent by user: {} with pagination: page={}, size={}", 
                userDetails.getUsername(), page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        List<TeamInvitationDTO> invitations = invitationService.getInvitationsSentByUser(userId, pageable);
        
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<TeamInvitationDTO>> getRecentInvitations(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting recent invitations for user: {} with limit: {}", userDetails.getUsername(), limit);
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        List<TeamInvitationDTO> invitations = invitationService.getRecentInvitations(userId, limit);
        
        return ResponseEntity.ok(invitations);
    }
}

/**
 * REST контроллер для управления отдельными приглашениями
 */
@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Slf4j
class InvitationController {

    private final TeamInvitationService invitationService;
    private final UserService userService;

    @GetMapping("/{invitationId}")
    public ResponseEntity<TeamInvitationDTO> getInvitationById(
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Getting invitation: {} by user: {}", invitationId, userDetails.getUsername());
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        TeamInvitationDTO invitation = invitationService.getInvitationById(invitationId, userId);
        
        return ResponseEntity.ok(invitation);
    }

    @PutMapping("/{invitationId}/respond")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TeamInvitationDTO> respondToInvitation(
            @PathVariable UUID invitationId,
            @Valid @RequestBody RespondToInvitationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Responding to invitation: {} by user: {} with accept: {}", 
                invitationId, userDetails.getUsername(), request.getAccept());
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        TeamInvitationDTO invitation = invitationService.respondToInvitation(invitationId, request, userId);
        
        return ResponseEntity.ok(invitation);
    }

    @PutMapping("/{invitationId}/accept")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TeamInvitationDTO> acceptInvitation(
            @PathVariable UUID invitationId,
            @RequestParam(required = false) String message,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Accepting invitation: {} by user: {}", invitationId, userDetails.getUsername());
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        TeamInvitationDTO invitation = invitationService.acceptInvitation(invitationId, userId, message);
        
        return ResponseEntity.ok(invitation);
    }

    @PutMapping("/{invitationId}/decline")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TeamInvitationDTO> declineInvitation(
            @PathVariable UUID invitationId,
            @RequestParam(required = false) String message,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Declining invitation: {} by user: {}", invitationId, userDetails.getUsername());
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        TeamInvitationDTO invitation = invitationService.declineInvitation(invitationId, userId, message);
        
        return ResponseEntity.ok(invitation);
    }

    @DeleteMapping("/{invitationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> revokeInvitation(
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Revoking invitation: {} by user: {}", invitationId, userDetails.getUsername());
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        invitationService.revokeInvitation(invitationId, userId);
        
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{invitationId}/reminder")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> sendInvitationReminder(
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Sending reminder for invitation: {} by user: {}", invitationId, userDetails.getUsername());
        
        invitationService.sendInvitationReminder(invitationId);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{invitationId}/can-respond")
    public ResponseEntity<Boolean> canRespondToInvitation(
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Checking if user {} can respond to invitation: {}", userDetails.getUsername(), invitationId);
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        boolean canRespond = invitationService.canRespondToInvitation(invitationId, userId);
        
        return ResponseEntity.ok(canRespond);
    }

    @GetMapping("/{invitationId}/can-revoke")
    public ResponseEntity<Boolean> canRevokeInvitation(
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Checking if user {} can revoke invitation: {}", userDetails.getUsername(), invitationId);
        
        UUID userId = userService.getUserIdByUsername(userDetails.getUsername());
        boolean canRevoke = invitationService.canRevokeInvitation(invitationId, userId);
        
        return ResponseEntity.ok(canRevoke);
    }
}

/**
 * REST контроллер для административных функций приглашений
 */
@RestController
@RequestMapping("/api/admin/invitations")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
class AdminInvitationController {

    private final TeamInvitationService invitationService;

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<TeamInvitationDTO>> getInvitationsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting invitations by status: {} with pagination: page={}, size={}", status, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<TeamInvitationDTO> invitations = invitationService.getInvitationsByStatus(status, pageable);
        
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/by-period")
    public ResponseEntity<List<TeamInvitationDTO>> getInvitationsByPeriod(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting invitations by period: {} to {} with pagination: page={}, size={}", 
                startDate, endDate, page, size);
        
        Instant start = Instant.parse(startDate);
        Instant end = Instant.parse(endDate);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        List<TeamInvitationDTO> invitations = invitationService.getInvitationsByPeriod(start, end, pageable);
        
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/by-message")
    public ResponseEntity<List<TeamInvitationDTO>> getInvitationsByMessage(
            @RequestParam String message,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting invitations by message: {} with pagination: page={}, size={}", message, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<TeamInvitationDTO> invitations = invitationService.getInvitationsByMessage(message, pageable);
        
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<TeamInvitationDTO>> getInvitationsExpiringSoon(
            @RequestParam(defaultValue = "24") int hours) {
        
        log.debug("Getting invitations expiring in {} hours", hours);
        
        List<TeamInvitationDTO> invitations = invitationService.getInvitationsExpiringSoon(hours);
        
        return ResponseEntity.ok(invitations);
    }

    @PostMapping("/update-expired")
    public ResponseEntity<Integer> updateExpiredInvitations() {
        log.debug("Updating expired invitations");
        
        int updatedCount = invitationService.updateExpiredInvitations();
        
        return ResponseEntity.ok(updatedCount);
    }

    @DeleteMapping("/old")
    public ResponseEntity<Integer> deleteOldInvitations(
            @RequestParam(defaultValue = "30") int daysOld) {
        
        log.debug("Deleting invitations older than {} days", daysOld);
        
        int deletedCount = invitationService.deleteOldInvitations(daysOld);
        
        return ResponseEntity.ok(deletedCount);
    }

    @GetMapping("/statistics/global")
    public ResponseEntity<GlobalInvitationStatisticsDTO> getGlobalInvitationStatistics() {
        log.debug("Getting global invitation statistics");
        
        GlobalInvitationStatisticsDTO statistics = invitationService.getGlobalInvitationStatistics();
        
        return ResponseEntity.ok(statistics);
    }
}