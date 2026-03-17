package dn.quest.usermanagement.service;

import dn.quest.usermanagement.dto.UserStatisticsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления статистикой пользователей
 */
public interface UserStatisticsService {

    /**
     * Создает статистику пользователя по умолчанию
     */
    UserStatisticsDTO createUserStatistics(Long userId);

    /**
     * Получает статистику пользователя по ID пользователя
     */
    Optional<UserStatisticsDTO> getUserStatisticsByUserId(Long userId);

    /**
     * Получает статистику пользователя по ID статистики
     */
    Optional<UserStatisticsDTO> getUserStatisticsById(Long id);

    /**
     * Добавляет опыт пользователю
     */
    UserStatisticsDTO addExperience(Long userId, Long experience);

    /**
     * Добавляет очки пользователю
     */
    UserStatisticsDTO addScore(Long userId, Long score);

    /**
     * Обновляет статистику квестов
     */
    UserStatisticsDTO updateQuestStatistics(Long userId, Boolean completed, Long playtimeMinutes);

    /**
     * Обновляет статистику уровней
     */
    UserStatisticsDTO updateLevelStatistics(Long userId, Boolean levelCompleted, 
                                           Boolean codeSolved, Boolean hintUsed, Boolean attemptMade);

    /**
     * Обновляет статистику команд
     */
    UserStatisticsDTO updateTeamStatistics(Long userId, String actionType);

    /**
     * Обновляет статистику достижений
     */
    UserStatisticsDTO updateAchievementStatistics(Long userId, String achievementType);

    /**
     * Обновляет статистику входов
     */
    UserStatisticsDTO updateLoginStatistics(Long userId);

    /**
     * Обновляет серию дней
     */
    UserStatisticsDTO updateStreakStatistics(Long userId);

    /**
     * Обновляет время последней активности
     */
    UserStatisticsDTO updateLastActivity(Long userId);

    /**
     * Удаляет статистику пользователя
     */
    void deleteUserStatistics(Long userId);

    /**
     * Получает топ пользователей по очкам
     */
    Page<UserStatisticsDTO> getTopUsersByScore(Pageable pageable);

    /**
     * Получает топ пользователей по уровню
     */
    Page<UserStatisticsDTO> getTopUsersByLevel(Pageable pageable);

    /**
     * Получает топ пользователей по количеству квестов
     */
    Page<UserStatisticsDTO> getTopUsersByQuests(Pageable pageable);

    /**
     * Получает топ пользователей по количеству решенных кодов
     */
    Page<UserStatisticsDTO> getTopUsersByCodes(Pageable pageable);

    /**
     * Получает топ пользователей по достижениям
     */
    Page<UserStatisticsDTO> getTopUsersByAchievements(Pageable pageable);

    /**
     * Получает пользователей с определенным уровнем
     */
    List<UserStatisticsDTO> getUsersByLevel(Integer minLevel, Integer maxLevel);

    /**
     * Получает пользователей с определенным количеством очков
     */
    List<UserStatisticsDTO> getUsersByScoreRange(Long minScore, Long maxScore);

    /**
     * Получает активных игроков
     */
    List<UserStatisticsDTO> getActivePlayers(Long minScore, Integer minQuests, java.time.Instant since);

    /**
     * Получает новых игроков
     */
    List<UserStatisticsDTO> getNewPlayers(java.time.Instant since);

    /**
     * Получает ветеранов
     */
    List<UserStatisticsDTO> getVeteranPlayers(java.time.Instant before);

    /**
     * Получает игроков с долгой серией
     */
    List<UserStatisticsDTO> getPlayersWithStreak(Integer minDays);

    /**
     * Получает частых игроков
     */
    List<UserStatisticsDTO> getFrequentPlayers(Integer minLogins);

    /**
     * Получает ранг пользователя по очкам
     */
    Long getUserRankByScore(Long userId);

    /**
     * Получает ранг пользователя по уровню
     */
    Long getUserRankByLevel(Long userId);

    /**
     * Проверяет существование статистики пользователя
     */
    boolean existsByUserId(Long userId);

    /**
     * Получает общую статистику сервиса
     */
    GlobalStatisticsSummary getGlobalStatistics();
}

/**
 * Класс для общей статистики сервиса
 */
record GlobalStatisticsSummary(
    long totalUsers,
    long activeUsers,
    Double averageScore,
    Double averageLevel,
    Double averageQuestsCompleted,
    Long totalScore,
    Long totalPlaytimeMinutes,
    java.util.Map<String, Long> usersByLevelRange,
    java.util.Map<String, Long> usersByQuestRange
) {}