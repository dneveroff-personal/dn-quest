package dn.quest.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для корневого маршрута "/"
 */
@RestController
@Slf4j
public class RootController {

    /**
     * Endpoint для корневого маршрута - возвращает информацию о API Gateway
     */
    @GetMapping("/")
    public Map<String, Object> root() {
        log.info("Запрос к корневому маршруту /");
        
        Map<String, Object> response = new HashMap<>();
        response.put("service", "dn-quest-api-gateway");
        response.put("status", "UP");
        response.put("message", "DN Quest API Gateway работает");
        response.put("timestamp", Instant.now().toString());
        
        Map<String, String> links = new HashMap<>();
        links.put("swagger-ui", "/swagger-ui.html");
        links.put("api-docs", "/api-docs");
        links.put("health", "/actuator/gateway-health");
        response.put("links", links);
        
        return response;
    }
}