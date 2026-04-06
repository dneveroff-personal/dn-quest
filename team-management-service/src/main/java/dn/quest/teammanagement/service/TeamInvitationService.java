package dn.quest.teammanagement.service;

import dn.quest.teammanagement.dto.GlobalInvitationStatisticsDTO;
import dn.quest.teammanagement.dto.InvitationStatisticsDTO;
import dn.quest.teammanagement.dto.TeamInvitationDTO;
import dn.quest.teammanagement.dto.request.InviteUserRequest;
import dn.quest.teammanagement.dto.request.RespondToInvitationRequest;
import dn.quest.teammanagement.dto.response.InvitationListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления приглашениями в команды
 */
public interface TeamInvitationService {

    /**
     * Пригласить пользователя в команду
     */
    TeamInvitationDTO inviteUser(UUID teamId, InviteUserRequest request, UUID inviterId);

    /**
     * Пригласить пользователя по ID
     */
    TeamInvitationDTO inviteUserById(UUID teamId, UUID userId, String message, UUID inviterId);

    /**
     * Ответить на приглашение
     */
    TeamInvitationDTO respondToInvitation(UUID invitationId, RespondToInvitationRequest request, UUID userId);

    /**
     * Принять приглашение
     */
    TeamInvitationDTO acceptInvitation(UUID invitationId, UUID userId, String message);

    /**
     * Отклонить приглашение
     */
    TeamInvitationDTO declineInvitation(UUID invitationId, UUID userId, String message);

    /**
     * Отозвать приглашение
     */
    void revokeInvitation(UUID invitationId, UUID revokerId);

    /**
     * Получить приглашение по ID
     */
    TeamInvitationDTO getInvitationById(UUID invitationId, UUID userId);

    /**
     * Получить приглашения пользователя
     */
    InvitationListResponse getUserInvitations(UUID userId, Pageable pageable);

    /**
     * Получить активные приглашения пользователя
     */
    List<TeamInvitationDTO> getUserActiveInvitations(UUID userId);

    /**
     * Получить приглашения команды
     */
    InvitationListResponse getTeamInvitations(UUID teamId, UUID requesterId, Pageable pageable);

    /**
     * Получить активные приглашения команды
     */
    List<TeamInvitationDTO> getTeamActiveInvitations(UUID teamId, UUID requesterId);

    /**
     * Получить приглашения, отправленные пользователем
     */
    List<TeamInvitationDTO> getInvitationsSentByUser(UUID userId, Pageable pageable);

    /**
     * Получить количество активных приглашений пользователя
     */
    long getUserActiveInvitationsCount(UUID userId);

    /**
     * Получить количество активных приглашений команды
     */
    long getTeamActiveInvitationsCount(UUID teamId, UUID requesterId);

    /**
     * Проверить, существует ли активное приглашение
     */
    boolean hasActiveInvitation(UUID teamId, UUID userId);

    /**
     * Проверить, может ли пользователь ответить на приглашение
     */
    boolean canRespondToInvitation(UUID invitationId, UUID userId);

    /**
     * Проверить, может ли пользователь отозвать приглашение
     */
    boolean canRevokeInvitation(UUID invitationId, UUID userId);

    /**
     * Обновить статус истекших приглашений
     */
    int updateExpiredInvitations();

    /**
     * Удалить старые приглашения
     */
    int deleteOldInvitations(int daysOld);

    /**
     * Получить приглашения по статусу
     */
    List<TeamInvitationDTO> getInvitationsByStatus(String status, Pageable pageable);

    /**
     * Получить статистику по приглашениям
     */
    InvitationStatisticsDTO getInvitationStatistics(UUID teamId, UUID requesterId);

    /**
     * Получить общую статистику по приглашениям
     */
    GlobalInvitationStatisticsDTO getGlobalInvitationStatistics();

    /**
     * Массовая отправка приглашений
     */
    List<TeamInvitationDTO> bulkInviteUsers(UUID teamId, List<UUID> userIds, String message, UUID inviterId);

    /**
     * Массовый отзыв приглашений
     */
    void bulkRevokeInvitations(UUID teamId, List<UUID> invitationIds, UUID revokerId);

    /**
     * Получить приглашения, созданные за период
     */
    List<TeamInvitationDTO> getInvitationsByPeriod(java.time.Instant startDate, 
                                                   java.time.Instant endDate, 
                                                   Pageable pageable);

    /**
     * Получить приглашения с определенным сообщением
     */
    List<TeamInvitationDTO> getInvitationsByMessage(String message, Pageable pageable);

    /**
     * Получить последние приглашения пользователя
     */
    List<TeamInvitationDTO> getRecentInvitations(UUID userId, int limit);

    /**
     * Проверить лимит приглашений для команды
     */
    boolean checkTeamInvitationLimit(UUID teamId);

    /**
     * Проверить лимит приглашений для пользователя
     */
    boolean checkUserInvitationLimit(UUID userId);

    /**
     * Получить приглашения, которые скоро истекут
     */
    List<TeamInvitationDTO> getInvitationsExpiringSoon(int hours);

    /**
     * Отправить напоминание о приглашении
     */
    void sendInvitationReminder(UUID invitationId);

    /**
     * Получить DTO для Entity
     */
    TeamInvitationDTO toDTO(dn.quest.teammanagement.entity.TeamInvitation invitation);
}
