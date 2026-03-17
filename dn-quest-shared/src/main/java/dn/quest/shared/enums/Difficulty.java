package dn.quest.shared.enums;

/**
 * Уровни сложности квестов
 */
public enum Difficulty {
    /**
     * Легкий уровень сложности
     */
    EASY("Легкий", 1),
    
    /**
     * Средний уровень сложности
     */
    MEDIUM("Средний", 2),
    
    /**
     * Сложный уровень сложности
     */
    HARD("Сложный", 3),
    
    /**
     * Экспертный уровень сложности
     */
    EXPERT("Экспертный", 4);

    private final String displayName;
    private final int level;

    Difficulty(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Проверяет, является ли сложность легкой
     */
    public boolean isEasy() {
        return this == EASY;
    }

    /**
     * Проверяет, является ли сложность средней или выше
     */
    public boolean isMediumOrHigher() {
        return this.level >= MEDIUM.level;
    }

    /**
     * Проверяет, является ли сложность сложной или выше
     */
    public boolean isHardOrHigher() {
        return this.level >= HARD.level;
    }
}