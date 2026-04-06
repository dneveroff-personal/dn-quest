package dn.quest.statistics.service.impl;

import dn.quest.statistics.dto.LeaderboardDTO;
import dn.quest.statistics.entity.Leaderboard;
import dn.quest.statistics.entity.QuestStatistics;
import dn.quest.statistics.entity.TeamStatistics;
import dn.quest.statistics.entity.UserStatistics;
import dn.quest.statistics.exception.StatisticsNotFoundException;
import dn.quest.statistics.repository.LeaderboardRepository;
import dn.quest.statistics.repository.QuestStatisticsRepository;
import dn.quest.statistics.repository.TeamStatisticsRepository;
import dn.quest.statistics.repository.UserStatisticsRepository;
import dn.quest.statistics.service.CacheService;
import dn.quest.statistics.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с лидербордами
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final QuestStatisticsRepository questStatisticsRepository;
    private final TeamStatisticsRepository teamStatisticsRepository;
    private final CacheService cacheService;

    @Override
    public Page<LeaderboardDTO> getGlobalLeaderboard(String period, String category, LocalDate date, Pageable pageable) {
        log.debug("Getting global leaderboard for period: {} category: {} date: {}", period, category, date);
        
        try {
            // Проверяем кэш
            List<Map<String, Object>> cachedLeaderboard = cacheService.getLeaderboard("global", period, date);
            if (cachedLeaderboard != null) {
                return convertToLeaderboardDTOPage(cachedLeaderboard, pageable);
            }
            
            // Получаем данные из БД
            LocalDate targetDate = getTargetDate(period, date);
            List<Leaderboard> leaderboards = leaderboardRepository.findByLeaderboardTypeAndPeriodAndDateOrderByRank(
                    "users", period, targetDate);
            
            // Фильтрация по категории если необходимо
            if (category != null && !category.isEmpty()) {
                leaderboards = leaderboards.stream()
                        .filter(l -> category.equals(l.getCategory()))
                        .collect(Collectors.toList());
            }
            
            // Сортировка по рангу
            leaderboards.sort(Comparator.comparing(Leaderboard::getRank));
            
            // Конвертация в DTO
            List<LeaderboardDTO> dtos = leaderboards.stream()
                    .map(this::convertToLeaderboardDTO)
                    .collect(Collectors.toList());
            
            // Кэширование результата
            List<Map<String, Object>> cacheData = leaderboards.stream()
                    .map(this::convertLeaderboardToMap)
                    .collect(Collectors.toList());
            cacheService.cacheLeaderboard("global", period, date, cacheData);
            
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), dtos.size());
            List<LeaderboardDTO> pageContent = dtos.subList(start, end);
            
            return new PageImpl<>(pageContent, pageable, dtos.size());
            
        } catch (Exception e) {
            log.error("Error getting global leaderboard", e);
            throw new StatisticsNotFoundException("Failed to get global leaderboard", e);
        }
    }

    @Override
    public Page<LeaderboardDTO> getQuestLeaderboard(String period, String category, String metric, LocalDate date, Pageable pageable) {
        log.debug("Getting quest leaderboard for period: {} category: {} metric: {} date: {}", period, category, metric, date);
        
        try {
            // Проверяем кэш
            String cacheKey = "quests_" + category + "_" + metric;
            List<Map<String, Object>> cachedLeaderboard = cacheService.getLeaderboard(cacheKey, period, date);
            if (cachedLeaderboard != null) {
                return convertToLeaderboardDTOPage(cachedLeaderboard, pageable);
            }
            
            // Получаем данные из БД
            LocalDate targetDate = getTargetDate(period, date);
            List<Leaderboard> leaderboards = leaderboardRepository.findByLeaderboardTypeAndPeriodAndDateOrderByRank(
                    "quests", period, targetDate);
            
            // Фильтрация по категории и метрике
            if (category != null && !category.isEmpty()) {
                leaderboards = leaderboards.stream()
                        .filter(l -> category.equals(l.getCategory()))
                        .collect(Collectors.toList());
            }
            
            // Сортировка по метрике
            leaderboards = sortByMetric(leaderboards, metric);
            
            // Конвертация в DTO
            List<LeaderboardDTO> dtos = leaderboards.stream()
                    .map(this::convertToLeaderboardDTO)
                    .collect(Collectors.toList());
            
            // Кэширование результата
            List<Map<String, Object>> cacheData = leaderboards.stream()
                    .map(this::convertLeaderboardToMap)
                    .collect(Collectors.toList());
            cacheService.cacheLeaderboard(cacheKey, period, date, cacheData);
            
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), dtos.size());
            List<LeaderboardDTO> pageContent = dtos.subList(start, end);
            
            return new PageImpl<>(pageContent, pageable, dtos.size());
            
        } catch (Exception e) {
            log.error("Error getting quest leaderboard", e);
            throw new StatisticsNotFoundException("Failed to get quest leaderboard", e);
        }
    }

    @Override
    public Page<LeaderboardDTO> getTeamLeaderboard(String period, String category, String metric, LocalDate date, Pageable pageable) {
        log.debug("Getting team leaderboard for period: {} category: {} metric: {} date: {}", period, category, metric, date);
        
        try {
            // Проверяем кэш
            String cacheKey = "teams_" + category + "_" + metric;
            List<Map<String, Object>> cachedLeaderboard = cacheService.getLeaderboard(cacheKey, period, date);
            if (cachedLeaderboard != null) {
                return convertToLeaderboardDTOPage(cachedLeaderboard, pageable);
            }
            
            // Получаем данные из БД
            LocalDate targetDate = getTargetDate(period, date);
            List<Leaderboard> leaderboards = leaderboardRepository.findByLeaderboardTypeAndPeriodAndDateOrderByRank(
                    "teams", period, targetDate);
            
            // Фильтрация по категории
            if (category != null && !category.isEmpty()) {
                leaderboards = leaderboards.stream()
                        .filter(l -> category.equals(l.getCategory()))
                        .collect(Collectors.toList());
            }
            
            // Сортировка по метрике
            leaderboards = sortByMetric(leaderboards, metric);
            
            // Конвертация в DTO
            List<LeaderboardDTO> dtos = leaderboards.stream()
                    .map(this::convertToLeaderboardDTO)
                    .collect(Collectors.toList());
            
            // Кэширование результата
            List<Map<String, Object>> cacheData = leaderboards.stream()
                    .map(this::convertLeaderboardToMap)
                    .collect(Collectors.toList());
            cacheService.cacheLeaderboard(cacheKey, period, date, cacheData);
            
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), dtos.size());
            List<LeaderboardDTO> pageContent = dtos.subList(start, end);
            
            return new PageImpl<>(pageContent, pageable, dtos.size());
            
        } catch (Exception e) {
            log.error("Error getting team leaderboard", e);
            throw new StatisticsNotFoundException("Failed to get team leaderboard", e);
        }
    }

    @Override
    public Map<String, Object> getUserLeaderboardPosition(UUID userId, String period, LocalDate date) {
        log.debug("Getting leaderboard position for user: {} period: {} date: {}", userId, period, date);
        
        try {
            LocalDate targetDate = getTargetDate(period, date);
            
            // Ищем позицию пользователя в лидерборде
            Optional<Leaderboard> leaderboardEntry = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateAndEntityId("users", period, targetDate, userId.toString());
            
            Map<String, Object> result = new HashMap<>();
            
            if (leaderboardEntry.isPresent()) {
                Leaderboard entry = leaderboardEntry.get();
                result.put("entityId", entry.getEntityId());
                result.put("entityName", entry.getEntityName());
                result.put("rank", entry.getRank());
                result.put("previousRank", entry.getPreviousRank());
                result.put("rankChange", entry.getRankChange());
                result.put("score", entry.getScore());
                result.put("previousScore", entry.getPreviousScore());
                result.put("scoreChange", entry.getScoreChange());
                result.put("category", entry.getCategory());
                result.put("level", entry.getLevel());
                result.put("progressPercentage", entry.getProgressPercentage());
                result.put("participationsCount", entry.getParticipationsCount());
                result.put("winsCount", entry.getWinsCount());
                result.put("winRate", entry.getWinRate());
                result.put("avgRating", entry.getAvgRating());
                result.put("achievementsCount", entry.getAchievementsCount());
            } else {
                // Если пользователь не найден в лидерборде, рассчитываем позицию динамически
                result = calculateUserPositionDynamically(userId, period, targetDate);
            }
            
            result.put("period", period);
            result.put("date", targetDate);
            
            return result;
            
        } catch (Exception e) {
            log.error("Error getting user leaderboard position", e);
            throw new StatisticsNotFoundException("Failed to get user leaderboard position", e);
        }
    }

    @Override
    public Map<String, Object> getQuestLeaderboardPosition(UUID questId, String period, String metric, LocalDate date) {
        log.debug("Getting leaderboard position for quest: {} period: {} metric: {} date: {}", questId, period, metric, date);
        
        try {
            LocalDate targetDate = getTargetDate(period, date);
            
            // Ищем позицию квеста в лидерборде
            Optional<Leaderboard> leaderboardEntry = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateAndEntityId("quests", period, targetDate, questId.toString());
            
            Map<String, Object> result = new HashMap<>();
            
            if (leaderboardEntry.isPresent()) {
                Leaderboard entry = leaderboardEntry.get();
                result.put("entityId", entry.getEntityId());
                result.put("entityName", entry.getEntityName());
                result.put("rank", entry.getRank());
                result.put("previousRank", entry.getPreviousRank());
                result.put("rankChange", entry.getRankChange());
                result.put("score", entry.getScore());
                result.put("previousScore", entry.getPreviousScore());
                result.put("scoreChange", entry.getScoreChange());
                result.put("category", entry.getCategory());
                result.put("level", entry.getLevel());
                result.put("progressPercentage", entry.getProgressPercentage());
                result.put("participationsCount", entry.getParticipationsCount());
                result.put("winsCount", entry.getWinsCount());
                result.put("winRate", entry.getWinRate());
                result.put("avgRating", entry.getAvgRating());
            } else {
                // Если квест не найден в лидерборде, рассчитываем позицию динамически
                result = calculateQuestPositionDynamically(questId, period, metric, targetDate);
            }
            
            result.put("period", period);
            result.put("metric", metric);
            result.put("date", targetDate);
            
            return result;
            
        } catch (Exception e) {
            log.error("Error getting quest leaderboard position", e);
            throw new StatisticsNotFoundException("Failed to get quest leaderboard position", e);
        }
    }

    @Override
    public Map<String, Object> getTeamLeaderboardPosition(UUID teamId, String period, String metric, LocalDate date) {
        log.debug("Getting leaderboard position for team: {} period: {} metric: {} date: {}", teamId, period, metric, date);
        
        try {
            LocalDate targetDate = getTargetDate(period, date);
            
            // Ищем позицию команды в лидерборде
            Optional<Leaderboard> leaderboardEntry = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateAndEntityId("teams", period, targetDate, teamId.toString());
            
            Map<String, Object> result = new HashMap<>();
            
            if (leaderboardEntry.isPresent()) {
                Leaderboard entry = leaderboardEntry.get();
                result.put("entityId", entry.getEntityId());
                result.put("entityName", entry.getEntityName());
                result.put("rank", entry.getRank());
                result.put("previousRank", entry.getPreviousRank());
                result.put("rankChange", entry.getRankChange());
                result.put("score", entry.getScore());
                result.put("previousScore", entry.getPreviousScore());
                result.put("scoreChange", entry.getScoreChange());
                result.put("category", entry.getCategory());
                result.put("level", entry.getLevel());
                result.put("progressPercentage", entry.getProgressPercentage());
                result.put("participationsCount", entry.getParticipationsCount());
                result.put("winsCount", entry.getWinsCount());
                result.put("winRate", entry.getWinRate());
                result.put("avgRating", entry.getAvgRating());
            } else {
                // Если команда не найдена в лидерборде, рассчитываем позицию динамически
                result = calculateTeamPositionDynamically(teamId, period, metric, targetDate);
            }
            
            result.put("period", period);
            result.put("metric", metric);
            result.put("date", targetDate);
            
            return result;
            
        } catch (Exception e) {
            log.error("Error getting team leaderboard position", e);
            throw new StatisticsNotFoundException("Failed to get team leaderboard position", e);
        }
    }

    @Override
    public List<LeaderboardDTO> getUserSurroundingInLeaderboard(UUID userId, String period, int count, LocalDate date) {
        log.debug("Getting surrounding users for user: {} period: {} count: {} date: {}", userId, period, count, date);
        
        try {
            LocalDate targetDate = getTargetDate(period, date);
            
            // Получаем позицию пользователя
            Map<String, Object> userPosition = getUserLeaderboardPosition(userId, period, targetDate);
            Integer userRank = (Integer) userPosition.get("rank");
            
            if (userRank == null) {
                return Collections.emptyList();
            }
            
            // Получаем пользователей вокруг
            int startRank = Math.max(1, userRank - count);
            int endRank = userRank + count;
            
            List<Leaderboard> surroundingEntries = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateAndRankBetween(
                            "users", period, targetDate, startRank, endRank);
            
            return surroundingEntries.stream()
                    .map(this::convertToLeaderboardDTO)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error getting user surrounding in leaderboard", e);
            throw new StatisticsNotFoundException("Failed to get user surrounding in leaderboard", e);
        }
    }

    @Override
    public Map<String, Object> getUserLeaderboardHistory(UUID userId, String period, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting leaderboard history for user: {} period: {} from {} to {}", userId, period, startDate, endDate);
        
        try {
            List<Leaderboard> historyEntries = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndEntityIdAndDateBetween(
                            "users", period, userId.toString(), startDate, endDate);
            
            List<Map<String, Object>> historyData = historyEntries.stream()
                    .map(entry -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("date", entry.getDate());
                        map.put("rank", entry.getRank());
                        map.put("score", entry.getScore());
                        map.put("rankChange", entry.getRankChange());
                        map.put("scoreChange", entry.getScoreChange());
                        return map;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("period", period);
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("history", historyData);

            return result;
        } catch (Exception e) {
            log.error("Error getting user leaderboard history", e);
            throw new StatisticsNotFoundException("Failed to get user leaderboard history", e);
        }
    }

    @Override
    public Map<String, List<String>> getLeaderboardCategories() {
        log.debug("Getting leaderboard categories");
        
        try {
            Map<String, List<String>> categories = new HashMap<>();
            
            // Категории для пользователей
            categories.put("users", Arrays.asList("overall", "daily", "weekly", "monthly", "rating", "score", "wins"));
            
            // Категории для квестов
            categories.put("quests", Arrays.asList("overall", "rating", "popularity", "difficulty", "completion_rate", "creativity"));
            
            // Категории для команд
            categories.put("teams", Arrays.asList("overall", "rating", "teamwork", "efficiency", "collaboration"));
            
            return categories;
            
        } catch (Exception e) {
            log.error("Error getting leaderboard categories", e);
            throw new StatisticsNotFoundException("Failed to get leaderboard categories", e);
        }
    }

    @Override
    public Map<String, Object> getLeaderboardStats(String period, LocalDate date) {
        log.debug("Getting leaderboard stats for period: {} date: {}", period, date);
        
        try {
            LocalDate targetDate = getTargetDate(period, date);
            
            // Статистика по пользователям
            List<Leaderboard> userLeaderboards = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateOrderByRank(
                            "users", period, targetDate);
            
            // Статистика по квестам
            List<Leaderboard> questLeaderboards = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateOrderByRank(
                            "quests", period, targetDate);
            
            // Статистика по командам
            List<Leaderboard> teamLeaderboards = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateOrderByRank(
                            "teams", period, targetDate);
            
            return Map.of(
                    "period", period,
                    "date", targetDate,
                    "users", Map.of(
                            "totalEntries", userLeaderboards.size(),
                            "avgScore", userLeaderboards.stream().mapToDouble(l -> l.getScore() != null ? l.getScore() : 0.0).average().orElse(0.0),
                            "avgRating", userLeaderboards.stream().mapToDouble(l -> l.getAvgRating() != null ? l.getAvgRating() : 0.0).average().orElse(0.0)
                    ),
                    "quests", Map.of(
                            "totalEntries", questLeaderboards.size(),
                            "avgScore", questLeaderboards.stream().mapToDouble(l -> l.getScore() != null ? l.getScore() : 0.0).average().orElse(0.0),
                            "avgRating", questLeaderboards.stream().mapToDouble(l -> l.getAvgRating() != null ? l.getAvgRating() : 0.0).average().orElse(0.0)
                    ),
                    "teams", Map.of(
                            "totalEntries", teamLeaderboards.size(),
                            "avgScore", teamLeaderboards.stream().mapToDouble(l -> l.getScore() != null ? l.getScore() : 0.0).average().orElse(0.0),
                            "avgRating", teamLeaderboards.stream().mapToDouble(l -> l.getAvgRating() != null ? l.getAvgRating() : 0.0).average().orElse(0.0)
                    )
            );
            
        } catch (Exception e) {
            log.error("Error getting leaderboard stats", e);
            throw new StatisticsNotFoundException("Failed to get leaderboard stats", e);
        }
    }

    @Override
    @Transactional
    public void updateLeaderboards(String period, LocalDate date) {
        log.info("Updating leaderboards for period: {} date: {}", period, date);
        
        try {
            LocalDate targetDate = getTargetDate(period, date);
            
            // Обновление глобальных лидербордов
            recalculateGlobalLeaderboard(period, targetDate);
            
            // Обновление лидербордов квестов
            recalculateQuestLeaderboard(period, null, "rating", targetDate);
            
            // Обновление командных лидербордов
            recalculateTeamLeaderboard(period, null, "rating", targetDate);
            
            // Инвалидация кэшей
            cacheService.invalidateLeaderboardCache("global", period, targetDate);
            cacheService.invalidateLeaderboardCache("quests", period, targetDate);
            cacheService.invalidateLeaderboardCache("teams", period, targetDate);
            
            log.info("Leaderboards update completed for period: {} date: {}", period, targetDate);
            
        } catch (Exception e) {
            log.error("Error updating leaderboards", e);
            throw new RuntimeException("Failed to update leaderboards", e);
        }
    }

    @Override
    @Transactional
    public void recalculateGlobalLeaderboard(String period, LocalDate date) {
        log.debug("Recalculating global leaderboard for period: {} date: {}", period, date);
        
        try {
            // Получаем статистику пользователей за период
            List<UserStatistics> userStats = getUserStatisticsForPeriod(period, date);
            
            // Рассчитываем очки и ранги
            List<Leaderboard> leaderboards = calculateUserLeaderboards(userStats, period, date);
            
            // Сохраняем в БД
            leaderboardRepository.saveAll(leaderboards);
            
            log.debug("Global leaderboard recalculation completed for period: {} date: {}", period, date);
            
        } catch (Exception e) {
            log.error("Error recalculating global leaderboard", e);
            throw new RuntimeException("Failed to recalculate global leaderboard", e);
        }
    }

    @Override
    @Transactional
    public void recalculateQuestLeaderboard(String period, String category, String metric, LocalDate date) {
        log.debug("Recalculating quest leaderboard for period: {} category: {} metric: {} date: {}", period, category, metric, date);
        
        try {
            // Получаем статистику квестов за период
            List<QuestStatistics> questStats = getQuestStatisticsForPeriod(period, date);
            
            // Фильтрация по категории если необходимо
            if (category != null && !category.isEmpty()) {
                questStats = questStats.stream()
                        .filter(q -> category.equals(q.getCategory()))
                        .collect(Collectors.toList());
            }
            
            // Рассчитываем очки и ранги
            List<Leaderboard> leaderboards = calculateQuestLeaderboards(questStats, period, metric, date);
            
            // Сохраняем в БД
            leaderboardRepository.saveAll(leaderboards);
            
            log.debug("Quest leaderboard recalculation completed for period: {} category: {} metric: {} date: {}", period, category, metric, date);
            
        } catch (Exception e) {
            log.error("Error recalculating quest leaderboard", e);
            throw new RuntimeException("Failed to recalculate quest leaderboard", e);
        }
    }

    @Override
    @Transactional
    public void recalculateTeamLeaderboard(String period, String category, String metric, LocalDate date) {
        log.debug("Recalculating team leaderboard for period: {} category: {} metric: {} date: {}", period, category, metric, date);
        
        try {
            // Получаем статистику команд за период
            List<TeamStatistics> teamStats = getTeamStatisticsForPeriod(period, date);
            
            // Фильтрация по категории если необходимо
            if (category != null && !category.isEmpty()) {
                teamStats = teamStats.stream()
                        .filter(t -> category.equals(t.getTeamType()))
                        .collect(Collectors.toList());
            }
            
            // Рассчитываем очки и ранги
            List<Leaderboard> leaderboards = calculateTeamLeaderboards(teamStats, period, metric, date);
            
            // Сохраняем в БД
            leaderboardRepository.saveAll(leaderboards);
            
            log.debug("Team leaderboard recalculation completed for period: {} category: {} metric: {} date: {}", period, category, metric, date);
            
        } catch (Exception e) {
            log.error("Error recalculating team leaderboard", e);
            throw new RuntimeException("Failed to recalculate team leaderboard", e);
        }
    }

    // Остальные методы реализации...

    @Override
    public List<LeaderboardDTO> getTopUsers(String period, String category, int limit, LocalDate date) {
        log.debug("Getting top users for period: {} category: {} limit: {} date: {}", period, category, limit, date);
        
        try {
            LocalDate targetDate = getTargetDate(period, date);
            
            List<Leaderboard> topUsers = leaderboardRepository
                    .findTopByLeaderboardTypeAndPeriodAndDateOrderByRankAsc("users", period, targetDate, limit);
            
            return topUsers.stream()
                    .map(this::convertToLeaderboardDTO)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error getting top users", e);
            throw new StatisticsNotFoundException("Failed to get top users", e);
        }
    }

    @Override
    public List<LeaderboardDTO> getTopQuests(String period, String category, String metric, int limit, LocalDate date) {
        log.debug("Getting top quests for period: {} category: {} metric: {} limit: {} date: {}", period, category, metric, limit, date);
        
        try {
            LocalDate targetDate = getTargetDate(period, date);
            
            List<Leaderboard> topQuests = leaderboardRepository
                    .findTopByLeaderboardTypeAndPeriodAndDateOrderByRankAsc("quests", period, targetDate, limit);
            
            return topQuests.stream()
                    .map(this::convertToLeaderboardDTO)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error getting top quests", e);
            throw new StatisticsNotFoundException("Failed to get top quests", e);
        }
    }

    @Override
    public List<LeaderboardDTO> getTopTeams(String period, String category, String metric, int limit, LocalDate date) {
        log.debug("Getting top teams for period: {} category: {} metric: {} limit: {} date: {}", period, category, metric, limit, date);
        
        try {
            LocalDate targetDate = getTargetDate(period, date);
            
            List<Leaderboard> topTeams = leaderboardRepository
                    .findTopByLeaderboardTypeAndPeriodAndDateOrderByRankAsc("teams", period, targetDate, limit);
            
            return topTeams.stream()
                    .map(this::convertToLeaderboardDTO)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error getting top teams", e);
            throw new StatisticsNotFoundException("Failed to get top teams", e);
        }
    }

    @Override
    public Page<LeaderboardDTO> getRatingLeaderboard(String entityType, String period, LocalDate date, Pageable pageable) {
        return null;
    }

    @Override
    public Page<LeaderboardDTO> getScoreLeaderboard(String entityType, String period, LocalDate date, Pageable pageable) {
        return null;
    }

    @Override
    public Page<LeaderboardDTO> getWinsLeaderboard(String entityType, String period, LocalDate date, Pageable pageable) {
        return null;
    }

    @Override
    public Page<LeaderboardDTO> getCompletionTimeLeaderboard(String entityType, String period, LocalDate date, Pageable pageable) {
        return null;
    }

    @Override
    public Page<LeaderboardDTO> getCompletionsLeaderboard(String entityType, String period, LocalDate date, Pageable pageable) {
        return null;
    }

    @Override
    public Page<LeaderboardDTO> getSuccessRateLeaderboard(String entityType, String period, LocalDate date, Pageable pageable) {
        return null;
    }

    @Override
    public Map<String, Object> getLeaderboardTrends(String entityType, String period, LocalDate startDate, LocalDate endDate) {
        return Map.of();
    }

    @Override
    public Map<String, Object> getPositionComparison(Long entityId, String entityType, LocalDate date1, LocalDate date2) {
        return Map.of();
    }

    @Override
    public Map<String, Object> getPositionForecast(Long entityId, String entityType, String period, int daysAhead) {
        return Map.of();
    }

    @Override
    public Map<String, Object> getLeaderboardChangeAnalysis(String entityType, String period, LocalDate date) {
        return Map.of();
    }

    @Override
    public Map<String, Object> getCategoryStatistics(String entityType, String period, LocalDate date) {
        return Map.of();
    }

    // Вспомогательные методы

    private LocalDate getTargetDate(String period, LocalDate date) {
        if (date != null) {
            return date;
        }
        
        return switch (period.toLowerCase()) {
            case "daily" -> LocalDate.now();
            case "weekly" -> LocalDate.now().minusWeeks(1);
            case "monthly" -> LocalDate.now().minusMonths(1);
            default -> LocalDate.now();
        };
    }

    private Page<LeaderboardDTO> convertToLeaderboardDTOPage(List<Map<String, Object>> cachedData, Pageable pageable) {
        List<LeaderboardDTO> dtos = cachedData.stream()
                .map(this::convertMapToLeaderboardDTO)
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        List<LeaderboardDTO> pageContent = dtos.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, dtos.size());
    }

    private LeaderboardDTO convertToLeaderboardDTO(Leaderboard leaderboard) {
        return LeaderboardDTO.builder()
                .id(leaderboard.getId())
                .leaderboardType(leaderboard.getLeaderboardType())
                .period(leaderboard.getPeriod())
                .date(leaderboard.getDate())
                .entityId(leaderboard.getEntityId())
                .entityName(leaderboard.getEntityName())
                .rank(leaderboard.getRank())
                .previousRank(leaderboard.getPreviousRank())
                .rankChange(leaderboard.getRankChange())
                .score(leaderboard.getScore())
                .previousScore(leaderboard.getPreviousScore())
                .scoreChange(leaderboard.getScoreChange())
                .category(leaderboard.getCategory())
                .level(leaderboard.getLevel())
                .progressPercentage(leaderboard.getProgressPercentage())
                .participationsCount(leaderboard.getParticipationsCount())
                .winsCount(leaderboard.getWinsCount())
                .winRate(leaderboard.getWinRate())
                .avgRating(leaderboard.getAvgRating())
                .achievementsCount(leaderboard.getAchievementsCount())
                .avatarUrl(leaderboard.getAvatarUrl())
                .profileUrl(leaderboard.getProfileUrl())
                .build();
    }

    private Map<String, Object> convertLeaderboardToMap(Leaderboard leaderboard) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", leaderboard.getId());
        map.put("leaderboardType", leaderboard.getLeaderboardType());
        map.put("period", leaderboard.getPeriod());
        map.put("date", leaderboard.getDate());
        map.put("entityId", leaderboard.getEntityId());
        map.put("entityName", leaderboard.getEntityName());
        map.put("rank", leaderboard.getRank());
        map.put("previousRank", leaderboard.getPreviousRank());
        map.put("rankChange", leaderboard.getRankChange());
        map.put("score", leaderboard.getScore());
        map.put("previousScore", leaderboard.getPreviousScore());
        map.put("scoreChange", leaderboard.getScoreChange());
        map.put("category", leaderboard.getCategory());
        map.put("level", leaderboard.getLevel());
        map.put("progressPercentage", leaderboard.getProgressPercentage());
        map.put("participationsCount", leaderboard.getParticipationsCount());
        map.put("winsCount", leaderboard.getWinsCount());
        map.put("winRate", leaderboard.getWinRate());
        map.put("avgRating", leaderboard.getAvgRating());
        map.put("achievementsCount", leaderboard.getAchievementsCount());
        map.put("avatarUrl", leaderboard.getAvatarUrl());
        map.put("profileUrl", leaderboard.getProfileUrl());
        return map;
    }

    private LeaderboardDTO convertMapToLeaderboardDTO(Map<String, Object> map) {
        return LeaderboardDTO.builder()
                .id(UUID.fromString((map.get("id")).toString()))
                .leaderboardType((String) map.get("leaderboardType"))
                .period((String) map.get("period"))
                .date((LocalDate) map.get("date"))
                .entityId((map.get("entityId")).toString())
                .entityName((String) map.get("entityName"))
                .rank((Integer) map.get("rank"))
                .previousRank((Integer) map.get("previousRank"))
                .rankChange((Integer) map.get("rankChange"))
                .score(((Number) map.get("score")).doubleValue())
                .previousScore(((Number) map.get("previousScore")).doubleValue())
                .scoreChange(((Number) map.get("scoreChange")).doubleValue())
                .category((String) map.get("category"))
                .level((Integer) map.get("level"))
                .progressPercentage(((Number) map.get("progressPercentage")).doubleValue())
                .participationsCount((Integer) map.get("participationsCount"))
                .winsCount((Integer) map.get("winsCount"))
                .winRate(((Number) map.get("winRate")).doubleValue())
                .avgRating(((Number) map.get("avgRating")).doubleValue())
                .achievementsCount((Integer) map.get("achievementsCount"))
                .avatarUrl((String) map.get("avatarUrl"))
                .profileUrl((String) map.get("profileUrl"))
                .build();
    }

    private List<Leaderboard> sortByMetric(List<Leaderboard> leaderboards, String metric) {
        return leaderboards.stream()
                .sorted((l1, l2) -> {
                    switch (metric.toLowerCase()) {
                        case "score":
                            return Double.compare(l2.getScore() != null ? l2.getScore() : 0.0, 
                                                l1.getScore() != null ? l1.getScore() : 0.0);
                        case "rating":
                            return Double.compare(l2.getAvgRating() != null ? l2.getAvgRating() : 0.0, 
                                                l1.getAvgRating() != null ? l1.getAvgRating() : 0.0);
                        case "wins":
                            return Integer.compare(l2.getWinsCount() != null ? l2.getWinsCount() : 0, 
                                                 l1.getWinsCount() != null ? l1.getWinsCount() : 0);
                        default:
                            return Integer.compare(l1.getRank(), l2.getRank());
                    }
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> calculateUserPositionDynamically(UUID userId, String period, LocalDate date) {
        // Динамический расчет позиции пользователя
        // В реальной реализации здесь был бы запрос к статистике и расчет позиции
        
        Map<String, Object> result = new HashMap<>();
        result.put("entityId", userId);
        result.put("rank", null);
        result.put("score", 0.0);
        result.put("message", "User not found in leaderboard");
        
        return result;
    }

    private Map<String, Object> calculateQuestPositionDynamically(UUID questId, String period, String metric, LocalDate date) {
        // Динамический расчет позиции квеста
        // В реальной реализации здесь был бы запрос к статистике и расчет позиции
        
        Map<String, Object> result = new HashMap<>();
        result.put("entityId", questId);
        result.put("rank", null);
        result.put("score", 0.0);
        result.put("message", "Quest not found in leaderboard");
        
        return result;
    }

    private Map<String, Object> calculateTeamPositionDynamically(UUID teamId, String period, String metric, LocalDate date) {
        // Динамический расчет позиции команды
        // В реальной реализации здесь был бы запрос к статистике и расчет позиции
        
        Map<String, Object> result = new HashMap<>();
        result.put("entityId", teamId);
        result.put("rank", null);
        result.put("score", 0.0);
        result.put("message", "Team not found in leaderboard");
        
        return result;
    }

    private List<UserStatistics> getUserStatisticsForPeriod(String period, LocalDate date) {
        log.debug("Getting user statistics for period: {} date: {}", period, date);
        
        LocalDate startDate = getPeriodStartDate(period, date);
        LocalDate endDate = date;
        
        return userStatisticsRepository.findByDateBetween(startDate, endDate);
    }

    private List<QuestStatistics> getQuestStatisticsForPeriod(String period, LocalDate date) {
        log.debug("Getting quest statistics for period: {} date: {}", period, date);
        
        LocalDate startDate = getPeriodStartDate(period, date);
        LocalDate endDate = date;
        
        return questStatisticsRepository.findByDateBetween(startDate, endDate);
    }

    private List<TeamStatistics> getTeamStatisticsForPeriod(String period, LocalDate date) {
        log.debug("Getting team statistics for period: {} date: {}", period, date);
        
        LocalDate startDate = getPeriodStartDate(period, date);
        LocalDate endDate = date;
        
        return teamStatisticsRepository.findByDateBetween(startDate, endDate);
    }

    private List<Leaderboard> calculateUserLeaderboards(List<UserStatistics> userStats, String period, LocalDate date) {
        log.debug("Calculating user leaderboards for period: {} date: {} stats count: {}", period, date, userStats.size());
        
        if (userStats.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Группируем статистику по пользователям
        Map<UUID, List<UserStatistics>> groupedByUser = userStats.stream()
                .collect(Collectors.groupingBy(UserStatistics::getUserId));
        
        // Рассчитываем очки для каждого пользователя
        List<Leaderboard> leaderboards = groupedByUser.entrySet().stream()
                .map(entry -> {
                    UUID userId = entry.getKey();
                    List<UserStatistics> stats = entry.getValue();
                    
                    // Агрегируем статистику пользователя за период
                    UserStatistics aggregated = aggregateUserStatsForPeriod(stats);
                    
                    // Рассчитываем очки на основе различных метрик
                    double score = calculateUserScore(aggregated);
                    
                    return Leaderboard.builder()
                            .leaderboardType("users")
                            .period(period)
                            .date(date)
                            .entityId(userId.toString())
                            .entityName("User_" + userId) // В реальности здесь было бы получение имени пользователя
                            .score(score)
                            .rank(0) // Будет рассчитан после сортировки
                            .previousRank(0) // Будет получен из предыдущего периода
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
            LocalDate previousDate = getPreviousPeriodDate(period, date);
            Optional<Leaderboard> previousEntry = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateAndEntityId(
                            "users", period, previousDate, leaderboard.getEntityId());
            
            if (previousEntry.isPresent()) {
                leaderboard.setPreviousRank(previousEntry.get().getRank());
                leaderboard.setRankChange(leaderboard.getPreviousRank() - leaderboard.getRank());
                leaderboard.setPreviousScore(previousEntry.get().getScore());
                leaderboard.setScoreChange(leaderboard.getScore() - previousEntry.get().getScore());
            }
        }
        
        return leaderboards;
    }

    private List<Leaderboard> calculateQuestLeaderboards(List<QuestStatistics> questStats, String period, String metric, LocalDate date) {
        log.debug("Calculating quest leaderboards for period: {} metric: {} date: {} stats count: {}", period, metric, date, questStats.size());
        
        if (questStats.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Группируем статистику по квестам
        Map<UUID, List<QuestStatistics>> groupedByQuest = questStats.stream()
                .collect(Collectors.groupingBy(QuestStatistics::getQuestId));
        
        // Рассчитываем очки для каждого квеста
        List<Leaderboard> leaderboards = groupedByQuest.entrySet().stream()
                .map(entry -> {
                    UUID questId = entry.getKey();
                    List<QuestStatistics> stats = entry.getValue();
                    
                    // Агрегируем статистику квеста за период
                    QuestStatistics aggregated = aggregateQuestStatsForPeriod(stats);
                    
                    // Рассчитываем очки на основе метрики
                    double score = calculateQuestScoreByMetric(aggregated, metric);
                    
                    return Leaderboard.builder()
                            .leaderboardType("quests")
                            .period(period)
                            .date(date)
                            .entityId(questId.toString())
                            .entityName(aggregated.getQuestTitle())
                            .score(score)
                            .rank(0) // Будет рассчитан после сортировки
                            .previousRank(0) // Будет получен из предыдущего периода
                            .rankChange(0) // Будет рассчитан
                            .category("overall")
                            .avgRating(calculateQuestAvgRating(questId))
                            .avgCompletionTime(calculateQuestAvgCompletionTime(questId))
                            .viewsCount(aggregated.getViews())
                            .likesCount(calculateQuestLikesCount(questId))
                            .build();
                })
                .sorted((l1, l2) -> Double.compare(l2.getScore() != null ? l2.getScore() : 0.0, l1.getScore() != null ? l1.getScore() : 0.0)) // Сортировка по убыванию очков
                .collect(Collectors.toList());
        
        // Устанавливаем ранги
        for (int i = 0; i < leaderboards.size(); i++) {
            Leaderboard leaderboard = leaderboards.get(i);
            leaderboard.setRank(i + 1);
            
            // Получаем предыдущий ранг
            LocalDate previousDate = getPreviousPeriodDate(period, date);
            Optional<Leaderboard> previousEntry = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateAndEntityId(
                            "quests", period, previousDate, leaderboard.getEntityId());
            
            if (previousEntry.isPresent()) {
                leaderboard.setPreviousRank(previousEntry.get().getRank());
                leaderboard.setRankChange(leaderboard.getPreviousRank() - leaderboard.getRank());
                leaderboard.setPreviousScore(previousEntry.get().getScore());
                leaderboard.setScoreChange(leaderboard.getScore() - previousEntry.get().getScore());
            }
        }
        
        return leaderboards;
    }

    private List<Leaderboard> calculateTeamLeaderboards(List<TeamStatistics> teamStats, String period, String metric, LocalDate date) {
        log.debug("Calculating team leaderboards for period: {} metric: {} date: {} stats count: {}", period, metric, date, teamStats.size());
        
        if (teamStats.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Группируем статистику по командам
        Map<UUID, List<TeamStatistics>> groupedByTeam = teamStats.stream()
                .collect(Collectors.groupingBy(TeamStatistics::getTeamId));
        
        // Рассчитываем очки для каждой команды
        List<Leaderboard> leaderboards = groupedByTeam.entrySet().stream()
                .map(entry -> {
                    UUID teamId = entry.getKey();
                    List<TeamStatistics> stats = entry.getValue();
                    
                    // Агрегируем статистику команды за период
                    TeamStatistics aggregated = aggregateTeamStatsForPeriod(stats);
                    
                    // Рассчитываем очки на основе метрики
                    double score = calculateTeamScoreByMetric(aggregated, metric);
                    
                    return Leaderboard.builder()
                            .leaderboardType("teams")
                            .period(period)
                            .date(date)
                            .entityId(teamId.toString())
                            .entityName(aggregated.getTeamName())
                            .score(score)
                            .rank(0) // Будет рассчитан после сортировки
                            .previousRank(0) // Будет получен из предыдущего периода
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
            LocalDate previousDate = getPreviousPeriodDate(period, date);
            Optional<Leaderboard> previousEntry = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateAndEntityId(
                            "teams", period, previousDate, leaderboard.getEntityId());
            
            if (previousEntry.isPresent()) {
                leaderboard.setPreviousRank(previousEntry.get().getRank());
                leaderboard.setRankChange(leaderboard.getPreviousRank() - leaderboard.getRank());
                leaderboard.setPreviousScore(previousEntry.get().getScore());
                leaderboard.setScoreChange(leaderboard.getScore() - previousEntry.get().getScore());
            }
        }
        
        return leaderboards;
    }

    // Дополнительные вспомогательные методы

    private LocalDate getPeriodStartDate(String period, LocalDate endDate) {
        return switch (period.toLowerCase()) {
            case "daily" -> endDate;
            case "weekly" -> endDate.minusWeeks(1);
            case "monthly" -> endDate.minusMonths(1);
            default -> endDate.minusDays(1);
        };
    }

    private LocalDate getPreviousPeriodDate(String period, LocalDate currentDate) {
        return switch (period.toLowerCase()) {
            case "daily" -> currentDate.minusDays(1);
            case "weekly" -> currentDate.minusWeeks(1);
            case "monthly" -> currentDate.minusMonths(1);
            default -> currentDate.minusDays(1);
        };
    }

    private UserStatistics aggregateUserStatsForPeriod(List<UserStatistics> stats) {
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

    private QuestStatistics aggregateQuestStatsForPeriod(List<QuestStatistics> stats) {
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

    private TeamStatistics aggregateTeamStatsForPeriod(List<TeamStatistics> stats) {
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

    private double calculateQuestScoreByMetric(QuestStatistics stats, String metric) {
        if (stats == null) return 0.0;
        
        return switch (metric.toLowerCase()) {
            case "rating" -> stats.getAvgRating() != null ? stats.getAvgRating() * 100.0 : 0.0;
            case "popularity" -> stats.getViews() * 1.0 + stats.getStarts() * 5.0 + stats.getCompletions() * 20.0;
            case "completion_rate" -> {
                double rate = stats.getStarts() > 0 ? (stats.getCompletions() / (double) stats.getStarts()) * 100.0 : 0.0;
                yield rate * 10.0;
            }
            case "creativity" -> stats.getUniqueParticipants() * 15.0 + stats.getTotalGameTimeMinutes() / 5.0;
            default -> stats.getViews() * 1.0 + stats.getStarts() * 5.0 + stats.getCompletions() * 20.0;
        };
    }

    private double calculateTeamScoreByMetric(TeamStatistics stats, String metric) {
        if (stats == null) return 0.0;
        
        return switch (metric.toLowerCase()) {
            case "rating" -> stats.getCurrentRating() != null ? stats.getCurrentRating() * 100.0 : 0.0;
            case "teamwork" -> stats.getPlayedQuests() * 10.0 + stats.getCompletedQuests() * 30.0;
            case "efficiency" -> {
                double efficiency = stats.getPlayedQuests() > 0 ?
                    (stats.getCompletedQuests() / (double) stats.getPlayedQuests()) * 100.0 : 0.0;
                yield efficiency * 15.0;
            }
            case "collaboration" -> stats.getSuccessfulCodeSubmissions() * 5.0 + stats.getCompletedLevels() * 15.0;
            default -> stats.getPlayedQuests() * 10.0 + stats.getCompletedQuests() * 30.0 + stats.getQuestWins() * 50.0;
        };
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
        
        double score = calculateTeamScoreByMetric(stats, "overall");
        
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
        
        double score = calculateTeamScoreByMetric(stats, "overall");
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

    private double calculateQuestAvgRating(UUID questId) {
        if (questId == null) return 0.0;
        
        // В реальной реализации здесь был бы запрос к данным о рейтингах квестов
        // Пока возвращаем средний рейтинг по умолчанию
        return 4.1;
    }

    private double calculateQuestAvgCompletionTime(UUID questId) {
        if (questId == null) return 0.0;
        
        // В реальной реализации здесь был бы запрос к данным о времени прохождения
        // Пока возвращаем среднее время на основе ID квеста
        return 0; // 30-90 минут
    }

    private int calculateQuestLikesCount(UUID questId) {
        if (questId == null) return 0;
        
        // В реальной реализации здесь был бы запрос к данным о лайках
        // Пока возвращаем количество на основе ID квеста
        return 0;
    }

    @Override
    public Map<String, Object> getSeasonalTrends(String entityType, String period, int year) {
        log.debug("Getting seasonal trends for entityType: {} period: {} year: {}", entityType, period, year);
        
        try {
            Map<String, Object> trends = new HashMap<>();
            
            // Получаем данные за все месяцы года
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            
            List<Leaderboard> leaderboards = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateBetweenOrderByDateDescRank(
                            entityType, period, startDate, endDate);
            
            // Группируем по месяцам
            Map<Integer, List<Leaderboard>> byMonth = leaderboards.stream()
                    .collect(Collectors.groupingBy(l -> l.getDate() != null ? l.getDate().getMonthValue() : 0));
            
            Map<String, Object> monthlyData = new HashMap<>();
            for (int month = 1; month <= 12; month++) {
                List<Leaderboard> monthData = byMonth.getOrDefault(month, Collections.emptyList());
                Map<String, Object> monthStats = new HashMap<>();
                monthStats.put("entriesCount", monthData.size());
                monthStats.put("avgScore", monthData.stream()
                        .filter(l -> l.getScore() != null)
                        .mapToDouble(Leaderboard::getScore)
                        .average()
                        .orElse(0.0));
                monthStats.put("topScore", monthData.stream()
                        .filter(l -> l.getScore() != null)
                        .mapToDouble(Leaderboard::getScore)
                        .max()
                        .orElse(0.0));
                monthlyData.put("month_" + month, monthStats);
            }
            
            trends.put("entityType", entityType);
            trends.put("period", period);
            trends.put("year", year);
            trends.put("monthlyData", monthlyData);
            
            return trends;
            
        } catch (Exception e) {
            log.error("Error getting seasonal trends", e);
            throw new StatisticsNotFoundException("Failed to get seasonal trends", e);
        }
    }

    @Override
    public Map<String, Object> getPositionStabilityAnalysis(Long entityId, String entityType, String period, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting position stability analysis for entityId: {} entityType: {} period: {} startDate: {} endDate: {}",
                entityId, entityType, period, startDate, endDate);
        
        try {
            Map<String, Object> analysis = new HashMap<>();
            
            // Получаем исторические данные лидерборда
            List<Leaderboard> leaderboards = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateBetweenOrderByDateDescRank(
                            entityType, period, startDate, endDate);
            
            // Фильтруем по конкретному entity
            List<Leaderboard> entityLeaderboards = leaderboards.stream()
                    .filter(l -> l.getEntityId() != null && l.getEntityId().equals(entityId))
                    .sorted(Comparator.comparing(Leaderboard::getDate))
                    .collect(Collectors.toList());
            
            if (entityLeaderboards.isEmpty()) {
                analysis.put("hasEnoughData", false);
                analysis.put("message", "No historical data found for the specified period");
                return analysis;
            }
            
            // Вычисляем статистику стабильности
            List<Integer> positions = entityLeaderboards.stream()
                    .map(Leaderboard::getRank)
                    .collect(Collectors.toList());
            
            double avgPosition = positions.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            
            double variance = calculateVariance(positions, avgPosition);
            double stdDev = Math.sqrt(variance);
            
            // Определяем стабильность
            String stabilityLevel;
            if (stdDev < 5.0) {
                stabilityLevel = "HIGH";
            } else if (stdDev < 15.0) {
                stabilityLevel = "MEDIUM";
            } else {
                stabilityLevel = "LOW";
            }
            
            analysis.put("entityId", entityId);
            analysis.put("entityType", entityType);
            analysis.put("period", period);
            analysis.put("hasEnoughData", true);
            analysis.put("dataPoints", positions.size());
            analysis.put("averagePosition", avgPosition);
            analysis.put("minPosition", Collections.min(positions));
            analysis.put("maxPosition", Collections.max(positions));
            analysis.put("positionChange", positions.get(positions.size() - 1) - positions.get(0));
            analysis.put("variance", variance);
            analysis.put("standardDeviation", stdDev);
            analysis.put("stabilityLevel", stabilityLevel);
            analysis.put("positions", positions);
            
            return analysis;
            
        } catch (Exception e) {
            log.error("Error getting position stability analysis", e);
            throw new StatisticsNotFoundException("Failed to get position stability analysis", e);
        }
    }
    
    private double calculateVariance(List<Integer> positions, double mean) {
        if (positions.isEmpty()) return 0.0;
        double sumSquaredDiff = positions.stream()
                .mapToDouble(p -> Math.pow(p - mean, 2))
                .sum();
        return sumSquaredDiff / positions.size();
    }

    @Override
    public Map<String, Object> getPositionMigration(String entityType, String period, LocalDate date) {
        log.debug("Getting position migration for entityType: {} period: {} date: {}", entityType, period, date);
        
        try {
            Map<String, Object> migration = new HashMap<>();
            
            // Получаем данные лидерборда за указанную дату
            List<Leaderboard> leaderboards = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateOrderByRank(entityType, period, date);
            
            if (leaderboards.isEmpty()) {
                migration.put("hasData", false);
                migration.put("message", "No leaderboard data found for the specified date");
                return migration;
            }
            
            // Анализируем миграцию позиций
            List<Map<String, Object>> positionChanges = new ArrayList<>();
            
            // Простая логика: сравниваем соседние позиции
            for (int i = 0; i < leaderboards.size() - 1; i++) {
                Leaderboard current = leaderboards.get(i);
                Leaderboard next = leaderboards.get(i + 1);
                
                Map<String, Object> change = new HashMap<>();
                change.put("fromRank", current.getRank());
                change.put("toRank", next.getRank());
                change.put("entityId", current.getEntityId());
                change.put("score", current.getScore());
                positionChanges.add(change);
            }
            
            migration.put("entityType", entityType);
            migration.put("period", period);
            migration.put("date", date);
            migration.put("hasData", true);
            migration.put("totalEntries", leaderboards.size());
            migration.put("positionChanges", positionChanges);
            
            // Вычисляем статистику миграции
            long upMigrations = positionChanges.stream()
                    .filter(c -> (Integer) c.get("fromRank") < (Integer) c.get("toRank"))
                    .count();
            long downMigrations = positionChanges.stream()
                    .filter(c -> (Integer) c.get("fromRank") > (Integer) c.get("toRank"))
                    .count();
            
            migration.put("upMigrations", upMigrations);
            migration.put("downMigrations", downMigrations);
            migration.put("stablePositions", positionChanges.size() - upMigrations - downMigrations);
            
            return migration;
            
        } catch (Exception e) {
            log.error("Error getting position migration", e);
            throw new StatisticsNotFoundException("Failed to get position migration", e);
        }
    }

    @Override
    public Map<String, Object> getHistoricalLeaderboardData(String entityType, String period, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting historical leaderboard data for entityType: {} period: {} startDate: {} endDate: {}",
                entityType, period, startDate, endDate);
        
        try {
            Map<String, Object> historicalData = new HashMap<>();
            
            // Получаем данные за диапазон дат
            List<Leaderboard> leaderboards = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateBetweenOrderByDateDescRank(
                            entityType, period, startDate, endDate);
            
            if (leaderboards.isEmpty()) {
                historicalData.put("hasData", false);
                historicalData.put("message", "No historical data found for the specified period");
                return historicalData;
            }
            
            // Группируем по датам
            Map<LocalDate, List<Leaderboard>> byDate = leaderboards.stream()
                    .collect(Collectors.groupingBy(Leaderboard::getDate));
            
            // Создаем список записей по датам
            List<Map<String, Object>> dailyData = new ArrayList<>();
            for (Map.Entry<LocalDate, List<Leaderboard>> entry : byDate.entrySet()) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", entry.getKey());
                dayData.put("entriesCount", entry.getValue().size());
                dayData.put("topScore", entry.getValue().stream()
                        .filter(l -> l.getScore() != null)
                        .mapToDouble(Leaderboard::getScore)
                        .max()
                        .orElse(0.0));
                dayData.put("avgScore", entry.getValue().stream()
                        .filter(l -> l.getScore() != null)
                        .mapToDouble(Leaderboard::getScore)
                        .average()
                        .orElse(0.0));
                
                // Добавляем топ-10
                List<Map<String, Object>> topEntries = entry.getValue().stream()
                        .sorted(Comparator.comparing(Leaderboard::getRank))
                        .limit(10)
                        .map(l -> {
                            Map<String, Object> e = new HashMap<>();
                            e.put("rank", l.getRank());
                            e.put("entityId", l.getEntityId());
                            e.put("entityName", l.getEntityName());
                            e.put("score", l.getScore());
                            return e;
                        })
                        .collect(Collectors.toList());
                dayData.put("topEntries", topEntries);
                
                dailyData.add(dayData);
            }
            
            // Сортируем по дате
            dailyData.sort(Comparator.comparing(m -> (LocalDate) m.get("date")));
            
            historicalData.put("entityType", entityType);
            historicalData.put("period", period);
            historicalData.put("startDate", startDate);
            historicalData.put("endDate", endDate);
            historicalData.put("hasData", true);
            historicalData.put("totalDays", byDate.size());
            historicalData.put("dailyData", dailyData);
            
            return historicalData;
            
        } catch (Exception e) {
            log.error("Error getting historical leaderboard data", e);
            throw new StatisticsNotFoundException("Failed to get historical leaderboard data", e);
        }
    }

    @Override
    public Map<String, Object> getCompetitionAnalysis(Long entityId, String entityType, String period, LocalDate date) {
        log.debug("Getting competition analysis for entityId: {} entityType: {} period: {} date: {}",
                entityId, entityType, period, date);
        
        try {
            Map<String, Object> analysis = new HashMap<>();
            
            List<Leaderboard> leaderboards = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateOrderByRank(entityType, period, date);
            
            if (leaderboards.isEmpty()) {
                analysis.put("hasData", false);
                analysis.put("message", "No leaderboard data found");
                return analysis;
            }
            
            Leaderboard entityEntry = leaderboards.stream()
                    .filter(l -> l.getEntityId() != null && l.getEntityId().equals(entityId))
                    .findFirst()
                    .orElse(null);
            
            if (entityEntry == null) {
                analysis.put("hasData", false);
                analysis.put("message", "Entity not found in leaderboard");
                return analysis;
            }
            
            int entityRank = entityEntry.getRank();
            
            List<Leaderboard> nearbyCompetitors = leaderboards.stream()
                    .filter(l -> Math.abs(l.getRank() - entityRank) <= 5)
                    .filter(l -> !l.getEntityId().equals(entityId))
                    .sorted(Comparator.comparing(Leaderboard::getRank))
                    .collect(Collectors.toList());
            
            List<Map<String, Object>> competitorData = nearbyCompetitors.stream()
                    .map(c -> {
                        Map<String, Object> comp = new HashMap<>();
                        comp.put("rank", c.getRank());
                        comp.put("entityId", c.getEntityId());
                        comp.put("entityName", c.getEntityName());
                        comp.put("score", c.getScore());
                        comp.put("rankDifference", c.getRank() - entityRank);
                        return comp;
                    })
                    .collect(Collectors.toList());
            
            analysis.put("entityId", entityId);
            analysis.put("entityType", entityType);
            analysis.put("period", period);
            analysis.put("date", date);
            analysis.put("hasData", true);
            analysis.put("currentRank", entityRank);
            analysis.put("totalEntries", leaderboards.size());
            analysis.put("nearbyCompetitors", competitorData);
            analysis.put("competitorsCount", competitorData.size());
            
            return analysis;
            
        } catch (Exception e) {
            log.error("Error getting competition analysis", e);
            throw new StatisticsNotFoundException("Failed to get competition analysis", e);
        }
    }

    @Override
    public Map<String, Object> getPositionDistribution(String entityType, String period, LocalDate date) {
        log.debug("Getting position distribution for entityType: {} period: {} date: {}", entityType, period, date);
        
        try {
            Map<String, Object> distribution = new HashMap<>();
            
            List<Leaderboard> leaderboards = leaderboardRepository
                    .findByLeaderboardTypeAndPeriodAndDateOrderByRank(entityType, period, date);
            
            if (leaderboards.isEmpty()) {
                distribution.put("hasData", false);
                distribution.put("message", "No leaderboard data found");
                return distribution;
            }
            
            // Группируем по диапазонам позиций
            Map<String, Long> ranges = new LinkedHashMap<>();
            ranges.put("top_10", leaderboards.stream().filter(l -> l.getRank() <= 10).count());
            ranges.put("top_50", leaderboards.stream().filter(l -> l.getRank() > 10 && l.getRank() <= 50).count());
            ranges.put("top_100", leaderboards.stream().filter(l -> l.getRank() > 50 && l.getRank() <= 100).count());
            ranges.put("mid_range", leaderboards.stream().filter(l -> l.getRank() > 100 && l.getRank() <= 500).count());
            ranges.put("lower_range", leaderboards.stream().filter(l -> l.getRank() > 500).count());
            
            distribution.put("entityType", entityType);
            distribution.put("period", period);
            distribution.put("date", date);
            distribution.put("hasData", true);
            distribution.put("totalEntries", leaderboards.size());
            distribution.put("ranges", ranges);
            
            return distribution;
            
        } catch (Exception e) {
            log.error("Error getting position distribution", e);
            throw new StatisticsNotFoundException("Failed to get position distribution", e);
        }
    }
}