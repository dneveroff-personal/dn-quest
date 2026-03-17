package dn.quest.gameengine.config;

import dn.quest.shared.config.KafkaConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Конфигурация Kafka для Game Engine Service
 * Использует общую конфигурацию из dn-quest-shared
 */
@Configuration
@Import(KafkaConfiguration.class)
public class KafkaConfig {
    // Дополнительная конфигурация специфичная для Game Engine Service
    // может быть добавлена здесь при необходимости
}