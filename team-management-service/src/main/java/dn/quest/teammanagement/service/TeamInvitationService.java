package dn.quest.teammanagement.service;

import dn.quest.teammanagement.dto.GlobalInvitationStatisticsDTO;
import dn.quest.teammanagement.dto.InvitationStatisticsDTO;
import dn.quest.teammanagement.dto.TeamInvitationDTO;
import dn.quest.teammanagement.dto.request.InviteUserRequest;
import dn.quest.teammanagement.dto.request.RespondToInvitationRequest;
import dn.quest.teammanagement.dto.response.InvitationListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Сервис для управления приглашениями в команды
 */
public interface TeamInvitationService {

    /**
     * Пригласить пользователя в команду
     */
    TeamInvitationDTO inviteUser(Long teamId, InviteUserRequest request, Long inviterId);

    /**
     * Пригласить пользователя по ID
     */
    TeamInvitationDTO inviteUserById(Long teamId, Long userId, String message, Long inviterId);

    /**
     * Ответить на приглашение
     */
    TeamInvitationDTO respondToInvitation(Long invitationId, RespondToInvitationRequest request, Long userId);

    /**
     * Принять приглашение
     */
    TeamInvitationDTO acceptInvitation(Long invitationId, Long userId, String message);

    /**
     * Отклонить приглашение
     */
    TeamInvitationDTO declineInvitation(Long invitationId, Long userId, String message);

    /**
     * Отозвать приглашение
     */
    void revokeInvitation(Long invitationId, Long revokerId);

    /**
     * Получить приглашение по ID
     */
    TeamInvitationDTO getInvitationById(Long invitationId, Long userId);

    /**
     * Получить приглашения пользователя
     */
    InvitationListResponse getUserInvitations(Long userId, Pageable pageable);

    /**
     * Получить активные приглашения пользователя
     */
    List<TeamInvitationDTO> getUserActiveInvitations(Long userId);

    /**
     * Получить приглашения команды
     */
    InvitationListResponse getTeamInvitations(Long teamId, Long requesterId, Pageable pageable);

    /**
     * Получить активные приглашения команды
     */
    List<TeamInvitationDTO> getTeamActiveInvitations(Long teamId, Long requesterId);

    /**
     * Получить приглашения, отправленные пользователем
     */
    List<TeamInvitationDTO> getInvitationsSentByUser(Long userId, Pageable pageable);

    /**
     * Получить количество активных приглашений пользователя
     */
    long getUserActiveInvitationsCount(Long userId);

    /**
     * Получить количество активных приглашений команды
     */
    long getTeamActiveInvitationsCount(Long teamId, Long requesterId);

    /**
     * Проверить, существует ли активное приглашение
     */
    boolean hasActiveInvitation(Long teamId, Long userId);

    /**
     * Проверить, может ли пользователь ответить на приглашение
     */
    boolean canRespondToInvitation(Long invitationId, Long userId);

    /**
     * Проверить, может ли пользователь отозвать приглашение
     */
    boolean canRevokeInvitation(Long invitationId, Long userId);

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
    InvitationStatisticsDTO getInvitationStatistics(Long teamId, Long requesterId);

    /**
     * Получить общую статистику по приглашениям
     */
    GlobalInvitationStatisticsDTO getGlobalInvitationStatistics();

    /**
     * Массовая отправка приглашений
     */
    List<TeamInvitationDTO> bulkInviteUsers(Long teamId, List<Long> userIds, String message, Long inviterId);

    /**
     * Массовый отзыв приглашений
     */
    void bulkRevokeInvitations(Long teamId, List<Long> invitationIds, Long revokerId);

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
    List<TeamInvitationDTO> getRecentInvitations(Long userId, int limit);

    /**
     * Проверить лимит приглашений для команды
     */
    boolean checkTeamInvitationLimit(Long teamId);

    /**
     * Проверить лимит приглашений для пользователя
     */
    boolean checkUserInvitationLimit(Long userId);

    /**
     * Получить приглашения, которые скоро истекут
     */
    List<TeamInvitationDTO> getInvitationsExpiringSoon(int hours);

    /**
     * Отправить напоминание о приглашении
     */
    void sendInvitationReminder(Long invitationId);

    /**
     * Получить DTO для Entity
     */
    TeamInvitationDTO toDTO(dn.quest.teammanagement.entity.TeamInvitation invitation);
}
