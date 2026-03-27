package dn.quest.notification.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.CompletableFuture;

/**
 * Реализация сервиса для обработки и отправки уведомлений
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("deprecation")
public class NotificationServiceImpl extends TelegramLongPollingBot {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Обработка входящих обновлений от Telegram
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            log.info("Received message from {}: {}", chatId, messageText);
            // Здесь можно добавить логику обработки команд
        }
    }

    /**
     * Отправка email уведомления
     */
    public CompletableFuture<Void> sendEmailNotification(String to, String subject, String message) {
        return CompletableFuture.runAsync(() -> {
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom(fromEmail);
                mailMessage.setTo(to);
                mailMessage.setSubject(subject);
                mailMessage.setText(message);
                mailSender.send(mailMessage);
                log.info("Email sent to: {}", to);
            } catch (Exception e) {
                log.error("Failed to send email to: {}", to, e);
            }
        });
    }

    /**
     * Отправка Telegram уведомления
     */
    public CompletableFuture<Void> sendTelegramNotification(Long userId, String message) {
        return CompletableFuture.runAsync(() -> {
            try {
                SendMessage sendMessage = SendMessage.builder()
                        .chatId(userId.toString())
                        .text(message)
                        .build();
                execute(sendMessage);
                log.info("Telegram notification sent to user: {}", userId);
            } catch (TelegramApiException e) {
                log.error("Failed to send Telegram notification to user: {}", userId, e);
            }
        });
    }

    /**
     * Отправка приветственного уведомления
     */
    public CompletableFuture<Void> sendWelcomeNotification(Long userId, String username, String email) {
        String subject = "Добро пожаловать в DN Quest!";
        String message = String.format(
                "Здравствуйте, %s!\n\n" +
                "Добро пожаловать в DN Quest - увлекательный мир программирования и квестов!\n\n" +
                "Мы рады видеть вас в нашем сообществе. Начните свое путешествие:\n" +
                "• Изучайте доступные квесты\n" +
                "• Присоединяйтесь к командам\n" +
                "• Развивайте свои навыки программирования\n\n" +
                "Удачи в ваших приключениях!\n" +
                "Команда DN Quest",
                username
        );

        sendEmailNotification(email, subject, message);
        return sendTelegramNotification(userId, message);
    }

    /**
     * Отправка уведомления об обновлении профиля
     */
    public CompletableFuture<Void> sendProfileUpdatedNotification(Long userId, String username) {
        String subject = "Ваш профиль обновлен";
        String message = String.format(
                "Здравствуйте, %s!\n\n" +
                "Ваш профиль был успешно обновлен.\n\n" +
                "Если вы не вносили изменений, пожалуйста, свяжитесь с поддержкой.",
                username
        );

        sendEmailNotification(userId.toString(), subject, message);
        return sendTelegramNotification(userId, message);
    }

    /**
     * Отправка уведомления о создании квеста
     */
    public CompletableFuture<Void> sendQuestCreatedNotification(Long questId, String title, Long authorId) {
        String subject = "Квест создан";
        String message = String.format(
                "Ваш квест \"%s\" (ID: %d) был успешно создан и готов к настройке.",
                title, questId
        );

        sendEmailNotification(authorId.toString(), subject, message);
        return sendTelegramNotification(authorId, message);
    }

    /**
     * Отправка уведомления об обновлении квеста
     */
    public CompletableFuture<Void> sendQuestUpdatedNotification(Long questId, String title, Long authorId) {
        String subject = "Квест обновлен";
        String message = String.format(
                "Ваш квест \"%s\" (ID: %d) был обновлен.",
                title, questId
        );

        sendEmailNotification(authorId.toString(), subject, message);
        return sendTelegramNotification(authorId, message);
    }

    /**
     * Отправка уведомления о публикации квеста
     */
    public CompletableFuture<Void> sendQuestPublishedNotification(Long questId, String title, Long authorId) {
        String subject = "Квест опубликован";
        String message = String.format(
                "Поздравляем! Ваш квест \"%s\" (ID: %d) был опубликован и теперь доступен всем игрокам.",
                title, questId
        );

        sendEmailNotification(authorId.toString(), subject, message);
        return sendTelegramNotification(authorId, message);
    }

    /**
     * Отправка уведомления о начале игровой сессии
     */
    public CompletableFuture<Void> sendGameSessionStartedNotification(Long userId, Long sessionId, Long questId) {
        String subject = "Игровая сессия начата";
        String message = String.format(
                "Вы начали игровую сессию (ID: %d) для квеста (ID: %d). Удачи!",
                sessionId, questId
        );

        sendEmailNotification(userId.toString(), subject, message);
        return sendTelegramNotification(userId, message);
    }

    /**
     * Отправка уведомления о завершении игровой сессии
     */
    public CompletableFuture<Void> sendGameSessionFinishedNotification(Long userId, Long sessionId, boolean completed) {
        String subject = completed ? "Квест завершен!" : "Игровая сессия завершена";
        String message = String.format(
                "Ваша игровая сессия (ID: %d) завершена.%s",
                sessionId,
                completed ? " Поздравляем с успешным завершением квеста!" : ""
        );

        sendEmailNotification(userId.toString(), subject, message);
        return sendTelegramNotification(userId, message);
    }

    /**
     * Отправка уведомления о завершении уровня
     */
    public CompletableFuture<Void> sendLevelCompletedNotification(Long userId, Long sessionId, Integer levelNumber) {
        String subject = "Уровень пройден!";
        String message = String.format(
                "Отлично! Вы прошли уровень %d в игровой сессии (ID: %d). Продолжайте в том же духе!",
                levelNumber, sessionId
        );

        sendEmailNotification(userId.toString(), subject, message);
        return sendTelegramNotification(userId, message);
    }

    /**
     * Отправка уведомления о создании команды
     */
    public CompletableFuture<Void> sendTeamCreatedNotification(Long teamId, String teamName, Long captainId) {
        String subject = "Команда создана";
        String message = String.format(
                "Ваша команда \"%s\" (ID: %d) была успешно создана. Вы назначены капитаном.",
                teamName, teamId
        );

        sendEmailNotification(captainId.toString(), subject, message);
        return sendTelegramNotification(captainId, message);
    }

    /**
     * Отправка уведомления об обновлении команды
     */
    public CompletableFuture<Void> sendTeamUpdatedNotification(Long teamId, String teamName) {
        String subject = "Команда обновлена";
        String message = String.format(
                "Команда \"%s\" (ID: %d) была обновлена.",
                teamName, teamId
        );

        log.info("Team updated notification: {}", message);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Отправка уведомления о добавлении участника в команду
     */
    public CompletableFuture<Void> sendTeamMemberAddedNotification(Long teamId, String teamName, Long userId, String userName) {
        String subject = "Добро пожаловать в команду!";
        String message = String.format(
                "Здравствуйте, %s!\n\n" +
                "Вы были добавлены в команду \"%s\" (ID: %d).\n\n" +
                "Приятной игры!",
                userName, teamName, teamId
        );

        sendEmailNotification(userId.toString(), subject, message);
        return sendTelegramNotification(userId, message);
    }

    /**
     * Отправка уведомления об удалении участника из команды
     */
    public CompletableFuture<Void> sendTeamMemberRemovedNotification(Long teamId, String teamName, Long userId, String userName) {
        String subject = "Вы покинули команду";
        String message = String.format(
                "Здравствуйте, %s!\n\n" +
                "Вы были удалены из команды \"%s\" (ID: %d).",
                userName, teamName, teamId
        );

        sendEmailNotification(userId.toString(), subject, message);
        return sendTelegramNotification(userId, message);
    }

    /**
     * Отправка уведомления о загрузке файла
     */
    public CompletableFuture<Void> sendFileUploadedNotification(Long userId, String fileName) {
        String subject = "Файл загружен";
        String message = String.format(
                "Ваш файл \"%s\" был успешно загружен.",
                fileName
        );

        sendEmailNotification(userId.toString(), subject, message);
        return sendTelegramNotification(userId, message);
    }
}