package dn.quest.shared.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.config.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Базовый класс для интеграционных тестов микросервисов
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@Testcontainers
@Import(TestContainerConfig.class)
public abstract class MicroserviceIntegrationTestBase {

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

    @LocalServerPort
    protected int port;

    protected MockMvc mockMvc;
    protected String baseUrl;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        baseUrl = "http://localhost:" + port;
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
     * Создание JWT токена для тестов
     */
    protected String createTestToken(String username, String role) {
        // В реальной реализации здесь будет генерация JWT токена
        return "Bearer test-token-" + username + "-" + role;
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
        Thread.sleep(1000);
    }

    /**
     * Очистка тестовых данных после теста
     */
    protected abstract void cleanupTestData();
}