package dn.quest.usermanagement.service;

import dn.quest.shared.enums.UserRole;
import dn.quest.usermanagement.dto.BlockUserRequestDTO;
import dn.quest.usermanagement.dto.UpdateProfileRequestDTO;
import dn.quest.usermanagement.dto.UserProfileDTO;
import dn.quest.usermanagement.dto.UserSearchRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления профилями пользователей
 */
public interface UserProfileService {

    /**
     * Создает профиль пользователя
     */
    UserProfileDTO createUserProfile(UUID userId, String username, String email, String publicName, String role);

    /**
     * Получает профиль пользователя по ID
     */
    Optional<UserProfileDTO> getUserProfileByUserId(UUID userId);

    /**
     * Получает профиль пользователя по ID профиля
     */
    Optional<UserProfileDTO> getUserProfileById(UUID id);

    /**
     * Получает профиль пользователя по имени пользователя
     */
    Optional<UserProfileDTO> getUserProfileByUsername(String username);

    /**
     * Получает профиль пользователя по email
     */
    Optional<UserProfileDTO> getUserProfileByEmail(String email);

    /**
     * Обновляет профиль пользователя
     */
    UserProfileDTO updateUserProfile(UUID userId, UpdateProfileRequestDTO request);

    /**
     * Обновляет профиль пользователя из событий Authentication Service
     */
    void updateUserProfileFromAuth(UUID userId, String username, String email, String publicName, String role, Boolean isActive);

    /**
     * Обновляет роль пользователя
     */
    void updateUserRole(UUID userId, String newRole);

    /**
     * Обновляет аватар пользователя
     */
    UserProfileDTO updateUserAvatar(UUID userId, String avatarUrl);

    /**
     * Удаляет аватар пользователя
     */
    UserProfileDTO removeUserAvatar(UUID userId);

    /**
     * Блокирует пользователя
     */
    UserProfileDTO blockUser(UUID userId, BlockUserRequestDTO request);

    /**
     * Разблокирует пользователя
     */
    UserProfileDTO unblockUser(UUID userId);

    /**
     * Активирует пользователя
     */
    UserProfileDTO activateUser(UUID userId);

    /**
     * Деактивирует пользователя
     */
    UserProfileDTO deactivateUser(UUID userId);

    /**
     * Удаляет профиль пользователя
     */
    void deleteUserProfile(UUID userId);

    /**
     * Обновляет время последней активности
     */
    void updateLastActivity(UUID userId);

    /**
     * Поиск пользователей по критериям
     */
    Page<UserProfileDTO> searchUsers(UserSearchRequestDTO request, Pageable pageable);

    /**
     * Получает всех пользователей с пагинацией
     */
    Page<UserProfileDTO> getAllUsers(Pageable pageable);

    /**
     * Получает пользователей по роли
     */
    Page<UserProfileDTO> getUsersByRole(UserRole role, Pageable pageable);

    /**
     * Получает активных пользователей
     */
    Page<UserProfileDTO> getActiveUsers(Pageable pageable);

    /**
     * Получает заблокированных пользователей
     */
    List<UserProfileDTO> getBlockedUsers();

    /**
     * Получает пользователей с истекшей блокировкой
     */
    List<UserProfileDTO> getExpiredBlockedUsers();

    /**
     * Получает недавно зарегистрированных пользователей
     */
    List<UserProfileDTO> getRecentlyRegisteredUsers(int days);

    /**
     * Получает активных пользователей за период
     */
    List<UserProfileDTO> getActiveUsersSince(java.time.Instant since);

    /**
     * Проверяет существование профиля пользователя
     */
    boolean existsByUserId(UUID userId);

    /**
     * Проверяет существование пользователя по имени
     */
    boolean existsByUsername(String username);

    /**
     * Проверяет существование пользователя по email
     */
    boolean existsByEmail(String email);

    /**
     * Получает статистику по пользователям
     */
    dn.quest.usermanagement.dto.UserStatisticsSummaryDTO getUserStatisticsSummary();

    /**
     * Обновляет профиль пользователя из события
     */
    void updateUserProfileFromEvent(dn.quest.shared.events.user.UserUpdatedEvent event);
}