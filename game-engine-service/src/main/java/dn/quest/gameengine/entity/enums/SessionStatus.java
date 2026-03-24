package dn.quest.gameengine.entity.enums;

/**
 * Статусы игровых сессий
 */
public enum SessionStatus {
    PENDING("Ожидает начала"),
    IN_PROGRESS("В процессе"),
    ACTIVE("Активна"),
    PAUSED("Приостановлена"),
    COMPLETED("Завершена"),
    FINISHED("Завершена"),
    ABORTED("Прервана"),
    TIME_EXPIRED("Время истекло");

    private final String displayName;

    SessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == PENDING || this == IN_PROGRESS || this == PAUSED;
    }

    public boolean isFinished() {
        return this == COMPLETED || this == ABORTED || this == TIME_EXPIRED;
    }

    public boolean canBeResumed() {
        return this == PAUSED;
    }

    public boolean canBeStarted() {
        return this == PENDING;
    }
}