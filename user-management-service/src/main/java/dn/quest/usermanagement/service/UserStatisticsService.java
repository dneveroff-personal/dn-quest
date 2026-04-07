package dn.quest.usermanagement.service;

import dn.quest.usermanagement.dto.UserStatisticsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления статистикой пользователей
 */
public interface UserStatisticsService {

    /**
     * Создает статистику пользователя по умолчанию
     */
    UserStatisticsDTO createUserStatistics(UUID userId);

    /**
     * Получает статистику пользователя по ID пользователя
     */
    Optional<UserStatisticsDTO> getUserStatisticsByUserId(UUID userId);

    /**
     * Получает статистику пользователя по ID статистики
     */
    Optional<UserStatisticsDTO> getUserStatisticsById(UUID id);

    /**
     * Добавляет опыт пользователю
     */
    UserStatisticsDTO addExperience(UUID userId, Long experience);

    /**
     * Добавляет очки пользователю
     */
    UserStatisticsDTO addScore(UUID userId, Long score);

    /**
     * Обновляет статистику квестов
     */
    UserStatisticsDTO updateQuestStatistics(UUID userId, Boolean completed, Long playtimeMinutes);

    /**
     * Обновляет статистику уровней
     */
    UserStatisticsDTO updateLevelStatistics(UUID userId, Boolean levelCompleted, 
                                           Boolean codeSolved, Boolean hintUsed, Boolean attemptMade);

    /**
     * Обновляет статистику команд
     */
    UserStatisticsDTO updateTeamStatistics(UUID userId, String actionType);

    /**
     * Обновляет статистику достижений
     */
    UserStatisticsDTO updateAchievementStatistics(UUID userId, String achievementType);

    /**
     * Обновляет статистику входов
     */
    UserStatisticsDTO updateLoginStatistics(UUID userId);

    /**
     * Обновляет серию дней
     */
    UserStatisticsDTO updateStreakStatistics(UUID userId);

    /**
     * Обновляет время последней активности
     */
    UserStatisticsDTO updateLastActivity(UUID userId);

    /**
     * Удаляет статистику пользователя
     */
    void deleteUserStatistics(UUID userId);

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
    Long getUserRankByScore(UUID userId);

    /**
     * Получает ранг пользователя по уровню
     */
    Long getUserRankByLevel(UUID userId);

    /**
     * Проверяет существование статистики пользователя
     */
    boolean existsByUserId(UUID userId);

    /**
     * Получает общую статистику сервиса
     */
    dn.quest.usermanagement.dto.GlobalStatisticsSummaryDTO getGlobalStatistics();

    /**
     * Обновляет статистику игровых сессий
     */
    void updateGameSessionStatistics(UUID userId, UUID sessionId, String status);

    /**
     * Обновляет статистику прохождения уровней
     */
    void updateLevelCompletionStatistics(UUID userId, Integer levelNumber);

    /**
     * Обновляет статистику файлов
     */
    void updateFileStatistics(UUID userId, Long fileId, String action);
}