package dn.quizengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class Helpers {

    /**
     * Universal printer.
     *
     * @return lambda console system out.
     */
    @Bean
    public Consumer<String> cmdPrint() {
        return System.out::println;
    }
}
