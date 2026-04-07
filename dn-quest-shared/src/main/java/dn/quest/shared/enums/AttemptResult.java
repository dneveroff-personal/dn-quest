package dn.quest.shared.enums;

/**
 * Результаты попыток ввода кодов
 */
public enum AttemptResult {
    /**
     * Код верный
     */
    CORRECT("Верно"),

    /**
     * Код принят (нормальный)
     */
    ACCEPTED_NORMAL("Принято"),
    
    /**
     * Код принят с бонусом
     */
    ACCEPTED_BONUS("Принято с бонусом"),
    
    /**
     * Код принят с штрафом
     */
    ACCEPTED_PENALTY("Принято с штрафом"),
    
    /**
     * Дубликат кода
     */
    DUPLICATE("Дубликат"),
    
    /**
     * Код неверный
     */
    INCORRECT("Неверно"),
    
    /**
     * Код уже был использован
     */
    ALREADY_USED("Уже использован"),
    
    /**
     * Код не найден
     */
    NOT_FOUND("Не найден"),
    
    /**
     * Попытка заблокирована (слишком много попыток)
     */
    BLOCKED("Заблокировано"),
    
    /**
     * Уровень уже завершен
     */
    LEVEL_COMPLETED("Уровень завершен"),
    
    /**
     * Сессия неактивна
     */
    SESSION_INACTIVE("Сессия неактивна");

    private final String displayName;

    AttemptResult(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Проверяет, является ли результат успешным
     */
    public boolean isSuccess() {
        return this == CORRECT || this == ACCEPTED_NORMAL || this == ACCEPTED_BONUS;
    }

    /**
     * Проверяет, является ли результат ошибочным
     */
    public boolean isError() {
        return this != CORRECT && this != ACCEPTED_NORMAL && this != ACCEPTED_BONUS;
    }

    /**
     * Проверяет, можно ли продолжать попытки
     */
    public boolean canContinue() {
        return this != BLOCKED && this != LEVEL_COMPLETED && this != SESSION_INACTIVE;
    }
}
