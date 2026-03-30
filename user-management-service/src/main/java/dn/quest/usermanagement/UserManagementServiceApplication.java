package dn.quest.usermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Основной класс приложения User Management Service
 */
@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableJpaAuditing
@EnableAsync
@ComponentScan(basePackages = {
        "dn.quest.usermanagement",
        "dn.quest.shared"
})
public class UserManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementServiceApplication.class, args);
    }
}