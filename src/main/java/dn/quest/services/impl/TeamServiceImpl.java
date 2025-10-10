package dn.quest.services.impl;

import dn.quest.model.dto.TeamDTO;
import dn.quest.model.dto.TeamInvitationDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.enums.InvitationStatus;
import dn.quest.model.entities.enums.TeamRole;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.team.TeamInvitation;
import dn.quest.model.entities.team.TeamMember;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.TeamInvitationRepository;
import dn.quest.repositories.TeamMemberRepository;
import dn.quest.repositories.TeamRepository;
import dn.quest.repositories.UserRepository;
import dn.quest.services.interfaces.TeamService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TeamInvitationRepository invitationRepository;

    @Override
    public TeamDTO createTeam(String name, Long captainUserId) {
        User captain = userRepository.findById(captainUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + captainUserId));

        Team team = new Team();
        team.setName(name);
        team.setCaptain(captain);
        team.setCreatedAt(Instant.now());
        Team saved = teamRepository.save(team);

        // капитан — всегда участник
        TeamMember tm = new TeamMember();
        tm.setTeam(saved);
        tm.setUser(captain);
        tm.setRole(TeamRole.CAPTAIN);
        tm.setJoinedAt(Instant.now());
        teamMemberRepository.save(tm);

        return getTeamDTO(saved.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Team getById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public TeamDTO getTeamDTO(Long teamId) {
        Team team = getById(teamId);
        Set<UserDTO> members = listMembers(teamId);

        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .captain(UserDTO.fromEntity(team.getCaptain()))
                .members(members)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UserDTO> listMembers(Long teamId) {
        Team team = getById(teamId);
        return teamMemberRepository.findByTeam(team).stream()
                .map(TeamMember::getUser)
                .map(UserDTO::fromEntity)  // ✅
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void addMember(Long teamId, Long userId) {
        Team team = getById(teamId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        boolean already = teamMemberRepository.findByTeamAndUser(team, user).isPresent();
        if (already) return;

        TeamMember tm = new TeamMember();
        tm.setTeam(team);
        tm.setUser(user);
        tm.setRole(TeamRole.MEMBER);
        tm.setJoinedAt(Instant.now());
        teamMemberRepository.save(tm);
    }

    @Override
    public void inviteUser(Long teamId, String username) {
        Team team = getById(teamId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        // нельзя пригласить капитана своей же команды
        if (team.getCaptain().getId().equals(user.getId())) {
            throw new IllegalStateException("Captain cannot invite himself");
        }

        // если приглашение уже висит
        if (invitationRepository.findByTeamAndUserAndStatus(team, user, InvitationStatus.PENDING).isPresent()) {
            throw new IllegalStateException("Invitation already exists");
        }

        // Проверяем, есть ли уже приглашение
        Optional<TeamInvitation> existingOpt = invitationRepository.findByTeamAndUser(team, user);

        if (existingOpt.isPresent()) {
            TeamInvitation existing = existingOpt.get();

            if (existing.getStatus() == InvitationStatus.PENDING) {
                throw new RuntimeException("Уже есть активное приглашение для этого игрока");
            }

            // "Возрождаем" приглашение
            existing.setStatus(InvitationStatus.PENDING);
            invitationRepository.save(existing);

        } else {
            // Создаём новое
            TeamInvitation invitation = new TeamInvitation();
            invitation.setTeam(team);
            invitation.setUser(user);
            invitation.setStatus(InvitationStatus.PENDING);
            invitationRepository.save(invitation);
        }
    }

    @Override
    public void respondToInvite(Long invitationId, boolean accept) {
        TeamInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));

        if (accept) {
            inv.setStatus(InvitationStatus.ACCEPTED);

            // если игрок уже состоит в команде – удалить старую привязку
            teamMemberRepository.findByUser(inv.getUser())
                    .ifPresent(teamMemberRepository::delete);

            // добавить в новую команду
            TeamMember member = new TeamMember();
            member.setTeam(inv.getTeam());
            member.setUser(inv.getUser());
            member.setRole(TeamRole.MEMBER);
            member.setJoinedAt(Instant.now());
            teamMemberRepository.save(member);
        } else {
            inv.setStatus(InvitationStatus.DECLINED);
        }

        invitationRepository.save(inv);
    }


    @Override
    public void removeMember(Long teamId, Long userId) {
        Team team = getById(teamId);

        // запрет удалить капитана
        if (team.getCaptain() != null && team.getCaptain().getId().equals(userId)) {
            throw new IllegalStateException("Cannot remove current captain. Transfer captain first.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        teamMemberRepository.findByTeamAndUser(team, user)
                .ifPresent(teamMemberRepository::delete);
    }

    @Override
    public void transferCaptain(Long teamId, Long newCaptainUserId) {
        Team team = getById(teamId);
        User newCaptain = userRepository.findById(newCaptainUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + newCaptainUserId));

        // новый капитан должен быть участником (если нет — добавим как MEMBER)
        TeamMember newCapTM = teamMemberRepository.findByTeamAndUser(team, newCaptain)
                .orElseGet(() -> {
                    TeamMember tm = new TeamMember();
                    tm.setTeam(team);
                    tm.setUser(newCaptain);
                    tm.setRole(TeamRole.MEMBER);
                    tm.setJoinedAt(Instant.now());
                    return teamMemberRepository.save(tm);
                });

        // понижаем старого капитана, если был
        User oldCaptain = team.getCaptain();
        if (oldCaptain != null) {
            teamMemberRepository.findByTeamAndUser(team, oldCaptain).ifPresent(tm -> {
                tm.setRole(TeamRole.MEMBER);
                teamMemberRepository.save(tm);
            });
        }

        // повышаем нового капитана
        newCapTM.setRole(TeamRole.CAPTAIN);
        teamMemberRepository.save(newCapTM);

        // и фиксируем на самой команде
        team.setCaptain(newCaptain);
        teamRepository.save(team);
    }

    @Override
    public void deleteTeam(Long teamId) {
        Team team = getById(teamId);
        // сначала очистим связи, чтобы не остались «висящие»
        teamMemberRepository.findByTeam(team).forEach(teamMemberRepository::delete);
        teamRepository.delete(team);
    }

}
