package dn.quest.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для обработки fallback сценариев
 * Поддерживает все HTTP методы: GET, POST, PUT, DELETE, PATCH
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    /**
     * Fallback для Authentication Service - обрабатывает все HTTP методы
     */
    @RequestMapping(value = "/authentication", method = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.PATCH
    })
    public Mono<ResponseEntity<Map<String, Object>>> authenticationFallback() {
        log.warn("Authentication Service недоступен - используется fallback");

        Map<String, Object> response = createFallbackResponse("authentication-service",
            "Сервис аутентификации временно недоступен. Пожалуйста, попробуйте позже.");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для User Management Service - обрабатывает все HTTP методы
     */
    @RequestMapping(value = "/users", method = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.PATCH
    })
    public Mono<ResponseEntity<Map<String, Object>>> userManagementFallback() {
        log.warn("User Management Service недоступен - используется fallback");

        Map<String, Object> response = createFallbackResponse("user-management-service",
            "Сервис управления пользователями временно недоступен. Пожалуйста, попробуйте позже.");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для Quest Management Service - обрабатывает все HTTP методы
     */
    @RequestMapping(value = "/quests", method = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.PATCH
    })
    public Mono<ResponseEntity<Map<String, Object>>> questManagementFallback() {
        log.warn("Quest Management Service недоступен - используется fallback");

        Map<String, Object> response = createFallbackResponse("quest-management-service",
            "Сервис управления квестами временно недоступен. Пожалуйста, попробуйте позже.");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для Game Engine Service - обрабатывает все HTTP методы
     */
    @RequestMapping(value = "/game", method = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.PATCH
    })
    public Mono<ResponseEntity<Map<String, Object>>> gameEngineFallback() {
        log.warn("Game Engine Service недост��пен - используется fallback");

        Map<String, Object> response = createFallbackResponse("game-engine-service",
            "Игровой движок временно недоступен. Пожалуйста, попробуйте позже.");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для Team Management Service - обрабатывает все HTTP методы
     */
    @RequestMapping(value = "/teams", method = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.PATCH
    })
    public Mono<ResponseEntity<Map<String, Object>>> teamManagementFallback() {
        log.warn("Team Management Service недоступен - используется fallback");

        Map<String, Object> response = createFallbackResponse("team-management-service",
            "Сервис управления командами временно недоступен. Пожалуйста, попробуйте позже.");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для Notification Service - обрабатывает все HTTP методы
     */
    @RequestMapping(value = "/notifications", method = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.PATCH
    })
    public Mono<ResponseEntity<Map<String, Object>>> notificationFallback() {
        log.warn("Notification Service недоступен - используется fallback");

        Map<String, Object> response = createFallbackResponse("notification-service",
            "Сервис уведомлений недоступен. Уведомления могут быть доставлены с задержкой.");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для Statistics Service - обрабатывает все HTTP методы
     */
    @RequestMapping(value = "/statistics", method = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.PATCH
    })
    public Mono<ResponseEntity<Map<String, Object>>> statisticsFallback() {
        log.warn("Statistics Service недоступен - используется fallback");

        Map<String, Object> response = createFallbackResponse("statistics-service",
            "Сервис статистики временно недоступен. Пожалуйста, попробуйте позже.");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для File Storage Service - обрабатывает все HTTP методы
     */
    @RequestMapping(value = "/files", method = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.PATCH
    })
    public Mono<ResponseEntity<Map<String, Object>>> fileStorageFallback() {
        log.warn("File Storage Service недоступен - используется fallback");

        Map<String, Object> response = createFallbackResponse("file-storage-service",
            "Сервис хранения файлов временно недоступен. Пожалуйста, попробуйте позже.");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Общий fallback для всех сервисов - обрабатывает все HTTP методы
     */
    @RequestMapping(value = "/general", method = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.PATCH
    })
    public Mono<ResponseEntity<Map<String, Object>>> generalFallback() {
        log.warn("Сервис недоступен - используется общий fallback");

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Запрошенный сервис временно недоступен. Пожалуйста, попробуйте позже.");
        response.put("timestamp", System.currentTimeMillis());

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Вспомогательный метод для создания ответа fallback
     */
    private Map<String, Object> createFallbackResponse(String serviceName, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", message);
        response.put("service", serviceName);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}