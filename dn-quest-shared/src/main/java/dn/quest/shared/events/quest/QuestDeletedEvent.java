package dn.quest.shared.events.quest;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие удаления квеста
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QuestDeletedEvent extends BaseEvent {

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
     * Причина удаления
     */
    private String deletionReason;

    /**
     * Тип удаления (soft, hard)
     */
    private String deletionType;

    /**
     * Количество участников на момент удаления
     */
    private Integer participantCount;

    /**
     * IP адрес удаления
     */
    private String deletionIp;

    /**
     * User Agent при удалении
     */
    private String userAgent;

    /**
     * ID администратора, удалившего квест
     */
    private String deletedBy;

    /**
     * Были ли активные сессии
     */
    private Boolean hadActiveSessions;

    /**
     * Создание события удаления квеста
     */
    public static QuestDeletedEvent create(String questId, String title, String authorId,
                                         String deletionReason, String deletionType,
                                         Integer participantCount, String deletionIp,
                                         String userAgent, String deletedBy,
                                         Boolean hadActiveSessions, String correlationId) {
        return QuestDeletedEvent.builder()
                .eventType("QuestDeleted")
                .eventVersion("1.0")
                .source("quest-management-service")
                .correlationId(correlationId)
                .questId(questId)
                .title(title)
                .authorId(authorId)
                .deletionReason(deletionReason)
                .deletionType(deletionType)
                .participantCount(participantCount)
                .deletionIp(deletionIp)
                .userAgent(userAgent)
                .deletedBy(deletedBy)
                .hadActiveSessions(hadActiveSessions)
                .data(Map.of(
                        "questId", questId,
                        "title", title,
                        "authorId", authorId,
                        "deletionReason", deletionReason,
                        "deletionType", deletionType,
                        "participantCount", participantCount,
                        "deletionIp", deletionIp,
                        "userAgent", userAgent,
                        "deletedBy", deletedBy,
                        "hadActiveSessions", hadActiveSessions
                ))
                .build();
    }
}