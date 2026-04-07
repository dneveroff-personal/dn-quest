package dn.quest.questmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Основной класс приложения Quest Management Service
 */
@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableJpaAuditing
@EnableAsync
@ComponentScan(basePackages = {
        "dn.quest.questmanagement",
        "dn.quest.shared"
})
public class QuestManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestManagementServiceApplication.class, args);
    }
}