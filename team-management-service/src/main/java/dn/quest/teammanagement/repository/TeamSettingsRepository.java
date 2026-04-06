package dn.quest.teammanagement.repository;

import dn.quest.teammanagement.entity.Team;
import dn.quest.teammanagement.entity.TeamSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с настройками команд
 */
@Repository
public interface TeamSettingsRepository extends JpaRepository<TeamSettings, UUID> {

    /**
     * Найти настройки по команде
     */
    Optional<TeamSettings> findByTeam(Team team);

    /**
     * Проверить, существуют ли настройки для команды
     */
    boolean existsByTeam(Team team);

    /**
     * Найти команды с разрешенными приглашениями от участников
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.allowMemberInvites = true AND ts.team.isActive = true")
    List<Team> findTeamsWithMemberInvitesAllowed();

    /**
     * Найти команды, требующие одобрения капитана
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.requireCaptainApproval = true AND ts.team.isActive = true")
    List<Team> findTeamsRequiringCaptainApproval();

    /**
     * Найти команды с авто-принятием приглашений
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.autoAcceptInvites = true AND ts.team.isActive = true")
    List<Team> findTeamsWithAutoAcceptInvites();

    /**
     * Найти команды с публичным профилем
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.publicProfile = true AND ts.team.isActive = true")
    List<Team> findTeamsWithPublicProfile();

    /**
     * Найти команды, разрешающие поиск
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.allowSearch = true AND ts.team.isPrivate = false AND ts.team.isActive = true")
    List<Team> findSearchableTeams();

    /**
     * Найти команды с включенным командным чатом
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.enableTeamChat = true AND ts.team.isActive = true")
    List<Team> findTeamsWithChatEnabled();

    /**
     * Найти команды с включенной статистикой
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.enableTeamStatistics = true AND ts.team.isActive = true")
    List<Team> findTeamsWithStatisticsEnabled();

    /**
     * Найти команды по тегам
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.teamTags IS NOT NULL AND LOWER(ts.teamTags) LIKE LOWER(CONCAT('%', :tag, '%')) AND ts.team.isActive = true")
    List<Team> findTeamsByTag(@Param("tag") String tag);

    /**
     * Найти команды с определенным сроком действия приглашений
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.invitationExpiryHours <= :maxHours AND ts.team.isActive = true")
    List<Team> findTeamsByInvitationExpiryHours(@Param("maxHours") Integer maxHours);

    /**
     * Найти команды с определенным лимитом приглашений
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.maxPendingInvitations >= :minInvitations AND ts.team.isActive = true")
    List<Team> findTeamsByMinPendingInvitations(@Param("minInvitations") Integer minInvitations);

    /**
     * Найти команды, разрешающие участникам покидать команду
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.allowMemberLeave = true AND ts.team.isActive = true")
    List<Team> findTeamsAllowingMemberLeave();

    /**
     * Найти команды, требующие капитана для расформирования
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.requireCaptainForDisband = true AND ts.team.isActive = true")
    List<Team> findTeamsRequiringCaptainForDisband();

    /**
     * Получить статистику по настройкам
     */
    @Query("SELECT COUNT(ts) FROM TeamSettings ts WHERE ts.allowMemberInvites = true")
    long countTeamsWithMemberInvitesAllowed();

    /**
     * Получить количество команд с публичным профилем
     */
    @Query("SELECT COUNT(ts) FROM TeamSettings ts WHERE ts.publicProfile = true")
    long countTeamsWithPublicProfile();

    /**
     * Получить количество команд, разрешающих поиск
     */
    @Query("SELECT COUNT(ts) FROM TeamSettings ts WHERE ts.allowSearch = true")
    long countSearchableTeams();

    /**
     * Найти команды с приветственным сообщением
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE ts.welcomeMessage IS NOT NULL AND ts.team.isActive = true")
    List<Team> findTeamsWithWelcomeMessage();

    /**
     * Найти команды по нескольким критериям настроек
     */
    @Query("SELECT ts.team FROM TeamSettings ts WHERE " +
           "(:allowMemberInvites IS NULL OR ts.allowMemberInvites = :allowMemberInvites) AND " +
           "(:publicProfile IS NULL OR ts.publicProfile = :publicProfile) AND " +
           "(:allowSearch IS NULL OR ts.allowSearch = :allowSearch) AND " +
           "ts.team.isActive = true")
    List<Team> findTeamsBySettings(@Param("allowMemberInvites") Boolean allowMemberInvites,
                                   @Param("publicProfile") Boolean publicProfile,
                                   @Param("allowSearch") Boolean allowSearch);
}