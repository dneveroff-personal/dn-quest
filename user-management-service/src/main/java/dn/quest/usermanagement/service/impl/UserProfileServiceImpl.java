package dn.quest.usermanagement.service.impl;

import dn.quest.shared.enums.UserRole;
import dn.quest.shared.events.user.UserUpdatedEvent;
import dn.quest.usermanagement.dto.BlockUserRequestDTO;
import dn.quest.usermanagement.dto.UpdateProfileRequestDTO;
import dn.quest.usermanagement.dto.UserProfileDTO;
import dn.quest.usermanagement.dto.UserSearchRequestDTO;
import dn.quest.usermanagement.dto.UserStatisticsSummaryDTO;
import dn.quest.usermanagement.entity.UserProfile;
import dn.quest.usermanagement.exception.UserManagementExceptions.UserProfileNotFoundException;
import dn.quest.usermanagement.repository.UserProfileRepository;
import dn.quest.usermanagement.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления профилями пользователей
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Override
    public UserProfileDTO createUserProfile(UUID userId, String username, String email, String publicName, String role) {
        log.info("Создание профиля пользователя: userId={}, username={}", userId, username);

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .publicName(publicName)
                .role(UserRole.valueOf(role))
                .isActive(true)
                .isBlocked(false)
                .build();

        profile = userProfileRepository.save(profile);
        log.info("Профиль пользователя создан: id={}", profile.getId());

        return mapToDto(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDTO> getUserProfileByUserId(UUID userId) {
        log.debug("Получение профиля пользователя по userId: {}", userId);
        return userProfileRepository.findByUserId(userId).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDTO> getUserProfileById(UUID id) {
        log.debug("Получение профиля пользователя по id: {}", id);
        return userProfileRepository.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDTO> getUserProfileByUsername(String username) {
        log.debug("Получение профиля пользователя по username: {}", username);
        return userProfileRepository.findByUsername(username).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserProfileDTO> getUserProfileByEmail(String email) {
        log.debug("Получение профиля пользователя по email: {}", email);
        return userProfileRepository.findByEmail(email).map(this::mapToDto);
    }

    @Override
    public UserProfileDTO updateUserProfile(UUID userId, UpdateProfileRequestDTO request) {
        log.info("Обновление профиля пользователя: userId={}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Профиль пользователя не найден: " + userId));

        if (request.getPublicName() != null) {
            profile.setPublicName(request.getPublicName());
        }
        if (request.getEmail() != null) {
            profile.setEmail(request.getEmail());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }
        if (request.getWebsite() != null) {
            profile.setWebsite(request.getWebsite());
        }

        profile = userProfileRepository.save(profile);
        log.info("Профиль пользователя обновлен: id={}", profile.getId());

        return mapToDto(profile);
    }

    @Override
    public void updateUserProfileFromAuth(UUID userId, String username, String email, String publicName, String role, Boolean isActive) {
        log.info("Обновление профиля из Authentication Service: userId={}", userId);

        Optional<UserProfile> existingProfile = userProfileRepository.findByUserId(userId);

        if (existingProfile.isPresent()) {
            UserProfile profile = existingProfile.get();
            profile.setUsername(username);
            profile.setEmail(email);
            profile.setPublicName(publicName);
            profile.setRole(UserRole.valueOf(role));
            profile.setIsActive(isActive);
            userProfileRepository.save(profile);
            log.info("Профиль пользователя обновлен из Auth: userId={}", userId);
        } else {
            log.warn("Профиль пользователя не найден для обновления из Auth: userId={}", userId);
        }
    }

    @Override
    public void updateUserRole(UUID userId, String newRole) {
        log.info("Обновление роли пользователя: userId={}, newRole={}", userId, newRole);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Профиль пользователя не найден: " + userId));

        profile.setRole(UserRole.valueOf(newRole));
        userProfileRepository.save(profile);

        log.info("Роль пользователя обновлена: userId={}", userId);
    }

    @Override
    public UserProfileDTO updateUserAvatar(UUID userId, String avatarUrl) {
        log.info("Обновление аватара пользователя: userId={}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Профиль пользователя не найден: " + userId));

        profile.setAvatarUrl(avatarUrl);
        profile = userProfileRepository.save(profile);

        log.info("Аватар пользователя обновлен: userId={}", userId);
        return mapToDto(profile);
    }

    @Override
    public UserProfileDTO removeUserAvatar(UUID userId) {
        log.info("Удаление аватара пользователя: userId={}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Профиль пользователя не найден: " + userId));

        profile.setAvatarUrl(null);
        profile = userProfileRepository.save(profile);

        log.info("Аватар пользователя удален: userId={}", userId);
        return mapToDto(profile);
    }

    @Override
    public UserProfileDTO blockUser(UUID userId, BlockUserRequestDTO request) {
        log.info("Блокировка пользователя: userId={}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Профиль пользователя не найден: " + userId));

        profile.block(request.getReason(), request.getBlockedUntil());
        profile = userProfileRepository.save(profile);

        log.info("Пользователь заблокирован: userId={}", userId);
        return mapToDto(profile);
    }

    @Override
    public UserProfileDTO unblockUser(UUID userId) {
        log.info("Разблокировка пользователя: userId={}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Профиль пользователя не найден: " + userId));

        profile.unblock();
        profile = userProfileRepository.save(profile);

        log.info("Пользователь разблокирован: userId={}", userId);
        return mapToDto(profile);
    }

    @Override
    public UserProfileDTO activateUser(UUID userId) {
        log.info("Активация пользователя: userId={}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Профиль пользователя не найден: " + userId));

        profile.setIsActive(true);
        profile = userProfileRepository.save(profile);

        log.info("Пользователь активирован: userId={}", userId);
        return mapToDto(profile);
    }

    @Override
    public UserProfileDTO deactivateUser(UUID userId) {
        log.info("Деактивация пользователя: userId={}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Профиль пользователя не найден: " + userId));

        profile.setIsActive(false);
        profile = userProfileRepository.save(profile);

        log.info("Пользователь деактивирован: userId={}", userId);
        return mapToDto(profile);
    }

    @Override
    public void deleteUserProfile(UUID userId) {
        log.info("Удаление профиля пользователя: userId={}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Профиль пользователя не найден: " + userId));

        userProfileRepository.delete(profile);
        log.info("Профиль пользователя удален: userId={}", userId);
    }

    @Override
    public void updateLastActivity(UUID userId) {
        log.debug("Обновление времени последней активности: userId={}", userId);

        userProfileRepository.findByUserId(userId).ifPresent(profile -> {
            profile.updateLastActivity();
            userProfileRepository.save(profile);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDTO> searchUsers(UserSearchRequestDTO request, Pageable pageable) {
        log.debug("Поиск пользователей: username={}, publicName={}, email={}, role={}, isActive={}",
                request.getUsername(), request.getPublicName(), request.getEmail(), request.getRole(), request.getIsActive());

        Page<UserProfile> profiles = userProfileRepository.searchUsers(
                request.getUsername(),
                request.getPublicName(),
                request.getEmail(),
                request.getRole(),
                request.getIsActive(),
                pageable);

        return profiles.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDTO> getAllUsers(Pageable pageable) {
        log.debug("Получение всех пользователей");
        return userProfileRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDTO> getUsersByRole(UserRole role, Pageable pageable) {
        log.debug("Получение пользователей по роли: {}", role);
        return userProfileRepository.findByRole(role, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDTO> getActiveUsers(Pageable pageable) {
        log.debug("Получение активных пользователей");
        return userProfileRepository.findByIsActive(true, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDTO> getBlockedUsers() {
        log.debug("Получение заблокированных пользователей");
        return userProfileRepository.findCurrentlyBlocked(Instant.now())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDTO> getExpiredBlockedUsers() {
        log.debug("Получение пользователей с истекшей блокировкой");
        return userProfileRepository.findExpiredBlocks(Instant.now())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDTO> getRecentlyRegisteredUsers(int days) {
        log.debug("Получение недавно зарегистрированных пользователей: days={}", days);
        Instant since = Instant.now().minusSeconds(days * 86400L);
        return userProfileRepository.findRecentlyRegistered(since)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDTO> getActiveUsersSince(Instant since) {
        log.debug("Получение активных пользователей с момента: {}", since);
        return userProfileRepository.findActiveSince(since)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        return userProfileRepository.existsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userProfileRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userProfileRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatisticsSummaryDTO getUserStatisticsSummary() {
        log.debug("Получение сводной статистики пользователей");

        long totalUsers = userProfileRepository.count();
        long activeUsers = userProfileRepository.countByIsActive(true);
        long blockedUsers = userProfileRepository.countCurrentlyBlocked(Instant.now());

        java.util.Map<String, Long> usersByRole = Arrays.stream(UserRole.values())
                .collect(Collectors.toMap(
                        UserRole::name,
                        role -> userProfileRepository.countByRole(role)
                ));

        return UserStatisticsSummaryDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .blockedUsers(blockedUsers)
                .usersByRole(usersByRole)
                .build();
    }

    @Override
    public void updateUserProfileFromEvent(UserUpdatedEvent event) {
        log.info("Обновление профиля пользователя из события: userId={}", event.getUserId());
        updateUserProfileFromAuth(
                event.getUserId(),
                event.getUsername(),
                event.getEmail(),
                event.getPublicName(),
                event.getRole(),
                event.getIsActive()
        );
    }

    /**
     * Преобразует сущность UserProfile в DTO UserProfileDTO
     */
    private UserProfileDTO mapToDto(UserProfile profile) {
        return UserProfileDTO.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .username(profile.getUsername())
                .email(profile.getEmail())
                .publicName(profile.getPublicName())
                .role(profile.getRole())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .location(profile.getLocation())
                .website(profile.getWebsite())
                .isActive(profile.getIsActive())
                .isBlocked(profile.getIsBlocked())
                .blockedUntil(profile.getBlockedUntil())
                .blockReason(profile.getBlockReason())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .lastActivityAt(profile.getLastActivityAt())
                .build();
    }
}