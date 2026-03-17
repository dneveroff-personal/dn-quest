package dn.quest.shared.util;

import dn.quest.authentication.dto.*;
import dn.quest.authentication.entity.User;
import dn.quest.shared.dto.auth.LoginResponseDTO;
import dn.quest.shared.dto.auth.RegisterDTO;
import dn.quest.shared.enums.*;
import dn.quest.shared.events.BaseEvent;
import dn.quest.shared.events.file.*;
import dn.quest.shared.events.game.*;
import dn.quest.shared.events.notification.NotificationEvent;
import dn.quest.shared.events.quest.*;
import dn.quest.shared.events.team.*;
import dn.quest.shared.events.user.*;

import java.time.Instant;
import java.util.*;

/**
 * Улучшенная фабрика для создания комплексных тестовых данных
 * с поддержкой всех типов сущностей и событий
 */
public class EnhancedTestDataFactory {

    private static final Random RANDOM = new Random();
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_EMAIL_DOMAIN = "@test.com";

    // ========== Пользователи ==========

    /**
     * Создание тестового пользователя
     */
    public static User createTestUser(String username, String email, UserRole role) {
        return User.builder()
                .id(RANDOM.nextLong(1000) + 1)
                .username(username)
                .passwordHash("hashedPassword")
                .email(email)
                .publicName("Test User " + username)
                .role(role)
                .isActive(true)
                .isEmailVerified(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .lastLoginAt(Instant.now())
                .build();
    }

    /**
     * Создание тестового пользователя с полными данными
     */
    public static User createFullTestUser(String username, UserRole role) {
        return User.builder()
                .id(RANDOM.nextLong(1000) + 1)
                .username(username)
                .passwordHash("hashedPassword")
                .email(username + TEST_EMAIL_DOMAIN)
                .publicName("Full Test User " + username)
                .role(role)
                .isActive(true)
                .isEmailVerified(true)
                .createdAt(Instant.now().minusSeconds(3600))
                .updatedAt(Instant.now().minusSeconds(1800))
                .lastLoginAt(Instant.now().minusSeconds(300))
                .profilePictureUrl("https://example.com/avatars/" + username + ".jpg")
                .bio("Test bio for " + username)
                .location("Test City")
                .website("https://" + username + ".example.com")
                .build();
    }

    /**
     * Создание DTO для регистрации
     */
    public static RegisterRequestDTO createRegisterRequestDTO(String username, String email) {
        return RegisterRequestDTO.builder()
                .username(username)
                .email(email)
                .publicName("Test User " + username)
                .password(TEST_PASSWORD)
                .build();
    }

    /**
     * Создание DTO для входа
     */
    public static LoginRequestDTO createLoginRequestDTO(String username) {
        return LoginRequestDTO.builder()
                .username(username)
                .password(TEST_PASSWORD)
                .build();
    }

    /**
     * Создание DTO для обновления профиля
     */
    public static UpdateProfileRequestDTO createUpdateProfileRequestDTO(String username) {
        return UpdateProfileRequestDTO.builder()
                .publicName("Updated User " + username)
                .email("updated." + username + TEST_EMAIL_DOMAIN)
                .bio("Updated bio")
                .location("Updated City")
                .website("https://updated." + username + ".example.com")
                .build();
    }

    // ========== Квесты ==========

    /**
     * Создание DTO для создания квеста
     */
    public static Map<String, Object> createQuestRequestDTO(String title) {
        Map<String, Object> quest = new HashMap<>();
        quest.put("title", title);
        quest.put("description", "Test quest description for " + title);
        quest.put("difficulty", "EASY");
        quest.put("questType", "SOLO");
        quest.put("estimatedDuration", 30);
        quest.put("maxParticipants", 1);
        quest.put("published", false);
        
        List<Map<String, Object>> levels = new ArrayList<>();
        Map<String, Object> level = new HashMap<>();
        level.put("title", "Level 1");
        level.put("description", "First level of " + title);
        level.put("orderIndex", 1);
        
        List<Map<String, Object>> codes = new ArrayList<>();
        Map<String, Object> code = new HashMap<>();
        code.put("value", "TEST123");
        code.put("type", "TEXT");
        code.put("points", 100);
        codes.add(code);
        
        level.put("codes", codes);
        levels.add(level);
        quest.put("levels", levels);
        
        return quest;
    }

    /**
     * Создание сложного квеста с несколькими уровнями
     */
    public static Map<String, Object> createComplexQuestRequestDTO(String title) {
        Map<String, Object> quest = createQuestRequestDTO(title);
        quest.put("difficulty", "MEDIUM");
        quest.put("estimatedDuration", 60);
        quest.put("maxParticipants", 4);
        quest.put("questType", "TEAM");
        
        List<Map<String, Object>> levels = new ArrayList<>();
        
        // Уровень 1
        Map<String, Object> level1 = new HashMap<>();
        level1.put("title", "Level 1");
        level1.put("description", "First level");
        level1.put("orderIndex", 1);
        
        List<Map<String, Object>> codes1 = new ArrayList<>();
        Map<String, Object> code1 = new HashMap<>();
        code1.put("value", "LEVEL1_CODE");
        code1.put("type", "TEXT");
        code1.put("points", 100);
        codes1.add(code1);
        level1.put("codes", codes1);
        levels.add(level1);
        
        // Уровень 2
        Map<String, Object> level2 = new HashMap<>();
        level2.put("title", "Level 2");
        level2.put("description", "Second level");
        level2.put("orderIndex", 2);
        
        List<Map<String, Object>> codes2 = new ArrayList<>();
        Map<String, Object> code2 = new HashMap<>();
        code2.put("value", "LEVEL2_CODE");
        code2.put("type", "QR");
        code2.put("points", 200);
        codes2.add(code2);
        level2.put("codes", codes2);
        levels.add(level2);
        
        quest.put("levels", levels);
        return quest;
    }

    // ========== Игровые сессии ==========

    /**
     * Создание DTO для начала игровой сессии
     */
    public static Map<String, Object> createGameSessionRequestDTO(Long questId) {
        Map<String, Object> session = new HashMap<>();
        session.put("questId", questId);
        return session;
    }

    /**
     * Создание DTO для отправки кода
     */
    public static Map<String, Object> createCodeSubmissionDTO(String rawCode, Long userId) {
        Map<String, Object> submission = new HashMap<>();
        submission.put("rawCode", rawCode);
        submission.put("userId", userId);
        return submission;
    }

    // ========== Команды ==========

    /**
     * Создание DTO для создания команды
     */
    public static Map<String, Object> createTeamRequestDTO(String name) {
        Map<String, Object> team = new HashMap<>();
        team.put("name", name);
        team.put("description", "Test team description for " + name);
        team.put("maxMembers", 5);
        return team;
    }

    // ========== Файлы ==========

    /**
     * Создание DTO для загрузки файла
     */
    public static Map<String, Object> createFileUploadRequestDTO(String fileName) {
        Map<String, Object> file = new HashMap<>();
        file.put("fileName", fileName);
        file.put("fileSize", 1024L);
        file.put("mimeType", "text/plain");
        file.put("content", "dGVzdCBjb250ZW50"); // Base64 encoded "test content"
        return file;
    }

    /**
     * Создание DTO для пакетной загрузки файлов
     */
    public static Map<String, Object> createBatchFileUploadRequestDTO() {
        Map<String, Object> batch = new HashMap<>();
        List<Map<String, Object>> files = new ArrayList<>();
        
        files.add(createFileUploadRequestDTO("test1.txt"));
        files.add(createFileUploadRequestDTO("test2.txt"));
        files.add(createFileUploadRequestDTO("test3.txt"));
        
        batch.put("files", files);
        return batch;
    }

    // ========== События ==========

    /**
     * Создание базового события
     */
    public static BaseEvent createBaseEvent(String eventType, String source) {
        return BaseEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .timestamp(Instant.now())
                .source(source)
                .correlationId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Создание события регистрации пользователя
     */
    public static UserRegisteredEvent createUserRegisteredEvent(String username, String email) {
        return UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_REGISTERED")
                .timestamp(Instant.now())
                .userId(RANDOM.nextLong(1000) + 1)
                .username(username)
                .email(email)
                .role(UserRole.PLAYER)
                .source("authentication-service")
                .correlationId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Создание события создания квеста
     */
    public static QuestCreatedEvent createQuestCreatedEvent(Long questId, String title) {
        return QuestCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("QUEST_CREATED")
                .timestamp(Instant.now())
                .questId(questId)
                .title(title)
                .description("Test quest description")
                .difficulty(Difficulty.EASY)
                .questType(QuestType.SOLO)
                .creatorId(RANDOM.nextLong(1000) + 1)
                .source("quest-management-service")
                .correlationId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Создание события начала игровой сессии
     */
    public static GameSessionStartedEvent createGameSessionStartedEvent(String sessionId, Long questId, Long userId) {
        return GameSessionStartedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("GAME_SESSION_STARTED")
                .timestamp(Instant.now())
                .sessionId(sessionId)
                .questId(questId)
                .userId(userId)
                .status(SessionStatus.ACTIVE)
                .source("game-engine-service")
                .correlationId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Создание события отправки кода
     */
    public static CodeSubmittedEvent createCodeSubmittedEvent(String sessionId, String code, AttemptResult result) {
        return CodeSubmittedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CODE_SUBMITTED")
                .timestamp(Instant.now())
                .sessionId(sessionId)
                .code(code)
                .result(result)
                .pointsEarned(result == AttemptResult.ACCEPTED_NORMAL ? 100 : 0)
                .userId(RANDOM.nextLong(1000) + 1)
                .source("game-engine-service")
                .correlationId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Создание события создания команды
     */
    public static TeamCreatedEvent createTeamCreatedEvent(Long teamId, String name, Long creatorId) {
        return TeamCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TEAM_CREATED")
                .timestamp(Instant.now())
                .teamId(teamId)
                .name(name)
                .description("Test team description")
                .creatorId(creatorId)
                .source("team-management-service")
                .correlationId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Создание события загрузки файла
     */
    public static FileUploadedEvent createFileUploadedEvent(String fileId, String fileName, Long userId) {
        return FileUploadedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("FILE_UPLOADED")
                .timestamp(Instant.now())
                .fileId(fileId)
                .fileName(fileName)
                .fileSize(1024L)
                .mimeType("text/plain")
                .userId(userId)
                .source("file-storage-service")
                .correlationId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Создание уведомления
     */
    public static NotificationEvent createNotificationEvent(Long userId, String title, String message) {
        return NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("NOTIFICATION_CREATED")
                .timestamp(Instant.now())
                .userId(userId)
                .title(title)
                .message(message)
                .type("INFO")
                .source("notification-service")
                .correlationId(UUID.randomUUID().toString())
                .build();
    }

    // ========== Вспомогательные методы ==========

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
        return username + TEST_EMAIL_DOMAIN;
    }

    /**
     * Создание тестового пароля
     */
    public static String createTestPassword() {
        return TEST_PASSWORD;
    }

    /**
     * Создание тестового имени пользователя
     */
    public static String createTestUsername(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }

    /**
     * Создание случайного числа в диапазоне
     */
    public static int randomInt(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    /**
     * Создание случайной строки
     */
    public static String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Создание тестового набора данных для нагрузочных тестов
     */
    public static List<User> createTestUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String username = "loaduser" + i;
            users.add(createTestUser(username, createTestEmail(username), UserRole.PLAYER));
        }
        return users;
    }

    /**
     * Создание тестового набора квестов
     */
    public static List<Map<String, Object>> createTestQuests(int count) {
        List<Map<String, Object>> quests = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            quests.add(createQuestRequestDTO("Test Quest " + i));
        }
        return quests;
    }

    /**
     * Создание тестового набора событий
     */
    public static List<BaseEvent> createTestEvents(int count) {
        List<BaseEvent> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            events.add(createBaseEvent("TEST_EVENT_" + i, "test-source"));
        }
        return events;
    }
}