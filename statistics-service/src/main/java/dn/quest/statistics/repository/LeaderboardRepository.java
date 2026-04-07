package dn.quest.statistics.repository;

import dn.quest.statistics.entity.Leaderboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с лидербордами
 */
@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {

    /**
     * Найти запись в лидерборде по типу, периоду, дате и ID сущности
     */
    Optional<Leaderboard> findByLeaderboardTypeAndPeriodAndDateAndEntityId(
            String leaderboardType, String period, LocalDate date, String entityId);

    /**
     * Найти все записи в лидерборде по типу, периоду и дате
     */
    List<Leaderboard> findByLeaderboardTypeAndPeriodAndDateOrderByRank(
            String leaderboardType, String period, LocalDate date);

    /**
     * Найти все записи в лидерборде по типу и периоду с пагинацией
     */
    Page<Leaderboard> findByLeaderboardTypeAndPeriodOrderByRank(
            String leaderboardType, String period, Pageable pageable);

    /**
     * Найти записи в лидерборде по категории
     */
    List<Leaderboard> findByLeaderboardTypeAndPeriodAndDateAndCategoryOrderByRank(
            String leaderboardType, String period, LocalDate date, String category);

    /**
     * Найти записи пользователя в разных лидербордах
     */
    List<Leaderboard> findByEntityIdAndPeriodAndDateOrderByRank(String entityId, String period, LocalDate date);

    /**
     * Найти все записи пользователя за период
     */
    List<Leaderboard> findByEntityIdAndPeriodAndDateBetweenOrderByDateDescRank(
            String entityId, String period, LocalDate startDate, LocalDate endDate);

    /**
     * Получить записи лидерборда за диапазон дат
     */
    List<Leaderboard> findByLeaderboardTypeAndPeriodAndDateBetweenOrderByDateDescRank(
            String leaderboardType, String period, LocalDate startDate, LocalDate endDate);

    /**
     * Получить записи лидерборда за диапазон дат с пагинацией
     */
    Page<Leaderboard> findByLeaderboardTypeAndPeriodAndDateBetweenOrderByDateDescRank(
            String leaderboardType, String period, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Получить топ N записей из лидерборда
     */
    @Query("SELECT l FROM Leaderboard l WHERE l.leaderboardType = :type AND l.period = :period AND l.date = :date ORDER BY l.rank ASC")
    List<Leaderboard> findTopEntries(@Param("type") String type, @Param("period") String period, 
                                   @Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить записи вокруг указанного ранга
     */
    @Query("SELECT l FROM Leaderboard l WHERE l.leaderboardType = :type AND l.period = :period AND l.date = :date " +
           "AND l.rank BETWEEN :startRank AND :endRank ORDER BY l.rank ASC")
    List<Leaderboard> findEntriesAroundRank(@Param("type") String type, @Param("period") String period, 
                                          @Param("date") LocalDate date, @Param("startRank") Integer startRank, 
                                          @Param("endRank") Integer endRank);

    /**
     * Получить статистику по лидербордам за дату
     */
    @Query("SELECT " +
           "l.leaderboardType, " +
           "l.period, " +
           "COUNT(l) as totalEntries, " +
           "MAX(l.rank) as maxRank, " +
           "AVG(l.score) as avgScore, " +
           "MAX(l.score) as maxScore " +
           "FROM Leaderboard l WHERE l.date = :date " +
           "GROUP BY l.leaderboardType, l.period")
    List<Object[]> getLeaderboardStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить записи с наибольшим изменением ранга
     */
    @Query("SELECT l FROM Leaderboard l WHERE l.leaderboardType = :type AND l.period = :period AND l.date = :date " +
           "AND l.rankChange IS NOT NULL ORDER BY ABS(l.rankChange) DESC")
    List<Leaderboard> findEntriesWithBiggestRankChange(@Param("type") String type, @Param("period") String period, 
                                                     @Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить записи с наибольшим изменением очков
     */
    @Query("SELECT l FROM Leaderboard l WHERE l.leaderboardType = :type AND l.period = :period AND l.date = :date " +
           "AND l.scoreChange IS NOT NULL ORDER BY ABS(l.scoreChange) DESC")
    List<Leaderboard> findEntriesWithBiggestScoreChange(@Param("type") String type, @Param("period") String period, 
                                                      @Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить уникальные типы лидербордов
     */
    @Query("SELECT DISTINCT l.leaderboardType FROM Leaderboard l")
    List<String> findDistinctLeaderboardTypes();

    /**
     * Получить уникальные периоды
     */
    @Query("SELECT DISTINCT l.period FROM Leaderboard l")
    List<String> findDistinctPeriods();

    /**
     * Получить уникальные категории для типа лидерборда
     */
    @Query("SELECT DISTINCT l.category FROM Leaderboard l WHERE l.leaderboardType = :type AND l.category IS NOT NULL")
    List<String> findDistinctCategoriesByType(@Param("type") String type);

    /**
     * Получить количество записей в лидерборде
     */
    @Query("SELECT COUNT(l) FROM Leaderboard l WHERE l.leaderboardType = :type AND l.period = :period AND l.date = :date")
    Long countEntries(@Param("type") String type, @Param("period") String period, @Param("date") LocalDate date);

    /**
     * Получить максимальный ранг в лидерборде
     */
    @Query("SELECT MAX(l.rank) FROM Leaderboard l WHERE l.leaderboardType = :type AND l.period = :period AND l.date = :date")
    Integer getMaxRank(@Param("type") String type, @Param("period") String period, @Param("date") LocalDate date);

    /**
     * Получить средний счет в лидерборде
     */
    @Query("SELECT AVG(l.score) FROM Leaderboard l WHERE l.leaderboardType = :type AND l.period = :period AND l.date = :date")
    Double getAverageScore(@Param("type") String type, @Param("period") String period, @Param("date") LocalDate date);

    /**
     * Обновить ранг записи
     */
    @Modifying
    @Query("UPDATE Leaderboard l SET l.rank = :rank WHERE l.id = :id")
    int updateRank(@Param("id") UUID id, @Param("rank") Integer rank);

    /**
     * Обновить изменение ранга
     */
    @Modifying
    @Query("UPDATE Leaderboard l SET l.previousRank = :previousRank, l.rankChange = :rankChange WHERE l.id = :id")
    int updateRankChange(@Param("id") UUID id, @Param("previousRank") Integer previousRank, @Param("rankChange") Integer rankChange);

    /**
     * Обновить счет и изменение счета
     */
    @Modifying
    @Query("UPDATE Leaderboard l SET l.score = :score, l.previousScore = :previousScore, l.scoreChange = :scoreChange WHERE l.id = :id")
    int updateScore(@Param("id") UUID id, @Param("score") Double score, @Param("previousScore") Double previousScore, @Param("scoreChange") Double scoreChange);

    /**
     * Удалить записи лидерборда за дату
     */
    @Modifying
    @Query("DELETE FROM Leaderboard l WHERE l.leaderboardType = :type AND l.period = :period AND l.date = :date")
    int deleteEntries(@Param("type") String type, @Param("period") String period, @Param("date") LocalDate date);

    /**
     * Удалить старые записи лидерборда
     */
    @Modifying
    @Query("DELETE FROM Leaderboard l WHERE l.date < :date")
    int deleteEntriesOlderThan(@Param("date") LocalDate date);

    /**
     * Найти статистику за дату
     */
    List<Leaderboard> findByDate(LocalDate date);

    /**
     * Найти статистику до указанной даты
     */
    List<Leaderboard> findByDateBefore(LocalDate date);

    /**
     * Найти записи в лидерборде по типу, периоду, ID сущности и диапазону дат
     */
    List<Leaderboard> findByLeaderboardTypeAndPeriodAndEntityIdAndDateBetween(
            String leaderboardType, String period, String entityId, LocalDate startDate, LocalDate endDate);

    /**
     * Найти топ N записей в лидерборде
     */
    List<Leaderboard> findTopByLeaderboardTypeAndPeriodAndDateOrderByRankAsc(
            String leaderboardType, String period, LocalDate date, int limit);

    /**
     * Найти записи в лидерборде по типу, периоду, дате и диапазону рангов
     */
    List<Leaderboard> findByLeaderboardTypeAndPeriodAndDateAndRankBetween(
            String leaderboardType, String period, LocalDate date, Integer startRank, Integer endRank);
}
