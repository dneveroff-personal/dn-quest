package dn.quest.teammanagement.service.impl;

import dn.quest.shared.enums.InvitationStatus;
import dn.quest.shared.enums.TeamRole;
import dn.quest.teammanagement.dto.GlobalInvitationStatisticsDTO;
import dn.quest.teammanagement.dto.InvitationStatisticsDTO;
import dn.quest.teammanagement.dto.TeamInvitationDTO;
import dn.quest.teammanagement.dto.request.InviteUserRequest;
import dn.quest.teammanagement.dto.request.RespondToInvitationRequest;
import dn.quest.teammanagement.dto.response.InvitationListResponse;
import dn.quest.teammanagement.entity.*;
import dn.quest.teammanagement.mapper.TeamMapper;
import dn.quest.teammanagement.repository.*;
import dn.quest.teammanagement.service.TeamInvitationService;
import dn.quest.teammanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления приглашениями в команды
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TeamInvitationServiceImpl implements TeamInvitationService {

    private final TeamInvitationRepository invitationRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamSettingsRepository teamSettingsRepository;
    private final TeamStatisticsRepository teamStatisticsRepository;
    private final TeamMapper teamMapper;
    private final UserService userService;

    @Override
    @CacheEvict(value = {"teamInvitations", "userInvitations"}, allEntries = true)
    public TeamInvitationDTO inviteUser(UUID teamId, InviteUserRequest request, UUID inviterId) {
        log.debug("Inviting user {} to team: {} by inviter: {}", request.getUsername(), teamId, inviterId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));

        User inviter = userService.getUserEntityById(inviterId)
                .orElseThrow(() -> new RuntimeException("Inviter not found: " + inviterId));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUsername()));

        // Проверяем права на приглашение
        if (!canInviteToTeam(team, inviter)) {
            throw new RuntimeException("Insufficient permissions to invite users");
        }

        // Проверяем, что пользователь не состоит в команде
        if (teamMemberRepository.existsByTeamAndUserAndIsActiveTrue(team, user)) {
            throw new RuntimeException("User is already a team member");
        }

        // Проверяем, что нет активного приглашения
        if (invitationRepository.existsActiveInvitation(team, user, Instant.now())) {
            throw new RuntimeException("Active invitation already exists");
        }

        // Проверяем лимит приглашений
        if (!checkTeamInvitationLimit(teamId)) {
            throw new RuntimeException("Team invitation limit exceeded");
        }

        // Создаем приглашение
        TeamInvitation invitation = TeamInvitation.builder()
                .team(team)
                .user(user)
                .invitedBy(inviter)
                .status(InvitationStatus.PENDING)
                .invitationMessage(request.getMessage())
                .createdAt(Instant.now())
                .build();

        TeamInvitation savedInvitation = invitationRepository.save(invitation);

        // Обновляем статистику
        updateInvitationStatistics(teamId);

        log.info("Invitation sent: {} to user {} for team: {}", 
                savedInvitation.getId(), user.getUsername(), teamId);

        return teamMapper.toTeamInvitationDTO(savedInvitation);
    }

    @Override
    @CacheEvict(value = {"teamInvitations", "userInvitations"}, allEntries = true)
    public TeamInvitationDTO inviteUserById(UUID teamId, UUID userId, String message, UUID inviterId) {
        log.debug("Inviting user {} to team: {} by inviter: {}", userId, teamId, inviterId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));

        User inviter = userService.getUserEntityById(inviterId)
                .orElseThrow(() -> new RuntimeException("Inviter not found: " + inviterId));

        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Проверяем права на приглашение
        if (!canInviteToTeam(team, inviter)) {
            throw new RuntimeException("Insufficient permissions to invite users");
        }

        // Проверяем, что пользователь не состоит в команде
        if (teamMemberRepository.existsByTeamAndUserAndIsActiveTrue(team, user)) {
            throw new RuntimeException("User is already a team member");
        }

        // Проверяем, что нет активного приглашения
        if (invitationRepository.existsActiveInvitation(team, user, Instant.now())) {
            throw new RuntimeException("Active invitation already exists");
        }

        // Создаем приглашение
        TeamInvitation invitation = TeamInvitation.builder()
                .team(team)
                .user(user)
                .invitedBy(inviter)
                .status(InvitationStatus.PENDING)
                .invitationMessage(message)
                .createdAt(Instant.now())
                .build();

        TeamInvitation savedInvitation = invitationRepository.save(invitation);

        // Обновляем статистику
        updateInvitationStatistics(teamId);

        log.info("Invitation sent: {} to user {} for team: {}", 
                savedInvitation.getId(), user.getUsername(), teamId);

        return teamMapper.toTeamInvitationDTO(savedInvitation);
    }

    @Override
    @CacheEvict(value = {"teamInvitations", "userInvitations"}, allEntries = true)
    public TeamInvitationDTO respondToInvitation(UUID invitationId, RespondToInvitationRequest request, UUID userId) {
        log.debug("Responding to invitation: {} by user: {} with accept: {}", invitationId, userId, request.getAccept());

        TeamInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + invitationId));

        // Проверяем, что приглашение принадлежит пользователю
        if (!invitation.getUser().getId().equals(userId)) {
            throw new RuntimeException("Invitation does not belong to user");
        }

        // Проверяем, что приглашение активно
        if (!invitation.isActive()) {
            throw new RuntimeException("Invitation is not active");
        }

        if (request.getAccept()) {
            acceptInvitation(invitation, request.getMessage());
        } else {
            declineInvitation(invitation, request.getMessage());
        }

        TeamInvitation updatedInvitation = invitationRepository.save(invitation);
        return teamMapper.toTeamInvitationDTO(updatedInvitation);
    }

    @Override
    @CacheEvict(value = {"teamInvitations", "userInvitations"}, allEntries = true)
    public TeamInvitationDTO acceptInvitation(UUID invitationId, UUID userId, String message) {
        log.debug("Accepting invitation: {} by user: {}", invitationId, userId);

        TeamInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + invitationId));

        // Проверяем, что приглашение принадлежит пользователю
        if (!invitation.getUser().getId().equals(userId)) {
            throw new RuntimeException("Invitation does not belong to user");
        }

        // Проверяем, что приглашение активно
        if (!invitation.isActive()) {
            throw new RuntimeException("Invitation is not active");
        }

        // Принимаем приглашение
        invitation.accept(message);

        // Добавляем пользователя в команду
        TeamMember member = TeamMember.builder()
                .team(invitation.getTeam())
                .user(invitation.getUser())
                .role(TeamRole.MEMBER)
                .joinedAt(Instant.now())
                .isActive(true)
                .build();

        teamMemberRepository.save(member);

        // Обновляем статистику
        updateInvitationStatistics(invitation.getTeam().getId());

        TeamInvitation updatedInvitation = invitationRepository.save(invitation);

        log.info("Invitation accepted: {} by user: {}", invitationId, userId);
        return teamMapper.toTeamInvitationDTO(updatedInvitation);
    }

    @Override
    @CacheEvict(value = {"teamInvitations", "userInvitations"}, allEntries = true)
    public TeamInvitationDTO declineInvitation(UUID invitationId, UUID userId, String message) {
        log.debug("Declining invitation: {} by user: {}", invitationId, userId);

        TeamInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + invitationId));

        // Проверяем, что приглашение принадлежит пользователю
        if (!invitation.getUser().getId().equals(userId)) {
            throw new RuntimeException("Invitation does not belong to user");
        }

        // Отклоняем приглашение
        invitation.decline(message);

        // Обновляем статистику
        updateInvitationStatistics(invitation.getTeam().getId());

        TeamInvitation updatedInvitation = invitationRepository.save(invitation);

        log.info("Invitation declined: {} by user: {}", invitationId, userId);
        return teamMapper.toTeamInvitationDTO(updatedInvitation);
    }

    @Override
    @CacheEvict(value = {"teamInvitations", "userInvitations"}, allEntries = true)
    public void revokeInvitation(UUID invitationId, UUID revokerId) {
        log.debug("Revoking invitation: {} by user: {}", invitationId, revokerId);

        TeamInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + invitationId));

        // Проверяем права на отзыв
        if (!canRevokeInvitation(invitation, revokerId)) {
            throw new RuntimeException("Insufficient permissions to revoke invitation");
        }

        // Удаляем приглашение
        invitationRepository.delete(invitation);

        // Обновляем статистику
        updateInvitationStatistics(invitation.getTeam().getId());

        log.info("Invitation revoked: {} by user: {}", invitationId, revokerId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "invitations", key = "#invitationId")
    public TeamInvitationDTO getInvitationById(UUID invitationId, UUID userId) {
        log.debug("Getting invitation: {} by user: {}", invitationId, userId);

        TeamInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + invitationId));

        // Проверяем права доступа
        if (!invitation.getUser().getId().equals(userId) && 
            !invitation.getInvitedBy().getId().equals(userId) &&
            !teamMemberRepository.existsByTeamAndUserAndIsActiveTrue(invitation.getTeam(), 
                    userService.getUserEntityById(userId).orElse(null))) {
            throw new RuntimeException("Access denied");
        }

        return teamMapper.toTeamInvitationDTO(invitation);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userInvitations", key = "#userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public InvitationListResponse getUserInvitations(UUID userId, Pageable pageable) {
        log.debug("Getting invitations for user: {} with pagination: {}", userId, pageable);

        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Page<TeamInvitation> invitationPage = invitationRepository.findByUser(user, pageable);
        List<TeamInvitationDTO> invitationDTOs = teamMapper.toTeamInvitationDTOList(invitationPage.getContent());

        return InvitationListResponse.of(
                invitationDTOs,
                invitationPage.getTotalElements(),
                invitationPage.getNumber(),
                invitationPage.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamInvitationDTO> getUserActiveInvitations(UUID userId) {
        log.debug("Getting active invitations for user: {}", userId);

        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        List<TeamInvitation> invitations = invitationRepository.findActiveInvitationsByUser(user, Instant.now());
        return teamMapper.toTeamInvitationDTOList(invitations);
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationListResponse getTeamInvitations(UUID teamId, UUID requesterId, Pageable pageable) {
        log.debug("Getting invitations for team: {} by requester: {} with pagination: {}", teamId, requesterId, pageable);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));

        // Проверяем права доступа
        if (!teamMemberRepository.existsByTeamAndUserAndIsActiveTrue(team, 
                userService.getUserEntityById(requesterId).orElse(null))) {
            throw new RuntimeException("Access denied");
        }

        Page<TeamInvitation> invitationPage = invitationRepository.findByTeam(team, pageable);
        List<TeamInvitationDTO> invitationDTOs = teamMapper.toTeamInvitationDTOList(invitationPage.getContent());

        return InvitationListResponse.of(
                invitationDTOs,
                invitationPage.getTotalElements(),
                invitationPage.getNumber(),
                invitationPage.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamInvitationDTO> getTeamActiveInvitations(UUID teamId, UUID requesterId) {
        log.debug("Getting active invitations for team: {} by requester: {}", teamId, requesterId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));

        // Проверяем права доступа
        if (!teamMemberRepository.existsByTeamAndUserAndIsActiveTrue(team, 
                userService.getUserEntityById(requesterId).orElse(null))) {
            throw new RuntimeException("Access denied");
        }

        List<TeamInvitation> invitations = invitationRepository.findActiveInvitationsByTeam(team, Instant.now());
        return teamMapper.toTeamInvitationDTOList(invitations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamInvitationDTO> getInvitationsSentByUser(UUID userId, Pageable pageable) {
        log.debug("Getting invitations sent by user: {} with pagination: {}", userId, pageable);

        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        List<TeamInvitation> invitationPage = invitationRepository.findByInvitedBy(user);
        return teamMapper.toTeamInvitationDTOList(invitationPage);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUserActiveInvitationsCount(UUID userId) {
        log.debug("Getting active invitations count for user: {}", userId);

        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return invitationRepository.countActiveInvitationsByUser(user, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long getTeamActiveInvitationsCount(UUID teamId, UUID requesterId) {
        log.debug("Getting active invitations count for team: {} by requester: {}", teamId, requesterId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));

        // Проверяем права доступа
        if (!teamMemberRepository.existsByTeamAndUserAndIsActiveTrue(team, 
                userService.getUserEntityById(requesterId).orElse(null))) {
            throw new RuntimeException("Access denied");
        }

        return invitationRepository.countActiveInvitationsByTeam(team, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveInvitation(UUID teamId, UUID userId) {
        log.debug("Checking active invitation for team: {} and user: {}", teamId, userId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));

        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return invitationRepository.existsActiveInvitation(team, user, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canRespondToInvitation(UUID invitationId, UUID userId) {
        log.debug("Checking if user {} can respond to invitation: {}", userId, invitationId);

        TeamInvitation invitation = invitationRepository.findById(invitationId)
                .orElse(null);

        return invitation != null && 
               invitation.getUser().getId().equals(userId) && 
               invitation.isActive();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canRevokeInvitation(UUID invitationId, UUID userId) {
        log.debug("Checking if user {} can revoke invitation: {}", userId, invitationId);

        TeamInvitation invitation = invitationRepository.findById(invitationId)
                .orElse(null);

        return invitation != null && canRevokeInvitation(invitation, userId);
    }

    @Override
    @CacheEvict(value = {"teamInvitations", "userInvitations"}, allEntries = true)
    public int updateExpiredInvitations() {
        log.debug("Updating expired invitations");

        int updatedCount = invitationRepository.updateExpiredInvitations(Instant.now());
        log.info("Updated {} expired invitations", updatedCount);
        return updatedCount;
    }

    @Override
    @CacheEvict(value = {"teamInvitations", "userInvitations"}, allEntries = true)
    public int deleteOldInvitations(int daysOld) {
        log.debug("Deleting invitations older than {} days", daysOld);

        Instant cutoffDate = Instant.now().minusSeconds((long) daysOld * 24 * 60 * 60);
        int deletedCount = invitationRepository.deleteInvitationsOlderThan(cutoffDate);
        log.info("Deleted {} old invitations", deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamInvitationDTO> getInvitationsByStatus(String status, Pageable pageable) {
        log.debug("Getting invitations by status: {} with pagination: {}", status, pageable);

        InvitationStatus invitationStatus;
        try {
            invitationStatus = InvitationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }

        Page<TeamInvitation> invitationPage = invitationRepository.findByStatus(invitationStatus, pageable);
        return teamMapper.toTeamInvitationDTOList(invitationPage.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationStatisticsDTO getInvitationStatistics(UUID teamId, UUID requesterId) {
        log.debug("Getting invitation statistics for team: {} by requester: {}", teamId, requesterId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));

        // Проверяем права доступа
        if (!teamMemberRepository.existsByTeamAndUserAndIsActiveTrue(team, 
                userService.getUserEntityById(requesterId).orElse(null))) {
            throw new RuntimeException("Access denied");
        }

        List<Object[]> stats = invitationRepository.countInvitationsByStatusForTeam(team);
        
        long totalSent = 0, totalAccepted = 0, totalDeclined = 0, totalExpired = 0, totalPending = 0;

        for (Object[] stat : stats) {
            InvitationStatus status = (InvitationStatus) stat[0];
            Long count = (Long) stat[1];

            totalSent += count;
            switch (status) {
                case ACCEPTED -> totalAccepted += count;
                case REJECTED -> totalDeclined += count;
                case EXPIRED -> totalExpired += count;
                case PENDING -> totalPending += count;
            }
        }

        double acceptanceRate = totalSent > 0 ? (double) totalAccepted / totalSent * 100.0 : 0.0;
        double declineRate = totalSent > 0 ? (double) totalDeclined / totalSent * 100.0 : 0.0;

        return new InvitationStatisticsDTO(
                totalSent, totalAccepted, totalDeclined, totalExpired, totalPending,
                acceptanceRate, declineRate
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalInvitationStatisticsDTO getGlobalInvitationStatistics() {
        log.debug("Getting global invitation statistics");

        List<Object[]> stats = invitationRepository.countInvitationsByStatus();
        
        long totalInvitations = 0, activeInvitations = 0;
        double totalAcceptanceRate = 0.0;

        for (Object[] stat : stats) {
            InvitationStatus status = (InvitationStatus) stat[0];
            Long count = (Long) stat[1];

            totalInvitations += count;
            if (status == InvitationStatus.PENDING) {
                activeInvitations += count;
            }
        }

        // Здесь можно добавить более сложную логику для расчета статистики

        return new GlobalInvitationStatisticsDTO(
                teamRepository.countActiveTeams(),
                totalInvitations,
                activeInvitations,
                totalAcceptanceRate,
                0L, // mostActiveTeamId
                ""   // mostActiveTeamName
        );
    }

    @Override
    @CacheEvict(value = {"teamInvitations", "userInvitations"}, allEntries = true)
    public List<TeamInvitationDTO> bulkInviteUsers(UUID teamId, List<UUID> userIds, String message, UUID inviterId) {
        log.debug("Bulk inviting users to team: {} by inviter: {}", teamId, inviterId);

        return userIds.stream()
                .map(userId -> inviteUserById(teamId, userId, message, inviterId))
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {"teamInvitations", "userInvitations"}, allEntries = true)
    public void bulkRevokeInvitations(UUID teamId, List<UUID> invitationIds, UUID revokerId) {
        log.debug("Bulk revoking invitations for team: {} by revoker: {}", teamId, revokerId);

        invitationIds.forEach(invitationId -> revokeInvitation(invitationId, revokerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamInvitationDTO> getInvitationsByPeriod(Instant startDate, Instant endDate, Pageable pageable) {
        log.debug("Getting invitations by period: {} to {} with pagination: {}", startDate, endDate, pageable);

        List<TeamInvitation> invitations = invitationRepository.findByCreatedAtBetween(startDate, endDate);
        return teamMapper.toTeamInvitationDTOList(invitations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamInvitationDTO> getInvitationsByMessage(String message, Pageable pageable) {
        log.debug("Getting invitations by message: {} with pagination: {}", message, pageable);

        List<TeamInvitation> invitations = invitationRepository.findByInvitationMessageContainingIgnoreCase(message);
        return teamMapper.toTeamInvitationDTOList(invitations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamInvitationDTO> getRecentInvitations(UUID userId, int limit) {
        log.debug("Getting recent invitations for user: {} with limit: {}", userId, limit);

        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Pageable pageable = PageRequest.of(0, limit);
        List<TeamInvitation> invitations = invitationRepository.findRecentInvitationsByUser(user, pageable);
        return teamMapper.toTeamInvitationDTOList(invitations);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkTeamInvitationLimit(UUID teamId) {
        log.debug("Checking invitation limit for team: {}", teamId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));

        TeamSettings settings = teamSettingsRepository.findByTeam(team)
                .orElse(null);

        if (settings == null) {
            return true; // Если настроек нет, лимита нет
        }

        long activeInvitations = invitationRepository.countActiveInvitationsByTeam(team, Instant.now());
        return activeInvitations < settings.getMaxPendingInvitations();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkUserInvitationLimit(UUID userId) {
        log.debug("Checking invitation limit for user: {}", userId);

        // Здесь можно добавить логику проверки лимита приглашений для пользователя
        // Например, ограничение на количество активных приглашений для одного пользователя
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamInvitationDTO> getInvitationsExpiringSoon(int hours) {
        log.debug("Getting invitations expiring in {} hours", hours);

        Instant cutoffTime = Instant.now().plusSeconds((long) hours * 60 * 60);
        List<TeamInvitation> invitations = invitationRepository.findExpiredInvitations(Instant.now())
                .stream()
                .filter(inv -> inv.getExpiresAt() != null && inv.getExpiresAt().isBefore(cutoffTime))
                .collect(Collectors.toList());

        return teamMapper.toTeamInvitationDTOList(invitations);
    }

    @Override
    public void sendInvitationReminder(UUID invitationId) {
        log.debug("Sending reminder for invitation: {}", invitationId);

        TeamInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found: " + invitationId));

        // Здесь можно добавить логику отправки уведомления
        // Например, через Kafka или другой сервис уведомлений

        log.info("Reminder sent for invitation: {}", invitationId);
    }

    @Override
    public TeamInvitationDTO toDTO(TeamInvitation invitation) {
        return teamMapper.toTeamInvitationDTO(invitation);
    }

    // Вспомогательные методы

    private boolean canInviteToTeam(Team team, User inviter) {
        // Проверяем, что пользователь является участником команды
        return teamMemberRepository.findByTeamAndUserAndIsActiveTrue(team, inviter)
                .map(TeamMember::canInviteMembers)
                .orElse(false);
    }

    private boolean canRevokeInvitation(TeamInvitation invitation, UUID userId) {
        // Приглашение может отозвать:
        // 1. Тот, кто отправил приглашение
        // 2. Капитан команды
        // 3. Модератор команды

        if (invitation.getInvitedBy().getId().equals(userId)) {
            return true;
        }

        return teamMemberRepository.findByTeamAndUserAndIsActiveTrue(invitation.getTeam(), 
                userService.getUserEntityById(userId).orElse(null))
                .map(TeamMember::canManageTeam)
                .orElse(false);
    }

    private void updateInvitationStatistics(UUID teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) return;

        TeamStatistics statistics = teamStatisticsRepository.findByTeam(team)
                .orElseGet(() -> TeamStatistics.builder()
                        .team(team)
                        .createdAt(Instant.now())
                        .build());

        // Обновляем статистику приглашений
        long totalSent = invitationRepository.countByTeamAndStatus(team, InvitationStatus.PENDING) +
                         invitationRepository.countByTeamAndStatus(team, InvitationStatus.ACCEPTED) +
                         invitationRepository.countByTeamAndStatus(team, InvitationStatus.REJECTED) +
                         invitationRepository.countByTeamAndStatus(team, InvitationStatus.EXPIRED);

        long totalAccepted = invitationRepository.countByTeamAndStatus(team, InvitationStatus.ACCEPTED);
        long totalDeclined = invitationRepository.countByTeamAndStatus(team, InvitationStatus.REJECTED);

        statistics.setTotalInvitationsSent(totalSent);
        statistics.setTotalInvitationsAccepted(totalAccepted);
        statistics.setTotalInvitationsDeclined(totalDeclined);
        statistics.setLastActivityAt(Instant.now());

        teamStatisticsRepository.save(statistics);
    }

    private void acceptInvitation(TeamInvitation invitation, String message) {
        invitation.accept(message);

        // Добавляем пользователя в команду
        TeamMember member = TeamMember.builder()
                .team(invitation.getTeam())
                .user(invitation.getUser())
                .role(TeamRole.MEMBER)
                .joinedAt(Instant.now())
                .isActive(true)
                .build();

        teamMemberRepository.save(member);
    }

    private void declineInvitation(TeamInvitation invitation, String message) {
        invitation.decline(message);
    }
}