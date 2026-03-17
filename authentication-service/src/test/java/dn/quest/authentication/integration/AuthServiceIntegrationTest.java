package dn.quest.authentication.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.authentication.dto.LoginRequestDTO;
import dn.quest.authentication.dto.RegisterRequestDTO;
import dn.quest.authentication.entity.User;
import dn.quest.authentication.repository.UserRepository;
import dn.quest.shared.base.MicroserviceIntegrationTestBase;
import dn.quest.shared.enums.UserRole;
import dn.quest.shared.util.TestDataFactory;
import dn.quest.shared.events.user.UserRegisteredEvent;
import dn.quest.shared.events.user.UserUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для Authentication Service с Kafka
 */
class AuthServiceIntegrationTest extends MicroserviceIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    protected void cleanupTestData() {
        userRepository.deleteAll();
    }

    @Test
    void testUserRegistrationFlow_Success() throws Exception {
        // Given
        RegisterRequestDTO registerRequest = TestDataFactory.createRegisterRequestDTO(
                "newuser", "newuser@test.com");

        // When & Then - Регистрация
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.username").value("newuser"))
                .andExpect(jsonPath("$.user.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.user.role").value("PLAYER"));

        // Проверка сохранения пользователя в БД
        Optional<User> savedUser = userRepository.findByUsername("newuser");
        assertTrue(savedUser.isPresent());
        assertEquals("newuser@test.com", savedUser.get().getEmail());
        assertTrue(passwordEncoder.matches("Password123", savedUser.get().getPasswordHash()));

        // Проверка отправки события в Kafka
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(kafkaTemplate, times(1)).send(
                    eq("user-events"),
                    any(String.class),
                    any(String.class)
            );
        });
    }

    @Test
    void testUserLoginFlow_Success() throws Exception {
        // Given - Создаем пользователя в БД
        User testUser = TestDataFactory.createTestUser("testuser", "test@test.com", UserRole.PLAYER);
        testUser.setPasswordHash(passwordEncoder.encode("Password123"));
        userRepository.save(testUser);

        LoginRequestDTO loginRequest = TestDataFactory.createLoginRequestDTO("testuser");

        // When & Then - Вход
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@test.com"));
    }

    @Test
    void testTokenValidationFlow_Success() throws Exception {
        // Given - Регистрируем пользователя и получаем токен
        RegisterRequestDTO registerRequest = TestDataFactory.createRegisterRequestDTO(
                "tokenuser", "token@test.com");

        String loginResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // When & Then - Валидация токена
        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void testPasswordResetFlow_Success() throws Exception {
        // Given - Создаем пользователя
        User testUser = TestDataFactory.createTestUser("resetuser", "reset@test.com", UserRole.PLAYER);
        userRepository.save(testUser);

        // When & Then - Запрос на сброс пароля
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"reset@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Инструкция по восстановлению пароля отправлена на указанный email"));
    }

    @Test
    void testUserUpdateFlow_Success() throws Exception {
        // Given - Создаем и логиним пользователя
        User testUser = TestDataFactory.createTestUser("updateuser", "update@test.com", UserRole.PLAYER);
        userRepository.save(testUser);

        LoginRequestDTO loginRequest = TestDataFactory.createLoginRequestDTO("updateuser");
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // When & Then - Обновление профиля
        mockMvc.perform(put("/api/auth/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"publicName\":\"Updated Name\",\"email\":\"updated@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicName").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@test.com"));

        // Проверка отправки события обновления
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(kafkaTemplate, atLeastOnce()).send(
                    eq("user-events"),
                    any(String.class),
                    any(String.class)
            );
        });
    }

    @Test
    void testLogoutFlow_Success() throws Exception {
        // Given - Логиним пользователя
        RegisterRequestDTO registerRequest = TestDataFactory.createRegisterRequestDTO(
                "logoutuser", "logout@test.com");

        String loginResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        // When & Then - Выход
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Выход выполнен успешно"));
    }

    @Test
    void testTokenRefreshFlow_Success() throws Exception {
        // Given - Регистрируем пользователя
        RegisterRequestDTO registerRequest = TestDataFactory.createRegisterRequestDTO(
                "refreshuser", "refresh@test.com");

        String loginResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        // When & Then - Обновление токена
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void testInvalidCredentialsFlow_Unauthorized() throws Exception {
        // Given
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .username("nonexistent")
                .password("wrongpassword")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Неверные учетные данные"));
    }

    @Test
    void testDuplicateUserRegistration_Conflict() throws Exception {
        // Given - Создаем пользователя
        User existingUser = TestDataFactory.createTestUser("duplicate", "duplicate@test.com", UserRole.PLAYER);
        userRepository.save(existingUser);

        RegisterRequestDTO registerRequest = TestDataFactory.createRegisterRequestDTO(
                "duplicate", "another@test.com");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Пользователь с таким именем уже существует"));
    }

    @Test
    void testInvalidTokenValidation_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void testProtectedEndpointWithoutToken_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isUnauthorized());
    }
}