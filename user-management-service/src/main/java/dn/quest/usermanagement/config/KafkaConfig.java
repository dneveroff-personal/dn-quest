package dn.quest.usermanagement.config;

import dn.quest.shared.config.KafkaConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Конфигурация Kafka для User Management Service
 * Импортирует общую конфигурацию из dn-quest-shared
 */
@Configuration
@Import(KafkaConfiguration.class)
public class KafkaConfig {
    // Конфигурация импортируется из dn-quest-shared
}