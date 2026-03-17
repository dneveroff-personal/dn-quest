package dn.quest.teammanagement.client;

import dn.quest.teammanagement.dto.UserDTO;
import dn.quest.teammanagement.dto.request.ValidateTokenRequest;
import dn.quest.teammanagement.dto.response.ValidateTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign клиент для взаимодействия с Authentication Service
 */
@FeignClient(name = "authentication-service", url = "${app.services.authentication-service.url}")
public interface AuthenticationServiceClient {

    /**
     * Валидация JWT токена
     */
    @PostMapping("/api/auth/validate")
    ValidateTokenResponse validateToken(@RequestBody ValidateTokenRequest request);

    /**
     * Получение информации о пользователе по токену
     */
    @GetMapping("/api/auth/me")
    UserDTO getCurrentUser(@RequestHeader("Authorization") String authorization);

    /**
     * Получение информации о пользователе по ID
     */
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long userId);

    /**
     * Получение информации о пользователе по имени пользователя
     */
    @GetMapping("/api/users/username/{username}")
    UserDTO getUserByUsername(@PathVariable("username") String username);

    /**
     * Получение информации о пользователе по email
     */
    @GetMapping("/api/users/email/{email}")
    UserDTO getUserByEmail(@PathVariable("email") String email);

    /**
     * Проверка существования пользователя по ID
     */
    @GetMapping("/api/users/{id}/exists")
    boolean userExists(@PathVariable("id") Long userId);

    /**
     * Проверка существования пользователя по имени пользователя
     */
    @GetMapping("/api/users/username/{username}/exists")
    boolean userExistsByUsername(@PathVariable("username") String username);

    /**
     * Проверка существования пользователя по email
     */
    @GetMapping("/api/users/email/{email}/exists")
    boolean userExistsByEmail(@PathVariable("email") String email);

    /**
     * Получение роли пользователя
     */
    @GetMapping("/api/users/{id}/role")
    String getUserRole(@PathVariable("id") Long userId);

    /**
     * Проверка, является ли пользователь администратором
     */
    @GetMapping("/api/users/{id}/is-admin")
    boolean isUserAdmin(@PathVariable("id") Long userId);

    /**
     * Проверка, активен ли пользователь
     */
    @GetMapping("/api/users/{id}/is-active")
    boolean isUserActive(@PathVariable("id") Long userId);

    /**
     * Обновление последней активности пользователя
     */
    @PutMapping("/api/users/{id}/last-activity")
    void updateLastActivity(@PathVariable("id") Long userId);

    /**
     * Получение списка пользователей по IDs
     */
    @PostMapping("/api/users/batch")
    java.util.List<UserDTO> getUsersByIds(@RequestBody java.util.List<Long> userIds);

    /**
     * Поиск пользователей
     */
    @GetMapping("/api/users/search")
    java.util.List<UserDTO> searchUsers(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    );

    /**
     * Получение активных пользователей
     */
    @GetMapping("/api/users/active")
    java.util.List<UserDTO> getActiveUsers(
            @RequestParam(value = "limit", defaultValue = "10") int limit
    );

    /**
     * Получение количества пользователей
     */
    @GetMapping("/api/users/count")
    long getTotalUsersCount();

    /**
     * Получение количества активных пользователей
     */
    @GetMapping("/api/users/active/count")
    long getActiveUsersCount();

    /**
     * Проверка прав доступа пользователя
     */
    @PostMapping("/api/auth/check-permission")
    boolean checkPermission(
            @RequestBody ValidateTokenRequest request,
            @RequestParam("permission") String permission
    );

    /**
     * Обновление информации о пользователе
     */
    @PutMapping("/api/users/{id}")
    UserDTO updateUser(@PathVariable("id") Long userId, @RequestBody UserDTO userDTO);

    /**
     * Деактивация пользователя
     */
    @PutMapping("/api/users/{id}/deactivate")
    void deactivateUser(@PathVariable("id") Long userId);

    /**
     * Активация пользователя
     */
    @PutMapping("/api/users/{id}/activate")
    void activateUser(@PathVariable("id") Long userId);
}