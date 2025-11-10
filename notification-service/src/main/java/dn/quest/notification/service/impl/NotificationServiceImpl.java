package dn.quest.notification.service.impl;

import dn.quest.shared.events.notification.NotificationEvent;
import dn.quest.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.CompletableFuture;

/**
 * Реализация сервиса для обработки и отправки уведомлений
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl extends TelegramLongPollingBot implements NotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void processNotificationEvent(NotificationEvent event) {
        log.info("Processing notification event: {} for user: {}", event.getType(), event.getUserId());
        
        try {
            // Отправляем уведомление в зависимости от типа
            switch (event.getType()) {
                case "EMAIL":
                    sendEmailNotification(event.getUserId().toString(), event.getTitle(), event.getMessage());
                    break;
                case "TELEGRAM":
                    sendTelegramNotification(event.getUserId(), event.getMessage());
                    break;
                case "SYSTEM":
                    // Системные уведомления могут обрабатываться по-разному
                    log.info("System notification: {}", event.getMessage());
                    break;
                default:
                    // По умолчанию отправляем email и telegram
                    sendEmailNotification(event.getUserId().toString(), event.getTitle(), event.getMessage());
                    sendTelegramNotification(event.getUserId(), event.getMessage());
            }
        } catch (Exception e) {
            log.error("Error processing notification event: {}", event.getType(), e);
        }
    }

    @Override
    public void sendWelcomeNotification(Long userId, String username, String email) {
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
        sendTelegramNotification(userId, message);
    }

    @Override
    public void sendProfileUpdatedNotification(Long userId, String username) {
        String subject = "Ваш профиль обновлен";
        String message = String.format(
                "Здравствуйте, %s!\n\n" +
                "Ваш профиль был успешно обновлен.\n\n" +
                "Если вы не вносили изменений, пожалуйста, свяжитесь с поддержкой.",
                username
        );
        
        sendEmailNotification(userId.toString(), subject, message);
        sendTelegramNotification(userId, message);
    }

    @Override
    public void sendQuestCreatedNotification(Long questId, String title, Long authorId) {
        String subject = "Квест создан";
        String message = String.format(
                "Ваш квест \"%s\" (ID: %d) был успешно создан и готов к настройке.",
                title, questId
        );
        
        sendEmailNotification(authorId.toString(), subject, message);
        sendTelegramNotification(authorId, message);
    }

    @Override
    public void sendQuestUpdatedNotification(Long questId, String title, Long authorId) {
        String subject = "Квест обновлен";
        String message = String.format(
                "Ваш квест \"%s\" (ID: %d) был обновлен.",
                title, questId
        );
        
        sendEmailNotification(authorId.toString(), subject, message);
        sendTelegramNotification(authorId, message);
    }

    @Override
    public void sendQuestPublishedNotification(Long questId, String title, Long authorId) {
        String subject = "Квест опубликован";
        String message = String.format(
                "Поздравляем! Ваш квест \"%s\" (ID: %d) был опубликован и теперь доступен всем игрокам.",
                title, questId
        );
        
        sendEmailNotification(authorId.toString(), subject, message);
        sendTelegramNotification(authorId, message);
    }

    @Override
    public void sendGameSessionStartedNotification(Long userId, Long sessionId, Long questId) {
        String subject = "Игровая сессия начата";
        String message = String.format(
                "Вы начали игровую сессию (ID: %d) для квеста (ID: %d). Удачи!",
                sessionId, questId
        );
        
        sendEmailNotification(userId.toString(), subject, message);
        sendTelegramNotification(userId, message);
    }

    @Override
    public void sendGameSessionFinishedNotification(Long userId, Long sessionId, boolean completed) {
        String subject = completed ? "Квест завершен!" : "Игровая сессия завершена";
        String message = String.format(
                "Ваша игровая сессия (ID: %d) завершена.%s",
                sessionId,
                completed ? " Поздравляем с успешным завершением квеста!" : ""
        );
        
        sendEmailNotification(userId.toString(), subject, message);
        sendTelegramNotification(userId, message);
    }

    @Override
    public void sendLevelCompletedNotification(Long userId, Long sessionId, Integer levelNumber) {
        String subject = "Уровень пройден!";
        String message = String.format(
                "Отлично! Вы прошли уровень %d в игровой сессии (ID: %d). Продолжайте в том же духе!",
                levelNumber, sessionId
        );
        
        sendEmailNotification(userId.toString(), subject, message);
        sendTelegramNotification(userId, message);
    }

    @Override
    public void sendTeamCreatedNotification(Long teamId, String teamName, Long captainId) {
        String subject = "Команда создана";
        String message = String.format(
                "Ваша команда \"%s\" (ID: %d) была успешно создана. Вы назначены капитаном.",
                teamName, teamId
        );
        
        sendEmailNotification(captainId.toString(), subject, message);
        sendTelegramNotification(captainId, message);
    }

    @Override
    public void sendTeamUpdatedNotification(Long teamId, String teamName) {
        String subject = "Команда обновлена";
        String message = String.format(
                "Команда \"%s\" (ID: %d) была обновлена.",
                teamName, teamId
        );
        
        log.info("Team updated notification: {}", message);
        // Здесь можно добавить логику для отправки уведомлений всем участникам команды
    }

    @Override
    public void sendTeamMemberAddedNotification(Long teamId, String teamName, Long userId, String userName) {
        String subject = "Добро пожаловать в команду!";
        String message = String.format(
                "Здравствуйте, %s!\n\n" +
                "Вы были добавлены в команду \"%s\" (ID: %d).\n\n" +
                "Приятной игры!",
                userName, teamName, teamId
        );
        
        sendEmailNotification(userId.toString(), subject, message);
        sendTelegramNotification(userId, message);
    }

    @Override
    public void sendTeamMemberRemovedNotification(Long teamId, String teamName, Long userId, String userName) {
        String subject = "Вы покинули команду";
        String message = String.format(
                "Здравствуйте, %s!\n\n" +
                "Вы были удалены из команды \"%s\" (ID: %d).",
                userName, teamName, teamId
        );
        
        sendEmailNotification(userId.toString(), subject, message);
        sendTelegramNotification(userId, message);
    }

    @Override
    public void sendFileUploadedNotification(Long userId, String fileName) {
        String subject = "Файл загружен";
        String message = String.format(
                "Ваш файл \"%s\" был успешно загружен.",
                fileName
        );
        
        sendEmailNotification(userId.toString(), subject, message);
        sendTelegramNotification(userId, message);
    }

    @Override
    public void sendEmailNotification(String to, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            
            CompletableFuture.runAsync(() -> {
                try {
                    mailSender.send(mailMessage);
                    log.info("Email sent successfully to: {}", to);
                } catch (Exception e) {
                    log.error("Failed to send email to: {}", to, e);
                }
            });
            
        } catch (Exception e) {
            log.error("Error preparing email notification to: {}", to, e);
        }
    }

    @Override
    public void sendTelegramNotification(Long userId, String message) {
        if (botToken == null || botToken.isEmpty()) {
            log.debug("Telegram bot token not configured, skipping notification");
            return;
        }
        
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(userId.toString());
            sendMessage.setText(message);
            sendMessage.setParseMode("HTML");
            
            CompletableFuture.runAsync(() -> {
                try {
                    execute(sendMessage);
                    log.info("Telegram message sent successfully to user: {}", userId);
                } catch (TelegramApiException e) {
                    log.error("Failed to send Telegram message to user: {}", userId, e);
                }
            });
            
        } catch (Exception e) {
            log.error("Error preparing Telegram notification for user: {}", userId, e);
        }
    }
}