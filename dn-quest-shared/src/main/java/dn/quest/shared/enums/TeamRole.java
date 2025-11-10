package dn.quest.shared.enums;

/**
 * Роли в команде
 */
public enum TeamRole {
    /**
     * Капитан команды - имеет полные права управления
     */
    CAPTAIN("Капитан"),
    
    /**
     * Обычный участник команды
     */
    MEMBER("Участник");

    private final String displayName;

    TeamRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Проверяет, является ли пользователь капитаном
     */
    public boolean isCaptain() {
        return this == CAPTAIN;
    }

    /**
     * Проверяет, может ли пользователь управлять командой
     */
    public boolean canManageTeam() {
        return this == CAPTAIN;
    }

    /**
     * Проверяет, может ли пользователь приглашать новых участников
     */
    public boolean canInviteMembers() {
        return this == CAPTAIN;
    }

    /**
     * Проверяет, может ли пользователь исключать участников
     */
    public boolean canRemoveMembers() {
        return this == CAPTAIN;
    }
}