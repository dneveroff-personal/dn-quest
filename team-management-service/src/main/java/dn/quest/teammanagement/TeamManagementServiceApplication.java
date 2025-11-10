package dn.quest.teammanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Основной класс Team Management Service
 */
@SpringBootApplication(scanBasePackages = {
    "dn.quest.teammanagement",
    "dn.quest.shared"
})
@EnableFeignClients(basePackages = "dn.quest.teammanagement.client")
@EnableKafka
@EnableCaching
@EnableAsync
@EnableScheduling
public class TeamManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamManagementServiceApplication.class, args);
    }
}