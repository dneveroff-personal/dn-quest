package dn.quest.notification.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Конфигурация асинхронной обработки уведомлений
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Executor для обработки уведомлений
     */
    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Основные настройки пула потоков
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notification-");
        
        // Настройки времени ожидания
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        
        // Политика отклонения задач
        executor.setRejectedExecutionHandler((r, executor1) -> {
            logger.warn("Задача отклонена из-за переполнения очереди: {}", r.toString());
            // Попытка выполнить в текущем потоке
            if (!executor1.isShutdown()) {
                r.run();
            }
        });
        
        // Настройка ожидания завершения задач при shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Executor для пакетной обработки уведомлений
     */
    @Bean(name = "batchNotificationTaskExecutor")
    public Executor batchNotificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Для пакетной обработки используем меньше потоков
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("batch-notification-");
        
        executor.setKeepAliveSeconds(120);
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.setRejectedExecutionHandler((r, executor1) -> {
            logger.warn("Пакетная задача отклонена: {}", r.toString());
            if (!executor1.isShutdown()) {
                r.run();
            }
        });
        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }

    /**
     * Executor для обработки retry логики
     */
    @Bean(name = "retryNotificationTaskExecutor")
    public Executor retryNotificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Для retry используем отдельный пул с ограниченным количеством потоков
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("retry-notification-");
        
        executor.setKeepAliveSeconds(300);
        executor.setAllowCoreThreadTimeOut(false); // Ядровые потоки всегда активны
        
        executor.setRejectedExecutionHandler((r, executor1) -> {
            logger.warn("Retry задача отклонена: {}", r.toString());
            if (!executor1.isShutdown()) {
                r.run();
            }
        });
        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        
        executor.initialize();
        return executor;
    }

    /**
     * Executor для WebSocket уведомлений
     */
    @Bean(name = "webSocketNotificationTaskExecutor")
    public Executor webSocketNotificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Для WebSocket используем быстрые потоки
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("websocket-notification-");
        
        executor.setKeepAliveSeconds(30);
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.setRejectedExecutionHandler((r, executor1) -> {
            logger.warn("WebSocket задача отклонена: {}", r.toString());
            if (!executor1.isShutdown()) {
                r.run();
            }
        });
        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return notificationTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    /**
     * Обработчик исключений для асинхронных задач
     */
    private static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        private static final Logger logger = LoggerFactory.getLogger(CustomAsyncExceptionHandler.class);

        @Override
        public void handleUncaughtException(Throwable ex, java.lang.reflect.Method method, Object... params) {
            logger.error("Необработанное исключение в асинхронном методе: {} - {}", 
                        method.getName(), ex.getMessage(), ex);
            
            // Дополнительная логика для обработки критических ошибок
            if (ex instanceof RuntimeException) {
                logger.error("RuntimeException в асинхронной задаче", ex);
            }
        }
    }
}