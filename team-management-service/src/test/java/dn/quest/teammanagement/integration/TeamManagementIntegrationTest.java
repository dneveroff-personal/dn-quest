package dn.quest.teammanagement.integration;

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
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для Team Management Service
 */
class TeamManagementIntegrationTest extends AbstractIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    protected void cleanupTestData() {
        // Очистка тестовых данных для команд
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testCreateTeam_Success() throws Exception {
        // Given
        var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("Test Team");

        // When & Then
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Team"))
                .andExpect(jsonPath("$.description").value("Test team description for Test Team"))
                .andExpect(jsonPath("$.maxMembers").value(5))
                .andExpect(jsonPath("$.memberCount").value(1))
                .andExpect(jsonPath("$.creatorId").exists())
                .andExpect(jsonPath("$.members").isArray())
                .andExpect(jsonPath("$.members[0].role").value("LEADER"));

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testCreateTeam_InvalidData_ReturnsBadRequest() throws Exception {
        // Given
        var invalidTeam = new java.util.HashMap<String, Object>();
        invalidTeam.put("name", ""); // Пустое имя
        invalidTeam.put("description", "Description");
        invalidTeam.put("maxMembers", 0); // Невалидное количество участников

        // When & Then
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTeam)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetTeam_Success() throws Exception {
        // Given - Сначала создаем команду
        var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("Team for Get");
        
        String createResponse = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long teamId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Получаем команду
        mockMvc.perform(get("/api/teams/" + teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teamId))
                .andExpect(jsonPath("$.name").value("Team for Get"))
                .andExpect(jsonPath("$.members").isArray())
                .andExpect(jsonPath("$.memberCount").value(1));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetTeam_NotFound_ReturnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/teams/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Team not found"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUpdateTeam_Success() throws Exception {
        // Given - Сначала создаем команду
        var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("Original Team");
        
        String createResponse = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long teamId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Обновляем команду
        var updateRequest = new java.util.HashMap<String, Object>();
        updateRequest.put("name", "Updated Team");
        updateRequest.put("description", "Updated description");
        updateRequest.put("maxMembers", 8);

        mockMvc.perform(put("/api/teams/" + teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(teamId))
                .andExpect(jsonPath("$.name").value("Updated Team"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.maxMembers").value(8));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGenerateInvitation_Success() throws Exception {
        // Given - Сначала создаем команду
        var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("Team for Invitation");
        
        String createResponse = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long teamId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Генерируем приглашение
        mockMvc.perform(post("/api/teams/" + teamId + "/invitations"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inviteCode").exists())
                .andExpect(jsonPath("$.inviteCode").isString())
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.teamId").value(teamId));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testJoinTeam_Success() throws Exception {
        // Given - Создаем команду и генерируем приглашение
        var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("Team for Join");
        
        String createResponse = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long teamId = objectMapper.readTree(createResponse).get("id").asLong();

        String inviteResponse = mockMvc.perform(post("/api/teams/" + teamId + "/invitations"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String inviteCode = objectMapper.readTree(inviteResponse).get("inviteCode").asText();

        // When & Then - Присоединяемся к команде
        var joinRequest = new java.util.HashMap<String, Object>();
        joinRequest.put("inviteCode", inviteCode);

        mockMvc.perform(post("/api/teams/" + teamId + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully joined team"))
                .andExpect(jsonPath("$.teamId").value(teamId));

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testJoinTeam_InvalidInviteCode_ReturnsBadRequest() throws Exception {
        // Given
        Long teamId = 1L;
        var joinRequest = new java.util.HashMap<String, Object>();
        joinRequest.put("inviteCode", "INVALID_CODE");

        // When & Then
        mockMvc.perform(post("/api/teams/" + teamId + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid invitation code"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testLeaveTeam_Success() throws Exception {
        // Given - Создаем команду и присоединяемся к ней
        var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("Team for Leave");
        
        String createResponse = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long teamId = objectMapper.readTree(createResponse).get("id").asLong();

        String inviteResponse = mockMvc.perform(post("/api/teams/" + teamId + "/invitations"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String inviteCode = objectMapper.readTree(inviteResponse).get("inviteCode").asText();

        var joinRequest = new java.util.HashMap<String, Object>();
        joinRequest.put("inviteCode", inviteCode);

        mockMvc.perform(post("/api/teams/" + teamId + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isOk());

        // When & Then - Выходим из команды
        mockMvc.perform(post("/api/teams/" + teamId + "/leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully left team"));

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testRemoveMember_Success() throws Exception {
        // Given - Создаем команду и добавляем участника
        var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("Team for Remove");
        
        String createResponse = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long teamId = objectMapper.readTree(createResponse).get("id").asLong();

        String inviteResponse = mockMvc.perform(post("/api/teams/" + teamId + "/invitations"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String inviteCode = objectMapper.readTree(inviteResponse).get("inviteCode").asText();

        var joinRequest = new java.util.HashMap<String, Object>();
        joinRequest.put("inviteCode", inviteCode);

        mockMvc.perform(post("/api/teams/" + teamId + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isOk());

        // When & Then - Удаляем участника (предполагаем, что знаем ID участника)
        Long memberId = 2L; // ID второго пользователя
        mockMvc.perform(delete("/api/teams/" + teamId + "/members/" + memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Member removed successfully"));

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testChangeMemberRole_Success() throws Exception {
        // Given - Создаем команду и добавляем участника
        var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("Team for Role Change");
        
        String createResponse = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long teamId = objectMapper.readTree(createResponse).get("id").asLong();

        String inviteResponse = mockMvc.perform(post("/api/teams/" + teamId + "/invitations"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String inviteCode = objectMapper.readTree(inviteResponse).get("inviteCode").asText();

        var joinRequest = new java.util.HashMap<String, Object>();
        joinRequest.put("inviteCode", inviteCode);

        mockMvc.perform(post("/api/teams/" + teamId + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isOk());

        // When & Then - Изменяем роль участника
        Long memberId = 2L; // ID второго пользователя
        var roleRequest = new java.util.HashMap<String, Object>();
        roleRequest.put("role", "MODERATOR");

        mockMvc.perform(put("/api/teams/" + teamId + "/members/" + memberId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MODERATOR"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetMyTeams_Success() throws Exception {
        // Given - Создаем несколько команд
        for (int i = 0; i < 3; i++) {
            var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("My Team " + i);
            mockMvc.perform(post("/api/teams")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(teamRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Получаем команды пользователя
        mockMvc.perform(get("/api/teams/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(3))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void testGetPublicTeams_Success() throws Exception {
        // Given - Создаем несколько публичных команд
        for (int i = 0; i < 2; i++) {
            var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("Public Team " + i);
            teamRequest.put("isPublic", true);
            
            mockMvc.perform(post("/api/teams")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(teamRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Получаем публичные команды
        mockMvc.perform(get("/api/teams/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[?(@.isPublic == true)]").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testSearchTeams_Success() throws Exception {
        // Given - Создаем команды с разными названиями
        List<String> teamNames = List.of("Adventure Team", "Puzzle Team", "Adventure Puzzle");
        
        for (String name : teamNames) {
            var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO(name);
            teamRequest.put("isPublic", true);
            
            mockMvc.perform(post("/api/teams")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(teamRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Ищем команды по ключевому слову
        mockMvc.perform(get("/api/teams/search")
                        .param("keyword", "Adventure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(2)) // Две команды с "Adventure"
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetTeamStatistics_Success() throws Exception {
        // Given - Создаем команду
        var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("Team for Stats");
        
        String createResponse = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long teamId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Получаем статистику команды
        mockMvc.perform(get("/api/teams/" + teamId + "/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(teamId))
                .andExpect(jsonPath("$.totalMembers").exists())
                .andExpect(jsonPath("$.activeMembers").exists())
                .andExpect(jsonPath("$.totalQuests").exists())
                .andExpect(jsonPath("$.completedQuests").exists())
                .andExpect(jsonPath("$.averageScore").exists());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllTeams_Admin_Success() throws Exception {
        // Given - Создаем несколько команд
        for (int i = 0; i < 5; i++) {
            var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("Admin Team " + i);
            mockMvc.perform(post("/api/teams")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(teamRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Администратор получает все команды
        mockMvc.perform(get("/api/teams/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(5))
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetAllTeams_Player_Forbidden() throws Exception {
        // When & Then - Обычный пользователь не может получить все команды
        mockMvc.perform(get("/api/teams/admin/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testDeleteTeam_Success() throws Exception {
        // Given - Сначала создаем команду
        var teamRequest = EnhancedTestDataFactory.createTeamRequestDTO("Team to Delete");
        
        String createResponse = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long teamId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Удаляем команду
        mockMvc.perform(delete("/api/teams/" + teamId))
                .andExpect(status().isNoContent());

        // Проверяем, что команда удалена
        mockMvc.perform(get("/api/teams/" + teamId))
                .andExpect(status().isNotFound());
    }
}