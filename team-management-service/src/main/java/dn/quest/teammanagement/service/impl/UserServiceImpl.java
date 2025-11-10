package dn.quest.teammanagement.service.impl;

import dn.quest.teammanagement.dto.UserDTO;
import dn.quest.teammanagement.entity.User;
import dn.quest.teammanagement.mapper.TeamMapper;
import dn.quest.teammanagement.repository.UserRepository;
import dn.quest.teammanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с пользователями
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TeamMapper teamMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public UserDTO getUserById(Long userId) {
        log.debug("Getting user by id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new RuntimeException("User not found: " + userId);
                });

        return teamMapper.toUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "usersByUsername", key = "#username")
    public UserDTO getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found by username: {}", username);
                    return new RuntimeException("User not found: " + username);
                });

        return teamMapper.toUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "usersByEmail", key = "#email")
    public UserDTO getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found by email: {}", email);
                    return new RuntimeException("User not found: " + email);
                });

        return teamMapper.toUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserEntityById(Long userId) {
        log.debug("Getting user entity by id: {}", userId);
        return userRepository.findById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserEntityByUsername(String username) {
        log.debug("Getting user entity by username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        log.debug("Checking if user exists: {}", userId);
        return userRepository.existsById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExistsByUsername(String username) {
        log.debug("Checking if user exists by username: {}", username);
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExistsByEmail(String email) {
        log.debug("Checking if user exists by email: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserActive(Long userId) {
        log.debug("Checking if user is active: {}", userId);
        return userRepository.findById(userId)
                .map(user -> Boolean.TRUE.equals(user.getIsActive()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsersByName(String name, int limit) {
        log.debug("Searching users by name: {} with limit: {}", name, limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<User> users = userRepository.findByFullNameContainingIgnoreCaseAndIsActiveTrue(name, pageable);
        return teamMapper.toUserDTOList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(String query, int limit) {
        log.debug("Searching users with query: {} with limit: {}", query, limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<User> users = userRepository.findByUsernameOrFullNameContainingIgnoreCaseAndIsActiveTrue(query, pageable);
        return teamMapper.toUserDTOList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersForTeamInvitation(Long teamId, String search, int limit) {
        log.debug("Getting users for team invitation: {} with search: {} and limit: {}", teamId, search, limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<User> users = userRepository.findUsersForTeamInvitation(teamId, search, pageable);
        return teamMapper.toUserDTOList(users);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "usersByIds", key = "#userIds.hashCode()")
    public List<UserDTO> getUsersByIds(List<Long> userIds) {
        log.debug("Getting users by ids: {}", userIds);

        List<User> users = userRepository.findByIdInAndIsActiveTrue(userIds);
        return teamMapper.toUserDTOList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUserEntitiesByIds(List<Long> userIds) {
        log.debug("Getting user entities by ids: {}", userIds);
        return userRepository.findByIdInAndIsActiveTrue(userIds);
    }

    @Override
    @CacheEvict(value = {"users", "usersByUsername", "usersByEmail", "usersByIds"}, allEntries = true)
    public UserDTO syncUser(UserDTO userDTO) {
        log.debug("Syncing user: {}", userDTO.getId());

        Optional<User> existingUser = userRepository.findById(userDTO.getId());
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Обновляем существующего пользователя
            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setAvatarUrl(userDTO.getAvatarUrl());
            user.setIsActive(userDTO.getIsActive());
        } else {
            // Создаем нового пользователя
            user = teamMapper.toEntity(userDTO);
            user.setCreatedAt(Instant.now());
        }

        User savedUser = userRepository.save(user);
        return teamMapper.toUserDTO(savedUser);
    }

    @Override
    @CacheEvict(value = {"users", "usersByUsername", "usersByEmail"}, allEntries = true)
    public User syncUserEntity(User user) {
        log.debug("Syncing user entity: {}", user.getId());

        Optional<User> existingUser = userRepository.findById(user.getId());
        
        if (existingUser.isPresent()) {
            User existing = existingUser.get();
            existing.setUsername(user.getUsername());
            existing.setEmail(user.getEmail());
            existing.setFirstName(user.getFirstName());
            existing.setLastName(user.getLastName());
            existing.setAvatarUrl(user.getAvatarUrl());
            existing.setIsActive(user.getIsActive());
            return userRepository.save(existing);
        } else {
            user.setCreatedAt(Instant.now());
            return userRepository.save(user);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveUsersCount() {
        log.debug("Getting active users count");
        return userRepository.countActiveUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRegistrationPeriod(Instant startDate, Instant endDate) {
        log.debug("Getting users by registration period: {} to {}", startDate, endDate);

        List<User> users = userRepository.findUsersByRegistrationPeriod(startDate, endDate);
        return teamMapper.toUserDTOList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersWithoutTeams() {
        log.debug("Getting users without teams");

        List<User> users = userRepository.findUsersWithoutTeams();
        return teamMapper.toUserDTOList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getTeamCaptains() {
        log.debug("Getting team captains");

        List<User> users = userRepository.findTeamCaptains();
        return teamMapper.toUserDTOList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByTeamCount(int teamCount) {
        log.debug("Getting users by team count: {}", teamCount);

        List<User> users = userRepository.findUsersByTeamCount(teamCount);
        return teamMapper.toUserDTOList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateUser(Long userId) {
        log.debug("Validating user: {}", userId);
        
        return userRepository.findById(userId)
                .map(user -> Boolean.TRUE.equals(user.getIsActive()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "publicUsers", key = "#userId")
    public UserDTO getPublicUserInfo(Long userId) {
        log.debug("Getting public user info: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Возвращаем только публичную информацию
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    @CacheEvict(value = {"users", "usersByUsername", "usersByEmail"}, allEntries = true)
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        log.debug("Updating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Обновляем разрешенные поля
        if (userDTO.getFirstName() != null) {
            user.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null) {
            user.setLastName(userDTO.getLastName());
        }
        if (userDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(userDTO.getAvatarUrl());
        }

        User savedUser = userRepository.save(user);
        return teamMapper.toUserDTO(savedUser);
    }

    @Override
    @CacheEvict(value = {"users", "usersByUsername", "usersByEmail"}, allEntries = true)
    public void deactivateUser(Long userId) {
        log.debug("Deactivating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @CacheEvict(value = {"users", "usersByUsername", "usersByEmail"}, allEntries = true)
    public void activateUser(Long userId) {
        log.debug("Activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setIsActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserBeInvited(Long userId, Long teamId) {
        log.debug("Checking if user {} can be invited to team: {}", userId, teamId);

        // Проверяем, что пользователь существует и активен
        if (!isUserActive(userId)) {
            return false;
        }

        // Здесь можно добавить дополнительные проверки
        // Например, проверка на блокировки, лимиты и т.д.

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public int getUserTeamCount(Long userId) {
        log.debug("Getting team count for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return (int) userRepository.countByUserAndIsActiveTrue(user);
    }

    @Override
    @Transactional(readOnly = true)
    public int getUserActiveTeamCount(Long userId) {
        log.debug("Getting active team count for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return (int) userRepository.countByUserAndIsActiveTrue(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkUserTeamLimit(Long userId, int maxTeams) {
        log.debug("Checking team limit for user: {} with max: {}", userId, maxTeams);

        int currentTeamCount = getUserActiveTeamCount(userId);
        return currentTeamCount < maxTeams;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByUsernamePrefix(String prefix, int limit) {
        log.debug("Getting users by username prefix: {} with limit: {}", prefix, limit);

        List<User> users = userRepository.findByUsernameStartingWithIgnoreCaseAndIsActiveTrue(prefix);
        return users.stream()
                .limit(limit)
                .map(teamMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatisticsDTO getUserStatistics() {
        log.debug("Getting user statistics");

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsers();
        long usersWithoutTeams = userRepository.findUsersWithoutTeams().size();
        long teamCaptains = userRepository.findTeamCaptains().size();

        double averageTeamsPerUser = activeUsers > 0 ? (double) totalUsers / activeUsers : 0.0;

        Instant monthAgo = Instant.now().minusSeconds(30 * 24 * 60 * 60);
        Instant weekAgo = Instant.now().minusSeconds(7 * 24 * 60 * 60);

        long newUsersThisMonth = userRepository.findUsersByRegistrationPeriod(monthAgo, Instant.now()).size();
        long newUsersThisWeek = userRepository.findUsersByRegistrationPeriod(weekAgo, Instant.now()).size();

        return new UserStatisticsDTO(
                totalUsers,
                activeUsers,
                usersWithoutTeams,
                teamCaptains,
                averageTeamsPerUser,
                newUsersThisMonth,
                newUsersThisWeek
        );
    }

    @Override
    @Transactional
    public int cleanupInactiveUsers(int daysInactive) {
        log.debug("Cleaning up inactive users older than {} days", daysInactive);

        Instant cutoffDate = Instant.now().minusSeconds(daysInactive * 24 * 60 * 60);
        
        // Здесь можно добавить логику для удаления неактивных пользователей
        // Например, удаление пользователей, которые не были активны долгое время
        
        return 0; // Возвращаем количество удаленных пользователей
    }

    @Override
    public UserDTO toDTO(User user) {
        return teamMapper.toUserDTO(user);
    }

    @Override
    public User toEntity(UserDTO userDTO) {
        return teamMapper.toEntity(userDTO);
    }
}