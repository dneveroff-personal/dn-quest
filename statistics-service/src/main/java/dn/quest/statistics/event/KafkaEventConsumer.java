package dn.quest.statistics.event;

import dn.quest.shared.events.BaseEvent;
import dn.quest.shared.events.team.TeamEvent;
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
import dn.quest.shared.events.file.FileUploadedEvent;
import dn.quest.shared.events.file.FileDeletedEvent;
import dn.quest.shared.events.file.FileUpdatedEvent;
import dn.quest.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Сервис для обработки событий из других сервисов через Kafka для агрегации статистики
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {

    private final StatisticsService statisticsService;

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
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
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
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
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
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received team event: {} from topic: {}, partition: {}, offset: {}", 
                event.getEventType(), topic, partition, offset);

        try {
            switch (event.getEventType()) {
                case "team.created":
                    handleTeamCreated((TeamEvent.TeamCreatedEvent) event);
                    break;
                case "team.updated":
                    handleTeamUpdated((TeamEvent.TeamUpdatedEvent) event);
                    break;
                case "team.member.added":
                    handleTeamMemberAdded((TeamEvent.TeamMemberAddedEvent) event);
                    break;
                case "team.member.removed":
                    handleTeamMemberRemoved((TeamEvent.TeamMemberRemovedEvent) event);
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
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
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

    // Методы обработки конкретных событий

    private void handleUserRegistered(UserRegisteredEvent event) {
        log.debug("Handling user registered event for user: {}", event.getUserId());
        statisticsService.updateUserRegistrationStatistics(event.getUserId(), event.getTimestamp());
    }

    private void handleUserUpdated(UserUpdatedEvent event) {
        log.debug("Handling user updated event for user: {}", event.getUserId());
        statisticsService.updateUserActivityStatistics(event.getUserId(), event.getTimestamp());
    }

    private void handleUserDeleted(UserDeletedEvent event) {
        log.debug("Handling user deleted event for user: {}", event.getUserId());
        statisticsService.updateUserDeletionStatistics(event.getUserId(), event.getTimestamp());
    }

    private void handleQuestCreated(QuestCreatedEvent event) {
        log.debug("Handling quest created event for quest: {}", event.getQuestId());
        statisticsService.updateQuestCreationStatistics(event.getQuestId(), event.getAuthorId(), event.getTimestamp());
    }

    private void handleQuestUpdated(QuestUpdatedEvent event) {
        log.debug("Handling quest updated event for quest: {}", event.getQuestId());
        statisticsService.updateQuestUpdateStatistics(event.getQuestId(), event.getTimestamp());
    }

    private void handleQuestPublished(QuestPublishedEvent event) {
        log.debug("Handling quest published event for quest: {}", event.getQuestId());
        statisticsService.updateQuestPublicationStatistics(event.getQuestId(), event.getAuthorId(), event.getTimestamp());
    }

    private void handleQuestDeleted(QuestDeletedEvent event) {
        log.debug("Handling quest deleted event for quest: {}", event.getQuestId());
        statisticsService.updateQuestDeletionStatistics(event.getQuestId(), event.getTimestamp());
    }

    private void handleGameSessionStarted(GameSessionStartedEvent event) {
        log.debug("Handling game session started event for session: {}", event.getSessionId());
        statisticsService.updateGameSessionStartStatistics(event.getSessionId(), event.getUserId(), event.getTeamId(), event.getQuestId(), event.getTimestamp());
    }

    private void handleGameSessionFinished(GameSessionFinishedEvent event) {
        log.debug("Handling game session finished event for session: {}", event.getSessionId());
        statisticsService.updateGameSessionFinishStatistics(event.getSessionId(), event.getUserId(), event.getTeamId(), event.getQuestId(), event.getIsCompleted(), event.getTimestamp());
    }

    private void handleCodeSubmitted(CodeSubmittedEvent event) {
        log.debug("Handling code submitted event for session: {}, user: {}", event.getSessionId(), event.getUserId());
        statisticsService.updateCodeSubmissionStatistics(event.getSessionId(), event.getUserId(), event.getLevelId(), event.getIsCorrect(), event.getTimestamp());
    }

    private void handleLevelCompleted(LevelCompletedEvent event) {
        log.debug("Handling level completed event for session: {}, user: {}, level: {}", 
                event.getSessionId(), event.getUserId(), event.getLevelNumber());
        statisticsService.updateLevelCompletionStatistics(event.getSessionId(), event.getUserId(), event.getLevelNumber(), event.getCompletionTime(), event.getTimestamp());
    }

    private void handleTeamCreated(TeamEvent.TeamCreatedEvent event) {
        log.debug("Handling team created event for team: {}", event.getTeamId());
        statisticsService.updateTeamCreationStatistics(event.getTeamId(), event.getCaptainId(), event.getTimestamp());
    }

    private void handleTeamUpdated(TeamEvent.TeamUpdatedEvent event) {
        log.debug("Handling team updated event for team: {}", event.getTeamId());
        statisticsService.updateTeamActivityStatistics(event.getTeamId(), event.getTimestamp());
    }

    private void handleTeamMemberAdded(TeamEvent.TeamMemberAddedEvent event) {
        log.debug("Handling team member added event for team: {}, user: {}", event.getTeamId(), event.getMemberId());
        statisticsService.updateTeamMembershipStatistics(event.getTeamId(), event.getMemberId(), "ADDED", event.getTimestamp());
    }

    private void handleTeamMemberRemoved(TeamEvent.TeamMemberRemovedEvent event) {
        log.debug("Handling team member removed event for team: {}, user: {}", event.getTeamId(), event.getMemberId());
        statisticsService.updateTeamMembershipStatistics(event.getTeamId(), event.getMemberId(), "REMOVED", event.getTimestamp());
    }

    private void handleFileUploaded(FileUploadedEvent event) {
        log.debug("Handling file uploaded event for file: {}, user: {}", event.getFileId(), event.getUserId());
        statisticsService.updateFileStatistics(event.getFileId(), event.getUserId(), event.getFileSize(), "UPLOADED", event.getTimestamp());
    }

    private void handleFileUpdated(FileUpdatedEvent event) {
        log.debug("Handling file updated event for file: {}, user: {}", event.getFileId(), event.getUserId());
        statisticsService.updateFileStatistics(event.getFileId(), event.getUserId(), event.getFileSize(), "UPDATED", event.getTimestamp());
    }

    private void handleFileDeleted(FileDeletedEvent event) {
        log.debug("Handling file deleted event for file: {}, user: {}", event.getFileId(), event.getUserId());
        statisticsService.updateFileStatistics(event.getFileId(), event.getUserId(), 0L, "DELETED", event.getTimestamp());
    }
}