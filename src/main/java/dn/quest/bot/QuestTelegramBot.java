package dn.quest.bot;

import dn.quest.services.interfaces.QuestService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class QuestTelegramBot extends TelegramLongPollingBot {

    private final String botToken;
    private final QuestService questService; // Ваш сервис для работы с квизами

    public QuestTelegramBot(String botToken, QuestService questService) {
        this.botToken = botToken;
        this.questService = questService;
    }

    @Override
    public void onUpdateReceived(Update update) {
/*        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if ("/start".equals(messageText)) {
                sendQuizList(chatId);
            } else if (messageText.startsWith("/answer_")) {
                processAnswer(chatId, messageText);
            }
        }*/
    }

   /* private void sendQuizList(Long chatId) {
        List<Quest> quizzes = questService.getAllQuizzes();
        StringBuilder message = new StringBuilder("Доступные квизы:\n");

        quizzes.forEach(quiz -> {
            message.append(quiz.getTitle()).append("\n");
            message.append("Для ответа: /answer_").append(quiz.getId()).append(" [номер_ответа]\n\n");
        });

        sendMessage(chatId, message.toString());
    }*/

  /*  private void processAnswer(Long chatId, String command) {
        String[] parts = command.split("_");
        Long quizId = Long.parseLong(parts[1]);

        // Todo: replace with get answers in correct class form
        AnswerRequest answerRequest = new AnswerRequest();
        answerRequest.setAnswer(Set.of(Integer.parseInt(parts[2])));
        AnswerResult result = questService.getAnswerResult(quizId, answerRequest, "admin");

        sendMessage(chatId, result.toString());
    }*/

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "YourQuizBot"; // Юзернейм без @
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}