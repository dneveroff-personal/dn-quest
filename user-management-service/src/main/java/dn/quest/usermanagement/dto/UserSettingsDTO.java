package dn.quest.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO для настроек пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Настройки пользователя")
public class UserSettingsDTO {
    
    @Schema(description = "ID настроек", example = "1")
    private Long id;
    
    @Schema(description = "ID пользователя", example = "1")
    private Long userId;
    
    // Настройки приватности
    @Schema(description = "Публичный профиль", example = "true")
    private Boolean profilePublic;
    
    @Schema(description = "Показывать email", example = "false")
    private Boolean showEmail;
    
    @Schema(description = "Показывать настоящее имя", example = "false")
    private Boolean showRealName;
    
    @Schema(description = "Показывать местоположение", example = "true")
    private Boolean showLocation;
    
    @Schema(description = "Показывать веб-сайт", example = "true")
    private Boolean showWebsite;
    
    @Schema(description = "Показывать статистику", example = "true")
    private Boolean showStatistics;
    
    // Настройки уведомлений
    @Schema(description = "Email уведомления", example = "true")
    private Boolean emailNotifications;
    
    @Schema(description = "Приглашения в команды", example = "true")
    private Boolean teamInvitations;
    
    @Schema(description = "Напоминания о квестах", example = "true")
    private Boolean questReminders;
    
    @Schema(description = "Уведомления о достижениях", example = "true")
    private Boolean achievementNotifications;
    
    @Schema(description = "Запросы в друзья", example = "true")
    private Boolean friendRequests;
    
    @Schema(description = "Системные уведомления", example = "true")
    private Boolean systemNotifications;
    
    // Настройки интерфейса
    @Schema(description = "Тема оформления", example = "light", allowableValues = {"light", "dark"})
    private String theme;
    
    @Schema(description = "Язык интерфейса", example = "ru")
    private String language;
    
    @Schema(description = "Часовой пояс", example = "UTC")
    private String timezone;
    
    @Schema(description = "Формат даты", example = "dd.MM.yyyy")
    private String dateFormat;
    
    @Schema(description = "Формат времени", example = "24h", allowableValues = {"12h", "24h"})
    private String timeFormat;
    
    // Настройки игры
    @Schema(description = "Автоматически вступать в команды", example = "false")
    private Boolean autoJoinTeams;
    
    @Schema(description = "Показывать подсказки", example = "true")
    private Boolean showHints;
    
    @Schema(description = "Звуковые эффекты", example = "true")
    private Boolean soundEffects;
    
    @Schema(description = "Музыка", example = "false")
    private Boolean music;
    
    @Schema(description = "Анимации", example = "true")
    private Boolean animations;
    
    @Schema(description = "Дата создания настроек", example = "2024-01-01T12:00:00Z")
    private Instant createdAt;
    
    @Schema(description = "Дата обновления настроек", example = "2024-01-15T10:30:00Z")
    private Instant updatedAt;
}