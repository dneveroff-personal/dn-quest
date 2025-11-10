package dn.quest.statistics.service;

/**
 * Сервис для агрегации статистических данных
 */
public interface StatisticsAggregationService {

    /**
     * Агрегация статистики в реальном времени
     */
    void aggregateRealTimeStatistics();

    /**
     * Агрегация почасовой статистики
     */
    void aggregateHourlyStatistics();

    /**
     * Агрегация дневной статистики
     */
    void aggregateDailyStatistics();

    /**
     * Агрегация недельной статистики
     */
    void aggregateWeeklyStatistics();

    /**
     * Агрегация месячной статистики
     */
    void aggregateMonthlyStatistics();

    /**
     * Обновление лидербордов
     */
    void updateLeaderboards();

    /**
     * Очистка старых статистических данных
     */
    void cleanupOldStatistics();

    /**
     * Генерация системных метрик
     */
    void generateSystemMetrics();

    /**
     * Проверка целостности данных
     */
    void validateDataIntegrity();

    /**
     * Пересчет агрегированных данных за период
     */
    void recalculateAggregatedData(java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * Агрегация статистики пользователя
     */
    void aggregateUserStatistics(Long userId, java.time.LocalDate date);

    /**
     * Агрегация статистики квеста
     */
    void aggregateQuestStatistics(Long questId, java.time.LocalDate date);

    /**
     * Агрегация статистики команды
     */
    void aggregateTeamStatistics(Long teamId, java.time.LocalDate date);

    /**
     * Агрегация игровой статистики
     */
    void aggregateGameStatistics(String sessionId, java.time.LocalDate date);

    /**
     * Агрегация файловой статистики
     */
    void aggregateFileStatistics(String fileId, java.time.LocalDate date);
}