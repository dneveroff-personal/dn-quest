package dn.quest.notification.enums;

/**
 * Статусы уведомлений
 */
public enum NotificationStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    SENT("sent"),
    DELIVERED("delivered"),
    READ("read"),
    FAILED("failed"),
    CANCELLED("cancelled");

    private final String value;

    NotificationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NotificationStatus fromValue(String value) {
        for (NotificationStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown notification status: " + value);
    }
}