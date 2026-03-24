package dn.quest.filestorage.event;

import dn.quest.shared.events.BaseEvent;
import dn.quest.shared.events.user.UserDeletedEvent;
import dn.quest.shared.events.quest.QuestDeletedEvent;
import dn.quest.shared.events.team.TeamDeletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Сервис для обработки событий из других сервисов через Kafka
 */
@Service
@Slf4j
public class KafkaEventConsumer {

    // private final FileStorageService fileStorageService; // TODO: implement after fixing FileStorageServiceImpl

    /**
     * Обработка пользовательских событий
     */
    @KafkaListener(
            topics = "${app.kafka.topics.user-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received user event: {} from topic: {}, partition: {}, offset: {}", 
                event.getEventType(), topic, partition, offset);

        try {
            switch (event.getEventType()) {
                case "user.deleted":
                    handleUserDeleted((UserDeletedEvent) event);
                    break;
                default:
                    log.debug("Ignoring user event type: {}", event.getEventType());
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed user event: {}", event.getEventType());
            
        } catch (Exception e) {
            log.error("Error processing user event: {}", event.getEventType(), e);
            throw e; // Перебрасываем исключение для retry механизма
        }
    }

    /**
     * Обработка событий квестов
     */
    @KafkaListener(
            topics = "${app.kafka.topics.quest-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleQuestEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received quest event: {} from topic: {}, partition: {}, offset: {}", 
                event.getEventType(), topic, partition, offset);

        try {
            switch (event.getEventType()) {
                case "quest.deleted":
                    handleQuestDeleted((QuestDeletedEvent) event);
                    break;
                default:
                    log.debug("Ignoring quest event type: {}", event.getEventType());
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed quest event: {}", event.getEventType());
            
        } catch (Exception e) {
            log.error("Error processing quest event: {}", event.getEventType(), e);
            throw e; // Перебрасываем исключение для retry механизма
        }
    }

    /**
     * Обработка командных событий
     */
    @KafkaListener(
            topics = "${app.kafka.topics.team-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTeamEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received team event: {} from topic: {}, partition: {}, offset: {}", 
                event.getEventType(), topic, partition, offset);

        try {
            switch (event.getEventType()) {
                case "team.deleted":
                    handleTeamDeleted((TeamDeletedEvent) event);
                    break;
                default:
                    log.debug("Ignoring team event type: {}", event.getEventType());
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed team event: {}", event.getEventType());
            
        } catch (Exception e) {
            log.error("Error processing team event: {}", event.getEventType(), e);
            throw e; // Перебрасываем исключение для retry механизма
        }
    }

    // Методы обработки конкретных событий

    private void handleUserDeleted(UserDeletedEvent event) {
        log.debug("Handling user deleted event for user: {}", event.getUserId());
        // TODO: implement file deletion after fixing FileStorageServiceImpl
        log.info("Received user deleted event for user: {} - file deletion pending", event.getUserId());
    }

    private void handleQuestDeleted(QuestDeletedEvent event) {
        log.debug("Handling quest deleted event for quest: {}", event.getQuestId());
        // TODO: implement file deletion after fixing FileStorageServiceImpl
        log.info("Received quest deleted event for quest: {} - file deletion pending", event.getQuestId());
    }

    private void handleTeamDeleted(TeamDeletedEvent event) {
        log.debug("Handling team deleted event for team: {}", event.getTeamId());
        // TODO: implement file deletion after fixing FileStorageServiceImpl
        log.info("Received team deleted event for team: {} - file deletion pending", event.getTeamId());
    }
}