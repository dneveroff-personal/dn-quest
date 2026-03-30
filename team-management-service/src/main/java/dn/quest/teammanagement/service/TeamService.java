package dn.quest.teammanagement.service;

import dn.quest.teammanagement.dto.*;
import dn.quest.teammanagement.dto.request.*;
import dn.quest.teammanagement.dto.response.TeamListResponse;
import dn.quest.teammanagement.entity.Team;
import dn.quest.teammanagement.entity.TeamMember;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Сервис для управления командами
 */
public interface TeamService {

    /**
     * Создать новую команду
     */
    TeamDTO createTeam(CreateTeamRequest request, Long captainId);

    /**
     * Получить команду по ID
     */
    TeamDTO getTeamById(Long teamId);

    /**
     * Получить полную информацию о команде
     */
    TeamDTO getFullTeamById(Long teamId);

    /**
     * Обновить информацию о команде
     */
    TeamDTO updateTeam(Long teamId, UpdateTeamRequest request, Long userId);

    /**
     * Удалить команду
     */
    void deleteTeam(Long teamId, Long userId);

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
    List<TeamDTO> getUserTeams(Long userId);

    /**
     * Получить команды пользователя постранично
     */
    TeamListResponse getUserTeams(Long userId, Pageable pageable);

    /**
     * Получить активные команды пользователя
     */
    List<TeamDTO> getUserActiveTeams(Long userId);

    /**
     * Проверить, является ли пользователь капитаном команды
     */
    boolean isTeamCaptain(Long teamId, Long userId);

    /**
     * Проверить, состоит ли пользователь в команде
     */
    boolean isTeamMember(Long teamId, Long userId);

    /**
     * Получить роль пользователя в команде
     */
    String getUserRoleInTeam(Long teamId, Long userId);

    /**
     * Проверить, может ли пользователь управлять командой
     */
    boolean canManageTeam(Long teamId, Long userId);

    /**
     * Передать права капитана
     */
    TeamMember transferCaptain(Long teamId, Long newCaptainId, Long currentCaptainId);

    /**
     * Добавить участника в команду
     */
    TeamMember addMember(Long teamId, Long userId, Long requesterId);

    /**
     * Удалить участника из команды
     */
    void removeMember(Long teamId, Long userId, Long requesterId);

    /**
     * Изменить роль участника
     */
    TeamMember changeMemberRole(Long teamId, Long userId, String newRole, Long requesterId);

    /**
     * Пригласить пользователя в команду
     */
    TeamInvitationDTO inviteUser(Long teamId, InviteUserRequest request, Long inviterId);

    /**
     * Получить приглашения команды
     */
    List<TeamInvitationDTO> getTeamInvitations(Long teamId, Long userId);

    /**
     * Получить активные приглашения команды
     */
    List<TeamInvitationDTO> getTeamActiveInvitations(Long teamId, Long userId);

    /**
     * Отозвать приглашение
     */
    void revokeInvitation(Long teamId, Long invitationId, Long userId);

    /**
     * Получить настройки команды
     */
    TeamSettingsDTO getTeamSettings(Long teamId);

    /**
     * Обновить настройки команды
     */
    TeamSettingsDTO updateTeamSettings(Long teamId, UpdateTeamSettingsRequest request, Long userId);

    /**
     * Получить статистику команды
     */
    TeamStatisticsDTO getTeamStatistics(Long teamId);

    /**
     * Обновить статистику команды
     */
    void updateTeamStatistics(Long teamId);

    /**
     * Получить участников команды
     */
    List<TeamMemberDTO> getTeamMembers(Long teamId);

    /**
     * Получить активных участников команды
     */
    List<TeamMemberDTO> getTeamActiveMembers(Long teamId, Long userId);

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
    void activateTeam(Long teamId, Long userId);

    /**
     * Деактивировать команду
     */
    void deactivateTeam(Long teamId, Long userId);

    /**
     * Проверить, существует ли команда
     */
    boolean teamExists(Long teamId);

    /**
     * Проверить, существует ли команда с таким названием
     */
    boolean teamExistsByName(String name);

    /**
     * Получить Entity команды (для внутреннего использования)
     */
    Team getTeamEntity(Long teamId);

    /**
     * Обновить рейтинг команды
     */
    void updateTeamRating(Long teamId, Double newRating);

    /**
     * Обновить ранг команды
     */
    void updateTeamRank(Long teamId, Integer newRank);

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
    void updateAvailableQuests(Long questId, String status);

    /**
     * Обновить кэш квестов
     */
    void updateQuestCache(Long questId, dn.quest.shared.events.quest.QuestUpdatedEvent event);

    /**
     * Удалить квест из доступных
     */
    void removeQuestFromAvailable(Long questId);

    /**
     * Обновить статистику игровой сессии
     */
    void updateGameSessionStatistics(Long teamId, String sessionId, String status);
}