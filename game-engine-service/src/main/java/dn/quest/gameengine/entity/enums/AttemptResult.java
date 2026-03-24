package dn.quest.gameengine.entity.enums;

/**
 * Результаты попыток ввода кодов
 */
public enum AttemptResult {
    CORRECT("Верно"),
    ACCEPTED_NORMAL("Принято"),
    ACCEPTED_BONUS("Принято с бонусом"),
    ACCEPTED_PENALTY("Принято с штрафом"),
    INCORRECT("Неверно"),
    ALREADY_USED("Уже использован"),
    DUPLICATE("Дубликат"),
    WRONG("Неверно"),
    NOT_FOUND("Не найден"),
    BLOCKED("Заблокировано"),
    LEVEL_COMPLETED("Уровень завершен"),
    SESSION_INACTIVE("Сессия неактивна");

    private final String displayName;

    AttemptResult(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSuccess() {
        return this == CORRECT;
    }

    public boolean isError() {
        return this != CORRECT;
    }

    public boolean canContinue() {
        return this != BLOCKED && this != LEVEL_COMPLETED && this != SESSION_INACTIVE;
    }
}