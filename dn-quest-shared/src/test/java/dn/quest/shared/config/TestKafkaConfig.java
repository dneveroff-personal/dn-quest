package dn.quest.shared.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

/**
 * Тестовая конфигурация Kafka для интеграционных тестов
 */
@TestConfiguration
public class TestKafkaConfig {

    @Bean
    @Primary
    public EmbeddedKafkaBroker embeddedKafkaBroker() {
        return new EmbeddedKafkaBroker(1, true, 1, 
                "user-events", "quest-events", "game-events", 
                "team-events", "file-events", "notification-events")
                .brokerProperty("listeners", "PLAINTEXT://localhost:9092")
                .brokerProperty("port", "9092");
    }

    @Bean
    @Primary
    public ConsumerFactory<String, String> consumerFactory(EmbeddedKafkaBroker embeddedKafka) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka);
        consumerProps.put("spring.json.trusted.packages", "dn.quest.shared.events");
        consumerProps.put("spring.json.value.default.type", "dn.quest.shared.events.BaseEvent");
        return new DefaultConsumerFactory<>(consumerProps);
    }

    @Bean
    @Primary
    public ProducerFactory<String, String> producerFactory(EmbeddedKafkaBroker embeddedKafka) {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        return new DefaultKafkaProducerFactory<>(producerProps);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}