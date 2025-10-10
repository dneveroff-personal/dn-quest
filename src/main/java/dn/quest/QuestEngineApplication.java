package dn.quest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class QuestEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestEngineApplication.class, args);
    }

}
