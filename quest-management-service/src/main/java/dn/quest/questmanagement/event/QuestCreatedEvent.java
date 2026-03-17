package dn.quest.questmanagement.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Событие создания квеста
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestCreatedEvent extends QuestEvent {

    public QuestCreatedEvent() {
        super();
    }

    public QuestCreatedEvent(Long questId, Long questNumber, String title, String description,
                           String difficulty, String questType, String category,
                           java.util.Set<String> tags, java.util.Set<Long> authorIds,
                           Long userId) {
        super("QUEST_CREATED", questId, userId);
        this.setQuestNumber(questNumber);
        this.setTitle(title);
        this.setDescription(description);
        this.setDifficulty(difficulty);
        this.setQuestType(questType);
        this.setCategory(category);
        this.setTags(tags);
        this.setAuthorIds(authorIds);
    }
}