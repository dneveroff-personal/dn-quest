package dn.quest.shared.events.team;

import dn.quest.shared.events.BaseEvent;
import dn.quest.shared.enums.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Событие добавления участника в команду
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeamMemberAddedEvent extends BaseEvent {

    /**
     * ID команды
     */
    private String teamId;

    /**
     * Название команды
     */
    private String teamName;

    /**
     * ID участника
     */
    private String memberId;

    /**
     * Имя участника
     */
    private String memberName;

    /**
     * Email участника
     */
    private String memberEmail;

    /**
     * Роль участника в команде
     */
    private TeamRole role;

    /**
     * ID пользователя, добавившего участника
     */
    private String addedBy;

    /**
     * Имя пользователя, добавившего участника
     */
    private String addedByName;

    /**
     * Время добавления
     */
    private java.time.Instant addedAt;

    /**
     * Общее количество участников после добавления
     */
    private Integer totalMembersCount;

    /**
     * Максимальное количество участников
     */
    private Integer maxMembers;

    /**
     * Способ добавления (invitation, request, direct)
     */
    private String additionMethod;

    /**
     * ID приглашения (если добавлен по приглашению)
     */
    private String invitationId;

    /**
     * IP адрес добавления
     */
    private String additionIp;

    /**
     * User Agent при добавлении
     */
    private String userAgent;

    /**
     * Создание события добавления участника в команду
     */
    public static TeamMemberAddedEvent create(String teamId, String teamName, String memberId,
                                            String memberName, String memberEmail, TeamRole role,
                                            String addedBy, String addedByName,
                                            java.time.Instant addedAt, Integer totalMembersCount,
                                            Integer maxMembers, String additionMethod,
                                            String invitationId, String additionIp,
                                            String userAgent, String correlationId) {
        return TeamMemberAddedEvent.builder()
                .eventType("TeamMemberAdded")
                .eventVersion("1.0")
                .source("team-management-service")
                .correlationId(correlationId)
                .teamId(teamId)
                .teamName(teamName)
                .memberId(memberId)
                .memberName(memberName)
                .memberEmail(memberEmail)
                .role(role)
                .addedBy(addedBy)
                .addedByName(addedByName)
                .addedAt(addedAt)
                .totalMembersCount(totalMembersCount)
                .maxMembers(maxMembers)
                .additionMethod(additionMethod)
                .invitationId(invitationId)
                .additionIp(additionIp)
                .userAgent(userAgent)
                .data(Map.of(
                        "teamId", teamId,
                        "teamName", teamName,
                        "memberId", memberId,
                        "memberName", memberName,
                        "memberEmail", memberEmail,
                        "role", role.name(),
                        "addedBy", addedBy,
                        "addedByName", addedByName,
                        "addedAt", addedAt.toString(),
                        "totalMembersCount", totalMembersCount,
                        "maxMembers", maxMembers,
                        "additionMethod", additionMethod,
                        "invitationId", invitationId,
                        "additionIp", additionIp,
                        "userAgent", userAgent
                ))
                .build();
    }
}