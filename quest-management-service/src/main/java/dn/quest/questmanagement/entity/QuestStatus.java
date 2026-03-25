package dn.quest.questmanagement.entity;

/**
 * Статусы квестов в системе управления квестами
 */
public enum QuestStatus {

    /**
     * Черновик - квест в разработке
     */
    DRAFT("Черновик"),
    
    /**
     * Опубликован - квест доступен для игры
     */
    PUBLISHED("Опубликован"),
    
    /**
     * Приостановлен - временно недоступен для игры
     */
    SUSPENDED("Приостановлен"),
    
    /**
     * Завершен - квест завершился
     */
    COMPLETED("Завершен"),
    
    /**
     * Архивирован - квест в архиве
     */
    ARCHIVED("Архивирован");

    private final String displayName;

    QuestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Проверяет, является ли статус активным (квест доступен для игры)
     */
    public boolean isActive() {
        return this == PUBLISHED;
    }

    /**
     * Проверяет, является ли статус неактивным (квест недоступен для игры)
     */
    public boolean isInactive() {
        return this == DRAFT || this == SUSPENDED || this == ARCHIVED || this == COMPLETED;
    }

    /**
     * Проверяет, можно ли редактировать квест в этом статусе
     */
    public boolean isEditable() {
        return this == DRAFT || this == SUSPENDED;
    }

    /**
     * Проверяет, можно ли опубликовать квест в этом статусе
     */
    public boolean canPublish() {
        return this == DRAFT || this == SUSPENDED;
    }

    /**
     * Проверяет, можно ли приостановить квест в этом статусе
     */
    public boolean canSuspend() {
        return this == PUBLISHED;
    }

    /**
     * Проверяет, можно ли завершить квест в этом статусе
     */
    public boolean canComplete() {
        return this == PUBLISHED || this == SUSPENDED;
    }

    /**
     * Проверяет, можно ли архивировать квест в этом статусе
     */
    public boolean canArchive() {
        return this != ARCHIVED;
    }
}