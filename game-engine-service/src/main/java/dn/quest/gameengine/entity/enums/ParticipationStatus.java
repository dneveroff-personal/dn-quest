package dn.quest.gameengine.entity.enums;

/**
 * Статусы участия в квесте
 */
public enum ParticipationStatus {
    PENDING("Ожидает"),
    APPROVED("Подтверждено"),
    REJECTED("Отклонено"),
    CANCELLED("Отменено"),
    COMPLETED("Завершено"),
    EXPIRED("Истекло");

    private final String displayName;

    ParticipationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isApproved() {
        return this == APPROVED;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }
}