package dn.quest.questmanagement.integration;

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
 * Интеграционные тесты для Quest Management Service
 */
class QuestManagementIntegrationTest extends AbstractIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    protected void cleanupTestData() {
        // Очистка тестовых данных для квестов
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testCreateQuest_Success() throws Exception {
        // Given
        var questRequest = EnhancedTestDataFactory.createQuestRequestDTO("Test Quest");

        // When & Then
        mockMvc.perform(post("/api/quests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Quest"))
                .andExpect(jsonPath("$.description").value("Test quest description for Test Quest"))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.questType").value("SOLO"))
                .andExpect(jsonPath("$.published").value(false))
                .andExpect(jsonPath("$.levels").isArray())
                .andExpect(jsonPath("$.levels[0].title").value("Level 1"));

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testCreateComplexQuest_Success() throws Exception {
        // Given
        var questRequest = EnhancedTestDataFactory.createComplexQuestRequestDTO("Complex Test Quest");

        // When & Then
        mockMvc.perform(post("/api/quests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Complex Test Quest"))
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"))
                .andExpect(jsonPath("$.questType").value("TEAM"))
                .andExpect(jsonPath("$.estimatedDuration").value(60))
                .andExpect(jsonPath("$.maxParticipants").value(4))
                .andExpect(jsonPath("$.levels").isArray())
                .andExpect(jsonPath("$.levels").hasJsonPath(2)); // Два уровня
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testCreateQuest_InvalidData_ReturnsBadRequest() throws Exception {
        // Given
        var invalidQuest = new java.util.HashMap<String, Object>();
        invalidQuest.put("title", ""); // Пустой заголовок
        invalidQuest.put("description", "Description");
        invalidQuest.put("difficulty", "INVALID"); // Невалидная сложность

        // When & Then
        mockMvc.perform(post("/api/quests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidQuest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetQuest_Success() throws Exception {
        // Given - Сначала создаем квест
        var questRequest = EnhancedTestDataFactory.createQuestRequestDTO("Test Quest for Get");
        
        String createResponse = mockMvc.perform(post("/api/quests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long questId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Получаем квест
        mockMvc.perform(get("/api/quests/" + questId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(questId))
                .andExpect(jsonPath("$.title").value("Test Quest for Get"))
                .andExpect(jsonPath("$.levels").isArray());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetQuest_NotFound_ReturnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/quests/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Quest not found"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUpdateQuest_Success() throws Exception {
        // Given - Сначала создаем квест
        var questRequest = EnhancedTestDataFactory.createQuestRequestDTO("Original Quest");
        
        String createResponse = mockMvc.perform(post("/api/quests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long questId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Обновляем квест
        var updateRequest = new java.util.HashMap<String, Object>();
        updateRequest.put("title", "Updated Quest");
        updateRequest.put("description", "Updated description");
        updateRequest.put("difficulty", "MEDIUM");

        mockMvc.perform(put("/api/quests/" + questId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(questId))
                .andExpect(jsonPath("$.title").value("Updated Quest"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testPublishQuest_Success() throws Exception {
        // Given - Сначала создаем квест
        var questRequest = EnhancedTestDataFactory.createQuestRequestDTO("Quest to Publish");
        
        String createResponse = mockMvc.perform(post("/api/quests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long questId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Публикуем квест
        mockMvc.perform(post("/api/quests/" + questId + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(questId))
                .andExpect(jsonPath("$.published").value(true));

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUnpublishQuest_Success() throws Exception {
        // Given - Сначала создаем и публикуем квест
        var questRequest = EnhancedTestDataFactory.createQuestRequestDTO("Quest to Unpublish");
        
        String createResponse = mockMvc.perform(post("/api/quests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long questId = objectMapper.readTree(createResponse).get("id").asLong();

        // Публикуем квест
        mockMvc.perform(post("/api/quests/" + questId + "/publish"))
                .andExpect(status().isOk());

        // When & Then - Снимаем с публикации
        mockMvc.perform(post("/api/quests/" + questId + "/unpublish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(questId))
                .andExpect(jsonPath("$.published").value(false));
    }

    @Test
    void testGetPublishedQuests_Success() throws Exception {
        // Given - Создаем несколько квестов
        for (int i = 0; i < 3; i++) {
            var questRequest = EnhancedTestDataFactory.createQuestRequestDTO("Public Quest " + i);
            
            String createResponse = mockMvc.perform(post("/api/quests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long questId = objectMapper.readTree(createResponse).get("id").asLong();
            
            // Публикуем квест
            mockMvc.perform(post("/api/quests/" + questId + "/publish"));
        }

        // When & Then - Получаем опубликованные квесты
        mockMvc.perform(get("/api/quests")
                        .param("published", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[?(@.published == true)]").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetMyQuests_Success() throws Exception {
        // Given - Создаем несколько квестов
        for (int i = 0; i < 2; i++) {
            var questRequest = EnhancedTestDataFactory.createQuestRequestDTO("My Quest " + i);
            
            mockMvc.perform(post("/api/quests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Получаем квесты текущего пользователя
        mockMvc.perform(get("/api/quests/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testDeleteQuest_Success() throws Exception {
        // Given - Сначала создаем квест
        var questRequest = EnhancedTestDataFactory.createQuestRequestDTO("Quest to Delete");
        
        String createResponse = mockMvc.perform(post("/api/quests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long questId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Удаляем квест
        mockMvc.perform(delete("/api/quests/" + questId))
                .andExpect(status().isNoContent());

        // Проверяем, что квест удален
        mockMvc.perform(get("/api/quests/" + questId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testSearchQuests_Success() throws Exception {
        // Given - Создаем квесты с разными названиями
        List<String> questTitles = List.of("Adventure Quest", "Puzzle Quest", "Adventure Puzzle");
        
        for (String title : questTitles) {
            var questRequest = EnhancedTestDataFactory.createQuestRequestDTO(title);
            
            String createResponse = mockMvc.perform(post("/api/quests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long questId = objectMapper.readTree(createResponse).get("id").asLong();
            
            // Публикуем квест
            mockMvc.perform(post("/api/quests/" + questId + "/publish"));
        }

        // When & Then - Ищем квесты по ключевому слову
        mockMvc.perform(get("/api/quests/search")
                        .param("keyword", "Adventure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(2)) // Два квеста с "Adventure"
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetQuestStatistics_Success() throws Exception {
        // Given - Создаем квест
        var questRequest = EnhancedTestDataFactory.createQuestRequestDTO("Quest for Stats");
        
        String createResponse = mockMvc.perform(post("/api/quests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long questId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Получаем статистику квеста
        mockMvc.perform(get("/api/quests/" + questId + "/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questId").value(questId))
                .andExpect(jsonPath("$.totalSessions").exists())
                .andExpect(jsonPath("$.completedSessions").exists())
                .andExpect(jsonPath("$.averageCompletionTime").exists())
                .andExpect(jsonPath("$.successRate").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllQuests_Admin_Success() throws Exception {
        // Given - Создаем несколько квестов
        for (int i = 0; i < 5; i++) {
            var questRequest = EnhancedTestDataFactory.createQuestRequestDTO("Admin Quest " + i);
            
            mockMvc.perform(post("/api/quests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Администратор получает все квесты
        mockMvc.perform(get("/api/quests/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(5))
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetAllQuests_Player_Forbidden() throws Exception {
        // When & Then - Обычный пользователь не может получить все квесты
        mockMvc.perform(get("/api/quests/admin/all"))
                .andExpect(status().isForbidden());
    }
}