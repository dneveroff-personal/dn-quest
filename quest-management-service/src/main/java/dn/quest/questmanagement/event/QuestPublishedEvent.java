package dn.quest.questmanagement.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Событие публикации квеста
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestPublishedEvent extends QuestEvent {

    public QuestPublishedEvent() {
        super();
    }

    public QuestPublishedEvent(Long questId, Long questNumber, String title, String description,
                              String difficulty, String questType, String category,
                              java.util.Set<String> tags, java.util.Set<Long> authorIds,
                              Long version, Long userId) {
        super("QUEST_PUBLISHED", questId, userId);
        this.setQuestNumber(questNumber);
        this.setTitle(title);
        this.setDescription(description);
        this.setDifficulty(difficulty);
        this.setQuestType(questType);
        this.setCategory(category);
        this.setTags(tags);
        this.setAuthorIds(authorIds);
        this.setVersion(version);
    }
}