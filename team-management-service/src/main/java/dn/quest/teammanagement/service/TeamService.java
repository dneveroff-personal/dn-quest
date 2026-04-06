package dn.quest.teammanagement.service;

import dn.quest.teammanagement.dto.*;
import dn.quest.teammanagement.dto.request.*;
import dn.quest.teammanagement.dto.response.TeamListResponse;
import dn.quest.teammanagement.entity.Team;
import dn.quest.teammanagement.entity.TeamMember;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления командами
 */
public interface TeamService {

    /**
     * Создать новую команду
     */
    TeamDTO createTeam(CreateTeamRequest request, UUID captainId);

    /**
     * Получить команду по ID
     */
    TeamDTO getTeamById(UUID teamId);

    /**
     * Получить полную информацию о команде
     */
    TeamDTO getFullTeamById(UUID teamId);

    /**
     * Обновить информацию о команде
     */
    TeamDTO updateTeam(UUID teamId, UpdateTeamRequest request, UUID userId);

    /**
     * Удалить команду
     */
    void deleteTeam(UUID teamId, UUID userId);

    /**
     * Получить список команд с пагинацией
     */
    TeamListResponse getTeams(Pageable pageable);

    /**
     * Поиск команд
     */
    TeamListResponse searchTeams(SearchTeamsRequest request, Pageable pageable);

    /**
     * Получить публичные команды
     */
    TeamListResponse getPublicTeams(Pageable pageable);

    /**
     * Получить топ команд по рейтингу
     */
    TeamListResponse getTopTeamsByRating(Pageable pageable);

    /**
     * Получить команды пользователя
     */
    List<TeamDTO> getUserTeams(UUID userId);

    /**
     * Получить команды пользователя постранично
     */
    TeamListResponse getUserTeams(UUID userId, Pageable pageable);

    /**
     * Получить активные команды пользователя
     */
    List<TeamDTO> getUserActiveTeams(UUID userId);

    /**
     * Проверить, является ли пользователь капитаном команды
     */
    boolean isTeamCaptain(UUID teamId, UUID userId);

    /**
     * Проверить, состоит ли пользователь в команде
     */
    boolean isTeamMember(UUID teamId, UUID userId);

    /**
     * Получить роль пользователя в команде
     */
    String getUserRoleInTeam(UUID teamId, UUID userId);

    /**
     * Проверить, может ли пользователь управлять командой
     */
    boolean canManageTeam(UUID teamId, UUID userId);

    /**
     * Передать права капитана
     */
    TeamMember transferCaptain(UUID teamId, UUID newCaptainId, UUID currentCaptainId);

    /**
     * Добавить участника в команду
     */
    TeamMember addMember(UUID teamId, UUID userId, UUID requesterId);

    /**
     * Удалить участника из команды
     */
    void removeMember(UUID teamId, UUID userId, UUID requesterId);

    /**
     * Изменить роль участника
     */
    TeamMember changeMemberRole(UUID teamId, UUID userId, String newRole, UUID requesterId);

    /**
     * Пригласить пользователя в команду
     */
    TeamInvitationDTO inviteUser(UUID teamId, InviteUserRequest request, Long inviterId);

    /**
     * Получить приглашения команды
     */
    List<TeamInvitationDTO> getTeamInvitations(UUID teamId, UUID userId);

    /**
     * Получить активные приглашения команды
     */
    List<TeamInvitationDTO> getTeamActiveInvitations(UUID teamId, UUID userId);

    /**
     * Отозвать приглашение
     */
    void revokeInvitation(UUID teamId, Long invitationId, UUID userId);

    /**
     * Получить настройки команды
     */
    TeamSettingsDTO getTeamSettings(UUID teamId);

    /**
     * Обновить настройки команды
     */
    TeamSettingsDTO updateTeamSettings(UUID teamId, UpdateTeamSettingsRequest request, UUID userId);

    /**
     * Получить статистику команды
     */
    TeamStatisticsDTO getTeamStatistics(UUID teamId);

    /**
     * Обновить статистику команды
     */
    void updateTeamStatistics(UUID teamId);

    /**
     * Получить участников команды
     */
    List<TeamMemberDTO> getTeamMembers(UUID teamId);

    /**
     * Получить активных участников команды
     */
    List<TeamMemberDTO> getTeamActiveMembers(UUID teamId, UUID userId);

    /**
     * Получить команды по названию
     */
    List<TeamDTO> getTeamsByName(String name);

    /**
     * Получить команды по тегам
     */
    List<TeamDTO> getTeamsByTag(String tag);

    /**
     * Получить количество команд
     */
    long getTeamsCount();

    /**
     * Получить количество публичных команд
     */
    long getPublicTeamsCount();

    /**
     * Получить количество приватных команд
     */
    long getPrivateTeamsCount();

    /**
     * Активировать команду
     */
    void activateTeam(UUID teamId, UUID userId);

    /**
     * Деактивировать команду
     */
    void deactivateTeam(UUID teamId, UUID userId);

    /**
     * Проверить, существует ли команда
     */
    boolean teamExists(UUID teamId);

    /**
     * Проверить, существует ли команда с таким названием
     */
    boolean teamExistsByName(String name);

    /**
     * Получить Entity команды (для внутреннего использования)
     */
    Team getTeamEntity(UUID teamId);

    /**
     * Обновить рейтинг команды
     */
    void updateTeamRating(UUID teamId, Double newRating);

    /**
     * Обновить ранг команды
     */
    void updateTeamRank(UUID teamId, Integer newRank);

    /**
     * Получить команды для обновления рейтингов
     */
    List<Team> getTeamsForRatingUpdate();

    /**
     * Очистить старые данные
     */
    void cleanupOldData();

    // === Методы для обработки событий Kafka ===

    /**
     * Обновить доступные квесты для команд
     */
    void updateAvailableQuests(UUID questId, String status);

    /**
     * Обновить кэш квестов
     */
    void updateQuestCache(UUID questId, dn.quest.shared.events.quest.QuestUpdatedEvent event);

    /**
     * Удалить квест из доступных
     */
    void removeQuestFromAvailable(UUID questId);

    /**
     * Обновить статистику игровой сессии
     */
    void updateGameSessionStatistics(UUID teamId, String sessionId, String status);
}