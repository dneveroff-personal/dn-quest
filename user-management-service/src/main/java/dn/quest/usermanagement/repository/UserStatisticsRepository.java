package dn.quest.usermanagement.repository;

import dn.quest.usermanagement.entity.UserStatistics;
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
 * Repository для работы со статистикой пользователей
 */
@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {

    // Базовые запросы
    Optional<UserStatistics> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    // Запросы для лидербордов
    @Query("SELECT us FROM UserStatistics us ORDER BY us.totalScore DESC")
    Page<UserStatistics> findTopByTotalScore(Pageable pageable);

    @Query("SELECT us FROM UserStatistics us ORDER BY us.level DESC, us.experiencePoints DESC")
    Page<UserStatistics> findTopByLevel(Pageable pageable);

    @Query("SELECT us FROM UserStatistics us ORDER BY us.questsCompleted DESC")
    Page<UserStatistics> findTopByQuestsCompleted(Pageable pageable);

    @Query("SELECT us FROM UserStatistics us ORDER BY us.codesSolved DESC")
    Page<UserStatistics> findTopByCodesSolved(Pageable pageable);

    @Query("SELECT us FROM UserStatistics us ORDER BY us.achievementsUnlocked DESC")
    Page<UserStatistics> findTopByAchievements(Pageable pageable);

    // Поиск по диапазонам значений
    List<UserStatistics> findByTotalScoreBetween(Long minScore, Long maxScore);
    List<UserStatistics> findByLevelBetween(Integer minLevel, Integer maxLevel);
    List<UserStatistics> findByExperiencePointsBetween(Long minExp, Long maxExp);

    // Поиск по количеству квестов
    List<UserStatistics> findByQuestsCompletedGreaterThan(Integer minQuests);
    List<UserStatistics> findByQuestsCompletedBetween(Integer minQuests, Integer maxQuests);

    // Поиск по времени игры
    List<UserStatistics> findByTotalPlaytimeMinutesGreaterThan(Long minMinutes);
    List<UserStatistics> findByTotalPlaytimeMinutesBetween(Long minMinutes, Long maxMinutes);

    // Поиск по достижениям
    List<UserStatistics> findByAchievementsUnlockedGreaterThan(Integer minAchievements);
    List<UserStatistics> findByRareAchievementsGreaterThan(Integer minRareAchievements);
    List<UserStatistics> findByLegendaryAchievementsGreaterThan(Integer minLegendaryAchievements);

    // Поиск по активности
    List<UserStatistics> findByLastActivityAtAfter(Instant after);
    List<UserStatistics> findByLastActivityAtBefore(Instant before);
    List<UserStatistics> findByLastActivityAtBetween(Instant start, Instant end);

    List<UserStatistics> findByLoginCountGreaterThan(Integer minLogins);
    List<UserStatistics> findByCurrentStreakDaysGreaterThan(Integer minStreakDays);

    // Комплексные запросы для поиска активных игроков
    @Query("SELECT us FROM UserStatistics us WHERE " +
           "us.totalScore >= :minScore AND " +
           "us.questsCompleted >= :minQuests AND " +
           "us.lastActivityAt >= :since")
    List<UserStatistics> findActivePlayers(
            @Param("minScore") Long minScore,
            @Param("minQuests") Integer minQuests,
            @Param("since") Instant since
    );

    // Поиск новых игроков
    @Query("SELECT us FROM UserStatistics us WHERE us.createdAt >= :since")
    List<UserStatistics> findNewPlayers(@Param("since") Instant since);

    // Поиск игроков с долгой историей
    @Query("SELECT us FROM UserStatistics us WHERE us.firstLoginAt <= :before")
    List<UserStatistics> findVeteranPlayers(@Param("before") Instant before);

    // Статистические запросы
    @Query("SELECT COUNT(us) FROM UserStatistics us WHERE us.level >= :level")
    long countByLevelAtLeast(@Param("level") Integer level);

    @Query("SELECT COUNT(us) FROM UserStatistics us WHERE us.totalScore >= :score")
    long countByScoreAtLeast(@Param("score") Long score);

    @Query("SELECT AVG(us.totalScore) FROM UserStatistics us")
    Double getAverageScore();

    @Query("SELECT AVG(us.level) FROM UserStatistics us")
    Double getAverageLevel();

    @Query("SELECT AVG(us.questsCompleted) FROM UserStatistics us")
    Double getAverageQuestsCompleted();

    @Query("SELECT SUM(us.totalScore) FROM UserStatistics us")
    Long getTotalScore();

    @Query("SELECT SUM(us.totalPlaytimeMinutes) FROM UserStatistics us")
    Long getTotalPlaytimeMinutes();

    // Запросы для ранжирования
    @Query("SELECT COUNT(us) + 1 FROM UserStatistics us WHERE us.totalScore > :score")
    Long getRankByScore(@Param("score") Long score);

    @Query("SELECT COUNT(us) + 1 FROM UserStatistics us WHERE us.level > :level OR (us.level = :level AND us.experiencePoints > :experience)")
    Long getRankByLevel(@Param("level") Integer level, @Param("experience") Long experience);

    // Запросы для анализа поведения игроков
    @Query("SELECT us FROM UserStatistics us WHERE us.questsStarted > 0 AND us.questsCompleted = 0")
    List<UserStatistics> findPlayersWhoNeverCompletedQuest();

    @Query("SELECT us FROM UserStatistics us WHERE us.attemptsMade > 0 AND us.codesSolved = 0")
    List<UserStatistics> findPlayersWhoNeverSolvedCode();

    @Query("SELECT us FROM UserStatistics us WHERE us.hintsUsed > us.codesSolved")
    List<UserStatistics> findPlayersWhoUseManyHints();

    // Запросы для анализа командной игры
    @Query("SELECT us FROM UserStatistics us WHERE us.teamsJoined > 0")
    List<UserStatistics> findTeamPlayers();

    @Query("SELECT us FROM UserStatistics us WHERE us.teamsCreated > 0")
    List<UserStatistics> findTeamCreators();

    @Query("SELECT us FROM UserStatistics us WHERE us.teamsLed > 0")
    List<UserStatistics> findTeamLeaders();

    // Запросы для анализа вовлеченности
    @Query("SELECT us FROM UserStatistics us WHERE us.currentStreakDays >= :days")
    List<UserStatistics> findPlayersWithStreak(@Param("days") Integer days);

    @Query("SELECT us FROM UserStatistics us WHERE us.loginCount >= :count")
    List<UserStatistics> findFrequentPlayers(@Param("count") Integer count);

    // Массовые операции
    @Query("UPDATE UserStatistics us SET us.totalScore = us.totalScore + :score WHERE us.userId = :userId")
    void addScoreToUser(@Param("userId") UUID userId, @Param("score") Long score);

    @Query("UPDATE UserStatistics us SET us.experiencePoints = us.experiencePoints + :experience WHERE us.userId = :userId")
    void addExperienceToUser(@Param("userId") UUID userId, @Param("experience") Long experience);

    @Query("UPDATE UserStatistics us SET us.questsCompleted = us.questsCompleted + 1 WHERE us.userId = :userId")
    void incrementQuestsCompleted(@Param("userId") UUID userId);

    @Query("UPDATE UserStatistics us SET us.lastActivityAt = :now WHERE us.userId = :userId")
    void updateLastActivity(@Param("userId") UUID userId, @Param("now") Instant now);
}