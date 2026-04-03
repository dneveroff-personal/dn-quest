package dn.quest.usermanagement.controller;

import dn.quest.usermanagement.dto.UpdateSettingsRequestDTO;
import dn.quest.usermanagement.dto.UserSettingsDTO;
import dn.quest.usermanagement.service.UserSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления настройками пользователей
 */
@RestController
@RequestMapping("/api/users/settings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Settings", description = "API для управления настройками пользователей")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Получить настройки пользователя", description = "Возвращает настройки пользователя по ID")
    public ResponseEntity<UserSettingsDTO> getUserSettings(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId) {
        
        return userSettingsService.getUserSettingsByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Обновить настройки пользователя", description = "Обновляет все настройки пользователя")
    public ResponseEntity<UserSettingsDTO> updateUserSettings(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @Valid @RequestBody UpdateSettingsRequestDTO request) {
        
        UserSettingsDTO updatedSettings = userSettingsService.updateUserSettings(userId, request);
        return ResponseEntity.ok(updatedSettings);
    }

    @PutMapping("/{userId}/privacy")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Обновить настройки приватности", description = "Обновляет настройки приватности пользователя")
    public ResponseEntity<UserSettingsDTO> updatePrivacySettings(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @Parameter(description = "Публичный профиль") @RequestParam(required = false) Boolean profilePublic,
            @Parameter(description = "Показывать email") @RequestParam(required = false) Boolean showEmail,
            @Parameter(description = "Показывать настоящее имя") @RequestParam(required = false) Boolean showRealName,
            @Parameter(description = "Показывать местоположение") @RequestParam(required = false) Boolean showLocation,
            @Parameter(description = "Показывать веб-сайт") @RequestParam(required = false) Boolean showWebsite,
            @Parameter(description = "Показывать статистику") @RequestParam(required = false) Boolean showStatistics) {
        
        UserSettingsDTO updatedSettings = userSettingsService.updatePrivacySettings(
                userId, profilePublic, showEmail, showRealName, showLocation, showWebsite, showStatistics);
        return ResponseEntity.ok(updatedSettings);
    }

    @PutMapping("/{userId}/notifications")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Обновить настройки уведомлений", description = "Обновляет настройки уведомлений пользователя")
    public ResponseEntity<UserSettingsDTO> updateNotificationSettings(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @Parameter(description = "Email уведомления") @RequestParam(required = false) Boolean emailNotifications,
            @Parameter(description = "Приглашения в команды") @RequestParam(required = false) Boolean teamInvitations,
            @Parameter(description = "Напоминания о квестах") @RequestParam(required = false) Boolean questReminders,
            @Parameter(description = "Уведомления о достижениях") @RequestParam(required = false) Boolean achievementNotifications,
            @Parameter(description = "Запросы в друзья") @RequestParam(required = false) Boolean friendRequests,
            @Parameter(description = "Системные уведомления") @RequestParam(required = false) Boolean systemNotifications) {
        
        UserSettingsDTO updatedSettings = userSettingsService.updateNotificationSettings(
                userId, emailNotifications, teamInvitations, questReminders, 
                achievementNotifications, friendRequests, systemNotifications);
        return ResponseEntity.ok(updatedSettings);
    }

    @PutMapping("/{userId}/interface")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Обновить настройки интерфейса", description = "Обновляет настройки интерфейса пользователя")
    public ResponseEntity<UserSettingsDTO> updateInterfaceSettings(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @Parameter(description = "Тема оформления") @RequestParam(required = false) String theme,
            @Parameter(description = "Язык интерфейса") @RequestParam(required = false) String language,
            @Parameter(description = "Часовой пояс") @RequestParam(required = false) String timezone,
            @Parameter(description = "Формат даты") @RequestParam(required = false) String dateFormat,
            @Parameter(description = "Формат времени") @RequestParam(required = false) String timeFormat) {
        
        UserSettingsDTO updatedSettings = userSettingsService.updateInterfaceSettings(
                userId, theme, language, timezone, dateFormat, timeFormat);
        return ResponseEntity.ok(updatedSettings);
    }

    @PutMapping("/{userId}/game")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Обновить игровые настройки", description = "Обновляет игровые настройки пользователя")
    public ResponseEntity<UserSettingsDTO> updateGameSettings(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId,
            @Parameter(description = "Автоматически вступать в команды") @RequestParam(required = false) Boolean autoJoinTeams,
            @Parameter(description = "Показывать подсказки") @RequestParam(required = false) Boolean showHints,
            @Parameter(description = "Звуковые эффекты") @RequestParam(required = false) Boolean soundEffects,
            @Parameter(description = "Музыка") @RequestParam(required = false) Boolean music,
            @Parameter(description = "Анимации") @RequestParam(required = false) Boolean animations) {
        
        UserSettingsDTO updatedSettings = userSettingsService.updateGameSettings(
                userId, autoJoinTeams, showHints, soundEffects, music, animations);
        return ResponseEntity.ok(updatedSettings);
    }

    @PostMapping("/{userId}/reset")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Operation(summary = "Сбросить настройки по умолчанию", description = "Сбрасывает настройки пользователя к значениям по умолчанию")
    public ResponseEntity<UserSettingsDTO> resetToDefaults(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId) {
        
        UserSettingsDTO resetSettings = userSettingsService.resetToDefaults(userId);
        return ResponseEntity.ok(resetSettings);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить настройки пользователя", description = "Удаляет настройки пользователя")
    public ResponseEntity<Void> deleteUserSettings(
            @Parameter(description = "ID пользователя") @PathVariable UUID userId) {
        
        userSettingsService.deleteUserSettings(userId);
        return ResponseEntity.noContent().build();
    }

    // Административные эндпоинты
    @GetMapping("/public")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить пользователей с публичными профилями", description = "Возвращает список пользователей с публичными профилями")
    public ResponseEntity<List<UserSettingsDTO>> getUsersWithPublicProfiles() {
        List<UserSettingsDTO> users = userSettingsService.getUsersWithPublicProfiles();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/email-notifications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить пользователей с email уведомлениями", description = "Возвращает список пользователей с включенными email уведомлениями")
    public ResponseEntity<List<UserSettingsDTO>> getUsersWithEmailNotifications() {
        List<UserSettingsDTO> users = userSettingsService.getUsersWithEmailNotifications();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/notifications/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить пользователей для уведомлений", description = "Возвращает список пользователей для рассылки уведомлений определенного типа")
    public ResponseEntity<List<UserSettingsDTO>> getUsersForNotificationType(
            @Parameter(description = "Тип уведомления") @PathVariable String type) {
        
        List<UserSettingsDTO> users = userSettingsService.getUsersForNotificationType(type);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/theme/{theme}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить пользователей по теме", description = "Возвращает список пользователей с определенной темой оформления")
    public ResponseEntity<List<UserSettingsDTO>> getUsersByTheme(
            @Parameter(description = "Тема оформления") @PathVariable String theme) {
        
        List<UserSettingsDTO> users = userSettingsService.getUsersByTheme(theme);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/language/{language}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить пользователей по языку", description = "Возвращает список пользователей с определенным языком интерфейса")
    public ResponseEntity<List<UserSettingsDTO>> getUsersByLanguage(
            @Parameter(description = "Язык интерфейса") @PathVariable String language) {
        
        List<UserSettingsDTO> users = userSettingsService.getUsersByLanguage(language);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/statistics/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить сводную статистику настроек", description = "Возвращает сводную статистику по настройкам пользователей")
    public ResponseEntity<dn.quest.usermanagement.dto.SettingsStatisticsSummaryDTO> getSettingsStatistics() {
        dn.quest.usermanagement.dto.SettingsStatisticsSummaryDTO summary = userSettingsService.getSettingsStatistics();
        return ResponseEntity.ok(summary);
    }
}