package dn.quest.shared.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.OptionHelper;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * Конфигурация структурированного логирования для DN Quest
 * Поддерживает JSON формат для ELK stack и консольный вывод для разработки
 */
@Configuration
@ConditionalOnProperty(name = "dn.quest.logging.enabled", havingValue = "true", matchIfMissing = true)
public class LoggingConfiguration {

    @PostConstruct
    public void configureLogging() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // Добавляем JSON аппендер для ELK
        addJsonFileAppender(loggerContext);
        
        // Добавляем консольный аппендер
        addConsoleAppender(loggerContext);
        
        // Настраиваем корневой логгер
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.setAdditive(false);
    }

    private void addJsonFileAppender(LoggerContext loggerContext) {
        // JSON аппендер для отправки в ELK
        RollingFileAppender<ILoggingEvent> jsonFileAppender = new RollingFileAppender<>();
        jsonFileAppender.setContext(loggerContext);
        jsonFileAppender.setName("JSON_FILE");
        jsonFileAppender.setFile(OptionHelper.getSystemProperty("dn.quest.logging.file.path", "logs/dn-quest.log"));
        
        // Политика ротации логов
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(jsonFileAppender);
        rollingPolicy.setFileNamePattern("logs/dn-quest.%d{yyyy-MM-dd}.%i.log.gz");
        rollingPolicy.setMaxFileSize(ch.qos.logback.core.util.FileSize.valueOf("100MB"));
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.setTotalSizeCap(ch.qos.logback.core.util.FileSize.valueOf("1GB"));
        rollingPolicy.start();
        
        jsonFileAppender.setRollingPolicy(rollingPolicy);
        
        // JSON энкодер
        LogstashLogbackEncoder jsonEncoder = new LogstashLogbackEncoder();
        jsonEncoder.setContext(loggerContext);
        jsonEncoder.setCustomFields("{\"service_name\":\"" + getServiceName() + 
                                   "\",\"service_version\":\"" + getServiceVersion() + 
                                   "\",\"environment\":\"" + getEnvironment() + "\"}");
        jsonEncoder.start();
        
        jsonFileAppender.setEncoder(jsonEncoder);
        jsonFileAppender.start();
        
        // Добавляем аппендер к корневому логгеру
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(jsonFileAppender);
    }

    private void addConsoleAppender(LoggerContext loggerContext) {
        // Консольный аппендер для разработки
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName("CONSOLE");
        
        // Паттерн для консольного вывода
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n");
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();
        
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        
        // Добавляем аппендер только для development профиля
        if ("development".equals(getEnvironment())) {
            Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.addAppender(consoleAppender);
        }
    }

    private String getServiceName() {
        return System.getProperty("spring.application.name", "dn-quest-service");
    }

    private String getServiceVersion() {
        return System.getProperty("dn.quest.service.version", "1.0.0");
    }

    private String getEnvironment() {
        return System.getProperty("spring.profiles.active", "development");
    }
}