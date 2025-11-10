package dn.quest.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для обработки fallback сценариев
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    /**
     * Fallback для Authentication Service
     */
    @GetMapping("/authentication")
    public Mono<ResponseEntity<Map<String, Object>>> authenticationFallback() {
        log.warn("Authentication Service недоступен - используется fallback");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Сервис аутентификации временно недоступен. Пожалуйста, попробуйте позже.");
        response.put("service", "authentication-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для User Management Service
     */
    @GetMapping("/users")
    public Mono<ResponseEntity<Map<String, Object>>> userManagementFallback() {
        log.warn("User Management Service недоступен - используется fallback");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Сервис управления пользователями временно недоступен. Пожалуйста, попробуйте позже.");
        response.put("service", "user-management-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для Quest Management Service
     */
    @GetMapping("/quests")
    public Mono<ResponseEntity<Map<String, Object>>> questManagementFallback() {
        log.warn("Quest Management Service недоступен - используется fallback");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Сервис управления квестами временно недоступен. Пожалуйста, попробуйте позже.");
        response.put("service", "quest-management-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для Game Engine Service
     */
    @GetMapping("/game")
    public Mono<ResponseEntity<Map<String, Object>>> gameEngineFallback() {
        log.warn("Game Engine Service недоступен - используется fallback");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Игровой движок временно недоступен. Пожалуйста, попробуйте позже.");
        response.put("service", "game-engine-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для Team Management Service
     */
    @GetMapping("/teams")
    public Mono<ResponseEntity<Map<String, Object>>> teamManagementFallback() {
        log.warn("Team Management Service недоступен - используется fallback");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Сервис управления командами временно недоступен. Пожалуйста, попробуйте позже.");
        response.put("service", "team-management-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для Notification Service
     */
    @GetMapping("/notifications")
    public Mono<ResponseEntity<Map<String, Object>>> notificationFallback() {
        log.warn("Notification Service недоступен - используется fallback");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Сервис уведомлений временно недоступен. Уведомления могут быть доставлены с задержкой.");
        response.put("service", "notification-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для Statistics Service
     */
    @GetMapping("/statistics")
    public Mono<ResponseEntity<Map<String, Object>>> statisticsFallback() {
        log.warn("Statistics Service недоступен - используется fallback");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Сервис статистики временно недоступен. Пожалуйста, попробуйте позже.");
        response.put("service", "statistics-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback для File Storage Service
     */
    @GetMapping("/files")
    public Mono<ResponseEntity<Map<String, Object>>> fileStorageFallback() {
        log.warn("File Storage Service недоступен - используется fallback");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Сервис хранения файлов временно недоступен. Пожалуйста, попробуйте позже.");
        response.put("service", "file-storage-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Общий fallback для всех сервисов
     */
    @GetMapping("/general")
    public Mono<ResponseEntity<Map<String, Object>>> generalFallback() {
        log.warn("Сервис недоступен - используется общий fallback");
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Запрошенный сервис временно недоступен. Пожалуйста, попробуйте позже.");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}