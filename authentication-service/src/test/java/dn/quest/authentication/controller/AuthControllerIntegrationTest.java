package dn.quest.authentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.authentication.dto.LoginRequestDTO;
import dn.quest.authentication.dto.RegisterRequestDTO;
import dn.quest.authentication.entity.User;
import dn.quest.authentication.repository.UserRepository;
import dn.quest.shared.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration тесты для AuthController
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Создаем тестового пользователя
        testUser = User.builder()
                .username("testuser")
                .passwordHash(passwordEncoder.encode("password123"))
                .email("test@example.com")
                .publicName("Test User")
                .role(UserRole.PLAYER)
                .isActive(true)
                .isEmailVerified(false)
                .build();

        userRepository.save(testUser);
    }

    @Test
    void testRegister_Success() throws Exception {
        // Given
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .username("newuser")
                .email("newuser@example.com")
                .publicName("New User")
                .password("Password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("newuser"))
                .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.user.role").value("PLAYER"));
    }

    @Test
    void testRegister_UsernameExists_ReturnsConflict() throws Exception {
        // Given
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .username("testuser") // Уже существует
                .email("another@example.com")
                .password("Password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Пользователь с таким именем уже существует"));
    }

    @Test
    void testRegister_InvalidData_ReturnsBadRequest() throws Exception {
        // Given
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .username("ab") // Слишком короткое имя
                .email("invalid-email") // Невалидный email
                .password("123") // Слишком простой пароль
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given
        LoginRequestDTO request = LoginRequestDTO.builder()
                .username("testuser")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void testLogin_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Given
        LoginRequestDTO request = LoginRequestDTO.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Неверные учетные данные"));
    }

    @Test
    void testLogin_UserNotFound_ReturnsUnauthorized() throws Exception {
        // Given
        LoginRequestDTO request = LoginRequestDTO.builder()
                .username("nonexistentuser")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Неверные учетные данные"));
    }

    @Test
    void testValidateToken_ValidToken_ReturnsValid() throws Exception {
        // Given - Сначала логинимся для получения токена
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .username("testuser")
                .password("password123")
                .build();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("accessToken").asText();

        // When & Then
        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void testValidateToken_NoToken_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void testLogout_Success() throws Exception {
        // Given - Сначала логинимся
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .username("testuser")
                .password("password123")
                .build();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Выход выполнен успешно"));
    }

    @Test
    void testLogout_NoBody_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Выход выполнен успешно"));
    }

    @Test
    void testForgotPassword_Success() throws Exception {
        // Given
        String requestJson = "{\"email\":\"test@example.com\"}";

        // When & Then
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Инструкция по восстановлению пароля отправлена на указанный email"));
    }

    @Test
    void testForgotPassword_NonexistentEmail_Success() throws Exception {
        // Given
        String requestJson = "{\"email\":\"nonexistent@example.com\"}";

        // When & Then
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Инструкция по восстановлению пароля отправлена на указанный email"));
    }

    @Test
    void testForgotPassword_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        String requestJson = "{\"email\":\"invalid-email\"}";

        // When & Then
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации"));
    }
}