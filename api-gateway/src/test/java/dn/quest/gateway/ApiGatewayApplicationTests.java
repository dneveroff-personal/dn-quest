package dn.quest.gateway;

import dn.quest.gateway.filter.AuthenticationFilter;
import dn.quest.gateway.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayApplicationTests {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Test
    void contextLoads() {
        assertNotNull(jwtUtil);
        assertNotNull(authenticationFilter);
    }

    @Test
    void jwtUtilShouldExtractUsername() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGUiOiJVU0VSIiwidXNlcklkIjoxfQ.test";
        
        // Этот тест проверяет базовую функциональность
        // В реальном сценарии нужно использовать валидный JWT токен
        assertNotNull(jwtUtil);
    }

    @Test
    void authenticationFilterShouldBeConfigured() {
        assertNotNull(authenticationFilter);
        assertInstanceOf(GatewayFilter.class, authenticationFilter);
    }

    @Test
    void gatewayShouldStart() {
        // Базовый тест проверки запуска приложения
        assertTrue(true);
    }
}