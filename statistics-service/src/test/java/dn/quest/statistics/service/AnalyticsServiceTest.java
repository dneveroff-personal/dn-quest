package dn.quest.statistics.service;

import dn.quest.statistics.dto.AnalyticsReportDTO;
import dn.quest.statistics.dto.StatisticsRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для AnalyticsService
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;
    
    private StatisticsRequestDTO testRequest;
    private LocalDate testDate;
    
    @BeforeEach
    void setUp() {
        testDate = LocalDate.now();
        testRequest = StatisticsRequestDTO.builder()
                .statisticsType("user")
                .startDate(testDate.minusDays(7))
                .endDate(testDate)
                .userId(100L)
                .build();
    }
    
    @Test
    void testGenerateAnalyticsReport() {
        // When
        AnalyticsReportDTO result = analyticsService.generateAnalyticsReport(testRequest);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getReportId());
        assertNotNull(result.getGeneratedAt());
        assertEquals(testRequest.getStatisticsType(), result.getStatisticsType());
        assertEquals(testRequest.getStartDate(), result.getStartDate());
        assertEquals(testRequest.getEndDate(), result.getEndDate());
        assertNotNull(result.getData());
    }
    
    @Test
    void testGetUserEngagementReport() {
        // Given
        LocalDate startDate = testDate.minusDays(7);
        LocalDate endDate = testDate;
        String groupBy = "day";
        
        // When
        Map<String, Object> result = analyticsService.getUserEngagementReport(startDate, endDate, groupBy);
        
        // Then
        assertNotNull(result);
        assertEquals("user_engagement", result.get("reportType"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, result.get("endDate"));
        assertEquals(groupBy, result.get("groupBy"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("dailyStats"));
        assertTrue(data.containsKey("summary"));
        assertTrue(data.containsKey("trends"));
    }
    
    @Test
    void testGetQuestPerformanceReport() {
        // Given
        LocalDate startDate = testDate.minusDays(7);
        LocalDate endDate = testDate;
        String category = "programming";
        Long authorId = 200L;
        
        // When
        Map<String, Object> result = analyticsService.getQuestPerformanceReport(
                startDate, endDate, category, authorId);
        
        // Then
        assertNotNull(result);
        assertEquals("quest_performance", result.get("reportType"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, result.get("endDate"));
        assertEquals(category, result.get("category"));
        assertEquals(authorId, result.get("authorId"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("questStats"));
        assertTrue(data.containsKey("performanceMetrics"));
        assertTrue(data.containsKey("popularQuests"));
    }
    
    @Test
    void testGetGameSessionReport() {
        // Given
        LocalDate startDate = testDate.minusDays(7);
        LocalDate endDate = testDate;
        Long questId = 300L;
        Long userId = 100L;
        
        // When
        Map<String, Object> result = analyticsService.getGameSessionReport(
                startDate, endDate, questId, userId);
        
        // Then
        assertNotNull(result);
        assertEquals("game_session", result.get("reportType"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, result.get("endDate"));
        assertEquals(questId, result.get("questId"));
        assertEquals(userId, result.get("userId"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("sessionStats"));
        assertTrue(data.containsKey("completionRates"));
        assertTrue(data.containsKey("averageTimes"));
    }
    
    @Test
    void testGetTeamActivityReport() {
        // Given
        LocalDate startDate = testDate.minusDays(7);
        LocalDate endDate = testDate;
        String teamType = "competitive";
        
        // When
        Map<String, Object> result = analyticsService.getTeamActivityReport(
                startDate, endDate, teamType);
        
        // Then
        assertNotNull(result);
        assertEquals("team_activity", result.get("reportType"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, result.get("endDate"));
        assertEquals(teamType, result.get("teamType"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("teamStats"));
        assertTrue(data.containsKey("activityMetrics"));
        assertTrue(data.containsKey("topTeams"));
    }
    
    @Test
    void testGetFileActivityReport() {
        // Given
        LocalDate startDate = testDate.minusDays(7);
        LocalDate endDate = testDate;
        String fileType = "image";
        
        // When
        Map<String, Object> result = analyticsService.getFileActivityReport(
                startDate, endDate, fileType);
        
        // Then
        assertNotNull(result);
        assertEquals("file_activity", result.get("reportType"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, endDate);
        assertEquals(fileType, result.get("fileType"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("fileStats"));
        assertTrue(data.containsKey("storageMetrics"));
        assertTrue(data.containsKey("popularTypes"));
    }
    
    @Test
    void testGetForecasts() {
        // Given
        String forecastType = "users";
        int periodDays = 30;
        double confidenceLevel = 0.9;
        
        // When
        Map<String, Object> result = analyticsService.getForecasts(
                forecastType, periodDays, confidenceLevel);
        
        // Then
        assertNotNull(result);
        assertEquals("forecast", result.get("reportType"));
        assertEquals(forecastType, result.get("forecastType"));
        assertEquals(periodDays, result.get("periodDays"));
        assertEquals(confidenceLevel, result.get("confidenceLevel"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("predictions"));
        assertTrue(data.containsKey("confidenceIntervals"));
        assertTrue(data.containsKey("accuracy"));
    }
    
    @Test
    void testGetCohortAnalysis() {
        // Given
        LocalDate startDate = testDate.minusDays(30);
        LocalDate endDate = testDate;
        String cohortSize = "week";
        
        // When
        Map<String, Object> result = analyticsService.getCohortAnalysis(
                startDate, endDate, cohortSize);
        
        // Then
        assertNotNull(result);
        assertEquals("cohort_analysis", result.get("reportType"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, result.get("endDate"));
        assertEquals(cohortSize, result.get("cohortSize"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("cohorts"));
        assertTrue(data.containsKey("retentionRates"));
        assertTrue(data.containsKey("summary"));
    }
    
    @Test
    void testGetConversionFunnel() {
        // Given
        LocalDate startDate = testDate.minusDays(7);
        LocalDate endDate = testDate;
        String funnelType = "quest_completion";
        
        // When
        Map<String, Object> result = analyticsService.getConversionFunnel(
                startDate, endDate, funnelType);
        
        // Then
        assertNotNull(result);
        assertEquals("conversion_funnel", result.get("reportType"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, result.get("endDate"));
        assertEquals(funnelType, result.get("funnelType"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("funnelSteps"));
        assertTrue(data.containsKey("conversionRates"));
        assertTrue(data.containsKey("dropOffPoints"));
    }
    
    @Test
    void testGetUserSegmentation() {
        // Given
        String segmentationType = "activity";
        LocalDate startDate = testDate.minusDays(7);
        LocalDate endDate = testDate;
        
        // When
        Map<String, Object> result = analyticsService.getUserSegmentation(
                segmentationType, startDate, endDate);
        
        // Then
        assertNotNull(result);
        assertEquals("user_segmentation", result.get("reportType"));
        assertEquals(segmentationType, result.get("segmentationType"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, result.get("endDate"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("segments"));
        assertTrue(data.containsKey("segmentSizes"));
        assertTrue(data.containsKey("characteristics"));
    }
    
    @Test
    void testGetAvailableReportTypes() {
        // When
        List<Map<String, String>> result = analyticsService.getAvailableReportTypes();
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Проверяем наличие основных типов отчетов
        assertTrue(result.stream().anyMatch(type -> 
                "user_engagement".equals(type.get("type"))));
        assertTrue(result.stream().anyMatch(type -> 
                "quest_performance".equals(type.get("type"))));
        assertTrue(result.stream().anyMatch(type -> 
                "game_session".equals(type.get("type"))));
    }
    
    @Test
    void testGetReportsMetadata() {
        // When
        Map<String, Object> result = analyticsService.getReportsMetadata();
        
        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("reportTypes"));
        assertTrue(result.containsKey("parameters"));
        assertTrue(result.containsKey("formats"));
        assertTrue(result.containsKey("filters"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reportTypes = (List<Map<String, Object>>) result.get("reportTypes");
        assertFalse(reportTypes.isEmpty());
    }
    
    @Test
    void testGetKpiMetrics() {
        // Given
        LocalDate startDate = testDate.minusDays(7);
        LocalDate endDate = testDate;
        
        // When
        Map<String, Object> result = analyticsService.getKpiMetrics(startDate, endDate);
        
        // Then
        assertNotNull(result);
        assertEquals("kpi_metrics", result.get("reportType"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, result.get("endDate"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("userMetrics"));
        assertTrue(data.containsKey("questMetrics"));
        assertTrue(data.containsKey("systemMetrics"));
    }
    
    @Test
    void testGetUserRetentionReport() {
        // Given
        LocalDate startDate = testDate.minusDays(30);
        LocalDate endDate = testDate;
        String period = "weekly";
        
        // When
        Map<String, Object> result = analyticsService.getUserRetentionReport(
                startDate, endDate, period);
        
        // Then
        assertNotNull(result);
        assertEquals("user_retention", result.get("reportType"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, result.get("endDate"));
        assertEquals(period, result.get("period"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("retentionRates"));
        assertTrue(data.containsKey("churnRates"));
        assertTrue(data.containsKey("lifecycleStages"));
    }
    
    @Test
    void testGetMonetizationReport() {
        // Given
        LocalDate startDate = testDate.minusDays(30);
        LocalDate endDate = testDate;
        
        // When
        Map<String, Object> result = analyticsService.getMonetizationReport(startDate, endDate);
        
        // Then
        assertNotNull(result);
        assertEquals("monetization", result.get("reportType"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, result.get("endDate"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("revenue"));
        assertTrue(data.containsKey("conversionRates"));
        assertTrue(data.containsKey("arpu"));
    }
    
    @Test
    void testGetSystemPerformanceReport() {
        // Given
        LocalDate startDate = testDate.minusDays(7);
        LocalDate endDate = testDate;
        
        // When
        Map<String, Object> result = analyticsService.getSystemPerformanceReport(startDate, endDate);
        
        // Then
        assertNotNull(result);
        assertEquals("system_performance", result.get("reportType"));
        assertEquals(startDate, result.get("startDate"));
        assertEquals(endDate, result.get("endDate"));
        assertNotNull(result.get("data"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(data.containsKey("responseTimes"));
        assertTrue(data.containsKey("errorRates"));
        assertTrue(data.containsKey("resourceUsage"));
    }
}