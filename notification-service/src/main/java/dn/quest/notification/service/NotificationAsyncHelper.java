package dn.quest.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.quest.notification.entity.Notification;
import dn.quest.notification.enums.NotificationPriority;
import dn.quest.notification.enums.NotificationStatus;
import dn.quest.notification.repository.NotificationRepository;
import dn.quest.notification.service.channel.NotificationChannelManager;
import dn.quest.notification.service.channel.NotificationChannelResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper-класс для асинхронной отправки уведомлений.
 * Вынесен в отдельный бин, чтобы Spring мог корректно проксировать @Async методы.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationAsyncHelper {

    private final NotificationRepository notificationRepository;
    private final NotificationChannelManager channelManager;
    private final UserNotificationPreferencesService preferencesService;
    private final NotificationTemplateService templateService;
    private final NotificationQueueService queueService;

    @Value("${app.notification.default-retry-count:3}")
    private int defaultRetryCount;

    @Value("${app.notification.use-queue:true}")
    private boolean useQueue;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Асинхронная отправка уведомления.
     * Этот метод выполняется в отдельном потоке благодаря @Async.
     */
    @Async
    @Transactional
    public void sendNotificationAsync(Notification notification) {
        try {
            // Проверка предпочтений пользователя
            if (!checkUserPreferences(notification)) {
                log.debug("Notification {} skipped due to user preferences", notification.getNotificationId());
                return;
            }

            // Проверка Do Not Disturb
            if (preferencesService.isDoNotDisturbActive(notification.getUserId())) {
                log.debug("Notification {} skipped due to Do Not Disturb mode", notification.getNotificationId());
                return;
            }

            // Проверка лимитов
            if (!preferencesService.checkNotificationLimits(notification.getUserId())) {
                log.debug("Notification {} skipped due to rate limits", notification.getNotificationId());
                return;
            }

            // Обработка шаблонов если нужно
            processTemplates(notification);

            // Сохранение уведомления
            notification = notificationRepository.save(notification);

            if (useQueue) {
                addToQueue(notification);
            } else {
                sendDirectly(notification);
            }

        } catch (Exception e) {
            log.error("Error sending notification: {}", notification.getNotificationId(), e);
            updateNotificationError(notification, e.getMessage());
        }
    }

    /**
     * Проверить пользовательские предпочтения
     */
    private boolean checkUserPreferences(Notification notification) {
        if (!preferencesService.isNotificationTypeEnabled(notification.getUserId(), notification.getType())) {
            return false;
        }

        String categoryValue = notification.getCategory() != null ? notification.getCategory().getValue() : null;
        if (categoryValue != null && !preferencesService.isNotificationCategoryEnabled(notification.getUserId(), categoryValue)) {
            return false;
        }

        return true;
    }

    /**
     * Обработать шаблоны уведомления
     */
    private void processTemplates(Notification notification) {
        try {
            if (notification.getTemplateData() != null && !notification.getTemplateData().isEmpty()) {
                Map<String, Object> variables = parseTemplateData(notification.getTemplateData());

                if (notification.getContent() != null) {
                    String categoryValue = notification.getCategory() != null ? notification.getCategory().getValue() : "default";
                    String processedContent = templateService.processTemplate("default_" + categoryValue, variables);
                    if (processedContent != null) {
                        notification.setContent(processedContent);
                    }
                }

                if (notification.getHtmlContent() != null) {
                    String categoryValue = notification.getCategory() != null ? notification.getCategory().getValue() : "default";
                    String processedHtml = templateService.processHtmlTemplate("default_" + categoryValue + "_html", variables);
                    if (processedHtml != null) {
                        notification.setHtmlContent(processedHtml);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to process templates for notification: {}", notification.getNotificationId(), e);
        }
    }

    /**
     * Парсинг данных шаблона
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseTemplateData(String templateData) {
        try {
            return objectMapper.readValue(templateData, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse template data: {}", templateData, e);
            return new HashMap<>();
        }
    }

    /**
     * Добавить уведомление в очередь
     */
    private void addToQueue(Notification notification) {
        try {
            List<String> channels = determineChannels(notification);

            for (String channel : channels) {
                String payload = objectMapper.writeValueAsString(notification);

                queueService.addToQueue(
                    notification.getId(),
                    notification.getUserId(),
                    channel,
                    notification.getPriority(),
                    payload,
                    notification.getScheduledAt() != null ?
                        java.time.LocalDateTime.ofInstant(notification.getScheduledAt(), java.time.ZoneId.systemDefault()) : null
                );
            }

            log.debug("Notification {} added to queue for channels: {}", notification.getNotificationId(), channels);

        } catch (Exception e) {
            log.error("Error adding notification to queue: {}", notification.getNotificationId(), e);
            throw new RuntimeException("Failed to add notification to queue", e);
        }
    }

    /**
     * Определить каналы для отправки уведомления
     */
    private List<String> determineChannels(Notification notification) {
        List<String> channels = new java.util.ArrayList<>();

        var preferencesOpt = preferencesService.getPreferences(notification.getUserId());
        var preferences = preferencesOpt.orElse(null);

        if (preferences == null) {
            channels.add("IN_APP");
            return channels;
        }

        dn.quest.notification.enums.NotificationType type = notification.getType();
        if (type == dn.quest.notification.enums.NotificationType.EMAIL) {
            if (preferences.getEmailEnabled() && notification.getRecipientEmail() != null) {
                channels.add("EMAIL");
            }
        } else if (type == dn.quest.notification.enums.NotificationType.PUSH) {
            if (preferences.getPushEnabled() && notification.getFcmToken() != null) {
                channels.add("PUSH");
            }
        } else if (type == dn.quest.notification.enums.NotificationType.IN_APP) {
            if (preferences.getInAppEnabled()) {
                channels.add("IN_APP");
            }
        } else if (type == dn.quest.notification.enums.NotificationType.TELEGRAM) {
            if (preferences.getTelegramEnabled() && notification.getTelegramChatId() != null) {
                channels.add("TELEGRAM");
            }
        } else if (type == dn.quest.notification.enums.NotificationType.SMS) {
            if (preferences.getSmsEnabled() && notification.getRecipientPhone() != null) {
                channels.add("SMS");
            }
        }

        // Всегда добавляем in-app для важных уведомлений
        if (notification.getPriority() == NotificationPriority.HIGH ||
            notification.getPriority() == NotificationPriority.URGENT) {
            if (!channels.contains("IN_APP")) {
                channels.add("IN_APP");
            }
        }

        return channels;
    }

    /**
     * Отправить уведомление напрямую (без очереди)
     */
    private void sendDirectly(Notification notification) {
        try {
            NotificationChannelResult result = channelManager.sendNotification(notification);
            updateNotificationStatus(notification, result);

        } catch (Exception e) {
            log.error("Error sending notification directly: {}", notification.getNotificationId(), e);
            updateNotificationError(notification, e.getMessage());
        }
    }

    /**
     * Обновить статус уведомления
     */
    private void updateNotificationStatus(Notification notification, NotificationChannelResult result) {
        if (result.isSuccess()) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(result.getSentAt());
            if (result.getDeliveredAt() != null) {
                notification.setStatus(NotificationStatus.DELIVERED);
                notification.setDeliveredAt(result.getDeliveredAt());
            }
        } else {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(result.getErrorMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
        }

        notificationRepository.save(notification);
    }

    /**
     * Обновить ошибку уведомления
     */
    private void updateNotificationError(Notification notification, String errorMessage) {
        notification.setStatus(NotificationStatus.FAILED);
        notification.setErrorMessage(errorMessage);
        notification.setRetryCount(notification.getRetryCount() + 1);
        notificationRepository.save(notification);
    }
}