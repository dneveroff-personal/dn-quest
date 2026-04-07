package dn.quest.filestorage.client;

import dn.quest.shared.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign клиент для взаимодействия с Authentication Service
 */
@FeignClient(name = "authentication-service")
public interface AuthenticationServiceClient {

    /**
     * Получить пользователя по имени пользователя
     */
    @GetMapping("/api/auth/users/username/{username}")
    UserDTO getUserByUsername(@PathVariable String username);

    /**
     * Получить пользователя по токену
     */
    @GetMapping("/api/auth/users/token")
    UserDTO getUserByToken(@RequestBody String token);

    /**
     * Валидировать токен
     */
    @PostMapping("/api/auth/validate")
    boolean validateToken(@RequestBody String token);

    /**
     * Извлечь имя пользователя из токена
     */
    @PostMapping("/api/auth/extract-username")
    String extractUsername(@RequestBody String token);

    /**
     * Проверить права доступа пользователя
     */
    @PostMapping("/api/auth/check-permission")
    boolean checkPermission(@RequestBody PermissionCheckRequest request);

    /**
     * DTO для проверки прав доступа
     */
    class PermissionCheckRequest {
        private String username;
        private String permission;

        public PermissionCheckRequest() {}

        public PermissionCheckRequest(String username, String permission) {
            this.username = username;
            this.permission = permission;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }
    }
}