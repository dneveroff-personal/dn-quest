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
 * Событие удаления участника из команды
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeamMemberRemovedEvent extends BaseEvent {

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
     * Роль участника в команде на момент удаления
     */
    private TeamRole role;

    /**
     * ID пользователя, удалившего участника
     */
    private String removedBy;

    /**
     * Имя пользователя, удалившего участника
     */
    private String removedByName;

    /**
     * Время удаления
     */
    private java.time.Instant removedAt;

    /**
     * Общее количество участников после удаления
     */
    private Integer totalMembersCount;

    /**
     * Максимальное количество участников
     */
    private Integer maxMembers;

    /**
     * Причина удаления
     */
    private String removalReason;

    /**
     * Способ удаления (kick, leave, admin_action)
     */
    private String removalMethod;

    /**
     * Был ли участник капитаном
     */
    private Boolean wasCaptain;

    /**
     * ID нового капитана (если старый был удален)
     */
    private String newCaptainId;

    /**
     * Имя нового капитана (если старый был удален)
     */
    private String newCaptainName;

    /**
     * IP адрес удаления
     */
    private String removalIp;

    /**
     * User Agent при удалении
     */
    private String userAgent;

    /**
     * Создание события удаления участника из команды
     */
    public static TeamMemberRemovedEvent create(String teamId, String teamName, String memberId,
                                              String memberName, String memberEmail, TeamRole role,
                                              String removedBy, String removedByName,
                                              java.time.Instant removedAt, Integer totalMembersCount,
                                              Integer maxMembers, String removalReason,
                                              String removalMethod, Boolean wasCaptain,
                                              String newCaptainId, String newCaptainName,
                                              String removalIp, String userAgent,
                                              String correlationId) {
        return TeamMemberRemovedEvent.builder()
                .eventType("TeamMemberRemoved")
                .eventVersion("1.0")
                .source("team-management-service")
                .correlationId(correlationId)
                .teamId(teamId)
                .teamName(teamName)
                .memberId(memberId)
                .memberName(memberName)
                .memberEmail(memberEmail)
                .role(role)
                .removedBy(removedBy)
                .removedByName(removedByName)
                .removedAt(removedAt)
                .totalMembersCount(totalMembersCount)
                .maxMembers(maxMembers)
                .removalReason(removalReason)
                .removalMethod(removalMethod)
                .wasCaptain(wasCaptain)
                .newCaptainId(newCaptainId)
                .newCaptainName(newCaptainName)
                .removalIp(removalIp)
                .userAgent(userAgent)
                .data(Map.of(
                        "teamId", teamId,
                        "teamName", teamName,
                        "memberId", memberId,
                        "memberName", memberName,
                        "memberEmail", memberEmail,
                        "role", role.name(),
                        "removedBy", removedBy,
                        "removedByName", removedByName,
                        "removedAt", removedAt.toString(),
                        "totalMembersCount", totalMembersCount,
                        "maxMembers", maxMembers,
                        "removalReason", removalReason,
                        "removalMethod", removalMethod,
                        "wasCaptain", wasCaptain,
                        "newCaptainId", newCaptainId,
                        "newCaptainName", newCaptainName,
                        "removalIp", removalIp,
                        "userAgent", userAgent
                ))
                .build();
    }
}