package dn.quest.questmanagement.event;

import dn.quest.shared.events.BaseEvent;
import dn.quest.shared.events.EventConsumer;
import dn.quest.shared.events.game.GameSessionFinishedEvent;
import dn.quest.shared.events.game.GameSessionStartedEvent;
import dn.quest.shared.events.game.LevelCompletedEvent;
import dn.quest.shared.events.team.TeamCreatedEvent;
import dn.quest.shared.events.team.TeamMemberAddedEvent;
import dn.quest.shared.events.team.TeamMemberRemovedEvent;
import dn.quest.shared.events.team.TeamUpdatedEvent;
import dn.quest.shared.events.user.UserDeletedEvent;
import dn.quest.shared.events.user.UserRegisteredEvent;
import dn.quest.shared.events.user.UserUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer для прослушивания событий из других сервисов
 */
@Slf4j
@Component
public class KafkaEventConsumer extends EventConsumer {

    /**
     * Обработка пользовательских событий
     */
    @Override
    protected void handleUserEvent(BaseEvent event, String topic) {
        super.handleUserEvent(event, topic);
        
        switch (event.getEventType()) {
            case "UserRegistered":
                handleUserRegistered((UserRegisteredEvent) event);
                break;
            case "UserUpdated":
                handleUserUpdated((UserUpdatedEvent) event);
                break;
            case "UserDeleted":
                handleUserDeleted((UserDeletedEvent) event);
                break;
            default:
                log.warn("Unknown user event type: {}", event.getEventType());
        }
    }

    /**
     * Обработка игровых событий
     */
    @Override
    protected void handleGameEvent(BaseEvent event, String topic) {
        super.handleGameEvent(event, topic);
        
        switch (event.getEventType()) {
            case "GameSessionStarted":
                handleGameSessionStarted((GameSessionStartedEvent) event);
                break;
            case "GameSessionFinished":
                handleGameSessionFinished((GameSessionFinishedEvent) event);
                break;
            case "LevelCompleted":
                handleLevelCompleted((LevelCompletedEvent) event);
                break;
            default:
                log.warn("Unknown game event type: {}", event.getEventType());
        }
    }

    /**
     * Обработка командных событий
     */
    @Override
    protected void handleTeamEvent(BaseEvent event, String topic) {
        super.handleTeamEvent(event, topic);
        
        switch (event.getEventType()) {
            case "TeamCreated":
                handleTeamCreated((TeamCreatedEvent) event);
                break;
            case "TeamUpdated":
                handleTeamUpdated((TeamUpdatedEvent) event);
                break;
            case "TeamMemberAdded":
                handleTeamMemberAdded((TeamMemberAddedEvent) event);
                break;
            case "TeamMemberRemoved":
                handleTeamMemberRemoved((TeamMemberRemovedEvent) event);
                break;
            default:
                log.warn("Unknown team event type: {}", event.getEventType());
        }
    }

    // Обработчики конкретных событий

    private void handleUserRegistered(UserRegisteredEvent event) {
        log.info("User registered: {} ({})", event.getUsername(), event.getUserId());
        // Здесь можно добавить логику для инициализации данных нового пользователя
    }

    private void handleUserUpdated(UserUpdatedEvent event) {
        log.info("User updated: {}", event.getUserId());
        // Здесь можно добавить логику для обновления информации об авторах квестов
    }

    private void handleUserDeleted(UserDeletedEvent event) {
        log.info("User deleted: {}", event.getUserId());
        // Здесь можно добавить логику для обработки удаления пользователя (архивация квестов и т.д.)
    }

    private void handleGameSessionStarted(GameSessionStartedEvent event) {
        log.info("Game session started for quest: {}, session: {}", 
                event.getQuestId(), event.getSessionId());
        // Здесь можно добавить логику для обновления статистики квеста
    }

    private void handleGameSessionFinished(GameSessionFinishedEvent event) {
        log.info("Game session finished for quest: {}, session: {}, status: {}", 
                event.getQuestId(), event.getSessionId(), event.getFinalStatus());
        // Здесь можно добавить логику для обновления статистики квеста
    }

    private void handleLevelCompleted(LevelCompletedEvent event) {
        log.info("Level completed: {} for quest: {} by participant: {}", 
                event.getLevelId(), event.getQuestId(), event.getParticipantId());
        // Здесь можно добавить логику для обновления статистики уровня
    }

    private void handleTeamCreated(TeamCreatedEvent event) {
        log.info("Team created: {} ({})", event.getTeamName(), event.getTeamId());
        // Здесь можно добавить логику для обработки создания команды
    }

    private void handleTeamUpdated(TeamUpdatedEvent event) {
        log.info("Team updated: {}", event.getTeamId());
        // Здесь можно добавить логику для обработки обновления команды
    }

    private void handleTeamMemberAdded(TeamMemberAddedEvent event) {
        log.info("Team member added: {} to team: {}", 
                event.getMemberId(), event.getTeamId());
        // Здесь можно добавить логику для обработки добавления участника в команду
    }

    private void handleTeamMemberRemoved(TeamMemberRemovedEvent event) {
        log.info("Team member removed: {} from team: {}", 
                event.getMemberId(), event.getTeamId());
        // Здесь можно добавить логику для обработки удаления участника из команды
    }
}