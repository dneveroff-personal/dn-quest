
package dn.quest.shared.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

/**
 * Базовый консьюмер для обработки событий из Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Обработчик пользовательских событий
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        autoCreateTopics = "false",
        topicSuffixingStrategy = org.springframework.kafka.retry.TopicNameSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        exclude = {DeserializationException.class}
    )
    @KafkaListener(
        topics = "${dn-quest.kafka.topics.user-events:dn-quest.users.events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        processEvent(event, topic, partition, offset, acknowledgment, this::handleUserEvent);
    }

    /**
     * Обработчик событий квестов
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        autoCreateTopics = "false",
        topicSuffixingStrategy = org.springframework.kafka.retry.TopicNameSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        exclude = {DeserializationException.class}
    )
    @KafkaListener(
        topics = "${dn-quest.kafka.topics.quest-events:dn-quest.quests.events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleQuestEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        processEvent(event, topic, partition, offset, acknowledgment, this::handleQuestEvent);
    }

    /**
     * Обработчик игровых событий
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        autoCreateTopics = "false",
        topicSuffixingStrategy = org.springframework.kafka.retry.TopicNameSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        exclude = {DeserializationException.class}
    )
    @KafkaListener(
        topics = "${dn-quest.kafka.topics.game-events:dn-quest.game.events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleGameEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        processEvent(event, topic, partition, offset, acknowledgment, this::handleGameEvent);
    }

    /**
     * Обработчик командных событий
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        autoCreateTopics = "false",
        topicSuffixingStrategy = org.springframework.kafka.retry.TopicNameSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        exclude = {DeserializationException.class}
    )
    @KafkaListener(
        topics = "${dn-quest.kafka.topics.team-events:dn-quest.teams.events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTeamEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        processEvent(event, topic, partition, offset, acknowledgment, this::handleTeamEvent);
    }

    /**
     * Обработчик файловых событий
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        autoCreateTopics = "false",
        topicSuffixingStrategy = org.springframework.kafka.retry.TopicNameSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        exclude = {DeserializationException.class}
    )
    @KafkaListener(
        topics = "${dn-quest.kafka.topics.file-events:dn-quest.files.events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleFileEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        processEvent(event, topic, partition, offset, acknowledgment, this::handleFileEvent);
    }

    /**
     * Обработчик событий уведомлений
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        autoCreateTopics = "false",
        topicSuffixingStrategy = org.springframework.kafka.retry.TopicNameSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        exclude = {DeserializationException.class}
    )
    @KafkaListener(
        topics = "${dn-quest.kafka.topics.notification-events:dn-quest.notifications.events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleNotificationEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        processEvent(event, topic, partition, offset, acknowledgment, this::handleNotificationEvent);
    }

    /**
     * Обработчик статистических событий
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        autoCreateTopics = "false",
        topicSuffixingStrategy = org.springframework.kafka.retry.TopicNameSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        exclude = {DeserializationException.class}
    )
    @KafkaListener(
        topics = "${dn-quest.kafka.topics.statistics-events:dn-quest.statistics.events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStatisticsEvents(
            @Payload BaseEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        processEvent(event, topic, partition, offset, acknowledgment, this::handleStatisticsEvent);
    }

    /**
     * Общая логика обработки событий
     */
    private void processEvent(BaseEvent event, String topic, int partition, long offset, 
                            Acknowledgment acknowledgment, BiConsumer<BaseEvent, String> eventHandler) {
        try {
            log.info("Received event: {} from topic: {}, partition: {}, offset: {}", 
                    event.getEventType(), topic, partition, offset);
            
            // Проверка на дубликаты (идемпотентность)
            if (isDuplicateEvent(event)) {
                log.warn("Duplicate event detected: {} with eventId: {}", 
                        event.getEventType(), event.getEventId());
                acknowledgment.acknowledge();
                return;
            }
            
            // Обработка события
            eventHandler.accept(event, topic);
            
            acknowledgment.acknowledge();
            log.info("Event processed successfully: {}", event.getEventType());
            
        } catch (Exception e) {
            log.error("Error processing event: {} from topic: {}", event.getEventType(), topic, e);
            // В зависимости от стратегии можно либо подтвердить, либо оставить для повторной обработки
            acknowledgment.acknowledge();
        }
    }

    /**
     * Проверка на дубликаты событий (простая реализация)
     */
    private boolean isDuplicateEvent(BaseEvent event) {
        // Здесь можно реализовать проверку в Redis или базе данных
        // Для простоты возвращаем false
        return false;
    }

    /**
     * Message converter для JSON
     */
    @Bean
    public RecordMessageConverter converter() {
        return new StringJsonMessageConverter();
    }

    /**
     * Обработчик ошибок с отправкой в DLQ
     */
    @Bean
    public SeekToCurrentErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
        return new SeekToCurrentErrorHandler(
            new DeadLetterPublishingRecoverer(template, (record, ex) -> {
                if (record.topic().endsWith(".dlq")) {
                    return null; // Не отправлять в DLQ если уже в DLQ
                }
                return record.topic() + ".dlq";
            }),
            new org.springframework.kafka.listener.FixedBackOff(1000L, 3)
        );
    }

    // Абстрактные методы для обработки конкретных типов событий
    protected void handleUserEvent(BaseEvent event, String topic) {
        log.info("Processing user event: {}", event.getEventType());
        // Переопределяется в конкретных сервисах
    }

    protected void handleQuestEvent(BaseEvent event, String topic) {
        log.info("Processing quest event: {}", event.getEventType());
        // Переопределяется в конкретных сервисах
    }

    protected void handleGameEvent(BaseEvent event, String topic) {
        log.info("Processing game event: {}", event.getEventType());
        // Переопределяется в конкретных сервисах
    }

    protected void handleTeamEvent(BaseEvent event, String topic) {
        log.info("Processing team event: {}", event.getEventType());
        // Переопределяется в конкретных сервисах
    }

    protected void handleFileEvent(BaseEvent event, String topic) {
        log.info("Processing file event: {}", event.getEventType());
        // Переопределяется в конкретных сервисах
    }

    protected void handleNotificationEvent(BaseEvent event, String topic) {
        log.info("Processing notification event: {}", event.getEventType());
        // Переопределяется в конкретных сервисах
    }

    protected void handleStatisticsEvent(BaseEvent event, String topic) {
        log.info("Processing statistics event: {}", event.getEventType());
        // Переопределяется в конкретных сервисах
    }
}
