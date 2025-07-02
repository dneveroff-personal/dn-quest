package dn.quizengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class QuizEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizEngineApplication.class, args);
    }

}
