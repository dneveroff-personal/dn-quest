package dn.quest.shared.enums;

/**
 * Типы квестов в системе DN Quest
 */
public enum QuestType {
    /**
     * Соло-квест - проходит один игрок
     */
    SOLO("Соло"),
    
    /**
     * Командный квест - проходит команда
     */
    TEAM("Командный");

    private final String displayName;

    QuestType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Проверяет, является ли квест командным
     */
    public boolean isTeam() {
        return this == TEAM;
    }

    /**
     * Проверяет, является ли квест соло
     */
    public boolean isSolo() {
        return this == SOLO;
    }
}