package dn.quest.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.shared.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Обработчик WebSocket соединений с аутентификацией через JWT
 * Обрабатывает подключения по адресу /ws
 */
public class WebSocketAuthHandler implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthHandler.class);
    
    private final JwtUtil jwtUtil;
    private final String jwtSecret;
    private final ObjectMapper objectMapper;
    
    // Хранилище активных сессий по userId
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public WebSocketAuthHandler(JwtUtil jwtUtil, @Value("${jwt.secret}") String jwtSecret) {
        this.jwtUtil = jwtUtil;
        this.jwtSecret = jwtSecret;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // Извлекаем токен из query параметров
        String token = extractTokenFromQuery(session);
        
        if (token == null || !JwtUtil.validateToken(token, jwtSecret)) {
            log.warn("WebSocket connection rejected: invalid or missing token");
            return session.close(CloseStatus.POLICY_VIOLATION);
        }

        UUID userId = JwtUtil.extractUserId(token, jwtSecret);
        String username = JwtUtil.extractUsername(token, jwtSecret);
        
        log.info("WebSocket connection established for user: {}", username);
        
        // Сохраняем сессию
        if (userId != null) {
            sessions.put(userId.toString(), session);
        }
        
        // Отправляем приветственное сообщение
        Map<String, Object> welcomeMessage = Map.of(
            "type", "CONNECTED",
            "data", Map.of(
                "userId", userId != null ? userId.toString() : null,
                "username", username,
                "message", "WebSocket connection established successfully"
            ),
            "timestamp", System.currentTimeMillis()
        );
        
        // Отправляем приветственное сообщение
        Mono<Void> sendWelcome = session.send(Flux.just(session.textMessage(toJson(welcomeMessage))));
        
        // Обрабатываем входящие сообщения
        Mono<Void> receiveMessages = session.receive()
            .doOnNext(message -> {
                // Обрабатываем входящие сообщения
                handleMessage(session, message.getPayloadAsText());
            })
            .then();
        
        // Запускаем обе операции параллельно
        return Mono.when(sendWelcome, receiveMessages)
            .doFinally(signalType -> {
                // Удаляем сессию при закрытии
                if (userId != null) {
                    sessions.remove(userId.toString());
                }
                log.info("WebSocket connection closed for user: {}", username);
            });
    }

    private String extractTokenFromQuery(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        if (query == null) return null;
        
        for (String param : query.split("&")) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }

    private void handleMessage(WebSocketSession session, String payload) {
        try {
            Map<String, Object> message = objectMapper.readValue(payload, Map.class);
            String type = (String) message.get("type");
            
            log.debug("Received WebSocket message of type: {}", type);
            
            // Обрабатываем PING сообщения
            if ("PING".equals(type)) {
                Map<String, Object> pongMessage = Map.of(
                    "type", "PONG",
                    "data", Map.of("timestamp", System.currentTimeMillis()),
                    "timestamp", System.currentTimeMillis()
                );
                session.send(Mono.just(session.textMessage(toJson(pongMessage)))).subscribe();
            }
            
            // Здесь можно добавить обработку других типов сообщений
            // и маршрутизацию на соответствующие сервисы
            
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Error converting to JSON", e);
            return "{}";
        }
    }
}
