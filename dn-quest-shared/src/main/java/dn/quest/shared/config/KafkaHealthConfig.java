package dn.quest.shared.config;

import dn.quest.shared.health.KafkaHealthIndicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;

/**
 * Конфигурация health check для Kafka
 */
@Configuration
public class KafkaHealthConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public HealthIndicator kafkaHealthIndicator(ConsumerFactory<String, String> consumerFactory) {
        return new KafkaHealthIndicator(bootstrapServers, consumerFactory);
    }
}