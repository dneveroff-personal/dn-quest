package dn.quest.shared.events.quest;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие публикации квеста
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuestPublishedEvent extends BaseEvent {

    /**
     * ID квеста
     */
    private String questId;

    /**
     * Название квеста
     */
    private String title;

    /**
     * ID автора квеста
     */
    private String authorId;

    /**
     * Время публикации
     */
    private java.time.Instant publishedAt;

    /**
     * Время начала регистрации
     */
    private java.time.Instant registrationStartTime;

    /**
     * Время окончания регистрации
     */
    private java.time.Instant registrationEndTime;

    /**
     * Время начала квеста
     */
    private java.time.Instant questStartTime;

    /**
     * Время окончания квеста
     */
    private java.time.Instant questEndTime;

    /**
     * Максимальное количество участников
     */
    private Integer maxParticipants;

    /**
     * Теги квеста
     */
    private java.util.List<String> tags;

    /**
     * IP адрес публикации
     */
    private String publicationIp;

    /**
     * User Agent при публикации
     */
    private String userAgent;

    /**
     * ID администратора, опубликовавшего квест
     */
    private String publishedBy;

    /**
     * Создание события публикации квеста
     */
    public static QuestPublishedEvent create(String questId, String title, String authorId,
                                           java.time.Instant publishedAt,
                                           java.time.Instant registrationStartTime,
                                           java.time.Instant registrationEndTime,
                                           java.time.Instant questStartTime,
                                           java.time.Instant questEndTime,
                                           Integer maxParticipants, java.util.List<String> tags,
                                           String publicationIp, String userAgent,
                                           String publishedBy, String correlationId) {
        return QuestPublishedEvent.builder()
                .eventType("QuestPublished")
                .eventVersion("1.0")
                .source("quest-management-service")
                .correlationId(correlationId)
                .questId(questId)
                .title(title)
                .authorId(authorId)
                .publishedAt(publishedAt)
                .registrationStartTime(registrationStartTime)
                .registrationEndTime(registrationEndTime)
                .questStartTime(questStartTime)
                .questEndTime(questEndTime)
                .maxParticipants(maxParticipants)
                .tags(tags)
                .publicationIp(publicationIp)
                .userAgent(userAgent)
                .publishedBy(publishedBy)
                .data(Map.of(
                        "questId", questId,
                        "title", title,
                        "authorId", authorId,
                        "publishedAt", publishedAt.toString(),
                        "registrationStartTime", registrationStartTime != null ? registrationStartTime.toString() : null,
                        "registrationEndTime", registrationEndTime != null ? registrationEndTime.toString() : null,
                        "questStartTime", questStartTime != null ? questStartTime.toString() : null,
                        "questEndTime", questEndTime != null ? questEndTime.toString() : null,
                        "maxParticipants", maxParticipants,
                        "tags", tags,
                        "publicationIp", publicationIp,
                        "userAgent", userAgent,
                        "publishedBy", publishedBy
                ))
                .build();
    }
}