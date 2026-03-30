package dn.quest.authentication.service;

import dn.quest.shared.dto.UserDTO;
import dn.quest.authentication.entity.User;
import dn.quest.shared.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Сервис управления пользователями
 */
public interface UserService {

    /**
     * Создание нового пользователя
     */
    User createUser(String username, String password, String email, String publicName, UserRole role);

    /**
     * Получение пользователя по ID
     */
    Optional<User> findById(Long id);

    /**
     * Получение пользователя по имени
     */
    Optional<User> findByUsername(String username);

    /**
     * Получение пользователя по email
     */
    Optional<User> findByEmail(String email);

    /**
     * Получение пользователя по токену сброса пароля
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Получение пользователя по имени с разрешениями
     */
    Optional<User> findByUsernameWithPermissions(String username);

    /**
     * Обновление пользователя
     */
    User updateUser(User user);

    /**
     * Удаление пользователя
     */
    void deleteUser(Long id);

    /**
     * Активация/деактивация пользователя
     */
    User toggleUserStatus(Long id, boolean isActive);

    /**
     * Изменение роли пользователя
     */
    User changeUserRole(Long id, UserRole newRole);

    /**
     * Обновление последнего входа
     */
    void updateLastLogin(String username);

    /**
     * Проверка существования пользователя по имени
     */
    boolean existsByUsername(String username);

    /**
     * Проверка существования пользователя по email
     */
    boolean existsByEmail(String email);

    /**
     * Получение всех пользователей с пагинацией
     */
    Page<User> findAllUsers(Pageable pageable);

    /**
     * Получение пользователей по роли
     */
    List<User> findByRole(UserRole role);

    /**
     * Получение активных пользователей
     */
    List<User> findActiveUsers();

    /**
     * Поиск пользователей по имени
     */
    Page<User> findByPublicNameContaining(String name, Pageable pageable);

    /**
     * Конвертация User в UserDTO
     */
    UserDTO toDTO(User user);

    /**
     * Конвертация User в UserDTO с разрешениями
     */
    UserDTO toDTOWithPermissions(User user);
}