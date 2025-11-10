package dn.quest.filestorage.config;

import dn.quest.shared.config.KafkaConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Конфигурация Kafka для File Storage Service
 * Импортирует общую конфигурацию из dn-quest-shared
 */
@Configuration
@Import(KafkaConfiguration.class)
public class KafkaConfig {
    // Конфигурация импортируется из dn-quest-shared
}