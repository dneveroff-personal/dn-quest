package dn.quest.shared.events.team;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие создания команды
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeamCreatedEvent extends BaseEvent {

    /**
     * ID команды
     */
    private String teamId;

    /**
     * Название команды
     */
    private String teamName;

    /**
     * Описание команды
     */
    private String description;

    /**
     * ID создателя/капитана команды
     */
    private String captainId;

    /**
     * Имя создателя/капитана команды
     */
    private String captainName;

    /**
     * Максимальное количество участников
     */
    private Integer maxMembers;

    /**
     * Текущее количество участников
     */
    private Integer currentMembersCount;

    /**
     * Статус команды (active, inactive, etc.)
     */
    private String status;

    /**
     * Тип команды (public, private, invite_only)
     */
    private String teamType;

    /**
     * Теги команды
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
     * Создание события создания команды
     */
    public static TeamCreatedEvent create(String teamId, String teamName, String description,
                                        String captainId, String captainName,
                                        Integer maxMembers, Integer currentMembersCount,
                                        String status, String teamType,
                                        java.util.List<String> tags, String creationIp,
                                        String userAgent, String correlationId) {
        return TeamCreatedEvent.builder()
                .eventType("TeamCreated")
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
                .creationIp(creationIp)
                .userAgent(userAgent)
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
                        "creationIp", creationIp,
                        "userAgent", userAgent
                ))
                .build();
    }
}