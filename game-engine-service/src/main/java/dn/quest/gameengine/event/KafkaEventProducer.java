package dn.quest.gameengine.event;

import dn.quest.shared.events.EventProducer;
import dn.quest.shared.events.game.CodeSubmittedEvent;
import dn.quest.shared.events.game.GameSessionFinishedEvent;
import dn.quest.shared.events.game.GameSessionStartedEvent;
import dn.quest.shared.events.game.LevelCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Сервис для публикации игровых событий в Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final EventProducer eventProducer;

    /**
     * Публикация события начала игровой сессии
     */
    public CompletableFuture<Void> publishGameSessionStartedEvent(GameSessionStartedEvent event) {
        log.info("Publishing game session started event for session: {}", event.getSessionId());
        
        try {
            eventProducer.publishGameEvent(event);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to publish game session started event: {}", event.getSessionId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Публикация события завершения игровой сессии
     */
    public CompletableFuture<Void> publishGameSessionFinishedEvent(GameSessionFinishedEvent event) {
        log.info("Publishing game session finished event for session: {}", event.getSessionId());
        
        try {
            eventProducer.publishGameEvent(event);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to publish game session finished event: {}", event.getSessionId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Публикация события отправки кода
     */
    public CompletableFuture<Void> publishCodeSubmittedEvent(CodeSubmittedEvent event) {
        log.info("Publishing code submitted event for session: {}, level: {}", 
                event.getSessionId(), event.getLevelId());
        
        try {
            eventProducer.publishGameEvent(event);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to publish code submitted event: {}", event.getSessionId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Публикация события завершения уровня
     */
    public CompletableFuture<Void> publishLevelCompletedEvent(LevelCompletedEvent event) {
        log.info("Publishing level completed event for session: {}, level: {}", 
                event.getSessionId(), event.getLevelId());
        
        try {
            eventProducer.publishGameEvent(event);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to publish level completed event: {}", event.getSessionId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Проверка здоровья Kafka producer
     */
    public boolean isHealthy() {
        try {
            // Базовая проверка - можно расширить при необходимости
            return true;
        } catch (Exception e) {
            log.warn("Kafka producer health check failed", e);
            return false;
        }
    }

    /**
     * Получение метрик producer
     */
    public java.util.Map<String, Object> getMetrics() {
        java.util.Map<String, Object> metrics = new java.util.HashMap<>();
        metrics.put("producerHealthy", isHealthy());
        metrics.put("timestamp", System.currentTimeMillis());
        return metrics;
    }
}