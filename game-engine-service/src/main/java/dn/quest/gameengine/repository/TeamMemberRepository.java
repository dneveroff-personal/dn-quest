package dn.quest.gameengine.repository;

import dn.quest.gameengine.entity.Team;
import dn.quest.gameengine.entity.TeamMember;
import dn.quest.gameengine.entity.User;
import dn.quest.shared.enums.TeamRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с участниками команд
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    // Базовые запросы
    Optional<TeamMember> findByTeamAndUser(Team team, User user);
    List<TeamMember> findByTeam(Team team);
    List<TeamMember> findByUser(User user);
    boolean existsByTeamAndUser(Team team, User user);

    // Запросы для поиска активных участников
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true ORDER BY tm.joinedAt ASC")
    List<TeamMember> findActiveMembers();

    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true ORDER BY tm.joinedAt ASC")
    Page<TeamMember> findActiveMembers(Pageable pageable);

    // Запросы для поиска по команде
    @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.isActive = true ORDER BY tm.joinedAt ASC")
    List<TeamMember> findActiveByTeam(@Param("team") Team team);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.isActive = true ORDER BY tm.joinedAt ASC")
    Page<TeamMember> findActiveByTeam(@Param("team") Team team, Pageable pageable);

    // Запросы для поиска по пользователю
    @Query("SELECT tm FROM TeamMember tm WHERE tm.user = :user AND tm.isActive = true ORDER BY tm.joinedAt DESC")
    List<TeamMember> findActiveByUser(@Param("user") User user);

    // Запросы для поиска по роли
    @Query("SELECT tm FROM TeamMember tm WHERE tm.role = :role AND tm.isActive = true ORDER BY tm.joinedAt ASC")
    List<TeamMember> findByRole(@Param("role") TeamRole role);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.role = :role AND tm.isActive = true ORDER BY tm.joinedAt ASC")
    List<TeamMember> findByTeamAndRole(@Param("team") Team team, @Param("role") TeamRole role);

    // Запросы для поиска капитанов
    @Query("SELECT tm FROM TeamMember tm WHERE tm.role = 'CAPTAIN' AND tm.isActive = true ORDER BY tm.joinedAt ASC")
    List<TeamMember> findCaptains();

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.role = 'CAPTAIN' AND tm.isActive = true")
    Optional<TeamMember> findCaptainByTeam(@Param("team") Team team);

    // Запросы для поиска по времени вступления
    @Query("SELECT tm FROM TeamMember tm WHERE tm.joinedAt >= :since AND tm.isActive = true ORDER BY tm.joinedAt DESC")
    List<TeamMember> findRecentMembers(@Param("since") Instant since);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.joinedAt BETWEEN :start AND :end AND tm.isActive = true ORDER BY tm.joinedAt DESC")
    List<TeamMember> findByJoinedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    // Запросы для поиска неактивных участников
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = false ORDER BY tm.leftAt DESC")
    List<TeamMember> findInactiveMembers();

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.isActive = false ORDER BY tm.leftAt DESC")
    List<TeamMember> findInactiveByTeam(@Param("team") Team team);

    // Статистические запросы
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team = :team AND tm.isActive = true")
    long countActiveMembersByTeam(@Param("team") Team team);

    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.user = :user AND tm.isActive = true")
    long countActiveTeamsByUser(@Param("user") User user);

    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.role = :role AND tm.isActive = true")
    long countByRole(@Param("role") TeamRole role);

    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.isActive = true")
    long countActiveMembers();

    // Запросы для анализа по ролям
    @Query("SELECT tm.role, COUNT(tm) FROM TeamMember tm WHERE tm.isActive = true GROUP BY tm.role ORDER BY COUNT(tm) DESC")
    List<Object[]> getRoleStatistics();

    @Query("SELECT tm.role, COUNT(tm) FROM TeamMember tm WHERE tm.team = :team AND tm.isActive = true GROUP BY tm.role ORDER BY COUNT(tm) DESC")
    List<Object[]> getRoleStatisticsByTeam(@Param("team") Team team);

    // Запросы для поиска участников с определенными характеристиками
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND tm.team.isActive = true ORDER BY tm.joinedAt ASC")
    List<TeamMember> findMembersOfActiveTeams();

    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND tm.team.isActive = true AND tm.team.maxMembers IS NOT NULL AND (SELECT COUNT(tm2) FROM TeamMember tm2 WHERE tm2.team = tm.team AND tm2.isActive = true) >= tm.team.maxMembers ORDER BY tm.team.name ASC")
    List<TeamMember> findMembersOfFullTeams();

    // Запросы для поиска участников, которые давно в команде
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND tm.joinedAt <= :threshold ORDER BY tm.joinedAt ASC")
    List<TeamMember> findLongTermMembers(@Param("threshold") Instant threshold);

    // Запросы для поиска участников по количеству игр
    @Query("SELECT tm FROM TeamMember tm WHERE tm.gamesPlayed > 0 AND tm.isActive = true ORDER BY tm.gamesPlayed DESC")
    List<TeamMember> findMostActivePlayers();

    @Query("SELECT tm FROM TeamMember tm WHERE tm.gamesPlayed >= :minGames AND tm.isActive = true ORDER BY tm.gamesPlayed DESC")
    List<TeamMember> findPlayersWithMinGames(@Param("minGames") Integer minGames);

    // Запросы для поиска участников по рейтингу
    @Query("SELECT tm FROM TeamMember tm WHERE tm.rating > 0 AND tm.isActive = true ORDER BY tm.rating DESC")
    List<TeamMember> findTopRatedMembers();

    @Query("SELECT tm FROM TeamMember tm WHERE tm.rating >= :minRating AND tm.isActive = true ORDER BY tm.rating DESC")
    List<TeamMember> findMembersWithMinRating(@Param("minRating") Double minRating);

    // Запросы для поиска участников по времени в игре
    @Query("SELECT tm FROM TeamMember tm WHERE tm.totalPlaytimeSeconds > 0 AND tm.isActive = true ORDER BY tm.totalPlaytimeSeconds DESC")
    List<TeamMember> findMostExperiencedMembers();

    @Query("SELECT tm FROM TeamMember tm WHERE tm.totalPlaytimeSeconds >= :minPlaytime AND tm.isActive = true ORDER BY tm.totalPlaytimeSeconds DESC")
    List<TeamMember> findMembersWithMinPlaytime(@Param("minPlaytime") Long minPlaytime);

    // Запросы для комплексного поиска
    @Query("""
        SELECT tm FROM TeamMember tm 
        WHERE (:isActive IS NULL OR tm.isActive = :isActive)
          AND (:team IS NULL OR tm.team = :team)
          AND (:user IS NULL OR tm.user = :user)
          AND (:role IS NULL OR tm.role = :role)
          AND (:minRating IS NULL OR tm.rating >= :minRating)
          AND (:minGamesPlayed IS NULL OR tm.gamesPlayed >= :minGamesPlayed)
        ORDER BY tm.joinedAt DESC
    """)
    List<TeamMember> findMembersWithFilters(
        @Param("isActive") Boolean isActive,
        @Param("team") Team team,
        @Param("user") User user,
        @Param("role") TeamRole role,
        @Param("minRating") Double minRating,
        @Param("minGamesPlayed") Integer minGamesPlayed
    );

    // Запросы для анализа по пользователям
    @Query("SELECT tm.user, COUNT(tm), AVG(tm.rating), SUM(tm.gamesPlayed) FROM TeamMember tm WHERE tm.isActive = true GROUP BY tm.user ORDER BY COUNT(tm) DESC")
    List<Object[]> getUserTeamStatistics();

    @Query("SELECT tm.user, COUNT(DISTINCT tm.team) FROM TeamMember tm WHERE tm.isActive = true GROUP BY tm.user ORDER BY COUNT(DISTINCT tm.team) DESC")
    List<Object[]> getUsersByTeamCount();

    // Запросы для анализа по командам
    @Query("SELECT tm.team, COUNT(tm), AVG(tm.rating), SUM(tm.gamesPlayed) FROM TeamMember tm WHERE tm.isActive = true GROUP BY tm.team ORDER BY COUNT(tm) DESC")
    List<Object[]> getTeamMemberStatistics();

    @Query("SELECT tm.team, COUNT(tm) FROM TeamMember tm WHERE tm.isActive = true AND tm.role = 'CAPTAIN' GROUP BY tm.team")
    List<Object[]> getTeamCaptainCount();

    // Запросы для поиска участников с определенным опытом
    @Query("SELECT tm FROM TeamMember tm WHERE tm.gamesPlayed > 0 AND tm.totalPlaytimeSeconds > 0 AND tm.isActive = true ORDER BY (tm.rating + tm.gamesPlayed * 0.1 + tm.totalPlaytimeSeconds * 0.0001) DESC")
    List<TeamMember> findMostExperiencedPlayersOverall();

    // Запросы для поиска участников, которые покинули команды
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = false AND tm.leftAt IS NOT NULL ORDER BY tm.leftAt DESC")
    List<TeamMember> findMembersWhoLeft();

    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = false AND tm.leftAt >= :since ORDER BY tm.leftAt DESC")
    List<TeamMember> findRecentlyLeftMembers(@Param("since") Instant since);

    // Запросы для анализа по времени пребывания в команде
    @Query("SELECT tm, CASE WHEN tm.isActive = true THEN AGE(CURRENT_TIMESTAMP, tm.joinedAt) ELSE AGE(tm.leftAt, tm.joinedAt) END FROM TeamMember tm WHERE tm.team = :team ORDER BY tm.joinedAt ASC")
    List<Object[]> getTeamMemberTenure(@Param("team") Team team);

    // Запросы для поиска участников с высоким вкладом
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND (tm.gamesPlayed > 10 OR tm.totalPlaytimeSeconds > 3600 OR tm.rating > 100) ORDER BY tm.rating DESC")
    List<TeamMember> findHighContributingMembers();

    // Запросы для пагинации
    Page<TeamMember> findByIsActiveTrueOrderByJoinedAtAsc(Pageable pageable);
    Page<TeamMember> findByTeamAndIsActiveTrueOrderByJoinedAtAsc(Team team, Pageable pageable);
    Page<TeamMember> findByUserAndIsActiveTrueOrderByJoinedAtDesc(User user, Pageable pageable);

    // Запросы для поиска участников по уровню
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND tm.user.rating >= :minUserRating ORDER BY tm.user.rating DESC")
    List<TeamMember> findMembersWithMinUserRating(@Param("minUserRating") Double minUserRating);

    // Запросы для анализа активности участников
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND (SELECT MAX(s.lastActivityAt) FROM GameSession s WHERE s.team = tm.team) >= :since ORDER BY tm.team.name ASC")
    List<TeamMember> findMembersOfActiveTeamsSince(@Param("since") Instant since);

    // Запросы для поиска участников команд с определенными характеристиками
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND tm.team.rating >= :minTeamRating ORDER BY tm.team.rating DESC, tm.joinedAt ASC")
    List<TeamMember> findMembersOfHighRatedTeams(@Param("minTeamRating") Double minTeamRating);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND tm.team.totalGamesPlayed >= :minTeamGames ORDER BY tm.team.totalGamesPlayed DESC, tm.joinedAt ASC")
    List<TeamMember> findMembersOfExperiencedTeams(@Param("minTeamGames") Integer minTeamGames);

    // Запросы для поиска участников с определенным соотношением побед
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND tm.team.totalGamesPlayed > 0 AND CAST(tm.team.totalGamesWon AS double) / tm.team.totalGamesPlayed >= :minWinRatio ORDER BY CAST(tm.team.totalGamesWon AS double) / tm.team.totalGamesPlayed DESC")
    List<TeamMember> findMembersOfWinningTeams(@Param("minWinRatio") Double minWinRatio);

    // Запросы для анализа по времени вступления в команду
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND tm.joinedAt BETWEEN :start AND :end ORDER BY tm.joinedAt ASC")
    List<TeamMember> findMembersJoinedBetween(@Param("start") Instant start, @Param("end") Instant end);

    // Запросы для поиска участников, которые присоединились недавно
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND tm.joinedAt >= :since ORDER BY tm.joinedAt DESC")
    List<TeamMember> findRecentlyJoinedMembers(@Param("since") Instant since);

    // Запросы для анализа по количеству участников в команде
    @Query("SELECT tm.team, COUNT(tm) FROM TeamMember tm WHERE tm.isActive = true GROUP BY tm.team HAVING COUNT(tm) >= :minMembers ORDER BY COUNT(tm) DESC")
    List<Object[]> findTeamsWithMinMemberCount(@Param("minMembers") Integer minMembers);

    // Запросы для поиска участников в командах с определенным размером
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true AND tm.team.id IN (SELECT t.id FROM Team t WHERE (SELECT COUNT(tm2) FROM TeamMember tm2 WHERE tm2.team = t AND tm2.isActive = true) BETWEEN :minSize AND :maxSize) ORDER BY tm.team.name ASC")
    List<TeamMember> findMembersInTeamsOfSizeRange(@Param("minSize") Integer minSize, @Param("maxSize") Integer maxSize);
}