package dn.quest.authentication.service.impl;

import dn.quest.shared.dto.UserDTO;
import dn.quest.authentication.entity.User;
import dn.quest.authentication.repository.UserPermissionRepository;
import dn.quest.authentication.repository.UserRepository;
import dn.quest.authentication.service.UserService;
import dn.quest.shared.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса управления пользователями
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(String username, String password, String email, String publicName, UserRole role) {
        log.debug("Создание нового пользователя: {}", username);
        
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        
        if (email != null && existsByEmail(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .email(email)
                .publicName(publicName != null ? publicName : username)
                .role(role != null ? role : UserRole.PLAYER)
                .isActive(true)
                .isEmailVerified(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Создан новый пользователь: {} с ID: {}", username, savedUser.getId());
        
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameWithPermissions(String username) {
        return userRepository.findByUsernameWithPermissions(username);
    }

    @Override
    public User updateUser(User user) {
        log.debug("Обновление пользователя: {}", user.getUsername());
        user.setUpdatedAt(Instant.now());
        User updatedUser = userRepository.save(user);
        log.info("Пользователь обновлен: {}", user.getUsername());
        return updatedUser;
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Удаление пользователя с ID: {}", id);
        User user = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        
        userRepository.delete(user);
        log.info("Пользователь удален: {} с ID: {}", user.getUsername(), id);
    }

    @Override
    public User toggleUserStatus(Long id, boolean isActive) {
        log.debug("Изменение статуса пользователя с ID: {} на {}", id, isActive);
        User user = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        
        user.setIsActive(isActive);
        user.setUpdatedAt(Instant.now());
        
        User updatedUser = userRepository.save(user);
        log.info("Статус пользователя изменен: {} -> {}", user.getUsername(), isActive);
        
        return updatedUser;
    }

    @Override
    public User changeUserRole(Long id, UserRole newRole) {
        log.debug("Изменение роли пользователя с ID: {} на {}", id, newRole);
        User user = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        
        UserRole oldRole = user.getRole();
        user.setRole(newRole);
        user.setUpdatedAt(Instant.now());
        
        User updatedUser = userRepository.save(user);
        log.info("Роль пользователя изменена: {} {} -> {}", user.getUsername(), oldRole, newRole);
        
        return updatedUser;
    }

    @Override
    public void updateLastLogin(String username) {
        log.debug("Обновление времени последнего входа для пользователя: {}", username);
        User user = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findActiveUsers() {
        return userRepository.findActiveUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findByPublicNameContaining(String name, Pageable pageable) {
        return userRepository.findByPublicNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .publicName(user.getPublicName())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    @Override
    public UserDTO toDTOWithPermissions(User user) {
        if (user == null) {
            return null;
        }

        List<String> permissions = userPermissionRepository.findPermissionNamesByUserId(user.getId());

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .publicName(user.getPublicName())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .permissions(permissions)
                .build();
    }
}