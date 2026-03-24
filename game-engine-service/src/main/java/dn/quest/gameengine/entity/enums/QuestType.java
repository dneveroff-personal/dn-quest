package dn.quest.gameengine.entity.enums;

/**
 * Типы квестов
 */
public enum QuestType {
    INDIVIDUAL("Индивидуальный"),
    TEAM("Командный"),
    MULTI_LEVEL("Многоуровневый"),
    TOURNAMENT("Турнирный");

    private final String displayName;

    QuestType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isTeamBased() {
        return this == TEAM;
    }

    public boolean isMultiLevel() {
        return this == MULTI_LEVEL;
    }
}