package dn.quest.questmanagement.event;

import dn.quest.shared.events.EventProducer;
import dn.quest.shared.events.quest.QuestCreatedEvent;
import dn.quest.shared.events.quest.QuestDeletedEvent;
import dn.quest.shared.events.quest.QuestPublishedEvent;
import dn.quest.shared.events.quest.QuestUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Kafka producer для публикации событий квестов
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final EventProducer eventProducer;

    /**
     * Публикация события создания квеста
     */
    public void publishQuestCreatedEvent(QuestCreatedEvent event) {
        log.info("Publishing quest created event for quest ID: {}", event.getQuestId());
        eventProducer.publishQuestEvent(event);
    }

    /**
     * Публикация события обновления квеста
     */
    public void publishQuestUpdatedEvent(QuestUpdatedEvent event) {
        log.info("Publishing quest updated event for quest ID: {}", event.getQuestId());
        eventProducer.publishQuestEvent(event);
    }

    /**
     * Публикация события публикации квеста
     */
    public void publishQuestPublishedEvent(QuestPublishedEvent event) {
        log.info("Publishing quest published event for quest ID: {}", event.getQuestId());
        eventProducer.publishQuestEvent(event);
    }

    /**
     * Публикация события удаления квеста
     */
    public void publishQuestDeletedEvent(QuestDeletedEvent event) {
        log.info("Publishing quest deleted event for quest ID: {}", event.getQuestId());
        eventProducer.publishQuestEvent(event);
    }
}