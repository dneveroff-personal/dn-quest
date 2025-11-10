package dn.quest.shared.config;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.tracing.Tracer;
import org.slf4j.MDC;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Кастомный JSON энкодер для Logstash
 * Создает структурированные логи в формате JSON для ELK stack
 */
public class LogstashLogbackEncoder extends PatternLayoutEncoder {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String customFields = "{}";
    private Tracer tracer;

    public void setCustomFields(String customFields) {
        this.customFields = customFields;
    }

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        try {
            ObjectNode logEntry = objectMapper.createObjectNode();
            
            // Базовые поля лога
            logEntry.put("@timestamp", Instant.now().format(DateTimeFormatter.ISO_INSTANT));
            logEntry.put("level", event.getLevel().toString());
            logEntry.put("logger", event.getLoggerName());
            logEntry.put("thread", event.getThreadName());
            logEntry.put("message", event.getFormattedMessage());
            
            // Добавляем информацию об исключении
            if (event.getThrowableProxy() != null) {
                logEntry.put("exception_class", event.getThrowableProxy().getClassName());
                logEntry.put("exception_message", event.getThrowableProxy().getMessage());
                
                // Стек трейс
                StringBuilder stackTrace = new StringBuilder();
                for (ch.qos.logback.classic.spi.StackTraceElementProxy element : event.getThrowableProxy().getStackTraceElementProxyArray()) {
                    stackTrace.append(element.toString()).append("\n");
                }
                logEntry.put("stack_trace", stackTrace.toString());
            }
            
            // Добавляем MDC контекст
            Map<String, String> mdcProperties = MDC.getCopyOfContextMap();
            if (mdcProperties != null && !mdcProperties.isEmpty()) {
                ObjectNode mdcNode = objectMapper.createObjectNode();
                mdcProperties.forEach(mdcNode::put);
                logEntry.set("mdc", mdcNode);
            }
            
            // Добавляем трейсинг информацию
            if (tracer != null && tracer.currentSpan() != null) {
                logEntry.put("trace_id", tracer.currentSpan().context().traceId());
                logEntry.put("span_id", tracer.currentSpan().context().spanId());
            }
            
            // Добавляем кастомные поля
            if (customFields != null && !customFields.isEmpty()) {
                try {
                    ObjectNode customFieldsNode = (ObjectNode) objectMapper.readTree(customFields);
                    customFieldsNode.fields().forEachRemaining(entry -> {
                        logEntry.set(entry.getKey(), entry.getValue());
                    });
                } catch (JsonProcessingException e) {
                    // Игнорируем ошибки парсинга кастомных полей
                }
            }
            
            // Добавляем информацию о приложении
            logEntry.put("application", System.getProperty("spring.application.name", "dn-quest"));
            logEntry.put("version", System.getProperty("dn.quest.service.version", "1.0.0"));
            logEntry.put("environment", System.getProperty("spring.profiles.active", "development"));
            logEntry.put("host", getHostname());
            
            // Добавляем метаданные
            ObjectNode metadataNode = objectMapper.createObjectNode();
            metadataNode.put("pid", getProcessId());
            metadataNode.put("log_format", "logstash");
            logEntry.set("metadata", metadataNode);
            
            return objectMapper.writeValueAsBytes(logEntry);
            
        } catch (Exception e) {
            // В случае ошибки кодирования, возвращаем простой текстовый формат
            return String.format("[%s] %s %s - %s", 
                Instant.now().format(DateTimeFormatter.ISO_INSTANT),
                event.getLevel(),
                event.getLoggerName(),
                event.getFormattedMessage()
            ).getBytes();
        }
    }
    
    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private String getProcessId() {
        try {
            String jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            return jvmName.split("@")[0];
        } catch (Exception e) {
            return "unknown";
        }
    }
}