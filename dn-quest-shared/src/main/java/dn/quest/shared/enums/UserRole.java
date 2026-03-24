package dn.quest.shared.enums;

/**
 * Роли пользователей в системе DN Quest
 */
public enum UserRole {
    /**
     * Игрок - может участвовать в квестах
     */
    PLAYER("Игрок"),
    
    /**
     * Автор - может создавать и управлять квестами
     */
    AUTHOR("Автор"),
    
    /**
     * Администратор - полные права доступа
     */
    ADMIN("Администратор"),

    /**
     * Модератор - может модерировать контент
     */
    MODERATOR("Модератор");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Проверяет, имеет ли пользователь права администратора
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Проверяет, может ли пользователь создавать квесты
     */
    public boolean canCreateQuests() {
        return this == AUTHOR || this == ADMIN;
    }

    /**
     * Проверяет, может ли пользователь участвовать в квестах
     */
    public boolean canParticipate() {
        return this == PLAYER || this == AUTHOR || this == ADMIN;
    }
}