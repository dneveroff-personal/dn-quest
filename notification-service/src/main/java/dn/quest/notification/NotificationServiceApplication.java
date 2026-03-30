package dn.quest.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Основной класс приложения Notification Service
 */
@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableAsync
@ComponentScan(basePackages = {
        "dn.quest.notification",
        "dn.quest.shared"
})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}