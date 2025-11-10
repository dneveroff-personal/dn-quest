package dn.quest.shared.events.quest;

import dn.quest.shared.events.BaseEvent;
import dn.quest.shared.enums.Difficulty;
import dn.quest.shared.enums.QuestType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие обновления квеста
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuestUpdatedEvent extends BaseEvent {

    /**
     * ID квеста
     */
    private String questId;

    /**
     * Название квеста (если изменилось)
     */
    private String title;

    /**
     * Описание квеста (если изменилось)
     */
    private String description;

    /**
     * ID автора квеста
     */
    private String authorId;

    /**
     * Тип квеста (если изменился)
     */
    private QuestType questType;

    /**
     * Сложность квеста (если изменилась)
     */
    private Difficulty difficulty;

    /**
     * Количество уровней (если изменилось)
     */
    private Integer levelCount;

    /**
     * Максимальное количество участников (если изменилось)
     */
    private Integer maxParticipants;

    /**
     * Время начала квеста (если изменилось)
     */
    private java.time.Instant startTime;

    /**
     * Время окончания квеста (если изменилось)
     */
    private java.time.Instant endTime;

    /**
     * Теги квеста (если изменились)
     */
    private java.util.List<String> tags;

    /**
     * Статус квеста
     */
    private String status;

    /**
     * IP адрес обновления
     */
    private String updateIp;

    /**
     * User Agent при обновлении
     */
    private String userAgent;

    /**
     * Список измененных полей
     */
    private java.util.List<String> changedFields;

    /**
     * Создание события обновления квеста
     */
    public static QuestUpdatedEvent create(String questId, String title, String description,
                                         String authorId, QuestType questType, Difficulty difficulty,
                                         Integer levelCount, Integer maxParticipants,
                                         java.time.Instant startTime, java.time.Instant endTime,
                                         java.util.List<String> tags, String status,
                                         String updateIp, String userAgent,
                                         java.util.List<String> changedFields, String correlationId) {
        return QuestUpdatedEvent.builder()
                .eventType("QuestUpdated")
                .eventVersion("1.0")
                .source("quest-management-service")
                .correlationId(correlationId)
                .questId(questId)
                .title(title)
                .description(description)
                .authorId(authorId)
                .questType(questType)
                .difficulty(difficulty)
                .levelCount(levelCount)
                .maxParticipants(maxParticipants)
                .startTime(startTime)
                .endTime(endTime)
                .tags(tags)
                .status(status)
                .updateIp(updateIp)
                .userAgent(userAgent)
                .changedFields(changedFields)
                .data(Map.of(
                        "questId", questId,
                        "title", title,
                        "description", description,
                        "authorId", authorId,
                        "questType", questType != null ? questType.name() : null,
                        "difficulty", difficulty != null ? difficulty.name() : null,
                        "levelCount", levelCount,
                        "maxParticipants", maxParticipants,
                        "startTime", startTime != null ? startTime.toString() : null,
                        "endTime", endTime != null ? endTime.toString() : null,
                        "tags", tags,
                        "status", status,
                        "updateIp", updateIp,
                        "userAgent", userAgent,
                        "changedFields", changedFields
                ))
                .build();
    }
}