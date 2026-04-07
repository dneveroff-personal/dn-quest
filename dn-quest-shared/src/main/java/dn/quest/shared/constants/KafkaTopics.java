package dn.quest.shared.constants;

/**
 * Константы для топиков Kafka
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Utility class
    }

    // Пользовательские события
    public static final String USER_EVENTS = "dn-quest.users.events";
    public static final String USER_EVENTS_DLQ = "dn-quest.users.events.dlq";

    // События квестов
    public static final String QUEST_EVENTS = "dn-quest.quests.events";
    public static final String QUEST_EVENTS_DLQ = "dn-quest.quests.events.dlq";

    // Игровые события
    public static final String GAME_EVENTS = "dn-quest.game.events";
    public static final String GAME_EVENTS_DLQ = "dn-quest.game.events.dlq";

    // Командные события
    public static final String TEAM_EVENTS = "dn-quest.teams.events";
    public static final String TEAM_EVENTS_DLQ = "dn-quest.teams.events.dlq";

    // Файловые события
    public static final String FILE_EVENTS = "dn-quest.files.events";
    public static final String FILE_EVENTS_DLQ = "dn-quest.files.events.dlq";

    // Уведомления
    public static final String NOTIFICATION_EVENTS = "dn-quest.notifications.events";
    public static final String NOTIFICATION_EVENTS_DLQ = "dn-quest.notifications.events.dlq";

    // Статистические события
    public static final String STATISTICS_EVENTS = "dn-quest.statistics.events";
    public static final String STATISTICS_EVENTS_DLQ = "dn-quest.statistics.events.dlq";

    // Общий DLQ для всех сервисов
    public static final String GENERAL_DLQ = "dn-quest.dlq";
}