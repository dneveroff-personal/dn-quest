package dn.quest.statistics.service;

import dn.quest.statistics.dto.LeaderboardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с лидербордами
 */
public interface LeaderboardService {

    /**
     * Получить глобальный лидерборд
     */
    Page<LeaderboardDTO> getGlobalLeaderboard(String period, String category, LocalDate date, Pageable pageable);

    /**
     * Получить лидерборд квестов
     */
    Page<LeaderboardDTO> getQuestLeaderboard(String period, String category, String metric, LocalDate date, Pageable pageable);

    /**
     * Получить лидерборд команд
     */
    Page<LeaderboardDTO> getTeamLeaderboard(String period, String category, String metric, LocalDate date, Pageable pageable);

    /**
     * Получить позицию пользователя в лидерборде
     */
    Map<String, Object> getUserLeaderboardPosition(Long userId, String period, LocalDate date);

    /**
     * Получить позицию квеста в лидерборде
     */
    Map<String, Object> getQuestLeaderboardPosition(Long questId, String period, String metric, LocalDate date);

    /**
     * Получить позицию команды в лидерборде
     */
    Map<String, Object> getTeamLeaderboardPosition(Long teamId, String period, String metric, LocalDate date);

    /**
     * Получить окружение пользователя в лидерборде
     */
    List<LeaderboardDTO> getUserSurroundingInLeaderboard(Long userId, String period, int count, LocalDate date);

    /**
     * Получить историю позиций в лидерборде
     */
    Map<String, Object> getUserLeaderboardHistory(Long userId, String period, LocalDate startDate, LocalDate endDate);

    /**
     * Получить доступные категории лидербордов
     */
    Map<String, List<String>> getLeaderboardCategories();

    /**
     * Получить статистику лидербордов
     */
    Map<String, Object> getLeaderboardStats(String period, LocalDate date);

    /**
     * Обновить лидерборды
     */
    void updateLeaderboards(String period, LocalDate date);

    /**
     * Пересчитать глобальный лидерборд
     */
    void recalculateGlobalLeaderboard(String period, LocalDate date);

    /**
     * Пересчитать лидерборд квестов
     */
    void recalculateQuestLeaderboard(String period, String category, String metric, LocalDate date);

    /**
     * Пересчитать лидерборд команд
     */
    void recalculateTeamLeaderboard(String period, String category, String metric, LocalDate date);

    /**
     * Получить топ пользователей
     */
    List<LeaderboardDTO> getTopUsers(String period, String category, int limit, LocalDate date);

    /**
     * Получить топ квестов
     */
    List<LeaderboardDTO> getTopQuests(String period, String category, String metric, int limit, LocalDate date);

    /**
     * Получить топ команд
     */
    List<LeaderboardDTO> getTopTeams(String period, String category, String metric, int limit, LocalDate date);

    /**
     * Получить лидерборд по рейтингу
     */
    Page<LeaderboardDTO> getRatingLeaderboard(String entityType, String period, LocalDate date, Pageable pageable);

    /**
     * Получить лидерборд по очкам
     */
    Page<LeaderboardDTO> getScoreLeaderboard(String entityType, String period, LocalDate date, Pageable pageable);

    /**
     * Получить лидерборд по победам
     */
    Page<LeaderboardDTO> getWinsLeaderboard(String entityType, String period, LocalDate date, Pageable pageable);

    /**
     * Получить лидерборд по времени прохождения
     */
    Page<LeaderboardDTO> getCompletionTimeLeaderboard(String entityType, String period, LocalDate date, Pageable pageable);

    /**
     * Получить лидерборд по количеству завершений
     */
    Page<LeaderboardDTO> getCompletionsLeaderboard(String entityType, String period, LocalDate date, Pageable pageable);

    /**
     * Получить лидерборд по успешности
     */
    Page<LeaderboardDTO> getSuccessRateLeaderboard(String entityType, String period, LocalDate date, Pageable pageable);

    /**
     * Получить тренды лидерборда
     */
    Map<String, Object> getLeaderboardTrends(String entityType, String period, LocalDate startDate, LocalDate endDate);

    /**
     * Получить сравнение позиций
     */
    Map<String, Object> getPositionComparison(Long entityId, String entityType, LocalDate date1, LocalDate date2);

    /**
     * Получить прогноз позиций
     */
    Map<String, Object> getPositionForecast(Long entityId, String entityType, String period, int daysAhead);

    /**
     * Получить анализ изменений в лидерборде
     */
    Map<String, Object> getLeaderboardChangeAnalysis(String entityType, String period, LocalDate date);

    /**
     * Получить статистику по категориям
     */
    Map<String, Object> getCategoryStatistics(String entityType, String period, LocalDate date);

    /**
     * Получить распределение позиций
     */
    Map<String, Object> getPositionDistribution(String entityType, String period, LocalDate date);

    /**
     * Получить анализ конкуренции
     */
    Map<String, Object> getCompetitionAnalysis(Long entityId, String entityType, String period, LocalDate date);

    /**
     * Получить исторические данные лидерборда
     */
    Map<String, Object> getHistoricalLeaderboardData(String entityType, String period, LocalDate startDate, LocalDate endDate);

    /**
     * Получить миграцию позиций
     */
    Map<String, Object> getPositionMigration(String entityType, String period, LocalDate date);

    /**
     * Получить анализ стабильности позиций
     */
    Map<String, Object> getPositionStabilityAnalysis(Long entityId, String entityType, String period, LocalDate startDate, LocalDate endDate);

    /**
     * Получить сезонные тренды
     */
    Map<String, Object> getSeasonalTrends(String entityType, String period, int year);

}