package dn.quest.filestorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Основной класс приложения File Storage Service
 */
@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableJpaAuditing
@EnableAsync
@ComponentScan(basePackages = {
        "dn.quest.filestorage",
        "dn.quest.shared"
})
public class FileStorageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileStorageServiceApplication.class, args);
    }
}