package dn.quest.statistics.integration;

import dn.quest.statistics.dto.StatisticsRequestDTO;
import dn.quest.statistics.dto.UserStatisticsDTO;
import dn.quest.statistics.entity.UserStatistics;
import dn.quest.statistics.repository.UserStatisticsRepository;
import dn.quest.statistics.service.StatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для Statistics Service
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class StatisticsServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private UserStatisticsRepository userStatisticsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetPlatformOverview() throws Exception {
        mockMvc.perform(get("/api/stats/overview")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetUserStatistics() throws Exception {
        // Создаем тестовые данные
        UserStatistics userStats = UserStatistics.builder()
                .userId(1L)
                .date(LocalDate.now())
                .gameSessions(5)
                .completedQuests(3)
                .totalGameTimeMinutes(120L)
                .build();
        userStatisticsRepository.save(userStats);

        mockMvc.perform(get("/api/stats/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.gameSessions").value(5))
                .andExpect(jsonPath("$.completedQuests").value(3));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetUserStatisticsForPeriod() throws Exception {
        // Создаем тестовые данные
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        UserStatistics stats1 = UserStatistics.builder()
                .userId(1L)
                .date(yesterday)
                .gameSessions(3)
                .completedQuests(2)
                .build();
        userStatisticsRepository.save(stats1);

        UserStatistics stats2 = UserStatistics.builder()
                .userId(1L)
                .date(today)
                .gameSessions(5)
                .completedQuests(3)
                .build();
        userStatisticsRepository.save(stats2);

        mockMvc.perform(get("/api/stats/users/1/period")
                        .param("startDate", yesterday.toString())
                        .param("endDate", today.toString())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetQuestStatistics() throws Exception {
        mockMvc.perform(get("/api/stats/quests/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetTeamStatistics() throws Exception {
        mockMvc.perform(get("/api/stats/teams/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetCustomStatistics() throws Exception {
        StatisticsRequestDTO request = StatisticsRequestDTO.builder()
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .statisticsType("users")
                .aggregationPeriod("daily")
                .build();

        mockMvc.perform(post("/api/stats/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetTopUsers() throws Exception {
        mockMvc.perform(get("/api/stats/top/users")
                        .param("metric", "completedQuests")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetTopQuests() throws Exception {
        mockMvc.perform(get("/api/stats/top/quests")
                        .param("metric", "completions")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetStatisticsByCategories() throws Exception {
        mockMvc.perform(get("/api/stats/categories")
                        .param("entityType", "quests")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetMetricTrends() throws Exception {
        mockMvc.perform(get("/api/stats/trends")
                        .param("metrics", "registrations,activeUsers")
                        .param("period", "days")
                        .param("periods", "30"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetSystemStatistics() throws Exception {
        mockMvc.perform(get("/api/stats/system")
                        .param("category", "performance"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetGlobalLeaderboard() throws Exception {
        mockMvc.perform(get("/api/stats/leaderboard/global")
                        .param("period", "all_time")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetQuestLeaderboard() throws Exception {
        mockMvc.perform(get("/api/stats/leaderboard/quests")
                        .param("period", "all_time")
                        .param("metric", "rating")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetTeamLeaderboard() throws Exception {
        mockMvc.perform(get("/api/stats/leaderboard/teams")
                        .param("period", "all_time")
                        .param("metric", "rating")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetUserLeaderboardPosition() throws Exception {
        mockMvc.perform(get("/api/stats/leaderboard/users/1/position")
                        .param("period", "all_time"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetUserSurroundingInLeaderboard() throws Exception {
        mockMvc.perform(get("/api/stats/leaderboard/users/1/surrounding")
                        .param("period", "all_time")
                        .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldGetLeaderboardCategories() throws Exception {
        mockMvc.perform(get("/api/stats/leaderboard/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetAnalyticsReport() throws Exception {
        StatisticsRequestDTO request = StatisticsRequestDTO.builder()
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .statisticsType("users")
                .build();

        mockMvc.perform(post("/api/stats/analytics/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reportId").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetUserEngagementReport() throws Exception {
        mockMvc.perform(get("/api/stats/analytics/engagement")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31")
                        .param("groupBy", "day"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetQuestPerformanceReport() throws Exception {
        mockMvc.perform(get("/api/stats/analytics/quests/performance")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldExportUserStatistics() throws Exception {
        mockMvc.perform(get("/api/stats/reports/export/users")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31")
                        .param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", "text/csv"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetAvailableReportTemplates() throws Exception {
        mockMvc.perform(get("/api/stats/reports/templates"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturnUnauthorizedForProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/stats/overview"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/stats/users/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/stats/analytics/engagement")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldReturnForbiddenForAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/stats/overview"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/stats/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/stats/analytics/engagement")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testStatisticsServiceDirectly() {
        // Прямое тестирование сервиса
        LocalDate today = LocalDate.now();
        
        // Создаем тестовые данные
        UserStatistics userStats = UserStatistics.builder()
                .userId(1L)
                .date(today)
                .gameSessions(5)
                .completedQuests(3)
                .totalGameTimeMinutes(120L)
                .build();
        userStatisticsRepository.save(userStats);

        // Проверяем получение статистики через сервис
        UserStatisticsDTO result = statisticsService.getUserStatistics(1L, today);
        
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getGameSessions()).isEqualTo(5);
        assertThat(result.getCompletedQuests()).isEqualTo(3);
        assertThat(result.getTotalGameTimeMinutes()).isEqualTo(120L);
    }

    @Test
    void testCustomStatisticsRequest() {
        StatisticsRequestDTO request = StatisticsRequestDTO.builder()
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .statisticsType("users")
                .aggregationPeriod("daily")
                .metrics(new String[]{"gameSessions", "completedQuests"})
                .build();

        Map<String, Object> result = statisticsService.getCustomStatistics(request);
        
        assertThat(result).isNotNull();
        assertThat(result).containsKey("statistics");
        assertThat(result).containsKey("metadata");
    }
}