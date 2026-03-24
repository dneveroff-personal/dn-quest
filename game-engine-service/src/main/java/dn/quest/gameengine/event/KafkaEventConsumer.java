package dn.quest.gameengine.event;

import dn.quest.shared.events.BaseEvent;
import dn.quest.shared.events.EventConsumer;
import dn.quest.shared.events.quest.QuestDeletedEvent;
import dn.quest.shared.events.quest.QuestPublishedEvent;
import dn.quest.shared.events.quest.QuestUpdatedEvent;
import dn.quest.shared.events.user.UserDeletedEvent;
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
     * Обработка событий квестов
     */
    @Override
    protected void handleQuestEvent(BaseEvent event, String topic) {
        super.handleQuestEvent(event, topic);
        
        switch (event.getEventType()) {
            case "QuestUpdated":
                handleQuestUpdated((QuestUpdatedEvent) event);
                break;
            case "QuestPublished":
                handleQuestPublished((QuestPublishedEvent) event);
                break;
            case "QuestDeleted":
                handleQuestDeleted((QuestDeletedEvent) event);
                break;
            default:
                log.warn("Unknown quest event type: {}", event.getEventType());
        }
    }

    /**
     * Обработка командных событий
     */
    @Override
    protected void handleTeamEvent(BaseEvent event, String topic) {
        super.handleTeamEvent(event, topic);
        
        switch (event.getEventType()) {
            case "TeamDeleted":
                log.info("Team deleted: {}", ((dn.quest.shared.events.team.TeamDeletedEvent) event).getTeamId());
                break;
            default:
                log.warn("Unknown team event type: {}", event.getEventType());
        }
    }

    // Обработчики конкретных событий

    private void handleUserUpdated(UserUpdatedEvent event) {
        log.info("User updated: {}", event.getUserId());
        // Здесь можно добавить логику для обновления информации об игроках в активных сессиях
    }

    private void handleUserDeleted(UserDeletedEvent event) {
        log.info("User deleted: {}", event.getUserId());
        // Здесь можно добавить логику для завершения активных сессий удаленного пользователя
    }

    private void handleQuestUpdated(QuestUpdatedEvent event) {
        log.info("Quest updated: {}", event.getQuestId());
        // Здесь можно добавить логику для обновления активных игровых сессий
    }

    private void handleQuestPublished(QuestPublishedEvent event) {
        log.info("Quest published: {}", event.getQuestId());
        // Здесь можно добавить логику для обработки публикации нового квеста
    }

    private void handleQuestDeleted(QuestDeletedEvent event) {
        log.info("Quest deleted: {}", event.getQuestId());
        // Здесь можно добавить логику для завершения всех сессий удаленного квеста
    }
}