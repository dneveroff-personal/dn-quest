package dn.quest.shared.events.quest;

import dn.quest.shared.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Событие публикации квеста
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestPublishedEvent extends BaseEvent {

    /**
     * ID опубликованного квеста
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
     * ID автора квеста
     */
    private Long authorId;
}