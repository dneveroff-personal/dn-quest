package dn.quest.shared.enums;

/**
 * Типы кодов в уровнях
 */
public enum CodeType {
    /**
     * Обычный код - основной код для прохождения уровня
     */
    NORMAL("Обычный"),
    
    /**
     * Бонусный код - дает дополнительное время или очки
     */
    BONUS("Бонусный"),
    
    /**
     * Штрафной код - уменьшает время или очки
     */
    PENALTY("Штрафной");

    private final String displayName;

    CodeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Проверяет, является ли код бонусным
     */
    public boolean isBonus() {
        return this == BONUS;
    }

    /**
     * Проверяет, является ли код штрафным
     */
    public boolean isPenalty() {
        return this == PENALTY;
    }

    /**
     * Проверяет, является ли код обычным
     */
    public boolean isNormal() {
        return this == NORMAL;
    }
}