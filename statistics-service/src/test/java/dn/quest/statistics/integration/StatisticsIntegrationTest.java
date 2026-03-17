package dn.quest.statistics.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.base.AbstractIntegrationTestBase;
import dn.quest.shared.util.EnhancedTestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для Statistics Service
 */
class StatisticsIntegrationTest extends AbstractIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    protected void cleanupTestData() {
        // Очистка тестовых данных для статистики
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetUserStatistics_Success() throws Exception {
        // Given - Предполагаем, что у пользователя есть статистика
        Long userId = 1L;

        // When & Then
        mockMvc.perform(get("/api/statistics/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalQuests").exists())
                .andExpect(jsonPath("$.completedQuests").exists())
                .andExpect(jsonPath("$.totalScore").exists())
                .andExpect(jsonPath("$.averageScore").exists())
                .andExpect(jsonPath("$.totalPlayTime").exists())
                .andExpect(jsonPath("$.level").exists())
                .andExpect(jsonPath("$.experience").exists())
                .andExpect(jsonPath("$.achievements").isArray())
                .andExpect(jsonPath("$.rank").exists())
                .andExpect(jsonPath("$.lastUpdated").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetUserStatistics_NotFound_ReturnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/user/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User statistics not found"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUpdateUserStatistics_Success() throws Exception {
        // Given
        Long userId = 1L;
        var updateRequest = new java.util.HashMap<String, Object>();
        updateRequest.put("questCompleted", true);
        updateRequest.put("score", 150);
        updateRequest.put("playTime", 3600); // в секундах
        updateRequest.put("experienceGained", 100);

        // When & Then
        mockMvc.perform(put("/api/statistics/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalQuests").exists())
                .andExpect(jsonPath("$.completedQuests").exists())
                .andExpect(jsonPath("$.totalScore").exists())
                .andExpect(jsonPath("$.totalPlayTime").exists())
                .andExpect(jsonPath("$.experience").exists())
                .andExpect(jsonPath("$.lastUpdated").exists());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetQuestStatistics_Success() throws Exception {
        // Given
        Long questId = 1L;

        // When & Then
        mockMvc.perform(get("/api/statistics/quest/" + questId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questId").value(questId))
                .andExpect(jsonPath("$.totalAttempts").exists())
                .andExpect(jsonPath("$.successfulAttempts").exists())
                .andExpect(jsonPath("$.averageCompletionTime").exists())
                .andExpect(jsonPath("$.averageScore").exists())
                .andExpect(jsonPath("$.difficulty").exists())
                .andExpect(jsonPath("$.popularity").exists())
                .andExpect(jsonPath("$.completionRate").exists())
                .andExpect(jsonPath("$.lastUpdated").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUpdateQuestStatistics_Success() throws Exception {
        // Given
        Long questId = 1L;
        var updateRequest = new java.util.HashMap<String, Object>();
        updateRequest.put("attemptCompleted", true);
        updateRequest.put("completionTime", 1800); // в секундах
        updateRequest.put("score", 85);
        updateRequest.put("hintsUsed", 2);

        // When & Then
        mockMvc.perform(put("/api/statistics/quest/" + questId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questId").value(questId))
                .andExpect(jsonPath("$.totalAttempts").exists())
                .andExpect(jsonPath("$.successfulAttempts").exists())
                .andExpect(jsonPath("$.averageCompletionTime").exists())
                .andExpect(jsonPath("$.averageScore").exists())
                .andExpect(jsonPath("$.lastUpdated").exists());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetTeamStatistics_Success() throws Exception {
        // Given
        Long teamId = 1L;

        // When & Then
        mockMvc.perform(get("/api/statistics/team/" + teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(teamId))
                .andExpect(jsonPath("$.totalMembers").exists())
                .andExpect(jsonPath("$.activeMembers").exists())
                .andExpect(jsonPath("$.totalQuests").exists())
                .andExpect(jsonPath("$.completedQuests").exists())
                .andExpect(jsonPath("$.totalScore").exists())
                .andExpect(jsonPath("$.averageScore").exists())
                .andExpect(jsonPath("$.teamRank").exists())
                .andExpect(jsonPath("$.lastUpdated").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUpdateTeamStatistics_Success() throws Exception {
        // Given
        Long teamId = 1L;
        var updateRequest = new java.util.HashMap<String, Object>();
        updateRequest.put("questCompleted", true);
        updateRequest.put("score", 200);
        updateRequest.put("memberContributions", Map.of(1L, 100, 2L, 100));

        // When & Then
        mockMvc.perform(put("/api/statistics/team/" + teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(teamId))
                .andExpect(jsonPath("$.totalQuests").exists())
                .andExpect(jsonPath("$.completedQuests").exists())
                .andExpect(jsonPath("$.totalScore").exists())
                .andExpect(jsonPath("$.lastUpdated").exists());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetLeaderboard_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/leaderboard")
                        .param("type", "GLOBAL")
                        .param("period", "WEEKLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaderboardType").value("GLOBAL"))
                .andExpect(jsonPath("$.period").value("WEEKLY"))
                .andExpect(jsonPath("$.entries").isArray())
                .andExpect(jsonPath("$.totalEntries").exists())
                .andExpect(jsonPath("$.currentPage").exists())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.lastUpdated").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetLeaderboard_InvalidType_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/leaderboard")
                        .param("type", "INVALID_TYPE")
                        .param("period", "WEEKLY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetUserRank_Success() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then
        mockMvc.perform(get("/api/statistics/user/" + userId + "/rank")
                        .param("type", "GLOBAL")
                        .param("period", "MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.rank").exists())
                .andExpect(jsonPath("$.totalUsers").exists())
                .andExpect(jsonPath("$.percentile").exists())
                .andExpect(jsonPath("$.leaderboardType").value("GLOBAL"))
                .andExpect(jsonPath("$.period").value("MONTHLY"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetAchievementStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAchievements").exists())
                .andExpect(jsonPath("$.unlockedAchievements").exists())
                .andExpect(jsonPath("$.lockedAchievements").exists())
                .andExpect(jsonPath("$.completionRate").exists())
                .andExpect(jsonPath("$.rarestAchievements").isArray())
                .andExpect(jsonPath("$.mostCommonAchievements").isArray())
                .andExpect(jsonPath("$.recentUnlocks").isArray());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUnlockAchievement_Success() throws Exception {
        // Given
        Long userId = 1L;
        var unlockRequest = new java.util.HashMap<String, Object>();
        unlockRequest.put("achievementId", "FIRST_QUEST_COMPLETED");
        unlockRequest.put("unlockedAt", LocalDateTime.now().toString());

        // When & Then
        mockMvc.perform(post("/api/statistics/user/" + userId + "/achievements/unlock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unlockRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Achievement unlocked successfully"))
                .andExpect(jsonPath("$.achievementId").value("FIRST_QUEST_COMPLETED"))
                .andExpect(jsonPath("$.unlockedAt").exists());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetGameSessionStatistics_Success() throws Exception {
        // Given
        Long sessionId = 1L;

        // When & Then
        mockMvc.perform(get("/api/statistics/session/" + sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.questId").exists())
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.endTime").exists())
                .andExpect(jsonPath("$.duration").exists())
                .andExpect(jsonPath("$.attempts").exists())
                .andExpect(jsonPath("$.hintsUsed").exists())
                .andExpect(jsonPath("$.finalScore").exists())
                .andExpect(jsonPath("$.completed").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testCreateGameSessionStatistics_Success() throws Exception {
        // Given
        var sessionRequest = new java.util.HashMap<String, Object>();
        sessionRequest.put("sessionId", 1L);
        sessionRequest.put("userId", 1L);
        sessionRequest.put("questId", 1L);
        sessionRequest.put("startTime", LocalDateTime.now().toString());

        // When & Then
        mockMvc.perform(post("/api/statistics/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.questId").value(1))
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.attempts").value(0))
                .andExpect(jsonPath("$.hintsUsed").value(0));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUpdateGameSessionStatistics_Success() throws Exception {
        // Given
        Long sessionId = 1L;
        var updateRequest = new java.util.HashMap<String, Object>();
        updateRequest.put("attemptCompleted", true);
        updateRequest.put("score", 95);
        updateRequest.put("hintsUsed", 1);
        updateRequest.put("endTime", LocalDateTime.now().toString());

        // When & Then
        mockMvc.perform(put("/api/statistics/session/" + sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.attempts").exists())
                .andExpect(jsonPath("$.hintsUsed").exists())
                .andExpect(jsonPath("$.finalScore").exists())
                .andExpect(jsonPath("$.endTime").exists())
                .andExpect(jsonPath("$.duration").exists());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetSystemStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/system"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").exists())
                .andExpect(jsonPath("$.activeUsers").exists())
                .andExpect(jsonPath("$.totalQuests").exists())
                .andExpect(jsonPath("$.publishedQuests").exists())
                .andExpect(jsonPath("$.totalTeams").exists())
                .andExpect(jsonPath("$.activeTeams").exists())
                .andExpect(jsonPath("$.totalGameSessions").exists())
                .andExpect(jsonPath("$.completedGameSessions").exists())
                .andExpect(jsonPath("$.averageSessionDuration").exists())
                .andExpect(jsonPath("$.totalAchievementsUnlocked").exists())
                .andExpect(jsonPath("$.lastUpdated").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetSystemStatistics_Player_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/system"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetUsageStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/usage")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-12-31")
                        .param("groupBy", "MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period.startDate").value("2023-01-01"))
                .andExpect(jsonPath("$.period.endDate").value("2023-12-31"))
                .andExpect(jsonPath("$.groupBy").value("MONTH"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.summary").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetUsageStatistics_Player_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/usage")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-12-31"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetUserProgress_Success() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then
        mockMvc.perform(get("/api/statistics/user/" + userId + "/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.currentLevel").exists())
                .andExpect(jsonPath("$.currentExperience").exists())
                .andExpect(jsonPath("$.experienceToNextLevel").exists())
                .andExpect(jsonPath("$.progressPercentage").exists())
                .andExpect(jsonPath("$.totalExperience").exists())
                .andExpect(jsonPath("$.levelHistory").isArray())
                .andExpect(jsonPath("$.recentAchievements").isArray());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetQuestDifficultyStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/quests/difficulty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.easy").exists())
                .andExpect(jsonPath("$.medium").exists())
                .andExpect(jsonPath("$.hard").exists())
                .andExpect(jsonPath("$.expert").exists())
                .andExpect(jsonPath("$.totalQuests").exists())
                .andExpect(jsonPath("$.averageDifficulty").exists())
                .andExpect(jsonPath("$.completionRatesByDifficulty").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetPopularQuests_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/quests/popular")
                        .param("period", "MONTHLY")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("MONTHLY"))
                .andExpect(jsonPath("$.quests").isArray())
                .andExpect(jsonPath("$.quests[0].questId").exists())
                .andExpect(jsonPath("$.quests[0].title").exists())
                .andExpect(jsonPath("$.quests[0].playCount").exists())
                .andExpect(jsonPath("$.quests[0].completionRate").exists())
                .andExpect(jsonPath("$.quests[0].averageScore").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetUserActivityHistory_Success() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then
        mockMvc.perform(get("/api/statistics/user/" + userId + "/activity")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-12-31")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.activities").isArray())
                .andExpect(jsonPath("$.activities[0].type").exists())
                .andExpect(jsonPath("$.activities[0].timestamp").exists())
                .andExpect(jsonPath("$.activities[0].details").exists())
                .andExpect(jsonPath("$.totalActivities").exists())
                .andExpect(jsonPath("$.currentPage").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testExportStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/export")
                        .param("type", "USER_STATS")
                        .param("format", "CSV")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-12-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"user_statistics.csv\""));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testExportStatistics_Player_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/export")
                        .param("type", "USER_STATS")
                        .param("format", "CSV"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetPerformanceMetrics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseTime").exists())
                .andExpect(jsonPath("$.throughput").exists())
                .andExpect(jsonPath("$.errorRate").exists())
                .andExpect(jsonPath("$.activeUsers").exists())
                .andExpect(jsonPath("$.databaseConnections").exists())
                .andExpect(jsonPath("$.memoryUsage").exists())
                .andExpect(jsonPath("$.cpuUsage").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetPerformanceMetrics_Player_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics/performance"))
                .andExpect(status().isForbidden());
    }
}