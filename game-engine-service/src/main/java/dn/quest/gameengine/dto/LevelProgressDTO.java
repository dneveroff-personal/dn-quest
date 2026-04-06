package dn.quest.gameengine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO для прогресса по уровню
 */
@Schema(description = "Прогресс пользователя по уровню")
public record LevelProgressDTO(
    
    @Schema(description = "ID прогресса", example = "12345")
    Long id,
    
    @Schema(description = "ID сессии", example = "789")
    UUID sessionId,
    
    @Schema(description = "ID пользователя", example = "123")
    UUID userId,
    
    @Schema(description = "ID уровня", example = "101")
    UUID levelId,
    
    @Schema(description = "Название уровня", example = "Первый этаж")
    String levelName,
    
    @Schema(description = "Описание уровня", example = "Найдите все коды на первом этаже")
    String levelDescription,
    
    @Schema(description = "Статус прогресса", example = "IN_PROGRESS")
    String status,
    
    @Schema(description = "Прогресс в процентах", example = "75.0")
    Double progressPercentage,
    
    @Schema(description = "Счет за уровень", example = "250.0")
    Double score,
    
    @Schema(description = "Бонусные очки", example = "50.0")
    Double bonusPoints,
    
    @Schema(description = "Штрафные очки", example = "-20.0")
    Double penaltyPoints,
    
    @Schema(description = "Общий счет", example = "280.0")
    Double totalScore,
    
    @Schema(description = "Количество найденных кодов", example = "3")
    Integer codesFound,
    
    @Schema(description = "Общее количество кодов", example = "4")
    Integer totalCodes,
    
    @Schema(description = "Количество попыток", example = "8")
    Integer attemptsCount,
    
    @Schema(description = "Количество правильных попыток", example = "3")
    Integer correctAttempts,
    
    @Schema(description = "Количество неправильных попыток", example = "5")
    Integer incorrectAttempts,
    
    @Schema(description = "Количество использованных подсказок", example = "2")
    Integer hintsUsed,
    
    @Schema(description = "Время начала уровня")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    Instant startedAt,
    
    @Schema(description = "Время завершения уровня")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    Instant completedAt,
    
    @Schema(description = "Время последней активности")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    Instant lastActivityAt,
    
    @Schema(description = "Затраченное время в секундах", example = "1800")
    Long timeSpentSeconds,
    
    @Schema(description = "Информация о найденных кодах")
    List<FoundCodeDTO> foundCodes,
    
    @Schema(description = "Информация о подсказках")
    List<UsedHintDTO> usedHints,
    
    @Schema(description = "Достижения, полученные за уровень")
    List<AchievementDTO> achievements
) {
    
    /**
     * DTO для информации о найденном коде
     */
    @Schema(description = "Информация о найденном коде")
    public record FoundCodeDTO(
        
        @Schema(description = "ID кода", example = "1001")
        Long codeId,
        
        @Schema(description = "Значение кода", example = "ABC123")
        String codeValue,
        
        @Schema(description = "Сектор", example = "A")
        String sector,
        
        @Schema(description = "Тип кода", example = "MAIN")
        String codeType,
        
        @Schema(description = "Очки за код", example = "100.0")
        Double points,
        
        @Schema(description = "Время нахождения")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        Instant foundAt,
        
        @Schema(description = "Количество попыток до нахождения", example = "3")
        Integer attemptsToFind,
        
        @Schema(description = "Был ли код бонусным", example = "false")
        Boolean isBonus
    ) {}
    
    /**
     * DTO для информации о использованной подсказке
     */
    @Schema(description = "Информация о использованной подсказке")
    public record UsedHintDTO(
        
        @Schema(description = "ID подсказки", example = "2001")
        Long hintId,
        
        @Schema(description = "Текст подсказки", example = "Ищите рядом с большим деревом")
        String hintText,
        
        @Schema(description = "Штрафные очки", example = "-10.0")
        Double penaltyPoints,
        
        @Schema(description = "Время использования")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        Instant usedAt,
        
        @Schema(description = "Порядковый номер подсказки", example = "1")
        Integer hintOrder
    ) {}
    
    /**
     * DTO для достижения
     */
    @Schema(description = "Достижение")
    public record AchievementDTO(
        
        @Schema(description = "ID достижения", example = "ACH_001")
        String id,
        
        @Schema(description = "Название достижения", example = "Первооткрыватель")
        String title,
        
        @Schema(description = "Описание достижения", example = "Первый нашедший код на уровне")
        String description,
        
        @Schema(description = "URL иконки", example = "https://example.com/icon.png")
        String iconUrl,
        
        @Schema(description = "Очки за достижение", example = "25")
        Integer points,
        
        @Schema(description = "Время получения")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        Instant unlockedAt
    ) {}
}