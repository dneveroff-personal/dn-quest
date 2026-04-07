package dn.quest.shared.enums;

/**
 * Статусы приглашений в команды
 */
public enum InvitationStatus {
    /**
     * Ожидает рассмотрения
     */
    PENDING("Ожидает"),
    
    /**
     * Принято
     */
    ACCEPTED("Принято"),
    
    /**
     * Отклонено
     */
    REJECTED("Отклонено"),
    
    /**
     * Отменено
     */
    CANCELLED("Отменено"),
    
    /**
     * Истекло
     */
    EXPIRED("Истекло");

    private final String displayName;

    InvitationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Проверяет, активно ли приглашение
     */
    public boolean isActive() {
        return this == PENDING;
    }

    /**
     * Проверяет, можно ли принять приглашение
     */
    public boolean canBeAccepted() {
        return this == PENDING;
    }

    /**
     * Проверяет, можно ли отклонить приглашение
     */
    public boolean canBeRejected() {
        return this == PENDING;
    }

    /**
     * Проверяет, можно ли отменить приглашение
     */
    public boolean canBeCancelled() {
        return this == PENDING;
    }
}