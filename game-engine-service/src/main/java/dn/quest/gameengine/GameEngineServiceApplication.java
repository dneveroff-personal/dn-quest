package dn.quest.gameengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Основной класс приложения Game Engine Service
 * Микросервис для управления игровыми сессиями, попытками ввода кодов и игровой логикой
 */
@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableJpaAuditing
@EnableAsync
@EnableKafka
@EnableScheduling
@ComponentScan(basePackages = {
        "dn.quest.gameengine",
        "dn.quest.shared"
})
public class GameEngineServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameEngineServiceApplication.class, args);
    }
}