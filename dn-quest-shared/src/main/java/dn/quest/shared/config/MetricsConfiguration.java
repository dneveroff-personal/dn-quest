package dn.quest.shared.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.cache.CacheMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.concurrent.TimeUnit;

/**
 * Конфигурация метрик для всех микросервисов DN Quest
 * Предоставляет общие метрики JVM, HTTP, кэша и бизнес-метрики
 */
@Configuration
@EnableAspectJAutoProxy
@ConditionalOnProperty(name = "management.metrics.enabled", havingValue = "true", matchIfMissing = true)
public class MetricsConfiguration {

    /**
     * Создание реестра метрик Prometheus
     */
    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    /**
     * Кастомизация реестра метрик с общими тегами
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(
                        "application", "dn-quest",
                        "environment", System.getProperty("spring.profiles.active", "unknown")
                );
    }

    /**
     * Аспект для автоматического измерения времени выполнения методов с аннотацией @Timed
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Аспект для автоматического подсчета вызовов методов с аннотацией @Counted
     */
    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }

    /**
     * JVM метрики памяти
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * JVM метрики потоков
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    /**
     * JVM метрики сборщика мусора
     */
    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    /**
     * Метрики процессора
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * Метрики времени работы приложения
     */
    @Bean
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }

    /**
     * Метрики логирования (если доступен Logback)
     */
    @Bean
    @ConditionalOnClass(name = "ch.qos.logback.classic.Logger")
    public LogbackMetrics logbackMetrics() {
        return new LogbackMetrics();
    }

    /**
     * Метрики кэша (если доступен Caffeine)
     */
    @Bean
    @ConditionalOnClass(CaffeineCacheManager.class)
    @ConditionalOnProperty(name = "management.metrics.cache.enabled", havingValue = "true", matchIfMissing = true)
    public CacheMetrics cacheMetrics(CacheManager cacheManager) {
        if (cacheManager instanceof CaffeineCacheManager) {
            CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;
            return new CacheMetrics(caffeineCacheManager.getCacheNames().stream()
                    .map(name -> (Cache<?, ?>) caffeineCacheManager.getCache(name).getNativeCache())
                    .toList());
        }
        return null;
    }

    /**
     * Утилитарный класс для создания бизнес-метрик
     */
    @Bean
    public BusinessMetrics businessMetrics(MeterRegistry meterRegistry) {
        return new BusinessMetrics(meterRegistry);
    }

    /**
     * Класс для бизнес-метрик DN Quest
     */
    public static class BusinessMetrics {
        private final MeterRegistry meterRegistry;

        public BusinessMetrics(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        /**
         * Регистрация успешной аутентификации
         */
        public void recordAuthenticationSuccess(String method) {
            meterRegistry.counter("dn.quest.authentication.success", "method", method).increment();
        }

        /**
         * Регистрация неудачной аутентификации
         */
        public void recordAuthenticationFailure(String method, String reason) {
            meterRegistry.counter("dn.quest.authentication.failure", "method", method, "reason", reason).increment();
        }

        /**
         * Регистрация создания пользователя
         */
        public void recordUserCreated() {
            meterRegistry.counter("dn.quest.users.created").increment();
        }

        /**
         * Регистрация создания команды
         */
        public void recordTeamCreated() {
            meterRegistry.counter("dn.quest.teams.created").increment();
        }

        /**
         * Регистрация создания квеста
         */
        public void recordQuestCreated(String difficulty) {
            meterRegistry.counter("dn.quest.quests.created", "difficulty", difficulty).increment();
        }

        /**
         * Регистрация начала игровой сессии
         */
        public void recordGameSessionStarted(String questType) {
            meterRegistry.counter("dn.quest.game.sessions.started", "quest_type", questType).increment();
        }

        /**
         * Регистрация завершения игровой сессии
         */
        public void recordGameSessionCompleted(String questType, boolean success) {
            meterRegistry.counter("dn.quest.game.sessions.completed", 
                    "quest_type", questType, "success", String.valueOf(success)).increment();
        }

        /**
         * Регистрация отправки уведомления
         */
        public void recordNotificationSent(String type, String channel) {
            meterRegistry.counter("dn.quest.notifications.sent", 
                    "type", type, "channel", channel).increment();
        }

        /**
         * Регистрация загрузки файла
         */
        public void recordFileUploaded(String type, long size) {
            meterRegistry.counter("dn.quest.files.uploaded", "type", type).increment();
            meterRegistry.timer("dn.quest.files.upload.size").record(size, TimeUnit.BYTES);
        }

        /**
         * Регистрация запроса к API
         */
        public Timer startApiRequest(String method, String endpoint) {
            return Timer.builder("dn.quest.api.request")
                    .tag("method", method)
                    .tag("endpoint", endpoint)
                    .register(meterRegistry);
        }

        /**
         * Регистрация ошибки API
         */
        public void recordApiError(String method, String endpoint, int statusCode, String errorType) {
            meterRegistry.counter("dn.quest.api.errors",
                    "method", method,
                    "endpoint", endpoint,
                    "status_code", String.valueOf(statusCode),
                    "error_type", errorType).increment();
        }

        /**
         * Регистрация операции с базой данных
         */
        public Timer startDatabaseOperation(String operation, String table) {
            return Timer.builder("dn.quest.database.operation")
                    .tag("operation", operation)
                    .tag("table", table)
                    .register(meterRegistry);
        }

        /**
         * Регистрация операции с Kafka
         */
        public void recordKafkaOperation(String operation, String topic, boolean success) {
            meterRegistry.counter("dn.quest.kafka.operations",
                    "operation", operation,
                    "topic", topic,
                    "success", String.valueOf(success)).increment();
        }

        /**
         * Регистрация операции с Redis
         */
        public Timer startRedisOperation(String operation) {
            return Timer.builder("dn.quest.redis.operation")
                    .tag("operation", operation)
                    .register(meterRegistry);
        }

        /**
         * Регистрация бизнес-события
         */
        public void recordBusinessEvent(String eventType, String service) {
            meterRegistry.counter("dn.quest.business.events",
                    "event_type", eventType,
                    "service", service).increment();
        }

        /**
         * Регистрация метрик производительности
         */
        public void recordPerformanceMetric(String metricName, double value, String unit) {
            meterRegistry.gauge("dn.quest.performance." + metricName, value, 
                    "unit", unit);
        }

        /**
         * Регистрация SLA метрик
         */
        public void recordSlaMetric(String slaType, boolean compliant) {
            meterRegistry.counter("dn.quest.sla.metrics",
                    "sla_type", slaType,
                    "compliant", String.valueOf(compliant)).increment();
        }

        /**
         * Регистрация метрик безопасности
         */
        public void recordSecurityEvent(String eventType, String severity, String source) {
            meterRegistry.counter("dn.quest.security.events",
                    "event_type", eventType,
                    "severity", severity,
                    "source", source).increment();
        }

        /**
         * Регистрация метрик использования ресурсов
         */
        public void recordResourceUsage(String resourceType, double usage, String unit) {
            meterRegistry.gauge("dn.quest.resources.usage", usage,
                    "resource_type", resourceType,
                    "unit", unit);
        }
    }
}