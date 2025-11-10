package dn.quest.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Конфигурация планировщика задач
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Включение планировщика для обработки очереди уведомлений
}