package dn.quest.notification.controller;

import dn.quest.notification.dto.ApiResponse;
import dn.quest.notification.entity.UserNotificationPreferences;
import dn.quest.notification.enums.NotificationType;
import dn.quest.notification.service.UserNotificationPreferencesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST контроллер для управления пользовательскими предпочтениями уведомлений
 */
@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Notification Preferences", description = "API для управления пользовательскими предпочтениями уведомлений")
public class UserNotificationPreferencesController {

    private final UserNotificationPreferencesService preferencesService;

    @Operation(summary = "Создать предпочтения", description = "Создать предпочтения уведомлений для пользователя")
    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserNotificationPreferences>> createPreferences(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId) {
        
        log.info("Creating notification preferences for user: {}", userId);
        
        try {
            UserNotificationPreferences preferences = preferencesService.createPreferences(userId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(preferences, "Preferences created successfully"));
            
        } catch (Exception e) {
            log.error("Error creating preferences for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create preferences: " + e.getMessage()));
        }
    }

    @Operation(summary = "Получить предпочтения", description = "Получить предпочтения уведомлений пользователя")
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserNotificationPreferences>> getPreferences(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId) {
        
        log.info("Getting notification preferences for user: {}", userId);
        
        try {
            return preferencesService.getPreferences(userId)
                    .map(preferences -> ResponseEntity.ok(ApiResponse.success(preferences)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Preferences not found for user: " + userId)));
            
        } catch (Exception e) {
            log.error("Error getting preferences for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get preferences: " + e.getMessage()));
        }
    }

    @Operation(summary = "Обновить предпочтения", description = "Обновить предпочтения уведомлений пользователя")
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserNotificationPreferences>> updatePreferences(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @Valid @RequestBody UserNotificationPreferences preferences) {
        
        log.info("Updating notification preferences for user: {}", userId);
        
        try {
            UserNotificationPreferences updated = preferencesService.updatePreferences(userId, preferences);
            
            return ResponseEntity.ok(ApiResponse.success(updated, "Preferences updated successfully"));
            
        } catch (Exception e) {
            log.error("Error updating preferences for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update preferences: " + e.getMessage()));
        }
    }

    @Operation(summary = "Обновить конкретные настройки", description = "Обновить конкретные настройки предпочтений")
    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserNotificationPreferences>> updatePartialPreferences(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @RequestBody Map<String, Object> updates) {
        
        log.info("Updating partial preferences for user: {}", userId);
        
        try {
            UserNotificationPreferences updated = preferencesService.updatePreferences(userId, updates);
            
            return ResponseEntity.ok(ApiResponse.success(updated, "Preferences updated successfully"));
            
        } catch (Exception e) {
            log.error("Error updating partial preferences for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update preferences: " + e.getMessage()));
        }
    }

    @Operation(summary = "Удалить предпочтения", description = "Удалить предпочтения уведомлений пользователя")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> deletePreferences(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId) {
        
        log.info("Deleting notification preferences for user: {}", userId);
        
        try {
            preferencesService.deletePreferences(userId);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Preferences deleted successfully"));
            
        } catch (Exception e) {
            log.error("Error deleting preferences for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete preferences: " + e.getMessage()));
        }
    }

    @Operation(summary = "Переключить тип уведомлений", description = "Включить или выключить тип уведомлений")
    @PutMapping("/{userId}/types/{type}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserNotificationPreferences>> toggleNotificationType(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @Parameter(description = "Тип уведомлений") @PathVariable NotificationType type,
            @Parameter(description = "Включить") @RequestParam boolean enabled) {
        
        log.info("Toggling notification type {} for user {} to: {}", type, userId, enabled);
        
        try {
            UserNotificationPreferences updated = preferencesService.toggleNotificationType(userId, type, enabled);
            
            return ResponseEntity.ok(ApiResponse.success(updated, "Notification type updated successfully"));
            
        } catch (Exception e) {
            log.error("Error toggling notification type for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to toggle notification type: " + e.getMessage()));
        }
    }

    @Operation(summary = "Переключить категорию уведомлений", description = "Включить или выключить категорию уведомлений")
    @PutMapping("/{userId}/categories/{category}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserNotificationPreferences>> toggleNotificationCategory(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @Parameter(description = "Категория уведомлений") @PathVariable String category,
            @Parameter(description = "Включить") @RequestParam boolean enabled) {
        
        log.info("Toggling notification category {} for user {} to: {}", category, userId, enabled);
        
        try {
            UserNotificationPreferences updated = preferencesService.toggleNotificationCategory(userId, category, enabled);
            
            return ResponseEntity.ok(ApiResponse.success(updated, "Notification category updated successfully"));
            
        } catch (Exception e) {
            log.error("Error toggling notification category for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to toggle notification category: " + e.getMessage()));
        }
    }

    @Operation(summary = "Установить Do Not Disturb", description = "Настроить режим Do Not Disturb")
    @PutMapping("/{userId}/do-not-disturb")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserNotificationPreferences>> setDoNotDisturb(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @RequestBody DoNotDisturbRequest request) {
        
        log.info("Setting Do Not Disturb for user {}: enabled={}, start={}, end={}", 
                userId, request.isEnabled(), request.getStartHour(), request.getEndHour());
        
        try {
            UserNotificationPreferences updated = preferencesService.setDoNotDisturb(
                    userId, request.isEnabled(), request.getStartHour(), request.getEndHour());
            
            return ResponseEntity.ok(ApiResponse.success(updated, "Do Not Disturb settings updated successfully"));
            
        } catch (Exception e) {
            log.error("Error setting Do Not Disturb for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to set Do Not Disturb: " + e.getMessage()));
        }
    }

    @Operation(summary = "Установить лимиты уведомлений", description = "Настроить лимиты уведомлений")
    @PutMapping("/{userId}/limits")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserNotificationPreferences>> setNotificationLimits(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @RequestBody NotificationLimitsRequest request) {
        
        log.info("Setting notification limits for user {}: perHour={}, perDay={}", 
                userId, request.getPerHour(), request.getPerDay());
        
        try {
            UserNotificationPreferences updated = preferencesService.setNotificationLimits(
                    userId, request.getPerHour(), request.getPerDay());
            
            return ResponseEntity.ok(ApiResponse.success(updated, "Notification limits updated successfully"));
            
        } catch (Exception e) {
            log.error("Error setting notification limits for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to set notification limits: " + e.getMessage()));
        }
    }

    @Operation(summary = "Обновить FCM token", description = "Обновить FCM token для push уведомлений")
    @PutMapping("/{userId}/fcm-token")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @RequestBody FcmTokenRequest request) {
        
        log.info("Updating FCM token for user: {}", userId);
        
        try {
            preferencesService.updateFcmToken(userId, request.getFcmToken());
            
            return ResponseEntity.ok(ApiResponse.success(null, "FCM token updated successfully"));
            
        } catch (Exception e) {
            log.error("Error updating FCM token for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update FCM token: " + e.getMessage()));
        }
    }

    @Operation(summary = "Обновить Telegram chat ID", description = "Обновить Telegram chat ID")
    @PutMapping("/{userId}/telegram")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> updateTelegramChatId(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @RequestBody TelegramChatIdRequest request) {
        
        log.info("Updating Telegram chat ID for user: {}", userId);
        
        try {
            preferencesService.updateTelegramChatId(userId, request.getTelegramChatId());
            
            return ResponseEntity.ok(ApiResponse.success(null, "Telegram chat ID updated successfully"));
            
        } catch (Exception e) {
            log.error("Error updating Telegram chat ID for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update Telegram chat ID: " + e.getMessage()));
        }
    }

    @Operation(summary = "Сбросить настройки", description = "Сбросить настройки к значениям по умолчанию")
    @PostMapping("/{userId}/reset")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserNotificationPreferences>> resetToDefaults(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId) {
        
        log.info("Resetting notification preferences to defaults for user: {}", userId);
        
        try {
            UserNotificationPreferences reset = preferencesService.resetToDefaults(userId);
            
            return ResponseEntity.ok(ApiResponse.success(reset, "Preferences reset to defaults successfully"));
            
        } catch (Exception e) {
            log.error("Error resetting preferences for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to reset preferences: " + e.getMessage()));
        }
    }

    @Operation(summary = "Экспортировать предпочтения", description = "Экспортировать предпочтения пользователя")
    @GetMapping("/{userId}/export")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportPreferences(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId) {
        
        log.info("Exporting notification preferences for user: {}", userId);
        
        try {
            Map<String, Object> exported = preferencesService.exportPreferences(userId);
            
            return ResponseEntity.ok(ApiResponse.success(exported));
            
        } catch (Exception e) {
            log.error("Error exporting preferences for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to export preferences: " + e.getMessage()));
        }
    }

    @Operation(summary = "Импортировать предпочтения", description = "Импортировать предпочтения пользователя")
    @PostMapping("/{userId}/import")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserNotificationPreferences>> importPreferences(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @RequestBody Map<String, Object> data) {
        
        log.info("Importing notification preferences for user: {}", userId);
        
        try {
            UserNotificationPreferences imported = preferencesService.importPreferences(userId, data);
            
            return ResponseEntity.ok(ApiResponse.success(imported, "Preferences imported successfully"));
            
        } catch (Exception e) {
            log.error("Error importing preferences for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to import preferences: " + e.getMessage()));
        }
    }

    @Operation(summary = "Получить доступные каналы", description = "Получить доступные каналы для пользователя")
    @GetMapping("/{userId}/channels")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<NotificationType>>> getAvailableChannels(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId) {
        
        log.info("Getting available channels for user: {}", userId);
        
        try {
            List<NotificationType> channels = preferencesService.getAvailableChannels(userId);
            
            return ResponseEntity.ok(ApiResponse.success(channels));
            
        } catch (Exception e) {
            log.error("Error getting available channels for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get available channels: " + e.getMessage()));
        }
    }

    @Operation(summary = "Получить статистику предпочтений", description = "Получить статистику по предпочтениям")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getPreferencesStatistics() {
        
        log.info("Getting preferences statistics");
        
        try {
            Object statistics = preferencesService.getPreferencesStatistics();
            
            return ResponseEntity.ok(ApiResponse.success(statistics));
            
        } catch (Exception e) {
            log.error("Error getting preferences statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get preferences statistics: " + e.getMessage()));
        }
    }

    // DTO классы для запросов

    public static class DoNotDisturbRequest {
        private boolean enabled;
        private Integer startHour;
        private Integer endHour;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getStartHour() {
            return startHour;
        }

        public void setStartHour(Integer startHour) {
            this.startHour = startHour;
        }

        public Integer getEndHour() {
            return endHour;
        }

        public void setEndHour(Integer endHour) {
            this.endHour = endHour;
        }
    }

    public static class NotificationLimitsRequest {
        private Integer perHour;
        private Integer perDay;

        public Integer getPerHour() {
            return perHour;
        }

        public void setPerHour(Integer perHour) {
            this.perHour = perHour;
        }

        public Integer getPerDay() {
            return perDay;
        }

        public void setPerDay(Integer perDay) {
            this.perDay = perDay;
        }
    }

    public static class FcmTokenRequest {
        private String fcmToken;

        public String getFcmToken() {
            return fcmToken;
        }

        public void setFcmToken(String fcmToken) {
            this.fcmToken = fcmToken;
        }
    }

    public static class TelegramChatIdRequest {
        private String telegramChatId;

        public String getTelegramChatId() {
            return telegramChatId;
        }

        public void setTelegramChatId(String telegramChatId) {
            this.telegramChatId = telegramChatId;
        }
    }
}