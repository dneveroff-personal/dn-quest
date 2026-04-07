package dn.quest.shared.events.impl;

import dn.quest.shared.constants.KafkaTopics;
import dn.quest.shared.events.EventProducer;
import dn.quest.shared.events.file.FileDeletedEvent;
import dn.quest.shared.events.file.FileUploadedEvent;
import dn.quest.shared.events.file.FileUpdatedEvent;
import dn.quest.shared.events.game.CodeSubmittedEvent;
import dn.quest.shared.events.game.GameSessionFinishedEvent;
import dn.quest.shared.events.game.GameSessionStartedEvent;
import dn.quest.shared.events.game.LevelCompletedEvent;
import dn.quest.shared.events.notification.NotificationEvent;
import dn.quest.shared.events.quest.QuestCreatedEvent;
import dn.quest.shared.events.quest.QuestUpdatedEvent;
import dn.quest.shared.events.team.TeamDeletedEvent;
import dn.quest.shared.events.team.TeamEvent;
import dn.quest.shared.events.user.UserDeletedEvent;
import dn.quest.shared.events.user.UserRegisteredEvent;
import dn.quest.shared.events.user.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Реализация EventProducer для публикации событий в Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventProducerImpl implements EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishFileUploadedEvent(FileUploadedEvent event) {
        publishEvent(KafkaTopics.FILE_EVENTS, event.getFileId().toString(), event);
    }

    @Override
    public void publishFileUpdatedEvent(FileUpdatedEvent event) {
        publishEvent(KafkaTopics.FILE_EVENTS, event.getFileId().toString(), event);
    }

    @Override
    public void publishFileDeletedEvent(FileDeletedEvent event) {
        publishEvent(KafkaTopics.FILE_EVENTS, event.getFileId().toString(), event);
    }

    @Override
    public void publishNotificationEvent(NotificationEvent event) {
        publishEvent(KafkaTopics.NOTIFICATION_EVENTS, event.getUserId().toString(), event);
    }

    @Override
    public void publishGameEvent(GameSessionStartedEvent event) {
        publishEvent(KafkaTopics.GAME_EVENTS, event.getSessionId().toString(), event);
    }

    @Override
    public void publishQuestEvent(QuestCreatedEvent event) {
        publishEvent(KafkaTopics.QUEST_EVENTS, event.getQuestId().toString(), event);
    }

    @Override
    public void publishQuestEvent(QuestUpdatedEvent event) {
        publishEvent(KafkaTopics.QUEST_EVENTS, event.getQuestId().toString(), event);
    }

    @Override
    public void publishGameEvent(GameSessionFinishedEvent event) {
        publishEvent(KafkaTopics.GAME_EVENTS, event.getSessionId().toString(), event);
    }

    @Override
    public void publishGameEvent(CodeSubmittedEvent event) {
        publishEvent(KafkaTopics.GAME_EVENTS, event.getSessionId().toString(), event);
    }

    @Override
    public void publishGameEvent(LevelCompletedEvent event) {
        publishEvent(KafkaTopics.GAME_EVENTS, event.getSessionId().toString(), event);
    }

    @Override
    public void publishTeamCreatedEvent(TeamEvent.TeamCreatedEvent event) {
        publishEvent(KafkaTopics.TEAM_EVENTS, event.getTeamId().toString(), event);
    }

    @Override
    public void publishTeamUpdatedEvent(TeamEvent.TeamUpdatedEvent event) {
        publishEvent(KafkaTopics.TEAM_EVENTS, event.getTeamId().toString(), event);
    }

    @Override
    public void publishTeamDeletedEvent(TeamDeletedEvent event) {
        publishEvent(KafkaTopics.TEAM_EVENTS, event.getTeamId().toString(), event);
    }

    @Override
    public void publishTeamMemberAddedEvent(TeamEvent.TeamMemberAddedEvent event) {
        publishEvent(KafkaTopics.TEAM_EVENTS, event.getTeamId().toString(), event);
    }

    @Override
    public void publishTeamMemberRemovedEvent(TeamEvent.TeamMemberRemovedEvent event) {
        publishEvent(KafkaTopics.TEAM_EVENTS, event.getTeamId().toString(), event);
    }

    @Override
    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        publishEvent(KafkaTopics.USER_EVENTS, event.getUserId().toString(), event);
    }

    @Override
    public void publishUserUpdatedEvent(UserUpdatedEvent event) {
        publishEvent(KafkaTopics.USER_EVENTS, event.getUserId().toString(), event);
    }

    @Override
    public void publishUserDeletedEvent(UserDeletedEvent event) {
        publishEvent(KafkaTopics.USER_EVENTS, event.getUserId().toString(), event);
    }

    /**
     * Универсальный метод для публикации событий в Kafka
     */
    private void publishEvent(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[{}] to topic=[{}] with offset=[{}]", 
                        event, topic, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send message=[{}] to topic=[{}] due to : {}", 
                        event, topic, ex.getMessage());
            }
        });
    }
}
