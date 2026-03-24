package dn.quest.shared.events;

import dn.quest.shared.events.file.FileDeletedEvent;
import dn.quest.shared.events.file.FileUploadedEvent;
import dn.quest.shared.events.file.FileUpdatedEvent;
import dn.quest.shared.events.notification.NotificationEvent;

/**
 * Интерфейс для публикации событий в Kafka
 */
public interface EventProducer {

    /**
     * Публикация события загрузки файла
     */
    void publishFileUploadedEvent(FileUploadedEvent event);

    /**
     * Публикация события обновления файла
     */
    void publishFileUpdatedEvent(FileUpdatedEvent event);

    /**
     * Публикация события удаления файла
     */
    void publishFileDeletedEvent(FileDeletedEvent event);

    /**
     * Публикация события уведомления
     */
    void publishNotificationEvent(NotificationEvent event);
}
