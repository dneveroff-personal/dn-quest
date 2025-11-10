package dn.quest.shared.events.team;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие обновления команды
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeamUpdatedEvent extends BaseEvent {

    /**
     * ID команды
     */
    private String teamId;

    /**
     * Название команды (если изменилось)
     */
    private String teamName;

    /**
     * Описание команды (если изменилось)
     */
    private String description;

    /**
     * ID капитана команды (если изменился)
     */
    private String captainId;

    /**
     * Имя капитана команды (если изменилось)
     */
    private String captainName;

    /**
     * Максимальное количество участников (если изменилось)
     */
    private Integer maxMembers;

    /**
     * Текущее количество участников
     */
    private Integer currentMembersCount;

    /**
     * Статус команды (если изменился)
     */
    private String status;

    /**
     * Тип команды (если изменился)
     */
    private String teamType;

    /**
     * Теги команды (если изменились)
     */
    private java.util.List<String> tags;

    /**
     * IP адрес обновления
     */
    private String updateIp;

    /**
     * User Agent при обновлении
     */
    private String userAgent;

    /**
     * ID пользователя, обновившего команду
     */
    private String updatedBy;

    /**
     * Список измененных полей
     */
    private java.util.List<String> changedFields;

    /**
     * Создание события обновления команды
     */
    public static TeamUpdatedEvent create(String teamId, String teamName, String description,
                                        String captainId, String captainName,
                                        Integer maxMembers, Integer currentMembersCount,
                                        String status, String teamType,
                                        java.util.List<String> tags, String updateIp,
                                        String userAgent, String updatedBy,
                                        java.util.List<String> changedFields, String correlationId) {
        return TeamUpdatedEvent.builder()
                .eventType("TeamUpdated")
                .eventVersion("1.0")
                .source("team-management-service")
                .correlationId(correlationId)
                .teamId(teamId)
                .teamName(teamName)
                .description(description)
                .captainId(captainId)
                .captainName(captainName)
                .maxMembers(maxMembers)
                .currentMembersCount(currentMembersCount)
                .status(status)
                .teamType(teamType)
                .tags(tags)
                .updateIp(updateIp)
                .userAgent(userAgent)
                .updatedBy(updatedBy)
                .changedFields(changedFields)
                .data(Map.of(
                        "teamId", teamId,
                        "teamName", teamName,
                        "description", description,
                        "captainId", captainId,
                        "captainName", captainName,
                        "maxMembers", maxMembers,
                        "currentMembersCount", currentMembersCount,
                        "status", status,
                        "teamType", teamType,
                        "tags", tags,
                        "updateIp", updateIp,
                        "userAgent", userAgent,
                        "updatedBy", updatedBy,
                        "changedFields", changedFields
                ))
                .build();
    }
}