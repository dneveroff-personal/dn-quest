package dn.quest.gameengine.dto;

import dn.quest.gameengine.entity.enums.SessionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * DTO для игровой сессии
 */
@Schema(description = "Информация о игровой сессии")
public record GameSessionDTO(
    
    @Schema(description = "ID сессии", example = "1")
    Long id,
    
    @Schema(description = "Название сессии", example = "Вечерний квест")
    String name,
    
    @Schema(description = "Описание сессии", example = "Увлекательный квест для всей команды")
    String description,
    
    @Schema(description = "Статус сессии", example = "ACTIVE")
    SessionStatus status,
    
    @Schema(description = "ID владельца сессии", example = "123")
    Long ownerId,
    
    @Schema(description = "ID квеста", example = "456")
    Long questId,
    
    @Schema(description = "ID команды", example = "789")
    Long teamId,
    
    @Schema(description = "ID текущего уровня", example = "101")
    Long currentLevelId,
    
    @Schema(description = "Максимальное количество участников", example = "10")
    Integer maxParticipants,
    
    @Schema(description = "Текущее количество участников", example = "5")
    Integer participantCount,
    
    @Schema(description = "ID участников")
    List<Long> participantIds,
    
    @Schema(description = "Продолжительность в секундах", example = "3600")
    Long durationSeconds,
    
    @Schema(description = "Общий счет", example = "1500.5")
    Double totalScore,
    
    @Schema(description = "Дата и время создания")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    Instant createdAt,
    
    @Schema(description = "Дата и время начала")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    Instant startedAt,
    
    @Schema(description = "Дата и время завершения")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    Instant finishedAt,
    
    @Schema(description = "Дата и время последней активности")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    Instant lastActivityAt,
    
    @Schema(description = "Информация о квесте")
    QuestSummaryDTO quest,
    
    @Schema(description = "Информация о владельце")
    UserSummaryDTO owner,
    
    @Schema(description = "Информация о команде")
    TeamSummaryDTO team,
    
    @Schema(description = "Прогресс по уровням")
    List<LevelProgressDTO> levelProgress,
    
    @Schema(description = "Статистика сессии")
    SessionStatsDTO stats
) {
    
    /**
     * DTO для краткой информации о квесте
     */
    @Schema(description = "Краткая информация о квесте")
    public record QuestSummaryDTO(
        @Schema(description = "ID квеста", example = "456")
        Long id,
        
        @Schema(description = "Название квеста", example = "Тайна старого замка")
        String name,
        
        @Schema(description = "Тип квеста", example = "ADVENTURE")
        String type,
        
        @Schema(description = "Сложность", example = "MEDIUM")
        String difficulty,
        
        @Schema(description = "Количество уровней", example = "5")
        Integer levelCount,
        
        @Schema(description = "Рейтинг", example = "4.5")
        Double rating
    ) {}
    
    /**
     * DTO для краткой информации о пользователе
     */
    @Schema(description = "Краткая информация о пользователе")
    public record UserSummaryDTO(
        @Schema(description = "ID пользователя", example = "123")
        Long id,
        
        @Schema(description = "Имя пользователя", example = "john_doe")
        String username,
        
        @Schema(description = "Отображаемое имя", example = "John Doe")
        String displayName,
        
        @Schema(description = "URL аватара", example = "https://example.com/avatar.jpg")
        String avatarUrl,
        
        @Schema(description = "Рейтинг", example = "1250.5")
        Double rating
    ) {}
    
    /**
     * DTO для краткой информации о команде
     */
    @Schema(description = "Краткая информации о команде")
    public record TeamSummaryDTO(
        @Schema(description = "ID команды", example = "789")
        Long id,
        
        @Schema(description = "Название команды", example = "Мстители")
        String name,
        
        @Schema(description = "Описание команды", example = "Команда супергероев")
        String description,
        
        @Schema(description = "URL логотипа", example = "https://example.com/logo.jpg")
        String logoUrl,
        
        @Schema(description = "Количество участников", example = "5")
        Integer memberCount,
        
        @Schema(description = "Рейтинг команды", example = "1450.0")
        Double rating
    ) {}
    
    /**
     * DTO для прогресса по уровням
     */
    @Schema(description = "Прогресс по уровням")
    public record LevelProgressDTO(
        @Schema(description = "ID уровня", example = "101")
        Long levelId,
        
        @Schema(description = "Название уровня", example = "Первый этаж")
        String levelName,
        
        @Schema(description = "Статус прохождения", example = "COMPLETED")
        String status,
        
        @Schema(description = "Прогресс в процентах", example = "100.0")
        Double progressPercentage,
        
        @Schema(description = "Счет за уровень", example = "300.0")
        Double score,
        
        @Schema(description = "Время начала")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        Instant startedAt,
        
        @Schema(description = "Время завершения")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        Instant completedAt,
        
        @Schema(description = "Количество попыток", example = "5")
        Integer attemptsCount,
        
        @Schema(description = "Количество использованных подсказок", example = "2")
        Integer hintsUsed
    ) {}
    
    /**
     * DTO для статистики сессии
     */
    @Schema(description = "Статистика сессии")
    public record SessionStatsDTO(
        @Schema(description = "Общее количество попыток", example = "25")
        Integer totalAttempts,
        
        @Schema(description = "Количество правильных попыток", example = "15")
        Integer correctAttempts,
        
        @Schema(description = "Количество неправильных попыток", example = "10")
        Integer incorrectAttempts,
        
        @Schema(description = "Процент успеха", example = "60.0")
        Double successRate,
        
        @Schema(description = "Среднее время на попытку в секундах", example = "120.5")
        Double averageAttemptTime,
        
        @Schema(description = "Количество пройденных уровней", example = "3")
        Integer completedLevels,
        
        @Schema(description = "Общее количество уровней", example = "5")
        Integer totalLevels,
        
        @Schema(description = "Процент прохождения квеста", example = "60.0")
        Double completionPercentage,
        
        @Schema(description = "Количество использованных подсказок", example = "8")
        Integer totalHintsUsed,
        
        @Schema(description = "Штрафные очки", example = "-50.0")
        Double penaltyPoints,
        
        @Schema(description = "Бонусные очки", example = "100.0")
        Double bonusPoints
    ) {}
}