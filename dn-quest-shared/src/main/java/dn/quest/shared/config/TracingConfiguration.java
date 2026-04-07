package dn.quest.shared.config;

import brave.sampler.Sampler;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация распределенного трейсинга для DN Quest
 * Использует Spring Cloud Sleuth (Brave) для трейсинга
 */
@Configuration
@ConditionalOnEnabledTracing
@ConditionalOnProperty(name = "dn.quest.tracing.enabled", havingValue = "true", matchIfMissing = true)
public class TracingConfiguration {

    /**
     * Sampler для трейсинга - 10% для production, 100% для development
     */
    @Bean
    public Sampler sampler() {
        String profile = System.getProperty("spring.profiles.active", "development");
        if ("production".equals(profile)) {
            return Sampler.create(0.1f); // 10% для production
        } else {
            return Sampler.create(1.0f); // 100% для development и testing
        }
    }

    /**
     * Утилитарный класс для работы с трейсингом
     */
    @Bean
    public TracingUtils tracingUtils(Tracer tracer) {
        return new TracingUtils(tracer);
    }

    /**
     * Утилитарный класс для работы с трейсингом
     */
    public static class TracingUtils {
        private final Tracer tracer;

        public TracingUtils(Tracer tracer) {
            this.tracer = tracer;
        }

        /**
         * Создание нового спана с именем
         */
        public io.micrometer.tracing.Span startSpan(String spanName) {
            return tracer.nextSpan().name(spanName).start();
        }

        /**
         * Добавление тегов к спану
         */
        public void addTag(io.micrometer.tracing.Span span, String key, String value) {
            span.tag(key, value);
        }

        /**
         * Добавление бизнес-тегов к спану
         */
        public void addBusinessTags(io.micrometer.tracing.Span span, String userId, String sessionId, String requestId) {
            if (userId != null) {
                span.tag("user.id", userId);
            }
            if (sessionId != null) {
                span.tag("session.id", sessionId);
            }
            if (requestId != null) {
                span.tag("request.id", requestId);
            }
            span.tag("service.name", getServiceName());
            span.tag("service.version", getServiceVersion());
            span.tag("environment", getEnvironment());
        }

        /**
         * Добавление тегов ошибки к спану
         */
        public void addErrorTags(io.micrometer.tracing.Span span, Exception exception) {
            span.tag("error", "true");
            span.tag("error.type", exception.getClass().getSimpleName());
            span.tag("error.message", exception.getMessage());
        }

        /**
         * Добавление тегов HTTP запроса к спану
         */
        public void addHttpTags(io.micrometer.tracing.Span span, String method, String url, int statusCode, long duration) {
            span.tag("http.method", method);
            span.tag("http.url", url);
            span.tag("http.status_code", String.valueOf(statusCode));
            span.tag("http.duration_ms", String.valueOf(duration));
            
            if (statusCode >= 400) {
                span.tag("error", "true");
            }
        }

        /**
         * Добавление тегов операции с базой данных к спану
         */
        public void addDatabaseTags(io.micrometer.tracing.Span span, String operation, String table, long duration) {
            span.tag("db.operation", operation);
            span.tag("db.table", table);
            span.tag("db.duration_ms", String.valueOf(duration));
            span.tag("db.system", "postgresql");
        }

        /**
         * Добавление тегов операции с Kafka к спану
         */
        public void addKafkaTags(io.micrometer.tracing.Span span, String operation, String topic, String partition) {
            span.tag("messaging.system", "kafka");
            span.tag("messaging.operation", operation);
            span.tag("messaging.destination", topic);
            if (partition != null) {
                span.tag("messaging.kafka.partition", partition);
            }
        }

        /**
         * Добавление тегов операции с Redis к спану
         */
        public void addRedisTags(io.micrometer.tracing.Span span, String operation, String key, long duration) {
            span.tag("db.system", "redis");
            span.tag("db.operation", operation);
            if (key != null) {
                span.tag("redis.key", key);
            }
            span.tag("db.duration_ms", String.valueOf(duration));
        }

        /**
         * Получение текущего trace ID
         */
        public String getCurrentTraceId() {
            return tracer.currentSpan().context().traceId();
        }

        /**
         * Получение текущего span ID
         */
        public String getCurrentSpanId() {
            return tracer.currentSpan().context().spanId();
        }

        /**
         * Проверка наличия активного спана
         */
        public boolean isCurrentSpanActive() {
            return tracer.currentSpan() != null;
        }

        private String getServiceName() {
            return System.getProperty("spring.application.name", "unknown-service");
        }

        private String getServiceVersion() {
            return System.getProperty("dn.quest.service.version", "1.0.0");
        }

        private String getEnvironment() {
            return System.getProperty("spring.profiles.active", "development");
        }
    }
}