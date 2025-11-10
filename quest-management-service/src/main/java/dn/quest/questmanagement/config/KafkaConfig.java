package dn.quest.questmanagement.config;

import dn.quest.shared.config.KafkaConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Конфигурация Kafka для Quest Management Service
 * Использует общую конфигурацию из dn-quest-shared
 */
@Configuration
@Import(KafkaConfiguration.class)
public class KafkaConfig {
    // Дополнительная конфигурация специфичная для Quest Management Service
    // может быть добавлена здесь при необходимости
}