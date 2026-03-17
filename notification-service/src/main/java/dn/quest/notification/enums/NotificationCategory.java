package dn.quest.notification.enums;

/**
 * Категории уведомлений
 */
public enum NotificationCategory {
    WELCOME("welcome"),
    QUEST("quest"),
    GAME("game"),
    TEAM("team"),
    SYSTEM("system"),
    SECURITY("security"),
    MARKETING("marketing"),
    REMINDER("reminder");

    private final String value;

    NotificationCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NotificationCategory fromValue(String value) {
        for (NotificationCategory category : values()) {
            if (category.value.equals(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown notification category: " + value);
    }
}