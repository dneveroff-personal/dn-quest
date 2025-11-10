package dn.quest.gameengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * DTO для запроса на создание игровой сессии
 */
@Schema(description = "Запрос на создание игровой сессии")
public record CreateGameSessionRequest(
    
    @Schema(description = "Название сессии", example = "Вечерний квест", required = true)
    @NotBlank(message = "Название сессии не может быть пустым")
    String name,
    
    @Schema(description = "Описание сессии", example = "Увлекательный квест для всей команды")
    String description,
    
    @Schema(description = "ID квеста", example = "456", required = true)
    @NotNull(message = "ID квеста обязателен")
    Long questId,
    
    @Schema(description = "ID команды (необязательно)", example = "789")
    Long teamId,
    
    @Schema(description = "Максимальное количество участников", example = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "Минимальное количество участников - 1")
    @Max(value = 100, message = "Максимальное количество участников - 100")
    Integer maxParticipants,
    
    @Schema(description = "Является ли сессия приватной", example = "false")
    Boolean isPrivate,
    
    @Schema(description = "Требуется ли одобрение для присоединения", example = "false")
    Boolean requiresApproval,
    
    @Schema(description = "Время начала сессии (ISO формат)")
    String scheduledStartTime,
    
    @Schema(description = "Длительность сессии в минутах", example = "120")
    @Min(value = 1, message = "Длительность должна быть положительной")
    Integer durationMinutes,
    
    @Schema(description = "Настройки сессии")
    SessionSettingsDTO settings
) {
    
    /**
     * DTO для настроек сессии
     */
    @Schema(description = "Настройки сессии")
    public record SessionSettingsDTO(
        
        @Schema(description = "Разрешить подсказки", example = "true")
        Boolean allowHints,
        
        @Schema(description = "Максимальное количество подсказок на уровень", example = "3")
        Integer maxHintsPerLevel,
        
        @Schema(description = "Штраф за использование подсказки", example = "10.0")
        Double hintPenalty,
        
        @Schema(description = "Разрешить пропуск уровней", example = "false")
        Boolean allowSkipLevels,
        
        @Schema(description = "Лимит времени на уровень в минутах", example = "30")
        Integer levelTimeLimit,
        
        @Schema(description = "Разрешить паузу", example = "true")
        Boolean allowPause,
        
        @Schema(description = "Максимальное количество попыток на код", example = "10")
        Integer maxAttemptsPerCode,
        
        @Schema(description = "Показывать лидерборд в реальном времени", example = "true")
        Boolean showRealtimeLeaderboard,
        
        @Schema(description = "Включить звуковые эффекты", example = "true")
        Boolean enableSoundEffects,
        
        @Schema(description = "Включить визуальные эффекты", example = "true")
        Boolean enableVisualEffects
    ) {}
}