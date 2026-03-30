package dn.quest.statistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Основной класс приложения Statistics Service
 */
@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableJpaAuditing
@EnableKafka
@EnableAsync
@ComponentScan(basePackages = {
        "dn.quest.statistics",
        "dn.quest.shared"
})
public class StatisticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatisticsServiceApplication.class, args);
    }
}