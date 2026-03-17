package dn.quest.gameengine.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * DTO для запроса на обновление игровой сессии
 */
@Schema(description = "Запрос на обновление игровой сессии")
public record UpdateGameSessionRequest(
    
    @Schema(description = "Название сессии", example = "Обновленный вечерний квест")
    String name,
    
    @Schema(description = "Описание сессии", example = "Обновленное описание квеста")
    String description,
    
    @Schema(description = "Максимальное количество участников", example = "15", minimum = "1", maximum = "100")
    @Min(value = 1, message = "Минимальное количество участников - 1")
    @Max(value = 100, message = "Максимальное количество участников - 100")
    Integer maxParticipants,
    
    @Schema(description = "Является ли сессия приватной", example = "false")
    Boolean isPrivate,
    
    @Schema(description = "Требуется ли одобрение для присоединения", example = "false")
    Boolean requiresApproval,
    
    @Schema(description = "Время начала сессии (ISO формат)")
    String scheduledStartTime,
    
    @Schema(description = "Длительность сессии в минутах", example = "150")
    @Min(value = 1, message = "Длительность должна быть положительной")
    Integer durationMinutes,
    
    @Schema(description = "Обновленные настройки сессии")
    CreateGameSessionRequest.SessionSettingsDTO settings
) {}