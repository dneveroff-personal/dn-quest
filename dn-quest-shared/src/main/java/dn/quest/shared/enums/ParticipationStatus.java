package dn.quest.shared.enums;

/**
 * Статусы заявок на участие в квестах
 */
public enum ParticipationStatus {
    /**
     * Ожидает рассмотрения
     */
    PENDING("Ожидает"),
    
    /**
     * Принята
     */
    ACCEPTED("Принята"),
    
    /**
     * Отклонена
     */
    REJECTED("Отклонена"),
    
    /**
     * Отменена заявителем
     */
    CANCELLED("Отменена");

    private final String displayName;

    ParticipationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Проверяет, активна ли заявка
     */
    public boolean isActive() {
        return this == PENDING;
    }

    /**
     * Проверяет, можно ли принять заявку
     */
    public boolean canBeAccepted() {
        return this == PENDING;
    }

    /**
     * Проверяет, можно ли отклонить заявку
     */
    public boolean canBeRejected() {
        return this == PENDING;
    }

    /**
     * Проверяет, можно ли отменить заявку
     */
    public boolean canBeCancelled() {
        return this == PENDING;
    }

    /**
     * Проверяет, одобрена ли заявка
     */
    public boolean isApproved() {
        return this == ACCEPTED;
    }
}