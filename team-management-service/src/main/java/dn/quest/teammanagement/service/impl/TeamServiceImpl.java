package dn.quest.teammanagement.service.impl;

import dn.quest.teammanagement.dto.*;
import dn.quest.teammanagement.dto.request.*;
import dn.quest.teammanagement.dto.response.TeamListResponse;
import dn.quest.teammanagement.entity.*;
import dn.quest.teammanagement.enums.TeamRole;
import dn.quest.teammanagement.mapper.TeamMapper;
import dn.quest.teammanagement.repository.*;
import dn.quest.teammanagement.service.TeamService;
import dn.quest.teammanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления командами
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamInvitationRepository teamInvitationRepository;
    private final TeamSettingsRepository teamSettingsRepository;
    private final TeamStatisticsRepository teamStatisticsRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;
    private final UserService userService;

    @Override
    @CacheEvict(value = {"teams", "teamLists"}, allEntries = true)
    public TeamDTO createTeam(CreateTeamRequest request, Long captainId) {
        log.debug("Creating team: {} with captain: {}", request.getName(), captainId);

        // Проверяем существование пользователя
        User captain = userService.getUserEntityById(captainId)
                .orElseThrow(() -> {
                    log.warn("Captain not found for team creation: {}", captainId);
                    return new RuntimeException("User not found: " + captainId);
                });

        // Проверяем, не состоит ли пользователь уже в команде
        if (teamMemberRepository.findByUserAndIsActiveTrue(captain).isPresent()) {
            log.warn("User {} already has a team", captainId);
            throw new RuntimeException("User already has a team");
        }

        // Проверяем уникальность названия
        if (teamRepository.existsByName(request.getName())) {
            log.warn("Team name already exists: {}", request.getName());
            throw new RuntimeException("Team name already exists: " + request.getName());
        }

        // Создаем команду
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .captain(captain)
                .maxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 10)
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                .isActive(true)
                .createdAt(Instant.now())
                .build();

        Team savedTeam = teamRepository.save(team);

        // Добавляем капитана как участника
        TeamMember captainMember = TeamMember.builder()
                .team(savedTeam)
                .user(captain)
                .role(TeamRole.CAPTAIN)
                .joinedAt(Instant.now())
                .isActive(true)
                .build();

        teamMemberRepository.save(captainMember);

        // Создаем настройки команды
        TeamSettings settings = TeamSettings.builder()
                .team(savedTeam)
                .teamTags(request.getTeamTags())
                .welcomeMessage(request.getWelcomeMessage())
                .createdAt(Instant.now())
                .build();

        teamSettingsRepository.save(settings);

        // Создаем статистику команды
        TeamStatistics statistics = TeamStatistics.builder()
                .team(savedTeam)
                .totalMembers(1)
                .activeMembers(1)
                .lastActivityAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        teamStatisticsRepository.save(statistics);

        log.info("Team created successfully: {} with captain: {}", savedTeam.getId(), captainId);
        return teamMapper.toFullTeamDTO(savedTeam);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "teams", key = "#teamId")
    public TeamDTO getTeamById(Long teamId) {
        log.debug("Getting team by id: {}", teamId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> {
                    log.warn("Team not found: {}", teamId);
                    return new RuntimeException("Team not found: " + teamId);
                });

        return teamMapper.toTeamDTOWithMembers(team);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "fullTeams", key = "#teamId")
    public TeamDTO getFullTeamById(Long teamId) {
        log.debug("Getting full team by id: {}", teamId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> {
                    log.warn("Team not found: {}", teamId);
                    return new RuntimeException("Team not found: " + teamId);
                });

        return teamMapper.toFullTeamDTO(team);
    }

    @Override
    @CacheEvict(value = {"teams", "teamLists"}, allEntries = true)
    public TeamDTO updateTeam(Long teamId, UpdateTeamRequest request, Long userId) {
        log.debug("Updating team: {} by user: {}", teamId, userId);

        Team team = getTeamEntity(teamId);

        // Проверяем права доступа
        if (!canManageTeam(teamId, userId)) {
            log.warn("User {} does not have permission to update team: {}", userId, teamId);
            throw new RuntimeException("Insufficient permissions");
        }

        // Обновляем поля
        if (request.getName() != null) {
            // Проверяем уникальность названия, если оно изменилось
            if (!request.getName().equals(team.getName()) && 
                teamRepository.existsByName(request.getName())) {
                throw new RuntimeException("Team name already exists: " + request.getName());
            }
            team.setName(request.getName());
        }

        if (request.getDescription() != null) {
            team.setDescription(request.getDescription());
        }

        if (request.getLogoUrl() != null) {
            team.setLogoUrl(request.getLogoUrl());
        }

        if (request.getMaxMembers() != null) {
            team.setMaxMembers(request.getMaxMembers());
        }

        if (request.getIsPrivate() != null) {
            team.setIsPrivate(request.getIsPrivate());
        }

        if (request.getIsActive() != null) {
            team.setIsActive(request.getIsActive());
        }

        Team updatedTeam = teamRepository.save(team);

        log.info("Team updated successfully: {}", teamId);
        return teamMapper.toFullTeamDTO(updatedTeam);
    }

    @Override
    @CacheEvict(value = {"teams", "teamLists"}, allEntries = true)
    public void deleteTeam(Long teamId, Long userId) {
        log.debug("Deleting team: {} by user: {}", teamId, userId);

        Team team = getTeamEntity(teamId);

        // Проверяем права доступа (только капитан может удалить команду)
        if (!team.getCaptain().getId().equals(userId)) {
            log.warn("User {} is not captain of team: {}", userId, teamId);
            throw new RuntimeException("Only captain can delete team");
        }

        // Удаляем все связанные данные
        teamInvitationRepository.deleteByTeam(team);
        teamMemberRepository.deleteByTeam(team);
        teamSettingsRepository.deleteByTeam(team);
        teamStatisticsRepository.deleteByTeam(team);

        // Удаляем команду
        teamRepository.delete(team);

        log.info("Team deleted successfully: {}", teamId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "teamLists", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort")
    public TeamListResponse getTeams(Pageable pageable) {
        log.debug("Getting teams with pagination: {}", pageable);

        Page<Team> teamPage = teamRepository.findAll(pageable);
        List<TeamDTO> teamDTOs = teamMapper.toTeamDTOList(teamPage.getContent());

        return TeamListResponse.of(
                teamDTOs,
                teamPage.getTotalElements(),
                teamPage.getNumber(),
                teamPage.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TeamListResponse searchTeams(SearchTeamsRequest request, Pageable pageable) {
        log.debug("Searching teams with request: {}", request);

        // Создаем pageable с сортировкой
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable searchPageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Team> teamPage;

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            if (request.getIsPrivate() != null) {
                teamPage = teamRepository.findByNameContainingIgnoreCaseAndIsPrivateAndIsActiveTrue(
                        request.getName(), request.getIsPrivate(), searchPageable);
            } else {
                teamPage = teamRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(
                        request.getName(), searchPageable);
            }
        } else if (request.getTag() != null && !request.getTag().trim().isEmpty()) {
            teamPage = teamRepository.findByTeamTagsContainingAndIsPublicAndActive(
                    request.getTag(), searchPageable);
        } else {
            teamPage = teamRepository.findAll(searchPageable);
        }

        List<TeamDTO> teamDTOs = teamMapper.toTeamDTOList(teamPage.getContent());

        return TeamListResponse.of(
                teamDTOs,
                teamPage.getTotalElements(),
                teamPage.getNumber(),
                teamPage.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TeamListResponse getPublicTeams(Pageable pageable) {
        log.debug("Getting public teams with pagination: {}", pageable);

        Page<Team> teamPage = teamRepository.findByIsPrivateFalseAndIsActiveTrue(pageable);
        List<TeamDTO> teamDTOs = teamMapper.toTeamDTOList(teamPage.getContent());

        return TeamListResponse.of(
                teamDTOs,
                teamPage.getTotalElements(),
                teamPage.getNumber(),
                teamPage.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TeamListResponse getTopTeamsByRating(Pageable pageable) {
        log.debug("Getting top teams by rating with pagination: {}", pageable);

        Page<Team> teamPage = teamRepository.findTopTeamsByRating(pageable);
        List<TeamDTO> teamDTOs = teamMapper.toTeamDTOList(teamPage.getContent());

        return TeamListResponse.of(
                teamDTOs,
                teamPage.getTotalElements(),
                teamPage.getNumber(),
                teamPage.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userTeams", key = "#userId")
    public List<TeamDTO> getUserTeams(Long userId) {
        log.debug("Getting teams for user: {}", userId);

        List<Team> teams = teamRepository.findTeamsByUserId(userId);
        return teamMapper.toTeamDTOList(teams);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userActiveTeams", key = "#userId")
    public List<TeamDTO> getUserActiveTeams(Long userId) {
        log.debug("Getting active teams for user: {}", userId);

        List<Team> teams = teamRepository.findActiveTeamsByUserId(userId);
        return teamMapper.toTeamDTOList(teams);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTeamCaptain(Long teamId, Long userId) {
        log.debug("Checking if user {} is captain of team: {}", userId, teamId);

        return teamRepository.existsByIdAndCaptainId(teamId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTeamMember(Long teamId, Long userId) {
        log.debug("Checking if user {} is member of team: {}", userId, teamId);

        Team team = getTeamEntity(teamId);
        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return teamMemberRepository.existsByTeamAndUserAndIsActiveTrue(team, user);
    }

    @Override
    @Transactional(readOnly = true)
    public String getUserRoleInTeam(Long teamId, Long userId) {
        log.debug("Getting role of user {} in team: {}", userId, teamId);

        Team team = getTeamEntity(teamId);
        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return teamMemberRepository.findByTeamAndUserAndIsActiveTrue(team, user)
                .map(member -> member.getRole().name())
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canManageTeam(Long teamId, Long userId) {
        log.debug("Checking if user {} can manage team: {}", userId, teamId);

        Team team = getTeamEntity(teamId);
        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return teamMemberRepository.findByTeamAndUserAndIsActiveTrue(team, user)
                .map(TeamMember::canManageTeam)
                .orElse(false);
    }

    @Override
    @CacheEvict(value = {"teams", "userTeams"}, allEntries = true)
    public void transferCaptain(Long teamId, Long newCaptainId, Long currentCaptainId) {
        log.debug("Transferring captain in team: {} from {} to {}", teamId, currentCaptainId, newCaptainId);

        Team team = getTeamEntity(teamId);

        // Проверяем, что текущий пользователь - капитан
        if (!team.getCaptain().getId().equals(currentCaptainId)) {
            throw new RuntimeException("Only current captain can transfer captain rights");
        }

        User newCaptain = userService.getUserEntityById(newCaptainId)
                .orElseThrow(() -> new RuntimeException("New captain not found: " + newCaptainId));

        // Проверяем, что новый капитан является участником команды
        TeamMember newCaptainMember = teamMemberRepository.findByTeamAndUserAndIsActiveTrue(team, newCaptain)
                .orElseThrow(() -> new RuntimeException("New captain must be a team member"));

        // Понижаем старого капитана до участника
        TeamMember oldCaptainMember = teamMemberRepository.findByTeamAndUserAndIsActiveTrue(team, team.getCaptain())
                .orElse(null);
        if (oldCaptainMember != null) {
            oldCaptainMember.setRole(TeamRole.MEMBER);
            teamMemberRepository.save(oldCaptainMember);
        }

        // Повышаем нового капитана
        newCaptainMember.setRole(TeamRole.CAPTAIN);
        teamMemberRepository.save(newCaptainMember);

        // Обновляем капитана в команде
        team.setCaptain(newCaptain);
        teamRepository.save(team);

        log.info("Captain transferred successfully in team: {} from {} to {}", teamId, currentCaptainId, newCaptainId);
    }

    @Override
    @CacheEvict(value = {"teams", "userTeams"}, allEntries = true)
    public void addMember(Long teamId, Long userId, Long requesterId) {
        log.debug("Adding member {} to team: {} by requester: {}", userId, teamId, requesterId);

        Team team = getTeamEntity(teamId);

        // Проверяем права доступа
        if (!canManageTeam(teamId, requesterId)) {
            throw new RuntimeException("Insufficient permissions to add members");
        }

        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Проверяем, что пользователь не состоит уже в команде
        if (teamMemberRepository.existsByTeamAndUserAndIsActiveTrue(team, user)) {
            throw new RuntimeException("User is already a team member");
        }

        // Проверяем лимит участников
        if (!team.canAddMember()) {
            throw new RuntimeException("Team is full");
        }

        // Создаем нового участника
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(TeamRole.MEMBER)
                .joinedAt(Instant.now())
                .isActive(true)
                .build();

        teamMemberRepository.save(member);

        log.info("Member {} added to team: {}", userId, teamId);
    }

    @Override
    @CacheEvict(value = {"teams", "userTeams"}, allEntries = true)
    public void removeMember(Long teamId, Long userId, Long requesterId) {
        log.debug("Removing member {} from team: {} by requester: {}", userId, teamId, requesterId);

        Team team = getTeamEntity(teamId);

        // Проверяем права доступа
        if (!canManageTeam(teamId, requesterId) && !userId.equals(requesterId)) {
            throw new RuntimeException("Insufficient permissions to remove members");
        }

        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        TeamMember member = teamMemberRepository.findByTeamAndUserAndIsActiveTrue(team, user)
                .orElseThrow(() -> new RuntimeException("User is not a team member"));

        // Нельзя удалить капитана
        if (member.isCaptain()) {
            throw new RuntimeException("Cannot remove captain from team");
        }

        // Деактивируем участника
        member.deactivate();
        teamMemberRepository.save(member);

        log.info("Member {} removed from team: {}", userId, teamId);
    }

    @Override
    @CacheEvict(value = {"teams", "userTeams"}, allEntries = true)
    public void changeMemberRole(Long teamId, Long userId, String newRole, Long requesterId) {
        log.debug("Changing role of member {} in team: {} to {} by requester: {}", 
                userId, teamId, newRole, requesterId);

        Team team = getTeamEntity(teamId);

        // Проверяем права доступа
        if (!canManageTeam(teamId, requesterId)) {
            throw new RuntimeException("Insufficient permissions to change roles");
        }

        User user = userService.getUserEntityById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        TeamMember member = teamMemberRepository.findByTeamAndUserAndIsActiveTrue(team, user)
                .orElseThrow(() -> new RuntimeException("User is not a team member"));

        TeamRole role;
        try {
            role = TeamRole.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + newRole);
        }

        // Нельзя изменить роль капитана через этот метод
        if (member.isCaptain()) {
            throw new RuntimeException("Cannot change captain role through this method");
        }

        member.setRole(role);
        teamMemberRepository.save(member);

        log.info("Role changed for member {} in team: {} to {}", userId, teamId, newRole);
    }

    // Остальные методы будут реализованы в следующей части...

    @Override
    public Team getTeamEntity(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));
    }

    // Вспомогательные методы
    private void validateTeamName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Team name cannot be empty");
        }
        if (name.length() < 3 || name.length() > 120) {
            throw new RuntimeException("Team name must be between 3 and 120 characters");
        }
    }

    private void validateUser(Long userId) {
        if (!userService.userExists(userId)) {
            throw new RuntimeException("User not found: " + userId);
        }
        if (!userService.isUserActive(userId)) {
            throw new RuntimeException("User is not active: " + userId);
        }
    }
}