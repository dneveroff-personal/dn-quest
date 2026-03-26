package dn.quest.shared.events;

import dn.quest.shared.events.file.FileDeletedEvent;
import dn.quest.shared.events.file.FileUploadedEvent;
import dn.quest.shared.events.file.FileUpdatedEvent;
import dn.quest.shared.events.game.CodeSubmittedEvent;
import dn.quest.shared.events.game.GameSessionFinishedEvent;
import dn.quest.shared.events.game.GameSessionStartedEvent;
import dn.quest.shared.events.game.LevelCompletedEvent;
import dn.quest.shared.events.notification.NotificationEvent;
import dn.quest.shared.events.quest.QuestCreatedEvent;
import dn.quest.shared.events.quest.QuestUpdatedEvent;

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

    /**
     * Публикация события начала игровой сессии
     */
    void publishGameEvent(GameSessionStartedEvent event);

    /**
     * Публикация квеста
     */
    void publishQuestEvent(QuestCreatedEvent event);

    /**
     * Публикация и обновление квеста
     */
    void publishQuestEvent(QuestUpdatedEvent event);

    /**
     * Публикация события завершения игровой сессии
     */
    void publishGameEvent(GameSessionFinishedEvent event);

    /**
     * Публикация события отправки кода
     */
    void publishGameEvent(CodeSubmittedEvent event);

    /**
     * Публикация события завершения уровня
     */
    void publishGameEvent(LevelCompletedEvent event);
}
