package dn.quest.teammanagement.service;

import dn.quest.teammanagement.dto.UserDTO;
import dn.quest.teammanagement.dto.UserStatisticsDTO;
import dn.quest.teammanagement.entity.User;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

/**
 * Сервис для работы с пользователями (интеграция с User Management Service)
 */
public interface UserService {

    /**
     * Получить пользователя по ID
     */
    UserDTO getUserById(Long userId);

    /**
     * Получить пользователя по имени пользователя
     */
    UserDTO getUserByUsername(String username);

    /**
     * Получить пользователя по email
     */
    UserDTO getUserByEmail(String email);

    /**
     * Получить Entity пользователя по ID
     */
    Optional<User> getUserEntityById(Long userId);

    /**
     * Получить Entity пользователя по имени пользователя
     */
    Optional<User> getUserEntityByUsername(String username);

    /**
     * Проверить, существует ли пользователь
     */
    boolean userExists(Long userId);

    /**
     * Проверить, существует ли пользователь с таким именем
     */
    boolean userExistsByUsername(String username);

    /**
     * Проверить, существует ли пользователь с таким email
     */
    boolean userExistsByEmail(String email);

    /**
     * Проверить, активен ли пользователь
     */
    boolean isUserActive(Long userId);

    /**
     * Поиск пользователей по имени
     */
    List<UserDTO> searchUsersByName(String name, int limit);

    /**
     * Поиск пользователей по имени пользователя или полному имени
     */
    List<UserDTO> searchUsers(String query, int limit);

    /**
     * Получить пользователей для приглашения в команду
     */
    List<UserDTO> getUsersForTeamInvitation(Long teamId, String search, int limit);

    /**
     * Получить пользователей по списку ID
     */
    List<UserDTO> getUsersByIds(List<Long> userIds);

    /**
     * Получить Entity пользователей по списку ID
     */
    List<User> getUserEntitiesByIds(List<Long> userIds);

    /**
     * Создать или обновить пользователя (синхронизация)
     */
    UserDTO syncUser(UserDTO userDTO);

    /**
     * Создать или обновить Entity пользователя
     */
    User syncUserEntity(User user);

    /**
     * Получить количество активных пользователей
     */
    long getActiveUsersCount();

    /**
     * Получить количество активных пользователей
     */
    List<UserDTO> getActiveUsers(int limit);

    /**
     * Получить пользователей, зарегистрированных за период
     */
    List<UserDTO> getUsersByRegistrationPeriod(java.time.Instant startDate, 
                                               java.time.Instant endDate);

    /**
     * Получить пользователей без команд
     */
    List<UserDTO> getUsersWithoutTeams();

    /**
     * Получить капитанов команд
     */
    List<UserDTO> getTeamCaptains();

    /**
     * Получить пользователей с определенным количеством команд
     */
    List<UserDTO> getUsersByTeamCount(int teamCount);

    /**
     * Валидировать пользователя
     */
    boolean validateUser(Long userId);

    /**
     * Получить публичную информацию о пользователе
     */
    UserDTO getPublicUserInfo(Long userId);

    /**
     * Обновить информацию о пользователе
     */
    UserDTO updateUser(Long userId, UserDTO userDTO);

    /**
     * Деактивировать пользователя
     */
    void deactivateUser(Long userId);

    /**
     * Активировать пользователя
     */
    void activateUser(Long userId);

    /**
     * Проверить, может ли пользователь быть приглашен в команду
     */
    boolean canUserBeInvited(Long userId, Long teamId);

    /**
     * Получить пользователей по первым буквам имени
     */
    List<UserDTO> getUsersByUsernamePrefix(String prefix, int limit);

    /**
     * Получить статистику по пользователям
     */
    UserStatisticsDTO getUserStatistics();

    /**
     * Очистить неактивных пользователей
     */
    int cleanupInactiveUsers(int daysInactive);

    /**
     * Конвертировать Entity в DTO
     */
    UserDTO toDTO(User user);

    /**
     * Конвертировать DTO в Entity
     */
    User toEntity(UserDTO userDTO);

    /**
     * Получить ID юзера по имени
     */
    Long getUserIdByUsername(String username);

    List<UserDTO> getUsersByRole(String role, Pageable pageable);
}
