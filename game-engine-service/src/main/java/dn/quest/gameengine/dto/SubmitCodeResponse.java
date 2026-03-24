package dn.quest.gameengine.dto;

import dn.quest.shared.enums.AttemptResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * DTO для ответа на отправку кода
 */
@Schema(description = "Ответ на проверку кода")
public record SubmitCodeResponse(
    
    @Schema(description = "Успешность проверки кода", example = "true")
    Boolean success,
    
    @Schema(description = "Результат попытки", example = "CORRECT")
    AttemptResult result,
    
    @Schema(description = "Сообщение о результате", example = "Код принят! Переход на следующий уровень.")
    String message,
    
    @Schema(description = "ID попытки", example = "12345")
    Long attemptId,
    
    @Schema(description = "Полученные очки", example = "100.0")
    Double points,
    
    @Schema(description = "Бонусные очки", example = "20.0")
    Double bonusPoints,
    
    @Schema(description = "Штрафные очки", example = "-10.0")
    Double penaltyPoints,
    
    @Schema(description = "Общий счет за попытку", example = "110.0")
    Double totalScore,
    
    @Schema(description = "Текущий общий счет", example = "1250.5")
    Double currentTotalScore,
    
    @Schema(description = "Номер попытки", example = "3")
    Integer attemptNumber,
    
    @Schema(description = "Оставшееся количество попыток", example = "7")
    Integer remainingAttempts,
    
    @Schema(description = "Время затраченное на попытку в секундах", example = "45.5")
    Double timeSpent,
    
    @Schema(description = "Информация о коде")
    CodeInfoDTO codeInfo,
    
    @Schema(description = "Информация о прогрессе")
    ProgressInfoDTO progressInfo,
    
    @Schema(description = "Информация о лидерборде")
    LeaderboardInfoDTO leaderboardInfo,
    
    @Schema(description = "Достижения, разблокированные этой попыткой")
    List<AchievementDTO> unlockedAchievements,
    
    @Schema(description = "Следующие доступные действия")
    List<String> nextActions,
    
    @Schema(description = "Дополнительные метаданные")
    ResponseMetadataDTO metadata
) {
    
    /**
     * DTO для информации о коде
     */
    @Schema(description = "Информация о коде")
    public record CodeInfoDTO(
        
        @Schema(description = "Отправленный код", example = "ABC123")
        String submittedCode,
        
        @Schema(description = "Правильный код (если разрешено)", example = "ABC123")
        String correctCode,
        
        @Schema(description = "Сектор кода", example = "A")
        String sector,
        
        @Schema(description = "Тип кода", example = "MAIN")
        String codeType,
        
        @Schema(description = "Базовая стоимость кода", example = "100.0")
        Double basePoints,
        
        @Schema(description = "Множитель сектора", example = "1.5")
        Double sectorMultiplier,
        
        @Schema(description = "Множитель сложности", example = "1.2")
        Double difficultyMultiplier,
        
        @Schema(description = "Был ли код бонусным", example = "true")
        Boolean isBonus,
        
        @Schema(description = "Описание бонуса", example = "Бонус за скорость")
        String bonusDescription
    ) {}
    
    /**
     * DTO для информации о прогрессе
     */
    @Schema(description = "Информация о прогрессе")
    public record ProgressInfoDTO(
        
        @Schema(description = "ID текущего уровня", example = "101")
        Long currentLevelId,
        
        @Schema(description = "Название текущего уровня", example = "Первый этаж")
        String currentLevelName,
        
        @Schema(description = "Прогресс по текущему уровню в процентах", example = "100.0")
        Double currentLevelProgress,
        
        @Schema(description = "Статус текущего уровня", example = "COMPLETED")
        String currentLevelStatus,
        
        @Schema(description = "ID следующего уровня", example = "102")
        Long nextLevelId,
        
        @Schema(description = "Название следующего уровня", example = "Второй этаж")
        String nextLevelName,
        
        @Schema(description = "Общий прогресс по квесту в процентах", example = "40.0")
        Double overallProgress,
        
        @Schema(description = "Количество пройденных уровней", example = "2")
        Integer completedLevels,
        
        @Schema(description = "Общее количество уровней", example = "5")
        Integer totalLevels,
        
        @Schema(description = "Время начала уровня")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        Instant levelStartedAt,
        
        @Schema(description = "Время завершения уровня")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        Instant levelCompletedAt
    ) {}
    
    /**
     * DTO для информации о лидерборде
     */
    @Schema(description = "Информация о лидерборде")
    public record LeaderboardInfoDTO(
        
        @Schema(description = "Текущий рейтинг пользователя", example = "3")
        Integer currentRank,
        
        @Schema(description = "Предыдущий рейтинг пользователя", example = "5")
        Integer previousRank,
        
        @Schema(description = "Изменение рейтинга", example = "2")
        Integer rankChange,
        
        @Schema(description = "Топ-3 игроков")
        List<TopPlayerDTO> topPlayers,
        
        @Schema(description = "Позиции рядом с пользователем")
        List<NearbyPlayerDTO> nearbyPlayers
    ) {
        
        @Schema(description = "Информация о топ-игроке")
        public record TopPlayerDTO(
            @Schema(description = "Рейтинг", example = "1")
            Integer rank,
            
            @Schema(description = "ID пользователя", example = "123")
            Long userId,
            
            @Schema(description = "Имя пользователя", example = "player1")
            String username,
            
            @Schema(description = "Счет", example = "1500.0")
            Double score
        ) {}
        
        @Schema(description = "Информация о игроке рядом")
        public record NearbyPlayerDTO(
            @Schema(description = "Рейтинг", example = "2")
            Integer rank,
            
            @Schema(description = "ID пользователя", example = "456")
            Long userId,
            
            @Schema(description = "Имя пользователя", example = "player2")
            String username,
            
            @Schema(description = "Счет", example = "1400.0")
            Double score,
            
            @Schema(description = "Разница в счете с текущим пользователем", example = "100.0")
            Double scoreDifference
        ) {}
    }
    
    /**
     * DTO для достижения
     */
    @Schema(description = "Достижение")
    public record AchievementDTO(
        
        @Schema(description = "ID достижения", example = "ACH_001")
        String id,
        
        @Schema(description = "Название достижения", example = "Первый код!")
        String title,
        
        @Schema(description = "Описание достижения", example = "Введите первый правильный код")
        String description,
        
        @Schema(description = "URL иконки", example = "https://example.com/icon.png")
        String iconUrl,
        
        @Schema(description = "Очки за достижение", example = "50")
        Integer points,
        
        @Schema(description = "Категория достижения", example = "CODES")
        String category,
        
        @Schema(description = "Редкость достижения", example = "COMMON")
        String rarity
    ) {}
    
    /**
     * DTO для метаданных ответа
     */
    @Schema(description = "Метаданные ответа")
    public record ResponseMetadataDTO(
        
        @Schema(description = "Время обработки запроса в миллисекундах", example = "150")
        Long processingTimeMs,
        
        @Schema(description = "Время ответа")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        Instant responseTime,
        
        @Schema(description = "ID сессии", example = "789")
        Long sessionId,
        
        @Schema(description = "ID пользователя", example = "123")
        Long userId,
        
        @Schema(description = "Версия API", example = "v1.0")
        String apiVersion,
        
        @Schema(description = "Дополнительная информация для отладки")
        String debugInfo
    ) {}
}