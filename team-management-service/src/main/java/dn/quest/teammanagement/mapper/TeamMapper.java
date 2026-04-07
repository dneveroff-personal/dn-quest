package dn.quest.teammanagement.mapper;

import dn.quest.shared.dto.UserDTO;
import dn.quest.teammanagement.dto.*;
import dn.quest.teammanagement.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования Entity в DTO и обратно
 */
@Component
public class TeamMapper {

    /**
     * Преобразовать User в UserDTO
     */
    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .publicName(user.getFirstName())
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Преобразовать Team в TeamDTO
     */
    public TeamDTO toTeamDTO(Team team) {
        if (team == null) {
            return null;
        }

        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .logoUrl(team.getLogoUrl())
                .captain(toUserDTO(team.getCaptain()))
                .maxMembers(team.getMaxMembers())
                .isPrivate(team.getIsPrivate())
                .isActive(team.getIsActive())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }

    /**
     * Преобразовать Team в TeamDTO с участниками
     */
    public TeamDTO toTeamDTOWithMembers(Team team) {
        if (team == null) {
            return null;
        }

        List<TeamMemberDTO> members = team.getMembers() != null ?
                team.getMembers().stream()
                        .map(this::toTeamMemberDTO)
                        .collect(Collectors.toList()) :
                List.of();

        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .logoUrl(team.getLogoUrl())
                .captain(toUserDTO(team.getCaptain()))
                .maxMembers(team.getMaxMembers())
                .isPrivate(team.getIsPrivate())
                .isActive(team.getIsActive())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .members(members)
                .build();
    }

    /**
     * Преобразовать Team в TeamDTO с полной информацией
     */
    public TeamDTO toFullTeamDTO(Team team) {
        if (team == null) {
            return null;
        }

        List<TeamMemberDTO> members = team.getMembers() != null ?
                team.getMembers().stream()
                        .map(this::toTeamMemberDTO)
                        .collect(Collectors.toList()) :
                List.of();

        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .logoUrl(team.getLogoUrl())
                .captain(toUserDTO(team.getCaptain()))
                .maxMembers(team.getMaxMembers())
                .isPrivate(team.getIsPrivate())
                .isActive(team.getIsActive())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .members(members)
                .settings(toTeamSettingsDTO(team.getSettings()))
                .statistics(toTeamStatisticsDTO(team.getStatistics()))
                .build();
    }

    /**
     * Преобразовать TeamMember в TeamMemberDTO
     */
    public TeamMemberDTO toTeamMemberDTO(TeamMember teamMember) {
        if (teamMember == null) {
            return null;
        }

        return TeamMemberDTO.builder()
                .id(teamMember.getId())
                .user(toUserDTO(teamMember.getUser()))
                .role(teamMember.getRole())
                .joinedAt(teamMember.getJoinedAt())
                .leftAt(teamMember.getLeftAt())
                .isActive(teamMember.getIsActive())
                .build();
    }

    /**
     * Преобразовать TeamInvitation в TeamInvitationDTO
     */
    public TeamInvitationDTO toTeamInvitationDTO(TeamInvitation invitation) {
        if (invitation == null) {
            return null;
        }

        return TeamInvitationDTO.builder()
                .id(invitation.getId())
                .team(toTeamDTO(invitation.getTeam()))
                .user(toUserDTO(invitation.getUser()))
                .invitedBy(toUserDTO(invitation.getInvitedBy()))
                .status(invitation.getStatus())
                .invitationMessage(invitation.getInvitationMessage())
                .createdAt(invitation.getCreatedAt())
                .updatedAt(invitation.getUpdatedAt())
                .respondedAt(invitation.getRespondedAt())
                .expiresAt(invitation.getExpiresAt())
                .responseMessage(invitation.getResponseMessage())
                .build();
    }

    /**
     * Преобразовать TeamSettings в TeamSettingsDTO
     */
    public TeamSettingsDTO toTeamSettingsDTO(TeamSettings settings) {
        if (settings == null) {
            return null;
        }

        return TeamSettingsDTO.builder()
                .id(settings.getId())
                .teamId(settings.getTeam() != null ? settings.getTeam().getId() : null)
                .allowMemberInvites(settings.getAllowMemberInvites())
                .requireCaptainApproval(settings.getRequireCaptainApproval())
                .autoAcceptInvites(settings.getAutoAcceptInvites())
                .invitationExpiryHours(settings.getInvitationExpiryHours())
                .maxPendingInvitations(settings.getMaxPendingInvitations())
                .allowMemberLeave(settings.getAllowMemberLeave())
                .requireCaptainForDisband(settings.getRequireCaptainForDisband())
                .enableTeamChat(settings.getEnableTeamChat())
                .enableTeamStatistics(settings.getEnableTeamStatistics())
                .publicProfile(settings.getPublicProfile())
                .allowSearch(settings.getAllowSearch())
                .teamTags(settings.getTeamTags())
                .welcomeMessage(settings.getWelcomeMessage())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    /**
     * Преобразовать TeamStatistics в TeamStatisticsDTO
     */
    public TeamStatisticsDTO toTeamStatisticsDTO(TeamStatistics statistics) {
        if (statistics == null) {
            return null;
        }

        return TeamStatisticsDTO.builder()
                .id(statistics.getId())
                .teamId(statistics.getTeam() != null ? statistics.getTeam().getId() : null)
                .totalMembers(statistics.getTotalMembers())
                .activeMembers(statistics.getActiveMembers())
                .totalInvitationsSent(statistics.getTotalInvitationsSent())
                .totalInvitationsAccepted(statistics.getTotalInvitationsAccepted())
                .totalInvitationsDeclined(statistics.getTotalInvitationsDeclined())
                .totalGamesPlayed(statistics.getTotalGamesPlayed())
                .totalGamesWon(statistics.getTotalGamesWon())
                .totalGamesLost(statistics.getTotalGamesLost())
                .totalQuestsCompleted(statistics.getTotalQuestsCompleted())
                .totalScore(statistics.getTotalScore())
                .averageScore(statistics.getAverageScore())
                .rating(statistics.getRating())
                .rank(statistics.getRank())
                .winRate(statistics.getWinRate())
                .lastActivityAt(statistics.getLastActivityAt())
                .createdAt(statistics.getCreatedAt())
                .updatedAt(statistics.getUpdatedAt())
                .build();
    }

    /**
     * Преобразовать список Team в список TeamDTO
     */
    public List<TeamDTO> toTeamDTOList(List<Team> teams) {
        if (teams == null) {
            return List.of();
        }
        return teams.stream()
                .map(this::toTeamDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразовать UserDTO в User Entity
     */
    public User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        return User.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .firstName(userDTO.getPublicName())
                .avatarUrl(userDTO.getAvatarUrl())
                .isActive(userDTO.getIsActive())
                .role(userDTO.getRole())
                .createdAt(userDTO.getCreatedAt())
                .build();
    }

    /**
     * Преобразовать список TeamMember в список TeamMemberDTO
     */
    public List<TeamMemberDTO> toTeamMemberDTOList(List<TeamMember> members) {
        if (members == null) {
            return List.of();
        }
        return members.stream()
                .map(this::toTeamMemberDTO)
                .collect(Collectors.toList());
    }

    /**
     * Преобразовать список TeamInvitation в список TeamInvitationDTO
     */
    public List<TeamInvitationDTO> toTeamInvitationDTOList(List<TeamInvitation> invitations) {
        if (invitations == null) {
            return List.of();
        }
        return invitations.stream()
                .map(this::toTeamInvitationDTO)
                .collect(Collectors.toList());
    }
}