package dn.quest.shared.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Prometheus метрик для Kafka
 */
@Configuration
public class KafkaMetricsConfig {

    @Bean
    public Counter kafkaEventPublishedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kafka.events.published.total")
                .description("Total number of events published to Kafka")
                .tag("service", "dn-quest")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaEventConsumedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kafka.events.consumed.total")
                .description("Total number of events consumed from Kafka")
                .tag("service", "dn-quest")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaEventProcessingErrorsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kafka.events.processing.errors.total")
                .description("Total number of event processing errors")
                .tag("service", "dn-quest")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaEventRetryCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kafka.events.retry.total")
                .description("Total number of event processing retries")
                .tag("service", "dn-quest")
                .register(meterRegistry);
    }

    @Bean
    public Timer kafkaEventProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("kafka.events.processing.time")
                .description("Time taken to process events")
                .tag("service", "dn-quest")
                .register(meterRegistry);
    }

    @Bean
    public Timer kafkaEventPublishTimer(MeterRegistry meterRegistry) {
        return Timer.builder("kafka.events.publish.time")
                .description("Time taken to publish events")
                .tag("service", "dn-quest")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaEventPublishedByTypeCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kafka.events.published.by.type")
                .description("Number of events published by type")
                .tag("service", "dn-quest")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaEventConsumedByTypeCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kafka.events.consumed.by.type")
                .description("Number of events consumed by type")
                .tag("service", "dn-quest")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaEventPublishedByTopicCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kafka.events.published.by.topic")
                .description("Number of events published by topic")
                .tag("service", "dn-quest")
                .register(meterRegistry);
    }

    @Bean
    public Counter kafkaEventConsumedByTopicCounter(MeterRegistry meterRegistry) {
        return Counter.builder("kafka.events.consumed.by.topic")
                .description("Number of events consumed by topic")
                .tag("service", "dn-quest")
                .register(meterRegistry);
    }
}