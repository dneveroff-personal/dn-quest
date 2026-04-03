package dn.quest.gameengine.integration;

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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для Game Engine Service
 */
class GameEngineIntegrationTest extends AbstractIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    protected void cleanupTestData() {
        // Очистка тестовых данных для игровых сессий
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testStartGameSession_Success() throws Exception {
        // Given - Сначала создаем квест (предполагаем, что он существует)
        Long questId = 1L;
        var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(questId);

        // When & Then
        mockMvc.perform(post("/api/game/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questId").value(questId))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.currentLevel").exists())
                .andExpect(jsonPath("$.currentLevel.orderIndex").value(1))
                .andExpect(jsonPath("$.progress").exists())
                .andExpect(jsonPath("$.progress.sectorsClosed").value(0))
                .andExpect(jsonPath("$.sessionId").exists());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testStartGameSession_QuestNotFound_ReturnsNotFound() throws Exception {
        // Given
        Long nonExistentQuestId = 99999L;
        var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(nonExistentQuestId);

        // When & Then
        mockMvc.perform(post("/api/game/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Quest not found"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetCurrentLevel_Success() throws Exception {
        // Given - Сначала создаем игровую сессию
        Long questId = 1L;
        var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(questId);

        String sessionResponse = mockMvc.perform(post("/api/game/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionResponse).get("sessionId").asText();

        // When & Then - Получаем текущий уровень
        mockMvc.perform(get("/api/game/sessions/" + sessionId + "/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").exists())
                .andExpect(jsonPath("$.level.orderIndex").value(1))
                .andExpect(jsonPath("$.level.title").exists())
                .andExpect(jsonPath("$.level.description").exists())
                .andExpect(jsonPath("$.progress").exists())
                .andExpect(jsonPath("$.progress.sectorsClosed").value(0))
                .andExpect(jsonPath("$.progress.codesFound").isArray());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testSubmitCode_Success() throws Exception {
        // Given - Сначала создаем игровую сессию
        Long questId = 1L;
        UUID userId = 1L;
        var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(questId);

        String sessionResponse = mockMvc.perform(post("/api/game/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionResponse).get("sessionId").asText();

        // When & Then - Отправляем правильный код
        var codeRequest = EnhancedTestDataFactory.createCodeSubmissionDTO("TEST123", userId);

        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ACCEPTED_NORMAL"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.pointsEarned").value(100))
                .andExpect(jsonPath("$.progress").exists());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testSubmitCode_WrongCode_ReturnsWrongCode() throws Exception {
        // Given - Сначала создаем игровую сессию
        Long questId = 1L;
        UUID userId = 1L;
        var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(questId);

        String sessionResponse = mockMvc.perform(post("/api/game/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionResponse).get("sessionId").asText();

        // When & Then - Отправляем неправильный код
        var codeRequest = EnhancedTestDataFactory.createCodeSubmissionDTO("WRONG_CODE", userId);

        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("WRONG_CODE"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.pointsEarned").value(0));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testSubmitCode_DuplicateCode_ReturnsAlreadyFound() throws Exception {
        // Given - Сначала создаем игровую сессию и отправляем правильный код
        Long questId = 1L;
        UUID userId = 1L;
        var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(questId);

        String sessionResponse = mockMvc.perform(post("/api/game/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionResponse).get("sessionId").asText();

        var codeRequest = EnhancedTestDataFactory.createCodeSubmissionDTO("TEST123", userId);

        // Отправляем код первый раз
        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ACCEPTED_NORMAL"));

        // When & Then - Отправляем тот же код второй раз
        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ALREADY_FOUND"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.pointsEarned").value(0));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetHint_Success() throws Exception {
        // Given - Сначала создаем игровую сессию
        Long questId = 1L;
        var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(questId);

        String sessionResponse = mockMvc.perform(post("/api/game/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionResponse).get("sessionId").asText();

        // When & Then - Получаем подсказку
        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/hint"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hint").exists())
                .andExpect(jsonPath("$.hintText").exists())
                .andExpect(jsonPath("$.pointsPenalty").value(50))
                .andExpect(jsonPath("$.hintsUsed").value(1));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testFinishGameSession_Success() throws Exception {
        // Given - Сначала создаем игровую сессию и завершаем все уровни
        Long questId = 1L;
        UUID userId = 1L;
        var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(questId);

        String sessionResponse = mockMvc.perform(post("/api/game/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionResponse).get("sessionId").asText();

        // Отправляем правильный код для завершения уровня
        var codeRequest = EnhancedTestDataFactory.createCodeSubmissionDTO("TEST123", userId);
        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codeRequest)))
                .andExpect(status().isOk());

        // When & Then - Завершаем сессию
        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists())
                .andExpect(jsonPath("$.totalPoints").exists())
                .andExpect(jsonPath("$.timeSpent").exists())
                .andExpect(jsonPath("$.finalScore").exists());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testPauseGameSession_Success() throws Exception {
        // Given - Сначала создаем игровую сессию
        Long questId = 1L;
        var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(questId);

        String sessionResponse = mockMvc.perform(post("/api/game/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionResponse).get("sessionId").asText();

        // When & Then - Приостанавливаем сессию
        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/pause"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAUSED"))
                .andExpect(jsonPath("$.pausedAt").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testResumeGameSession_Success() throws Exception {
        // Given - Сначала создаем и приостанавливаем игровую сессию
        Long questId = 1L;
        var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(questId);

        String sessionResponse = mockMvc.perform(post("/api/game/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionResponse).get("sessionId").asText();

        // Приостанавливаем сессию
        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/pause"))
                .andExpect(status().isOk());

        // When & Then - Возобновляем сессию
        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.resumedAt").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetGameSession_Success() throws Exception {
        // Given - Сначала создаем игровую сессию
        Long questId = 1L;
        var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(questId);

        String sessionResponse = mockMvc.perform(post("/api/game/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String sessionId = objectMapper.readTree(sessionResponse).get("sessionId").asText();

        // When & Then - Получаем информацию о сессии
        mockMvc.perform(get("/api/game/sessions/" + sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.questId").value(questId))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.currentLevel").exists())
                .andExpect(jsonPath("$.progress").exists())
                .andExpect(jsonPath("$.statistics").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetMyActiveSessions_Success() throws Exception {
        // Given - Создаем несколько игровых сессий
        for (int i = 0; i < 3; i++) {
            var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(1L);
            mockMvc.perform(post("/api/game/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sessionRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Получаем активные сессии пользователя
        mockMvc.perform(get("/api/game/sessions/my/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(3))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetSessionHistory_Success() throws Exception {
        // Given - Создаем и завершаем несколько игровых сессий
        for (int i = 0; i < 2; i++) {
            var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(1L);
            
            String sessionResponse = mockMvc.perform(post("/api/game/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sessionRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String sessionId = objectMapper.readTree(sessionResponse).get("sessionId").asText();
            
            // Завершаем сессию
            mockMvc.perform(post("/api/game/sessions/" + sessionId + "/finish"));
        }

        // When & Then - Получаем историю сессий
        mockMvc.perform(get("/api/game/sessions/my/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[?(@.status == 'COMPLETED')]").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetLeaderboard_Success() throws Exception {
        // Given - Создаем и завершаем несколько игровых сессий для формирования лидерборда
        for (int i = 0; i < 5; i++) {
            var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(1L);
            
            String sessionResponse = mockMvc.perform(post("/api/game/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sessionRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String sessionId = objectMapper.readTree(sessionResponse).get("sessionId").asText();
            
            // Завершаем сессию
            mockMvc.perform(post("/api/game/sessions/" + sessionId + "/finish"));
        }

        // When & Then - Получаем лидерборд
        mockMvc.perform(get("/api/game/leaderboard")
                        .param("questId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(5))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.content[0].rank").value(1))
                .andExpect(jsonPath("$.content[0].score").exists())
                .andExpect(jsonPath("$.content[0].completionTime").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllSessions_Admin_Success() throws Exception {
        // Given - Создаем несколько игровых сессий
        for (int i = 0; i < 3; i++) {
            var sessionRequest = EnhancedTestDataFactory.createGameSessionRequestDTO(1L);
            mockMvc.perform(post("/api/game/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sessionRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Администратор получает все сессии
        mockMvc.perform(get("/api/game/sessions/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(3))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetAllSessions_Player_Forbidden() throws Exception {
        // When & Then - Обычный пользователь не может получить все сессии
        mockMvc.perform(get("/api/game/sessions/admin/all"))
                .andExpect(status().isForbidden());
    }
}