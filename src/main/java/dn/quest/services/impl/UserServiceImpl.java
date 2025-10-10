package dn.quest.services.impl;

import dn.quest.model.dto.RegisterDTO;
import dn.quest.model.dto.TeamDTO;
import dn.quest.model.dto.TeamInvitationDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.enums.InvitationStatus;
import dn.quest.model.entities.enums.UserRole;
import dn.quest.model.entities.user.User;
import dn.quest.model.entities.team.TeamMember;
import dn.quest.repositories.TeamInvitationRepository;
import dn.quest.repositories.TeamMemberRepository;
import dn.quest.repositories.UserRepository;
import dn.quest.services.interfaces.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeamInvitationRepository invitationRepository;

    private UserDTO toDTO(User u) {
        TeamMember membership = teamMemberRepository.findByUser(u).orElse(null);

        UserDTO.TeamShortDTO teamDto = null;
        boolean isCaptain = false;

        if (membership != null) {
            teamDto = UserDTO.TeamShortDTO.builder()
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

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getById(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        return toDTO(u);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getByUsername(String username) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        return toDTO(u);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getByEmail(String email) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
        return toDTO(u);
    }

    @Override
    public List<UserDTO> getByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        if (UserRole.ADMIN.equals(u.getRole())) {
            throw new IllegalArgumentException("Cannot delete an administrative user");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO register(RegisterDTO dto) {
        if (existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPublicName(dto.getPublicName() != null && !dto.getPublicName().isBlank()
                ? dto.getPublicName()
                : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.PLAYER);

        user = userRepository.save(user);
        return toDTO(user);
    }

    @Override
    public UserDTO updateRole(Long id, UserRole role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        user.setRole(role);
        return toDTO(userRepository.save(user));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }


    @Override
    @Transactional(readOnly = true)
    public List<TeamInvitationDTO> getPendingInvitations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        return invitationRepository.findByUserAndStatus(user, InvitationStatus.PENDING)
                .stream()
                .map(inv -> TeamInvitationDTO.builder()
                        .id(inv.getId())
                        .team(TeamDTO.builder()
                                .id(inv.getTeam().getId())
                                .name(inv.getTeam().getName())
                                .captain(toDTO(inv.getTeam().getCaptain()))
                                .build())
                        .status(inv.getStatus().name())
                        .createdAt(inv.getCreatedAt())
                        .build()
                )
                .toList();
    }

}
