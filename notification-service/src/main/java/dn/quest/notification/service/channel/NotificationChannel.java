package dn.quest.notification.service.channel;

import dn.quest.notification.entity.Notification;

/**
 * Интерфейс стратегии для каналов доставки уведомлений
 */
public interface NotificationChannel {

    /**
     * Получить тип канала
     */
    String getChannelType();

    /**
     * Проверить доступность канала
     */
    boolean isAvailable();

    /**
     * Проверить возможность отправки уведомления
     */
    boolean canSend(Notification notification);

    /**
     * Отправить уведомление
     */
    NotificationChannelResult send(Notification notification);

    /**
     * Валидировать уведомление для данного канала
     */
    boolean validate(Notification notification);

    /**
     * Получить стоимость отправки (опционально)
     */
    default double getCost(Notification notification) {
        return 0.0;
    }

    /**
     * Получить приоритет канала (чем выше, тем приоритетнее)
     */
    default int getPriority() {
        return 1;
    }
}