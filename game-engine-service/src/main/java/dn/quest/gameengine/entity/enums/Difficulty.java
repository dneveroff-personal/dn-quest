package dn.quest.gameengine.entity.enums;

/**
 * Уровни сложности квестов
 */
public enum Difficulty {
    EASY("Легкий"),
    MEDIUM("Средний"),
    HARD("Сложный"),
    EXPERT("Эксперт");

    private final String displayName;

    Difficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isHard() {
        return this == HARD || this == EXPERT;
    }
}