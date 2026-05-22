package dn.quest.gateway.config;

import dn.quest.shared.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация WebSocket для API Gateway
 * Обрабатывает подключения по адресу /ws с аутентификацией через JWT токен
 */
@Configuration
public class WebSocketConfig {

    @Bean
    public WebSocketHandler webSocketHandler(JwtUtil jwtUtil, @Value("${jwt.secret}") String jwtSecret) {
        return new WebSocketAuthHandler(jwtUtil, jwtSecret);
    }

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    public SimpleUrlHandlerMapping simpleUrlHandlerMapping(WebSocketHandler webSocketHandler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws", webSocketHandler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(1);
        mapping.setUrlMap(map);
        return mapping;
    }
}
