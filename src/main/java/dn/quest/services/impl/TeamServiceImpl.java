package dn.quest.services.impl;

import dn.quest.model.dto.TeamDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.enums.TeamRole;
import dn.quest.model.entities.team.Team;
import dn.quest.model.entities.team.TeamMember;
import dn.quest.model.entities.user.User;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

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
                .captain(new UserDTO(team.getCaptain().getId(), team.getCaptain().getPublicName()))
                .members(members)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UserDTO> listMembers(Long teamId) {
        Team team = getById(teamId);
        return teamMemberRepository.findByTeam(team).stream()
                .map(TeamMember::getUser)
                .map(u -> new UserDTO(u.getId(), u.getPublicName()))
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
