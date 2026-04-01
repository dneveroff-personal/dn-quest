package dn.quest.shared.config;

import dn.quest.shared.constants.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Централизованная конфигурация Kafka для всех микросервисов
 */
@Slf4j
@Configuration
@EnableKafka
public class KafkaConfiguration {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:default-group}")
    private String consumerGroupId;

    @Value("${spring.kafka.producer.acks:all}")
    private String producerAcks;

    @Value("${spring.kafka.producer.retries:3}")
    private Integer producerRetries;

    @Value("${spring.kafka.producer.batch-size:16384}")
    private Integer producerBatchSize;

    @Value("${spring.kafka.producer.linger-ms:1}")
    private Integer producerLingerMs;

    @Value("${spring.kafka.producer.buffer-memory:33554432}")
    private Integer producerBufferMemory;

    @Value("${spring.kafka.producer.compression-type:snappy}")
    private String producerCompressionType;

    @Value("${spring.kafka.consumer.max-poll-records:500}")
    private Integer consumerMaxPollRecords;

    @Value("${spring.kafka.consumer.session-timeout-ms:30000}")
    private Integer consumerSessionTimeoutMs;

    @Value("${spring.kafka.consumer.heartbeat-interval-ms:10000}")
    private Integer consumerHeartbeatIntervalMs;

    @Value("${spring.kafka.consumer.concurrency:3}")
    private Integer consumerConcurrency;

    // В dev = 1 брокер → replication factor = 1
    // В prod = 3 брокера → replication factor = 3
    @Value("${kafka.topics.replication-factor:1}")
    private short replicationFactor;

    @Value("${kafka.topics.partitions.default:1}")
    private int defaultPartitions;

    /**
     * Конфигурация продюсера Kafka
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, producerAcks);
        configProps.put(ProducerConfig.RETRIES_CONFIG, producerRetries);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, producerBatchSize);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, producerLingerMs);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, producerBufferMemory);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, producerCompressionType);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Добавление доверенных пакетов для JsonDeserializer
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        configProps.put(JsonSerializer.TYPE_MAPPINGS,
                "BaseEvent:dn.quest.shared.events.BaseEvent," +
                        "UserEvent:dn.quest.shared.events.user.UserEvent," +
                        "QuestEvent:dn.quest.shared.events.quest.QuestEvent," +
                        "GameEvent:dn.quest.shared.events.game.GameEvent," +
                        "TeamEvent:dn.quest.shared.events.team.TeamEvent," +
                        "FileEvent:dn.quest.shared.events.file.FileEvent");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate для отправки сообщений
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        template.setDefaultTopic(KafkaTopics.USER_EVENTS);
        return template;
    }

    /**
     * Конфигурация консьюмера Kafka
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, consumerMaxPollRecords);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, consumerSessionTimeoutMs);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, consumerHeartbeatIntervalMs);
        configProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        // Добавление доверенных пакетов для JsonDeserializer
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES,
                "dn.quest.shared.events," +
                        "dn.quest.shared.events.user," +
                        "dn.quest.shared.events.quest," +
                        "dn.quest.shared.events.game," +
                        "dn.quest.shared.events.team," +
                        "dn.quest.shared.events.file");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "dn.quest.shared.events.BaseEvent");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Фабрика контейнеров для слушателей Kafka
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(consumerConcurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.getContainerProperties().setIdleBetweenPolls(1000);

        // Настройка retry механизма
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());

        return factory;
    }

    /**
     * Конфигурация для администрирования Kafka
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Создание топиков при старте приложения.
     * Партиции и replication factor берутся из конфига:
     *   dev:  kafka.topics.replication-factor=1, kafka.topics.partitions.default=1
     *   prod: kafka.topics.replication-factor=3, kafka.topics.partitions.default=6
     */
    @Bean
    public NewTopic userEventsTopic() {
        return new NewTopic(KafkaTopics.USER_EVENTS, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic questEventsTopic() {
        return new NewTopic(KafkaTopics.QUEST_EVENTS, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic gameEventsTopic() {
        return new NewTopic(KafkaTopics.GAME_EVENTS, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic teamEventsTopic() {
        return new NewTopic(KafkaTopics.TEAM_EVENTS, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic fileEventsTopic() {
        return new NewTopic(KafkaTopics.FILE_EVENTS, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return new NewTopic(KafkaTopics.NOTIFICATION_EVENTS, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic statisticsEventsTopic() {
        return new NewTopic(KafkaTopics.STATISTICS_EVENTS, defaultPartitions, replicationFactor);
    }

    // DLQ топики
    @Bean
    public NewTopic userEventsDlqTopic() {
        return new NewTopic(KafkaTopics.USER_EVENTS_DLQ, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic questEventsDlqTopic() {
        return new NewTopic(KafkaTopics.QUEST_EVENTS_DLQ, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic gameEventsDlqTopic() {
        return new NewTopic(KafkaTopics.GAME_EVENTS_DLQ, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic teamEventsDlqTopic() {
        return new NewTopic(KafkaTopics.TEAM_EVENTS_DLQ, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic fileEventsDlqTopic() {
        return new NewTopic(KafkaTopics.FILE_EVENTS_DLQ, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic notificationEventsDlqTopic() {
        return new NewTopic(KafkaTopics.NOTIFICATION_EVENTS_DLQ, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic statisticsEventsDlqTopic() {
        return new NewTopic(KafkaTopics.STATISTICS_EVENTS_DLQ, defaultPartitions, replicationFactor);
    }

    @Bean
    public NewTopic generalDlqTopic() {
        return new NewTopic(KafkaTopics.GENERAL_DLQ, defaultPartitions, replicationFactor);
    }
}