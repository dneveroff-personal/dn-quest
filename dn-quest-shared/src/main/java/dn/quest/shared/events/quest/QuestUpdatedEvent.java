package dn.quest.shared.events.quest;

import dn.quest.shared.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Событие обновления квеста
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestUpdatedEvent extends BaseEvent {

    /**
     * ID обновлённого квеста
     */
    private Long questId;

    /**
     * Название квеста
     */
    private String title;

    /**
     * Описание квеста
     */
    private String description;

    /**
     * Тип квеста
     */
    private String type;

    /**
     * Сложность квеста
     */
    private String difficulty;

    /**
     * Опубликован ли квест
     */
    private Boolean isPublished;
}