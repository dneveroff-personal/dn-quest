package dn.quest.bot.config;

import dn.quest.bot.QuestTelegramBot;
import dn.quest.services.interfaces.QuestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Configuration
public class TelegramBotConfig {
    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    public TelegramLongPollingBot questBot(QuestService questService) {
        return new QuestTelegramBot(botToken, questService);
    }
}

