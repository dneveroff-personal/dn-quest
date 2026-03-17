package dn.quest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class Helpers {

    private static final Logger logger = LoggerFactory.getLogger(Helpers.class);

    /**
     * Universal printer.
     *
     * @return lambda console logger.
     */
    @Bean
    public Consumer<String> cmdPrint() {
        return logger::info;
    }
}
