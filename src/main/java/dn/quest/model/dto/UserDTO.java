package dn.quest.model.dto;

import dn.quest.model.entities.user.User;
import dn.quest.model.entities.team.TeamMember;
import dn.quest.repositories.TeamMemberRepository;
import dn.quest.model.entities.enums.UserRole;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String publicName;
    private UserRole role;
    private TeamShortDTO team;
    private boolean captain;  // ✅ новый флаг

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamShortDTO {
        private Long id;
        private String name;
    }

    public static UserDTO fromEntity(User u, TeamMemberRepository teamMemberRepository) {
        TeamMember membership = teamMemberRepository.findByUser(u).orElse(null);

        TeamShortDTO teamDto = null;
        boolean isCaptain = false;
        if (membership != null) {
            teamDto = TeamShortDTO.builder()
                    .id(membership.getTeam().getId())
                    .name(membership.getTeam().getName())
                    .build();
            isCaptain = membership.getRole() == dn.quest.model.entities.enums.TeamRole.CAPTAIN;
        }

        return UserDTO.builder()
                .id(u.getId())
                .publicName(u.getPublicName())
                .role(u.getRole())
                .team(teamDto)
                .captain(isCaptain)
                .build();
    }

    public static UserDTO fromEntity(User u) {
        return UserDTO.builder()
                .id(u.getId())
                .publicName(u.getPublicName())
                .role(u.getRole())
                .captain(false) // без команды – точно не капитан
                .build();
    }
}
