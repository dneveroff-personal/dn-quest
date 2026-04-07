package dn.quest.gameengine.repository;

import dn.quest.gameengine.entity.Team;
import dn.quest.gameengine.entity.User;
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
 * Репозиторий для работы с командами
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    // Базовые запросы
    Optional<Team> findByName(String name);
    List<Team> findByCaptain(User captain);
    boolean existsByName(String name);

    // Запросы для поиска активных команд
    @Query("SELECT t FROM Team t WHERE t.isActive = true ORDER BY t.name ASC")
    List<Team> findActiveTeams();

    @Query("SELECT t FROM Team t WHERE t.isActive = true ORDER BY t.name ASC")
    Page<Team> findActiveTeams(Pageable pageable);

    // Запросы для поиска по капитану
    @Query("SELECT t FROM Team t WHERE t.captain = :captain AND t.isActive = true ORDER BY t.name ASC")
    List<Team> findActiveByCaptain(@Param("captain") User captain);

    // Запросы для поиска по названию
    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY t.name ASC")
    List<Team> findByNameContainingIgnoreCase(@Param("name") String name);

    // Запросы для поиска по рейтингу
    @Query("SELECT t FROM Team t WHERE t.rating >= :minRating ORDER BY t.rating DESC")
    List<Team> findByRatingGreaterThanEqual(@Param("minRating") Double minRating);

    @Query("SELECT t FROM Team t WHERE t.rating BETWEEN :minRating AND :maxRating ORDER BY t.rating DESC")
    List<Team> findByRatingBetween(@Param("minRating") Double minRating, @Param("maxRating") Double maxRating);

    @Query("SELECT t FROM Team t ORDER BY t.rating DESC")
    List<Team> findTopTeamsByRating(Pageable pageable);

    // Запросы для поиска по статистике игр
    @Query("SELECT t FROM Team t WHERE t.totalGamesPlayed > 0 ORDER BY t.totalGamesPlayed DESC")
    List<Team> findMostActiveTeams();

    @Query("SELECT t FROM Team t WHERE t.totalGamesWon > 0 ORDER BY t.totalGamesWon DESC")
    List<Team> findTopWinningTeams();

    @Query("SELECT t FROM Team t WHERE t.totalPlaytimeSeconds > 0 ORDER BY t.totalPlaytimeSeconds DESC")
    List<Team> findMostExperiencedTeams();

    // Запросы для поиска по времени создания
    @Query("SELECT t FROM Team t WHERE t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<Team> findRecentlyCreatedTeams(@Param("since") Instant since);

    // Статистические запросы
    @Query("SELECT COUNT(t) FROM Team t WHERE t.isActive = true")
    long countActiveTeams();

    @Query("SELECT AVG(t.rating) FROM Team t WHERE t.rating IS NOT NULL")
    Double getAverageRating();

    @Query("SELECT AVG(t.totalGamesPlayed) FROM Team t WHERE t.totalGamesPlayed > 0")
    Double getAverageGamesPlayed();

    // Запросы для поиска по размеру команды
    @Query("SELECT t FROM Team t WHERE t.maxMembers IS NOT NULL ORDER BY t.maxMembers ASC")
    List<Team> findTeamsWithMemberLimit();

    @Query("SELECT t FROM Team t WHERE t.maxMembers IS NULL OR t.maxMembers > :minSize ORDER BY t.name ASC")
    List<Team> findTeamsWithMinMemberLimit(@Param("minSize") Integer minSize);

    // Запросы для поиска команд с определенными характеристиками
    @Query("SELECT t FROM Team t WHERE t.totalGamesPlayed >= :minGames ORDER BY t.totalGamesPlayed DESC")
    List<Team> findExperiencedTeams(@Param("minGames") Integer minGames);

    @Query("SELECT t FROM Team t WHERE t.totalPlaytimeSeconds >= :minPlaytime ORDER BY t.totalPlaytimeSeconds DESC")
    List<Team> findTeamsWithMinPlaytime(@Param("minPlaytime") Long minPlaytime);

    // Запросы для поиска по проценту побед
    @Query("SELECT t FROM Team t WHERE t.totalGamesPlayed > 0 ORDER BY CAST(t.totalGamesWon AS double) / t.totalGamesPlayed DESC")
    List<Team> findTeamsByWinRatio();

    @Query("SELECT t FROM Team t WHERE t.totalGamesPlayed > 0 AND CAST(t.totalGamesWon AS double) / t.totalGamesPlayed >= :minRatio ORDER BY CAST(t.totalGamesWon AS double) / t.totalGamesPlayed DESC")
    List<Team> findTeamsWithMinWinRatio(@Param("minRatio") Double minRatio);

    // Запросы для поиска команд с описанием
    @Query("SELECT t FROM Team t WHERE t.description IS NOT NULL AND t.description != '' ORDER BY t.name ASC")
    List<Team> findTeamsWithDescription();

    @Query("SELECT t FROM Team t WHERE LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY t.name ASC")
    List<Team> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);

    // Запросы для поиска команд с логотипами
    @Query("SELECT t FROM Team t WHERE t.logoUrl IS NOT NULL AND t.logoUrl != '' ORDER BY t.name ASC")
    List<Team> findTeamsWithLogo();

    // Запросы для комплексного поиска
    @Query("""
        SELECT t FROM Team t 
        WHERE (:isActive IS NULL OR t.isActive = :isActive)
          AND (:minRating IS NULL OR t.rating >= :minRating)
          AND (:maxRating IS NULL OR t.rating <= :maxRating)
          AND (:minGamesPlayed IS NULL OR t.totalGamesPlayed >= :minGamesPlayed)
          AND (:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:captain IS NULL OR t.captain = :captain)
        ORDER BY t.rating DESC
    """)
    List<Team> findTeamsWithFilters(
        @Param("isActive") Boolean isActive,
        @Param("minRating") Double minRating,
        @Param("maxRating") Double maxRating,
        @Param("minGamesPlayed") Integer minGamesPlayed,
        @Param("name") String name,
        @Param("captain") User captain
    );

    // Запросы для анализа по капитанам
    @Query("SELECT t.captain, COUNT(t) FROM Team t WHERE t.isActive = true GROUP BY t.captain ORDER BY COUNT(t) DESC")
    List<Object[]> getCaptainStatistics();

    @Query("SELECT t.captain, AVG(t.rating), AVG(t.totalGamesPlayed) FROM Team t WHERE t.rating IS NOT NULL AND t.isActive = true GROUP BY t.captain")
    List<Object[]> getCaptainStatisticsWithRating();

    // Запросы для поиска команд, в которых участвует пользователь
    @Query("SELECT DISTINCT t FROM Team t JOIN t.members tm WHERE tm.user = :user AND tm.isActive = true AND t.isActive = true ORDER BY t.name ASC")
    List<Team> findTeamsByMember(@Param("user") User user);

    @Query("SELECT DISTINCT t FROM Team t WHERE t.captain = :user OR t.id IN (SELECT tm.team.id FROM TeamMember tm WHERE tm.user = :user AND tm.isActive = true) ORDER BY t.name ASC")
    List<Team> findTeamsByCaptainOrMember(@Param("user") User user);

    // Запросы для анализа размера команд
    @Query("SELECT t, COUNT(tm) FROM Team t LEFT JOIN t.members tm WHERE tm.isActive = true AND t.isActive = true GROUP BY t.id ORDER BY COUNT(tm) DESC")
    List<Object[]> findTeamsByMemberCount();

    @Query("SELECT t FROM Team t WHERE t.isActive = true AND (SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team = t AND tm.isActive = true) >= :minMembers ORDER BY t.name ASC")
    List<Team> findTeamsWithMinActiveMembers(@Param("minMembers") Integer minMembers);

    // Запросы для поиска команд с свободными местами
    @Query("SELECT t FROM Team t WHERE t.isActive = true AND (t.maxMembers IS NULL OR (SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team = t AND tm.isActive = true) < t.maxMembers) ORDER BY t.name ASC")
    List<Team> findTeamsWithAvailableSlots();

    // Запросы для анализа по времени создания
    @Query("SELECT t FROM Team t WHERE t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
    List<Team> findByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    // Запросы для поиска лучших команд по разным метрикам
    @Query("SELECT t FROM Team t WHERE t.totalGamesPlayed >= 5 ORDER BY CAST(t.totalGamesWon AS double) / t.totalGamesPlayed DESC, t.totalGamesWon DESC")
    List<Team> findBestTeams();

    @Query("SELECT t FROM Team t WHERE t.totalPlaytimeSeconds >= 7200 ORDER BY t.rating DESC")
    List<Team> findExperiencedHighRatedTeams();

    // Запросы для пагинации
    Page<Team> findByIsActiveTrueOrderByNameAsc(Pageable pageable);
    Page<Team> findByCaptainOrderByNameAsc(User captain, Pageable pageable);

    // Запросы для поиска команд по уровню участников
    @Query("SELECT t FROM Team t JOIN t.members tm JOIN tm.user u WHERE t.isActive = true AND tm.isActive = true GROUP BY t HAVING AVG(u.rating) >= :minAvgRating ORDER BY t.name ASC")
    List<Team> findTeamsWithMinAverageMemberRating(@Param("minAvgRating") Double minAvgRating);

    // Запросы для анализа активности команд
    @Query("SELECT t FROM Team t WHERE t.isActive = true AND (SELECT MAX(s.lastActivityAt) FROM GameSession s WHERE s.team = t) >= :since ORDER BY t.name ASC")
    List<Team> findActiveTeamsSince(@Param("since") Instant since);

    // Запросы для поиска неактивных команд
    @Query("SELECT t FROM Team t WHERE t.isActive = true AND (SELECT MAX(s.lastActivityAt) FROM GameSession s WHERE s.team = t) < :threshold ORDER BY t.name ASC")
    List<Team> findInactiveTeams(@Param("threshold") Instant threshold);

    // Запросы для анализа по количеству участников
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team = :team AND tm.isActive = true")
    long countActiveMembersByTeam(@Param("team") Team team);

    @Query("SELECT t FROM Team t WHERE (SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team = t AND tm.isActive = true) = :memberCount ORDER BY t.name ASC")
    List<Team> findTeamsByExactMemberCount(@Param("memberCount") Integer memberCount);
}