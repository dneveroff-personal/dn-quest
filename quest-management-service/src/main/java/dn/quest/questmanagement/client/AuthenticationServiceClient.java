package dn.quest.questmanagement.client;

import dn.quest.shared.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

/**
 * Feign клиент для интеграции с Authentication Service
 */
@FeignClient(name = "authentication-service", url = "${authentication.service.url:http://authentication-service:8081}")
public interface AuthenticationServiceClient {

    /**
     * Получить информацию о пользователе по ID
     */
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") UUID id);

    /**
     * Получить информацию о пользователе по токену
     */
    @GetMapping("/api/users/me")
    UserDTO getCurrentUser(@RequestHeader("Authorization") String authorization);

    /**
     * Валидировать токен
     */
    @GetMapping("/api/auth/validate")
    Boolean validateToken(@RequestHeader("Authorization") String authorization);

    /**
     * Проверить, существует ли пользователь
     */
    @GetMapping("/api/users/{id}/exists")
    Boolean userExists(@PathVariable("id") UUID id);

    /**
     * Получить пользователей по списку ID
     */
    @GetMapping("/api/users/batch")
    java.util.List<UserDTO> getUsersByIds(@RequestHeader("X-User-Ids") java.util.List<UUID> userIds);
}