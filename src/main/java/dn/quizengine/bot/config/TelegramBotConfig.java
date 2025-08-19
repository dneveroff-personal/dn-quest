package dn.quizengine.bot.config;

import dn.quizengine.bot.QuizTelegramBot;
import dn.quizengine.service.QuizService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Configuration
public class TelegramBotConfig {
    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    public TelegramLongPollingBot quizBot(QuizService quizService) {
        return new QuizTelegramBot(botToken, quizService);
    }
}

