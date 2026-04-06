package dn.quest.teammanagement.repository;

import dn.quest.teammanagement.entity.Team;
import dn.quest.teammanagement.entity.TeamStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы со статистикой команд
 */
@Repository
public interface TeamStatisticsRepository extends JpaRepository<TeamStatistics, UUID> {

    /**
     * Найти статистику по команде
     */
    Optional<TeamStatistics> findByTeam(Team team);

    /**
     * Проверить, существует ли статистика для команды
     */
    boolean existsByTeam(Team team);

    /**
     * Найти команды по рейтингу (топ)
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.team.isActive = true ORDER BY ts.rating DESC")
    Page<Team> findTopTeamsByRating(Pageable pageable);

    /**
     * Найти команды по количеству участников
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.team.isActive = true ORDER BY ts.activeMembers DESC")
    Page<Team> findTeamsByMemberCount(Pageable pageable);

    /**
     * Найти команды по количеству сыгранных игр
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.team.isActive = true ORDER BY ts.totalGamesPlayed DESC")
    Page<Team> findTeamsByGamesPlayed(Pageable pageable);

    /**
     * Найти команды по винрейту
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.team.isActive = true AND ts.totalGamesPlayed > 0 ORDER BY ts.winRate DESC")
    Page<Team> findTeamsByWinRate(Pageable pageable);

    /**
     * Найти команды по общему счету
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.team.isActive = true ORDER BY ts.totalScore DESC")
    Page<Team> findTeamsByTotalScore(Pageable pageable);

    /**
     * Найти команды по среднему счету
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.team.isActive = true AND ts.totalGamesPlayed > 0 ORDER BY ts.averageScore DESC")
    Page<Team> findTeamsByAverageScore(Pageable pageable);

    /**
     * Найти команды по количеству выполненных квестов
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.team.isActive = true ORDER BY ts.totalQuestsCompleted DESC")
    Page<Team> findTeamsByQuestsCompleted(Pageable pageable);

    /**
     * Найти команды с рейтингом выше определенного значения
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.rating >= :minRating AND ts.team.isActive = true ORDER BY ts.rating DESC")
    List<Team> findTeamsByMinRating(@Param("minRating") Double minRating);

    /**
     * Найти команды с рейтингом в определенном диапазоне
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.rating BETWEEN :minRating AND :maxRating AND ts.team.isActive = true ORDER BY ts.rating DESC")
    List<Team> findTeamsByRatingRange(@Param("minRating") Double minRating, @Param("maxRating") Double maxRating);

    /**
     * Найти команды с определенным количеством участников
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.activeMembers = :memberCount AND ts.team.isActive = true")
    List<Team> findTeamsByMemberCount(@Param("memberCount") Integer memberCount);

    /**
     * Найти команды с минимальным количеством участников
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.activeMembers >= :minMembers AND ts.team.isActive = true ORDER BY ts.activeMembers DESC")
    List<Team> findTeamsByMinMemberCount(@Param("minMembers") Integer minMembers);

    /**
     * Найти команды с винрейтом выше определенного значения
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.winRate >= :minWinRate AND ts.totalGamesPlayed > 0 AND ts.team.isActive = true ORDER BY ts.winRate DESC")
    List<Team> findTeamsByMinWinRate(@Param("minWinRate") Double minWinRate);

    /**
     * Найти активные команды (с недавней активностью)
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.lastActivityAt >= :since AND ts.team.isActive = true ORDER BY ts.lastActivityAt DESC")
    List<Team> findActiveTeamsSince(@Param("since") Instant since);

    /**
     * Найти неактивные команды (без активности долгое время)
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.lastActivityAt < :before AND ts.team.isActive = true ORDER BY ts.lastActivityAt ASC")
    List<Team> findInactiveTeamsBefore(@Param("before") Instant before);

    /**
     * Получить средний рейтинг всех команд
     */
    @Query("SELECT AVG(ts.rating) FROM TeamStatistics ts WHERE ts.team.isActive = true")
    Double getAverageRating();

    /**
     * Получить средний винрейт всех команд
     */
    @Query("SELECT AVG(ts.winRate) FROM TeamStatistics ts WHERE ts.totalGamesPlayed > 0 AND ts.team.isActive = true")
    Double getAverageWinRate();

    /**
     * Получить общее количество участников во всех командах
     */
    @Query("SELECT SUM(ts.activeMembers) FROM TeamStatistics ts WHERE ts.team.isActive = true")
    Long getTotalActiveMembers();

    /**
     * Получить общее количество сыгранных игр
     */
    @Query("SELECT SUM(ts.totalGamesPlayed) FROM TeamStatistics ts WHERE ts.team.isActive = true")
    Long getTotalGamesPlayed();

    /**
     * Получить общее количество выполненных квестов
     */
    @Query("SELECT SUM(ts.totalQuestsCompleted) FROM TeamStatistics ts WHERE ts.team.isActive = true")
    Long getTotalQuestsCompleted();

    /**
     * Получить статистику по рейтингам
     */
    @Query("SELECT " +
           "COUNT(ts) as total_teams, " +
           "AVG(ts.rating) as avg_rating, " +
           "MIN(ts.rating) as min_rating, " +
           "MAX(ts.rating) as max_rating " +
           "FROM TeamStatistics ts WHERE ts.team.isActive = true")
    Object[] getRatingStatistics();

    /**
     * Получить статистику по играм
     */
    @Query("SELECT " +
           "COUNT(ts) as total_teams, " +
           "SUM(ts.totalGamesPlayed) as total_games, " +
           "SUM(ts.totalGamesWon) as total_wins, " +
           "SUM(ts.totalGamesLost) as total_losses, " +
           "AVG(ts.winRate) as avg_win_rate " +
           "FROM TeamStatistics ts WHERE ts.team.isActive = true")
    Object[] getGameStatistics();

    /**
     * Найти команды с самым высоким рейтингом
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.team.isActive = true ORDER BY ts.rating DESC")
    List<Team> findTopRatedTeams(Pageable pageable);

    /**
     * Найти команды с самым высоким винрейтом
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.totalGamesPlayed >= :minGames AND ts.team.isActive = true ORDER BY ts.winRate DESC")
    List<Team> findTopWinRateTeams(@Param("minGames") Long minGames, Pageable pageable);

    /**
     * Найти команды с наибольшим количеством участников
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.team.isActive = true ORDER BY ts.activeMembers DESC")
    List<Team> findLargestTeams(Pageable pageable);

    /**
     * Обновить ранги команд на основе рейтинга
     */
    @Query("UPDATE TeamStatistics ts SET ts.rank = :rank WHERE ts.team.id = :teamId")
    void updateTeamRank(@Param("teamId") UUID teamId, @Param("rank") Integer rank);

    /**
     * Получить ранг команды
     */
    @Query("SELECT COUNT(ts) + 1 FROM TeamStatistics ts WHERE ts.rating > (SELECT ts2.rating FROM TeamStatistics ts2 WHERE ts2.team.id = :teamId) AND ts.team.isActive = true")
    Integer getTeamRank(@Param("teamId") UUID teamId);

    /**
     * Найти команды с определенным рангом
     */
    @Query("SELECT ts.team FROM TeamStatistics ts WHERE ts.rank = :rank AND ts.team.isActive = true")
    List<Team> findTeamsByRank(@Param("rank") Integer rank);

    /**
     * Получить распределение команд по рейтингам
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN ts.rating < 1000 THEN 'Beginner' " +
           "WHEN ts.rating < 1200 THEN 'Intermediate' " +
           "WHEN ts.rating < 1400 THEN 'Advanced' " +
           "WHEN ts.rating < 1600 THEN 'Expert' " +
           "ELSE 'Master' " +
           "END as rating_category, " +
           "COUNT(ts) as count " +
           "FROM TeamStatistics ts WHERE ts.team.isActive = true " +
           "GROUP BY " +
           "CASE " +
           "WHEN ts.rating < 1000 THEN 'Beginner' " +
           "WHEN ts.rating < 1200 THEN 'Intermediate' " +
           "WHEN ts.rating < 1400 THEN 'Advanced' " +
           "WHEN ts.rating < 1600 THEN 'Expert' " +
           "ELSE 'Master' " +
           "END")
    List<Object[]> getRatingDistribution();
}