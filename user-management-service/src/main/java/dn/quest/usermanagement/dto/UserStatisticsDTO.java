package dn.quest.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO для статистики пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Статистика пользователя")
public class UserStatisticsDTO {
    
    @Schema(description = "ID статистики", example = "1")
    private UUID id;
    
    @Schema(description = "ID пользователя", example = "1")
    private UUID userId;
    
    // Общая статистика
    @Schema(description = "Общий счет", example = "15000")
    private Long totalScore;
    
    @Schema(description = "Уровень", example = "15")
    private Integer level;
    
    @Schema(description = "Очки опыта", example = "750")
    private Long experiencePoints;
    
    @Schema(description = "Опыт до следующего уровня", example = "800")
    private Long experienceToNextLevel;
    
    @Schema(description = "Прогресс до следующего уровня в процентах", example = "93.75")
    private Double levelProgress;
    
    // Статистика квестов
    @Schema(description = "Квестов завершено", example = "25")
    private Integer questsCompleted;
    
    @Schema(description = "Квестов начато", example = "30")
    private Integer questsStarted;
    
    @Schema(description = "Квестов брошено", example = "5")
    private Integer questsAbandoned;
    
    @Schema(description = "Общее время игры в минутах", example = "1800")
    private Long totalPlaytimeMinutes;
    
    @Schema(description = "Общее время игры в часах", example = "30.0")
    private Double totalPlaytimeHours;
    
    // Статистика уровней
    @Schema(description = "Уровней завершено", example = "120")
    private Integer levelsCompleted;
    
    @Schema(description = "Кодов решено", example = "85")
    private Integer codesSolved;
    
    @Schema(description = "Подсказок использовано", example = "15")
    private Integer hintsUsed;
    
    @Schema(description = "Попыток сделано", example = "200")
    private Integer attemptsMade;
    
    @Schema(description = "Успешность попыток в процентах", example = "42.5")
    private Double attemptSuccessRate;
    
    // Статистика команд
    @Schema(description = "Команд присоединилось", example = "8")
    private Integer teamsJoined;
    
    @Schema(description = "Команд создано", example = "3")
    private Integer teamsCreated;
    
    @Schema(description = "Команд возглавлено", example = "2")
    private Integer teamsLed;
    
    @Schema(description = "Приглашений отправлено", example = "12")
    private Integer invitationsSent;
    
    @Schema(description = "Приглашений получено", example = "8")
    private Integer invitationsReceived;
    
    // Достижения
    @Schema(description = "Достижений разблокировано", example = "18")
    private Integer achievementsUnlocked;
    
    @Schema(description = "Редких достижений", example = "5")
    private Integer rareAchievements;
    
    @Schema(description = "Легендарных достижений", example = "2")
    private Integer legendaryAchievements;
    
    // Активность
    @Schema(description = "Количество входов", example = "150")
    private Integer loginCount;
    
    @Schema(description = "Текущая серия дней", example = "7")
    private Integer currentStreakDays;
    
    @Schema(description = "Самая длинная серия дней", example = "15")
    private Integer longestStreakDays;
    
    @Schema(description = "Дата последнего входа", example = "2024-01-20T15:45:00Z")
    private Instant lastLoginAt;
    
    @Schema(description = "Дата последней активности", example = "2024-01-20T16:30:00Z")
    private Instant lastActivityAt;
    
    @Schema(description = "Дата первого входа", example = "2024-01-01T12:00:00Z")
    private Instant firstLoginAt;
    
    // Временные метки
    @Schema(description = "Дата создания статистики", example = "2024-01-01T12:00:00Z")
    private Instant createdAt;
    
    @Schema(description = "Дата обновления статистики", example = "2024-01-20T16:30:00Z")
    private Instant updatedAt;
    
    // Вычисляемые поля
    @Schema(description = "Среднее время на квест в минутах", example = "72.0")
    private Double averageTimePerQuest;
    
    @Schema(description = "Среднее время на уровень в минутах", example = "15.0")
    private Double averageTimePerLevel;
    
    @Schema(description = "Процент завершенных квестов", example = "83.33")
    private Double questCompletionRate;
    
    @Schema(description = "Процент успешных кодов", example = "70.83")
    private Double codeSuccessRate;
    
    @Schema(description = "Дней с первого входа", example = "19")
    private Integer daysSinceFirstLogin;
    
    @Schema(description = "Среднее количество входов в день", example = "7.89")
    private Double averageLoginsPerDay;
}