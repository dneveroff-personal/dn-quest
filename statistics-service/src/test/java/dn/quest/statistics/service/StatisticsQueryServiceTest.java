package dn.quest.statistics.service;

import dn.quest.statistics.dto.StatisticsRequestDTO;
import dn.quest.statistics.dto.UserStatisticsDTO;
import dn.quest.statistics.entity.*;
import dn.quest.statistics.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для StatisticsQueryService
 */
@ExtendWith(MockitoExtension.class)
class StatisticsQueryServiceTest {

    @Mock
    private UserStatisticsRepository userStatisticsRepository;
    
    @Mock
    private QuestStatisticsRepository questStatisticsRepository;
    
    @Mock
    private TeamStatisticsRepository teamStatisticsRepository;
    
    @Mock
    private GameStatisticsRepository gameStatisticsRepository;
    
    @Mock
    private SystemStatisticsRepository systemStatisticsRepository;
    
    @Mock
    private DailyAggregatedStatisticsRepository dailyAggregatedStatisticsRepository;
    
    @Mock
    private CacheService cacheService;
    
    @InjectMocks
    private StatisticsQueryServiceImpl statisticsQueryService;
    
    private UserStatistics testUserStatistics;
    private QuestStatistics testQuestStatistics;
    private TeamStatistics testTeamStatistics;
    private SystemStatistics testSystemStatistics;
    
    @BeforeEach
    void setUp() {
        LocalDate testDate = LocalDate.now();
        
        testUserStatistics = UserStatistics.builder()
                .id(1L)
                .userId(100L)
                .date(testDate)
                .registrations(1)
                .logins(5)
                .gameSessions(3)
                .completedQuests(2)
                .createdQuests(1)
                .createdTeams(0)
                .teamMemberships(1)
                .totalGameTimeMinutes(120L)
                .uploadedFiles(2)
                .totalFileSizeBytes(1024L)
                .successfulCodeSubmissions(10)
                .failedCodeSubmissions(2)
                .completedLevels(5)
                .lastActiveAt(LocalDateTime.now())
                .build();
        
        testQuestStatistics = QuestStatistics.builder()
                .id(1L)
                .questId(200L)
                .date(testDate)
                .views(50)
                .starts(20)
                .completions(10)
                .averageCompletionTimeMinutes(30L)
                .rating(4.5)
                .ratingCount(8)
                .build();
        
        testTeamStatistics = TeamStatistics.builder()
                .id(1L)
                .teamId(300L)
                .date(testDate)
                .activeMembers(3)
                .totalMembers(5)
                .gameSessions(2)
                .completedQuests(1)
                .averageScore(85.0)
                .build();
        
        testSystemStatistics = SystemStatistics.builder()
                .id(1L)
                .date(testDate)
                .category("performance")
                .metric("response_time")
                .value(150.0)
                .totalRequests(1000L)
                .successfulRequests(950L)
                .failedRequests(50L)
                .averageResponseTimeMs(150.0)
                .cpuUsagePercent(45.0)
                .memoryUsagePercent(60.0)
                .diskUsagePercent(30.0)
                .build();
    }
    
    @Test
    void testGetPlatformOverview() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        when(userStatisticsRepository.countDistinctUsersByDateBetween(startDate, endDate))
                .thenReturn(100L);
        when(questStatisticsRepository.countDistinctQuestsByDateBetween(startDate, endDate))
                .thenReturn(50L);
        when(gameStatisticsRepository.countDistinctSessionsByDateBetween(startDate, endDate))
                .thenReturn(200L);
        when(teamStatisticsRepository.countDistinctTeamsByDateBetween(startDate, endDate))
                .thenReturn(25L);
        
        // When
        Map<String, Object> result = statisticsQueryService.getPlatformOverview(startDate, endDate);
        
        // Then
        assertNotNull(result);
        assertEquals(100L, result.get("totalUsers"));
        assertEquals(50L, result.get("totalQuests"));
        assertEquals(200L, result.get("totalSessions"));
        assertEquals(25L, result.get("totalTeams"));
        assertNotNull(result.get("trends"));
        
        verify(userStatisticsRepository).countDistinctUsersByDateBetween(startDate, endDate);
        verify(questStatisticsRepository).countDistinctQuestsByDateBetween(startDate, endDate);
        verify(gameStatisticsRepository).countDistinctSessionsByDateBetween(startDate, endDate);
        verify(teamStatisticsRepository).countDistinctTeamsByDateBetween(startDate, endDate);
    }
    
    @Test
    void testGetUserStatistics() {
        // Given
        Long userId = 100L;
        LocalDate date = LocalDate.now();
        
        when(userStatisticsRepository.findByUserIdAndDate(userId, date))
                .thenReturn(Optional.of(testUserStatistics));
        
        // When
        UserStatisticsDTO result = statisticsQueryService.getUserStatistics(userId, date);
        
        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(date, result.getDate());
        assertEquals(5, result.getLogins());
        assertEquals(3, result.getGameSessions());
        assertEquals(2, result.getCompletedQuests());
        
        verify(userStatisticsRepository).findByUserIdAndDate(userId, date);
    }
    
    @Test
    void testGetUserStatisticsNotFound() {
        // Given
        Long userId = 100L;
        LocalDate date = LocalDate.now();
        
        when(userStatisticsRepository.findByUserIdAndDate(userId, date))
                .thenReturn(Optional.empty());
        
        // When
        UserStatisticsDTO result = statisticsQueryService.getUserStatistics(userId, date);
        
        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(date, result.getDate());
        assertEquals(0, result.getLogins());
        assertEquals(0, result.getGameSessions());
        
        verify(userStatisticsRepository).findByUserIdAndDate(userId, date);
    }
    
    @Test
    void testGetUserStatisticsForPeriod() {
        // Given
        Long userId = 100L;
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 20);
        
        List<UserStatistics> statisticsList = Arrays.asList(testUserStatistics);
        Page<UserStatistics> statisticsPage = new PageImpl<>(statisticsList, pageable, 1);
        
        when(userStatisticsRepository.findByUserIdAndDateBetween(userId, startDate, endDate, pageable))
                .thenReturn(statisticsPage);
        
        // When
        Page<UserStatisticsDTO> result = statisticsQueryService.getUserStatisticsForPeriod(
                userId, startDate, endDate, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        
        UserStatisticsDTO dto = result.getContent().get(0);
        assertEquals(userId, dto.getUserId());
        assertEquals(testUserStatistics.getDate(), dto.getDate());
        
        verify(userStatisticsRepository).findByUserIdAndDateBetween(userId, startDate, endDate, pageable);
    }
    
    @Test
    void testGetQuestStatistics() {
        // Given
        Long questId = 200L;
        LocalDate date = LocalDate.now();
        
        when(questStatisticsRepository.findByQuestIdAndDate(questId, date))
                .thenReturn(Optional.of(testQuestStatistics));
        
        // When
        Map<String, Object> result = statisticsQueryService.getQuestStatistics(questId, date);
        
        // Then
        assertNotNull(result);
        assertEquals(questId, result.get("questId"));
        assertEquals(date, result.get("date"));
        assertEquals(50, result.get("views"));
        assertEquals(20, result.get("starts"));
        assertEquals(10, result.get("completions"));
        assertNotNull(result.get("completionRate"));
        
        verify(questStatisticsRepository).findByQuestIdAndDate(questId, date);
    }
    
    @Test
    void testGetQuestStatisticsNotFound() {
        // Given
        Long questId = 200L;
        LocalDate date = LocalDate.now();
        
        when(questStatisticsRepository.findByQuestIdAndDate(questId, date))
                .thenReturn(Optional.empty());
        
        // When
        Map<String, Object> result = statisticsQueryService.getQuestStatistics(questId, date);
        
        // Then
        assertNotNull(result);
        assertEquals(questId, result.get("questId"));
        assertEquals(date, result.get("date"));
        assertEquals(0, result.get("views"));
        assertEquals(0, result.get("starts"));
        assertEquals(0, result.get("completions"));
        assertEquals(0.0, result.get("completionRate"));
        
        verify(questStatisticsRepository).findByQuestIdAndDate(questId, date);
    }
    
    @Test
    void testGetTeamStatistics() {
        // Given
        Long teamId = 300L;
        LocalDate date = LocalDate.now();
        
        when(teamStatisticsRepository.findByTeamIdAndDate(teamId, date))
                .thenReturn(Optional.of(testTeamStatistics));
        
        // When
        Map<String, Object> result = statisticsQueryService.getTeamStatistics(teamId, date);
        
        // Then
        assertNotNull(result);
        assertEquals(teamId, result.get("teamId"));
        assertEquals(date, result.get("date"));
        assertEquals(3, result.get("activeMembers"));
        assertEquals(5, result.get("totalMembers"));
        assertEquals(2, result.get("gameSessions"));
        assertNotNull(result.get("activeRate"));
        
        verify(teamStatisticsRepository).findByTeamIdAndDate(teamId, date);
    }
    
    @Test
    void testGetCustomStatistics() {
        // Given
        StatisticsRequestDTO request = StatisticsRequestDTO.builder()
                .statisticsType("user")
                .userId(100L)
                .endDate(LocalDate.now())
                .build();
        
        when(userStatisticsRepository.findByUserIdAndDate(anyLong(), any()))
                .thenReturn(Optional.of(testUserStatistics));
        
        // When
        Map<String, Object> result = statisticsQueryService.getCustomStatistics(request);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.get("userStatistics"));
        assertEquals(request, result.get("request"));
        
        verify(userStatisticsRepository).findByUserIdAndDate(anyLong(), any());
    }
    
    @Test
    void testGetTopUsers() {
        // Given
        String metric = "completedQuests";
        int limit = 10;
        LocalDate date = LocalDate.now();
        
        List<UserStatistics> topUsers = Arrays.asList(testUserStatistics);
        when(userStatisticsRepository.findTopUsersByCompletedQuests(date, limit))
                .thenReturn(topUsers);
        
        // When
        List<Map<String, Object>> result = statisticsQueryService.getTopUsers(metric, limit, date);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Map<String, Object> userStats = result.get(0);
        assertEquals(testUserStatistics.getUserId(), userStats.get("userId"));
        assertEquals(testUserStatistics.getCompletedQuests(), userStats.get("completedQuests"));
        
        verify(userStatisticsRepository).findTopUsersByCompletedQuests(date, limit);
    }
    
    @Test
    void testGetTopQuests() {
        // Given
        String metric = "completions";
        int limit = 10;
        LocalDate date = LocalDate.now();
        
        List<QuestStatistics> topQuests = Arrays.asList(testQuestStatistics);
        when(questStatisticsRepository.findTopQuestsByCompletions(date, limit))
                .thenReturn(topQuests);
        
        // When
        List<Map<String, Object>> result = statisticsQueryService.getTopQuests(metric, limit, date);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        Map<String, Object> questStats = result.get(0);
        assertEquals(testQuestStatistics.getQuestId(), questStats.get("questId"));
        assertEquals(testQuestStatistics.getCompletions(), questStats.get("completions"));
        
        verify(questStatisticsRepository).findTopQuestsByCompletions(date, limit);
    }
    
    @Test
    void testGetStatisticsByCategories() {
        // Given
        String entityType = "quest";
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        // When
        Map<String, Object> result = statisticsQueryService.getStatisticsByCategories(entityType, startDate, endDate);
        
        // Then
        assertNotNull(result);
        assertEquals(entityType, result.get("entityType"));
        assertNotNull(result.get("categories"));
        assertNotNull(result.get("period"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categories = (List<Map<String, Object>>) result.get("categories");
        assertFalse(categories.isEmpty());
    }
    
    @Test
    void testGetMetricTrends() {
        // Given
        List<String> metrics = Arrays.asList("users", "quests");
        String period = "days";
        int periods = 7;
        
        // When
        Map<String, Object> result = statisticsQueryService.getMetricTrends(metrics, period, periods);
        
        // Then
        assertNotNull(result);
        assertEquals(period, result.get("period"));
        assertEquals(periods, result.get("periods"));
        assertNotNull(result.get("metrics"));
        
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> metricData = 
                (Map<String, List<Map<String, Object>>>) result.get("metrics");
        
        assertTrue(metricData.containsKey("users"));
        assertTrue(metricData.containsKey("quests"));
        assertEquals(7, metricData.get("users").size());
        assertEquals(7, metricData.get("quests").size());
    }
    
    @Test
    void testGetSystemStatistics() {
        // Given
        String category = "performance";
        LocalDate date = LocalDate.now();
        
        when(systemStatisticsRepository.findByCategoryAndDate(category, date))
                .thenReturn(Optional.of(testSystemStatistics));
        
        // When
        Map<String, Object> result = statisticsQueryService.getSystemStatistics(category, date);
        
        // Then
        assertNotNull(result);
        assertEquals(category, result.get("category"));
        assertEquals(date, result.get("date"));
        assertEquals(1000L, result.get("totalRequests"));
        assertEquals(950L, result.get("successfulRequests"));
        assertEquals(50L, result.get("failedRequests"));
        assertEquals(150.0, result.get("averageResponseTimeMs"));
        assertEquals(45.0, result.get("cpuUsagePercent"));
        assertEquals(60.0, result.get("memoryUsagePercent"));
        assertEquals(30.0, result.get("diskUsagePercent"));
        assertNotNull(result.get("successRate"));
        
        verify(systemStatisticsRepository).findByCategoryAndDate(category, date);
    }
    
    @Test
    void testGetSystemStatisticsNotFound() {
        // Given
        String category = "performance";
        LocalDate date = LocalDate.now();
        
        when(systemStatisticsRepository.findByCategoryAndDate(category, date))
                .thenReturn(Optional.empty());
        
        // When
        Map<String, Object> result = statisticsQueryService.getSystemStatistics(category, date);
        
        // Then
        assertNotNull(result);
        assertEquals(category, result.get("category"));
        assertEquals(date, result.get("date"));
        assertEquals(0L, result.get("totalRequests"));
        assertEquals(0L, result.get("successfulRequests"));
        assertEquals(0L, result.get("failedRequests"));
        assertEquals(0.0, result.get("averageResponseTimeMs"));
        assertEquals(0.0, result.get("cpuUsagePercent"));
        assertEquals(0.0, result.get("memoryUsagePercent"));
        assertEquals(0.0, result.get("diskUsagePercent"));
        assertEquals(0.0, result.get("successRate"));
        
        verify(systemStatisticsRepository).findByCategoryAndDate(category, date);
    }
}