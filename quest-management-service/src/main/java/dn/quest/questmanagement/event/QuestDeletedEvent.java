package dn.quest.questmanagement.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Событие удаления квеста
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestDeletedEvent extends QuestEvent {

    public QuestDeletedEvent() {
        super();
    }

    public QuestDeletedEvent(Long questId, Long questNumber, String title, String description,
                            String difficulty, String questType, String category,
                            java.util.Set<String> tags, java.util.Set<Long> authorIds,
                            String status, Long version, Long userId) {
        super("QUEST_DELETED", questId, userId);
        this.setQuestNumber(questNumber);
        this.setTitle(title);
        this.setDescription(description);
        this.setDifficulty(difficulty);
        this.setQuestType(questType);
        this.setCategory(category);
        this.setTags(tags);
        this.setAuthorIds(authorIds);
        this.setStatus(status);
        this.setVersion(version);
    }
}