package dn.quest.notification.enums;

/**
 * Приоритеты уведомлений
 */
public enum NotificationPriority {
    LOW("low", 1),
    NORMAL("normal", 2),
    HIGH("high", 3),
    URGENT("urgent", 4);

    private final String value;
    private final int level;

    NotificationPriority(String value, int level) {
        this.value = value;
        this.level = level;
    }

    public String getValue() {
        return value;
    }

    public int getLevel() {
        return level;
    }

    public static NotificationPriority fromValue(String value) {
        for (NotificationPriority priority : values()) {
            if (priority.value.equals(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown notification priority: " + value);
    }
}