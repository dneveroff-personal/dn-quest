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
        TeamEvent.TeamCreatedEvent event = new TeamEvent.TeamCreatedEvent(
                teamId,
                teamName,
                null,
                null,
                null,
                captainId,
                captainName,
                false,
                0
        );
        eventProducer.publishTeamCreatedEvent(event);
        log.info("Published team created event for team: {}", teamId);
    }

    /**
     * Публикация события обновления команды
     */
    public void publishTeamUpdatedEvent(Long teamId, String teamName, String description) {
        TeamEvent.TeamUpdatedEvent event = new TeamEvent.TeamUpdatedEvent(
                teamId,
                teamName,
                description,
                null,
                null,
                null,
                null,
                null,
                null
        );
        eventProducer.publishTeamUpdatedEvent(event);
        log.info("Published team updated event for team: {}", teamId);
    }

    /**
     * Публикация события удаления команды
     */
    public void publishTeamDeletedEvent(Long teamId, String teamName) {
        TeamDeletedEvent event = new TeamDeletedEvent(teamId);
        eventProducer.publishTeamDeletedEvent(event);
        log.info("Published team deleted event for team: {}", teamId);
    }

    /**
     * Публикация события добавления участника в команду
     */
    public void publishTeamMemberAddedEvent(Long teamId, Long userId, String userName, String role) {
        TeamEvent.TeamMemberAddedEvent event = new TeamEvent.TeamMemberAddedEvent(
                teamId,
                null,
                userId,
                userName,
                role,
                null,
                null,
                null
        );
        eventProducer.publishTeamMemberAddedEvent(event);
        log.info("Published team member added event for team: {}, user: {}", teamId, userId);
    }

    /**
     * Публикация события удаления участника из команды
     */
    public void publishTeamMemberRemovedEvent(Long teamId, Long userId, String userName) {
        TeamEvent.TeamMemberRemovedEvent event = new TeamEvent.TeamMemberRemovedEvent(
                teamId,
                null,
                userId,
                userName,
                null,
                null,
                null
        );
        eventProducer.publishTeamMemberRemovedEvent(event);
        log.info("Published team member removed event for team: {}, user: {}", teamId, userId);
    }

    /**
     * Публикация события регистрации пользователя
     */
    public void publishUserRegisteredEvent(Long userId, String username, String email) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                userId,
                username,
                email,
                null,
                null
        );
        eventProducer.publishUserRegisteredEvent(event);
        log.info("Published user registered event for user: {}", userId);
    }

    /**
     * Публикация события обновления пользователя
     */
    public void publishUserUpdatedEvent(Long userId, String username, String email) {
        UserUpdatedEvent event = new UserUpdatedEvent(
                userId,
                username,
                email,
                null,
                null,
                null,
                null
        );
        eventProducer.publishUserUpdatedEvent(event);
        log.info("Published user updated event for user: {}", userId);
    }

    /**
     * Публикация события удаления пользователя
     */
    public void publishUserDeletedEvent(Long userId) {
        UserDeletedEvent event = new UserDeletedEvent(userId);
        eventProducer.publishUserDeletedEvent(event);
        log.info("Published user deleted event for user: {}", userId);
    }

    /**
     * Публикация события начала игровой сессии
     */
    public void publishGameSessionStartedEvent(Long sessionId, Long userId, Long teamId, Long questId, String difficultyString) {
        GameSessionStartedEvent event = new GameSessionStartedEvent(
                sessionId,
                userId,
                teamId,
                questId,
                difficultyString
        );
        eventProducer.publishGameEvent(event);
        log.info("Published game session started event for session: {}", sessionId);
    }

    /**
     * Публикация события завершения игровой сессии
     */
    public void publishGameSessionFinishedEvent(Long sessionId, Long userId, Long teamId, Long questId, boolean completed) {
        GameSessionFinishedEvent event = new GameSessionFinishedEvent(
                questId,
                sessionId,
                userId,
                teamId,
                null,
                null,
                completed
        );
        eventProducer.publishGameEvent(event);
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