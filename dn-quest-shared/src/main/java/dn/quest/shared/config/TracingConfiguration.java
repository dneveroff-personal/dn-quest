package dn.quest.shared.config;

import brave.baggage.BaggageField;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationCustomizer;
import brave.propagation.B3Propagation;
import brave.propagation.Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.jaeger.thrift.JaegerThriftSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import zipkin2.reporter.Sender;
import zipkin2.reporter.okhttp3.OkHttpSender;

import java.util.List;

/**
 * Конфигурация распределенного трейсинга для DN Quest
 * Поддерживает как Brave (Spring Cloud Sleuth) так и OpenTelemetry
 */
@Configuration
@ConditionalOnEnabledTracing
@ConditionalOnProperty(name = "dn.quest.tracing.enabled", havingValue = "true", matchIfMissing = true)
public class TracingConfiguration {

    /**
     * Конфигурация кастомных полей baggage для трейсинга
     */
    @Bean
    public BaggagePropagationCustomizer<BaggagePropagation.FactoryBuilder> baggagePropagationCustomizer() {
        return builder -> {
            // Добавляем кастомные поля для бизнес-контекста
            builder.add(BaggageField.newBuilder("user-id").build())
                    .add(BaggageField.newBuilder("request-id").build())
                    .add(BaggageField.newBuilder("session-id").build())
                    .add(BaggageField.newBuilder("tenant-id").build())
                    .add(BaggageField.newBuilder("correlation-id").build())
                    .add(BaggageField.newBuilder("service-version").build())
                    .add(BaggageField.newBuilder("environment").build());
        };
    }

    /**
     * Конфигурация propagation для трейсинга
     */
    @Bean
    public Propagation.Factory propagationFactory() {
        return BaggagePropagation.newFactoryBuilder(B3Propagation.FACTORY)
                .build();
    }

    /**
     * Конфигурация контекста трейсинга
     */
    @Bean
    public ThreadLocalCurrentTraceContext currentTraceContext() {
        return ThreadLocalCurrentTraceContext.newBuilder()
                .addThreadLocalConverter()
                .build();
    }

    /**
     * Sampler для трейсинга - 10% для production, 100% для development
     */
    @Bean
    public Sampler sampler() {
        String profile = System.getProperty("spring.profiles.active", "development");
        if ("production".equals(profile)) {
            return Sampler.create(0.1); // 10% для production
        } else {
            return Sampler.create(1.0); // 100% для development и testing
        }
    }

    /**
     * Sender для отправки спанов в Jaeger
     */
    @Bean
    @ConditionalOnProperty(name = "dn.quest.tracing.jaeger.enabled", havingValue = "true", matchIfMissing = true)
    public Sender jaegerSender() {
        String jaegerEndpoint = System.getProperty("dn.quest.tracing.jaeger.endpoint", "http://localhost:14268/api/v2/spans");
        return OkHttpSender.create(jaegerEndpoint);
    }

    /**
     * Конфигурация OpenTelemetry для интеграции с Jaeger
     */
    @Bean
    @ConditionalOnClass(name = "io.opentelemetry.exporter.jaeger.thrift.JaegerThriftSpanExporter")
    @ConditionalOnProperty(name = "dn.quest.tracing.opentelemetry.enabled", havingValue = "true")
    public OpenTelemetrySdk openTelemetrySdk() {
        String jaegerEndpoint = System.getProperty("dn.quest.tracing.jaeger.endpoint", "http://localhost:14268/api/v2/spans");
        
        JaegerThriftSpanExporter jaegerExporter = JaegerThriftSpanExporter.builder()
                .setEndpoint(jaegerEndpoint)
                .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(jaegerExporter).build())
                .setSampler(io.opentelemetry.sdk.trace.samplers.Sampler.traceIdRatioBased(getSamplingRatio()))
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
    }

    /**
     * OpenTelemetry Tracer
     */
    @Bean
    @ConditionalOnClass(name = "io.opentelemetry.api.trace.Tracer")
    @ConditionalOnProperty(name = "dn.quest.tracing.opentelemetry.enabled", havingValue = "true")
    public io.opentelemetry.api.trace.Tracer openTelemetryTracer(OpenTelemetrySdk openTelemetry) {
        return openTelemetry.getTracer("dn-quest", "1.0.0");
    }

    /**
     * Brave Tracer (для Spring Cloud Sleuth)
     */
    @Bean
    @Primary
    @ConditionalOnClass(name = "brave.Tracer")
    public BraveTracer braveTracer(brave.Tracer braveTracer, BraveBaggageManager baggageManager) {
        return new BraveTracer(braveTracer, baggageManager);
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
        public Span startSpan(String spanName) {
            return tracer.nextSpan().name(spanName).start();
        }

        /**
         * Создание дочернего спана
         */
        public Span startChildSpan(String spanName, Span parentSpan) {
            return tracer.spanBuilder(spanName)
                    .setParent(parentSpan)
                    .startSpan();
        }

        /**
         * Добавление тегов к спану
         */
        public void addTag(Span span, String key, String value) {
            span.setAttribute(key, value);
        }

        /**
         * Добавление бизнес-тегов к спану
         */
        public void addBusinessTags(Span span, String userId, String sessionId, String requestId) {
            if (userId != null) {
                span.setAttribute("user.id", userId);
            }
            if (sessionId != null) {
                span.setAttribute("session.id", sessionId);
            }
            if (requestId != null) {
                span.setAttribute("request.id", requestId);
            }
            span.setAttribute("service.name", getServiceName());
            span.setAttribute("service.version", getServiceVersion());
            span.setAttribute("environment", getEnvironment());
        }

        /**
         * Добавление тегов ошибки к спану
         */
        public void addErrorTags(Span span, Exception exception) {
            span.setAttribute("error", true);
            span.setAttribute("error.type", exception.getClass().getSimpleName());
            span.setAttribute("error.message", exception.getMessage());
            span.recordException(exception);
        }

        /**
         * Добавление тегов HTTP запроса к спану
         */
        public void addHttpTags(Span span, String method, String url, int statusCode, long duration) {
            span.setAttribute("http.method", method);
            span.setAttribute("http.url", url);
            span.setAttribute("http.status_code", statusCode);
            span.setAttribute("http.duration_ms", duration);
            
            // Добавляем статус ошибки если код не успешный
            if (statusCode >= 400) {
                span.setAttribute("error", true);
            }
        }

        /**
         * Добавление тегов операции с базой данных к спану
         */
        public void addDatabaseTags(Span span, String operation, String table, long duration) {
            span.setAttribute("db.operation", operation);
            span.setAttribute("db.table", table);
            span.setAttribute("db.duration_ms", duration);
            span.setAttribute("db.system", "postgresql");
        }

        /**
         * Добавление тегов операции с Kafka к спану
         */
        public void addKafkaTags(Span span, String operation, String topic, String partition) {
            span.setAttribute("messaging.system", "kafka");
            span.setAttribute("messaging.operation", operation);
            span.setAttribute("messaging.destination", topic);
            if (partition != null) {
                span.setAttribute("messaging.kafka.partition", partition);
            }
        }

        /**
         * Добавление тегов операции с Redis к спану
         */
        public void addRedisTags(Span span, String operation, String key, long duration) {
            span.setAttribute("db.system", "redis");
            span.setAttribute("db.operation", operation);
            if (key != null) {
                span.setAttribute("redis.key", key);
            }
            span.setAttribute("db.duration_ms", duration);
        }

        /**
         * Выполнение операции в контексте спана
         */
        public <T> T trace(String spanName, ThrowableSupplier<T> supplier) {
            Span span = startSpan(spanName);
            try (Scope scope = tracer.withSpan(span)) {
                return supplier.get();
            } catch (Exception e) {
                addErrorTags(span, e);
                throw e;
            } finally {
                span.end();
            }
        }

        /**
         * Выполнение операции в контексте спана с бизнес-тегами
         */
        public <T> T traceWithBusinessContext(String spanName, String userId, String sessionId, 
                                           String requestId, ThrowableSupplier<T> supplier) {
            Span span = startSpan(spanName);
            try (Scope scope = tracer.withSpan(span)) {
                addBusinessTags(span, userId, sessionId, requestId);
                return supplier.get();
            } catch (Exception e) {
                addErrorTags(span, e);
                throw e;
            } finally {
                span.end();
            }
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
            return tracer.currentSpan() != null && tracer.currentSpan().isRecording();
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

        private double getSamplingRatio() {
            String profile = System.getProperty("spring.profiles.active", "development");
            return "production".equals(profile) ? 0.1 : 1.0;
        }
    }

    @FunctionalInterface
    public interface ThrowableSupplier<T> {
        T get() throws Exception;
    }
}