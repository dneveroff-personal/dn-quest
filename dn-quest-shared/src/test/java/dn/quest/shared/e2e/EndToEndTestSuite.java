package dn.quest.shared.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.authentication.dto.LoginRequestDTO;
import dn.quest.authentication.dto.RegisterRequestDTO;
import dn.quest.shared.base.MicroserviceIntegrationTestBase;
import dn.quest.shared.enums.UserRole;
import dn.quest.shared.util.TestDataFactory;
import dn.quest.shared.events.user.UserRegisteredEvent;
import dn.quest.shared.events.quest.QuestCreatedEvent;
import dn.quest.shared.events.game.GameSessionStartedEvent;
import dn.quest.shared.events.team.TeamCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end тесты для основных бизнес-сценариев DN Quest
 */
@Transactional
class EndToEndTestSuite extends MicroserviceIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    private String userToken;
    private String adminToken;
    private Long userId;
    private Long questId;
    private Long teamId;
    private String sessionId;

    @Override
    protected void cleanupTestData() {
        // Очистка тестовых данных после каждого теста
    }

    @Test
    @Order(1)
    void testCompleteUserRegistrationFlow() throws Exception {
        // Given
        RegisterRequestDTO registerRequest = TestDataFactory.createRegisterRequestDTO(
                "e2euser", "e2e@test.com");

        // When & Then - Регистрация пользователя
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.username").value("e2euser"))
                .andExpect(jsonPath("$.user.email").value("e2e@test.com"))
                .andExpect(jsonPath("$.user.role").value("PLAYER"))
                .andReturn().getResponse().getContentAsString();

        userToken = objectMapper.readTree(registerResponse).get("accessToken").asText();
        userId = objectMapper.readTree(registerResponse).get("user").get("id").asLong();

        // Проверка валидации токена
        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        // Проверка получения профиля
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("e2euser"))
                .andExpect(jsonPath("$.email").value("e2e@test.com"));
    }

    @Test
    @Order(2)
    void testCompleteQuestCreationFlow() throws Exception {
        // Given - Аутентифицированный пользователь
        if (userToken == null) {
            testCompleteUserRegistrationFlow();
        }

        // When & Then - Создание квеста
        String questRequest = """
                {
                    "title": "E2E Test Quest",
                    "description": "End-to-end test quest",
                    "difficulty": "EASY",
                    "questType": "SOLO",
                    "estimatedDuration": 30,
                    "maxParticipants": 1,
                    "levels": [
                        {
                            "title": "Level 1",
                            "description": "First level",
                            "orderIndex": 1,
                            "codes": [
                                {
                                    "value": "TEST123",
                                    "type": "TEXT",
                                    "points": 100
                                }
                            ]
                        }
                    ]
                }
                """;

        String questResponse = mockMvc.perform(post("/api/quests")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("E2E Test Quest"))
                .andExpect(jsonPath("$.description").value("End-to-end test quest"))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.questType").value("SOLO"))
                .andExpect(jsonPath("$.published").value(false))
                .andReturn().getResponse().getContentAsString();

        questId = objectMapper.readTree(questResponse).get("id").asLong();

        // Публикация квеста
        mockMvc.perform(post("/api/quests/" + questId + "/publish")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));

        // Получение списка квестов
        mockMvc.perform(get("/api/quests")
                        .param("published", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + questId + ")]").exists());
    }

    @Test
    @Order(3)
    void testCompleteGameSessionFlow() throws Exception {
        // Given - Созданный квест
        if (questId == null) {
            testCompleteQuestCreationFlow();
        }

        // When & Then - Начало игровой сессии
        String sessionRequest = """
                {
                    "questId": """ + questId + """
                }
                """;

        String sessionResponse = mockMvc.perform(post("/api/game/sessions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sessionRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questId").value(questId))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.currentLevel.orderIndex").value(1))
                .andReturn().getResponse().getContentAsString();

        sessionId = objectMapper.readTree(sessionResponse).get("id").asText();

        // Получение текущего уровня
        mockMvc.perform(get("/api/game/sessions/" + sessionId + "/current")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level.orderIndex").value(1))
                .andExpect(jsonPath("$.progress.sectorsClosed").value(0));

        // Отправка кода
        String codeRequest = """
                {
                    "rawCode": "TEST123",
                    "userId": """ + userId + """
                }
                """;

        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/code")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(codeRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ACCEPTED_NORMAL"))
                .andExpect(jsonPath("$.message").exists());

        // Завершение сессии
        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/finish")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    @Order(4)
    void testCompleteTeamFlow() throws Exception {
        // Given - Аутентифицированный пользователь
        if (userToken == null) {
            testCompleteUserRegistrationFlow();
        }

        // When & Then - Создание команды
        String teamRequest = """
                {
                    "name": "E2E Test Team",
                    "description": "End-to-end test team",
                    "maxMembers": 5
                }
                """;

        String teamResponse = mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teamRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("E2E Test Team"))
                .andExpect(jsonPath("$.description").value("End-to-end test team"))
                .andExpect(jsonPath("$.memberCount").value(1))
                .andReturn().getResponse().getContentAsString();

        teamId = objectMapper.readTree(teamResponse).get("id").asLong();

        // Получение информации о команде
        mockMvc.perform(get("/api/teams/" + teamId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("E2E Test Team"))
                .andExpect(jsonPath("$.members[?(@.userId == " + userId + ")]").exists());

        // Генерация приглашения
        String inviteResponse = mockMvc.perform(post("/api/teams/" + teamId + "/invitations")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inviteCode").exists())
                .andReturn().getResponse().getContentAsString();

        String inviteCode = objectMapper.readTree(inviteResponse).get("inviteCode").asText();

        // Регистрация второго пользователя
        RegisterRequestDTO secondUserRequest = TestDataFactory.createRegisterRequestDTO(
                "e2euser2", "e2e2@test.com");

        String secondUserResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUserRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String secondUserToken = objectMapper.readTree(secondUserResponse).get("accessToken").asText();

        // Присоединение к команде
        mockMvc.perform(post("/api/teams/" + teamId + "/join")
                        .header("Authorization", "Bearer " + secondUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inviteCode\":\"" + inviteCode + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully joined team"));

        // Проверка количества участников
        mockMvc.perform(get("/api/teams/" + teamId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberCount").value(2));
    }

    @Test
    @Order(5)
    void testCompleteFileUploadFlow() throws Exception {
        // Given - Аутентифицированный пользователь
        if (userToken == null) {
            testCompleteUserRegistrationFlow();
        }

        // When & Then - Загрузка файла
        String fileRequest = """
                {
                    "fileName": "test-file.txt",
                    "fileSize": 1024,
                    "mimeType": "text/plain",
                    "content": "dGVzdCBjb250ZW50"
                }
                """;

        String fileResponse = mockMvc.perform(post("/api/files/upload")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fileRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("test-file.txt"))
                .andExpect(jsonPath("$.fileSize").value(1024))
                .andExpect(jsonPath("$.mimeType").value("text/plain"))
                .andExpect(jsonPath("$.url").exists())
                .andReturn().getResponse().getContentAsString();

        String fileId = objectMapper.readTree(fileResponse).get("id").asText();

        // Получение информации о файле
        mockMvc.perform(get("/api/files/" + fileId + "/info")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test-file.txt"))
                .andExpect(jsonPath("$.fileSize").value(1024));

        // Получение статистики хранилища
        mockMvc.perform(get("/api/files/stats")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFiles").exists())
                .andExpect(jsonPath("$.totalSize").exists());
    }

    @Test
    @Order(6)
    void testCompleteAdminFlow() throws Exception {
        // Given - Регистрация администратора
        RegisterRequestDTO adminRequest = RegisterRequestDTO.builder()
                .username("e2eadmin")
                .email("e2eadmin@test.com")
                .publicName("E2E Admin")
                .password("AdminPass123!")
                .build();

        String adminResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        adminToken = objectMapper.readTree(adminResponse).get("accessToken").asText();

        // When & Then - Получение списка пользователей (только для админа)
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());

        // Блокировка пользователя
        mockMvc.perform(post("/api/admin/users/" + userId + "/toggle-status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        // Разблокировка пользователя
        mockMvc.perform(post("/api/admin/users/" + userId + "/toggle-status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @Order(7)
    void testCompleteNotificationFlow() throws Exception {
        // Given - Аутентифицированный пользователь
        if (userToken == null) {
            testCompleteUserRegistrationFlow();
        }

        // When & Then - Получение уведомлений
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // Отметка уведомлений как прочитанных
        mockMvc.perform(post("/api/notifications/mark-all-read")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All notifications marked as read"));
    }

    @Test
    @Order(8)
    void testCompleteStatisticsFlow() throws Exception {
        // Given - Аутентифицированный пользователь
        if (userToken == null) {
            testCompleteUserRegistrationFlow();
        }

        // When & Then - Получение статистики пользователя
        mockMvc.perform(get("/api/statistics/user")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuests").exists())
                .andExpect(jsonPath("$.completedQuests").exists())
                .andExpect(jsonPath("$.totalPoints").exists());

        // Получение глобальной статистики
        mockMvc.perform(get("/api/statistics/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").exists())
                .andExpect(jsonPath("$.totalQuests").exists())
                .andExpect(jsonPath("$.activeSessions").exists());
    }
}