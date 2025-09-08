package dn.quest.services.impl;

import dn.quest.model.dto.RegisterDTO;
import dn.quest.model.dto.UserAdminDTO;
import dn.quest.model.dto.UserDTO;
import dn.quest.model.entities.enums.UserRole;
import dn.quest.model.entities.user.User;
import dn.quest.repositories.UserRepository;
import dn.quest.services.interfaces.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void delete(Long id) {
        if (UserRole.ADMIN.equals(getById(id).getRole())) {
            throw new IllegalArgumentException("Cannot delete an administrative user");
        }

        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserDTO register(RegisterDTO dto) {
        if (existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (StringUtils.hasText(dto.getEmail()) && existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPublicName(StringUtils.hasText(dto.getPublicName()) ? dto.getPublicName() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.PLAYER);

        user = userRepository.save(user);
        return toDTO(user);
    }

    @Override
    public UserDTO getById(Long id) {
        return userRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserDTO getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserDTO getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<UserAdminDTO> getAll() {
        return userRepository.findAll().stream()
                .map(this::toAdminDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());    }

    @Override
    public UserDTO updateRole(Long id, UserRole role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
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

    // ===== Маппинг =====
    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .publicName(user.getPublicName())
                .role(user.getRole())
                .build();
    }

    private UserAdminDTO toAdminDTO(User user) {
        return UserAdminDTO.builder()
                .id(user.getId())
                .publicName(user.getPublicName())
                .role(user.getRole())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    private User toEntity(RegisterDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPublicName(StringUtils.hasText(dto.getPublicName()) ? dto.getPublicName() : dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole(UserRole.PLAYER);
        return user;
    }
}
