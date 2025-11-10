package dn.quest.shared.enums;

/**
 * Статусы игровых сессий
 */
public enum SessionStatus {
    /**
     * Ожидает начала
     */
    PENDING("Ожидает начала"),
    
    /**
     * В процессе игры
     */
    IN_PROGRESS("В процессе"),
    
    /**
     * Приостановлена
     */
    PAUSED("Приостановлена"),
    
    /**
     * Завершена успешно
     */
    COMPLETED("Завершена"),
    
    /**
     * Прервана
     */
    ABORTED("Прервана"),
    
    /**
     * Истекло время
     */
    TIME_EXPIRED("Время истекло");

    private final String displayName;

    SessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Проверяет, активна ли сессия
     */
    public boolean isActive() {
        return this == PENDING || this == IN_PROGRESS || this == PAUSED;
    }

    /**
     * Проверяет, завершена ли сессия
     */
    public boolean isFinished() {
        return this == COMPLETED || this == ABORTED || this == TIME_EXPIRED;
    }

    /**
     * Проверяет, можно ли возобновить сессию
     */
    public boolean canBeResumed() {
        return this == PAUSED;
    }

    /**
     * Проверяет, можно ли начать сессию
     */
    public boolean canBeStarted() {
        return this == PENDING;
    }
}