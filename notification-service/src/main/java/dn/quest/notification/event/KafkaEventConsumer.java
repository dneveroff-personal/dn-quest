package dn.quest.notification.event;

import dn.quest.shared.events.EventConsumer;
import dn.quest.shared.events.BaseEvent;
import dn.quest.shared.events.user.UserRegisteredEvent;
import dn.quest.shared.events.user.UserUpdatedEvent;
import dn.quest.shared.events.user.UserDeletedEvent;
import dn.quest.shared.events.quest.QuestCreatedEvent;
import dn.quest.shared.events.quest.QuestUpdatedEvent;
import dn.quest.shared.events.quest.QuestPublishedEvent;
import dn.quest.shared.events.quest.QuestDeletedEvent;
import dn.quest.shared.events.game.GameSessionStartedEvent;
import dn.quest.shared.events.game.GameSessionFinishedEvent;
import dn.quest.shared.events.game.CodeSubmittedEvent;
import dn.quest.shared.events.game.LevelCompletedEvent;
import dn.quest.shared.events.team.TeamCreatedEvent;
import dn.quest.shared.events.team.TeamUpdatedEvent;
import dn.quest.shared.events.team.TeamMemberAddedEvent;
import dn.quest.shared.events.team.TeamMemberRemovedEvent;
import dn.quest.shared.events.file.FileUploadedEvent;
import dn.quest.shared.events.file.FileDeletedEvent;
import dn.quest.shared.events.file.FileUpdatedEvent;
import dn.quest.shared.events.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {

    private final NotificationService notificationService;

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
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received user event: {} from topic: {}, partition: {}, offset: {}", 
                event.getEventType(), topic, partition, offset);

        try {
            switch (event.getEventType()) {
                case "user.registered":
                    handleUserRegistered((UserRegisteredEvent) event);
                    break;
                case "user.updated":
                    handleUserUpdated((UserUpdatedEvent) event);
                    break;
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
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received quest event: {} from topic: {}, partition: {}, offset: {}", 
                event.getEventType(), topic, partition, offset);

        try {
            switch (event.getEventType()) {
                case "quest.created":
                    handleQuestCreated((QuestCreatedEvent) event);
                    break;
                case "quest.updated":
                    handleQuestUpdated((QuestUpdatedEvent) event);
                    break;
                case "quest.published":
                    handleQuestPublished((QuestPublishedEvent) event);
                    break;
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
     * Обработка игровых событий
     */
    @KafkaListener(
            topics = "${app.kafka.topics.game-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleGameEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received game event: {} from topic: {}, partition: {}, offset: {}", 
                event.getEventType(), topic, partition, offset);

        try {
            switch (event.getEventType()) {
                case "game.session.started":
                    handleGameSessionStarted((GameSessionStartedEvent) event);
                    break;
                case "game.session.finished":
                    handleGameSessionFinished((GameSessionFinishedEvent) event);
                    break;
                case "game.code.submitted":
                    handleCodeSubmitted((CodeSubmittedEvent) event);
                    break;
                case "game.level.completed":
                    handleLevelCompleted((LevelCompletedEvent) event);
                    break;
                default:
                    log.debug("Ignoring game event type: {}", event.getEventType());
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed game event: {}", event.getEventType());
            
        } catch (Exception e) {
            log.error("Error processing game event: {}", event.getEventType(), e);
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
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received team event: {} from topic: {}, partition: {}, offset: {}", 
                event.getEventType(), topic, partition, offset);

        try {
            switch (event.getEventType()) {
                case "team.created":
                    handleTeamCreated((TeamCreatedEvent) event);
                    break;
                case "team.updated":
                    handleTeamUpdated((TeamUpdatedEvent) event);
                    break;
                case "team.member.added":
                    handleTeamMemberAdded((TeamMemberAddedEvent) event);
                    break;
                case "team.member.removed":
                    handleTeamMemberRemoved((TeamMemberRemovedEvent) event);
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

    /**
     * Обработка файловых событий
     */
    @KafkaListener(
            topics = "${app.kafka.topics.file-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleFileEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received file event: {} from topic: {}, partition: {}, offset: {}", 
                event.getEventType(), topic, partition, offset);

        try {
            switch (event.getEventType()) {
                case "file.uploaded":
                    handleFileUploaded((FileUploadedEvent) event);
                    break;
                case "file.updated":
                    handleFileUpdated((FileUpdatedEvent) event);
                    break;
                case "file.deleted":
                    handleFileDeleted((FileDeletedEvent) event);
                    break;
                default:
                    log.debug("Ignoring file event type: {}", event.getEventType());
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed file event: {}", event.getEventType());
            
        } catch (Exception e) {
            log.error("Error processing file event: {}", event.getEventType(), e);
            throw e; // Перебрасываем исключение для retry механизма
        }
    }

    /**
     * Обработка событий уведомлений
     */
    @KafkaListener(
            topics = "${app.kafka.topics.notification-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleNotificationEvents(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received notification event: {} from topic: {}, partition: {}, offset: {}", 
                event.getType(), topic, partition, offset);

        try {
            // Обрабатываем событие уведомления напрямую
            notificationService.processNotificationEvent(event);
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed notification event: {}", event.getType());
            
        } catch (Exception e) {
            log.error("Error processing notification event: {}", event.getType(), e);
            throw e; // Перебрасываем исключение для retry механизма
        }
    }

    // Методы обработки конкретных событий

    private void handleUserRegistered(UserRegisteredEvent event) {
        log.debug("Handling user registered event for user: {}", event.getUserId());
        notificationService.sendWelcomeNotification(event.getUserId(), event.getUsername(), event.getEmail());
    }

    private void handleUserUpdated(UserUpdatedEvent event) {
        log.debug("Handling user updated event for user: {}", event.getUserId());
        notificationService.sendProfileUpdatedNotification(event.getUserId(), event.getUsername());
    }

    private void handleUserDeleted(UserDeletedEvent event) {
        log.debug("Handling user deleted event for user: {}", event.getUserId());
        // Уведомление об удалении пользователя обычно не отправляется
    }

    private void handleQuestCreated(QuestCreatedEvent event) {
        log.debug("Handling quest created event for quest: {}", event.getQuestId());
        notificationService.sendQuestCreatedNotification(event.getQuestId(), event.getTitle(), event.getAuthorId());
    }

    private void handleQuestUpdated(QuestUpdatedEvent event) {
        log.debug("Handling quest updated event for quest: {}", event.getQuestId());
        notificationService.sendQuestUpdatedNotification(event.getQuestId(), event.getTitle(), event.getAuthorId());
    }

    private void handleQuestPublished(QuestPublishedEvent event) {
        log.debug("Handling quest published event for quest: {}", event.getQuestId());
        notificationService.sendQuestPublishedNotification(event.getQuestId(), event.getTitle(), event.getAuthorId());
    }

    private void handleQuestDeleted(QuestDeletedEvent event) {
        log.debug("Handling quest deleted event for quest: {}", event.getQuestId());
        // Уведомление об удалении квеста обычно не отправляется
    }

    private void handleGameSessionStarted(GameSessionStartedEvent event) {
        log.debug("Handling game session started event for session: {}", event.getSessionId());
        notificationService.sendGameSessionStartedNotification(event.getUserId(), event.getSessionId(), event.getQuestId());
    }

    private void handleGameSessionFinished(GameSessionFinishedEvent event) {
        log.debug("Handling game session finished event for session: {}", event.getSessionId());
        notificationService.sendGameSessionFinishedNotification(event.getUserId(), event.getSessionId(), event.isCompleted());
    }

    private void handleCodeSubmitted(CodeSubmittedEvent event) {
        log.debug("Handling code submitted event for session: {}, user: {}", event.getSessionId(), event.getUserId());
        // Уведомление о отправке кода обычно не отправляется
    }

    private void handleLevelCompleted(LevelCompletedEvent event) {
        log.debug("Handling level completed event for session: {}, user: {}, level: {}", 
                event.getSessionId(), event.getUserId(), event.getLevelNumber());
        notificationService.sendLevelCompletedNotification(event.getUserId(), event.getSessionId(), event.getLevelNumber());
    }

    private void handleTeamCreated(TeamCreatedEvent event) {
        log.debug("Handling team created event for team: {}", event.getTeamId());
        notificationService.sendTeamCreatedNotification(event.getTeamId(), event.getTeamName(), event.getCaptainId());
    }

    private void handleTeamUpdated(TeamUpdatedEvent event) {
        log.debug("Handling team updated event for team: {}", event.getTeamId());
        notificationService.sendTeamUpdatedNotification(event.getTeamId(), event.getTeamName());
    }

    private void handleTeamMemberAdded(TeamMemberAddedEvent event) {
        log.debug("Handling team member added event for team: {}, user: {}", event.getTeamId(), event.getUserId());
        notificationService.sendTeamMemberAddedNotification(event.getTeamId(), event.getTeamName(), event.getUserId(), event.getUserName());
    }

    private void handleTeamMemberRemoved(TeamMemberRemovedEvent event) {
        log.debug("Handling team member removed event for team: {}, user: {}", event.getTeamId(), event.getUserId());
        notificationService.sendTeamMemberRemovedNotification(event.getTeamId(), event.getTeamName(), event.getUserId(), event.getUserName());
    }

    private void handleFileUploaded(FileUploadedEvent event) {
        log.debug("Handling file uploaded event for file: {}, user: {}", event.getFileId(), event.getUserId());
        notificationService.sendFileUploadedNotification(event.getUserId(), event.getFileName());
    }

    private void handleFileUpdated(FileUpdatedEvent event) {
        log.debug("Handling file updated event for file: {}, user: {}", event.getFileId(), event.getUserId());
        // Уведомление об обновлении файла обычно не отправляется
    }

    private void handleFileDeleted(FileDeletedEvent event) {
        log.debug("Handling file deleted event for file: {}, user: {}", event.getFileId(), event.getUserId());
        // Уведомление об удалении файла обычно не отправляется
    }
}