package dn.quest.teammanagement.event;

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
import dn.quest.shared.events.file.FileUploadedEvent;
import dn.quest.shared.events.file.FileDeletedEvent;
import dn.quest.shared.events.file.FileUpdatedEvent;
import dn.quest.teammanagement.service.UserService;
import dn.quest.teammanagement.service.TeamService;
import dn.quest.teammanagement.service.TeamInvitationService;
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

    private final UserService userService;
    private final TeamService teamService;
    private final TeamInvitationService teamInvitationService;

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
                    log.warn("Unknown user event type: {}", event.getEventType());
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
                    log.warn("Unknown quest event type: {}", event.getEventType());
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
                    log.warn("Unknown game event type: {}", event.getEventType());
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed game event: {}", event.getEventType());
            
        } catch (Exception e) {
            log.error("Error processing game event: {}", event.getEventType(), e);
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
                    log.warn("Unknown file event type: {}", event.getEventType());
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
        
        try {
            // Создаем пользователя в локальной базе данных для кэширования
            if (!userService.userExists(event.getUserId())) {
                log.info("User {} will be synchronized from registration event", event.getUserId());
                // Здесь можно добавить логику синхронизации с User Management Service
            }
        } catch (Exception e) {
            log.error("Error handling user registered event for user: {}", event.getUserId(), e);
            throw e;
        }
    }

    private void handleUserUpdated(UserUpdatedEvent event) {
        log.debug("Handling user updated event for user: {}", event.getUserId());
        
        try {
            // Обновляем информацию о пользователе в локальной базе данных
            userService.updateUserFromEvent(event);
        } catch (Exception e) {
            log.error("Error handling user updated event for user: {}", event.getUserId(), e);
            throw e;
        }
    }

    private void handleUserDeleted(UserDeletedEvent event) {
        log.debug("Handling user deleted event for user: {}", event.getUserId());
        
        try {
            // Удаляем пользователя из всех команд
            // Отзываем все приглашения
            // Обновляем статистику команд
            userService.deactivateUser(event.getUserId());
        } catch (Exception e) {
            log.error("Error handling user deleted event for user: {}", event.getUserId(), e);
            throw e;
        }
    }

    private void handleQuestCreated(QuestCreatedEvent event) {
        log.debug("Handling quest created event for quest: {}", event.getQuestId());
        
        try {
            // Обновляем информацию о доступных квестах для команд
            teamService.updateAvailableQuests(event.getQuestId(), "CREATED");
        } catch (Exception e) {
            log.error("Error handling quest created event for quest: {}", event.getQuestId(), e);
            throw e;
        }
    }

    private void handleQuestUpdated(QuestUpdatedEvent event) {
        log.debug("Handling quest updated event for quest: {}", event.getQuestId());
        
        try {
            // Обновляем информацию о квесте в кэше команд
            teamService.updateQuestCache(event.getQuestId(), event);
        } catch (Exception e) {
            log.error("Error handling quest updated event for quest: {}", event.getQuestId(), e);
            throw e;
        }
    }

    private void handleQuestPublished(QuestPublishedEvent event) {
        log.debug("Handling quest published event for quest: {}", event.getQuestId());
        
        try {
            // Делаем квест доступным для команд
            teamService.updateAvailableQuests(event.getQuestId(), "PUBLISHED");
        } catch (Exception e) {
            log.error("Error handling quest published event for quest: {}", event.getQuestId(), e);
            throw e;
        }
    }

    private void handleQuestDeleted(QuestDeletedEvent event) {
        log.debug("Handling quest deleted event for quest: {}", event.getQuestId());
        
        try {
            // Удаляем квест из доступных для команд
            teamService.removeQuestFromAvailable(event.getQuestId());
        } catch (Exception e) {
            log.error("Error handling quest deleted event for quest: {}", event.getQuestId(), e);
            throw e;
        }
    }

    private void handleGameSessionStarted(GameSessionStartedEvent event) {
        log.debug("Handling game session started event for session: {}", event.getSessionId());
        
        try {
            // Обновляем статистику активности команды
            teamService.updateTeamStatistics(event.getTeamId());
        } catch (Exception e) {
            log.error("Error handling game session started event for session: {}", event.getSessionId(), e);
            throw e;
        }
    }

    private void handleGameSessionFinished(GameSessionFinishedEvent event) {
        log.debug("Handling game session finished event for session: {}", event.getSessionId());
        
        try {
            // Обновляем статистику команды на основе результатов сессии
            teamService.updateGameSessionStatistics(event.getTeamId(), event.getSessionId(), "FINISHED");
        } catch (Exception e) {
            log.error("Error handling game session finished event for session: {}", event.getSessionId(), e);
            throw e;
        }
    }

    private void handleCodeSubmitted(CodeSubmittedEvent event) {
        log.debug("Handling code submitted event for session: {}, user: {}", 
                event.getSessionId(), event.getUserId());
        
        try {
            // Обновляем статистику активности пользователя в команде
            userService.updateCodeSubmissionStatistics(event.getUserId(), event.getSessionId());
        } catch (Exception e) {
            log.error("Error handling code submitted event for session: {}, user: {}", 
                    event.getSessionId(), event.getUserId(), e);
            throw e;
        }
    }

    private void handleLevelCompleted(LevelCompletedEvent event) {
        log.debug("Handling level completed event for session: {}, user: {}, level: {}", 
                event.getSessionId(), event.getUserId(), event.getLevelNumber());
        
        try {
            // Обновляем достижения пользователя в команде
            userService.updateLevelCompletionStatistics(event.getUserId(), event.getSessionId(), event.getLevelNumber());
        } catch (Exception e) {
            log.error("Error handling level completed event for session: {}, user: {}", 
                    event.getSessionId(), event.getUserId(), e);
            throw e;
        }
    }

    private void handleFileUploaded(FileUploadedEvent event) {
        log.debug("Handling file uploaded event for file: {}, user: {}", 
                event.getFileId(), event.getUserId());
        
        try {
            // Обновляем статистику файлов пользователя в команде
            userService.updateFileStatistics(event.getUserId(), event.getFileId(), "UPLOADED");
        } catch (Exception e) {
            log.error("Error handling file uploaded event for file: {}, user: {}", 
                    event.getFileId(), event.getUserId(), e);
            throw e;
        }
    }

    private void handleFileUpdated(FileUpdatedEvent event) {
        log.debug("Handling file updated event for file: {}, user: {}", 
                event.getFileId(), event.getUserId());
        
        try {
            // Обновляем информацию о файле в кэше команды
            userService.updateFileCache(event.getFileId(), event);
        } catch (Exception e) {
            log.error("Error handling file updated event for file: {}, user: {}", 
                    event.getFileId(), event.getUserId(), e);
            throw e;
        }
    }

    private void handleFileDeleted(FileDeletedEvent event) {
        log.debug("Handling file deleted event for file: {}, user: {}", 
                event.getFileId(), event.getUserId());
        
        try {
            // Удаляем файл из статистики пользователя
            userService.updateFileStatistics(event.getUserId(), event.getFileId(), "DELETED");
        } catch (Exception e) {
            log.error("Error handling file deleted event for file: {}, user: {}", 
                    event.getFileId(), event.getUserId(), e);
            throw e;
        }
    }
}