package dn.quest.statistics.service.impl;

import dn.quest.statistics.dto.StatisticsRequestDTO;
import dn.quest.statistics.dto.UserStatisticsDTO;
import dn.quest.statistics.entity.*;
import dn.quest.statistics.repository.*;
import dn.quest.statistics.service.CacheService;
import dn.quest.statistics.service.StatisticsQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для запросов статистики
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatisticsQueryServiceImpl implements StatisticsQueryService {

    private final UserStatisticsRepository userStatisticsRepository;
    private final QuestStatisticsRepository questStatisticsRepository;
    private final TeamStatisticsRepository teamStatisticsRepository;
    private final GameStatisticsRepository gameStatisticsRepository;
    private final SystemStatisticsRepository systemStatisticsRepository;
    private final DailyAggregatedStatisticsRepository dailyAggregatedStatisticsRepository;
    private final CacheService cacheService;

    @Override
    @Cacheable(value = "platform-overview", key = "{#startDate, #endDate}")
    public Map<String, Object> getPlatformOverview(LocalDate startDate, LocalDate endDate) {
        log.debug("Getting platform overview for period: {} to {}", startDate, endDate);
        
        Map<String, Object> overview = new HashMap<>();
        
        try {
            // Получаем общую статистику пользователей
            Long totalUsers = userStatisticsRepository.countDistinctUsersByDateBetween(
                    startDate != null ? startDate : LocalDate.now().minusDays(30),
                    endDate != null ? endDate : LocalDate.now());
            
            // Получаем общую статистику квестов
            Long totalQuests = questStatisticsRepository.countDistinctQuestsByDateBetween(
                    startDate != null ? startDate : LocalDate.now().minusDays(30),
                    endDate != null ? endDate : LocalDate.now());
            
            // Получаем общую статистику игровых сессий
            Long totalSessions = gameStatisticsRepository.countDistinctSessionsByDateBetween(
                    startDate != null ? startDate : LocalDate.now().minusDays(30),
                    endDate != null ? endDate : LocalDate.now());
            
            // Получаем общую статистику команд
            Long totalTeams = teamStatisticsRepository.countDistinctTeamsByDateBetween(
                    startDate != null ? startDate : LocalDate.now().minusDays(30),
                    endDate != null ? endDate : LocalDate.now());
            
            overview.put("totalUsers", totalUsers != null ? totalUsers : 0L);
            overview.put("totalQuests", totalQuests != null ? totalQuests : 0L);
            overview.put("totalSessions", totalSessions != null ? totalSessions : 0L);
            overview.put("totalTeams", totalTeams != null ? totalTeams : 0L);
            
            // Добавляем тренды
            overview.put("trends", getMetricTrends(Arrays.asList("users", "quests", "sessions"), "days", 7));
            
            log.info("Generated platform overview: {}", overview);
            
        } catch (Exception e) {
            log.error("Error getting platform overview", e);
            overview.put("error", "Failed to generate overview");
        }
        
        return overview;
    }

    @Override
    @Cacheable(value = "user-statistics", key = "{#userId, #date}")
    public UserStatisticsDTO getUserStatistics(UUID userId, LocalDate date) {
        log.debug("Getting statistics for user: {} for date: {}", userId, date);
        
        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            
            UserStatistics stats = userStatisticsRepository.findByUserIdAndDate(userId, targetDate)
                    .orElse(UserStatistics.builder()
                            .userId(userId)
                            .date(targetDate)
                            .registrations(0)
                            .logins(0)
                            .gameSessions(0)
                            .completedQuests(0)
                            .createdQuests(0)
                            .createdTeams(0)
                            .teamMemberships(0)
                            .totalGameTimeMinutes(0L)
                            .uploadedFiles(0)
                            .totalFileSizeBytes(0L)
                            .successfulCodeSubmissions(0)
                            .failedCodeSubmissions(0)
                            .completedLevels(0)
                            .lastActiveAt(LocalDateTime.now())
                            .build());
            
            return UserStatisticsDTO.builder()
                    .userId(stats.getUserId())
                    .date(stats.getDate())
                    .registrations(stats.getRegistrations())
                    .logins(stats.getLogins())
                    .gameSessions(stats.getGameSessions())
                    .completedQuests(stats.getCompletedQuests())
                    .createdQuests(stats.getCreatedQuests())
                    .createdTeams(stats.getCreatedTeams())
                    .teamMemberships(stats.getTeamMemberships())
                    .totalGameTimeMinutes(stats.getTotalGameTimeMinutes())
                    .uploadedFiles(stats.getUploadedFiles())
                    .totalFileSizeBytes(stats.getTotalFileSizeBytes())
                    .successfulCodeSubmissions(stats.getSuccessfulCodeSubmissions())
                    .failedCodeSubmissions(stats.getFailedCodeSubmissions())
                    .completedLevels(stats.getCompletedLevels())
                    .lastActiveAt(stats.getLastActiveAt())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error getting user statistics for user: {}", userId, e);
            throw new RuntimeException("Failed to get user statistics", e);
        }
    }

    @Override
    @Cacheable(value = "user-statistics-period", key = "{#userId, #startDate, #endDate, #pageable}")
    public Page<UserStatisticsDTO> getUserStatisticsForPeriod(UUID userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Getting statistics for user: {} for period: {} to {}", userId, startDate, endDate);
        
        try {
            Page<UserStatistics> statsPage = userStatisticsRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, startDate, endDate, pageable);
            
            List<UserStatisticsDTO> dtoList = statsPage.getContent().stream()
                    .map(stats -> UserStatisticsDTO.builder()
                            .userId(stats.getUserId())
                            .date(stats.getDate())
                            .registrations(stats.getRegistrations())
                            .logins(stats.getLogins())
                            .gameSessions(stats.getGameSessions())
                            .completedQuests(stats.getCompletedQuests())
                            .createdQuests(stats.getCreatedQuests())
                            .createdTeams(stats.getCreatedTeams())
                            .teamMemberships(stats.getTeamMemberships())
                            .totalGameTimeMinutes(stats.getTotalGameTimeMinutes())
                            .uploadedFiles(stats.getUploadedFiles())
                            .totalFileSizeBytes(stats.getTotalFileSizeBytes())
                            .successfulCodeSubmissions(stats.getSuccessfulCodeSubmissions())
                            .failedCodeSubmissions(stats.getFailedCodeSubmissions())
                            .completedLevels(stats.getCompletedLevels())
                            .lastActiveAt(stats.getLastActiveAt())
                            .build())
                    .collect(Collectors.toList());
            
            return new PageImpl<>(dtoList, pageable, statsPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error getting user statistics for period for user: {}", userId, e);
            throw new RuntimeException("Failed to get user statistics for period", e);
        }
    }

    @Override
    @Cacheable(value = "quest-statistics", key = "{#questId, #date}")
    public Map<String, Object> getQuestStatistics(UUID questId, LocalDate date) {
        log.debug("Getting statistics for quest: {} for date: {}", questId, date);
        
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            
            QuestStatistics stats = questStatisticsRepository.findByQuestIdAndDate(questId, targetDate)
                    .orElse(QuestStatistics.builder()
                            .questId(questId)
                            .date(targetDate)
                            .views(0)
                            .starts(0)
                            .completions(0)
                            .avgCompletionTimeMinutes(0.0)
                            .ratingCount(0)
                            .build());
            
            statistics.put("questId", stats.getQuestId());
            statistics.put("date", stats.getDate());
            statistics.put("views", stats.getViews());
            statistics.put("starts", stats.getStarts());
            statistics.put("completions", stats.getCompletions());
            statistics.put("averageCompletionTimeMinutes", stats.getAvgCompletionTimeMinutes());
            statistics.put("rating", stats.getCurrentRating());
            statistics.put("ratingCount", stats.getRatingCount());
            
            // Добавляем процент завершения
            if (stats.getStarts() > 0) {
                double completionRate = (double) stats.getCompletions() / stats.getStarts() * 100;
                statistics.put("completionRate", completionRate);
            } else {
                statistics.put("completionRate", 0.0);
            }
            
        } catch (Exception e) {
            log.error("Error getting quest statistics for quest: {}", questId, e);
            statistics.put("error", "Failed to get quest statistics");
        }
        
        return statistics;
    }

    @Override
    @Cacheable(value = "team-statistics", key = "{#teamId, #date}")
    public Map<String, Object> getTeamStatistics(UUID teamId, LocalDate date) {
        log.debug("Getting statistics for team: {} for date: {}", teamId, date);
        
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            
            TeamStatistics stats = teamStatisticsRepository.findByTeamIdAndDate(teamId, targetDate)
                    .orElse(TeamStatistics.builder()
                            .teamId(teamId)
                            .teamName("")
                            .date(targetDate)
                            .currentMembersCount(0)
                            .totalUniqueMembers(0)
                            .completedQuests(0)
                            .currentRating(0.0)
                            .playedQuests(0)
                            .questWins(0)
                            .build());
            
            statistics.put("teamId", stats.getTeamId());
            statistics.put("teamName", stats.getTeamName());
            statistics.put("date", stats.getDate());
            statistics.put("currentMembersCount", stats.getCurrentMembersCount());
            statistics.put("totalUniqueMembers", stats.getTotalUniqueMembers());
            statistics.put("currentRating", stats.getCurrentRating());
            statistics.put("completedQuests", stats.getCompletedQuests());
            statistics.put("playedQuests", stats.getPlayedQuests());
            statistics.put("questWins", stats.getQuestWins());

            // Добавляем процент активных членов
            if (stats.getTotalUniqueMembers() > 0) {
                double activeRate = (double) stats.getCurrentMembersCount() / stats.getTotalUniqueMembers() * 100;
                statistics.put("activeRate", activeRate);
            } else {
                statistics.put("activeRate", 0.0);
            }
            
        } catch (Exception e) {
            log.error("Error getting team statistics for team: {}", teamId, e);
            statistics.put("error", "Failed to get team statistics");
        }
        
        return statistics;
    }

    @Override
    public Map<String, Object> getCustomStatistics(StatisticsRequestDTO request) {
        log.debug("Getting custom statistics with request: {}", request);
        
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // В зависимости от типа статистики вызываем соответствующие методы
            switch (request.getStatisticsType()) {
                case "user":
                    if (request.getUserId() != null) {
                        UserStatisticsDTO userStats = getUserStatistics(request.getUserId(), request.getEndDate());
                        statistics.put("userStatistics", userStats);
                    }
                    break;
                case "quest":
                    if (request.getQuestId() != null) {
                        Map<String, Object> questStats = getQuestStatistics(request.getQuestId(), request.getEndDate());
                        statistics.put("questStatistics", questStats);
                    }
                    break;
                case "team":
                    if (request.getTeamId() != null) {
                        Map<String, Object> teamStats = getTeamStatistics(request.getTeamId(), request.getEndDate());
                        statistics.put("teamStatistics", teamStats);
                    }
                    break;
                case "platform":
                    Map<String, Object> platformStats = getPlatformOverview(request.getStartDate(), request.getEndDate());
                    statistics.put("platformStatistics", platformStats);
                    break;
                default:
                    statistics.put("error", "Unknown statistics type: " + request.getStatisticsType());
            }
            
            statistics.put("request", request);
            
        } catch (Exception e) {
            log.error("Error getting custom statistics", e);
            statistics.put("error", "Failed to get custom statistics");
        }
        
        return statistics;
    }

    @Override
    @Cacheable(value = "top-users", key = "{#metric, #limit, #date}")
    public List<Map<String, Object>> getTopUsers(String metric, int limit, LocalDate date) {
        log.debug("Getting top users by metric: {} limit: {} date: {}", metric, limit, date);
        
        List<Map<String, Object>> topUsers = new ArrayList<>();
        
        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            Pageable pageable = PageRequest.of(0, limit, Sort.Direction.DESC);

            // В зависимости от метрики получаем топ пользователей
            switch (metric.toLowerCase()) {
                case "completedquests":
                    topUsers = userStatisticsRepository.findTopUsersByCompletedQuests(targetDate, pageable)
                            .stream()
                            .map(this::convertUserStatsToMap)
                            .collect(Collectors.toList());
                    break;
                case "gamesessions":
                    topUsers = userStatisticsRepository.findTopUsersByGameSessions(targetDate, pageable)
                            .stream()
                            .map(this::convertUserStatsToMap)
                            .collect(Collectors.toList());
                    break;
                case "createdquests":
                    topUsers = userStatisticsRepository.findTopUsersByCreatedQuests(targetDate, pageable)
                            .stream()
                            .map(this::convertUserStatsToMap)
                            .collect(Collectors.toList());
                    break;
                default:
                    // По умолчанию используем completedQuests
                    topUsers = userStatisticsRepository.findTopUsersByCompletedQuests(targetDate, pageable)
                            .stream()
                            .map(this::convertUserStatsToMap)
                            .collect(Collectors.toList());
            }
            
        } catch (Exception e) {
            log.error("Error getting top users by metric: {}", metric, e);
        }
        
        return topUsers;
    }

    @Override
    @Cacheable(value = "top-quests", key = "{#metric, #limit, #date}")
    public List<Map<String, Object>> getTopQuests(String metric, int limit, LocalDate date) {
        log.debug("Getting top quests by metric: {} limit: {} date: {}", metric, limit, date);
        
        List<Map<String, Object>> topQuests = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, limit, Sort.Direction.DESC);

        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            
            // В зависимости от метрики получаем топ квестов
            switch (metric.toLowerCase()) {
                case "completions":
                    topQuests = questStatisticsRepository.findTopQuestsByCompletions(targetDate, pageable)
                            .stream()
                            .map(this::convertQuestStatsToMap)
                            .collect(Collectors.toList());
                    break;
                case "views":
                    topQuests = questStatisticsRepository.findTopQuestsByViews(targetDate, pageable)
                            .stream()
                            .map(this::convertQuestStatsToMap)
                            .collect(Collectors.toList());
                    break;
                case "rating":
                    topQuests = questStatisticsRepository.findTopQuestsByRating(targetDate, pageable)
                            .stream()
                            .map(this::convertQuestStatsToMap)
                            .collect(Collectors.toList());
                    break;
                default:
                    // По умолчанию используем completions
                    topQuests = questStatisticsRepository.findTopQuestsByCompletions(targetDate, pageable)
                            .stream()
                            .map(this::convertQuestStatsToMap)
                            .collect(Collectors.toList());
            }
            
        } catch (Exception e) {
            log.error("Error getting top quests by metric: {}", metric, e);
        }
        
        return topQuests;
    }

    @Override
    @Cacheable(value = "statistics-by-categories", key = "{#entityType, #startDate, #endDate}")
    public Map<String, Object> getStatisticsByCategories(String entityType, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting statistics by categories for entity type: {} period: {} to {}", entityType, startDate, endDate);
        
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? endDate : LocalDate.now();
            
            // В зависимости от типа сущности получаем статистику по категориям
            switch (entityType.toLowerCase()) {
                case "quest":
                    // Здесь должна быть логика получения статистики квестов по категориям
                    statistics.put("categories", Arrays.asList(
                            Map.of("name", "Programming", "count", 150),
                            Map.of("name", "Mathematics", "count", 89),
                            Map.of("name", "Logic", "count", 67)
                    ));
                    break;
                case "user":
                    // Здесь должна быть логика получения статистики пользователей по категориям
                    statistics.put("categories", Arrays.asList(
                            Map.of("name", "Active", "count", 1200),
                            Map.of("name", "Inactive", "count", 300),
                            Map.of("name", "New", "count", 150)
                    ));
                    break;
                default:
                    statistics.put("error", "Unknown entity type: " + entityType);
            }
            
            statistics.put("entityType", entityType);
            statistics.put("period", Map.of("startDate", start, "endDate", end));
            
        } catch (Exception e) {
            log.error("Error getting statistics by categories for entity type: {}", entityType, e);
            statistics.put("error", "Failed to get statistics by categories");
        }
        
        return statistics;
    }

    @Override
    @Cacheable(value = "metric-trends", key = "{#metrics, #period, #periods}")
    public Map<String, Object> getMetricTrends(List<String> metrics, String period, int periods) {
        log.debug("Getting metric trends for metrics: {} period: {} periods: {}", metrics, period, periods);
        
        Map<String, Object> trends = new HashMap<>();
        
        try {
            Map<String, List<Map<String, Object>>> metricData = new HashMap<>();
            
            for (String metric : metrics != null ? metrics : Arrays.asList("users", "quests", "sessions")) {
                List<Map<String, Object>> data = new ArrayList<>();
                LocalDate currentDate = LocalDate.now();
                
                for (int i = periods - 1; i >= 0; i--) {
                    LocalDate date = switch (period.toLowerCase()) {
                        case "weeks" -> currentDate.minusWeeks(i);
                        case "months" -> currentDate.minusMonths(i);
                        default -> currentDate.minusDays(i);
                    };
                    
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", date.format(DateTimeFormatter.ISO_DATE));
                    
                    // Получаем значение метрики за эту дату
                    Long value = getMetricValue(metric, date);
                    point.put("value", value);
                    
                    data.add(point);
                }
                
                metricData.put(metric, data);
            }
            
            trends.put("metrics", metricData);
            trends.put("period", period);
            trends.put("periods", periods);
            
        } catch (Exception e) {
            log.error("Error getting metric trends", e);
            trends.put("error", "Failed to get metric trends");
        }
        
        return trends;
    }

    @Override
    @Cacheable(value = "system-statistics", key = "{#category, #date}")
    public Map<String, Object> getSystemStatistics(String category, LocalDate date) {
        log.debug("Getting system statistics for category: {} date: {}", category, date);
        
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            
            SystemStatistics stats = systemStatisticsRepository.findByCategoryAndDate(category, targetDate)
                    .orElse(SystemStatistics.builder()
                            .category(category)
                            .date(targetDate)
                            .totalRequests(0L)
                            .successfulRequests(0L)
                            .failedRequests(0L)
                            .averageResponseTimeMs(0.0)
                            .cpuUsagePercent(0.0)
                            .memoryUsagePercent(0.0)
                            .diskUsagePercent(0.0)
                            .build());
            
            statistics.put("category", stats.getCategory());
            statistics.put("date", stats.getDate());
            statistics.put("totalRequests", stats.getTotalRequests());
            statistics.put("successfulRequests", stats.getSuccessfulRequests());
            statistics.put("failedRequests", stats.getFailedRequests());
            statistics.put("averageResponseTimeMs", stats.getAverageResponseTimeMs());
            statistics.put("cpuUsagePercent", stats.getCpuUsagePercent());
            statistics.put("memoryUsagePercent", stats.getMemoryUsagePercent());
            statistics.put("diskUsagePercent", stats.getDiskUsagePercent());
            
            // Добавляем процент успешных запросов
            if (stats.getTotalRequests() > 0) {
                double successRate = (double) stats.getSuccessfulRequests() / stats.getTotalRequests() * 100;
                statistics.put("successRate", successRate);
            } else {
                statistics.put("successRate", 0.0);
            }
            
        } catch (Exception e) {
            log.error("Error getting system statistics for category: {}", category, e);
            statistics.put("error", "Failed to get system statistics");
        }
        
        return statistics;
    }

    // Вспомогательные методы

    private Map<String, Object> convertUserStatsToMap(UserStatistics stats) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", stats.getUserId());
        map.put("date", stats.getDate());
        map.put("completedQuests", stats.getCompletedQuests());
        map.put("gameSessions", stats.getGameSessions());
        map.put("createdQuests", stats.getCreatedQuests());
        map.put("totalGameTimeMinutes", stats.getTotalGameTimeMinutes());
        return map;
    }

    private Map<String, Object> convertQuestStatsToMap(QuestStatistics stats) {
        Map<String, Object> map = new HashMap<>();
        map.put("questId", stats.getQuestId());
        map.put("date", stats.getDate());
        map.put("completions", stats.getCompletions());
        map.put("views", stats.getViews());
        map.put("starts", stats.getStarts());
        map.put("rating", stats.getAvgRating());
        return map;
    }

    private Long getMetricValue(String metric, LocalDate date) {
        // В реальной реализации здесь был бы запрос к БД для получения значения метрики
        // Для примера возвращаем случайные значения
        return switch (metric.toLowerCase()) {
            case "users" -> (long) (Math.random() * 1000);
            case "quests" -> (long) (Math.random() * 500);
            case "sessions" -> (long) (Math.random() * 2000);
            default -> 0L;
        };
    }
}