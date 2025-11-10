package dn.quest.shared.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.base.AbstractIntegrationTestBase;
import dn.quest.shared.util.EnhancedTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End тесты для полного цикла пользователя в DN Quest
 */
@DisplayName("End-to-End тесты пользовательского пути")
class UserJourneyE2ETest extends AbstractIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    private String userToken;
    private Long userId;
    private Long questId;
    private Long teamId;
    private Long sessionId;

    @Override
    protected void cleanupTestData() {
        // Очистка тестовых данных после каждого теста
    }

    @Test
    @Order(1)
    @DisplayName("Полный цикл: Регистрация -> Создание квеста -> Игра -> Завершение")
    void testCompleteUserJourney() throws Exception {
        // Шаг 1: Регистрация пользователя
        registerUser();
        
        // Шаг 2: Создание квеста
        createQuest();
        
        // Шаг 3: Создание команды
        createTeam();
        
        // Шаг 4: Начало игровой сессии
        startGameSession();
        
        // Шаг 5: Выполнение заданий квеста
        completeQuestTasks();
        
        // Шаг 6: Завершение игровой сессии
        completeGameSession();
        
        // Шаг 7: Проверка статистики
        verifyUserStatistics();
        
        // Шаг 8: Проверка уведомлений
        verifyNotifications();
    }

    @Test
    @Order(2)
    @DisplayName("Цикл с загрузкой файлов и совместной игрой")
    void testJourneyWithFileUploadAndTeamPlay() throws Exception {
        // Шаг 1: Регистрация и аутентификация
        registerUser();
        
        // Шаг 2: Загрузка аватара
        uploadAvatar();
        
        // Шаг 3: Создание команды
        createTeam();
        
        // Шаг 4: Генерация приглашения в команду
        generateTeamInvitation();
        
        // Шаг 5: Создание квеста с файлами
        createQuestWithFiles();
        
        // Шаг 6: Начало командной игры
        startTeamGameSession();
        
        // Шаг 7: Совместное выполнение заданий
        completeTeamQuestTasks();
        
        // Шаг 8: Проверка командной статистики
        verifyTeamStatistics();
    }

    @Test
    @Order(3)
    @DisplayName("Цикл с поиском квестов и участием в существующих командах")
    void testJourneyWithQuestSearchAndTeamJoin() throws Exception {
        // Шаг 1: Регистрация
        registerUser();
        
        // Шаг 2: Поиск публичных квестов
        searchPublicQuests();
        
        // Шаг 3: Поиск команд для присоединения
        searchTeams();
        
        // Шаг 4: Присоединение к команде
        joinTeam();
        
        // Шаг 5: Участие в игровом сеансе
        participateInGameSession();
        
        // Шаг 6: Проверка прогресса
        verifyProgress();
    }

    private void registerUser() throws Exception {
        var registerRequest = new java.util.HashMap<String, Object>();
        registerRequest.put("username", "journeyuser");
        registerRequest.put("email", "journey@example.com");
        registerRequest.put("password", "SecurePassword123!");
        registerRequest.put("firstName", "Journey");
        registerRequest.put("lastName", "User");

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("journeyuser"))
                .andExpect(jsonPath("$.email").value("journey@example.com"))
                .andReturn().getResponse().getContentAsString();

        userId = objectMapper.readTree(response).get("id").asLong();

        // Логин для получения токена
        var loginRequest = new java.util.HashMap<String, Object>();
        loginRequest.put("username", "journeyuser");
        loginRequest.put("password", "SecurePassword123!");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        userToken = objectMapper.readTree(loginResponse).get("token").asText();

        // Проверка отправки событий в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    private void createQuest() throws Exception {
        var questRequest = new java.util.HashMap<String, Object>();
        questRequest.put("title", "E2E Test Quest");
        questRequest.put("description", "Quest created for end-to-end testing");
        questRequest.put("difficulty", "MEDIUM");
        questRequest.put("estimatedDuration", 45);
        questRequest.put("category", "ADVENTURE");
        questRequest.put("tags", List.of("e2e", "test", "adventure"));
        questRequest.put("isPublic", true);

        String response = mockMvc.perform(post("/api/quests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(objectMapper.writeValueAsString(questRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("E2E Test Quest"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        questId = objectMapper.readTree(response).get("id").asLong();

        // Публикация квеста
        mockMvc.perform(post("/api/quests/" + questId + "/publish")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    private void createTeam() throws Exception {
        var teamRequest = new java.util.HashMap<String, Object>();
        teamRequest.put("name", "E2E Test Team");
        teamRequest.put("description", "Team created for end-to-end testing");
        teamRequest.put("maxMembers", 5);
        teamRequest.put("isPublic", true);

        String response = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(objectMapper.writeValueAsString(teamRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("E2E Test Team"))
                .andReturn().getResponse().getContentAsString();

        teamId = objectMapper.readTree(response).get("id").asLong();

        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    private void startGameSession() throws Exception {
        var sessionRequest = new java.util.HashMap<String, Object>();
        sessionRequest.put("questId", questId);
        sessionRequest.put("teamId", teamId);

        String response = mockMvc.perform(post("/api/game-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();

        sessionId = objectMapper.readTree(response).get("id").asLong();

        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    private void completeQuestTasks() throws Exception {
        // Получение первого уровня квеста
        String levelResponse = mockMvc.perform(get("/api/quests/" + questId + "/levels")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long levelId = objectMapper.readTree(levelResponse).get("content").get(0).get("id").asLong();

        // Отправка решения для кода
        var codeSubmission = new java.util.HashMap<String, Object>();
        codeSubmission.put("code", "print('Hello, E2E Test!')");
        codeSubmission.put("language", "python");

        mockMvc.perform(post("/api/game-sessions/" + sessionId + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(objectMapper.writeValueAsString(codeSubmission)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true));

        // Получение подсказки
        mockMvc.perform(post("/api/game-sessions/" + sessionId + "/hints/" + levelId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hintText").exists());

        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    private void completeGameSession() throws Exception {
        mockMvc.perform(post("/api/game-sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.finalScore").exists())
                .andExpect(jsonPath("$.completionTime").exists());

        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    private void verifyUserStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/user/" + userId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.completedQuests").value(1))
                .andExpect(jsonPath("$.totalScore").exists())
                .andExpect(jsonPath("$.level").exists());
    }

    private void verifyNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/my")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.type == 'QUEST_COMPLETED')]").exists())
                .andExpect(jsonPath("$.content[?(@.type == 'ACHIEVEMENT_UNLOCKED')]").exists());
    }

    private void uploadAvatar() throws Exception {
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar",
                "journey-avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "journey avatar image content".getBytes()
        );

        mockMvc.perform(multipart("/api/users/avatar")
                        .file(avatarFile)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").exists());

        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    private void generateTeamInvitation() throws Exception {
        mockMvc.perform(post("/api/teams/" + teamId + "/invitations")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inviteCode").exists())
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    private void createQuestWithFiles() throws Exception {
        // Загрузка файла для квеста
        MockMultipartFile questFile = new MockMultipartFile(
                "file",
                "quest-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "quest image content".getBytes()
        );

        String fileResponse = mockMvc.perform(multipart("/api/files/upload")
                        .file(questFile)
                        .param("category", "QUEST_IMAGE")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String fileId = objectMapper.readTree(fileResponse).get("id").asText();

        // Создание квеста с файлом
        var questRequest = new java.util.HashMap<String, Object>();
        questRequest.put("title", "E2E Quest with Files");
        questRequest.put("description", "Quest with attached files");
        questRequest.put("difficulty", "EASY");
        questRequest.put("estimatedDuration", 30);
        questRequest.put("category", "PUZZLE");
        questRequest.put("imageFileId", fileId);

        String response = mockMvc.perform(post("/api/quests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(objectMapper.writeValueAsString(questRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        questId = objectMapper.readTree(response).get("id").asLong();

        // Публикация квеста
        mockMvc.perform(post("/api/quests/" + questId + "/publish")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    private void startTeamGameSession() throws Exception {
        var sessionRequest = new java.util.HashMap<String, Object>();
        sessionRequest.put("questId", questId);
        sessionRequest.put("teamId", teamId);

        String response = mockMvc.perform(post("/api/game-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(objectMapper.writeValueAsString(sessionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        sessionId = objectMapper.readTree(response).get("id").asLong();
    }

    private void completeTeamQuestTasks() throws Exception {
        // Получение уровней
        String levelResponse = mockMvc.perform(get("/api/quests/" + questId + "/levels")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long levelId = objectMapper.readTree(levelResponse).get("content").get(0).get("id").asLong();

        // Командное решение
        var codeSubmission = new java.util.HashMap<String, Object>();
        codeSubmission.put("code", "def team_solution():\n    return 'Team E2E Success!'");
        codeSubmission.put("language", "python");

        mockMvc.perform(post("/api/game-sessions/" + sessionId + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(objectMapper.writeValueAsString(codeSubmission)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true));

        // Завершение сессии
        mockMvc.perform(post("/api/game-sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    private void verifyTeamStatistics() throws Exception {
        mockMvc.perform(get("/api/statistics/team/" + teamId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(teamId))
                .andExpect(jsonPath("$.completedQuests").value(1))
                .andExpect(jsonPath("$.totalScore").exists());
    }

    private void searchPublicQuests() throws Exception {
        mockMvc.perform(get("/api/quests/search")
                        .param("keyword", "adventure")
                        .param("difficulty", "MEDIUM")
                        .param("isPublic", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    private void searchTeams() throws Exception {
        mockMvc.perform(get("/api/teams/search")
                        .param("keyword", "test")
                        .param("isPublic", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    private void joinTeam() throws Exception {
        // Генерация приглашения для присоединения
        String inviteResponse = mockMvc.perform(post("/api/teams/" + teamId + "/invitations")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String inviteCode = objectMapper.readTree(inviteResponse).get("inviteCode").asText();

        // Присоединение к команде
        var joinRequest = new java.util.HashMap<String, Object>();
        joinRequest.put("inviteCode", inviteCode);

        mockMvc.perform(post("/api/teams/" + teamId + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully joined team"));
    }

    private void participateInGameSession() throws Exception {
        // Поиск активных игровых сессий
        mockMvc.perform(get("/api/game-sessions/active")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // Участие в существующей сессии
        var participationRequest = new java.util.HashMap<String, Object>();
        participationRequest.put("sessionId", sessionId);

        mockMvc.perform(post("/api/game-sessions/" + sessionId + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(objectMapper.writeValueAsString(participationRequest)))
                .andExpect(status().isOk());
    }

    private void verifyProgress() throws Exception {
        mockMvc.perform(get("/api/statistics/user/" + userId + "/progress")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.currentLevel").exists())
                .andExpect(jsonPath("$.progressPercentage").exists())
                .andExpect(jsonPath("$.recentAchievements").isArray());
    }
}