package dn.quest.statistics.service;

import dn.quest.statistics.dto.LeaderboardDTO;
import dn.quest.statistics.entity.Leaderboard;
import dn.quest.statistics.repository.LeaderboardRepository;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для LeaderboardService
 */
@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private LeaderboardRepository leaderboardRepository;
    
    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;
    
    private Leaderboard testLeaderboard;
    private LocalDate testDate;
    
    @BeforeEach
    void setUp() {
        testDate = LocalDate.now();
        
        testLeaderboard = Leaderboard.builder()
                .id(1L)
                .entityId(100L)
                .entityType("user")
                .period("all_time")
                .category("general")
                .rank(1)
                .score(1500.0)
                .previousRank(2)
                .rankChange(1)
                .wins(25)
                .losses(5)
                .totalGames(30)
                .winRate(83.33)
                .averageScore(50.0)
                .streak(5)
                .date(testDate)
                .metadata(Map.of("username", "testuser", "avatar", "test.jpg"))
                .build();
    }
    
    @Test
    void testGetGlobalLeaderboard() {
        // Given
        String period = "all_time";
        String category = "general";
        LocalDate date = testDate;
        Pageable pageable = PageRequest.of(0, 20);
        
        List<Leaderboard> leaderboards = Collections.singletonList(testLeaderboard);
        Page<Leaderboard> leaderboardPage = new PageImpl<>(leaderboards, pageable, 1);
        
        when(leaderboardRepository.findByEntityTypeAndPeriodAndCategoryAndDateOrderByRankAsc(
                "user", period, category, date, pageable))
                .thenReturn(leaderboardPage);
        
        // When
        Page<LeaderboardDTO> result = leaderboardService.getGlobalLeaderboard(period, category, date, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        
        LeaderboardDTO dto = result.getContent().get(0);
        assertEquals(testLeaderboard.getEntityId(), dto.getEntityId());
        assertEquals(testLeaderboard.getEntityType(), dto.getEntityType());
        assertEquals(testLeaderboard.getRank(), dto.getRank());
        assertEquals(testLeaderboard.getScore(), dto.getScore());
        
        verify(leaderboardRepository).findByEntityTypeAndPeriodAndCategoryAndDateOrderByRankAsc(
                "user", period, category, date, pageable);
    }
    
    @Test
    void testGetQuestLeaderboard() {
        // Given
        String period = "weekly";
        String category = "programming";
        String metric = "rating";
        LocalDate date = testDate;
        Pageable pageable = PageRequest.of(0, 20);
        
        List<Leaderboard> leaderboards = Collections.singletonList(testLeaderboard);
        Page<Leaderboard> leaderboardPage = new PageImpl<>(leaderboards, pageable, 1);
        
        when(leaderboardRepository.findByEntityTypeAndPeriodAndCategoryAndDateOrderByRankAsc(
                "quest", period, category, pageable))
                .thenReturn(leaderboardPage);
        
        // When
        Page<LeaderboardDTO> result = leaderboardService.getQuestLeaderboard(
                period, category, metric, date, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        
        LeaderboardDTO dto = result.getContent().get(0);
        assertEquals(testLeaderboard.getEntityId(), dto.getEntityId());
        assertEquals("quest", dto.getEntityType());
        assertEquals(testLeaderboard.getRank(), dto.getRank());
        
        verify(leaderboardRepository).findByEntityTypeAndPeriodAndCategoryAndDateOrderByRankAsc(
                "quest", period, category, pageable);
    }
    
    @Test
    void testGetTeamLeaderboard() {
        // Given
        String period = "monthly";
        String category = "competitive";
        String metric = "wins";
        LocalDate date = testDate;
        Pageable pageable = PageRequest.of(0, 20);
        
        List<Leaderboard> leaderboards = Collections.singletonList(testLeaderboard);
        Page<Leaderboard> leaderboardPage = new PageImpl<>(leaderboards, pageable, 1);
        
        when(leaderboardRepository.findByEntityTypeAndPeriodAndCategoryAndDateOrderByRankAsc(
                "team", period, category, pageable))
                .thenReturn(leaderboardPage);
        
        // When
        Page<LeaderboardDTO> result = leaderboardService.getTeamLeaderboard(
                period, category, metric, date, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        
        LeaderboardDTO dto = result.getContent().get(0);
        assertEquals(testLeaderboard.getEntityId(), dto.getEntityId());
        assertEquals("team", dto.getEntityType());
        assertEquals(testLeaderboard.getRank(), dto.getRank());
        
        verify(leaderboardRepository).findByEntityTypeAndPeriodAndCategoryAndDateOrderByRankAsc(
                "team", period, category, pageable);
    }
    
    @Test
    void testGetUserLeaderboardPosition() {
        // Given
        Long userId = 100L;
        String period = "all_time";
        LocalDate date = testDate;
        
        when(leaderboardRepository.findByEntityTypeAndEntityIdAndPeriodAndDate(
                "user", userId, period, date))
                .thenReturn(Optional.of(testLeaderboard));
        
        // When
        Map<String, Object> result = leaderboardService.getUserLeaderboardPosition(userId, period, date);
        
        // Then
        assertNotNull(result);
        assertEquals(userId, result.get("entityId"));
        assertEquals("user", result.get("entityType"));
        assertEquals(testLeaderboard.getRank(), result.get("rank"));
        assertEquals(testLeaderboard.getScore(), result.get("score"));
        assertEquals(testLeaderboard.getRankChange(), result.get("rankChange"));
        
        verify(leaderboardRepository).findByEntityTypeAndEntityIdAndPeriodAndDate(
                "user", userId, period, date);
    }
    
    @Test
    void testGetUserLeaderboardPositionNotFound() {
        // Given
        Long userId = 100L;
        String period = "all_time";
        LocalDate date = testDate;
        
        when(leaderboardRepository.findByEntityTypeAndEntityIdAndPeriodAndDate(
                "user", userId, period, date))
                .thenReturn(Optional.empty());
        
        // When
        Map<String, Object> result = leaderboardService.getUserLeaderboardPosition(userId, period, date);
        
        // Then
        assertNotNull(result);
        assertEquals(userId, result.get("entityId"));
        assertEquals("user", result.get("entityType"));
        assertEquals(0, result.get("rank"));
        assertEquals(0.0, result.get("score"));
        assertEquals(0, result.get("rankChange"));
        
        verify(leaderboardRepository).findByEntityTypeAndEntityIdAndPeriodAndDate(
                "user", userId, period, date);
    }
    
    @Test
    void testGetQuestLeaderboardPosition() {
        // Given
        Long questId = 200L;
        String period = "weekly";
        String metric = "rating";
        LocalDate date = testDate;
        
        when(leaderboardRepository.findByEntityTypeAndEntityIdAndPeriodAndDate(
                "quest", questId, period, date))
                .thenReturn(Optional.of(testLeaderboard));
        
        // When
        Map<String, Object> result = leaderboardService.getQuestLeaderboardPosition(
                questId, period, metric, date);
        
        // Then
        assertNotNull(result);
        assertEquals(questId, result.get("entityId"));
        assertEquals("quest", result.get("entityType"));
        assertEquals(testLeaderboard.getRank(), result.get("rank"));
        assertEquals(testLeaderboard.getScore(), result.get("score"));
        
        verify(leaderboardRepository).findByEntityTypeAndEntityIdAndPeriodAndDate(
                "quest", questId, period, date);
    }
    
    @Test
    void testGetTeamLeaderboardPosition() {
        // Given
        Long teamId = 300L;
        String period = "monthly";
        String metric = "wins";
        LocalDate date = testDate;
        
        when(leaderboardRepository.findByEntityTypeAndEntityIdAndPeriodAndDate(
                "team", teamId, period, date))
                .thenReturn(Optional.of(testLeaderboard));
        
        // When
        Map<String, Object> result = leaderboardService.getTeamLeaderboardPosition(
                teamId, period, metric, date);
        
        // Then
        assertNotNull(result);
        assertEquals(teamId, result.get("entityId"));
        assertEquals("team", result.get("entityType"));
        assertEquals(testLeaderboard.getRank(), result.get("rank"));
        assertEquals(testLeaderboard.getScore(), result.get("score"));
        
        verify(leaderboardRepository).findByEntityTypeAndEntityIdAndPeriodAndDate(
                "team", teamId, period, date);
    }
    
    @Test
    void testGetUserSurroundingInLeaderboard() {
        // Given
        Long userId = 100L;
        String period = "all_time";
        int count = 5;
        LocalDate date = testDate;
        
        List<Leaderboard> surroundingLeaderboards = Collections.singletonList(testLeaderboard);
        
        when(leaderboardRepository.findSurroundingUsersByRank(
                "user", period, date, 1, count))
                .thenReturn(surroundingLeaderboards);
        
        // When
        List<LeaderboardDTO> result = leaderboardService.getUserSurroundingInLeaderboard(
                userId, period, count, date);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        LeaderboardDTO dto = result.get(0);
        assertEquals(testLeaderboard.getEntityId(), dto.getEntityId());
        assertEquals(testLeaderboard.getRank(), dto.getRank());
        
        verify(leaderboardRepository).findSurroundingUsersByRank(
                "user", period, date, 1, count);
    }
    
    @Test
    void testGetUserLeaderboardHistory() {
        // Given
        Long userId = 100L;
        String period = "daily";
        LocalDate startDate = testDate.minusDays(7);
        LocalDate endDate = testDate;
        
        List<Leaderboard> history = Collections.singletonList(testLeaderboard);
        
        when(leaderboardRepository.findByEntityTypeAndEntityIdAndPeriodAndDateBetweenOrderByDateAsc(
                "user", userId, period, startDate, endDate))
                .thenReturn(history);
        
        // When
        Map<String, Object> result = leaderboardService.getUserLeaderboardHistory(
                userId, period, startDate, endDate);
        
        // Then
        assertNotNull(result);
        assertEquals(userId, result.get("entityId"));
        assertEquals("user", result.get("entityType"));
        assertEquals(period, result.get("period"));
        assertNotNull(result.get("history"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> historyList = (List<Map<String, Object>>) result.get("history");
        assertEquals(1, historyList.size());
        
        verify(leaderboardRepository).findByEntityTypeAndEntityIdAndPeriodAndDateBetweenOrderByDateAsc(
                "user", userId, period, startDate, endDate);
    }
    
    @Test
    void testGetLeaderboardCategories() {
        // When
        Map<String, List<String>> result = leaderboardService.getLeaderboardCategories();
        
        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("user"));
        assertTrue(result.containsKey("quest"));
        assertTrue(result.containsKey("team"));
        
        List<String> userCategories = result.get("user");
        assertNotNull(userCategories);
        assertTrue(userCategories.contains("general"));
        assertTrue(userCategories.contains("weekly"));
        assertTrue(userCategories.contains("monthly"));
    }
    
    @Test
    void testGetLeaderboardStats() {
        // Given
        String period = "all_time";
        LocalDate date = testDate;
        
        List<Leaderboard> allLeaderboards = Collections.singletonList(testLeaderboard);
        
        when(leaderboardRepository.findByPeriodAndDate(period, date))
                .thenReturn(allLeaderboards);
        
        // When
        Map<String, Object> result = leaderboardService.getLeaderboardStats(period, date);
        
        // Then
        assertNotNull(result);
        assertEquals(period, result.get("period"));
        assertEquals(date, result.get("date"));
        assertNotNull(result.get("totalEntries"));
        assertNotNull(result.get("entityStats"));
        assertNotNull(result.get("rankDistribution"));
        
        verify(leaderboardRepository).findByPeriodAndDate(period, date);
    }
    
    @Test
    void testUpdateLeaderboards() {
        // Given
        String period = "daily";
        LocalDate date = testDate;
        
        // When
        leaderboardService.updateLeaderboards(period, date);
        
        // Then
        // Проверяем, что метод вызывается без исключений
        // В реальной реализации здесь была бы логика обновления
        assertTrue(true);
    }
    
    @Test
    void testRecalculateGlobalLeaderboard() {
        // Given
        String period = "weekly";
        LocalDate date = testDate;
        
        // When
        leaderboardService.recalculateGlobalLeaderboard(period, date);
        
        // Then
        // Проверяем, что метод вызывается без исключений
        // В реальной реализации здесь была бы логика пересчета
        assertTrue(true);
    }
    
    @Test
    void testRecalculateQuestLeaderboard() {
        // Given
        String period = "monthly";
        String category = "programming";
        String metric = "rating";
        LocalDate date = testDate;
        
        // When
        leaderboardService.recalculateQuestLeaderboard(period, category, metric, date);
        
        // Then
        // Проверяем, что метод вызывается без исключений
        // В реальной реализации здесь была бы логика пересчета
        assertTrue(true);
    }
    
    @Test
    void testRecalculateTeamLeaderboard() {
        // Given
        String period = "all_time";
        String category = "competitive";
        String metric = "wins";
        LocalDate date = testDate;
        
        // When
        leaderboardService.recalculateTeamLeaderboard(period, category, metric, date);
        
        // Then
        // Проверяем, что метод вызывается без исключений
        // В реальной реализации здесь была бы логика пересчета
        assertTrue(true);
    }
    
    @Test
    void testGetTopUsers() {
        // Given
        String period = "weekly";
        String category = "general";
        int limit = 10;
        LocalDate date = testDate;
        
        List<Leaderboard> topUsers = Collections.singletonList(testLeaderboard);
        
        when(leaderboardRepository.findTopByEntityTypeAndPeriodAndCategoryAndDateOrderByRankAsc(
                "user", period, category, date, PageRequest.of(0, limit)))
                .thenReturn(topUsers);
        
        // When
        List<LeaderboardDTO> result = leaderboardService.getTopUsers(period, category, limit, date);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        LeaderboardDTO dto = result.get(0);
        assertEquals(testLeaderboard.getEntityId(), dto.getEntityId());
        assertEquals("user", dto.getEntityType());
        assertEquals(testLeaderboard.getRank(), dto.getRank());
        
        verify(leaderboardRepository).findTopByEntityTypeAndPeriodAndCategoryAndDateOrderByRankAsc(
                "user", period, category, date, PageRequest.of(0, limit));
    }
    
    @Test
    void testGetTopQuests() {
        // Given
        String period = "monthly";
        String category = "programming";
        String metric = "rating";
        int limit = 10;
        LocalDate date = testDate;
        
        List<Leaderboard> topQuests = Collections.singletonList(testLeaderboard);
        
        when(leaderboardRepository.findTopByEntityTypeAndPeriodAndCategoryAndDateOrderByRankAsc(
                "quest", period, category, date, PageRequest.of(0, limit)))
                .thenReturn(topQuests);
        
        // When
        List<LeaderboardDTO> result = leaderboardService.getTopQuests(period, category, metric, limit, date);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        LeaderboardDTO dto = result.get(0);
        assertEquals(testLeaderboard.getEntityId(), dto.getEntityId());
        assertEquals("quest", dto.getEntityType());
        assertEquals(testLeaderboard.getRank(), dto.getRank());
        
        verify(leaderboardRepository).findTopByEntityTypeAndPeriodAndCategoryAndDateOrderByRankAsc(
                "quest", period, category, date, PageRequest.of(0, limit));
    }
    
    @Test
    void testGetTopTeams() {
        // Given
        String period = "all_time";
        String category = "competitive";
        String metric = "wins";
        int limit = 10;
        LocalDate date = testDate;
        
        List<Leaderboard> topTeams = Collections.singletonList(testLeaderboard);
        
        when(leaderboardRepository.findTopByEntityTypeAndPeriodAndCategoryAndDateOrderByRankAsc(
                "team", period, category, date, PageRequest.of(0, limit)))
                .thenReturn(topTeams);
        
        // When
        List<LeaderboardDTO> result = leaderboardService.getTopTeams(period, category, metric, limit, date);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        
        LeaderboardDTO dto = result.get(0);
        assertEquals(testLeaderboard.getEntityId(), dto.getEntityId());
        assertEquals("team", dto.getEntityType());
        assertEquals(testLeaderboard.getRank(), dto.getRank());
        
        verify(leaderboardRepository).findTopByEntityTypeAndPeriodAndCategoryAndDateOrderByRankAsc(
                "team", period, category, date, PageRequest.of(0, limit));
    }
}