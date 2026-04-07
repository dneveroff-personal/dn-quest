package dn.quest.teammanagement.service;

import dn.quest.shared.dto.UserDTO;
import dn.quest.teammanagement.dto.UserStatisticsDTO;
import dn.quest.teammanagement.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

/**
 * Сервис для работы с пользователями (интеграция с User Management Service)
 */
public interface UserService {

    /**
     * Получить пользователя по ID
     */
    UserDTO getUserById(UUID userId);

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
    Optional<User> getUserEntityById(UUID userId);

    /**
     * Получить Entity пользователя по имени пользователя
     */
    Optional<User> getUserEntityByUsername(String username);

    /**
     * Проверить, существует ли пользователь
     */
    boolean userExists(UUID userId);

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
    boolean isUserActive(UUID userId);

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
    List<UserDTO> getUsersForTeamInvitation(UUID teamId, String search, int limit);

    /**
     * Получить пользователей по списку ID
     */
    List<UserDTO> getUsersByIds(List<UUID> userIds);

    /**
     * Получить Entity пользователей по списку ID
     */
    List<User> getUserEntitiesByIds(List<UUID> userIds);

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
    boolean validateUser(UUID userId);

    /**
     * Получить публичную информацию о пользователе
     */
    UserDTO getPublicUserInfo(UUID userId);

    /**
     * Обновить информацию о пользователе
     */
    UserDTO updateUser(UUID userId, UserDTO userDTO);

    /**
     * Деактивировать пользователя
     */
    void deactivateUser(UUID userId);

    /**
     * Активировать пользователя
     */
    void activateUser(UUID userId);

    /**
     * Проверить, может ли пользователь быть приглашен в команду
     */
    boolean canUserBeInvited(UUID userId, UUID teamId);

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
    UUID getUserIdByUsername(String username);

    List<UserDTO> getUsersByRole(String role, Pageable pageable);

    // === Методы для обработки событий Kafka ===

    /**
     * Обновить информацию о пользователе из события
     */
    void updateUserFromEvent(dn.quest.shared.events.user.UserUpdatedEvent event);

    /**
     * Обновить статистику отправки кода пользователя
     */
    void updateCodeSubmissionStatistics(UUID userId, String sessionId);

    /**
     * Обновить статистику завершения уровней
     */
    void updateLevelCompletionStatistics(UUID userId, String sessionId, int levelNumber);

    /**
     * Обновить статистику файлов пользователя
     */
    void updateFileStatistics(UUID userId, Long fileId, String action);

    /**
     * Обновить кэш файлов
     */
    void updateFileCache(Long fileId, dn.quest.shared.events.file.FileUpdatedEvent event);
}
