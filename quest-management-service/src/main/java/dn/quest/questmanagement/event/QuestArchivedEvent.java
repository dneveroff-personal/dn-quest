package dn.quest.questmanagement.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Событие архивации квеста
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestArchivedEvent extends QuestEvent {

    public QuestArchivedEvent() {
        super();
    }

    public QuestArchivedEvent(UUID questId, Long questNumber, String title, String description,
                             String difficulty, String questType, String category,
                             java.util.Set<String> tags, java.util.Set<UUID> authorIds,
                             String status, Long version, String reason, UUID userId) {
        super("QUEST_ARCHIVED", questId, userId);
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
        this.setReason(reason);
    }
}