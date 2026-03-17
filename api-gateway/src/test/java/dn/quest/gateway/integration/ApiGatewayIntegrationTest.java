package dn.quest.gateway.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.base.AbstractIntegrationTestBase;
import dn.quest.shared.util.EnhancedTestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Интеграционные тесты для API Gateway
 */
class ApiGatewayIntegrationTest extends AbstractIntegrationTestBase {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void cleanupTestData() {
        // Очистка тестовых данных для API Gateway
    }

    @Test
    void testRouteToAuthenticationService_Success() throws Exception {
        // Given
        var loginRequest = new java.util.HashMap<String, Object>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "password123");

        // When & Then
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(loginRequest))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").exists()
                .jsonPath("$.refreshToken").exists()
                .jsonPath("$.expiresIn").exists();
    }

    @Test
    void testRouteToQuestManagementService_Success() throws Exception {
        // Given
        String token = getValidToken();
        var questRequest = new java.util.HashMap<String, Object>();
        questRequest.put("title", "Test Quest");
        questRequest.put("description", "Test quest description");
        questRequest.put("difficulty", "EASY");
        questRequest.put("estimatedDuration", 30);
        questRequest.put("category", "ADVENTURE");

        // When & Then
        webTestClient.post()
                .uri("/api/quests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(BodyInserters.fromValue(questRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.title").value("Test Quest")
                .jsonPath("$.description").value("Test quest description")
                .jsonPath("$.difficulty").value("EASY");
    }

    @Test
    void testRouteToGameEngineService_Success() throws Exception {
        // Given
        String token = getValidToken();
        var sessionRequest = new java.util.HashMap<String, Object>();
        sessionRequest.put("questId", 1L);
        sessionRequest.put("teamId", null);

        // When & Then
        webTestClient.post()
                .uri("/api/game-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(BodyInserters.fromValue(sessionRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.questId").value(1)
                .jsonPath("$.status").value("ACTIVE");
    }

    @Test
    void testRouteToTeamManagementService_Success() throws Exception {
        // Given
        String token = getValidToken();
        var teamRequest = new java.util.HashMap<String, Object>();
        teamRequest.put("name", "Test Team");
        teamRequest.put("description", "Test team description");
        teamRequest.put("maxMembers", 5);

        // When & Then
        webTestClient.post()
                .uri("/api/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(BodyInserters.fromValue(teamRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").value("Test Team")
                .jsonPath("$.description").value("Test team description");
    }

    @Test
    void testRouteToFileStorageService_Success() throws Exception {
        // Given
        String token = getValidToken();
        var fileRequest = new java.util.HashMap<String, Object>();
        fileRequest.put("fileName", "test.jpg");
        fileRequest.put("contentType", "image/jpeg");
        fileRequest.put("category", "AVATAR");

        // When & Then
        webTestClient.post()
                .uri("/api/files/presigned-upload-url")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(BodyInserters.fromValue(fileRequest))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.uploadUrl").exists()
                .jsonPath("$.fileName").value("test.jpg");
    }

    @Test
    void testRouteToNotificationService_Success() throws Exception {
        // Given
        String token = getValidToken();
        var notificationRequest = new java.util.HashMap<String, Object>();
        notificationRequest.put("userId", 1L);
        notificationRequest.put("type", "QUEST_COMPLETED");
        notificationRequest.put("title", "Quest Completed!");
        notificationRequest.put("message", "Congratulations! You have completed the quest.");

        // When & Then
        webTestClient.post()
                .uri("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(BodyInserters.fromValue(notificationRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.type").value("QUEST_COMPLETED");
    }

    @Test
    void testRouteToStatisticsService_Success() throws Exception {
        // Given
        String token = getValidToken();

        // When & Then
        webTestClient.get()
                .uri("/api/statistics/user/1")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").value(1)
                .jsonPath("$.totalQuests").exists()
                .jsonPath("$.completedQuests").exists();
    }

    @Test
    void testRouteToUserManagementService_Success() throws Exception {
        // Given
        String token = getValidToken();

        // When & Then
        webTestClient.get()
                .uri("/api/users/profile")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.username").exists()
                .jsonPath("$.email").exists();
    }

    @Test
    void testCircuitBreaker_OpenState_ReturnsServiceUnavailable() throws Exception {
        // Given - Симулируем открытие circuit breaker
        // В реальном тесте здесь нужно настроить circuit breaker для открытия

        // When & Then
        webTestClient.get()
                .uri("/api/quests/99999")
                .header("Authorization", "Bearer " + getValidToken())
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").value("Service temporarily unavailable");
    }

    @Test
    void testRateLimit_Exceeded_ReturnsTooManyRequests() throws Exception {
        // Given - Отправляем множество запросов для превышения лимита
        String token = getValidToken();

        // When & Then - Отправляем запросы быстро для превышения лимита
        for (int i = 0; i < 20; i++) {
            webTestClient.get()
                    .uri("/api/quests")
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus().isOk();
        }

        // Следующий запрос должен быть отклонен из-за rate limiting
        webTestClient.get()
                .uri("/api/quests")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isTooManyRequests()
                .expectBody()
                .jsonPath("$.message").value("Rate limit exceeded");
    }

    @Test
    void testInvalidToken_ReturnsUnauthorized() throws Exception {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        webTestClient.get()
                .uri("/api/users/profile")
                .header("Authorization", "Bearer " + invalidToken)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").value("Invalid or expired token");
    }

    @Test
    void testMissingToken_ReturnsUnauthorized() throws Exception {
        // When & Then
        webTestClient.get()
                .uri("/api/users/profile")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").value("Authorization header is missing");
    }

    @Test
    void testCorsHeaders_Success() throws Exception {
        // When & Then - Preflight request
        webTestClient.options()
                .uri("/api/quests")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization, Content-Type")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Access-Control-Allow-Origin")
                .expectHeader().exists("Access-Control-Allow-Methods")
                .expectHeader().exists("Access-Control-Allow-Headers");
    }

    @Test
    void testRequestLogging_Success() throws Exception {
        // Given
        String token = getValidToken();

        // When & Then
        webTestClient.get()
                .uri("/api/quests")
                .header("Authorization", "Bearer " + token)
                .header("X-Request-ID", "test-request-id")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("X-Request-ID");
    }

    @Test
    void testHealthCheck_Success() throws Exception {
        // When & Then
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").value("UP");
    }

    @Test
    void testGatewayHealth_Success() throws Exception {
        // When & Then
        webTestClient.get()
                .uri("/api/gateway/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").value("UP")
                .jsonPath("$.services").exists();
    }

    @Test
    void testMetricsEndpoint_Success() throws Exception {
        // When & Then
        webTestClient.get()
                .uri("/actuator/metrics")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.names").isArray();
    }

    @Test
    void testInvalidRoute_ReturnsNotFound() throws Exception {
        // Given
        String token = getValidToken();

        // When & Then
        webTestClient.get()
                .uri("/api/invalid-endpoint")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").value("Endpoint not found");
    }

    @Test
    void testLargeRequest_ReturnsPayloadTooLarge() throws Exception {
        // Given
        String token = getValidToken();
        var largeRequest = new java.util.HashMap<String, Object>();
        largeRequest.put("title", "A".repeat(10000)); // Очень большой заголовок
        largeRequest.put("description", "B".repeat(100000)); // Очень большое описание

        // When & Then
        webTestClient.post()
                .uri("/api/quests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(BodyInserters.fromValue(largeRequest))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void testMalformedJson_ReturnsBadRequest() throws Exception {
        // Given
        String token = getValidToken();
        String malformedJson = "{title: \"Test\", description: \"Test\"}"; // Невалидный JSON

        // When & Then
        webTestClient.post()
                .uri("/api/quests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .body(BodyInserters.fromValue(malformedJson))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value("Malformed JSON request");
    }

    @Test
    void testUnsupportedMediaType_ReturnsUnsupportedMediaType() throws Exception {
        // Given
        String token = getValidToken();

        // When & Then
        webTestClient.post()
                .uri("/api/quests")
                .contentType(MediaType.TEXT_PLAIN)
                .header("Authorization", "Bearer " + token)
                .body(BodyInserters.fromValue("plain text"))
                .exchange()
                .expectStatus().isUnsupportedMediaType()
                .expectBody()
                .jsonPath("$.message").value("Unsupported media type");
    }

    @Test
    void testTimeout_ReturnsGatewayTimeout() throws Exception {
        // Given - Симулируем долгий ответ от сервиса
        String token = getValidToken();

        // When & Then
        webTestClient.get()
                .uri("/api/quests/slow-endpoint") // Предполагаем, что такой эндпоинт существует
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void testRetryMechanism_Success() throws Exception {
        // Given - Симулируем временный сбой сервиса
        String token = getValidToken();

        // When & Then - Gateway должен повторить запрос
        webTestClient.get()
                .uri("/api/quests/retry-test") // Предполагаем, что такой эндпоинт существует
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testLoadBalancing_Success() throws Exception {
        // Given
        String token = getValidToken();

        // When & Then - Отправляем несколько запросов для проверки балансировки нагрузки
        for (int i = 0; i < 5; i++) {
            webTestClient.get()
                    .uri("/api/quests")
                    .header("Authorization", "Bearer " + token)
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    @Test
    void testWebSocketUpgrade_Success() throws Exception {
        // When & Then
        webTestClient.get()
                .uri("/ws/game-updates")
                .header("Upgrade", "websocket")
                .header("Connection", "Upgrade")
                .header("Sec-WebSocket-Key", "dGhlIHNhbXBsZSBub25jZQ==")
                .header("Sec-WebSocket-Version", "13")
                .exchange()
                .expectStatus().isSwitchingProtocols()
                .expectHeader().exists("Upgrade")
                .expectHeader().valueEquals("Upgrade", "websocket");
    }

    @Test
    void testServiceDiscovery_Success() throws Exception {
        // When & Then
        webTestClient.get()
                .uri("/actuator/services")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.services").isArray();
    }

    @Test
    void testConfigurationRefresh_Success() throws Exception {
        // When & Then
        webTestClient.post()
                .uri("/actuator/refresh")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.changedProperties").exists();
    }

    /**
     * Вспомогательный метод для получения валидного токена
     */
    private String getValidToken() {
        // В реальном тесте здесь нужно получить токен от authentication service
        // Для упрощения возвращаем mock токен
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }
}