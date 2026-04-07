package dn.quest.statistics.service;

import dn.quest.statistics.dto.StatisticsRequestDTO;
import dn.quest.statistics.dto.UserStatisticsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для запросов статистики
 */
public interface StatisticsQueryService {

    /**
     * Получить общую статистику платформы
     */
    Map<String, Object> getPlatformOverview(LocalDate startDate, LocalDate endDate);

    /**
     * Получить статистику пользователя
     */
    UserStatisticsDTO getUserStatistics(UUID userId, LocalDate date);

    /**
     * Получить статистику пользователя за период
     */
    Page<UserStatisticsDTO> getUserStatisticsForPeriod(UUID userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Получить статистику квеста
     */
    Map<String, Object> getQuestStatistics(UUID questId, LocalDate date);

    /**
     * Получить статистику команды
     */
    Map<String, Object> getTeamStatistics(UUID teamId, LocalDate date);

    /**
     * Получить кастомную статистику
     */
    Map<String, Object> getCustomStatistics(StatisticsRequestDTO request);

    /**
     * Получить топ пользователей по метрике
     */
    List<Map<String, Object>> getTopUsers(String metric, int limit, LocalDate date);

    /**
     * Получить топ квестов по метрике
     */
    List<Map<String, Object>> getTopQuests(String metric, int limit, LocalDate date);

    /**
     * Получить статистику по категориям
     */
    Map<String, Object> getStatisticsByCategories(String entityType, LocalDate startDate, LocalDate endDate);

    /**
     * Получить тренды метрик
     */
    Map<String, Object> getMetricTrends(List<String> metrics, String period, int periods);

    /**
     * Получить системную статистику
     */
    Map<String, Object> getSystemStatistics(String category, LocalDate date);
}