package dn.quest.shared.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.config.TestContainerConfig;
import dn.quest.shared.util.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Расширенный базовый класс для интеграционных тестов микросервисов
 * с поддержкой WebSocket, Kafka, Redis и улучшенной конфигурацией TestContainers
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@Testcontainers
@Import({TestContainerConfig.class})
public abstract class AbstractIntegrationTestBase {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dnquest_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    @LocalServerPort
    protected int port;

    @MockBean
    protected KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    protected EmbeddedKafkaBroker embeddedKafka;

    protected MockMvc mockMvc;
    protected String baseUrl;
    protected WebSocketStompClient webSocketStompClient;
    protected StandardWebSocketClient webSocketClient;

    protected static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    protected static final String TEST_USER_TOKEN = "test-user-token";
    protected static final String TEST_ADMIN_TOKEN = "test-admin-token";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
    }

    @BeforeEach
    void setUp() {
        // Настройка MockMvc с поддержкой безопасности
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        baseUrl = "http://localhost:" + port;

        // Настройка WebSocket клиента
        webSocketClient = new StandardWebSocketClient();
        webSocketStompClient = new WebSocketStompClient(webSocketClient);
        webSocketStompClient.setMessageConverter(new org.springframework.messaging.converter.MappingJackson2MessageConverter());

        // Мокирование Kafka шаблона
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(org.springframework.kafka.support.SendResult.failed());

        // Очистка Redis перед тестами
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        } catch (Exception e) {
            // Игнорируем ошибки, если Redis недоступен
        }

        // Инициализация тестовых данных
        setupTestData();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
        
        // Очистка Redis после тестов
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        } catch (Exception e) {
            // Игнорируем ошибки, если Redis недоступен
        }
    }

    /**
     * Получение базового URL для тестов
     */
    protected String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Получение URL для конкретного эндпоинта
     */
    protected String getEndpointUrl(String endpoint) {
        return baseUrl + endpoint;
    }

    /**
     * Получение WebSocket URL
     */
    protected String getWebSocketUrl(String path) {
        return "ws://localhost:" + port + path;
    }

    /**
     * Создание JWT токена для тестов
     */
    protected String createTestToken(String username, String role) {
        if ("ADMIN".equals(role)) {
            return TEST_ADMIN_TOKEN;
        }
        return TEST_USER_TOKEN;
    }

    /**
     * Создание заголовков авторизации
     */
    protected String createAuthHeaders(String token) {
        return "Bearer " + token;
    }

    /**
     * Ожидание выполнения асинхронных операций
     */
    protected void waitForAsyncOperations() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(500);
    }

    /**
     * Ожидание обработки сообщений в Kafka
     */
    protected void waitForKafkaMessageProcessing() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
    }

    /**
     * Ожидание обработки сообщений в Redis
     */
    protected void waitForRedisOperation() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /**
     * Проверка наличия данных в Redis
     */
    protected boolean existsInRedis(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Получение данных из Redis
     */
    protected Object getFromRedis(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Сохранение данных в Redis
     */
    protected void saveToRedis(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(5));
        } catch (Exception e) {
            // Игнорируем ошибки, если Redis недоступен
        }
    }

    /**
     * Создание тестового пользователя с правами доступа
     */
    protected org.springframework.security.core.userdetails.User createTestUser(String username, String role) {
        return new org.springframework.security.core.userdetails.User(
                username,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    /**
     * Мокирование внешних сервисов
     */
    protected void mockExternalServices() {
        // Мокирование внешних API вызовов
        // Переопределяется в конкретных тестах
    }

    /**
     * Настройка тестовых данных перед каждым тестом
     */
    protected void setupTestData() {
        // Переопределяется в конкретных тестах
    }

    /**
     * Очистка тестовых данных после каждого теста
     */
    protected abstract void cleanupTestData();

    /**
     * Проверка состояния контейнеров
     */
    protected boolean areContainersReady() {
        return postgres.isRunning();
    }

    /**
     * Получение информации о тестовом окружении
     */
    protected String getTestEnvironmentInfo() {
        return String.format(
                "Test Environment - PostgreSQL: %s:%d, Port: %d, Profile: test",
                postgres.getHost(),
                postgres.getMappedPort(5432),
                port
        );
    }
}