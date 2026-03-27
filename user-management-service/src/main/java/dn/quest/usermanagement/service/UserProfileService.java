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

/**
 * Сервис для управления профилями пользователей
 */
public interface UserProfileService {

    /**
     * Создает профиль пользователя
     */
    UserProfileDTO createUserProfile(Long userId, String username, String email, String publicName, String role);

    /**
     * Получает профиль пользователя по ID
     */
    Optional<UserProfileDTO> getUserProfileByUserId(Long userId);

    /**
     * Получает профиль пользователя по ID профиля
     */
    Optional<UserProfileDTO> getUserProfileById(Long id);

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
    UserProfileDTO updateUserProfile(Long userId, UpdateProfileRequestDTO request);

    /**
     * Обновляет профиль пользователя из событий Authentication Service
     */
    void updateUserProfileFromAuth(Long userId, String username, String email, String publicName, String role, Boolean isActive);

    /**
     * Обновляет роль пользователя
     */
    void updateUserRole(Long userId, String newRole);

    /**
     * Обновляет аватар пользователя
     */
    UserProfileDTO updateUserAvatar(Long userId, String avatarUrl);

    /**
     * Удаляет аватар пользователя
     */
    UserProfileDTO removeUserAvatar(Long userId);

    /**
     * Блокирует пользователя
     */
    UserProfileDTO blockUser(Long userId, BlockUserRequestDTO request);

    /**
     * Разблокирует пользователя
     */
    UserProfileDTO unblockUser(Long userId);

    /**
     * Активирует пользователя
     */
    UserProfileDTO activateUser(Long userId);

    /**
     * Деактивирует пользователя
     */
    UserProfileDTO deactivateUser(Long userId);

    /**
     * Удаляет профиль пользователя
     */
    void deleteUserProfile(Long userId);

    /**
     * Обновляет время последней активности
     */
    void updateLastActivity(Long userId);

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
    boolean existsByUserId(Long userId);

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