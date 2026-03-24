package dn.quest.gameengine.entity.enums;

/**
 * Роли пользователей в системе DN Quest
 */
public enum UserRole {
    PLAYER("Игрок"),
    AUTHOR("Автор"),
    ADMIN("Администратор"),
    MODERATOR("Модератор");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean canCreateQuests() {
        return this == AUTHOR || this == ADMIN;
    }

    public boolean canParticipate() {
        return this == PLAYER || this == AUTHOR || this == ADMIN;
    }
}