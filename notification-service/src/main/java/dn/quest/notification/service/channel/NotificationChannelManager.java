package dn.quest.notification.service.channel;

import dn.quest.notification.entity.Notification;
import dn.quest.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Менеджер каналов доставки уведомлений
 * Реализует паттерн Strategy для выбора канала доставки
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationChannelManager {

    private final List<NotificationChannel> channels;
    private Map<NotificationType, NotificationChannel> channelMap;

    /**
     * Инициализация мапы каналов после внедрения зависимостей
     */
    private Map<NotificationType, NotificationChannel> getChannelMap() {
        if (channelMap == null) {
            channelMap = channels.stream()
                    .collect(Collectors.toMap(
                            channel -> NotificationType.fromValue(channel.getChannelType()),
                            Function.identity()
                    ));
        }
        return channelMap;
    }

    /**
     * Получить канал для уведомления
     */
    public NotificationChannel getChannel(Notification notification) {
        return getChannelMap().get(notification.getType());
    }

    /**
     * Получить канал по типу
     */
    public NotificationChannel getChannel(NotificationType type) {
        return getChannelMap().get(type);
    }

    /**
     * Отправить уведомление через соответствующий канал
     */
    public NotificationChannelResult sendNotification(Notification notification) {
        NotificationChannel channel = getChannel(notification);
        
        if (channel == null) {
            String errorMsg = String.format("No channel found for notification type: %s", notification.getType());
            log.error(errorMsg);
            return NotificationChannelResult.failure(errorMsg, "CHANNEL_NOT_FOUND");
        }

        if (!channel.isAvailable()) {
            String errorMsg = String.format("Channel %s is not available", channel.getChannelType());
            log.error(errorMsg);
            return NotificationChannelResult.failure(errorMsg, "CHANNEL_UNAVAILABLE");
        }

        if (!channel.canSend(notification)) {
            String errorMsg = String.format("Cannot send notification through channel %s", channel.getChannelType());
            log.error(errorMsg);
            return NotificationChannelResult.failure(errorMsg, "CANNOT_SEND");
        }

        log.debug("Sending notification {} through channel {}", 
                notification.getNotificationId(), channel.getChannelType());

        return channel.send(notification);
    }

    /**
     * Проверить доступность канала
     */
    public boolean isChannelAvailable(NotificationType type) {
        NotificationChannel channel = getChannel(type);
        return channel != null && channel.isAvailable();
    }

    /**
     * Проверить возможность отправки уведомления
     */
    public boolean canSendNotification(Notification notification) {
        NotificationChannel channel = getChannel(notification);
        return channel != null && channel.isAvailable() && channel.canSend(notification);
    }

    /**
     * Валидировать уведомление для его канала
     */
    public boolean validateNotification(Notification notification) {
        NotificationChannel channel = getChannel(notification);
        return channel != null && channel.validate(notification);
    }

    /**
     * Получить все доступные каналы
     */
    public List<NotificationChannel> getAvailableChannels() {
        return channels.stream()
                .filter(NotificationChannel::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Получить стоимость отправки уведомления
     */
    public double getNotificationCost(Notification notification) {
        NotificationChannel channel = getChannel(notification);
        if (channel != null) {
            return channel.getCost(notification);
        }
        return 0.0;
    }

    /**
     * Получить приоритет канала
     */
    public int getChannelPriority(NotificationType type) {
        NotificationChannel channel = getChannel(type);
        if (channel != null) {
            return channel.getPriority();
        }
        return 0;
    }

    /**
     * Отправить уведомление через несколько каналов (fallback)
     */
    public NotificationChannelResult sendWithFallback(Notification notification, List<NotificationType> fallbackTypes) {
        NotificationChannelResult lastResult = null;
        
        // Сначала пробуем основной канал
        lastResult = sendNotification(notification);
        if (lastResult.isSuccess()) {
            return lastResult;
        }

        log.warn("Primary channel failed for notification {}, trying fallback channels", 
                notification.getNotificationId());

        // Пробуем fallback каналы в порядке приоритета
        for (NotificationType fallbackType : fallbackTypes) {
            if (fallbackType == notification.getType()) {
                continue; // Пропускаем основной тип
            }

            NotificationChannel fallbackChannel = getChannel(fallbackType);
            if (fallbackChannel != null && fallbackChannel.isAvailable() && fallbackChannel.canSend(notification)) {
                
                // Создаем копию уведомления с новым типом
                Notification fallbackNotification = createFallbackNotification(notification, fallbackType);
                
                lastResult = fallbackChannel.send(fallbackNotification);
                if (lastResult.isSuccess()) {
                    log.info("Fallback channel {} succeeded for notification {}", 
                            fallbackChannel.getChannelType(), notification.getNotificationId());
                    return lastResult;
                }
                
                log.warn("Fallback channel {} failed for notification {}: {}", 
                        fallbackChannel.getChannelType(), notification.getNotificationId(), 
                        lastResult.getErrorMessage());
            }
        }

        log.error("All channels failed for notification {}", notification.getNotificationId());
        return lastResult != null ? lastResult : 
                NotificationChannelResult.failure("All channels failed", "ALL_CHANNELS_FAILED");
    }

    /**
     * Создать копию уведомления для fallback канала
     */
    private Notification createFallbackNotification(Notification original, NotificationType newType) {
        return Notification.builder()
                .notificationId(original.getNotificationId() + "_fallback_" + newType.getValue())
                .userId(original.getUserId())
                .recipientEmail(original.getRecipientEmail())
                .recipientPhone(original.getRecipientPhone())
                .telegramChatId(original.getTelegramChatId())
                .fcmToken(original.getFcmToken())
                .type(newType)
                .category(original.getCategory())
                .priority(original.getPriority())
                .subject(original.getSubject())
                .content(original.getContent())
                .htmlContent(original.getHtmlContent())
                .templateData(original.getTemplateData())
                .relatedEntityId(original.getRelatedEntityId())
                .relatedEntityType(original.getRelatedEntityType())
                .sourceEventId(original.getSourceEventId())
                .sourceEventType(original.getSourceEventType())
                .correlationId(original.getCorrelationId())
                .metadata(original.getMetadata())
                .build();
    }

    /**
     * Получить статистику по каналам
     */
    public Map<String, Object> getChannelStatistics() {
        Map<String, Object> stats = Map.of(
                "totalChannels", channels.size(),
                "availableChannels", getAvailableChannels().size(),
                "channelTypes", channels.stream()
                        .map(NotificationChannel::getChannelType)
                        .collect(Collectors.toList()),
                "availableChannelTypes", getAvailableChannels().stream()
                        .map(NotificationChannel::getChannelType)
                        .collect(Collectors.toList())
        );
        
        return stats;
    }

    /**
     * Проверить здоровье всех каналов
     */
    public Map<String, Boolean> checkChannelsHealth() {
        return channels.stream()
                .collect(Collectors.toMap(
                        NotificationChannel::getChannelType,
                        NotificationChannel::isAvailable
                ));
    }
}