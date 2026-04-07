package dn.quest.statistics.scheduler;

import dn.quest.statistics.service.StatisticsAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Планировщик для агрегации статистических данных
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.statistics.aggregation.enabled", havingValue = "true", matchIfMissing = true)
public class StatisticsAggregationScheduler {

    private final StatisticsAggregationService aggregationService;

    /**
     * Ежеминутная агрегация реального времени
     */
    @Scheduled(fixedDelayString = "#{@environment.getProperty('app.statistics.aggregation.interval-ms', '60000')}")
    public void aggregateRealTimeStatistics() {
        try {
            log.debug("Starting real-time statistics aggregation");
            aggregationService.aggregateRealTimeStatistics();
            log.debug("Completed real-time statistics aggregation");
        } catch (Exception e) {
            log.error("Error during real-time statistics aggregation", e);
        }
    }

    /**
     * Ежечасная агрегация данных
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void aggregateHourlyStatistics() {
        try {
            log.info("Starting hourly statistics aggregation");
            aggregationService.aggregateHourlyStatistics();
            log.info("Completed hourly statistics aggregation");
        } catch (Exception e) {
            log.error("Error during hourly statistics aggregation", e);
        }
    }

    /**
     * Ежедневная агрегация данных в 2:00 ночи
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void aggregateDailyStatistics() {
        try {
            log.info("Starting daily statistics aggregation");
            aggregationService.aggregateDailyStatistics();
            log.info("Completed daily statistics aggregation");
        } catch (Exception e) {
            log.error("Error during daily statistics aggregation", e);
        }
    }

    /**
     * Еженедельная агрегация данных каждый понедельник в 3:00 ночи
     */
    @Scheduled(cron = "0 0 3 ? * MON")
    public void aggregateWeeklyStatistics() {
        try {
            log.info("Starting weekly statistics aggregation");
            aggregationService.aggregateWeeklyStatistics();
            log.info("Completed weekly statistics aggregation");
        } catch (Exception e) {
            log.error("Error during weekly statistics aggregation", e);
        }
    }

    /**
     * Ежемесячная агрегация данных 1-го числа каждого месяца в 4:00 ночи
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void aggregateMonthlyStatistics() {
        try {
            log.info("Starting monthly statistics aggregation");
            aggregationService.aggregateMonthlyStatistics();
            log.info("Completed monthly statistics aggregation");
        } catch (Exception e) {
            log.error("Error during monthly statistics aggregation", e);
        }
    }

    /**
     * Обновление лидербордов каждые 15 минут
     */
    @Scheduled(fixedDelayString = "#{@environment.getProperty('app.statistics.leaderboard.update-interval-minutes', '15') * 60 * 1000}")
    public void updateLeaderboards() {
        try {
            log.debug("Starting leaderboard update");
            aggregationService.updateLeaderboards();
            log.debug("Completed leaderboard update");
        } catch (Exception e) {
            log.error("Error during leaderboard update", e);
        }
    }

    /**
     * Очистка старых данных каждый день в 5:00 утра
     */
    @Scheduled(cron = "0 0 5 * * ?")
    @ConditionalOnProperty(name = "app.statistics.cleanup.enabled", havingValue = "true", matchIfMissing = true)
    public void cleanupOldStatistics() {
        try {
            log.info("Starting statistics cleanup");
            aggregationService.cleanupOldStatistics();
            log.info("Completed statistics cleanup");
        } catch (Exception e) {
            log.error("Error during statistics cleanup", e);
        }
    }

    /**
     * Генерация системных метрик каждые 5 минут
     */
    @Scheduled(fixedDelay = 300000) // 5 минут
    public void generateSystemMetrics() {
        try {
            log.debug("Starting system metrics generation");
            aggregationService.generateSystemMetrics();
            log.debug("Completed system metrics generation");
        } catch (Exception e) {
            log.error("Error during system metrics generation", e);
        }
    }

    /**
     * Проверка целостности данных каждый час
     */
    @Scheduled(cron = "0 30 * * * ?")
    public void validateDataIntegrity() {
        try {
            log.debug("Starting data integrity validation");
            aggregationService.validateDataIntegrity();
            log.debug("Completed data integrity validation");
        } catch (Exception e) {
            log.error("Error during data integrity validation", e);
        }
    }
}