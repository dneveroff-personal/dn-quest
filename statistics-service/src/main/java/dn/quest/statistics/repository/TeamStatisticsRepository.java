package dn.quest.statistics.repository;

import dn.quest.statistics.entity.TeamStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с командной статистикой
 */
@Repository
public interface TeamStatisticsRepository extends JpaRepository<TeamStatistics, Long> {

    /**
     * Найти статистику команды по ID и дате
     */
    Optional<TeamStatistics> findByTeamIdAndDate(Long teamId, LocalDate date);

    /**
     * Найти всю статистику команды по ID
     */
    List<TeamStatistics> findByTeamIdOrderByDateDesc(Long teamId);

    /**
     * Найти статистику команды за период
     */
    List<TeamStatistics> findByTeamIdAndDateBetweenOrderByDateDesc(Long teamId, LocalDate startDate, LocalDate endDate);

    /**
     * Найти статистику по капитану
     */
    List<TeamStatistics> findByCaptainIdOrderByDateDesc(Long captainId);

    /**
     * Найти статистику по капитану за период
     */
    List<TeamStatistics> findByCaptainIdAndDateBetweenOrderByDateDesc(Long captainId, LocalDate startDate, LocalDate endDate);

    /**
     * Получить количество созданных команд за дату
     */
    @Query("SELECT COALESCE(SUM(t.creations), 0) FROM TeamStatistics t WHERE t.date = :date")
    Long countCreatedTeamsByDate(@Param("date") LocalDate date);

    /**
     * Получить количество активных команд за дату
     */
    @Query("SELECT COUNT(DISTINCT t.teamId) FROM TeamStatistics t WHERE t.date = :date AND t.status = 'active'")
    Long countActiveTeamsByDate(@Param("date") LocalDate date);

    /**
     * Получить общее количество участников в командах за дату
     */
    @Query("SELECT SUM(t.currentMembersCount) FROM TeamStatistics t WHERE t.date = :date")
    Long totalTeamMembersByDate(@Param("date") LocalDate date);

    /**
     * Получить средний размер команды за дату
     */
    @Query("SELECT AVG(t.currentMembersCount) FROM TeamStatistics t WHERE t.date = :date AND t.currentMembersCount > 0")
    Double avgTeamSizeByDate(@Param("date") LocalDate date);

    /**
     * Получить топ команд по количеству сыгранных квестов
     */
    @Query("SELECT t FROM TeamStatistics t WHERE t.date = :date ORDER BY t.playedQuests DESC")
    List<TeamStatistics> findTopTeamsByPlayedQuests(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить топ команд по количеству побед
     */
    @Query("SELECT t FROM TeamStatistics t WHERE t.date = :date ORDER BY t.questWins DESC")
    List<TeamStatistics> findTopTeamsByWins(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить топ команд по рейтингу
     */
    @Query("SELECT t FROM TeamStatistics t WHERE t.date = :date AND t.currentRating IS NOT NULL ORDER BY t.currentRating DESC")
    List<TeamStatistics> findTopTeamsByRating(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Получить статистику по командам за период
     */
    @Query("SELECT " +
           "t.teamId, " +
           "t.teamName, " +
           "t.captainId, " +
           "SUM(t.playedQuests) as totalPlayed, " +
           "SUM(t.completedQuests) as totalCompleted, " +
           "SUM(t.questWins) as totalWins, " +
           "SUM(t.totalGameTimeMinutes) as totalTime, " +
           "AVG(t.currentRating) as avgRating " +
           "FROM TeamStatistics t " +
           "WHERE t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY t.teamId, t.teamName, t.captainId")
    List<Object[]> getTeamPeriodStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить статистику по капитанам за период
     */
    @Query("SELECT " +
           "t.captainId, " +
           "COUNT(DISTINCT t.teamId) as totalTeams, " +
           "SUM(t.creations) as creations, " +
           "SUM(t.playedQuests) as totalPlayed, " +
           "SUM(t.questWins) as totalWins, " +
           "AVG(t.currentRating) as avgRating " +
           "FROM TeamStatistics t " +
           "WHERE t.date BETWEEN :startDate AND :endDate AND t.captainId IS NOT NULL " +
           "GROUP BY t.captainId")
    List<Object[]> getCaptainPeriodStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Получить статистику по типам команд
     */
    @Query("SELECT " +
           "t.teamType, " +
           "COUNT(DISTINCT t.teamId) as totalTeams, " +
           "SUM(t.currentMembersCount) as totalMembers, " +
           "AVG(t.currentMembersCount) as avgSize, " +
           "SUM(t.playedQuests) as totalPlayed " +
           "FROM TeamStatistics t " +
           "WHERE t.date = :date AND t.teamType IS NOT NULL " +
           "GROUP BY t.teamType")
    List<Object[]> getTeamTypeStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить статистику по статусам команд
     */
    @Query("SELECT " +
           "t.status, " +
           "COUNT(DISTINCT t.teamId) as totalTeams, " +
           "SUM(t.currentMembersCount) as totalMembers, " +
           "AVG(t.currentMembersCount) as avgSize " +
           "FROM TeamStatistics t " +
           "WHERE t.date = :date AND t.status IS NOT NULL " +
           "GROUP BY t.status")
    List<Object[]> getTeamStatusStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить среднее время прохождения квеста командами
     */
    @Query("SELECT AVG(t.avgQuestCompletionTimeMinutes) FROM TeamStatistics t WHERE t.date = :date AND t.avgQuestCompletionTimeMinutes IS NOT NULL")
    Double getAvgQuestCompletionTimeByDate(@Param("date") LocalDate date);

    /**
     * Получить команды с последней активностью после указанного времени
     */
    List<TeamStatistics> findByLastActivityAtAfter(LocalDateTime time);

    /**
     * Увеличить количество сыгранных квестов
     */
    @Modifying
    @Query("UPDATE TeamStatistics t SET t.playedQuests = t.playedQuests + 1, t.lastActivityAt = :lastActivityAt WHERE t.teamId = :teamId AND t.date = :date")
    int incrementPlayedQuests(@Param("teamId") Long teamId, @Param("date") LocalDate date, @Param("lastActivityAt") LocalDateTime lastActivityAt);

    /**
     * Увеличить количество завершенных квестов
     */
    @Modifying
    @Query("UPDATE TeamStatistics t SET t.completedQuests = t.completedQuests + 1 WHERE t.teamId = :teamId AND t.date = :date")
    int incrementCompletedQuests(@Param("teamId") Long teamId, @Param("date") LocalDate date);

    /**
     * Увеличить количество побед в квестах
     */
    @Modifying
    @Query("UPDATE TeamStatistics t SET t.questWins = t.questWins + 1 WHERE t.teamId = :teamId AND t.date = :date")
    int incrementQuestWins(@Param("teamId") Long teamId, @Param("date") LocalDate date);

    /**
     * Увеличить количество добавлений участников
     */
    @Modifying
    @Query("UPDATE TeamStatistics t SET t.memberAdditions = t.memberAdditions + 1, t.currentMembersCount = t.currentMembersCount + 1 WHERE t.teamId = :teamId AND t.date = :date")
    int incrementMemberAdditions(@Param("teamId") Long teamId, @Param("date") LocalDate date);

    /**
     * Увеличить количество удалений участников
     */
    @Modifying
    @Query("UPDATE TeamStatistics t SET t.memberRemovals = t.memberRemovals + 1, t.currentMembersCount = t.currentMembersCount - 1 WHERE t.teamId = :teamId AND t.date = :date")
    int incrementMemberRemovals(@Param("teamId") Long teamId, @Param("date") LocalDate date);

    /**
     * Обновить количество участников
     */
    @Modifying
    @Query("UPDATE TeamStatistics t SET t.currentMembersCount = :count WHERE t.teamId = :teamId AND t.date = :date")
    int updateMembersCount(@Param("teamId") Long teamId, @Param("date") LocalDate date, @Param("count") Integer count);

    /**
     * Обновить рейтинг команды
     */
    @Modifying
    @Query("UPDATE TeamStatistics t SET t.currentRating = :rating, t.ratingChange = :change WHERE t.teamId = :teamId AND t.date = :date")
    int updateTeamRating(@Param("teamId") Long teamId, @Param("date") LocalDate date, @Param("rating") Double rating, @Param("change") Double change);

    /**
     * Добавить время игры
     */
    @Modifying
    @Query("UPDATE TeamStatistics t SET t.totalGameTimeMinutes = t.totalGameTimeMinutes + :minutes WHERE t.teamId = :teamId AND t.date = :date")
    int addGameTime(@Param("teamId") Long teamId, @Param("date") LocalDate date, @Param("minutes") Long minutes);

    /**
     * Увеличить количество успешных отправок кода
     */
    @Modifying
    @Query("UPDATE TeamStatistics t SET t.successfulCodeSubmissions = t.successfulCodeSubmissions + 1 WHERE t.teamId = :teamId AND t.date = :date")
    int incrementSuccessfulCodeSubmissions(@Param("teamId") Long teamId, @Param("date") LocalDate date);

    /**
     * Увеличить количество неудачных отправок кода
     */
    @Modifying
    @Query("UPDATE TeamStatistics t SET t.failedCodeSubmissions = t.failedCodeSubmissions + 1 WHERE t.teamId = :teamId AND t.date = :date")
    int incrementFailedCodeSubmissions(@Param("teamId") Long teamId, @Param("date") LocalDate date);

    /**
     * Увеличить количество завершенных уровней
     */
    @Modifying
    @Query("UPDATE TeamStatistics t SET t.completedLevels = t.completedLevels + 1 WHERE t.teamId = :teamId AND t.date = :date")
    int incrementCompletedLevels(@Param("teamId") Long teamId, @Param("date") LocalDate date);

    /**
     * Получить количество команд с рейтингом
     */
    @Query("SELECT COUNT(DISTINCT t.teamId) FROM TeamStatistics t WHERE t.date = :date AND t.currentRating IS NOT NULL")
    Long countTeamsWithRating(@Param("date") LocalDate date);

    /**
     * Получить популярные теги команд
     */
    @Query("SELECT t.tags FROM TeamStatistics t WHERE t.date = :date AND t.tags IS NOT NULL")
    List<String> findPopularTags(@Param("date") LocalDate date);

    /**
     * Получить статистику по просмотрам профилей команд
     */
    @Query("SELECT " +
           "SUM(t.profileViews) as totalViews, " +
           "SUM(t.uniqueProfileViews) as uniqueViews " +
           "FROM TeamStatistics t " +
           "WHERE t.date = :date")
    Object[] getProfileViewsStatsByDate(@Param("date") LocalDate date);

    /**
     * Получить статистику по приглашениям
     */
    @Query("SELECT " +
           "SUM(t.invitationsSent) as sent, " +
           "SUM(t.invitationsAccepted) as accepted, " +
           "SUM(t.invitationsDeclined) as declined " +
           "FROM TeamStatistics t " +
           "WHERE t.date = :date")
    Object[] getInvitationsStatsByDate(@Param("date") LocalDate date);

    /**
     * Удалить старую статистику (для очистки)
     */
    @Modifying
    @Query("DELETE FROM TeamStatistics t WHERE t.date < :date")
    int deleteStatisticsOlderThan(@Param("date") LocalDate date);

    /**
     * Найти статистику за дату
     */
    List<TeamStatistics> findByDate(LocalDate date);

    /**
     * Найти статистику до указанной даты
     */
    List<TeamStatistics> findByDateBefore(LocalDate date);

    /**
     * Найти статистику за период
     */
    List<TeamStatistics> findByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Получить количество уникальных команд за период
     */
    @Query("SELECT COUNT(DISTINCT t.teamId) FROM TeamStatistics t WHERE t.date BETWEEN :startDate AND :endDate")
    Long countDistinctTeamsByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}