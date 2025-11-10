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
 * Событие создания нового квеста
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuestCreatedEvent extends BaseEvent {

    /**
     * ID квеста
     */
    private String questId;

    /**
     * Название квеста
     */
    private String title;

    /**
     * Описание квеста
     */
    private String description;

    /**
     * ID автора квеста
     */
    private String authorId;

    /**
     * Тип квеста
     */
    private QuestType questType;

    /**
     * Сложность квеста
     */
    private Difficulty difficulty;

    /**
     * Количество уровней
     */
    private Integer levelCount;

    /**
     * Максимальное количество участников
     */
    private Integer maxParticipants;

    /**
     * Время начала квеста
     */
    private java.time.Instant startTime;

    /**
     * Время окончания квеста
     */
    private java.time.Instant endTime;

    /**
     * Теги квеста
     */
    private java.util.List<String> tags;

    /**
     * IP адрес создания
     */
    private String creationIp;

    /**
     * User Agent при создании
     */
    private String userAgent;

    /**
     * Создание события создания квеста
     */
    public static QuestCreatedEvent create(String questId, String title, String description,
                                         String authorId, QuestType questType, Difficulty difficulty,
                                         Integer levelCount, Integer maxParticipants,
                                         java.time.Instant startTime, java.time.Instant endTime,
                                         java.util.List<String> tags, String creationIp,
                                         String userAgent, String correlationId) {
        return QuestCreatedEvent.builder()
                .eventType("QuestCreated")
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
                .creationIp(creationIp)
                .userAgent(userAgent)
                .data(Map.of(
                        "questId", questId,
                        "title", title,
                        "description", description,
                        "authorId", authorId,
                        "questType", questType.name(),
                        "difficulty", difficulty.name(),
                        "levelCount", levelCount,
                        "maxParticipants", maxParticipants,
                        "startTime", startTime != null ? startTime.toString() : null,
                        "endTime", endTime != null ? endTime.toString() : null,
                        "tags", tags,
                        "creationIp", creationIp,
                        "userAgent", userAgent
                ))
                .build();
    }
}