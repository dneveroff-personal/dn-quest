package dn.quest.services.impl;

import dn.quest.config.ApplicationConstants;
import dn.quest.model.dto.TeamDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TeamInvitationRepository invitationRepository;

    @Override
    public TeamDTO createTeam(String name, Long captainUserId) {
        log.debug("Creating team: {} with captain: {}", name, captainUserId);
        
        User captain = userRepository.findById(captainUserId)
                .orElseThrow(() -> {
                    log.warn("Captain not found for team creation: {}", captainUserId);
                    return new EntityNotFoundException(ApplicationConstants.USER_NOT_FOUND + ": " + captainUserId);
                });

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

        log.info("Team created successfully: {} with captain: {}", saved.getId(), captainUserId);
        return getTeamDTO(saved.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Team getById(Long id) {
        log.debug("Getting team by id: {}", id);
        
        return teamRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Team not found: {}", id);
                    return new EntityNotFoundException(ApplicationConstants.TEAM_NOT_FOUND + ": " + id);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public TeamDTO getTeamDTO(Long teamId) {
        log.debug("Getting team DTO for id: {}", teamId);
        
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
        log.debug("Listing members for team: {}", teamId);
        
        Team team = getById(teamId);
        Set<UserDTO> members = teamMemberRepository.findByTeam(team).stream()
                .map(TeamMember::getUser)
                .map(UserDTO::fromEntity)  // ✅
                .collect(Collectors.toCollection(LinkedHashSet::new));
        
        log.debug("Found {} members for team: {}", members.size(), teamId);
        return members;
    }

    @Override
    public void addMember(Long teamId, Long userId) {
        log.debug("Adding member {} to team: {}", userId, teamId);
        
        Team team = getById(teamId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for adding to team: {}", userId);
                    return new EntityNotFoundException(ApplicationConstants.USER_NOT_FOUND + ": " + userId);
                });

        boolean already = teamMemberRepository.findByTeamAndUser(team, user).isPresent();
        if (already) {
            log.debug("User {} is already member of team: {}", userId, teamId);
            return;
        }

        TeamMember tm = new TeamMember();
        tm.setTeam(team);
        tm.setUser(user);
        tm.setRole(TeamRole.MEMBER);
        tm.setJoinedAt(Instant.now());
        teamMemberRepository.save(tm);
        
        log.info("User {} added to team: {}", userId, teamId);
    }

    @Override
    public void inviteUser(Long teamId, String username) {
        log.debug("Inviting user {} to team: {}", username, teamId);
        
        Team team = getById(teamId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found for invitation: {}", username);
                    return new EntityNotFoundException(ApplicationConstants.USER_NOT_FOUND + ": " + username);
                });

        // нельзя пригласить капитана своей же команды
        if (team.getCaptain().getId().equals(user.getId())) {
            log.warn("Captain {} trying to invite himself to team: {}", username, teamId);
            throw new IllegalStateException("Captain cannot invite himself");
        }

        // если приглашение уже висит
        if (invitationRepository.findByTeamAndUserAndStatus(team, user, InvitationStatus.PENDING).isPresent()) {
            log.warn("Invitation already exists for user {} to team: {}", username, teamId);
            throw new IllegalStateException("Invitation already exists");
        }

        // Проверяем, есть ли уже приглашение
        Optional<TeamInvitation> existingOpt = invitationRepository.findByTeamAndUser(team, user);

        if (existingOpt.isPresent()) {
            TeamInvitation existing = existingOpt.get();

            if (existing.getStatus() == InvitationStatus.PENDING) {
                log.warn("Active invitation already exists for user {} to team: {}", username, teamId);
                throw new RuntimeException("Уже есть активное приглашение для этого игрока");
            }

            // "Возрождаем" приглашение
            existing.setStatus(InvitationStatus.PENDING);
            invitationRepository.save(existing);
            log.info("Invitation revived for user {} to team: {}", username, teamId);

        } else {
            // Создаём новое
            TeamInvitation invitation = new TeamInvitation();
            invitation.setTeam(team);
            invitation.setUser(user);
            invitation.setStatus(InvitationStatus.PENDING);
            invitationRepository.save(invitation);
            log.info("New invitation created for user {} to team: {}", username, teamId);
        }
    }

    @Override
    public void respondToInvite(Long invitationId, boolean accept) {
        log.debug("Responding to invitation: {} with accept: {}", invitationId, accept);
        
        TeamInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> {
                    log.warn("Invitation not found: {}", invitationId);
                    return new EntityNotFoundException("Invitation not found");
                });

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
            
            log.info("User {} accepted invitation to team: {}",
                    inv.getUser().getUsername(), inv.getTeam().getName());
        } else {
            inv.setStatus(InvitationStatus.DECLINED);
            log.info("User {} declined invitation to team: {}",
                    inv.getUser().getUsername(), inv.getTeam().getName());
        }

        invitationRepository.save(inv);
    }

    @Override
    public void removeMember(Long teamId, Long userId) {
        log.debug("Removing member {} from team: {}", userId, teamId);
        
        Team team = getById(teamId);

        // запрет удалить капитана
        if (team.getCaptain() != null && team.getCaptain().getId().equals(userId)) {
            log.warn("Attempt to remove captain {} from team: {}", userId, teamId);
            throw new IllegalStateException("Cannot remove current captain. Transfer captain first.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for removal from team: {}", userId);
                    return new EntityNotFoundException(ApplicationConstants.USER_NOT_FOUND + ": " + userId);
                });

        teamMemberRepository.findByTeamAndUser(team, user)
                .ifPresent(teamMemberRepository::delete);
        
        log.info("User {} removed from team: {}", userId, teamId);
    }

    @Override
    public void transferCaptain(Long teamId, Long newCaptainUserId) {
        log.debug("Transferring captain in team: {} to user: {}", teamId, newCaptainUserId);
        
        Team team = getById(teamId);
        User newCaptain = userRepository.findById(newCaptainUserId)
                .orElseThrow(() -> {
                    log.warn("New captain not found: {}", newCaptainUserId);
                    return new EntityNotFoundException(ApplicationConstants.USER_NOT_FOUND + ": " + newCaptainUserId);
                });

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
        
        log.info("Captain transferred in team: {} from {} to {}",
                teamId,
                oldCaptain != null ? oldCaptain.getUsername() : "null",
                newCaptain.getUsername());
    }

    @Override
    public void deleteTeam(Long teamId) {
        log.debug("Deleting team: {}", teamId);
        
        Team team = getById(teamId);
        // сначала очистим связи, чтобы не остались «висящие»
        teamMemberRepository.findByTeam(team).forEach(teamMemberRepository::delete);
        teamRepository.delete(team);
        
        log.info("Team deleted successfully: {}", teamId);
    }

}
