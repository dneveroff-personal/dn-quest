package dn.quest.notification.service.channel;

import dn.quest.notification.entity.Notification;
import dn.quest.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация Telegram канала доставки уведомлений
 */
@Component
@Slf4j
@SuppressWarnings("deprecation")
public class TelegramNotificationChannel extends TelegramLongPollingBot implements NotificationChannel {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${app.notification.telegram.enabled:true}")
    private boolean telegramEnabled;

    @Value("${app.notification.telegram.max-message-length:4096}")
    private int maxMessageLength;

    @Value("${app.notification.telegram.parse-mode:HTML}")
    private String parseMode;

    @Value("${app.notification.telegram.enable-web-buttons:true}")
    private boolean enableWebButtons;

    @Value("${app.notification.telegram.web-base-url:https://dn-quest.com}")
    private String webBaseUrl;

    public TelegramNotificationChannel() {
        super();
    }

    @Override
    public String getChannelType() {
        return "telegram";
    }

    @Override
    public boolean isAvailable() {
        return telegramEnabled && botToken != null && !botToken.isEmpty();
    }

    @Override
    public boolean canSend(Notification notification) {
        if (notification.getType() != NotificationType.TELEGRAM) {
            return false;
        }

        String telegramChatId = notification.getTelegramChatId();
        return telegramChatId != null && !telegramChatId.isEmpty();
    }

    @Override
    public boolean validate(Notification notification) {
        if (notification.getContent() == null || notification.getContent().isEmpty()) {
            log.warn("Telegram notification validation failed: missing content");
            return false;
        }

        // Проверка длины сообщения
        if (notification.getContent().length() > maxMessageLength) {
            log.warn("Telegram notification validation failed: content too long (max {} chars)", maxMessageLength);
            return false;
        }

        return canSend(notification);
    }

    @Override
    public NotificationChannelResult send(Notification notification) {
        if (!validate(notification)) {
            return NotificationChannelResult.failure("Telegram notification validation failed");
        }

        try {
            SendMessage sendMessage = createSendMessage(notification);
            
            // Добавляем inline кнопки если нужно
            if (enableWebButtons && shouldAddButtons(notification)) {
                sendMessage.setReplyMarkup(createInlineKeyboard(notification));
            }

            // Отправка сообщения
            org.telegram.telegrambots.meta.api.objects.Message sentMessage = execute(sendMessage);

            log.info("Telegram message sent successfully to chat: {} for notification: {}", 
                    notification.getTelegramChatId(), notification.getNotificationId());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("chatId", notification.getTelegramChatId());
            metadata.put("messageId", sentMessage.getMessageId());
            metadata.put("hasButtons", enableWebButtons && shouldAddButtons(notification));
            metadata.put("parseMode", parseMode);

            return NotificationChannelResult.success(
                    "telegram_" + sentMessage.getMessageId() + "_" + notification.getNotificationId(),
                    metadata
            );

        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message to chat: {} for notification: {}", 
                    notification.getTelegramChatId(), notification.getNotificationId(), e);
            
            String errorCode = extractErrorCode(e);
            return NotificationChannelResult.failure(
                    "Telegram message failed: " + e.getMessage(), 
                    errorCode
            );
        } catch (Exception e) {
            log.error("Unexpected error sending Telegram message to chat: {} for notification: {}", 
                    notification.getTelegramChatId(), notification.getNotificationId(), e);
            return NotificationChannelResult.failure(
                    "Unexpected error: " + e.getMessage(), 
                    "TELEGRAM_UNKNOWN_ERROR"
            );
        }
    }

    @Override
    public double getCost(Notification notification) {
        // Telegram API бесплатный
        return 0.0;
    }

    @Override
    public int getPriority() {
        return 2; // Средний приоритет
    }

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
     * Создание SendMessage объекта
     */
    private SendMessage createSendMessage(Notification notification) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(notification.getTelegramChatId());
        sendMessage.setText(formatMessage(notification));
        sendMessage.setParseMode(parseMode);

        // Устанавливаем тему если есть
        if (notification.getSubject() != null && !notification.getSubject().isEmpty()) {
            String text = "*" + escapeMarkdown(notification.getSubject()) + "*\n\n" + 
                         formatMessageContent(notification);
            sendMessage.setText(text);
        }

        // Отключаем превью ссылок
        sendMessage.setDisableWebPagePreview(false);

        return sendMessage;
    }

    /**
     * Форматирование сообщения
     */
    private String formatMessage(Notification notification) {
        if (notification.getSubject() != null && !notification.getSubject().isEmpty()) {
            return "*" + escapeMarkdown(notification.getSubject()) + "*\n\n" + 
                   formatMessageContent(notification);
        }
        return formatMessageContent(notification);
    }

    /**
     * Форматирование содержимого сообщения
     */
    private String formatMessageContent(Notification notification) {
        String content = notification.getContent();
        
        // Если HTML режим, оставляем как есть
        if ("HTML".equals(parseMode)) {
            return content;
        }
        
        // Для Markdown режима экранируем спецсимволы
        return escapeMarkdown(content);
    }

    /**
     * Экранирование Markdown символов
     */
    private String escapeMarkdown(String text) {
        if (text == null) {
            return "";
        }
        
        return text.replace("*", "\\*")
                  .replace("_", "\\_")
                  .replace("`", "\\`")
                  .replace("[", "\\[")
                  .replace("]", "\\]");
    }

    /**
     * Проверка, нужно ли добавлять кнопки
     */
    private boolean shouldAddButtons(Notification notification) {
        return notification.getRelatedEntityId() != null && 
               notification.getRelatedEntityType() != null;
    }

    /**
     * Создание inline клавиатуры
     */
    private InlineKeyboardMarkup createInlineKeyboard(Notification notification) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Кнопка для перехода к связанной сущности
        if (notification.getRelatedEntityId() != null && notification.getRelatedEntityType() != null) {
            String url = buildEntityUrl(notification);
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("🔗 Открыть");
            button.setUrl(url);
            
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        // Кнопка для управления уведомлениями
        InlineKeyboardButton settingsButton = new InlineKeyboardButton();
        settingsButton.setText("⚙️ Настройки");
        settingsButton.setUrl(webBaseUrl + "/settings/notifications");
        
        List<InlineKeyboardButton> settingsRow = new ArrayList<>();
        settingsRow.add(settingsButton);
        rows.add(settingsRow);

        markup.setKeyboard(rows);
        return markup;
    }

    /**
     * Построение URL для связанной сущности
     */
    private String buildEntityUrl(Notification notification) {
        String entityType = notification.getRelatedEntityType();
        String entityId = notification.getRelatedEntityId();
        
        switch (entityType.toLowerCase()) {
            case "quest":
                return webBaseUrl + "/quests/" + entityId;
            case "team":
                return webBaseUrl + "/teams/" + entityId;
            case "game":
            case "session":
                return webBaseUrl + "/game/" + entityId;
            case "user":
                return webBaseUrl + "/profile/" + entityId;
            default:
                return webBaseUrl + "/notifications/" + notification.getNotificationId();
        }
    }

    /**
     * Извлечение кода ошибки из TelegramApiException
     */
    private String extractErrorCode(TelegramApiException e) {
        String message = e.getMessage();
        
        if (message.contains("chat not found")) {
            return "TELEGRAM_CHAT_NOT_FOUND";
        } else if (message.contains("bot was blocked")) {
            return "TELEGRAM_BOT_BLOCKED";
        } else if (message.contains("PEER_ID_INVALID")) {
            return "TELEGRAM_INVALID_PEER_ID";
        } else if (message.contains("message is too long")) {
            return "TELEGRAM_MESSAGE_TOO_LONG";
        } else if (message.contains("too many requests")) {
            return "TELEGRAM_RATE_LIMIT";
        } else {
            return "TELEGRAM_API_ERROR";
        }
    }

    /**
     * Обновление существующего сообщения
     */
    public NotificationChannelResult updateMessage(String chatId, Integer messageId, String newText) {
        try {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(chatId);
            editMessage.setMessageId(messageId);
            editMessage.setText(newText);
            editMessage.setParseMode(parseMode);

            execute(editMessage);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("chatId", chatId);
            metadata.put("messageId", messageId);
            metadata.put("updated", true);

            return NotificationChannelResult.success(
                    "telegram_updated_" + messageId + "_" + System.currentTimeMillis(),
                    metadata
            );

        } catch (TelegramApiException e) {
            log.error("Failed to update Telegram message {} in chat {}", messageId, chatId, e);
            return NotificationChannelResult.failure(
                    "Failed to update message: " + e.getMessage(),
                    "TELEGRAM_UPDATE_ERROR"
            );
        }
    }
}