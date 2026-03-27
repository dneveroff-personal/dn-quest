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
import dn.quest.shared.events.team.TeamDeletedEvent;
import dn.quest.shared.events.team.TeamEvent;
import dn.quest.shared.events.user.UserDeletedEvent;
import dn.quest.shared.events.user.UserRegisteredEvent;
import dn.quest.shared.events.user.UserUpdatedEvent;

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

    /**
     * Публикация события создания команды
     */
    void publishTeamCreatedEvent(TeamEvent.TeamCreatedEvent event);

    /**
     * Публикация события обновления команды
     */
    void publishTeamUpdatedEvent(TeamEvent.TeamUpdatedEvent event);

    /**
     * Публикация события удаления команды
     */
    void publishTeamDeletedEvent(TeamDeletedEvent event);

    /**
     * Публикация события добавления участника в команду
     */
    void publishTeamMemberAddedEvent(TeamEvent.TeamMemberAddedEvent event);

    /**
     * Публикация события удаления участника из команды
     */
    void publishTeamMemberRemovedEvent(TeamEvent.TeamMemberRemovedEvent event);

    /**
     * Публикация события регистрации пользователя
     */
    void publishUserRegisteredEvent(UserRegisteredEvent event);

    /**
     * Публикация события обновления пользователя
     */
    void publishUserUpdatedEvent(UserUpdatedEvent event);

    /**
     * Публикация события удаления пользователя
     */
    void publishUserDeletedEvent(UserDeletedEvent event);
}
