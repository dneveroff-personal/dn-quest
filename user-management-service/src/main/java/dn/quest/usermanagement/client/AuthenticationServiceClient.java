package dn.quest.usermanagement.client;

import dn.quest.shared.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

/**
 * Feign клиент для интеграции с Authentication Service
 */
@FeignClient(name = "authentication-service", url = "${authentication.service.url:http://localhost:8081}")
public interface AuthenticationServiceClient {

    /**
     * Получает информацию о пользователе по ID
     */
    @GetMapping("/api/auth/users/{id}")
    UserDTO getUserById(@PathVariable("id") UUID id);

    /**
     * Получает информацию о пользователе по имени пользователя
     */
    @GetMapping("/api/auth/users/username/{username}")
    UserDTO getUserByUsername(@PathVariable("username") String username);

    /**
     * Валидирует JWT токен
     */
    @GetMapping("/api/auth/validate")
    Boolean validateToken(@RequestHeader("Authorization") String token);

    /**
     * Получает информацию о пользователе из токена
     */
    @GetMapping("/api/auth/me")
    UserDTO getCurrentUser(@RequestHeader("Authorization") String token);
}