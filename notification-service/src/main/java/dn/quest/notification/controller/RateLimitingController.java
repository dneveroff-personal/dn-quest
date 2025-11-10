package dn.quest.notification.controller;

import dn.quest.notification.service.RateLimitingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления Rate Limiting
 */
@RestController
@RequestMapping("/api/rate-limiting")
@Tag(name = "Rate Limiting API", description = "API для управления ограничениями частоты отправки")
public class RateLimitingController {

    private final RateLimitingService rateLimitingService;

    public RateLimitingController(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить статус rate limiting для пользователя")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RateLimitingService.UserRateLimitStatus> getUserRateLimitStatus(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        return ResponseEntity.ok(rateLimitingService.getUserRateLimitStatus(userId));
    }

    @GetMapping("/ip/{ipAddress}")
    @Operation(summary = "Получить статус rate limiting для IP адреса")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RateLimitingService.IpRateLimitStatus> getIpRateLimitStatus(
            @Parameter(description = "IP адрес") @PathVariable String ipAddress) {
        return ResponseEntity.ok(rateLimitingService.getIpRateLimitStatus(ipAddress));
    }

    @GetMapping("/global")
    @Operation(summary = "Получить глобальный статус rate limiting")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RateLimitingService.GlobalRateLimitStatus> getGlobalRateLimitStatus() {
        return ResponseEntity.ok(rateLimitingService.getGlobalRateLimitStatus());
    }

    @PostMapping("/user/{userId}/reset")
    @Operation(summary = "Сбросить счетчики для пользователя")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetUserCounters(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        rateLimitingService.resetUserCounters(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ip/{ipAddress}/reset")
    @Operation(summary = "Сбросить счетчики для IP адреса")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetIpCounters(
            @Parameter(description = "IP адрес") @PathVariable String ipAddress) {
        rateLimitingService.resetIpCounters(ipAddress);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/{userId}/blacklist")
    @Operation(summary = "Добавить пользователя в черный список")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blacklistUser(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "Причина") @RequestParam String reason,
            @Parameter(description = "Длительность в минутах") @RequestParam(defaultValue = "60") long durationMinutes) {
        rateLimitingService.blacklistUser(userId, reason, durationMinutes);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/{userId}/blacklist")
    @Operation(summary = "Удалить пользователя из черного списка")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeFromBlacklist(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        rateLimitingService.removeFromBlacklist(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/blacklisted")
    @Operation(summary = "Проверить, находится ли пользователь в черном списке")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> isUserBlacklisted(
            @Parameter(description = "ID пользователя") @PathVariable Long userId) {
        return ResponseEntity.ok(rateLimitingService.isUserBlacklisted(userId));
    }

    @GetMapping("/check/user/{userId}")
    @Operation(summary = "Проверить, может ли пользователь отправить уведомление")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> canSendNotification(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "Тип уведомления") @RequestParam String notificationType) {
        return ResponseEntity.ok(rateLimitingService.canSendNotification(userId, notificationType));
    }

    @GetMapping("/check/ip/{ipAddress}")
    @Operation(summary = "Проверить, может ли IP отправить запрос")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> canSendFromIp(
            @Parameter(description = "IP адрес") @PathVariable String ipAddress,
            @Parameter(description = "Эндпоинт") @RequestParam String endpoint) {
        return ResponseEntity.ok(rateLimitingService.canSendFromIp(ipAddress, endpoint));
    }

    @GetMapping("/check/global")
    @Operation(summary = "Проверить глобальные лимиты")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> canSendGlobally() {
        return ResponseEntity.ok(rateLimitingService.canSendGlobally());
    }
}