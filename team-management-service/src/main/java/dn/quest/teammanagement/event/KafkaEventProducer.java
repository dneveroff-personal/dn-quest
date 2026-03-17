package dn.quest.teammanagement.event;

import dn.quest.shared.events.EventProducer;
import dn.quest.shared.events.team.*;
import dn.quest.shared.events.user.UserRegisteredEvent;
import dn.quest.shared.events.user.UserUpdatedEvent;
import dn.quest.shared.events.user.UserDeletedEvent;
import dn.quest.shared.events.game.GameSessionStartedEvent;
import dn.quest.shared.events.game.GameSessionFinishedEvent;
import dn.quest.shared.events.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Сервис для публикации событий в Kafka
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final EventProducer eventProducer;

    /**
     * Публикация события создания команды
     */
    public void publishTeamCreatedEvent(Long teamId, String teamName, Long captainId, String captainName) {
        TeamCreatedEvent event = new TeamCreatedEvent(
                UUID.randomUUID().toString(),
                teamId,
                captainId,
                teamName,
                captainName
        );
        eventProducer.publishTeamCreatedEvent(event);
        log.info("Published team created event for team: {}", teamId);
    }

    /**
     * Публикация события обновления команды
     */
    public void publishTeamUpdatedEvent(Long teamId, String teamName, String description) {
        TeamUpdatedEvent event = new TeamUpdatedEvent(
                UUID.randomUUID().toString(),
                teamId,
                teamName,
                description
        );
        eventProducer.publishTeamUpdatedEvent(event);
        log.info("Published team updated event for team: {}", teamId);
    }

    /**
     * Публикация события удаления команды
     */
    public void publishTeamDeletedEvent(Long teamId, String teamName) {
        TeamDeletedEvent event = new TeamDeletedEvent(
                UUID.randomUUID().toString(),
                teamId,
                teamName
        );
        eventProducer.publishTeamDeletedEvent(event);
        log.info("Published team deleted event for team: {}", teamId);
    }

    /**
     * Публикация события добавления участника в команду
     */
    public void publishTeamMemberAddedEvent(Long teamId, Long userId, String userName, String role) {
        TeamMemberAddedEvent event = new TeamMemberAddedEvent(
                UUID.randomUUID().toString(),
                teamId,
                userId,
                userName,
                role
        );
        eventProducer.publishTeamMemberAddedEvent(event);
        log.info("Published team member added event for team: {}, user: {}", teamId, userId);
    }

    /**
     * Публикация события удаления участника из команды
     */
    public void publishTeamMemberRemovedEvent(Long teamId, Long userId, String userName) {
        TeamMemberRemovedEvent event = new TeamMemberRemovedEvent(
                UUID.randomUUID().toString(),
                teamId,
                userId,
                userName
        );
        eventProducer.publishTeamMemberRemovedEvent(event);
        log.info("Published team member removed event for team: {}, user: {}", teamId, userId);
    }

    /**
     * Публикация события регистрации пользователя
     */
    public void publishUserRegisteredEvent(Long userId, String username, String email) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID().toString(),
                userId,
                username,
                email
        );
        eventProducer.publishUserRegisteredEvent(event);
        log.info("Published user registered event for user: {}", userId);
    }

    /**
     * Публикация события обновления пользователя
     */
    public void publishUserUpdatedEvent(Long userId, String username, String email) {
        UserUpdatedEvent event = new UserUpdatedEvent(
                UUID.randomUUID().toString(),
                userId,
                username,
                email
        );
        eventProducer.publishUserUpdatedEvent(event);
        log.info("Published user updated event for user: {}", userId);
    }

    /**
     * Публикация события удаления пользователя
     */
    public void publishUserDeletedEvent(Long userId, String username) {
        UserDeletedEvent event = new UserDeletedEvent(
                UUID.randomUUID().toString(),
                userId,
                username
        );
        eventProducer.publishUserDeletedEvent(event);
        log.info("Published user deleted event for user: {}", userId);
    }

    /**
     * Публикация события начала игровой сессии
     */
    public void publishGameSessionStartedEvent(Long sessionId, Long teamId, Long questId) {
        GameSessionStartedEvent event = new GameSessionStartedEvent(
                UUID.randomUUID().toString(),
                sessionId,
                teamId,
                questId
        );
        eventProducer.publishGameSessionStartedEvent(event);
        log.info("Published game session started event for session: {}", sessionId);
    }

    /**
     * Публикация события завершения игровой сессии
     */
    public void publishGameSessionFinishedEvent(Long sessionId, Long teamId, Long questId, boolean completed) {
        GameSessionFinishedEvent event = new GameSessionFinishedEvent(
                UUID.randomUUID().toString(),
                sessionId,
                teamId,
                questId,
                completed
        );
        eventProducer.publishGameSessionFinishedEvent(event);
        log.info("Published game session finished event for session: {}", sessionId);
    }

    /**
     * Публикация события уведомления
     */
    public void publishNotificationEvent(Long userId, String title, String message, String type) {
        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID().toString(),
                userId,
                title,
                message,
                type
        );
        eventProducer.publishNotificationEvent(event);
        log.info("Published notification event for user: {}", userId);
    }
}