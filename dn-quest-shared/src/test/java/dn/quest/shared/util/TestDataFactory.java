package dn.quest.shared.util;

import dn.quest.authentication.dto.LoginRequestDTO;
import dn.quest.authentication.dto.RegisterRequestDTO;
import dn.quest.authentication.entity.User;
import dn.quest.shared.dto.auth.LoginResponseDTO;
import dn.quest.shared.dto.auth.RegisterDTO;
import dn.quest.shared.enums.UserRole;
import dn.quest.shared.events.user.UserRegisteredEvent;
import dn.quest.shared.events.user.UserUpdatedEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Фабрика для создания тестовых данных
 */
public class TestDataFactory {

    /**
     * Создание тестового пользователя
     */
    public static User createTestUser(String username, String email, UserRole role) {
        return User.builder()
                .id(1L)
                .username(username)
                .passwordHash("hashedPassword")
                .email(email)
                .publicName("Test User " + username)
                .role(role)
                .isActive(true)
                .isEmailVerified(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Создание тестового DTO для регистрации
     */
    public static RegisterRequestDTO createRegisterRequestDTO(String username, String email) {
        return RegisterRequestDTO.builder()
                .username(username)
                .email(email)
                .publicName("Test User " + username)
                .password("Password123")
                .build();
    }

    /**
     * Создание тестового DTO для входа
     */
    public static LoginRequestDTO createLoginRequestDTO(String username) {
        return LoginRequestDTO.builder()
                .username(username)
                .password("Password123")
                .build();
    }

    /**
     * Создание тестового ответа для входа
     */
    public static LoginResponseDTO createLoginResponseDTO(String username, String role) {
        return LoginResponseDTO.builder()
                .accessToken("test-access-token-" + UUID.randomUUID())
                .refreshToken("test-refresh-token-" + UUID.randomUUID())
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(dn.quest.shared.dto.UserDTO.builder()
                        .id(1L)
                        .username(username)
                        .email(username + "@test.com")
                        .publicName("Test User " + username)
                        .role(UserRole.valueOf(role))
                        .isActive(true)
                        .isEmailVerified(false)
                        .createdAt(Instant.now())
                        .build())
                .build();
    }

    /**
     * Создание тестового события регистрации пользователя
     */
    public static UserRegisteredEvent createUserRegisteredEvent(String username, String email) {
        return UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_REGISTERED")
                .timestamp(Instant.now())
                .userId(1L)
                .username(username)
                .email(email)
                .role(UserRole.PLAYER)
                .build();
    }

    /**
     * Создание тестового события обновления пользователя
     */
    public static UserUpdatedEvent createUserUpdatedEvent(Long userId, String username) {
        return UserUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_UPDATED")
                .timestamp(Instant.now())
                .userId(userId)
                .username(username)
                .build();
    }

    /**
     * Создание тестового JWT токена
     */
    public static String createTestJwtToken(String username, String role) {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI" + username + 
               "In0.test-signature-" + role;
    }

    /**
     * Создание тестовых заголовков
     */
    public static String createTestAuthHeader(String token) {
        return "Bearer " + token;
    }

    /**
     * Создание тестового UUID
     */
    public static String createTestUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Создание тестовой временной метки
     */
    public static Instant createTestTimestamp() {
        return Instant.now();
    }

    /**
     * Создание тестовой строки JSON
     */
    public static String createTestJson(String key, String value) {
        return String.format("{\"%s\":\"%s\"}", key, value);
    }

    /**
     * Создание тестового email
     */
    public static String createTestEmail(String username) {
        return username + "@test.com";
    }

    /**
     * Создание тестового пароля
     */
    public static String createTestPassword() {
        return "Password123!";
    }

    /**
     * Создание тестового имени пользователя
     */
    public static String createTestUsername(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }
}