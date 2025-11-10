package dn.quest.usermanagement.integration;

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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для User Management Service
 */
class UserManagementIntegrationTest extends AbstractIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    protected void cleanupTestData() {
        // Очистка тестовых данных для управления пользователями
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateUser_Success() throws Exception {
        // Given
        var userRequest = new java.util.HashMap<String, Object>();
        userRequest.put("username", "testuser");
        userRequest.put("email", "test@example.com");
        userRequest.put("password", "SecurePassword123!");
        userRequest.put("firstName", "Test");
        userRequest.put("lastName", "User");
        userRequest.put("role", "PLAYER");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("PLAYER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist()); // Пароль не должен возвращаться

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateUser_InvalidData_ReturnsBadRequest() throws Exception {
        // Given
        var invalidUser = new java.util.HashMap<String, Object>();
        invalidUser.put("username", ""); // Пустое имя пользователя
        invalidUser.put("email", "invalid-email"); // Невалидный email
        invalidUser.put("password", "123"); // Слишком короткий пароль
        invalidUser.put("firstName", "Test");
        invalidUser.put("lastName", "User");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testCreateUser_Player_Forbidden() throws Exception {
        // Given
        var userRequest = new java.util.HashMap<String, Object>();
        userRequest.put("username", "newuser");
        userRequest.put("email", "new@example.com");
        userRequest.put("password", "SecurePassword123!");
        userRequest.put("firstName", "New");
        userRequest.put("lastName", "User");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetUser_Success() throws Exception {
        // Given - Сначала создаем пользователя
        var userRequest = new java.util.HashMap<String, Object>();
        userRequest.put("username", "getuser");
        userRequest.put("email", "get@example.com");
        userRequest.put("password", "SecurePassword123!");
        userRequest.put("firstName", "Get");
        userRequest.put("lastName", "User");
        userRequest.put("role", "PLAYER");

        String createResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Получаем пользователя
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("getuser"))
                .andExpect(jsonPath("$.email").value("get@example.com"))
                .andExpect(jsonPath("$.firstName").value("Get"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("PLAYER"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetMyProfile_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.lastName").exists())
                .andExpect(jsonPath("$.role").exists())
                .andExpect(jsonPath("$.active").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUpdateMyProfile_Success() throws Exception {
        // Given
        var updateRequest = new java.util.HashMap<String, Object>();
        updateRequest.put("firstName", "Updated");
        updateRequest.put("lastName", "Name");
        updateRequest.put("email", "updated@example.com");

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testChangePassword_Success() throws Exception {
        // Given
        var passwordRequest = new java.util.HashMap<String, Object>();
        passwordRequest.put("currentPassword", "oldPassword");
        passwordRequest.put("newPassword", "NewSecurePassword123!");

        // When & Then
        mockMvc.perform(put("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testChangePassword_WrongCurrentPassword_ReturnsBadRequest() throws Exception {
        // Given
        var passwordRequest = new java.util.HashMap<String, Object>();
        passwordRequest.put("currentPassword", "wrongPassword");
        passwordRequest.put("newPassword", "NewSecurePassword123!");

        // When & Then
        mockMvc.perform(put("/api/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateUser_Success() throws Exception {
        // Given - Сначала создаем пользователя
        var userRequest = new java.util.HashMap<String, Object>();
        userRequest.put("username", "updateuser");
        userRequest.put("email", "update@example.com");
        userRequest.put("password", "SecurePassword123!");
        userRequest.put("firstName", "Update");
        userRequest.put("lastName", "User");
        userRequest.put("role", "PLAYER");

        String createResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Обновляем пользователя
        var updateRequest = new java.util.HashMap<String, Object>();
        updateRequest.put("firstName", "Updated");
        updateRequest.put("lastName", "Name");
        updateRequest.put("role", "MODERATOR");
        updateRequest.put("active", true);

        mockMvc.perform(put("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.role").value("MODERATOR"))
                .andExpect(jsonPath("$.active").value(true));

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeactivateUser_Success() throws Exception {
        // Given - Сначала создаем пользователя
        var userRequest = new java.util.HashMap<String, Object>();
        userRequest.put("username", "deactivateuser");
        userRequest.put("email", "deactivate@example.com");
        userRequest.put("password", "SecurePassword123!");
        userRequest.put("firstName", "Deactivate");
        userRequest.put("lastName", "User");
        userRequest.put("role", "PLAYER");

        String createResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Деактивируем пользователя
        mockMvc.perform(put("/api/users/" + userId + "/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.active").value(false));

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testActivateUser_Success() throws Exception {
        // Given - Сначала создаем и деактивируем пользователя
        var userRequest = new java.util.HashMap<String, Object>();
        userRequest.put("username", "activateuser");
        userRequest.put("email", "activate@example.com");
        userRequest.put("password", "SecurePassword123!");
        userRequest.put("firstName", "Activate");
        userRequest.put("lastName", "User");
        userRequest.put("role", "PLAYER");

        String createResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(put("/api/users/" + userId + "/deactivate"))
                .andExpect(status().isOk());

        // When & Then - Активируем пользователя
        mockMvc.perform(put("/api/users/" + userId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.active").value(true));

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteUser_Success() throws Exception {
        // Given - Сначала создаем пользователя
        var userRequest = new java.util.HashMap<String, Object>();
        userRequest.put("username", "deleteuser");
        userRequest.put("email", "delete@example.com");
        userRequest.put("password", "SecurePassword123!");
        userRequest.put("firstName", "Delete");
        userRequest.put("lastName", "User");
        userRequest.put("role", "PLAYER");

        String createResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Удаляем пользователя
        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNoContent());

        // Проверяем, что пользователь удален
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isNotFound());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllUsers_Success() throws Exception {
        // Given - Создаем несколько пользователей
        for (int i = 0; i < 3; i++) {
            var userRequest = new java.util.HashMap<String, Object>();
            userRequest.put("username", "user" + i);
            userRequest.put("email", "user" + i + "@example.com");
            userRequest.put("password", "SecurePassword123!");
            userRequest.put("firstName", "User");
            userRequest.put("lastName", String.valueOf(i));
            userRequest.put("role", "PLAYER");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Получаем всех пользователей
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetAllUsers_Player_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testSearchUsers_Success() throws Exception {
        // Given - Создаем пользователей с разными именами
        List<String> usernames = List.of("john_doe", "jane_smith", "bob_wilson");
        
        for (String username : usernames) {
            var userRequest = new java.util.HashMap<String, Object>();
            userRequest.put("username", username);
            userRequest.put("email", username + "@example.com");
            userRequest.put("password", "SecurePassword123!");
            userRequest.put("firstName", username.split("_")[0]);
            userRequest.put("lastName", username.split("_")[1]);
            userRequest.put("role", "PLAYER");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Ищем пользователей по ключевому слову
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].username").value("john_doe"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetUsersByRole_Success() throws Exception {
        // Given - Создаем пользователей с разными ролями
        var playerRequest = new java.util.HashMap<String, Object>();
        playerRequest.put("username", "player1");
        playerRequest.put("email", "player1@example.com");
        playerRequest.put("password", "SecurePassword123!");
        playerRequest.put("firstName", "Player");
        playerRequest.put("lastName", "One");
        playerRequest.put("role", "PLAYER");

        var moderatorRequest = new java.util.HashMap<String, Object>();
        moderatorRequest.put("username", "moderator1");
        moderatorRequest.put("email", "moderator1@example.com");
        moderatorRequest.put("password", "SecurePassword123!");
        moderatorRequest.put("firstName", "Moderator");
        moderatorRequest.put("lastName", "One");
        moderatorRequest.put("role", "MODERATOR");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(playerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moderatorRequest)))
                .andExpect(status().isCreated());

        // When & Then - Получаем пользователей по роли
        mockMvc.perform(get("/api/users/role/PLAYER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").hasJsonPath(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].role").value("PLAYER"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetUserStatistics_Success() throws Exception {
        // Given - Создаем несколько пользователей
        for (int i = 0; i < 2; i++) {
            var userRequest = new java.util.HashMap<String, Object>();
            userRequest.put("username", "statsuser" + i);
            userRequest.put("email", "statsuser" + i + "@example.com");
            userRequest.put("password", "SecurePassword123!");
            userRequest.put("firstName", "Stats");
            userRequest.put("lastName", "User" + i);
            userRequest.put("role", "PLAYER");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequest)))
                    .andExpect(status().isCreated());
        }

        // When & Then - Получаем статистику пользователей
        mockMvc.perform(get("/api/users/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").exists())
                .andExpect(jsonPath("$.activeUsers").exists())
                .andExpect(jsonPath("$.inactiveUsers").exists())
                .andExpect(jsonPath("$.usersByRole").exists())
                .andExpect(jsonPath("$.newUsersThisMonth").exists())
                .andExpect(jsonPath("$.lastUpdated").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetUserStatistics_Player_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/statistics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testAssignRole_Success() throws Exception {
        // Given - Сначала создаем пользователя
        var userRequest = new java.util.HashMap<String, Object>();
        userRequest.put("username", "roleuser");
        userRequest.put("email", "role@example.com");
        userRequest.put("password", "SecurePassword123!");
        userRequest.put("firstName", "Role");
        userRequest.put("lastName", "User");
        userRequest.put("role", "PLAYER");

        String createResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Назначаем новую роль
        var roleRequest = new java.util.HashMap<String, Object>();
        roleRequest.put("role", "MODERATOR");

        mockMvc.perform(put("/api/users/" + userId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.role").value("MODERATOR"));

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUploadAvatar_Success() throws Exception {
        // Given
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar image content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/users/avatar")
                        .file(avatarFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Avatar uploaded successfully"))
                .andExpect(jsonPath("$.avatarUrl").exists());

        // Проверка отправки события в Kafka
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUploadAvatar_InvalidFile_ReturnsBadRequest() throws Exception {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "avatar",
                "avatar.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not an image".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/users/avatar")
                        .file(invalidFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid file format. Only images are allowed"));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testGetUserPreferences_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.language").exists())
                .andExpect(jsonPath("$.timezone").exists())
                .andExpect(jsonPath("$.notifications").exists())
                .andExpect(jsonPath("$.privacy").exists())
                .andExpect(jsonPath("$.theme").exists());
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testUpdateUserPreferences_Success() throws Exception {
        // Given
        var preferencesRequest = new java.util.HashMap<String, Object>();
        preferencesRequest.put("language", "ru");
        preferencesRequest.put("timezone", "Europe/Moscow");
        preferencesRequest.put("theme", "dark");
        preferencesRequest.put("notifications", Map.of(
                "email", true,
                "push", false,
                "inApp", true
        ));
        preferencesRequest.put("privacy", Map.of(
                "profileVisibility", "PUBLIC",
                "showEmail", false,
                "showStatistics", true
        ));

        // When & Then
        mockMvc.perform(put("/api/users/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preferencesRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("ru"))
                .andExpect(jsonPath("$.timezone").value("Europe/Moscow"))
                .andExpect(jsonPath("$.theme").value("dark"))
                .andExpect(jsonPath("$.notifications.email").value(true))
                .andExpect(jsonPath("$.privacy.profileVisibility").value("PUBLIC"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testExportUsers_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/export")
                        .param("format", "CSV")
                        .param("role", "PLAYER"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"users.csv\""));
    }

    @Test
    @WithMockUser(roles = {"PLAYER"})
    void testExportUsers_Player_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/export")
                        .param("format", "CSV"))
                .andExpect(status().isForbidden());
    }
}