package dn.quest.shared.events;

import dn.quest.shared.events.quest.QuestDeletedEvent;
import dn.quest.shared.events.quest.QuestPublishedEvent;
import dn.quest.shared.events.quest.QuestUpdatedEvent;
import dn.quest.shared.events.team.TeamDeletedEvent;
import dn.quest.shared.events.team.TeamEvent.TeamCaptainChangedEvent;
import dn.quest.shared.events.team.TeamEvent.TeamMemberAddedEvent;
import dn.quest.shared.events.team.TeamEvent.TeamMemberRemovedEvent;
import dn.quest.shared.events.team.TeamDeletedEvent;
import dn.quest.shared.events.user.UserDeletedEvent;
import dn.quest.shared.events.user.UserUpdatedEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Базовый класс для обработки событий из Kafka
 */
@Slf4j
public abstract class EventConsumer {

    /**
     * Обработка пользовательских событий
     */
    protected void handleUserEvent(BaseEvent event, String topic) {
        log.debug("Handling user event: {} from topic: {}", event.getEventType(), topic);
    }

    /**
     * Обработка событий квестов
     */
    protected void handleQuestEvent(BaseEvent event, String topic) {
        log.debug("Handling quest event: {} from topic: {}", event.getEventType(), topic);
    }

    /**
     * Обработка командных событий
     */
    protected void handleTeamEvent(BaseEvent event, String topic) {
        log.debug("Handling team event: {} from topic: {}", event.getEventType(), topic);
    }

    /**
     * Обработка событий файлов
     */
    protected void handleFileEvent(BaseEvent event, String topic) {
        log.debug("Handling file event: {} from topic: {}", event.getEventType(), topic);
    }

    /**
     * Обработка событий уведомлений
     */
    protected void handleNotificationEvent(BaseEvent event, String topic) {
        log.debug("Handling notification event: {} from topic: {}", event.getEventType(), topic);
    }
}