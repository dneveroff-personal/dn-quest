package dn.quest.shared.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Базовый класс для WebSocket тестов с поддержкой STOMP
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractWebSocketTestBase {

    @Autowired
    protected ObjectMapper objectMapper;

    @LocalServerPort
    protected int port;

    protected WebSocketStompClient stompClient;
    protected String webSocketUrl;
    protected static final int CONNECTION_TIMEOUT = 5; // seconds

    @BeforeEach
    void setUp() {
        webSocketUrl = "ws://localhost:" + port + "/ws";
        
        // Настройка STOMP клиента
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setConnectionTimeout(CONNECTION_TIMEOUT * 1000);
        
        log.info("WebSocket test initialized - URL: {}", webSocketUrl);
    }

    @AfterEach
    void tearDown() {
        // Очистка после тестов
        if (stompClient != null) {
            stompClient.stop();
        }
    }

    /**
     * Установка WebSocket соединения
     */
    protected StompSession connectWebSocket(String accessToken) throws Exception {
        return connectWebSocket(accessToken, "/user");
    }

    /**
     * Установка WebSocket соединения с указанным путем
     */
    protected StompSession connectWebSocket(String accessToken, String destination) throws Exception {
        CountDownLatch connectionLatch = new CountDownLatch(1);
        AtomicReference<StompSession> sessionRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        StompSessionHandlerAdapter handler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                log.info("WebSocket connected successfully");
                sessionRef.set(session);
                connectionLatch.countDown();
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                log.error("WebSocket transport error", exception);
                errorRef.set(exception);
                connectionLatch.countDown();
            }
        };

        // Добавление заголовка авторизации если предоставлен токен
        StompHeaders connectHeaders = new StompHeaders();
        if (accessToken != null && !accessToken.isEmpty()) {
            connectHeaders.add("Authorization", "Bearer " + accessToken);
        }

        stompClient.connect(webSocketUrl, connectHeaders, handler);

        // Ожидание подключения
        boolean connected = connectionLatch.await(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        
        if (!connected) {
            throw new RuntimeException("Failed to connect to WebSocket within timeout");
        }

        if (errorRef.get() != null) {
            throw new RuntimeException("WebSocket connection failed", errorRef.get());
        }

        return sessionRef.get();
    }

    /**
     * Подписка на топик с ожиданием сообщения
     */
    protected <T> T subscribeAndWaitForMessage(StompSession session, String destination, 
                                              Class<T> messageClass, long timeoutSeconds) throws Exception {
        CountDownLatch messageLatch = new CountDownLatch(1);
        AtomicReference<T> messageRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        StompFrameHandler handler = new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return messageClass;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    @SuppressWarnings("unchecked")
                    T message = (T) payload;
                    messageRef.set(message);
                    log.debug("Received message: {}", message);
                } catch (Exception e) {
                    errorRef.set(e);
                } finally {
                    messageLatch.countDown();
                }
            }
        };

        session.subscribe(destination, handler);

        // Ожидание сообщения
        boolean received = messageLatch.await(timeoutSeconds, TimeUnit.SECONDS);
        
        if (!received) {
            throw new RuntimeException("No message received within timeout");
        }

        if (errorRef.get() != null) {
            throw new RuntimeException("Error processing message", errorRef.get());
        }

        return messageRef.get();
    }

    /**
     * Отправка сообщения и ожидание ответа
     */
    protected <T> T sendMessageAndWaitForResponse(StompSession session, String destination, 
                                                  Object message, Class<T> responseClass, 
                                                  long timeoutSeconds) throws Exception {
        CountDownLatch responseLatch = new CountDownLatch(1);
        AtomicReference<T> responseRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        // Подписка на ответ
        String responseDestination = destination + "/response";
        StompFrameHandler responseHandler = new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return responseClass;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    @SuppressWarnings("unchecked")
                    T response = (T) payload;
                    responseRef.set(response);
                    log.debug("Received response: {}", response);
                } catch (Exception e) {
                    errorRef.set(e);
                } finally {
                    responseLatch.countDown();
                }
            }
        };

        session.subscribe(responseDestination, responseHandler);

        // Отправка сообщения
        session.send(destination, message);
        log.debug("Sent message to {}: {}", destination, message);

        // Ожидание ответа
        boolean received = responseLatch.await(timeoutSeconds, TimeUnit.SECONDS);
        
        if (!received) {
            throw new RuntimeException("No response received within timeout");
        }

        if (errorRef.get() != null) {
            throw new RuntimeException("Error processing response", errorRef.get());
        }

        return responseRef.get();
    }

    /**
     * Подписка на пользовательский топик
     */
    protected <T> T subscribeToUserTopic(StompSession session, String topic, 
                                        Class<T> messageClass, long timeoutSeconds) throws Exception {
        String destination = "/user/" + topic;
        return subscribeAndWaitForMessage(session, destination, messageClass, timeoutSeconds);
    }

    /**
     * Подписка на общий топик
     */
    protected <T> T subscribeToTopic(StompSession session, String topic, 
                                    Class<T> messageClass, long timeoutSeconds) throws Exception {
        String destination = "/topic/" + topic;
        return subscribeAndWaitForMessage(session, destination, messageClass, timeoutSeconds);
    }

    /**
     * Отправка сообщения в пользовательский топик
     */
    protected void sendToUserTopic(StompSession session, String topic, Object message) {
        String destination = "/user/" + topic;
        session.send(destination, message);
        log.debug("Sent message to user topic {}: {}", destination, message);
    }

    /**
     * Отправка сообщения в общий топик
     */
    protected void sendToTopic(StompSession session, String topic, Object message) {
        String destination = "/topic/" + topic;
        session.send(destination, message);
        log.debug("Sent message to topic {}: {}", destination, message);
    }

    /**
     * Отправка сообщения в приложение
     */
    protected void sendToApp(StompSession session, String endpoint, Object message) {
        String destination = "/app/" + endpoint;
        session.send(destination, message);
        log.debug("Sent message to app {}: {}", destination, message);
    }

    /**
     * Проверка активности WebSocket соединения
     */
    protected boolean isSessionActive(StompSession session) {
        return session != null && session.isConnected();
    }

    /**
     * Отключение от WebSocket
     */
    protected void disconnectWebSocket(StompSession session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.info("WebSocket disconnected");
        }
    }

    /**
     * Ожидание подключения нескольких клиентов
     */
    protected void waitForConnections(StompSession... sessions) throws InterruptedException {
        for (StompSession session : sessions) {
            int attempts = 0;
            while (!isSessionActive(session) && attempts < CONNECTION_TIMEOUT * 10) {
                Thread.sleep(100);
                attempts++;
            }
            
            if (!isSessionActive(session)) {
                throw new RuntimeException("Failed to establish WebSocket connection");
            }
        }
    }

    /**
     * Создание тестового сообщения
     */
    protected Object createTestMessage(String type, Object data) {
        return new TestMessage(type, data);
    }

    /**
     * Тестовое сообщение для WebSocket
     */
    public static class TestMessage {
        private final String type;
        private final Object data;
        private final long timestamp;

        public TestMessage(String type, Object data) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        public String getType() { return type; }
        public Object getData() { return data; }
        public long getTimestamp() { return timestamp; }
    }

    /**
     * Обработчик для отслеживания нескольких сообщений
     */
    protected static class MultiMessageHandler<T> implements StompFrameHandler {
        private final Class<T> messageClass;
        private final CountDownLatch latch;
        private final AtomicReference<T> lastMessage;
        private final AtomicReference<Throwable> error;

        public MultiMessageHandler(Class<T> messageClass, int expectedMessages) {
            this.messageClass = messageClass;
            this.latch = new CountDownLatch(expectedMessages);
            this.lastMessage = new AtomicReference<>();
            this.error = new AtomicReference<>();
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return messageClass;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            try {
                @SuppressWarnings("unchecked")
                T message = (T) payload;
                lastMessage.set(message);
                log.debug("Received message: {}", message);
            } catch (Exception e) {
                error.set(e);
            } finally {
                latch.countDown();
            }
        }

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return latch.await(timeout, unit);
        }

        public T getLastMessage() {
            return lastMessage.get();
        }

        public Throwable getError() {
            return error.get();
        }
    }
}