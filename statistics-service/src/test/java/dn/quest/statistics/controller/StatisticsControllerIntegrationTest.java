package dn.quest.statistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.statistics.dto.StatisticsRequestDTO;
import dn.quest.statistics.entity.UserStatistics;
import dn.quest.statistics.repository.UserStatisticsRepository;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для StatisticsController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class StatisticsControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private UserStatisticsRepository userStatisticsRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MockMvc mockMvc;
    
    private UserStatistics testUserStatistics;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        
        // Создаем тестовые данные
        LocalDate testDate = LocalDate.now();
        testUserStatistics = UserStatistics.builder()
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
        
        userStatisticsRepository.save(testUserStatistics);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetPlatformOverview() throws Exception {
        mockMvc.perform(get("/api/stats/overview")
                .param("startDate", LocalDate.now().minusDays(7).toString())
                .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalUsers", isA(Number.class)))
                .andExpect(jsonPath("$.totalQuests", isA(Number.class)))
                .andExpect(jsonPath("$.totalSessions", isA(Number.class)))
                .andExpect(jsonPath("$.totalTeams", isA(Number.class)))
                .andExpect(jsonPath("$.trends", isA(Map.class)));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void testGetUserStatistics() throws Exception {
        mockMvc.perform(get("/api/stats/users/{userId}", 100L)
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is(100)))
                .andExpect(jsonPath("$.logins", is(5)))
                .andExpect(jsonPath("$.gameSessions", is(3)))
                .andExpect(jsonPath("$.completedQuests", is(2)))
                .andExpect(jsonPath("$.createdQuests", is(1)));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void testGetUserStatisticsNotFound() throws Exception {
        mockMvc.perform(get("/api/stats/users/{userId}", 999L)
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is(999)))
                .andExpect(jsonPath("$.logins", is(0)))
                .andExpect(jsonPath("$.gameSessions", is(0)))
                .andExpect(jsonPath("$.completedQuests", is(0)));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void testGetUserStatisticsForPeriod() throws Exception {
        mockMvc.perform(get("/api/stats/users/{userId}/period", 100L)
                .param("startDate", LocalDate.now().minusDays(7).toString())
                .param("endDate", LocalDate.now().toString())
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", isA(List.class)))
                .andExpect(jsonPath("$.totalElements", isA(Number.class)))
                .andExpect(jsonPath("$.size", is(20)))
                .andExpect(jsonPath("$.number", is(0)));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void testGetQuestStatistics() throws Exception {
        mockMvc.perform(get("/api/stats/quests/{questId}", 200L)
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.questId", is(200)))
                .andExpect(jsonPath("$.views", is(0)))
                .andExpect(jsonPath("$.starts", is(0)))
                .andExpect(jsonPath("$.completions", is(0)))
                .andExpect(jsonPath("$.completionRate", is(0.0)));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void testGetTeamStatistics() throws Exception {
        mockMvc.perform(get("/api/stats/teams/{teamId}", 300L)
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.teamId", is(300)))
                .andExpect(jsonPath("$.activeMembers", is(0)))
                .andExpect(jsonPath("$.totalMembers", is(0)))
                .andExpect(jsonPath("$.gameSessions", is(0)))
                .andExpect(jsonPath("$.activeRate", is(0.0)));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetCustomStatistics() throws Exception {
        StatisticsRequestDTO request = StatisticsRequestDTO.builder()
                .statisticsType("user")
                .userId(100L)
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now())
                .build();
        
        mockMvc.perform(post("/api/stats/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userStatistics", isA(Map.class)))
                .andExpect(jsonPath("$.request", isA(Map.class)));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void testGetTopUsers() throws Exception {
        mockMvc.perform(get("/api/stats/top/users")
                .param("metric", "completedQuests")
                .param("limit", "10")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$[0].userId", isA(Number.class)))
                .andExpect(jsonPath("$[0].completedQuests", isA(Number.class)));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void testGetTopQuests() throws Exception {
        mockMvc.perform(get("/api/stats/top/quests")
                .param("metric", "completions")
                .param("limit", "10")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$[0].questId", isA(Number.class)))
                .andExpect(jsonPath("$[0].completions", isA(Number.class)));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void testGetStatisticsByCategories() throws Exception {
        mockMvc.perform(get("/api/stats/categories")
                .param("entityType", "quest")
                .param("startDate", LocalDate.now().minusDays(7).toString())
                .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.entityType", is("quest")))
                .andExpect(jsonPath("$.categories", isA(List.class)))
                .andExpect(jsonPath("$.period", isA(Map.class)));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetMetricTrends() throws Exception {
        mockMvc.perform(get("/api/stats/trends")
                .param("metrics", "users,quests")
                .param("period", "days")
                .param("periods", "7"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.period", is("days")))
                .andExpect(jsonPath("$.periods", is(7)))
                .andExpect(jsonPath("$.metrics", isA(Map.class)))
                .andExpect(jsonPath("$.metrics.users", isA(List.class)))
                .andExpect(jsonPath("$.metrics.quests", isA(List.class)));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetSystemStatistics() throws Exception {
        mockMvc.perform(get("/api/stats/system")
                .param("category", "performance")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.category", is("performance")))
                .andExpect(jsonPath("$.date", isA(String.class)))
                .andExpect(jsonPath("$.totalRequests", is(0)))
                .andExpect(jsonPath("$.successfulRequests", is(0)))
                .andExpect(jsonPath("$.failedRequests", is(0)))
                .andExpect(jsonPath("$.averageResponseTimeMs", is(0.0)))
                .andExpect(jsonPath("$.cpuUsagePercent", is(0.0)))
                .andExpect(jsonPath("$.memoryUsagePercent", is(0.0)))
                .andExpect(jsonPath("$.diskUsagePercent", is(0.0)))
                .andExpect(jsonPath("$.successRate", is(0.0)));
    }
    
    @Test
    void testGetPlatformOverviewUnauthorized() throws Exception {
        mockMvc.perform(get("/api/stats/overview")
                .param("startDate", LocalDate.now().minusDays(7).toString())
                .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void testGetPlatformOverviewForbidden() throws Exception {
        mockMvc.perform(get("/api/stats/overview")
                .param("startDate", LocalDate.now().minusDays(7).toString())
                .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetCustomStatisticsInvalidRequest() throws Exception {
        StatisticsRequestDTO request = StatisticsRequestDTO.builder()
                .statisticsType("invalid_type")
                .build();
        
        mockMvc.perform(post("/api/stats/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", containsString("Unknown statistics type")));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void testGetTopUsersInvalidMetric() throws Exception {
        mockMvc.perform(get("/api/stats/top/users")
                .param("metric", "invalid_metric")
                .param("limit", "10")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(List.class)));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void testGetStatisticsByCategoriesInvalidEntityType() throws Exception {
        mockMvc.perform(get("/api/stats/categories")
                .param("entityType", "invalid_type")
                .param("startDate", LocalDate.now().minusDays(7).toString())
                .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", containsString("Unknown entity type")));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetMetricTrendsDefaultParameters() throws Exception {
        mockMvc.perform(get("/api/stats/trends"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.period", is("days")))
                .andExpect(jsonPath("$.periods", is(30)))
                .andExpect(jsonPath("$.metrics", isA(Map.class)));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetSystemStatisticsDefaultParameters() throws Exception {
        mockMvc.perform(get("/api/stats/system"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.category", is(nullValue())))
                .andExpect(jsonPath("$.date", isA(String.class)))
                .andExpect(jsonPath("$.totalRequests", is(0)))
                .andExpect(jsonPath("$.successRate", is(0.0)));
    }
}