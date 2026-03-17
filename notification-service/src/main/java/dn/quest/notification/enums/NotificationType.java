package dn.quest.notification.enums;

/**
 * Типы уведомлений
 */
public enum NotificationType {
    EMAIL("email"),
    PUSH("push"),
    IN_APP("in_app"),
    TELEGRAM("telegram"),
    SMS("sms");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NotificationType fromValue(String value) {
        for (NotificationType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification type: " + value);
    }
}