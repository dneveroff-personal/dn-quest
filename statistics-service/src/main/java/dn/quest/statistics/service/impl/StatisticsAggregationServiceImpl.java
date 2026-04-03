package dn.quest.statistics.service.impl;

import dn.quest.statistics.entity.DailyAggregatedStatistics;
import dn.quest.statistics.entity.FileStatistics;
import dn.quest.statistics.entity.GameStatistics;
import dn.quest.statistics.entity.Leaderboard;
import dn.quest.statistics.entity.QuestStatistics;
import dn.quest.statistics.entity.SystemStatistics;
import dn.quest.statistics.entity.TeamStatistics;
import dn.quest.statistics.entity.UserStatistics;
import dn.quest.statistics.exception.AggregationException;
import dn.quest.statistics.repository.DailyAggregatedStatisticsRepository;
import dn.quest.statistics.repository.FileStatisticsRepository;
import dn.quest.statistics.repository.GameStatisticsRepository;
import dn.quest.statistics.repository.LeaderboardRepository;
import dn.quest.statistics.repository.QuestStatisticsRepository;
import dn.quest.statistics.repository.SystemStatisticsRepository;
import dn.quest.statistics.repository.TeamStatisticsRepository;
import dn.quest.statistics.repository.UserStatisticsRepository;
import dn.quest.statistics.service.CacheService;
import dn.quest.statistics.service.StatisticsAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.embedded.TomcatVirtualThreadsWebServerFactoryCustomizer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для агрегации статистических данных
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StatisticsAggregationServiceImpl implements StatisticsAggregationService {

    private final UserStatisticsRepository userStatisticsRepository;
    private final QuestStatisticsRepository questStatisticsRepository;
    private final TeamStatisticsRepository teamStatisticsRepository;
    private final DailyAggregatedStatisticsRepository dailyAggregatedStatisticsRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final GameStatisticsRepository gameStatisticsRepository;
    private final FileStatisticsRepository fileStatisticsRepository;
    private final SystemStatisticsRepository systemStatisticsRepository;
    private final CacheService cacheService;
    private final TomcatVirtualThreadsWebServerFactoryCustomizer tomcatVirtualThreadsProtocolHandlerCustomizer;

    @Override
    @Async
    public void aggregateRealTimeStatistics() {
        log.debug("Starting real-time statistics aggregation");
        
        try {
            LocalDate today = LocalDate.now();
            
            // Агрегация пользовательской статистики
            aggregateUserStatisticsForDate(today);
            
            // Агрегация статистики квестов
            aggregateQuestStatisticsForDate(today);
            
            // Агрегация командной статистики
            aggregateTeamStatisticsForDate(today);
            
            // Обновление лидербордов
            updateLeaderboards();
            
            log.debug("Real-time statistics aggregation completed");
            
        } catch (Exception e) {
            log.error("Error during real-time statistics aggregation", e);
            throw new AggregationException("Failed to aggregate real-time statistics", e);
        }
    }

    @Override
    @Async
    public void aggregateHourlyStatistics() {
        log.debug("Starting hourly statistics aggregation");
        
        try {
            LocalDate today = LocalDate.now();
            
            // Агрегация за последний час
            aggregateUserStatisticsForDate(today);
            aggregateQuestStatisticsForDate(today);
            aggregateTeamStatisticsForDate(today);
            
            // Генерация системных метрик
            generateSystemMetrics();
            
            log.debug("Hourly statistics aggregation completed");
            
        } catch (Exception e) {
            log.error("Error during hourly statistics aggregation", e);
            throw new AggregationException("Failed to aggregate hourly statistics", e);
        }
    }

    @Override
    @Async
    public void aggregateDailyStatistics() {
        log.info("Starting daily statistics aggregation for date: {}", LocalDate.now().minusDays(1));
        
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            // Агрегация дневной статистики
            aggregateDailyAggregatedStatistics(yesterday);
            
            // Обновление дневных лидербордов
            updateDailyLeaderboards(yesterday);
            
            // Очистка старых кэшей
            cacheService.invalidateDateCache(yesterday.minusDays(7));
            
            log.info("Daily statistics aggregation completed for date: {}", yesterday);
            
        } catch (Exception e) {
            log.error("Error during daily statistics aggregation", e);
            throw new AggregationException("Failed to aggregate daily statistics", e);
        }
    }

    @Override
    @Async
    public void aggregateWeeklyStatistics() {
        log.info("Starting weekly statistics aggregation");
        
        try {
            LocalDate weekStart = LocalDate.now().minusWeeks(1).withDayOfMonth(1);
            LocalDate weekEnd = LocalDate.now().minusWeeks(1).withDayOfMonth(weekStart.lengthOfMonth());
            
            // Агрегация недельной статистики
            aggregateWeeklyAggregatedStatistics(weekStart, weekEnd);
            
            // Обновление недельных лидербордов
            updateWeeklyLeaderboards(weekEnd);
            
            log.info("Weekly statistics aggregation completed for period: {} to {}", weekStart, weekEnd);
            
        } catch (Exception e) {
            log.error("Error during weekly statistics aggregation", e);
            throw new AggregationException("Failed to aggregate weekly statistics", e);
        }
    }

    @Override
    @Async
    public void aggregateMonthlyStatistics() {
        log.info("Starting monthly statistics aggregation");
        
        try {
            LocalDate monthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            LocalDate monthEnd = LocalDate.now().minusMonths(1).withDayOfMonth(monthStart.lengthOfMonth());
            
            // Агрегация месячной статистики
            aggregateMonthlyAggregatedStatistics(monthStart, monthEnd);
            
            // Обновление месячных лидербордов
            updateMonthlyLeaderboards(monthEnd);
            
            log.info("Monthly statistics aggregation completed for period: {} to {}", monthStart, monthEnd);
            
        } catch (Exception e) {
            log.error("Error during monthly statistics aggregation", e);
            throw new AggregationException("Failed to aggregate monthly statistics", e);
        }

    }

    @Override
    @Async
    public void updateLeaderboards() {
        log.debug("Updating leaderboards");
        
        try {
            LocalDate today = LocalDate.now();
            
            // Обновление глобальных лидербордов
            updateGlobalLeaderboards(today);
            
            // Обновление лидербордов квестов
            updateQuestLeaderboards(today);
            
            // Обновление командных лидербордов
            updateTeamLeaderboards(today);
            
            log.debug("Leaderboards update completed");
            
        } catch (Exception e) {
            log.error("Error during leaderboards update", e);
            throw new AggregationException("Failed to update leaderboards", e);
        }

    }

    @Override
    @Async
    public void cleanupOldStatistics() {
        log.info("Starting cleanup of old statistics");
        
        try {
            LocalDate cutoffDate = LocalDate.now().minusMonths(12);
            
            // Очистка старых детальных статистик
            cleanupOldUserStatistics(cutoffDate);
            cleanupOldQuestStatistics(cutoffDate);
            cleanupOldTeamStatistics(cutoffDate);
            
            // Очистка старых лидербордов
            cleanupOldLeaderboards(cutoffDate);
            
            // Очистка старых кэшей
            cacheService.cleanupExpiredCache();
            
            log.info("Old statistics cleanup completed");
            
        } catch (Exception e) {
            log.error("Error during old statistics cleanup", e);
            throw new AggregationException("Failed to cleanup old statistics", e);
        }
    }

    @Override
    @Async
    public void generateSystemMetrics() {
        log.debug("Generating system metrics");
        
        try {
            LocalDate today = LocalDate.now();
            
            // Генерация метрик производительности
            generatePerformanceMetrics(today);
            
            // Генерация бизнес-метрик
            generateBusinessMetrics(today);
            
            // Генерация метрик использования
            generateUsageMetrics(today);
            
            log.debug("System metrics generation completed");
            
        } catch (Exception e) {
            log.error("Error during system metrics generation", e);
            throw new AggregationException("Failed to generate system metrics", e);
        }
    }

    @Override
    @Async
    public void validateDataIntegrity() {
        log.info("Starting data integrity validation");
        
        try {
            LocalDate today = LocalDate.now();
            
            // Валидация пользовательской статистики
            validateUserStatisticsIntegrity(today);
            
            // Валидация статистики квестов
            validateQuestStatisticsIntegrity(today);
            
            // Валидация командной статистики
            validateTeamStatisticsIntegrity(today);
            
            // Валидация агрегированных данных
            validateAggregatedStatisticsIntegrity(today);
            
            log.info("Data integrity validation completed");
            
        } catch (Exception e) {
            log.error("Error during data integrity validation", e);
            throw new AggregationException("Failed to validate data integrity", e);
        }
    }

    @Override
    @Async
    public void recalculateAggregatedData(LocalDate startDate, LocalDate endDate) {
        log.info("Recalculating aggregated data from {} to {}", startDate, endDate);
        
        try {
            // Пересчет дневной агрегированной статистики
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                aggregateDailyAggregatedStatistics(date);
            }
            
            // Пересчет лидербордов
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                updateDailyLeaderboards(date);
            }
            
            // Инвалидация кэшей
            cacheService.invalidateAllStatisticsCache();
            
            log.info("Aggregated data recalculation completed from {} to {}", startDate, endDate);
            
        } catch (Exception e) {
            log.error("Error during aggregated data recalculation", e);
            throw new AggregationException("Failed to recalculate aggregated data", e);
        }
    }

    @Override
    @Async
    public void aggregateUserStatistics(UUID userId, LocalDate date) {
        log.debug("Aggregating user statistics for user: {} date: {}", userId, date);
        
        try {
            List<UserStatistics> dailyStats = userStatisticsRepository.findByUserIdAndDate(userId, date)
                    .map(List::of)
                    .orElseGet(List::of);
            
            if (!dailyStats.isEmpty()) {
                // Агрегация данных за день
                UserStatistics aggregated = aggregateDailyUserStats(dailyStats);
                userStatisticsRepository.save(aggregated);
                
                // Кэширование результата
                Map<String, Object> statsMap = convertUserStatisticsToMap(aggregated);
                cacheService.cacheUserStatistics(userId, date, statsMap);
            }
            
            log.debug("User statistics aggregation completed for user: {} date: {}", userId, date);
            
        } catch (Exception e) {
            log.error("Error during user statistics aggregation for user: {} date: {}", userId, date, e);
            throw new AggregationException("Failed to aggregate user statistics", e);
        }
    }

    @Override
    @Async
    public void aggregateQuestStatistics(Long questId, LocalDate date) {
        log.debug("Aggregating quest statistics for quest: {} date: {}", questId, date);
        
        try {
            List<QuestStatistics> dailyStats = questStatisticsRepository.findByQuestIdAndDate(questId, date)
                    .map(List::of)
                    .orElseGet(List::of);
            
            if (!dailyStats.isEmpty()) {
                // Агрегация данных за день
                QuestStatistics aggregated = aggregateDailyQuestStats(dailyStats);
                questStatisticsRepository.save(aggregated);
                
                // Кэширование результата
                Map<String, Object> statsMap = convertQuestStatisticsToMap(aggregated);
                cacheService.cacheQuestStatistics(questId, date, statsMap);
            }
            
            log.debug("Quest statistics aggregation completed for quest: {} date: {}", questId, date);
            
        } catch (Exception e) {
            log.error("Error during quest statistics aggregation for quest: {} date: {}", questId, date, e);
            throw new AggregationException("Failed to aggregate quest statistics", e);
        }
    }

    @Override
    @Async
    public void aggregateTeamStatistics(UUID teamId, LocalDate date) {
        log.debug("Aggregating team statistics for team: {} date: {}", teamId, date);
        
        try {
            List<TeamStatistics> dailyStats = teamStatisticsRepository.findByTeamIdAndDate(teamId, date)
                    .map(List::of)
                    .orElseGet(List::of);
            
            if (!dailyStats.isEmpty()) {
                // Агрегация данных за день
                TeamStatistics aggregated = aggregateDailyTeamStats(dailyStats);
                teamStatisticsRepository.save(aggregated);
                
                // Кэширование результата
                Map<String, Object> statsMap = convertTeamStatisticsToMap(aggregated);
                cacheService.cacheTeamStatistics(teamId, date, statsMap);
            }
            
            log.debug("Team statistics aggregation completed for team: {} date: {}", teamId, date);
            
        } catch (Exception e) {
            log.error("Error during team statistics aggregation for team: {} date: {}", teamId, date, e);
            throw new AggregationException("Failed to aggregate team statistics", e);
        }
    }

    @Override
    @Async
    public void aggregateGameStatistics(String sessionId, LocalDate date) {
        log.debug("Aggregating game statistics for session: {} date: {}", sessionId, date);
        
        try {
            // Здесь должна быть логика агрегации игровой статистики
            // В реальной реализации здесь был бы запрос к данным игровых сессий
            
            log.debug("Game statistics aggregation completed for session: {} date: {}", sessionId, date);
            
        } catch (Exception e) {
            log.error("Error during game statistics aggregation for session: {} date: {}", sessionId, date, e);
            throw new AggregationException("Failed to aggregate game statistics", e);
        }
    }

    @Override
    @Async
    public void aggregateFileStatistics(String fileId, LocalDate date) {
        log.debug("Aggregating file statistics for file: {} date: {}", fileId, date);
        
        try {
            // Здесь должна быть логика агрегации файловой статистики
            // В реальной реализации здесь был бы запрос к данным файлов
            
            log.debug("File statistics aggregation completed for file: {} date: {}", fileId, date);
            
        } catch (Exception e) {
            log.error("Error during file statistics aggregation for file: {} date: {}", fileId, date, e);
            throw new AggregationException("Failed to aggregate file statistics", e);
        }
    }

    // Приватные методы для агрегации данных

    private void aggregateUserStatisticsForDate(LocalDate date) {
        List<UserStatistics> stats = userStatisticsRepository.findByDate(date);
        
        Map<UUID, List<UserStatistics>> groupedByUser = stats.stream()
                .collect(Collectors.groupingBy(UserStatistics::getUserId));
        
        for (Map.Entry<UUID, List<UserStatistics>> entry : groupedByUser.entrySet()) {
            UserStatistics aggregated = aggregateDailyUserStats(entry.getValue());
            userStatisticsRepository.save(aggregated);
        }
    }

    private void aggregateQuestStatisticsForDate(LocalDate date) {
        List<QuestStatistics> stats = questStatisticsRepository.findByDate(date);
        
        Map<Long, List<QuestStatistics>> groupedByQuest = stats.stream()
                .collect(Collectors.groupingBy(QuestStatistics::getQuestId));
        
        for (Map.Entry<Long, List<QuestStatistics>> entry : groupedByQuest.entrySet()) {
            QuestStatistics aggregated = aggregateDailyQuestStats(entry.getValue());
            questStatisticsRepository.save(aggregated);
        }
    }

    private void aggregateTeamStatisticsForDate(LocalDate date) {
        List<TeamStatistics> stats = teamStatisticsRepository.findByDate(date);
        
        Map<UUID, List<TeamStatistics>> groupedByTeam = stats.stream()
                .collect(Collectors.groupingBy(TeamStatistics::getTeamId));
        
        for (Map.Entry<UUID, List<TeamStatistics>> entry : groupedByTeam.entrySet()) {
            TeamStatistics aggregated = aggregateDailyTeamStats(entry.getValue());
            teamStatisticsRepository.save(aggregated);
        }
    }

    private UserStatistics aggregateDailyUserStats(List<UserStatistics> stats) {
        if (stats.isEmpty()) {
            return null;
        }
        
        UserStatistics first = stats.get(0);
        
        return UserStatistics.builder()
                .userId(first.getUserId())
                .date(first.getDate())
                .registrations(stats.stream().mapToInt(UserStatistics::getRegistrations).sum())
                .logins(stats.stream().mapToInt(UserStatistics::getLogins).sum())
                .gameSessions(stats.stream().mapToInt(UserStatistics::getGameSessions).sum())
                .completedQuests(stats.stream().mapToInt(UserStatistics::getCompletedQuests).sum())
                .createdQuests(stats.stream().mapToInt(UserStatistics::getCreatedQuests).sum())
                .createdTeams(stats.stream().mapToInt(UserStatistics::getCreatedTeams).sum())
                .teamMemberships(stats.stream().mapToInt(UserStatistics::getTeamMemberships).sum())
                .totalGameTimeMinutes(stats.stream().mapToLong(UserStatistics::getTotalGameTimeMinutes).sum())
                .uploadedFiles(stats.stream().mapToInt(UserStatistics::getUploadedFiles).sum())
                .totalFileSizeBytes(stats.stream().mapToLong(UserStatistics::getTotalFileSizeBytes).sum())
                .successfulCodeSubmissions(stats.stream().mapToInt(UserStatistics::getSuccessfulCodeSubmissions).sum())
                .failedCodeSubmissions(stats.stream().mapToInt(UserStatistics::getFailedCodeSubmissions).sum())
                .completedLevels(stats.stream().mapToInt(UserStatistics::getCompletedLevels).sum())
                .lastActiveAt(stats.stream()
                        .map(UserStatistics::getLastActiveAt)
                        .max(LocalDateTime::compareTo)
                        .orElse(LocalDateTime.now()))
                .build();
    }

    private QuestStatistics aggregateDailyQuestStats(List<QuestStatistics> stats) {
        if (stats.isEmpty()) {
            return null;
        }
        
        QuestStatistics first = stats.get(0);
        
        return QuestStatistics.builder()
                .questId(first.getQuestId())
                .questTitle(first.getQuestTitle())
                .authorId(first.getAuthorId())
                .date(first.getDate())
                .creations(stats.stream().mapToInt(QuestStatistics::getCreations).sum())
                .updates(stats.stream().mapToInt(QuestStatistics::getUpdates).sum())
                .publications(stats.stream().mapToInt(QuestStatistics::getPublications).sum())
                .deletions(stats.stream().mapToInt(QuestStatistics::getDeletions).sum())
                .views(stats.stream().mapToInt(QuestStatistics::getViews).sum())
                .uniqueViews(stats.stream().mapToInt(QuestStatistics::getUniqueViews).sum())
                .starts(stats.stream().mapToInt(QuestStatistics::getStarts).sum())
                .completions(stats.stream().mapToInt(QuestStatistics::getCompletions).sum())
                .uniqueParticipants(stats.stream().mapToInt(QuestStatistics::getUniqueParticipants).sum())
                .totalGameTimeMinutes(stats.stream().mapToLong(QuestStatistics::getTotalGameTimeMinutes).sum())
                .build();
    }

    private TeamStatistics aggregateDailyTeamStats(List<TeamStatistics> stats) {
        if (stats.isEmpty()) {
            return null;
        }
        
        TeamStatistics first = stats.get(0);
        
        return TeamStatistics.builder()
                .teamId(first.getTeamId())
                .teamName(first.getTeamName())
                .captainId(first.getCaptainId())
                .date(first.getDate())
                .creations(stats.stream().mapToInt(TeamStatistics::getCreations).sum())
                .updates(stats.stream().mapToInt(TeamStatistics::getUpdates).sum())
                .deletions(stats.stream().mapToInt(TeamStatistics::getDeletions).sum())
                .memberAdditions(stats.stream().mapToInt(TeamStatistics::getMemberAdditions).sum())
                .memberRemovals(stats.stream().mapToInt(TeamStatistics::getMemberRemovals).sum())
                .playedQuests(stats.stream().mapToInt(TeamStatistics::getPlayedQuests).sum())
                .completedQuests(stats.stream().mapToInt(TeamStatistics::getCompletedQuests).sum())
                .questWins(stats.stream().mapToInt(TeamStatistics::getQuestWins).sum())
                .totalGameTimeMinutes(stats.stream().mapToLong(TeamStatistics::getTotalGameTimeMinutes).sum())
                .successfulCodeSubmissions(stats.stream().mapToInt(TeamStatistics::getSuccessfulCodeSubmissions).sum())
                .failedCodeSubmissions(stats.stream().mapToInt(TeamStatistics::getFailedCodeSubmissions).sum())
                .completedLevels(stats.stream().mapToInt(TeamStatistics::getCompletedLevels).sum())
                .lastActivityAt(stats.stream()
                        .map(TeamStatistics::getLastActivityAt)
                        .max(LocalDateTime::compareTo)
                        .orElse(LocalDateTime.now()))
                .build();
    }

    private void aggregateDailyAggregatedStatistics(LocalDate date) {
        log.debug("Aggregating daily statistics for date: {}", date);
        
        // Получаем статистику за день
        List<UserStatistics> userStats = userStatisticsRepository.findByDate(date);
        List<QuestStatistics> questStats = questStatisticsRepository.findByDate(date);
        List<TeamStatistics> teamStats = teamStatisticsRepository.findByDate(date);
        List<GameStatistics> gameStats = gameStatisticsRepository.findByDate(date);
        
        // Рассчитываем агрегированные метрики
        long totalUsers = userStats.stream().map(UserStatistics::getUserId).distinct().count();
        long activeUsers = userStats.stream()
                .filter(us -> us.getGameSessions() > 0)
                .map(UserStatistics::getUserId)
                .distinct()
                .count();
        
        long totalQuests = questStats.stream().map(QuestStatistics::getQuestId).distinct().count();
        long completedQuests = userStats.stream().mapToLong(UserStatistics::getCompletedQuests).sum();
        
        long totalTeams = teamStats.stream().map(TeamStatistics::getTeamId).distinct().count();
        long activeTeams = teamStats.stream()
                .filter(ts -> ts.getPlayedQuests() > 0)
                .map(TeamStatistics::getTeamId)
                .distinct()
                .count();
        
        long totalGameSessions = gameStats.size();
        long completedGameSessions = gameStats.stream()
                .filter(gs -> "completed".equals(gs.getStatus()))
                .count();
        
        // Создаем агрегированную запись
        DailyAggregatedStatistics aggregated = DailyAggregatedStatistics.builder()
                .date(date)
                .aggregationType("daily")
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalQuests(totalQuests)
                .completedQuests(completedQuests)
                .totalTeams(totalTeams)
                .totalGameSessions(totalGameSessions)
                .completedGameSessions(completedGameSessions)
                .totalGameTime(userStats.stream().mapToLong(UserStatistics::getTotalGameTimeMinutes).sum())
                .codeSubmissions(userStats.stream()
                        .mapToInt(us -> us.getSuccessfulCodeSubmissions() + us.getFailedCodeSubmissions())
                        .sum())
                .build();
        
        dailyAggregatedStatisticsRepository.save(aggregated);
        log.debug("Daily aggregated statistics saved for date: {}", date);
    }

    private void updateDailyLeaderboards(LocalDate date) {
        // Обновление дневных лидербордов
        updateGlobalLeaderboards(date);
        updateQuestLeaderboards(date);
        updateTeamLeaderboards(date);
    }

    private void updateWeeklyLeaderboards(LocalDate weekEnd) {
        // Обновление недельных лидербордов
    }

    private void updateMonthlyLeaderboards(LocalDate monthEnd) {
        // Обновление месячных лидербордов
    }

    private void updateGlobalLeaderboards(LocalDate date) {
        log.debug("Updating global leaderboards for date: {}", date);
        
        // Получаем статистику пользователей за день
        List<UserStatistics> userStats = userStatisticsRepository.findByDate(date);
        
        // Рассчитываем очки для каждого пользователя
        List<Leaderboard> leaderboards = userStats.stream()
                .collect(Collectors.groupingBy(UserStatistics::getUserId))
                .entrySet().stream()
                .map(entry -> {
                    UUID userId = entry.getKey();
                    List<UserStatistics> stats = entry.getValue();
                    
                    // Агрегируем статистику пользователя за день
                    UserStatistics aggregated = aggregateDailyUserStats(stats);
                    
                    // Рассчитываем очки на основе различных метрик
                    double score = calculateUserScore(aggregated);
                    
                    return Leaderboard.builder()
                            .leaderboardType("users")
                            .period("daily")
                            .date(date)
                            .entityId(String.valueOf(userId))
                            .entityName("User_" + userId) // В реальности здесь было бы получение имени пользователя
                            .score(score)
                            .rank(0) // Будет рассчитан после сортировки
                            .previousRank(0) // Будет получен из предыдущего дня
                            .rankChange(0) // Будет рассчитан
                            .category("overall")
                            .level(calculateUserLevel(aggregated))
                            .progressPercentage(calculateUserProgress(aggregated))
                            .participationsCount(aggregated.getGameSessions())
                            .winsCount(calculateUserWins(aggregated))
                            .winRate(calculateUserWinRate(aggregated))
                            .avgRating(calculateUserAvgRating(userId))
                            .achievementsCount(calculateUserAchievements(userId))
                            .build();
                })
                .sorted((l1, l2) -> Double.compare(l2.getScore(), l1.getScore())) // Сортировка по убыванию очков
                .collect(Collectors.toList());
        
        // Устанавливаем ранги
        for (int i = 0; i < leaderboards.size(); i++) {
            Leaderboard leaderboard = leaderboards.get(i);
            leaderboard.setRank(i + 1);
            
            // Получаем предыдущий ранг
            Optional<Leaderboard> previousEntry = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateAndEntityId(
                            "users", "daily", date.minusDays(1), leaderboard.getEntityId());
            
            if (previousEntry.isPresent()) {
                leaderboard.setPreviousRank(previousEntry.get().getRank());
                leaderboard.setRankChange(leaderboard.getPreviousRank() - leaderboard.getRank());
            }
        }
        
        // Сохраняем лидерборд
        leaderboardRepository.saveAll(leaderboards);
        log.debug("Global leaderboards updated for date: {}", date);
    }

    private void updateQuestLeaderboards(LocalDate date) {
        log.debug("Updating quest leaderboards for date: {}", date);
        
        // Получаем статистику квестов за день
        List<QuestStatistics> questStats = questStatisticsRepository.findByDate(date);
        
        // Рассчитываем очки для каждого квеста
        List<Leaderboard> leaderboards = questStats.stream()
                .collect(Collectors.groupingBy(QuestStatistics::getQuestId))
                .entrySet().stream()
                .map(entry -> {
                    Long questId = entry.getKey();
                    List<QuestStatistics> stats = entry.getValue();
                    
                    // Агрегируем статистику квеста за день
                    QuestStatistics aggregated = aggregateDailyQuestStats(stats);
                    
                    // Рассчитываем очки на основе различных метрик
                    double score = calculateQuestScore(aggregated);
                    
                    return Leaderboard.builder()
                            .leaderboardType("quests")
                            .period("daily")
                            .date(date)
                            .entityId(String.valueOf(questId))
                            .entityName(aggregated.getQuestTitle())
                            .score(score)
                            .rank(0) // Будет рассчитан после сортировки
                            .previousRank(0) // Будет получен из предыдущего дня
                            .rankChange(0) // Будет рассчитан
                            .category("overall")
                            .avgRating(calculateQuestAvgRating(questId))
                            .ratingsCount(calculateQuestRatingsCount(questId))
                            .avgCompletionTime(calculateQuestAvgCompletionTime(questId))
                            .viewsCount(aggregated.getViews())
                            .likesCount(calculateQuestLikesCount(questId))
                            .build();
                })
                .sorted((l1, l2) -> Double.compare(l2.getScore(), l1.getScore())) // Сортировка по убыванию очков
                .collect(Collectors.toList());
        
        // Устанавливаем ранги
        for (int i = 0; i < leaderboards.size(); i++) {
            Leaderboard leaderboard = leaderboards.get(i);
            leaderboard.setRank(i + 1);
            
            // Получаем предыдущий ранг
            Optional<Leaderboard> previousEntry = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateAndEntityId(
                            "quests", "daily", date.minusDays(1), leaderboard.getEntityId());
            
            if (previousEntry.isPresent()) {
                leaderboard.setPreviousRank(previousEntry.get().getRank());
                leaderboard.setRankChange(leaderboard.getPreviousRank() - leaderboard.getRank());
            }
        }
        
        // Сохраняем лидерборд
        leaderboardRepository.saveAll(leaderboards);
        log.debug("Quest leaderboards updated for date: {}", date);
    }

    private void updateTeamLeaderboards(LocalDate date) {
        log.debug("Updating team leaderboards for date: {}", date);
        
        // Получаем статистику команд за день
        List<TeamStatistics> teamStats = teamStatisticsRepository.findByDate(date);
        
        // Рассчитываем очки для каждой команды
        List<Leaderboard> leaderboards = teamStats.stream()
                .collect(Collectors.groupingBy(TeamStatistics::getTeamId))
                .entrySet().stream()
                .map(entry -> {
                    UUID teamId = entry.getKey();
                    List<TeamStatistics> stats = entry.getValue();
                    
                    // Агрегируем статистику команды за день
                    TeamStatistics aggregated = aggregateDailyTeamStats(stats);
                    
                    // Рассчитываем очки на основе различных метрик
                    double score = calculateTeamScore(aggregated);
                    
                    return Leaderboard.builder()
                            .leaderboardType("teams")
                            .period("daily")
                            .date(date)
                            .entityId(String.valueOf(teamId))
                            .entityName(aggregated.getTeamName())
                            .score(score)
                            .rank(0) // Будет рассчитан после сортировки
                            .previousRank(0) // Будет получен из предыдущего дня
                            .rankChange(0) // Будет рассчитан
                            .category("overall")
                            .level(calculateTeamLevel(aggregated))
                            .progressPercentage(calculateTeamProgress(aggregated))
                            .participationsCount(aggregated.getPlayedQuests())
                            .winsCount(aggregated.getQuestWins())
                            .winRate(calculateTeamWinRate(aggregated))
                            .avgRating(calculateTeamAvgRating(teamId))
                            .build();
                })
                .sorted((l1, l2) -> Double.compare(l2.getScore(), l1.getScore())) // Сортировка по убыванию очков
                .collect(Collectors.toList());
        
        // Устанавливаем ранги
        for (int i = 0; i < leaderboards.size(); i++) {
            Leaderboard leaderboard = leaderboards.get(i);
            leaderboard.setRank(i + 1);
            
            // Получаем предыдущий ранг
            Optional<Leaderboard> previousEntry = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateAndEntityId(
                            "teams", "daily", date.minusDays(1), leaderboard.getEntityId());
            
            if (previousEntry.isPresent()) {
                leaderboard.setPreviousRank(previousEntry.get().getRank());
                leaderboard.setRankChange(leaderboard.getPreviousRank() - leaderboard.getRank());
            }
        }
        
        // Сохраняем лидерборд
        leaderboardRepository.saveAll(leaderboards);
        log.debug("Team leaderboards updated for date: {}", date);
    }

    private void cleanupOldUserStatistics(LocalDate cutoffDate) {
        log.info("Cleaning up old user statistics before: {}", cutoffDate);
        
        // Получаем старые записи
        List<UserStatistics> oldStats = userStatisticsRepository.findByDateBefore(cutoffDate);
        
        if (!oldStats.isEmpty()) {
            // Архивируем данные перед удалением (в реальности здесь было бы сохранение в архив)
            log.info("Archiving {} old user statistics records", oldStats.size());
            
            // Удаляем старые записи
            userStatisticsRepository.deleteAll(oldStats);
            log.info("Deleted {} old user statistics records", oldStats.size());
        }
    }

    private void cleanupOldQuestStatistics(LocalDate cutoffDate) {
        log.info("Cleaning up old quest statistics before: {}", cutoffDate);
        
        // Получаем старые записи
        List<QuestStatistics> oldStats = questStatisticsRepository.findByDateBefore(cutoffDate);
        
        if (!oldStats.isEmpty()) {
            // Архивируем данные перед удалением
            log.info("Archiving {} old quest statistics records", oldStats.size());
            
            // Удаляем старые записи
            questStatisticsRepository.deleteAll(oldStats);
            log.info("Deleted {} old quest statistics records", oldStats.size());
        }
    }

    private void cleanupOldTeamStatistics(LocalDate cutoffDate) {
        log.info("Cleaning up old team statistics before: {}", cutoffDate);
        
        // Получаем старые записи
        List<TeamStatistics> oldStats = teamStatisticsRepository.findByDateBefore(cutoffDate);
        
        if (!oldStats.isEmpty()) {
            // Архивируем данные перед удалением
            log.info("Archiving {} old team statistics records", oldStats.size());
            
            // Удаляем старые записи
            teamStatisticsRepository.deleteAll(oldStats);
            log.info("Deleted {} old team statistics records", oldStats.size());
        }
    }

    private void cleanupOldLeaderboards(LocalDate cutoffDate) {
        log.info("Cleaning up old leaderboards before: {}", cutoffDate);
        
        // Получаем старые записи
        List<Leaderboard> oldLeaderboards = leaderboardRepository.findByDateBefore(cutoffDate);
        
        if (!oldLeaderboards.isEmpty()) {
            // Архивируем данные перед удалением
            log.info("Archiving {} old leaderboard records", oldLeaderboards.size());
            
            // Удаляем старые записи
            leaderboardRepository.deleteAll(oldLeaderboards);
            log.info("Deleted {} old leaderboard records", oldLeaderboards.size());
        }
    }

    private void generatePerformanceMetrics(LocalDate date) {
        log.debug("Generating performance metrics for date: {}", date);
        
        // Получаем статистику за день
        List<UserStatistics> userStats = userStatisticsRepository.findByDate(date);
        List<GameStatistics> gameStats = gameStatisticsRepository.findByDate(date);
        
        // Рассчитываем метрики производительности
        double avgSessionDuration = gameStats.stream()
                .filter(gs -> gs.getDurationMinutes() != null)
                .mapToLong(GameStatistics::getDurationMinutes)
                .average()
                .orElse(0.0);
        
        double completionRate = gameStats.stream()
                .mapToDouble(gs -> "completed".equals(gs.getStatus()) ? 1.0 : 0.0)
                .average()
                .orElse(0.0) * 100.0;
        
        long totalActiveUsers = userStats.stream()
                .filter(us -> us.getGameSessions() > 0)
                .count();
        
        // Сохраняем метрики производительности
        SystemStatistics performanceMetric = SystemStatistics.builder()
                .date(date)
                .category("performance")
                .metric("avg_session_duration")
                .value(avgSessionDuration)
                .unit("minutes")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(performanceMetric);
        
        SystemStatistics completionMetric = SystemStatistics.builder()
                .date(date)
                .category("performance")
                .metric("completion_rate")
                .value(completionRate)
                .unit("percent")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(completionMetric);
        
        SystemStatistics activeUsersMetric = SystemStatistics.builder()
                .date(date)
                .category("performance")
                .metric("active_users")
                .value((double) totalActiveUsers)
                .unit("count")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(activeUsersMetric);
        
        log.debug("Performance metrics generated for date: {}", date);
    }

    private void generateBusinessMetrics(LocalDate date) {
        log.debug("Generating business metrics for date: {}", date);
        
        // Получаем статистику за день
        List<UserStatistics> userStats = userStatisticsRepository.findByDate(date);
        List<QuestStatistics> questStats = questStatisticsRepository.findByDate(date);
        List<TeamStatistics> teamStats = teamStatisticsRepository.findByDate(date);
        
        // Рассчитываем бизнес-метрики
        long newRegistrations = userStats.stream().mapToInt(UserStatistics::getRegistrations).sum();
        long newQuests = questStats.stream().mapToInt(QuestStatistics::getCreations).sum();
        long newTeams = teamStats.stream().mapToInt(TeamStatistics::getCreations).sum();
        
        long totalGameTime = userStats.stream().mapToLong(UserStatistics::getTotalGameTimeMinutes).sum();
        long totalCodeSubmissions = userStats.stream()
                .mapToInt(us -> us.getSuccessfulCodeSubmissions() + us.getFailedCodeSubmissions())
                .sum();
        
        // Сохраняем бизнес-метрики
        SystemStatistics registrationsMetric = SystemStatistics.builder()
                .date(date)
                .category("business")
                .metric("new_registrations")
                .value((double) newRegistrations)
                .unit("count")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(registrationsMetric);
        
        SystemStatistics questsMetric = SystemStatistics.builder()
                .date(date)
                .category("business")
                .metric("new_quests")
                .value((double) newQuests)
                .unit("count")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(questsMetric);
        
        SystemStatistics teamsMetric = SystemStatistics.builder()
                .date(date)
                .category("business")
                .metric("new_teams")
                .value((double) newTeams)
                .unit("count")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(teamsMetric);
        
        SystemStatistics engagementMetric = SystemStatistics.builder()
                .date(date)
                .category("business")
                .metric("total_game_time")
                .value((double) totalGameTime)
                .unit("minutes")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(engagementMetric);
        
        SystemStatistics codeMetric = SystemStatistics.builder()
                .date(date)
                .category("business")
                .metric("code_submissions")
                .value((double) totalCodeSubmissions)
                .unit("count")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(codeMetric);
        
        log.debug("Business metrics generated for date: {}", date);
    }

    private void generateUsageMetrics(LocalDate date) {
        log.debug("Generating usage metrics for date: {}", date);
        
        // Получаем статистику за день
        List<UserStatistics> userStats = userStatisticsRepository.findByDate(date);
        List<FileStatistics> fileStats = fileStatisticsRepository.findByDate(date);
        
        // Рассчитываем метрики использования
        long totalLogins = userStats.stream().mapToInt(UserStatistics::getLogins).sum();
        long totalGameSessions = userStats.stream().mapToInt(UserStatistics::getGameSessions).sum();
        long totalFileUploads = fileStats.stream().mapToInt(FileStatistics::getUploads).sum();
        long totalFileDownloads = fileStats.stream().mapToInt(FileStatistics::getDownloads).sum();
        
        long totalFileSize = fileStats.stream().mapToLong(FileStatistics::getTotalSizeBytes).sum();
        
        // Сохраняем метрики использования
        SystemStatistics loginsMetric = SystemStatistics.builder()
                .date(date)
                .category("usage")
                .metric("total_logins")
                .value((double) totalLogins)
                .unit("count")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(loginsMetric);
        
        SystemStatistics sessionsMetric = SystemStatistics.builder()
                .date(date)
                .category("usage")
                .metric("total_game_sessions")
                .value((double) totalGameSessions)
                .unit("count")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(sessionsMetric);
        
        SystemStatistics uploadsMetric = SystemStatistics.builder()
                .date(date)
                .category("usage")
                .metric("file_uploads")
                .value((double) totalFileUploads)
                .unit("count")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(uploadsMetric);
        
        SystemStatistics downloadsMetric = SystemStatistics.builder()
                .date(date)
                .category("usage")
                .metric("file_downloads")
                .value((double) totalFileDownloads)
                .unit("count")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(downloadsMetric);
        
        SystemStatistics storageMetric = SystemStatistics.builder()
                .date(date)
                .category("usage")
                .metric("storage_used")
                .value((double) totalFileSize)
                .unit("bytes")
                .createdAt(LocalDateTime.now())
                .build();
        
        systemStatisticsRepository.save(storageMetric);
        
        log.debug("Usage metrics generated for date: {}", date);
    }

    private void validateUserStatisticsIntegrity(LocalDate date) {
        log.debug("Validating user statistics integrity for date: {}", date);
        
        List<UserStatistics> userStats = userStatisticsRepository.findByDate(date);
        List<String> integrityIssues = new ArrayList<>();
        
        for (UserStatistics stat : userStats) {
            // Проверка на отрицательные значения
            if (stat.getGameSessions() < 0) {
                integrityIssues.add("Negative game sessions for user " + stat.getUserId());
            }
            
            if (stat.getCompletedQuests() < 0) {
                integrityIssues.add("Negative completed quests for user " + stat.getUserId());
            }
            
            if (stat.getTotalGameTimeMinutes() < 0) {
                integrityIssues.add("Negative game time for user " + stat.getUserId());
            }
            
            // Проверка логических соотношений
            if (stat.getCompletedQuests() > stat.getGameSessions()) {
                integrityIssues.add("More completed quests than sessions for user " + stat.getUserId());
            }
            
            if (stat.getSuccessfulCodeSubmissions() + stat.getFailedCodeSubmissions() < 0) {
                integrityIssues.add("Invalid code submissions count for user " + stat.getUserId());
            }
        }
        
        if (!integrityIssues.isEmpty()) {
            log.warn("Found {} integrity issues in user statistics for date: {}", integrityIssues.size(), date);
            integrityIssues.forEach(issue -> log.warn("Integrity issue: {}", issue));
            
            // В реальности здесь было бы создание задачи для исправления данных
        } else {
            log.debug("User statistics integrity validation passed for date: {}", date);
        }
    }

    private void validateQuestStatisticsIntegrity(LocalDate date) {
        log.debug("Validating quest statistics integrity for date: {}", date);
        
        List<QuestStatistics> questStats = questStatisticsRepository.findByDate(date);
        List<String> integrityIssues = new ArrayList<>();
        
        for (QuestStatistics stat : questStats) {
            // Проверка на отрицательные значения
            if (stat.getViews() < 0) {
                integrityIssues.add("Negative views for quest " + stat.getQuestId());
            }
            
            if (stat.getStarts() < 0) {
                integrityIssues.add("Negative starts for quest " + stat.getQuestId());
            }
            
            if (stat.getCompletions() < 0) {
                integrityIssues.add("Negative completions for quest " + stat.getQuestId());
            }
            
            // Проверка логических соотношений
            if (stat.getUniqueViews() > stat.getViews()) {
                integrityIssues.add("More unique views than total views for quest " + stat.getQuestId());
            }
            
            if (stat.getCompletions() > stat.getStarts()) {
                integrityIssues.add("More completions than starts for quest " + stat.getQuestId());
            }
            
            if (stat.getUniqueParticipants() > stat.getStarts()) {
                integrityIssues.add("More unique participants than starts for quest " + stat.getQuestId());
            }
        }
        
        if (!integrityIssues.isEmpty()) {
            log.warn("Found {} integrity issues in quest statistics for date: {}", integrityIssues.size(), date);
            integrityIssues.forEach(issue -> log.warn("Integrity issue: {}", issue));
        } else {
            log.debug("Quest statistics integrity validation passed for date: {}", date);
        }
    }

    private void validateTeamStatisticsIntegrity(LocalDate date) {
        log.debug("Validating team statistics integrity for date: {}", date);
        
        List<TeamStatistics> teamStats = teamStatisticsRepository.findByDate(date);
        List<String> integrityIssues = new ArrayList<>();
        
        for (TeamStatistics stat : teamStats) {
            // Проверка на отрицательные значения
            if (stat.getPlayedQuests() < 0) {
                integrityIssues.add("Negative played quests for team " + stat.getTeamId());
            }
            
            if (stat.getCompletedQuests() < 0) {
                integrityIssues.add("Negative completed quests for team " + stat.getTeamId());
            }
            
            if (stat.getQuestWins() < 0) {
                integrityIssues.add("Negative quest wins for team " + stat.getTeamId());
            }
            
            // Проверка логических соотношений
            if (stat.getCompletedQuests() > stat.getPlayedQuests()) {
                integrityIssues.add("More completed quests than played for team " + stat.getTeamId());
            }
            
            if (stat.getQuestWins() > stat.getCompletedQuests()) {
                integrityIssues.add("More wins than completed quests for team " + stat.getTeamId());
            }
            
            if (stat.getMemberAdditions() < 0 || stat.getMemberRemovals() < 0) {
                integrityIssues.add("Negative member changes for team " + stat.getTeamId());
            }
        }
        
        if (!integrityIssues.isEmpty()) {
            log.warn("Found {} integrity issues in team statistics for date: {}", integrityIssues.size(), date);
            integrityIssues.forEach(issue -> log.warn("Integrity issue: {}", issue));
        } else {
            log.debug("Team statistics integrity validation passed for date: {}", date);
        }
    }

    private void validateAggregatedStatisticsIntegrity(LocalDate date) {
        log.debug("Validating aggregated statistics integrity for date: {}", date);
        
        List<DailyAggregatedStatistics> aggregatedStats = dailyAggregatedStatisticsRepository.findByDate(date);
        List<String> integrityIssues = new ArrayList<>();
        
        for (DailyAggregatedStatistics stat : aggregatedStats) {
            // Проверка на отрицательные значения
            if (stat.getTotalUsers() < 0) {
                integrityIssues.add("Negative total users in aggregated statistics");
            }
            
            if (stat.getActiveUsers() < 0) {
                integrityIssues.add("Negative active users in aggregated statistics");
            }
            
            if (stat.getTotalQuests() < 0) {
                integrityIssues.add("Negative total quests in aggregated statistics");
            }
            
            // Проверка логических соотношений
            if (stat.getActiveUsers() > stat.getTotalUsers()) {
                integrityIssues.add("More active users than total users in aggregated statistics");
            }
            
            if (stat.getCompletedQuests() > stat.getTotalQuests()) {
                integrityIssues.add("More completed quests than total quests in aggregated statistics");
            }
            
            if (stat.getCompletedGameSessions() > stat.getTotalGameSessions()) {
                integrityIssues.add("More completed sessions than total sessions in aggregated statistics");
            }
        }
        
        if (!integrityIssues.isEmpty()) {
            log.warn("Found {} integrity issues in aggregated statistics for date: {}", integrityIssues.size(), date);
            integrityIssues.forEach(issue -> log.warn("Integrity issue: {}", issue));
        } else {
            log.debug("Aggregated statistics integrity validation passed for date: {}", date);
        }
    }

    private void aggregateWeeklyAggregatedStatistics(LocalDate weekStart, LocalDate weekEnd) {
        log.info("Aggregating weekly statistics from {} to {}", weekStart, weekEnd);
        
        // Получаем дневную агрегированную статистику за неделю
        List<DailyAggregatedStatistics> dailyStats = dailyAggregatedStatisticsRepository
                .findByDateBetweenOrderByDateDesc(weekStart, weekEnd);
        
        if (dailyStats.isEmpty()) {
            log.warn("No daily statistics found for weekly aggregation from {} to {}", weekStart, weekEnd);
            return;
        }
        
        // Агрегируем недельные метрики
        long totalUsers = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getTotalUsers).sum();
        long activeUsers = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getActiveUsers).sum();
        long totalQuests = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getTotalQuests).sum();
        long completedQuests = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getCompletedQuests).sum();
        long totalTeams = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getTotalTeams).sum();
        long totalGameSessions = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getTotalGameSessions).sum();
        long completedGameSessions = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getCompletedGameSessions).sum();
        
        // Создаем недельную агрегированную запись
        DailyAggregatedStatistics weeklyAggregated = DailyAggregatedStatistics.builder()
                .date(weekEnd) // Используем конец недели как дату
                .aggregationType("weekly")
                .activeUsers(activeUsers)
                .completedQuests(completedQuests)
                .completedGameSessions(completedGameSessions)
                .totalQuests(totalQuests)
                .totalTeams(totalTeams)
                .totalGameSessions(totalGameSessions)
                .totalUsers(totalUsers)
                .totalGameTime(dailyStats.stream().mapToLong(DailyAggregatedStatistics::getTotalGameTime).sum())
                .build();
        
        dailyAggregatedStatisticsRepository.save(weeklyAggregated);
        log.info("Weekly aggregated statistics saved from {} to {}", weekStart, weekEnd);
    }

    private void aggregateMonthlyAggregatedStatistics(LocalDate monthStart, LocalDate monthEnd) {
        log.info("Aggregating monthly statistics from {} to {}", monthStart, monthEnd);
        
        // Получаем дневную агрегированную статистику за месяц
        List<DailyAggregatedStatistics> dailyStats = dailyAggregatedStatisticsRepository
                .findByDateBetweenOrderByDateDesc(monthStart, monthEnd);
        
        if (dailyStats.isEmpty()) {
            log.warn("No daily statistics found for monthly aggregation from {} to {}", monthStart, monthEnd);
            return;
        }
        
        // Агрегируем месячные метрики
        long totalUsers = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getTotalUsers).sum();
        long activeUsers = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getActiveUsers).sum();
        long totalQuests = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getTotalQuests).sum();
        long completedQuests = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getCompletedQuests).sum();
        long totalTeams = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getTotalTeams).sum();
        long totalGameSessions = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getTotalGameSessions).sum();
        long completedGameSessions = dailyStats.stream().mapToLong(DailyAggregatedStatistics::getCompletedGameSessions).sum();
        
        // Создаем месячную агрегированную запись
        DailyAggregatedStatistics monthlyAggregated = DailyAggregatedStatistics.builder()
                .date(monthEnd) // Используем конец месяца как дату
                .aggregationType("monthly")
                .activeUsers(activeUsers)
                .completedQuests(completedQuests)
                .completedGameSessions(completedGameSessions)
                .totalQuests(totalQuests)
                .totalTeams(totalTeams)
                .totalUsers(totalUsers)
                .totalGameSessions(totalGameSessions)
                .totalGameTime(dailyStats.stream().mapToLong(DailyAggregatedStatistics::getTotalGameTime).sum())
                .build();
        
        dailyAggregatedStatisticsRepository.save(monthlyAggregated);
        log.info("Monthly aggregated statistics saved from {} to {}", monthStart, monthEnd);
    }

    // Вспомогательные методы для конвертации в Map
    private Map<String, Object> convertUserStatisticsToMap(UserStatistics stats) {
        return Map.ofEntries(
                Map.entry("userId", stats.getUserId()),
                Map.entry("date", stats.getDate()),
                Map.entry("registrations", stats.getRegistrations()),
                Map.entry("logins", stats.getLogins()),
                Map.entry("gameSessions", stats.getGameSessions()),
                Map.entry("completedQuests", stats.getCompletedQuests()),
                Map.entry("createdQuests", stats.getCreatedQuests()),
                Map.entry("createdTeams", stats.getCreatedTeams()),
                Map.entry("teamMemberships", stats.getTeamMemberships()),
                Map.entry("totalGameTimeMinutes", stats.getTotalGameTimeMinutes()),
                Map.entry("successfulCodeSubmissions", stats.getSuccessfulCodeSubmissions()),
                Map.entry("failedCodeSubmissions", stats.getFailedCodeSubmissions()),
                Map.entry("completedLevels", stats.getCompletedLevels()),
                Map.entry("lastActiveAt", stats.getLastActiveAt())
        );
    }

    private Map<String, Object> convertQuestStatisticsToMap(QuestStatistics stats) {
        return Map.ofEntries(
            Map.entry("questId", stats.getQuestId()),
            Map.entry("questTitle", stats.getQuestTitle()),
            Map.entry("authorId", stats.getAuthorId()),
            Map.entry("date", stats.getDate()),
            Map.entry("creations", stats.getCreations()),
            Map.entry("updates", stats.getUpdates()),
            Map.entry("publications", stats.getPublications()),
            Map.entry("deletions", stats.getDeletions()),
            Map.entry("views", stats.getViews()),
            Map.entry("uniqueViews", stats.getUniqueViews()),
            Map.entry("starts", stats.getStarts()),
            Map.entry("completions", stats.getCompletions()),
            Map.entry("uniqueParticipants", stats.getUniqueParticipants()),
            Map.entry("totalGameTimeMinutes", stats.getTotalGameTimeMinutes())
        );
    }

    private Map<String, Object> convertTeamStatisticsToMap(TeamStatistics stats) {
        return Map.ofEntries(
                Map.entry("teamId", stats.getTeamId()),
                Map.entry("teamName", stats.getTeamName()),
                Map.entry("captainId", stats.getCaptainId()),
                Map.entry("date", stats.getDate()),
                Map.entry("creations", stats.getCreations()),
                Map.entry("updates", stats.getUpdates()),
                Map.entry("deletions", stats.getDeletions()),
                Map.entry("memberAdditions", stats.getMemberAdditions()),
                Map.entry("memberRemovals", stats.getMemberRemovals()),
                Map.entry("playedQuests", stats.getPlayedQuests()),
                Map.entry("completedQuests", stats.getCompletedQuests()),
                Map.entry("questWins", stats.getQuestWins()),
                Map.entry("totalGameTimeMinutes", stats.getTotalGameTimeMinutes()),
                Map.entry("successfulCodeSubmissions", stats.getSuccessfulCodeSubmissions()),
                Map.entry("failedCodeSubmissions", stats.getFailedCodeSubmissions()),
                Map.entry("completedLevels", stats.getCompletedLevels()),
                Map.entry("lastActivityAt", stats.getLastActivityAt())
        );
    }

    // Вспомогательные методы для расчета очков и метрик

    private double calculateUserScore(UserStatistics stats) {
        if (stats == null) return 0.0;
        
        double score = 0.0;
        
        // Очки за завершенные квесты
        score += stats.getCompletedQuests() * 100.0;
        
        // Очки за созданные квесты
        score += stats.getCreatedQuests() * 50.0;
        
        // Очки за игровое время (1 очко за 10 минут)
        score += stats.getTotalGameTimeMinutes() / 10.0;
        
        // Очки за успешные отправки кода
        score += stats.getSuccessfulCodeSubmissions() * 10.0;
        
        // Очки за завершенные уровни
        score += stats.getCompletedLevels() * 25.0;
        
        // Бонус за создание команд
        score += stats.getCreatedTeams() * 30.0;
        
        return score;
    }

    private double calculateQuestScore(QuestStatistics stats) {
        if (stats == null) return 0.0;
        
        double score = 0.0;
        
        // Очки за просмотры
        score += stats.getViews() * 1.0;
        
        // Очки за уникальные просмотры
        score += stats.getUniqueViews() * 2.0;
        
        // Очки за запуски
        score += stats.getStarts() * 5.0;
        
        // Очки за завершения
        score += stats.getCompletions() * 20.0;
        
        // Очки за уникальных участников
        score += stats.getUniqueParticipants() * 15.0;
        
        // Очки за игровое время
        score += stats.getTotalGameTimeMinutes() / 5.0;
        
        return score;
    }

    private double calculateTeamScore(TeamStatistics stats) {
        if (stats == null) return 0.0;
        
        double score = 0.0;
        
        // Очки за сыгранные квесты
        score += stats.getPlayedQuests() * 10.0;
        
        // Очки за завершенные квесты
        score += stats.getCompletedQuests() * 30.0;
        
        // Очки за победы
        score += stats.getQuestWins() * 50.0;
        
        // Очки за игровое время
        score += stats.getTotalGameTimeMinutes() / 8.0;
        
        // Очки за успешные отправки кода
        score += stats.getSuccessfulCodeSubmissions() * 5.0;
        
        // Очки за завершенные уровни
        score += stats.getCompletedLevels() * 15.0;
        
        return score;
    }

    private int calculateUserLevel(UserStatistics stats) {
        if (stats == null) return 1;
        
        double score = calculateUserScore(stats);
        
        if (score < 100) return 1;
        if (score < 300) return 2;
        if (score < 600) return 3;
        if (score < 1000) return 4;
        if (score < 1500) return 5;
        if (score < 2500) return 6;
        if (score < 4000) return 7;
        if (score < 6000) return 8;
        if (score < 8500) return 9;
        return 10;
    }

    private double calculateUserProgress(UserStatistics stats) {
        if (stats == null) return 0.0;
        
        double score = calculateUserScore(stats);
        int currentLevel = calculateUserLevel(stats);
        
        // Определяем границы для текущего уровня
        int levelMinScore = getLevelMinScore(currentLevel);
        int levelMaxScore = getLevelMaxScore(currentLevel);
        
        if (score >= levelMaxScore) return 100.0;
        
        return ((score - levelMinScore) / (double)(levelMaxScore - levelMinScore)) * 100.0;
    }

    private int getLevelMinScore(int level) {
        return switch (level) {
            case 1 -> 0;
            case 2 -> 100;
            case 3 -> 300;
            case 4 -> 600;
            case 5 -> 1000;
            case 6 -> 1500;
            case 7 -> 2500;
            case 8 -> 4000;
            case 9 -> 6000;
            case 10 -> 8500;
            default -> 0;
        };
    }

    private int getLevelMaxScore(int level) {
        return switch (level) {
            case 1 -> 99;
            case 2 -> 299;
            case 3 -> 599;
            case 4 -> 999;
            case 5 -> 1499;
            case 6 -> 2499;
            case 7 -> 3999;
            case 8 -> 5999;
            case 9 -> 8499;
            case 10 -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    private int calculateUserWins(UserStatistics stats) {
        if (stats == null) return 0;
        
        // В реальной реализации здесь был бы запрос к данным о победах
        // Пока используем упрощенную логику на основе завершенных квестов
        return stats.getCompletedQuests() / 3; // Предполагаем, что каждая 3-я победа
    }

    private double calculateUserWinRate(UserStatistics stats) {
        if (stats == null || stats.getGameSessions() == 0) return 0.0;
        
        int wins = calculateUserWins(stats);
        return (wins / (double) stats.getGameSessions()) * 100.0;
    }

    private double calculateUserAvgRating(UUID userId) {
        if (userId == null) return 0.0;
        
        // В реальной реализации здесь был бы запрос к данным о рейтингах
        // Пока возвращаем средний рейтинг по умолчанию
        return 4.2;
    }

    private int calculateUserAchievements(UUID userId) {
        if (userId == null) return 0;
        
        // В реальной реализации здесь был бы запрос к данным о достижениях
        // Пока возвращаем количество на основе ID пользователя
        return 0;
    }

    private int calculateTeamLevel(TeamStatistics stats) {
        if (stats == null) return 1;
        
        double score = calculateTeamScore(stats);
        
        if (score < 50) return 1;
        if (score < 150) return 2;
        if (score < 300) return 3;
        if (score < 500) return 4;
        if (score < 750) return 5;
        if (score < 1250) return 6;
        if (score < 2000) return 7;
        if (score < 3000) return 8;
        if (score < 4250) return 9;
        return 10;
    }

    private double calculateTeamProgress(TeamStatistics stats) {
        if (stats == null) return 0.0;
        
        double score = calculateTeamScore(stats);
        int currentLevel = calculateTeamLevel(stats);
        
        // Используем те же границы уровней, что и для пользователей
        int levelMinScore = getLevelMinScore(currentLevel);
        int levelMaxScore = getLevelMaxScore(currentLevel);
        
        if (score >= levelMaxScore) return 100.0;
        
        return ((score - levelMinScore) / (double)(levelMaxScore - levelMinScore)) * 100.0;
    }

    private double calculateTeamWinRate(TeamStatistics stats) {
        if (stats == null || stats.getPlayedQuests() == 0) return 0.0;
        
        return (stats.getQuestWins() / (double) stats.getPlayedQuests()) * 100.0;
    }

    private double calculateTeamAvgRating(UUID teamId) {
        if (teamId == null) return 0.0;
        
        // В реальной реализации здесь был бы запрос к данным о рейтингах команд
        // Пока возвращаем средний рейтинг по умолчанию
        return 4.0;
    }

    private double calculateQuestAvgRating(Long questId) {
        if (questId == null) return 0.0;
        
        // В реальной реализации здесь был бы запрос к данным о рейтингах квестов
        // Пока возвращаем средний рейтинг по умолчанию
        return 4.1;
    }

    private int calculateQuestRatingsCount(Long questId) {
        if (questId == null) return 0;
        
        // В реальной реализации здесь был бы запрос к данным о рейтингах квестов
        // Пока возвращаем количество на основе ID квеста
        return (int) (questId % 50) + 10;
    }

    private double calculateQuestAvgCompletionTime(Long questId) {
        if (questId == null) return 0.0;
        
        // В реальной реализации здесь был бы запрос к данным о времени прохождения
        // Пока возвращаем среднее время на основе ID квеста
        return 30.0 + (questId % 60); // 30-90 минут
    }

    private int calculateQuestLikesCount(Long questId) {
        if (questId == null) return 0;
        
        // В реальной реализации здесь был бы запрос к данным о лайках
        // Пока возвращаем количество на основе ID квеста
        return (int) (questId % 100) + 20;
    }
}